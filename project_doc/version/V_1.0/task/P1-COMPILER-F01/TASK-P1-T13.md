# TASK-P1-T13 — 确定性 Digest、Deadline 与 Observer

- Current Iteration：`I002`
- Status：`COMPLETED / PASSED`
- Base：`dev_all@659fb74563bbe1fa1daaf4d3a0e868f702daaec6`
- Rework Base：`PR28@9d180f2d34728cd453c377a6310b01fe1a7659cf`
- Dependency：`COMPLETION-P1-T12-R07@74f402287bc4`
- Branch：`feature/p1-t13-semantic-digest-20260805-2005`
- PR：`#28 / OPEN / READY_FOR_REVIEW / NOT_MERGED`
- Current Design：`DESIGN-R46@P1-T13-REWORK-I002`
- Current Plan：`TP-P1-COMPILER-F01-R42@P1-T13-REWORK-I002`
- Current TDD：`TDD-P1-T13-R02@83c66072849c`
- Current Architecture：`DEVSKEL-P1-T13-R02@83c66072849c`
- Current Development：`DEV-P1-T13-R02@7d39c3bc0ab4`
- Current Code Review：`CODEREVIEW-P1-T13-R03@7d39c3bc0ab4`
- Current Testing：`TESTING-P1-T13-R02@7d39c3bc0ab4`
- Current Completion：`COMPLETION-P1-T13-R02@7d39c3bc0ab4`
- Previous Code Review：`CODEREVIEW-P1-T13-R01@74672ee1367b` — `INVALIDATED / PRESERVED`
- Previous Completion：`COMPLETION-P1-T13-R01@74672ee1367b` — `INVALIDATED / PRESERVED`
- Reviews：`REV-000672`～`REV-000692`
- Evidence：`EVD-001019`～`EVD-001033`
- Open P0/P1/P2：`0 / 0 / 0`

## I002 closure

- `FND-P1-T13-I002-001`：CLOSED；
- `FND-P1-T13-I002-002`：CLOSED。

Source identity 在进入 `DEC-SOURCE-DIGEST-V1` 前使用独立 strict UTF-8 `CharsetEncoder(REPORT/REPORT)`；malformed high/low surrogate 稳定 fail-closed，异常 message 为 `sourceId must contain valid Unicode`，cause 为 `CharacterCodingException`。

合法 ASCII、BMP、supplementary digest vector、Source 排序、长度前缀、content 字节、SHA-256 和小写 hex 合同均保持。FAILED transition Observer exception 只追加 `MIX_OBSERVER_FAILURE / WARNING`，原 ERROR、FAILED、publisher=0 和 empty artifacts 保持。

## Validation

### I002 valid RED

- Head：`83c66072849c8017beb74adbb539820a15bb515e`
- P0 Run：`31011478257` — `FAILURE / EXPECTED_RED`
- Artifact：`8932629734`
- SHA-256：`dff13bcc110615bf1648e2df535b3cd1149045851f5c3f2cbcb0cfa5e4a9642c`
- Compiler：483 / 3 expected failures / 0 errors。

### First GREEN

- Production：`e2842eb888651858770202c560b1f4cd5932e7d7`
- P0 Run：`31011691306` — SUCCESS
- Artifact：`8932726363`
- SHA-256：`473c25ed28e6ab58ff29471f658390597d7aba6e4722d567df2377b8c6b3dfc9`

### Clean-code / Independent Review

- Code/Test Revision：`7d39c3bc0ab45b6cd3c8ab637c10ae40a15e07b8`
- P0 Run：`31011874941` — SUCCESS
- Artifact：`8932801028`
- SHA-256：`679600735885f589a6370b0ad54845c909a24b2749b7b5edc4ac231822a8bf05`
- Surefire XML：106；T13：34/34；T12：133/133；Compiler：486/486；
- 正常测试：606/606；intentional failure：1；Errors/Skipped：0/0；
- Java release 8、12 modules Reactor：PASSED；MySQL：`SKIPPED_NOT_APPLICABLE`。

## Preserved I001 history

- Design：`DESIGN-R45@P1-T13-I001`；
- Plan：`TP-P1-COMPILER-F01-R41@P1-T13-I001`；
- TDD：`TDD-P1-T13-R01@4f3d444f779f`；
- Architecture：`DEVSKEL-P1-T13-R01@4f3d444f779f`；
- Development：`DEV-P1-T13-R01@74672ee1367b`；
- Testing：`TESTING-P1-T13-R01@74672ee1367b`；
- Code Review：`CODEREVIEW-P1-T13-R01@74672ee1367b` — INVALIDATED / PRESERVED；
- Completion：`COMPLETION-P1-T13-R01@74672ee1367b` — INVALIDATED / PRESERVED；
- Final I001 Head/Run/Artifact：`9d180f2d3472` / `31008895114` / `8931548482`。

## Revision integrity and style

- R46 first commit/blob：`126db598680958cd6f4c4c2bdc8745743402b4ca` / `20034ec2cdb4353dbca459df7cdb2335e25b182b`；
- R42 first commit/blob：`3fa56d310286b4f72d4843f2129b5d1906cc21a0` / `6a346fe9d15f9ffbca17b0edb6622e1d044b57a0`；
- R46/R42 均早于 RED且 blob 未变化；
- Code/Test Revision 后只允许 `project_doc` 更新；
- 所有 `@Override` 独占一行；
- 方法与重要逻辑使用中文注释。

PR #28 未执行合并；未经用户明确授权不得合并。`TASK-P1-T14` 保持 `BLOCKED_UNTIL_PR_28_MERGE`。
