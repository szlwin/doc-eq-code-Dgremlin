# TASK-P1-T07 R02 Development Evidence

- TDD：`TDD-P1-T07-R02@619714e24fd5`
- Rejected Skeleton：`DEVSKEL-P1-T07-R02-A01@15f6e0e8ef9b`
- Accepted Architecture：`DEVSKEL-P1-T07-R02@ffe544e3060d`
- Development：`DEV-P1-T07-R02@ffe544e3060d`
- Code Review：`CODEREVIEW-P1-T07-R02@ffe544e3060d`

## 有效 RED

- Head：`619714e24fd5e37fc186897485aef1f9039c6209`
- P0 Run：`30818564155`
- Artifact：`8857812598`
- SHA-256：`d584ce9118363c6cb36d39812d0de7b8b6a733e668a0d92b791a1c46aa50c163`
- I002：9 failures / 0 errors；
- R01 Symbol：23/23 PASSED；
- Java release 8 与既有 Context/Compiler 编译：PASSED。

RED 精确覆盖：

1. padded lexical owner 被 trim 后 Key name 错误比较；
2. RuleView 位于 System 前、指向非最近 System、多 System owner；
3. Diagnostic 线性扫描步骤 15≠6、1≠2。

## Skeleton 历史

`15f6e0e8ef9b238e8f4936c453f8bd5ca991966f` 已分离 lexical/key 上下文并延迟 RuleView，但显式 `not-implemented` 导致 4 项既有 RuleView 合同回退。该 attempt 被 Architecture Review 拒绝并不可变保留：

- Run：`30818790734`
- Artifact：`8857911802`
- SHA-256：`b485ecfc40d33a685ca6ac0cc8d8a5a12319ad6838f692fbe0e595171f62f288`
- Result：11 failures / 0 errors

## 最终实现

- `OwnerContext` 同时保存 canonical Key 与 Raw lexical name；
- Information、Directory、Action、Produce 使用原始 lexical parent 校验；
- RuleView 延迟到所有 System 登记后，按自身 ownerToken 构造 SystemKey 并校验存在；
- missing System 产生 `MIX_STRUCTURE_UNKNOWN / symbol.owner.system.missing`；
- DiagnosticAccumulator 使用 LinkedHashSet，每次报告仅一次 Set.add；
- 无效 Directory/Action/System 上下文会产生 Diagnostic 或清理状态，禁止陈旧 owner 泄漏；
- 任一失败不发布部分 SymbolTable；
- RawDefinition lexical 不改写，RawReference 不解析；
- 所有新增方法和重要逻辑使用中文注释，未新增不合规的同行 `@Override`。

## Findings

- `FND-P1-T07-I002-001`：CLOSED；
- `FND-P1-T07-I002-002`：CLOSED；
- `FND-P1-T07-I002-003`：CLOSED。

## Review

- `REV-000328` TDD Review — PASSED；
- `REV-000329` Skeleton A01 Review — REJECTED；
- `REV-000330` Accepted Architecture Review — PASSED；
- `REV-000331` Engineering Review — PASSED；
- `REV-000332` Architecture Review — PASSED；
- `REV-000333` Security Review — PASSED；
- `REV-000334` TDD Review — PASSED；
- `REV-000336` Code Review — PASSED；
- Evidence：`EVD-000571`～`EVD-000580`。
