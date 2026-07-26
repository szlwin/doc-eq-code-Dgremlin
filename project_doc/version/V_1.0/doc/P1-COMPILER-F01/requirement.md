# P1-COMPILER-F01 统一 `mix` AST、Registry 与 Compiler 骨架

> Revision：REQCONF-R02-DRAFT / REQAN-R03-DRAFT。状态：已按实际 `mix` fixture 重写，仍需按 common-develop 门禁完成需求确认、分析和串行 Review 后才能进入实施。

## 1. 需求信息

| 项目 | 内容 |
|---|---|
| 需求编号 | P1-COMPILER |
| 变更编号 | P1-COMPILER-CR01 |
| 需求名称 | 统一 `mix` AST、Registry 与 Compiler 骨架 |
| 主责模块 | `dec-core-compiler`（新增） |
| 协作模块 | `dec-core-context`、XML/YAML frontend、`dec-core-starter`、`dec-demo` |
| 唯一目标配置契约 | `mix` |
| 实物 fixture | `dec-demo/src/main/resources/mix` |
| 当前状态 | 调整草案完成，待重新确认与 Review |

## 2. 已确认架构决策

1. `dec-expand-declaration` 是临时项目，P1 整体删除，不抽取代码、不迁移运行时、不建立 Adapter；
2. System、Information、Directory、Action、Produce 直接以 `mix` 配置为后续阶段输入；
3. Business 只是 `BusinessScope` 逻辑作用域，不是独立 Maven 项目，也不拥有第二套 Context、事务器或执行器；
4. P1 发布物命名为 `CompiledModelSet`，不再使用容易形成第二套业务运行时含义的 `CompiledBusiness`；
5. 通用术语统一为 `RawDefinition`，不再使用容易与废弃模块混淆的 `RawDeclaration`；
6. 旧核心读取兼容仅允许 `CoreConfigProjection` 只读投影，不提供 declaration 兼容层。

## 3. 实际 `mix` 输入拓扑

```text
mix/orm-config.xml
  ├─ data file set  -> mix/data/*.xml
  ├─ view file set  -> mix/view/*.xml
  ├─ system file    -> mix/system/systems.xml
  │                    └─ each system -> mix/rule/*.xml
  └─ business file  -> mix/business/order-business.xml
```

详细实物清单见 `DEC_COMPILER_mix_contract_inventory.md`。

## 4. 目标

1. 将配置加载重构为“源图发现 → 格式前端 → RawDefinitionSet → 编译 Pass → CompiledModelSet → EngineContext”；
2. 建立确定性的源发现图，支持根配置、目录文件集、显式文件和 System 间接 Rule 文件；
3. 建立强类型 Key、稳定 Diagnostic、不可变 Registry、显式 DeferredDefinitionRegistry 和稳定 digest；
4. 同一 JVM 可创建多个不可变 EngineContext，互不污染；
5. 以实际 `mix` fixture 证明 Data/View/System/Rule/Business 的结构均能被发现、解析和建立符号；
6. 不提前实现 P2～P7 的业务运行语义。

## 5. 范围

### 5.1 P1 范围内

- 新增 `dec-core-compiler` 并纳入默认 Reactor；
- 整体删除 `dec-expand-declaration` 目录、module、依赖和运行引用；
- `DocumentSource`、`DocumentFrontend`、`CanonicalDocumentNode`、`SourceRef`；
- `MixSourceResolver` 与 `MixSourceGraph`；
- `RawDefinitionSet`，覆盖 datasource、connection、data、view、system、rule-view、business-scope、information、directory、action、produce；
- 强类型 Key、SymbolTable、RegistryBuilder、Diagnostic、CompilationResult；
- P1 可完成的结构和引用编译，以及 P2+ 语义的显式 Deferred Registry；
- `CompiledModelSet`、实例级不可变 `EngineContext`；
- 旧核心 Data/View/Rule 读取的 `CoreConfigProjection`；
- 基于真实 `mix` fixture 的契约测试和残留扫描。

### 5.2 P1 范围外

- P2：System 模型访问权限、RuleView 执行归属和完整复合 Key 语义；
- P3：Information 表达式求值、DAG、物化和失效；
- P4：Action/Produce 执行；
- P5：Directory 状态机、case 查询和 back；
- P6：QueryPlan；
- P7：Session、事务和资源生命周期；
- 完整 XML/YAML 业务对等迁移；
- 生成大量业务 Java 类；
- 任何 `dec-expand-declaration` 兼容包。

## 6. 功能规则

