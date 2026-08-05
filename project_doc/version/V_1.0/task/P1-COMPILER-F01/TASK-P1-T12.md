# TASK-P1-T12 — 十阶段 Compiler Pipeline 与 Publication 原子终态

- Current Iteration：`I007`
- Status：`COMPLETED / PASSED`
- Base：`dev_all@3f53ea4a31b0b1366aad383f665736b0487d4d00`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- Branch：`feature/p1-t12-compiler-pipeline-20260804-2331`
- PR：`#27`
- Current Completion：`COMPLETION-P1-T12-R07@74f402287bc4`
- Open P0/P1/P2：`0 / 0 / 0`

## Completion history

- R01 / I001：`COMPLETION-P1-T12-R01@c6a515820972` — INVALIDATED / PRESERVED；
- R02 / I002：`COMPLETION-P1-T12-R02@5d5a7d72119b` — INVALIDATED / PRESERVED；
- R03 / I003：`COMPLETION-P1-T12-R03@4d4cd5c4c049` — INVALIDATED / PRESERVED；
- R04 / I004：`COMPLETION-P1-T12-R04@923129b1f20d` — INVALIDATED / PRESERVED；
- R05 / I005：`COMPLETION-P1-T12-R05@304a2156ff5e` — INVALIDATED / PRESERVED；
- R06 / I006：`COMPLETION-P1-T12-R06@ce8c92523256` — INVALIDATED / PRESERVED；失效原因记录于 `review-p1-t12-r12-invalidation.md`；
- R07 / I007：`COMPLETION-P1-T12-R07@74f402287bc4` — CURRENT / PASSED。

## Current revision

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

## Current published contract

- 十 Pass 固定名称和顺序；前九 Pass 无 Publication capability；
- final Pass prepare-only，Pipeline 在完整 Diagnostic 门禁后唯一 commit；
- 发布前失败 publisher=0；成功路径 publisher=1；PUBLISHED 不可逆；
- artifact freeze 使用显式 traversal stack、VISITING/FROZEN identity memo 和 snapshot budgets；
- equality/query 使用 operation-level non-recursive comparison 与四类 budgets；
- 一个公开查询内候选共享 `VISITING/EQUAL/NOT_EQUAL` pair state、canonical metadata 和 scalar intern table；
- List equality 与外部容器 canonicalization 使用 Iterator，不读取不可信 size、不整体复制；
- edge/node budget 在外部读取、metadata 保存和 task push 前生效；
- MAP/SET 在 canonical node intern 前拒绝 duplicate canonical key/element；
- collision 稳定抛 package-private `CanonicalCollisionException`，Map.Entry 不误用容器门禁；
- 正常 LinkedHashMap/Set、hash collision、Frozen receiver 与标准 Map key 折叠保持精确语义；
- iterator 业务异常原样传播，资源超限稳定抛 `ComparisonLimitException`；
- 循环、null、未知对象、conflict/null result/status、publisher exception 和重复 prepare 均稳定处理；
- 不执行 P2～P7 runtime，不实现 T13/T14/T15。

## Validation

- Valid RED：`cb3f08f28807ad40e2a4b40519baf4a2fc83ba61` / Run `31000174741` / 4 failures / 0 errors；
- First GREEN：`2da699060a4bb596c612a7b26fa022fcb6474a4d` / Run `31000726214` — SUCCESS；
- Clean-code Head：`74f402287bc4968dae3221848a91d968ecad0698`；
- P0 Run：`31000986498` — SUCCESS；
- Artifact：`8928238806`；
- SHA-256：`7d0a8c38c9d93df547ced820b3bf5ebdc964307bfc1032aeb48cf10cc12f19b5`；
- I007 16/16；T12 133/133；Compiler 452/452；正常测试 572/572；Surefire XML 100；
- Errors/Skipped 0/0；Java 8、12 modules、故意失败门禁：PASSED；MySQL：`SKIPPED_NOT_APPLICABLE`。

PR #27 未经用户明确授权不得合并；PR #27 合并前 `TASK-P1-T13` 保持 `BLOCKED_UNTIL_PR_27_MERGE`。
