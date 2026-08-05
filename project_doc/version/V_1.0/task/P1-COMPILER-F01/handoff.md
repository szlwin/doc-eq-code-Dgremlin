# P1-COMPILER-F01 阶段交接

> T01～T12 已合并到 `dev_all@659fb74563bbe1fa1daaf4d3a0e868f702daaec6`。TASK-P1-T13 / I001 已完成，当前有效 Completion 为 `COMPLETION-P1-T13-R01@74672ee1367b`。PR #28 尚未合并，T14 保持阻断。

## T13 I001

- Base：`dev_all@659fb74563bbe1fa1daaf4d3a0e868f702daaec6`
- Dependency：`COMPLETION-P1-T12-R07@74f402287bc4`
- Branch：`feature/p1-t13-semantic-digest-20260805-2005`
- PR：`#28 / OPEN / READY_FOR_REVIEW`
- Design：`DESIGN-R45@P1-T13-I001`
- Plan：`TP-P1-COMPILER-F01-R41@P1-T13-I001`
- TDD：`TDD-P1-T13-R01@4f3d444f779f`
- Architecture：`DEVSKEL-P1-T13-R01@4f3d444f779f`
- Development：`DEV-P1-T13-R01@74672ee1367b`
- Code Review：`CODEREVIEW-P1-T13-R01@74672ee1367b`
- Testing：`TESTING-P1-T13-R01@74672ee1367b`
- Completion：`COMPLETION-P1-T13-R01@74672ee1367b`
- Reviews：`REV-000653`～`REV-000671`
- Evidence：`EVD-001003`～`EVD-001018`
- Open P0/P1/P2：`0 / 0 / 0`

## Current contract

- `DEC-SEMANTIC-DIGEST-V1` 输入为不可变 Published Source、Definition、Deferred 与版本域；
- sourceDigest 使用 Source ID/原始内容长度前缀 SHA-256；
- semanticDigest 使用 UTF-8 canonical JSON SHA-256；
- canonical JSON 的 Object key 使用 Unicode code point 顺序；
- SourceRef line/column、Source format/content digest、Timing、Observer、DigestPair 与 Publication 状态不进入 semantic digest；
- 完整成功 Timing 为 PASS=10、DISCOVERY=1、PARSE=1、DIGEST=1；
- supplemental timing 复用原 elapsed，不增加 Clock 读取；
- Observer failure 转为非阻断 `MIX-OBSERVER-FAILURE / WARNING`；
- PUBLISHED/FAILED、artifact、Context、publisher 次数和 digest 不受 Observer 失败影响；
- T12 的 Deadline、Cancel、Clock overflow、prepare/commit 与 commit-wins 保持；
- T14/T15 与 P2～P7 runtime 未实现。

## Validation

- Valid RED：`4f3d444f779f...` / Run `31005889102` / 11 expected failures / 2 controls / 0 errors；
- First GREEN：`44aaa9767840...` / Run `31007497348` — SUCCESS；
- Production：`65f96c71ae0560f375d402b586125ad4879dde4b`；
- Code/Test：`74672ee1367bab9de75b4028cd4578b6118f96f0`；
- Clean validation：`eadeeffba4a9...` / Run `31008161016` — SUCCESS；
- Artifact：`8931238649`；SHA-256：`57c6b57716f52e0c86ace7daf221fb51b8c88a5c7af5e2396a8d690c9f4dfed4`；
- T13 `25/25`；T12 `133/133`；Compiler `477/477`；正常测试 `597/597`；Surefire XML `105`；
- Java 8、12 modules、intentional failure gate：PASSED；MySQL：`SKIPPED_NOT_APPLICABLE`。

## Recovery and next step

- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t13-r01/completion-report.json`
- Review：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t13-r01.md`
- Revision Lock：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/revision-lock-p1-t13-r01.json`
- Machine checkpoint：`project_doc/version/V_1.0/tdd_p1_t13_r01_completion.json`
- 未经用户明确授权不得合并 PR #28；
- PR #28 合并前 `TASK-P1-T14` 保持 `BLOCKED_UNTIL_PR_28_MERGE`。
