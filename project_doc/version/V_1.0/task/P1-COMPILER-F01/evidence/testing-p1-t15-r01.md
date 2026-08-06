# TESTING-P1-T15-R01 — Starter 与 Declaration 退役验证

- Evidence ID：`EVD-001105`
- Testing：`TESTING-P1-T15-R01@f36b03e6243`
- Code/Test Revision：`f36b03e6243f6e3c9d2f5b2ffce7cf4b1fd63eb3`
- Status：`PASSED`

## Validation run

- P0 Run：`31083267905` — SUCCESS；
- Artifact：`8960370768`；
- GitHub digest：`sha256:ea2c919cbacfead831a5d137894991b09b7a2163f0616c9bc47f99505db517b3`；
- 独立 ZIP SHA-256：`ea2c919cbacfead831a5d137894991b09b7a2163f0616c9bc47f99505db517b3`。

## Surefire independent parse

- Surefire XML：110；
- All records：633；
- Normal passed：632；
- Intentional failure：1；
- Errors：0；
- Skipped：0。

关键模块：

- T15：10/10；
- Starter：10/10；
- Compiler：504/504；
- Context：26/26 正常通过，另 1 项 intentional failure；
- XML frontend：30/30；
- YAML frontend：59/59；
- Demo：3/3。

T15 suites：

- `StarterRetirementT15Test`：3/3；
- `CompilerStarterBehaviorT15Test`：3/3；
- `CompilerStarterBehaviorT15IndependentReviewTest`：4/4。

唯一 Failure 为 `dec.core.context.gate.P0IntentionalFailureTest.mustFail`，Workflow 已按预期证明失败测试会阻断普通构建。

## Retirement proof

Artifact 中独立解析结果：

- baseline retirement summary：`PASSED / 0 violations`；
- source scan：无旧 declaration 残留；
- artifact scan：无旧 declaration 残留；
- current artifact match：无不一致；
- mutation run：按预期 `FAILED`，检测到旧 module 与旧 package/source 回流；
- mutation proof：`PASSED`；
- detected categories：`MODULE / SOURCE`；
- cleanup 后 restored baseline：`PASSED`。

## Build gates

- 默认 Reactor：PASSED；
- Java release 8：PASSED；
- T14 provenance mutation gate：PASSED；
- T15 declaration retirement mutation gate：PASSED；
- intentional failure gate：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

Artifact 已独立下载、计算 SHA-256，并解析全部 Surefire XML、retirement summary、mutation summary 与 mutation proof。Testing 结论为 `PASSED`。
