# P1-COMPILER-F01 恢复上下文

- 当前逻辑任务：`TASK-P1-T12 / I007` 已完成
- 当前有效 Completion：`COMPLETION-P1-T12-R07@74f402287bc4`
- 失效但保留：`COMPLETION-P1-T12-R01@c6a515820972`、`R02@5d5a7d72119b`、`R03@4d4cd5c4c049`、`R04@923129b1f20d`、`R05@304a2156ff5e`、`R06@ce8c92523256`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- 状态：`COMPLETED / PASSED`
- Base：`dev_all@3f53ea4a31b0b1366aad383f665736b0487d4d00`
- Branch：`feature/p1-t12-compiler-pipeline-20260804-2331`
- PR：`#27`
- Design：`DESIGN-R44@P1-T12-REWORK-I007`
- Plan：`TP-P1-COMPILER-F01-R40@P1-T12-REWORK-I007`
- TDD：`TDD-P1-T12-R07@cb3f08f28807`
- Architecture：`DEVSKEL-P1-T12-R07@cb3f08f28807`
- Development：`DEV-P1-T12-R07@74f402287bc4`
- Code Review：`CODEREVIEW-P1-T12-R13@74f402287bc4`
- Testing：`TESTING-P1-T12-R07@74f402287bc4`
- Reviews：`REV-000634`～`REV-000652`
- Evidence：`EVD-000991`～`EVD-001002`
- Open P0/P1/P2：`0 / 0 / 0`

## Current contract

- final Pass prepare-only，Pipeline 在完整 Diagnostic 门禁后唯一调用 publisher；
- ERROR/cancel/timeout/Clock/timing/Pass 异常和 candidate 缺失路径 publisher=0；
- 成功路径 publisher=1，PUBLISHED 不可逆；
- snapshot/comparison 使用显式 traversal、identity memo、operation cache 和严格 budgets；
- 外部 List/Set/Map/Entry iterator-driven，不信任 size、不整体复制；
- MAP/SET 在 canonical node intern 前检测 duplicate canonical key/element；
- collision 稳定抛 `ArtifactSnapshots.CanonicalCollisionException`，消息为 `map-key` 或 `set-element`；
- Map.Entry 不应用 duplicate 容器门禁；
- 正常 LinkedHashMap/Set、普通 hash collision、Frozen receiver、标准 Map key 折叠保持；
- `ConditionalCompareTask` 已删除；
- I001～I006、Context/Result、Diagnostic、Clock、Deadline、cancel 和 Publication 原子性保持；
- 未实现 T13/T14/T15 或 P2～P7 runtime。

## Validation

- Valid RED：`cb3f08f28807ad40e2a4b40519baf4a2fc83ba61` / Run `31000174741` / `4 failures, 0 errors`
- First GREEN：`2da699060a4bb596c612a7b26fa022fcb6474a4d` / Run `31000726214` — SUCCESS
- Clean-code Head：`74f402287bc4968dae3221848a91d968ecad0698`
- P0 Run：`31000986498` — SUCCESS
- Artifact：`8928238806`
- SHA-256：`7d0a8c38c9d93df547ced820b3bf5ebdc964307bfc1032aeb48cf10cc12f19b5`
- I007：`16/16`；T12：`133/133`；Compiler：`452/452`；Normal：`572/572`
- Surefire XML：`100`；Errors/Skipped：`0/0`
- 12 modules / Java release 8 / intentional failure gate：PASSED
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Recovery

- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t12-r07/completion-report.json`
- Review：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t12-r13.md`
- Revision Lock：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/revision-lock-p1-t12-r07.json`
- Machine checkpoint：`project_doc/version/V_1.0/tdd_p1_t12_r07_completion.json`
- Skill baseline：`common-develop-v2.44-rc8@4787876e135d347e9f37580910e2d28b09ea2ba4`；guard=`DIRTY / HEAD_MATCHES / CRITICAL_DRIFT_0`；
- 所有 `@Override` 独占一行，方法和重要逻辑使用中文注释；
- 仅在用户明确授权后合并 PR #27；
- TASK-P1-T13：`BLOCKED_UNTIL_PR_27_MERGE`。
