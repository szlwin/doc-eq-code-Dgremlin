# TESTING-P1-T14-R03 — I003 Testing Evidence

- Evidence ID：`EVD-001095`
- Testing：`TESTING-P1-T14-R03@37fb814b39c5`
- Code/Test Revision：`37fb814b39c54e6260fd65d13cb31e817bc0fe92`
- Status：`PASSED`

## Validation run

- P0 Run：`31073434459` — SUCCESS；
- Artifact：`8956534261`；
- GitHub digest：`sha256:3266e2b475bbcdf0f6dc24b3de097c84efbc40853ae77bec8432e6feaa7207e5`；
- 独立 ZIP SHA-256：`3266e2b475bbcdf0f6dc24b3de097c84efbc40853ae77bec8432e6feaa7207e5`。

## Normal Surefire parse

- Surefire XML：109；
- All records：625；
- Normal passed：624；
- Intentional failure：1；
- Errors：0；
- Skipped：0；
- T14：18/18；
- T13：34/34；
- T12：133/133；
- Compiler：504/504。

T14 suites：

- `CandidateContextT14Test`：5/5；
- `CandidateContextT14I002RedTest`：2/2；
- `CandidateContextT14IndependentReviewTest`：11/11。

唯一正常报告 Failure 为 `dec.core.context.gate.P0IntentionalFailureTest.mustFail`，用于证明失败测试阻断构建，Workflow 已按预期识别。

## Mutation proof parse

Artifact 中独立保存：

- `request-binding.xml/json/log`；
- `source-closure.xml/json/log`；
- `restored-request-binding.xml`；
- `restored-source-closure.xml`；
- `restored-green.json/log`；
- `summary.json`。

解析结果：

- REQUEST_BINDING_BYPASS：1 test / 1 assertion failure / 0 error / 0 skipped；
- SOURCE_CLOSURE_BYPASS：1 test / 1 assertion failure / 0 error / 0 skipped；
- 两个 Maven status 均为 1；
- 恢复后两个目标测试各 1/1 PASSED；
- `fullSurefireReportsRestored=true`；
- mutation proof result：`PASSED`。

## Build gates

- 12 modules Reactor：PASSED；
- Java release 8：PASSED；
- T14 provenance mutation gate：PASSED；
- intentional failure gate：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## Independent verification

Artifact 已独立下载、计算 SHA-256、解析正常 Surefire XML 和 mutation proof XML/JSON。首版 harness 覆盖完整报告的问题已修复，最终 Artifact 的正常统计与完整 `clean verify` 一致。
