# DEC Compiler Implementation Plan R20 — T06 Raw Invariants Rework

- Revision：`TP-P1-COMPILER-F01-R20@P1-T06-REWORK-I002`
- Design：`DESIGN-R24@P1-T06-REWORK-I002`
- Task：`TASK-P1-T06 / I002`
- PR：`#21`

## 1. 顺序

1. PR #21 转回 Draft；
2. 保留 I001 R23/R19/R01 和全部历史；
3. 在 RED 前创建并锁定 R24/R20；
4. 提交 lexical、public invariant、reference validation、limits、toString Oracle；
5. 以 P0 证明新增 Oracle 为行为 RED，既有测试不回归；
6. Architecture Skeleton 建立 `RawBuilderLimits`、Kind matrix 与 failed diagnostic policy；
7. Development 实现 lexical 保留、验证前置、深度/节点门禁、全字段 toString；
8. 执行 Specification、Architecture、Security、Code、TDD 独立 Review；
9. 全量 Testing、Completion R02、handoff、resume、机器 checkpoint；
10. 最终文档化 Head 独立 P0，通过后更新 PR #21 并恢复 Ready for review，不合并。

## 2. RED Oracle

新增测试覆盖：

- 七类直接 name 定义及 ROOT_CONFIG/BUSINESS_SCOPE 的首尾空白；
- owner context：DATA_SOURCE、CONNECTION、INFORMATION、MODEL_ACCESS、RULE_VIEW、RULE、DIRECTORY、ACTION、PRODUCE；
- definition 自身 reference 与后代 reference 原值；
- name/owner/attributes/body/reference target 一致；
- 14 Kind public constructor 正向矩阵；
- 14 Kind owner/name 缺失或多余的负向矩阵；
- `RawBuildResult.failed` 对错误 code、WARNING/INFO、错误 pass 的拒绝；
- 空白 reference 在第一阶段以当前节点 SourceRef 失败；
- 小型注入 depth/node limits 的边界内成功和超限失败；
- `RawDefinition.toString` 能区分 attributes/references/body/format/schemaVersion 差异。

## 3. Skeleton

- 新增 package-private final `RawBuilderLimits`；
- 生产默认深度 256、节点数 65,536；
- Builder public 构造使用生产 limits，同包构造允许注入小预算；
- RawDefinition 添加集中矩阵校验方法；
- RawBuildResult 添加 diagnostic contract 校验方法；
- Skeleton 不得伪造完整 Builder 成功行为。

## 4. Development

### 4.1 Lexical

- 将 `attribute`、`optionalAttribute`、`requiredOptional`、`required` 改为只用 trim 判断、返回原值；
- RawDefinition optional 和 schemaVersion 的非空白校验返回原值；
- RawReference role/target 非空白校验返回原值；
- composite owner 使用未规范化组件；
- attributes 和 body 保持原值。

### 4.2 Public invariants

- RawDefinition 构造完成 optional defensive copy 后执行 Kind matrix；
- RawBuildResult.failed 对每个 Diagnostic 校验 ERROR、MIX_STRUCTURE_UNKNOWN、固定 pass；
- 失败 diagnostics 仍排序并冻结。

### 4.3 Validation / Limits

- validateDocuments 创建 `ValidationBudget`；
- 根深度为 1，每进入节点前预留 node count 并检查深度；
- reference lexical 验证与 definition facts 在同一第一阶段执行；
- 第二阶段只提取已验证值；
- 不捕获 Error。

### 4.4 toString

RawDefinition.toString 输出所有 equals/hashCode 语义字段，使用稳定字段名。

## 5. Review 与 Testing

- 复核 R24/R20 blob 从 RED 前到最终 Head 不变；
- 复核无 parser 类型、无 I/O、无全局 registry；
- 复核所有递归都只能在已验证最大 256 深度内执行；
- 复核 `@Override` 独行和中文注释；
- 运行 P0 workflow，记录 Run、Artifact、SHA-256 和 Surefire 计数；
- MySQL 仅记录 `SKIPPED_NOT_APPLICABLE`。

## 6. Completion

创建 `COMPLETION-P1-T06-R02@<clean-code-head>`。明确 R01 被新独立 Review 推翻但不可变保留；开放 P0/P1/P2 必须为 0；T07 保持未启动。
