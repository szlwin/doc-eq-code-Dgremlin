# DESIGN-R27 — TASK-P1-T07 TypedKey 与两遍 Symbol 注册

- Revision：`DESIGN-R27@P1-T07-I001`
- Status：`PASSED`
- Base：`dev_all@3e0492b0319173c87abff6952d4dad0f5507c31c`
- Dependency：`COMPLETION-P1-T06-R04@242db638c61d`
- Execution：`SEQUENTIAL / auto / architecture_review / git_checkpoint`
- Owner module：`dec-core-compiler`
- Owner component：`dec.core.compiler.symbol`

## 1. 目标

将 T06 的不可变 `RawDefinitionSet` 转换为强类型、稳定有序且只读的 `SymbolTable`，为 T08 前向引用解析准备完整 Symbol 集合。本阶段只登记身份，不解析 `RawReference`、不执行 I/O、不构造运行时对象，也不启动 T08。

## 2. 复用的 Context TypedKey

T01 已冻结以下 11 类 Context Key，T07 必须直接复用，禁止创建平行字符串命名空间：

| RawDefinitionKind | TypedKey |
|---|---|
| `DATA_SOURCE` | `DataSourceKey` |
| `CONNECTION` | `ConnectionKey` |
| `DATA` | `DataKey` |
| `VIEW` | `ViewKey` |
| `SYSTEM` | `SystemKey` |
| `RULE_VIEW` | `RuleViewKey(SystemKey, name)` |
| `BUSINESS_SCOPE` | `BusinessScopeKey` |
| `INFORMATION` | `InformationKey(SystemKey, name)` |
| `DIRECTORY` | `DirectoryKey(BusinessScopeKey, name)` |
| `ACTION` | `ActionKey(DirectoryKey, actionName)` |
| `PRODUCE` | `ProduceKey(ActionKey, sourceOrdinal)` |

`ROOT_CONFIG`、`RULE`、`MODEL_ACCESS` 在已发布 Context 契约中没有对应 TypedKey，本阶段保留为 Raw 事实，不伪造新 Key。后续 Pass 可通过其 owner、body 和 reference 处理。

## 3. 公共结果边界

新增：

- `SymbolBuildStatus`：`BUILT` / `FAILED`；
- `SymbolBuildResult`：成功时仅携带完整 `SymbolTable`，失败时仅携带稳定排序的 Diagnostic；
- `SymbolTable`：包装 Context `ImmutableRegistry<DefinitionKey, RawDefinition>`，只提供 find/require/keys/definitions/size；
- `SymbolTableBuilder`：无状态构建入口。

不变量：

1. BUILT 必须有完整 table 且 diagnostics 为空；
2. FAILED 必须 table 为空且至少一个 ERROR Diagnostic；
3. 所有集合 defensive copy 且不可变；
4. `keys()` 使用 `DefinitionKey.compareTo` 的 canonical 顺序；
5. `definitions()` 与 keys 一一对应并保留原 `RawDefinition.sourceRef()`；
6. 不公开 register、put、replace 或 mutable Map。

## 4. 两遍注册

### 第一遍：顶层与 owner Key

按 `RawDefinitionSet.definitions()` 的稳定 sourceOrdinal 顺序登记：

- 全局顶层：DataSource、Connection、Data、View、System、BusinessScope；
- owner Key：RuleView、Directory、Action。

Business owner 链按 Canonical 先序事实维护：

- BusinessScope 建立当前 scope 并清空 directory/action；
- Directory 必须绑定当前 scope，ownerToken 必须等于 scope name；
- Action 必须绑定当前 directory，ownerToken 必须等于 directory name；
- 每个第一遍 Key 同时绑定其 sourceOrdinal，供第二遍恢复精确 owner 上下文。

### 第二遍：子定义

重新按 sourceOrdinal 顺序扫描同一个不可变 RawDefinitionSet：

