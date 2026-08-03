# DESIGN-R29 — TASK-P1-T08 P1 强类型引用解析

- Revision：`DESIGN-R29@P1-T08-I001`
- Status：`PASSED`
- Base：`dev_all@c6cd8ec156563480ec30989cdd358d4979a8599b`
- Dependency：`COMPLETION-P1-T07-R02@ffe544e3060d`
- Branch：`feature/p1-t08-reference-resolution-20260803-2254`
- Target PR：`#23`
- Execution：`SEQUENTIAL / auto / architecture_review / git_checkpoint`
- Owner module：`dec-core-compiler`
- Owner component：`dec.core.compiler.symbol.ReferenceResolver`

## 1. 目标

在 T07 已发布完整 `SymbolTable` 后，以不可变 `RawDefinitionSet + SymbolTable` 为输入，完成 P1 可确定引用的精确绑定。所有目标必须通过 Context 已发布的 TypedKey 查询，不使用名称模糊搜索、跨类型降级或发现顺序猜测。

成功结果发布完整不可变 `ResolvedReferenceSet`；任一 ERROR 时只发布稳定排序 Diagnostic，不暴露部分解析结果。

## 2. 输入与前置不变量

- RawDefinitionSet 已由 T06 完成结构与 lexical 冻结；
- SymbolTable 已由 T07 完成 11 类 TypedKey 注册；
- RawDefinition、RawReference 的 lexical token 不改写；
- Resolver 必须先观察完整 SymbolTable，再解析全部引用，因此天然支持跨文件前向引用；
- sourceOrdinal 只用于恢复来源定义 Key，不参与目标名称猜测。

## 3. 本轮解析矩阵

### 3.1 Connection

- `CONNECTION` 的 `/data-source-info/data-source@ref` → `DataSourceKey(target)`；
- 目标不存在但同名其他类型存在时，产生 `reference.type.mismatch`；
- 完全不存在时产生 `reference.unknown`。

### 3.2 View

- `VIEW.attributes["target-main"]` → `DataKey(target-main)`；
- View body 中每个 `data` 引用 → `DataKey(target)`；
- `ref-property` 不建立平行 PropertyKey。Resolver 在当前 View 已绑定的 Data 定义 body 中按区分大小写完整名称校验属性；成功的属性事实仍绑定到对应 `DataKey`，并由 `ResolvedReference.role/targetToken` 保留具体 lexical；
- 不跨 Data 搜索 property，不做前缀、后缀、大小写或 root-property 降级。

### 3.3 System

- System `/data-info/data-ref@ref|@name` → `DataKey`；
- System `/view-info/view-ref@ref|@name` → `ViewKey`；
- Resolver 建立 `SystemKey → declared ViewKey` 的只读声明边界，供 RuleView 校验；
- Information 表达式、Information 的 view/rule 绑定留给 T09，不在 T08 解析。

### 3.4 RuleView 与 Rule 文件归属

- `RULE_VIEW.ownerToken` → `SystemKey`；
- `RULE_VIEW @view-ref` → `ViewKey`；
- 目标 View 必须同时出现在该 System 的 `view-info` 声明集合；否则产生 `MIX_REF_RULE_SYSTEM_MISMATCH / reference.rule-system.mismatch`；
- RuleView 可以位于 System 文档之前或之后；解析不依赖发现顺序；
- Rule 内部 `property` 与运行时 Rule 语义不在 T08 执行。

### 3.5 Business Action

- `ACTION @system-ref` → `SystemKey`；
- 同一 Action 的 `@rule-ref` → `RuleViewKey(resolved SystemKey, target)`；
- Action 缺失 system-ref、目标 RuleView 不属于指定 System 或错误绑定到同名其他 System 时，产生 `MIX_REF_RULE_SYSTEM_MISMATCH`；
- 没有 system-ref/rule-ref 的自定义 Action 保持无引用事实，不失败。

### 3.6 Directory

- `DIRECTORY @information-ref` 与其 body 内同类引用 → `InformationKey(SystemKey(qualified system), name)`；
- `DIRECTORY ...@rel` → 当前 `BusinessScopeKey` 下的 `DirectoryKey`；
- Information target 必须是两段 `system.name`，禁止未限定名称；
- `model-ref` 留给 T10/P5，不在 T08 解析。