- BR-P1-001：`mix/orm-config.xml` 是 fixture 根入口，但编译器不得硬编码 `dec-demo` 或 `mix` 路径；
- BR-P1-002：源发现必须区分根引用、目录文件集、显式文件和 System 间接 Rule 文件；
- BR-P1-003：目录文件集按标准化 sourceId 排序，文件系统枚举顺序不得影响结果；
- BR-P1-004：发现顺序可以分阶段，语义解析必须允许跨文件前向引用；
- BR-P1-005：RuleViewKey 预留 `(SystemKey, ruleViewName)`，并校验 `<system>` 归属与 RuleView `system` 属性一致；
- BR-P1-006：BusinessScopeKey 只提供命名空间；InformationKey、DirectoryKey 等必须携带 BusinessScopeKey；
- BR-P1-007：P1 完整编译 Data/View 结构和 P1 可验证的 RuleView 符号；System、Information、Directory、Action、Produce 的运行语义进入 Deferred Registry；
- BR-P1-008：DeferredDefinition 必须说明 requiredStage、reason、SourceRef 和已解析的类型引用，禁止静默忽略；
- BR-P1-009：存在 ERROR 时不得发布 `CompiledModelSet` 或 `EngineContext`；
- BR-P1-010：同义输入、稳定选项和版本产生相同 semanticDigest；sourceDigest 单独反映原始内容；
- BR-P1-011：EngineContext 实例级、不可变，不提供全局 current Context；
- BR-P1-012：`CoreConfigProjection` 只读、deprecated，不注册、不删除、不修改事实；
- BR-P1-013：XML 禁止外部实体和网络解析；YAML 禁止任意 Java 类型构造；
- BR-P1-014：`dec-expand-declaration` 不得出现在仓库、Reactor、依赖树、ServiceLoader、反射字符串或发布 artifact 中；
- BR-P1-015：Business 文件必须作为普通配置源解析，不能触发独立模块加载或旧 declaration runtime。

## 7. 编译状态

```text
CREATED
 -> SOURCES_DISCOVERED
 -> PARSED
 -> RAW_BUILT
 -> STRUCTURALLY_VALIDATED
 -> SYMBOLS_REGISTERED
 -> REFERENCES_RESOLVED
 -> GRAPH_PREPARED
 -> SEMANTICALLY_VALIDATED
 -> PUBLISHED | FAILED
```

任何 ERROR 使本次 Session 进入 FAILED；调用方当前使用的旧 EngineContext 保持不变。

## 8. 验收标准

### AC-P1-COMPILER-001 实物源图发现

给定用户提供的 `dec-demo/src/main/resources/mix`，执行 `MixSourceResolver` 后必须发现 10 个 XML 文件，并形成 root→data/view/system/business、system→rule 的有类型边；路径顺序稳定且无硬编码项目目录。

### AC-P1-COMPILER-002 统一前端与 RawDefinitionSet

同义 XML/YAML 最小 fixture 通过同一 Canonical/Raw 契约；真实 XML `mix` 中 5 Data、2 View、3 System、14 RuleView、1 BusinessScope、16 Information、5 Directory、8 Action、4 Produce 均进入 RawDefinitionSet。

### AC-P1-COMPILER-003 强类型符号与引用

Data/View/System/RuleView/BusinessScope/Information/Directory 使用不同 Key；合法前向引用成功解析；同 Key 重复、未知目标和 RuleView System 归属冲突产生稳定 ERROR。

### AC-P1-COMPILER-004 Deferred 边界

System ModelAccess、Information 表达式、Directory 状态机、Action/Produce 执行被显式登记为 Deferred，包含 requiredStage 和 SourceRef；P1 不执行这些语义，也不把它们伪装为已完整编译。

### AC-P1-COMPILER-005 不可变发布与摘要

无 ERROR 时发布不可变 `CompiledModelSet` 和 `EngineContext`；相同输入摘要稳定，两个 Context 互不污染，编译结果不持有 DOM/SnakeYAML 节点。

### AC-P1-COMPILER-006 旧核心只读投影

旧核心 Data/View/Rule 读取可通过 `CoreConfigProjection` 获取与新 Context 一致的只读视图；所有写操作明确拒绝，不创建第二事实源。

### AC-P1-COMPILER-007 临时模块整体退役

删除 `dec-expand-declaration` 后默认 Reactor 和必要测试可通过；仓库、POM、依赖树、服务发现、反射字符串和 artifact 清单无残留；无 `LegacyDeclarationAdapter` 或复制实现。

## 9. 非功能要求

- Java 8 源码兼容；
- 编译器核心不依赖 SQL、MySQL、demo 和前端实现；
- 诊断排序键固定为 sourceId、line、column、code、entityKey、pass；
- 所有发布集合做防御性复制；
- `MixSourceResolver` 阻止路径逃逸、重复 sourceId 和不允许的 URI scheme；
- P1 不设置绝对性能门槛，但必须暴露 discovery/parse/pass/digest 的计时接缝。

## 10. 禁止事项

- 不得重新引入 `dec-expand-declaration`；
- 不得把 BusinessScope 实现为独立模块或第二套 Engine；
- 不得用“忽略未知节点”让 `mix` 看似通过；
- 不得让 Parser 直接写全局 Config；
- 不得在 P1 执行 Information、Directory、Action/Produce 或事务逻辑；
- 不得将 `dec-demo/src/main/resources/mix` 写死在生产代码中。