- System 通过第一遍 ordinal→Key 映射恢复当前 System；
- Information 必须绑定当前 System，且 ownerToken 必须与 System name 一致；
- BusinessScope、Directory、Action 通过第一遍 ordinal→Key 映射恢复 owner 链；
- Produce 必须绑定当前 Action，ownerToken 必须精确等于 `directory/action`；
- Produce name 可 absent，身份只使用 ActionKey 与该 RawDefinition 的 sourceOrdinal。

两遍完成后才创建 `ImmutableRegistry` 和 `SymbolTable`。任一错误都不发布部分 table。

## 5. 重复与失败语义

登记使用 `TreeMap<DefinitionKey, RawDefinition>`，每次 put 前显式检查，不允许 last-write-wins。

同 TypedKey 重复：

- code：`MIX_SYMBOL_DUPLICATE`；
- severity：`ERROR`；
- messageKey：`symbol.duplicate`；
- definitionKey：重复 Key；
- sourceRef：后出现定义；
- relatedRefs：首定义 SourceRef；
- pass：`symbol-registration`。

不同 TypedKey 类型即使 lexical name 相同也必须共存。

Raw owner 链不完整或 ownerToken 与当前结构不一致时 fail closed：

- code：`MIX_STRUCTURE_UNKNOWN`；
- severity：`ERROR`；
- messageKey：`symbol.owner.context.invalid`；
- sourceRef：当前 RawDefinition；
- 不构造猜测 owner，不跨 scope/system 搜索。

## 6. 确定性与资源边界

- 输入只读取 `RawDefinitionSet` 的不可变有序列表；
- 不访问 parser、filesystem、network、ConfigFactory 或全局 Registry；
- T06 已冻结生产 Canonical 节点上限 65,536，T07 同时在任何 Symbol Map 分配前拒绝 `RawDefinitionSet.size() > 65,536`；
- sourceOrdinal 转换为 ProduceKey int 前必须验证范围；
- Diagnostic 排序复用 Context `Diagnostic.compareTo`；
- 重复运行同一输入必须得到 equals 相同的 SymbolTable、keys 和 SourceRef 序列。

## 7. 测试 Oracle

1. 11 类 Context TypedKey 全映射；
2. DataKey/ViewKey 同名类型隔离；
3. InformationKey owner 必须是对应 SystemKey；
4. 不同 System 下同名 Information 可共存；
5. 不同 BusinessScope 下同名 Directory/Action 可共存并保持 owner 链；
6. 无名 Produce 使用 sourceOrdinal，多个 Produce 不互相覆盖；
7. 同 TypedKey 重复失败，首定义不被覆盖；
8. duplicate Diagnostic 精确绑定重复和首定义 SourceRef；
9. owner 上下文不完整或 token 不匹配 fail closed；
10. keys/definitions 稳定、有序、不可变；
11. 重复构建完全确定；
12. 超过生产 definition 上限在 Map 分配前受控失败；
13. `ROOT_CONFIG/RULE/MODEL_ACCESS` 不产生伪 TypedKey；
14. 不解析 RawReference，不启动 T08。

## 8. Scope

允许：

- `dec-core-compiler/src/main/java/dec/core/compiler/symbol/**`
- `dec-core-compiler/src/test/java/dec/core/compiler/symbol/**`
- `project_doc/version/V_1.0/**` 中 T07 的 Design、Plan、Review、Evidence、Completion 与恢复事实

禁止：

- 修改 `dec-core-context` 生产代码或 TypedKey canonical 规则；
- 修改 T06 Raw 生产合同；
- 实现 ReferenceResolver、Information、ModelAccess、Deferred、Pipeline、Digest、Publication；
- 启动 `TASK-P1-T08`。

## 9. Review

- `REV-000309` — DesignReviewAgent — `PASSED`；
- `REV-000310` — ArchitectureReviewAgent — `PASSED`；
- `EVD-000551`～`EVD-000552`；
- 下一阶段：冻结 R23 实施计划并建立真实 TDD RED。
