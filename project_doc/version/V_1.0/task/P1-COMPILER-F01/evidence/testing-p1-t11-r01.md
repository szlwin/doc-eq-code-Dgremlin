# TASK-P1-T11 R01 Testing Evidence

- Testing：`TESTING-P1-T11-R01@f09d9786fad8`
- Evidence：`EVD-000755`～`EVD-000759`

## First GREEN

- Head：`daa1f2b709a10955351e62e3fa7aa973d73dcd12`
- P0 Run：`30913850792` — SUCCESS
- Artifact：`8894204525`
- SHA-256：`5472615ce64e317fd8109fee1317503762ae68de811e39b76103b3b8975db441`

## Independent Review

- `150591bd8c78...` / Run `30914001720`：1 个 Review Oracle 因 JaCoCo synthetic `$jacocoData` 误判而失败；生产测试全部绿色，作为 rejected test attempt 保留。
- 修正仅忽略 instrumentation synthetic 字段；Head `edf14730c099...` / Run `30914170907` — SUCCESS。

## Clean-code validation

- Head：`f09d9786fad8974bdbe8c37704d44ee4466da862`
- P0 Run：`30914377427` — SUCCESS
- Artifact：`8894415605`
- SHA-256：`702bd6c66b0debfaca9c7dd91c6b00baf971e114779d4c252f014ba867cfa315`
- Surefire XML：`81`
- T11：`26/26`
- Compiler module：`311/311`
- XML：`30/30`；YAML：`59/59`；Context 正常：`26/26`；Demo：`4/4`；Legacy：`1/1`
- 正常测试：`431/431`
- 故意失败门禁：`1` 项按预期失败并被识别
- Errors / Skipped：`0 / 0`
- 12 模块 Reactor、Java release 8：`PASSED`
- MySQL：`SKIPPED_NOT_APPLICABLE`

Artifact 已独立下载解析，实际 ZIP SHA-256 与 GitHub digest 完全一致。
