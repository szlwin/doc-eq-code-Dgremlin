# P1-COMPILER-F01 阶段交接

> T01～T12 已合并到 `dev_all@659fb74563bbe1fa1daaf4d3a0e868f702daaec6`。TASK-P1-T13 / I002 已完成，当前有效 Completion 为 `COMPLETION-P1-T13-R02@7d39c3bc0ab4`。R01 已失效但历史不可变保留。PR #28 尚未合并，T14 保持阻断。

## Completion history

- R01 / I001：`COMPLETION-P1-T13-R01@74672ee1367b` — `INVALIDATED / PRESERVED`；
- R02 / I002：`COMPLETION-P1-T13-R02@7d39c3bc0ab4` — `CURRENT / PASSED`。

## T13 I002

- Base：`dev_all@659fb74563bbe1fa1daaf4d3a0e868f702daaec6`
- Rework Base：`PR28@9d180f2d34728cd453c377a6310b01fe1a7659cf`
- Dependency：`COMPLETION-P1-T12-R07@74f402287bc4`
- Branch：`feature/p1-t13-semantic-digest-20260805-2005`
- PR：`#28 / OPEN / READY_FOR_REVIEW / NOT_MERGED`
- Design：`DESIGN-R46@P1-T13-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R42@P1-T13-REWORK-I002`
- TDD：`TDD-P1-T13-R02@83c66072849c`
- Architecture：`DEVSKEL-P1-T13-R02@83c66072849c`
- Development：`DEV-P1-T13-R02@7d39c3bc0ab4`
- Code Review：`CODEREVIEW-P1-T13-R03@7d39c3bc0ab4`
- Testing：`TESTING-P1-T13-R02@7d39c3bc0ab4`
- Completion：`COMPLETION-P1-T13-R02@7d39c3bc0ab4`
- Reviews：`REV-000672`～`REV-000692`
- Evidence：`EVD-001019`～`EVD-001033`
- Open P0/P1/P2：`0 / 0 / 0`

## Current contract

- `DEC-SEMANTIC-DIGEST-V1`、不可变 semantic input、canonical JSON 与版本域合同保持；
- Source identity 在进入 `DEC-SOURCE-DIGEST-V1` 前使用每次调用独立的 strict UTF-8 `CharsetEncoder`；
- malformed 与 unmappable 输入均使用 `CodingErrorAction.REPORT`；
- malformed high/low surrogate 稳定抛 `IllegalArgumentException("sourceId must contain valid Unicode")`，cause 保留 `CharacterCodingException`；
- 不再允许宽松替代字节造成 SHA-256 前置 identity 碰撞；
- ASCII、BMP、supplementary sourceDigest 已知向量保持；
- Source domain、Source 数量、Unicode code point 排序、sourceId/content 长度前缀和 SHA-256 合同未变；
- strict Encoder 不共享可变状态，同一 `CompilerDigestService` 可并发复用；
- FAILED transition Observer exception 只追加 `MIX-OBSERVER-FAILURE / WARNING`；
- 原始 ERROR、FAILED 状态、publisher=0 和 empty artifacts 保持；
- 完整成功 Timing 继续为 PASS=10、DISCOVERY=1、PARSE=1、DIGEST=1；
- T12 Deadline、Cancel、Clock overflow、prepare/commit、publisher=0/1 与 commit-wins 保持；
- T14/T15 与 P2～P7 runtime 未实现。

## Validation

### I002 valid RED

- Head：`83c66072849c8017beb74adbb539820a15bb515e`；
- Run：`31011478257` — `FAILURE / EXPECTED_RED`；
- Artifact：`8932629734`；
- SHA-256：`dff13bcc110615bf1648e2df535b3cd1149045851f5c3f2cbcb0cfa5e4a9642c`；
- Compiler：483 tests / 3 expected failures / 0 errors。

### First GREEN

- Production：`e2842eb888651858770202c560b1f4cd5932e7d7`；
- Run：`31011691306` — SUCCESS；
- Artifact：`8932726363`；
- SHA-256：`473c25ed28e6ab58ff29471f658390597d7aba6e4722d567df2377b8c6b3dfc9`。

### Clean-code / Independent Review

- Code/Test Revision：`7d39c3bc0ab45b6cd3c8ab637c10ae40a15e07b8`；
- Run：`31011874941` — SUCCESS；
- Artifact：`8932801028`；
- SHA-256：`679600735885f589a6370b0ad54845c909a24b2749b7b5edc4ac231822a8bf05`；
- Surefire XML：106；T13：34/34；T12：133/133；Compiler：486/486；
- 正常测试：606/606；intentional failure：1；Errors/Skipped：0/0；
- Java release 8、12 modules Reactor：PASSED；MySQL：`SKIPPED_NOT_APPLICABLE`。

## Revision Integrity

- R46 first commit/blob：`126db598680958cd6f4c4c2bdc8745743402b4ca` / `20034ec2cdb4353dbca459df7cdb2335e25b182b`；
- R42 first commit/blob：`3fa56d310286b4f72d4843f2129b5d1906cc21a0` / `6a346fe9d15f9ffbca17b0edb6622e1d044b57a0`；
- R46/R42 均早于有效 RED，clean-code Head 时 blob 未变化；
- Code/Test Revision 后只允许 `project_doc` Evidence/Completion/Handoff 更新。

## Recovery and next step

- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t13-r02/completion-report.json`
- Review：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t13-r03.md`
- Revision Lock：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/revision-lock-p1-t13-r02.json`
- Machine checkpoint：`project_doc/version/V_1.0/tdd_p1_t13_r02_completion.json`
- R01 失效记录：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t13-r02-invalidation.md`
- 未经用户明确授权不得合并 PR #28；
- PR #28 合并前 `TASK-P1-T14` 保持 `BLOCKED_UNTIL_PR_28_MERGE`。
