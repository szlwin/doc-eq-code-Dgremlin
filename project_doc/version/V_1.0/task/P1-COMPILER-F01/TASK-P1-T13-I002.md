# TASK-P1-T13 / I002 — 严格 Unicode Source 身份与 FAILED Observer 返工

- Status：`COMPLETED / PASSED`
- Base：`PR28@9d180f2d34728cd453c377a6310b01fe1a7659cf`
- Branch：`feature/p1-t13-semantic-digest-20260805-2005`
- PR：`#28 / OPEN / READY_FOR_REVIEW / NOT_MERGED`
- Previous Completion：`COMPLETION-P1-T13-R01@74672ee1367b` — `INVALIDATED / PRESERVED`
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

## Closed findings

- `FND-P1-T13-I002-001`：`CLOSED`；
- `FND-P1-T13-I002-002`：`CLOSED`。

## Delivered contract

- `sourceId` 在进入 `DEC-SOURCE-DIGEST-V1` 前使用 strict UTF-8 `CharsetEncoder`；
- malformed/unmappable 均 REPORT；
- malformed high/low surrogate 稳定抛 `IllegalArgumentException("sourceId must contain valid Unicode")`；
- cause 保留 `CharacterCodingException`；
- Encoder 每次调用独立创建，服务保持无状态且可并发复用；
- ASCII/BMP/supplementary 已知摘要向量保持；
- Source domain、排序、数量、长度前缀、原始 content 和 SHA-256 合同未变；
- FAILED transition Observer exception 只追加 Warning；
- 原 ERROR、FAILED、publisher=0 和 empty artifacts 保持；
- T12 Deadline/Cancel/Clock/Publication 原子性保持；
- T14/T15 与 P2～P7 runtime 未实现。

## Validation

### Valid RED

- Head：`83c66072849c8017beb74adbb539820a15bb515e`
- Run：`31011478257` — `FAILURE / EXPECTED_RED`
- Artifact：`8932629734`
- SHA-256：`dff13bcc110615bf1648e2df535b3cd1149045851f5c3f2cbcb0cfa5e4a9642c`
- Compiler：483 tests / 3 expected failures / 0 errors。

### First GREEN

- Production Head：`e2842eb888651858770202c560b1f4cd5932e7d7`
- Run：`31011691306` — SUCCESS
- Artifact：`8932726363`
- SHA-256：`473c25ed28e6ab58ff29471f658390597d7aba6e4722d567df2377b8c6b3dfc9`

### Clean-code / Independent Review

- Code/Test Revision：`7d39c3bc0ab45b6cd3c8ab637c10ae40a15e07b8`
- Run：`31011874941` — SUCCESS
- Artifact：`8932801028`
- SHA-256：`679600735885f589a6370b0ad54845c909a24b2749b7b5edc4ac231822a8bf05`
- Surefire XML：106；
- T13：34/34；
- T12：133/133；
- Compiler：486/486；
- 正常测试：606/606；
- intentional failure：1；
- Errors/Skipped：0/0；
- Java 8、12 modules Reactor：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## Revision integrity

- R46 first commit/blob：`126db598680958cd6f4c4c2bdc8745743402b4ca` / `20034ec2cdb4353dbca459df7cdb2335e25b182b`；
- R42 first commit/blob：`3fa56d310286b4f72d4843f2129b5d1906cc21a0` / `6a346fe9d15f9ffbca17b0edb6622e1d044b57a0`；
- R46/R42 均早于有效 RED且 blob 未变化；
- Code/Test Revision 后只允许 `project_doc` 更新。

PR #28 未执行合并；未经用户明确授权不得合并。`TASK-P1-T14` 保持 `BLOCKED_UNTIL_PR_28_MERGE`。
