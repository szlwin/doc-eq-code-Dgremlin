# DEC Compiler Design R24 — T06 Raw Invariants Rework

- Revision：`DESIGN-R24@P1-T06-REWORK-I002`
- Task：`TASK-P1-T06 / I002`
- Rework Base：`1247c024b38e1affe35f38671446187df98f5c34`
- Dependency：`COMPLETION-P1-T05-R03@30529276cd8f`
- Superseded Completion：`COMPLETION-P1-T06-R01@90d483290cf3`
- History：R23/R19、R01 及全部 Review/Evidence 不覆盖、不删除

## 1. Rework Findings

- `FND-P1-T06-I002-001`：owner/name/reference lexical token 被静默 trim；
- `FND-P1-T06-I002-002`：公开 RawDefinition 与 RawBuildResult 可构造 R23 禁止状态；
- `FND-P2-T06-I002-003`：空白 reference 未在验证阶段精确定位；
- `FND-P2-T06-I002-004`：public Builder 缺少独立深度/节点安全边界；
- `FND-P2-T06-I002-005`：RawDefinition.toString 未覆盖全部语义字段。

## 2. Lexical 来源事实

T06 不负责 TypedKey 规范化。所有 lexical token 采用“检查与存储分离”规则：

1. 使用 `value.trim().isEmpty()` 判断 null/空白是否合法；
2. 验证通过后保存并返回原始 `value`，不得 trim、大小写转换或其他规范化；
3. RawDefinition.name、ownerToken、attributes、body attributes 与 RawReference.target 必须保持同一原始 token；
4. RULE owner 使用原始 `system + "/" + ruleViewName`；
5. PRODUCE owner 使用原始 `directory + "/" + action`；
6. 可选 PRODUCE name/ref 缺失或纯空白映射为 empty；present 且非空白时保留原值。

该规则覆盖 ROOT_CONFIG、DATA_SOURCE、CONNECTION、DATA、VIEW、SYSTEM、INFORMATION、MODEL_ACCESS、RULE_VIEW、RULE、BUSINESS_SCOPE、DIRECTORY、ACTION、PRODUCE，以及定义自身和后代 lexical scope 的 reference。

## 3. RawDefinition 公开不变量

public 构造器保留，但必须强制完整 Kind/owner/name 矩阵：

| Kind | ownerToken | name |
|---|---|---|
| ROOT_CONFIG | absent | required |
| DATA_SOURCE | required | required |
| CONNECTION | required | required |
| DATA | absent | required |
| VIEW | absent | required |
| SYSTEM | absent | required |
| INFORMATION | required | required |
| MODEL_ACCESS | required | required |
| RULE_VIEW | required | required |
| RULE | required | required |
| BUSINESS_SCOPE | absent | required |
| DIRECTORY | required | required |
| ACTION | required | required |
| PRODUCE | required | optional |

present token 只检查非空白并保留原值。矩阵之外的任何公开构造必须抛出 `IllegalArgumentException`。

## 4. RawBuildResult 公开失败合同

`RawBuildResult.failed(...)` 中每个 Diagnostic 必须同时满足：

- `severity == ERROR`；
- `code == MIX_STRUCTURE_UNKNOWN`；
- `pass == "raw-definition-builder"`；
- 非 null；
- FAILED 不携带 RawDefinitionSet，且 diagnostics 非空。

WARNING、INFO、错误 code 或错误 pass 必须在公开工厂边界拒绝。

## 5. 两阶段 Reference 验证

`validateDocuments` 必须在提取任何 RawDefinition 前验证当前 lexical scope 的全部 reference attribute：

- 白名单属性和 `*-ref`；
- value null 或 trim 后为空时，抛出受控 `RawBuildFailure`；
- messageKey：`raw.reference.target.required`；
- SourceRef：声明该 reference 的当前 Canonical 节点；
- `extractDefinitions` 不承担必填事实发现，只消费已验证输入。

## 6. Builder 独立资源边界

public `RawDefinitionBuilder.build(List<CanonicalDocumentNode>)` 不依赖输入一定来自内建 Frontend。Builder 自身冻结：

- `maxCanonicalDepth = 256`；
- `maxCanonicalNodeCount = 65,536`。

要求：

- 在递归进入子节点前检查下一深度；
- 在任何 RawDefinition、RawReference 或 RawNodeBody 分配前完成整批深度和节点验证；
- 超限返回 `FAILED / MIX_STRUCTURE_UNKNOWN / empty set`；
- messageKey 分别为 `raw.limit.depth`、`raw.limit.node-count`；
- 不捕获 `StackOverflowError`；
- 同包 package-private limits 构造器用于小型 Oracle，不以真实栈溢出验证。

整批验证通过后，后续递归深度最多 256，处于冻结安全边界内。

## 7. 值对象完整表现

RawDefinition 的 `equals`、`hashCode`、`toString` 必须基于同一全部语义字段：

- kind；sourceOrdinal；sourceRef；ownerToken；name；
- attributes；references；body；format；schemaVersion。

RawDefinitionSet.toString 因此能够表现集合内不同 RawDefinition 的语义差异。

## 8. 保持不变

- 六类根 Grammar 和 14 Kind 不变化；
- 定义顺序、sourceOrdinal、父引用嵌套边界不变化；
- attributes、references、body、diagnostics 继续 defensive copy 和不可变；
- reference 仍不解析、不执行 I/O；
- 不修改 Canonical、XML、YAML、Source Graph 或 Context 生产代码；
- 不启动 TypedKey、SymbolTable、Pipeline 或 T07；
- `@Override` 独占一行；公共方法、构造器和关键逻辑使用中文注释；
- PR #21 未经明确授权不得合并。

## 9. 验收

- R24/R20 在 RED 前冻结，后续 blob 不变；
- RED 精确覆盖 5 个 Finding；
- Skeleton 建立 limits、矩阵、diagnostic policy 接缝；
- GREEN 后既有 T06 16 项和 I002 新 Oracle 全绿；
- 五类独立 Review 全部 PASSED；
- Context、Compiler、XML、YAML、Demo、legacy declaration、12 模块 Reactor、Java 8 和故意失败门禁通过；
- 开放 P0/P1/P2 为 0。
