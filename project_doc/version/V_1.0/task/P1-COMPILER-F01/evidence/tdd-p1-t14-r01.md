# TDD-P1-T14-R01 — Candidate Context TDD Evidence

- TDD：`TDD-P1-T14-R01@f0f76facdd76`
- Task：`TASK-P1-T14 / I001`
- Evidence：`EVD-001050`～`EVD-001054`

## Valid RED

- Head：`f0f76facdd76d626cd82859ef8413964ae1b6fdf`
- P0 Run：`31021944964`
- Artifact：`8936970743`
- SHA-256：`f9e5259bb29a11f7ebf23637f3541df0f82485af10a2dc6953b7e89c939ccc5e`
- Result：`FAILURE / EXPECTED_RED`

生产和测试均成功编译。`CandidateContextT14Test` 共 4 项：

- 2 项 Failure；
- 1 项 Error；
- 1 项 missing-input 控制项通过。

失败均由 `TASK-P1-T14 candidate context builder is not implemented` 或最终状态仍为 FAILED 引起。Compiler 其余 486 项测试全部绿色，不依赖 testCompile 失败或无关回归。

## Review RED

独立 Review 新增 `keys()/size()` 不一致反例：

- Head：`a494fa37574f7ae37362421d15e4f6a175ff6091`
- P0 Run：`31023013154`
- Artifact：`8937412168`
- SHA-256：`28448029b7f95dee776129bbf8c6fd521856d5dc489bd37f25d0a59c37c9ed99`
- Compiler：498 tests / 1 expected failure / 0 errors。

唯一失败为 `inconsistentRegistrySizesFailClosed`，用于证明原实现可能静默接受不完整 Registry 枚举。
