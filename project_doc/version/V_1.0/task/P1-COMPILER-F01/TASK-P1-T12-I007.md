# TASK-P1-T12 / I007 — canonical Map/Set collision fail-closed 返工

- Status：`IN_PROGRESS / DESIGN_PASSED`
- Base：`PR27@a59a39fde202366742963658bf07797c9537de57`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- Invalidated History：`COMPLETION-P1-T12-R01@c6a515820972`、`R02@5d5a7d72119b`、`R03@4d4cd5c4c049`、`R04@923129b1f20d`、`R05@304a2156ff5e`、`R06@ce8c92523256`
- Branch：`feature/p1-t12-compiler-pipeline-20260804-2331`
- PR：`#27 / DRAFT`
- Design：`DESIGN-R44@P1-T12-REWORK-I007`
- Plan：`TP-P1-COMPILER-F01-R40@P1-T12-REWORK-I007`
- Review：`NEEDS_CHANGES / REWORK`
- Open P0/P1/P2：`0 / 0 / 1`

## Finding

- `FND-P1-T12-I007-001` `[P2][SPEC][CORRECTNESS][ORACLE]`：Map canonical pair 排序后缺少 duplicate canonical key 门禁；identity-backed Set 的 duplicate canonical element 也未 fail-closed。两个非法 collision 容器可能形成相同合法 node 并被判为相等。

## Goal

在 MAP/SET canonical node intern 前识别重复 canonical key/element，并以 package-private `ArtifactSnapshots.CanonicalCollisionException` 稳定拒绝；正常 LinkedHashMap/Set 和 hash collision 精确比较保持。删除无调用点的 `ConditionalCompareTask`。

## History preservation

R38～R43、R34～R39、I001～I006 的 Design、Plan、RED、Architecture、Development、Review、Testing、Completion、CI、Artifact、Revision Lock 与 documented Head 均保持原文件及原 SHA，不回写为通过。I007 使用新的 Revision、Evidence、Review 和 Completion。

## Stop conditions

- R44/R40 未早于 I007 RED；
- duplicate canonical key/element 仍可形成合法 node；
- 两个非法 collision Map/Set 仍返回 true；
- hashCode 相同但 equals 不同被误拒绝；
- collision 后形成 partial FROZEN/cache；
- `ConditionalCompareTask` 仍为无调用死代码；
- I001～I006、Publication、snapshot、iterator、budget、operation cache 合同回归；
- Open P0/P1/P2 未清零；
- 未完成 final P0、Artifact、Revision Integrity 和独立 Review；
- 用户未授权时合并 PR #27 或启动 T13。