### 3.7 Produce

- `PRODUCE @information-ref` → `InformationKey`；
- `PRODUCE @ref` 是后续 Produce/模型输出语义，T08 不猜测为 View/Data；
- 无 information-ref 的 Produce 可以在 T08 保持无解析引用，由后续 P4 分类。

## 4. 结果模型

新增 Java 8 不可变类型：

- `ReferenceResolutionStatus { RESOLVED, FAILED }`；
- `ResolvedReference`：`sourceKey, role, targetToken, targetKey, sourceRef`；
- `ResolvedReferenceSet`：按 `ResolvedReference.compareTo` 冻结的完整集合；
- `ReferenceResolutionResult`：成功携带完整集合且无 Diagnostic，失败携带 Diagnostic 且不携带部分集合；
- `ReferenceResolver`：无状态、线程安全、无 I/O、无 static mutable registry。

`ResolvedReference` 排序固定为：SourceRef → sourceKey canonical/type → role → targetKey canonical/type → targetToken。

## 5. Diagnostic 合同

Pass 固定为 `reference-resolution`，severity 固定为 ERROR：

- `MIX_REF_UNKNOWN / reference.unknown`：期望 TypedKey 不存在且没有同名其他类型；
- `MIX_REF_UNKNOWN / reference.type.mismatch`：期望类型不存在，但 SymbolTable 中存在同 lexical 的其他 TypedKey；
- `MIX_REF_UNKNOWN / reference.property.unknown`：目标 Data 存在，但属性不存在；
- `MIX_REF_UNKNOWN / reference.owner.invalid`：限定目标或结构 owner 不完整；
- `MIX_REF_RULE_SYSTEM_MISMATCH / reference.rule-system.mismatch`：RuleView、System 声明 View、Action system/rule 归属冲突。

所有 Diagnostic 使用 LinkedHashSet 去重，完整扫描后由 `Diagnostic.compareTo` 稳定排序。首个错误不得短路后续引用。

## 6. 资源与安全边界

- 单次 build 不创建全局缓存；
- 解析次数受 RawReference/Definition 数量已有上限约束；
- 精确 Map/Set 查询，平均 O(1) 或 TreeMap O(log n)；
- 不执行文件 I/O、classpath 查找、反射加载、规则运行或表达式求值；
- 不解析网络、文件、SQL 或用户代码；
- 不发布失败中的部分引用集合。

## 7. 明确排除

本轮不得实现：

- T09 Information expression AST、common 跨 System 和 Information 求值；
- T10 ModelAccess selector、property path fallback 与读写冲突；
- Rule property 运行时语义；
- Produce 模型输出和 P4 执行；
- P2～P7 Deferred 分类、Pipeline、Digest、Publication；
- PropertyKey 等未冻结的平行 Key；
- 模糊匹配、大小写降级、前后缀搜索或 first-match。

## 8. 测试 Oracle

1. connection 精确绑定 DataSourceKey；
2. View target-main/data/ref-property 精确校验并绑定 DataKey；
3. System Data/View 声明精确绑定；
4. RuleView 前向顺序、System owner 与 declared View 成功；
5. RuleView 指向未声明 View 时稳定 mismatch；
6. Action system/rule 精确绑定，跨 System 同名 RuleView 不得误用；
7. Directory qualified Information 与同 Scope subdirectory 精确绑定；
8. Produce information-ref 精确绑定；
9. unknown 与 type mismatch 使用不同 messageKey；
10. 多错误完整收集、稳定排序、重复运行一致；
11. 失败不发布部分 ResolvedReferenceSet；
12. T07 Symbol 32 项与 Compiler 全量继续通过；
13. `@Override` 独占一行，方法和重要逻辑使用中文注释。

## 9. Review

- `REV-000339` — DependencyGateReview — `PASSED`；
- `REV-000340` — DesignReviewAgent — `PASSED`；
- `REV-000341` — ArchitectureReviewAgent — `PASSED`；
- Evidence：`EVD-000586`～`EVD-000588`；
- 下一阶段：冻结 R25 实施计划，再建立可编译 TDD seam 与有效 RED。
