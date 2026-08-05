# EVD-000995 — TESTING-P1-T12-R07

- Testing：`TESTING-P1-T12-R07@74f402287bc4`
- Status：`PASSED`

## First GREEN

- Head：`2da699060a4bb596c612a7b26fa022fcb6474a4d`
- P0 Run：`31000726214` — `SUCCESS`
- Artifact：`8928131274`
- SHA-256：`67856fa28719e6448bb583eeb3d0b2fba2818ee04862eff52f11dc7f622cf25b`
- I007 direct Oracle：`6/6`

## Independent Review invalid attempt

- Head：`f8125c2abf80c8edd77641128e5c8df0362ccb0a`
- P0 Run：`31000871729` — `FAILURE / INVALID_REVIEW_FIXTURE`
- Artifact：`8928189764`
- SHA-256：`5d2c14edb47639fbb9efaae6a013bd82ca8516f6e1324c9f2d29c87c9b53949c`
- Result：I007 Independent Review `10 tests / 8 pass / 2 fixture errors`；两项错误均因测试 key 未实现既有 `ImmutablePipelineArtifact` snapshot 合同，未进入 collision comparison；不作为 Review GREEN。

## Clean-code validation

- Head：`74f402287bc4968dae3221848a91d968ecad0698`
- P0 Run：`31000986498` — `SUCCESS`
- Artifact：`8928238806`
- SHA-256：`7d0a8c38c9d93df547ced820b3bf5ebdc964307bfc1032aeb48cf10cc12f19b5`
- Surefire XML：`100`
- I007 direct：`6/6`
- I007 independent：`10/10`
- I007 total：`16/16`
- T12 total：`133/133`
- Compiler module：`452/452`
- 正常测试：`572/572`
- 全部测试记录：`573`
- Intentional failure gate：`1` 项按预期失败并被识别
- Errors / Skipped：`0 / 0`
- 12 modules Reactor：`PASSED`
- Java release 8：`PASSED`
- MySQL：`SKIPPED_NOT_APPLICABLE`

Artifact 已独立下载并解析，ZIP SHA-256 与 GitHub digest 完全一致。
