# TDD-P1-T13-R03 — I003 Oracle Hardening Evidence

- TDD ID：`TDD-P1-T13-R03@5075793d06cc`
- Iteration：`TASK-P1-T13 / I003`
- Design：`DESIGN-R47@P1-T13-REWORK-I003`
- Plan：`TP-P1-COMPILER-F01-R43@P1-T13-REWORK-I003`
- Code/Test Revision：`5075793d06cc028038d9689f0ca733ecc446e7b0`
- Mode：`ORACLE_HARDENING`
- Production RED：`NOT_APPLICABLE`
- Status：`PASSED`

## Why RED is not applicable

本迭代不新增或修改生产行为，只把 R46 已经规定且当前生产实现已满足的 FAILED Observer 合同冻结为完整回归 Oracle。新增断言预期直接通过；人为制造 production RED 会伪造缺陷或要求无意义的生产改动。

## Negative mutation proof

旧 Oracle 只按 `severity=ERROR + messageKey=test.pass.error` 计数，因此以下变异仍可能通过旧测试：

- 原 ERROR code 改为其他 code；
- Warning subject 与真实 transition 不一致；
- transitions/timings/executedPasses 被删除、重复或重排。

新 Oracle 分别精确验证完整 Diagnostic identity、Warning subject，并使用相同失败位置与确定性 Clock 的 Control/Observed 对照，因此上述任一变异都会使断言失败。

## Validation

- Head：`5075793d06cc028038d9689f0ca733ecc446e7b0`
- P0 Run：`31016766448` — SUCCESS
- Artifact：`8934826368`
- Artifact SHA-256：`13ad7a816de48d7aca33a18996934d41d330e3f41df9742a4c33c9a167926ef6`
- `CompilationObserverIndependentReviewTest`：5/5；
- 正常测试：606/606；
- intentional failure：1；
- Errors/Skipped：0/0。
