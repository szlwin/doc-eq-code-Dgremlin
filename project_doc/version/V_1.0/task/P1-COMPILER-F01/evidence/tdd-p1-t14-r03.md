# TDD-P1-T14-R03 — I003 TDD Repair Evidence

- Evidence ID：`EVD-001093`
- TDD：`TDD-P1-T14-R03@37fb814b39c5`
- Iteration：`TASK-P1-T14 / I003`
- Mode：`TDD_REPAIR / ORACLE_HARDENING`
- Code/Test Revision：`37fb814b39c54e6260fd65d13cb31e817bc0fe92`
- Status：`PASSED`

## Historical invalidation

`TDD-P1-T14-R02@1df0a14f2a74` 已标记为 `INVALIDATED / PRESERVED`。对应 Run `31068551065` 在 `maven-compiler-plugin:testCompile` 阶段失败，Compiler 测试未执行，不能作为行为 RED。原 Git 历史、Artifact 和证据文件均保持不变。

## Repair strategy

当前正确生产实现已经存在，因此 I003 不回写历史、不伪造“生产代码之前的 RED”。本轮采用可复现 mutation proof：

1. 完整 `clean verify` 先证明当前 Head 正常 GREEN；
2. CI 临时短路 request schema/options 门禁；
3. 执行 `CandidateContextT14Test#requestMismatchFailsWithExactDiagnostic`；
4. CI 临时跳过 raw/published Source identity 闭包门禁；
5. 执行 `CandidateContextT14IndependentReviewTest#sourceManifestClosureMismatchFailsClosed`；
6. 每个 mutation 必须产生 1 个已执行的 assertion failure、0 error；
7. 恢复正确源码后两个目标测试必须各 1/1 GREEN；
8. mutation 代码不得进入 Git，完整日志、XML 和 JSON 摘要进入 Artifact。

## Mutation proof

### REQUEST_BINDING_BYPASS

- Test：`CandidateContextT14Test#requestMismatchFailsWithExactDiagnostic`；
- Tests：1；
- Failures：1；
- Errors/Skipped：0/0；
- Failure type：`org.opentest4j.AssertionFailedError`；
- Maven status：1；
- Classification：`BEHAVIOR_ASSERTION_FAILURE`。

该 mutation 使 request mismatch candidate 越过 provenance 门禁，现有 Oracle 因最终状态、publisher 次数、artifact 或 Diagnostic 不符合合同而失败。

### SOURCE_CLOSURE_BYPASS

- Test：`CandidateContextT14IndependentReviewTest#sourceManifestClosureMismatchFailsClosed`；
- Tests：1；
- Failures：1；
- Errors/Skipped：0/0；
- Failure type：`org.opentest4j.AssertionFailedError`；
- Maven status：1；
- Classification：`BEHAVIOR_ASSERTION_FAILURE`。

该 mutation 使 raw/published Source identity 不一致的输入不再 fail-closed，现有 Oracle 因未抛出预期异常而失败。

## Restored GREEN

恢复正确生产源码后：

- request mismatch 目标测试：1/1 PASSED；
- Source closure 目标测试：1/1 PASSED；
- Failures/Errors/Skipped：0/0/0；
- 完整 `CandidateContextT14Test` 5 项和 `CandidateContextT14IndependentReviewTest` 11 项 Surefire XML 已恢复，未被定向重跑覆盖。

## Validation

- P0 Run：`31073434459` — SUCCESS；
- Artifact：`8956534261`；
- SHA-256：`3266e2b475bbcdf0f6dc24b3de097c84efbc40853ae77bec8432e6feaa7207e5`；
- Mutation summary：`result=PASSED / fullSurefireReportsRestored=true`；
- 正常 Compiler 测试：504/504；
- 正常全量记录：625，其中 624 通过、1 项 intentional failure；
- Errors/Skipped：0/0。

## Gate

I003 已提供真实、可编译、测试实际执行的行为负向证明。`FND-P1-T14-I003-001` 的 TDD/Evidence Gate 已关闭。
