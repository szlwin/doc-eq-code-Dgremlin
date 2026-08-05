# P1-COMPILER-F01 恢复上下文

- 当前逻辑任务：`TASK-P1-T13 / I002` 已完成
- 当前有效 Completion：`COMPLETION-P1-T13-R02@7d39c3bc0ab4`
- 失效但保留：`COMPLETION-P1-T13-R01@74672ee1367b`
- Dependency：`COMPLETION-P1-T12-R07@74f402287bc4`
- 状态：`COMPLETED / PASSED`
- Base：`dev_all@659fb74563bbe1fa1daaf4d3a0e868f702daaec6`
- Rework Base：`PR28@9d180f2d34728cd453c377a6310b01fe1a7659cf`
- Branch：`feature/p1-t13-semantic-digest-20260805-2005`
- PR：`#28 / OPEN / READY_FOR_REVIEW / NOT_MERGED`
- Design：`DESIGN-R46@P1-T13-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R42@P1-T13-REWORK-I002`
- TDD：`TDD-P1-T13-R02@83c66072849c`
- Architecture：`DEVSKEL-P1-T13-R02@83c66072849c`
- Development：`DEV-P1-T13-R02@7d39c3bc0ab4`
- Code Review：`CODEREVIEW-P1-T13-R03@7d39c3bc0ab4`
- Testing：`TESTING-P1-T13-R02@7d39c3bc0ab4`
- Reviews：`REV-000672`～`REV-000692`
- Evidence：`EVD-001019`～`EVD-001033`
- Open P0/P1/P2：`0 / 0 / 0`

## Current contract

- `SemanticDigestInput`、canonical JSON、semantic digest 输入闭包与版本域保持 R45 合同；
- Source identity 使用局部 strict UTF-8 `CharsetEncoder(REPORT/REPORT)`；
- malformed high/low surrogate 不得进入 Source SHA-256，稳定抛 `sourceId must contain valid Unicode`；
- 异常 cause 为 `CharacterCodingException`；
- Encoder 每次调用独立创建，无共享 mutable state；
- ASCII/BMP/supplementary 已知 sourceDigest vectors 保持；
- Source domain、排序、数量、长度前缀、content 和 SHA-256 未变；
- FAILED transition Observer exception 只追加 `MIX_OBSERVER_FAILURE / WARNING`；
- 原 ERROR、FAILED、publisher=0、empty artifacts 保持；
- Timing 为 PASS=10、DISCOVERY=1、PARSE=1、DIGEST=1，补充 Timing 不增加 Clock 读取；
- T12 Deadline/Cancel/Clock/Publication 原子性保持；
- 未实现 T14/T15 或 P2～P7 runtime。

## Validation

- Valid RED：`83c66072849c8017beb74adbb539820a15bb515e` / Run `31011478257` / `3 expected failures, 0 errors`
- RED Artifact：`8932629734`
- RED SHA-256：`dff13bcc110615bf1648e2df535b3cd1149045851f5c3f2cbcb0cfa5e4a9642c`
- Production：`e2842eb888651858770202c560b1f4cd5932e7d7`
- First GREEN：Run `31011691306` — SUCCESS
- First GREEN Artifact/SHA：`8932726363` / `473c25ed28e6ab58ff29471f658390597d7aba6e4722d567df2377b8c6b3dfc9`
- Code/Test Revision：`7d39c3bc0ab45b6cd3c8ab637c10ae40a15e07b8`
- Clean P0：`31011874941` — SUCCESS
- Clean Artifact：`8932801028`
- Clean SHA-256：`679600735885f589a6370b0ad54845c909a24b2749b7b5edc4ac231822a8bf05`
- Clean T13/T12/Compiler/Normal：`34/34` / `133/133` / `486/486` / `606/606`
- Final documented input Head：`00052a538e8b022d3f19529403dfabc907cd826e`
- Final documented P0：`31012799485` — SUCCESS
- Final documented Artifact：`8933193699`
- Final documented SHA-256：`605a24fa8d81346089b1479a192aec063256ee17ded71045b05ef2554d796c9b`
- Final Surefire XML：`106`；T13：`34/34`；T12：`133/133`；Compiler：`486/486`；Normal：`606/606`
- Final all records：`607`；intentional failure：`1`；Errors/Skipped：`0/0`
- 12 modules / Java release 8 / intentional failure gate：PASSED
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Revision Integrity

- R46 first commit/blob：`126db598680958cd6f4c4c2bdc8745743402b4ca` / `20034ec2cdb4353dbca459df7cdb2335e25b182b`
- R42 first commit/blob：`3fa56d310286b4f72d4843f2129b5d1906cc21a0` / `6a346fe9d15f9ffbca17b0edb6622e1d044b57a0`
- R46/R42 均早于 RED，blob 未变化；
- Code/Test Revision 后只允许 `project_doc` 更新；
- `7d39c3bc0ab4...` 到 `00052a538e8b...` 的 11 个提交全部仅修改 `project_doc`。

## Recovery

- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t13-r02/completion-report.json`
- Review：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t13-r03.md`
- Revision Lock：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/revision-lock-p1-t13-r02.json`
- Machine checkpoint：`project_doc/version/V_1.0/tdd_p1_t13_r02_completion.json`
- R01 invalidation：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t13-r02-invalidation.md`
- Skill baseline：`common-develop-v2.44-rc8@4787876e135d347e9f37580910e2d28b09ea2ba4`；guard=`DIRTY / HEAD_MATCHES / CRITICAL_DRIFT_0`；
- 所有 `@Override` 独占一行，方法和重要逻辑使用中文注释；
- 仅在用户明确授权后合并 PR #28；
- TASK-P1-T14：`BLOCKED_UNTIL_PR_28_MERGE`。
