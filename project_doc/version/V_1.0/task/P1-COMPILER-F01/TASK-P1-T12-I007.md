# TASK-P1-T12 / I007 — canonical Map/Set collision fail-closed 返工

- Status：`COMPLETED / PASSED`
- Base：`PR27@a59a39fde202366742963658bf07797c9537de57`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- Invalidated History：`COMPLETION-P1-T12-R01@c6a515820972`、`R02@5d5a7d72119b`、`R03@4d4cd5c4c049`、`R04@923129b1f20d`、`R05@304a2156ff5e`、`R06@ce8c92523256`
- Branch：`feature/p1-t12-compiler-pipeline-20260804-2331`
- PR：`#27 / DRAFT_PENDING_FINAL_P0`
- Design：`DESIGN-R44@P1-T12-REWORK-I007`
- Plan：`TP-P1-COMPILER-F01-R40@P1-T12-REWORK-I007`
- TDD：`TDD-P1-T12-R07@cb3f08f28807`
- Architecture：`DEVSKEL-P1-T12-R07@cb3f08f28807`
- Development：`DEV-P1-T12-R07@74f402287bc4`
- Code Review：`CODEREVIEW-P1-T12-R13@74f402287bc4`
- Testing：`TESTING-P1-T12-R07@74f402287bc4`
- Completion：`COMPLETION-P1-T12-R07@74f402287bc4`
- Reviews：`REV-000634`～`REV-000652`
- Evidence：`EVD-000991`～`EVD-001002`
- Open P0/P1/P2：`0 / 0 / 0`

## Finding closure

- `FND-P1-T12-I007-001` `[P2][SPEC][CORRECTNESS][ORACLE]` — `CLOSED`：MAP/SET 在 canonical node intern 前检测 duplicate canonical key/element，以稳定 `CanonicalCollisionException` fail-closed；两个非法 collision 容器不得返回 true。

## Delivered contract

- `FinishPairsTask` 在 MAP 排序后扫描相邻 canonical key ID；
- `FinishSequenceTask` 在 SET 排序后扫描相邻 canonical element ID；
- collision 检查严格早于 `nodeId()`、`complete()` 与父 assignment；
- Map.Entry 不应用容器 duplicate-key 门禁；
- 正常 LinkedHashMap/Set、标准 Map key 折叠和普通 hash collision 保持；
- FrozenMap/FrozenSet 对同 size/hash 非法外部容器稳定拒绝；
- `ConditionalCompareTask` 死代码已删除；
- I001～I006、Publication、snapshot、iterator、operation cache 与 budgets 合同保持；
- 所有 `@Override` 独占一行，新增关键逻辑使用中文注释；
- 未实现 T13/T14/T15 或 P2～P7 runtime。

## Validation

- Valid RED：`cb3f08f28807...` / Run `31000174741` / Artifact `8927903337` / 4 expected failures / 0 errors；
- First GREEN：`2da699060a4b...` / Run `31000726214` — SUCCESS；
- Clean-code Head：`74f402287bc4968dae3221848a91d968ecad0698`；
- Clean P0：`31000986498` — SUCCESS；
- Artifact：`8928238806`；SHA-256：`7d0a8c38c9d93df547ced820b3bf5ebdc964307bfc1032aeb48cf10cc12f19b5`；
- I007 `16/16`；T12 `133/133`；Compiler `452/452`；正常测试 `572/572`；Surefire XML `100`；
- Errors/Skipped `0/0`；Java 8、12 modules、intentional failure gate：PASSED；MySQL：`SKIPPED_NOT_APPLICABLE`。

R38～R43、R34～R39、I001～I006 的全部历史保持原文件和原 SHA。PR #27 未经用户明确授权不得合并；TASK-P1-T13 保持 `BLOCKED_UNTIL_PR_27_MERGE`。
