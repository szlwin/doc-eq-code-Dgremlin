# TASK-P1-T12 — 十阶段 Compiler Pipeline 与 Publication 原子终态

- Current Iteration：`I005`
- Status：`COMPLETED / PASSED`
- Base：`dev_all@3f53ea4a31b0b1366aad383f665736b0487d4d00`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- Branch：`feature/p1-t12-compiler-pipeline-20260804-2331`
- PR：`#27`
- Current Completion：`COMPLETION-P1-T12-R05@304a2156ff5e`
- Open P0/P1/P2：`0 / 0 / 0`

## Completion history

- R01 / I001：`COMPLETION-P1-T12-R01@c6a515820972` — INVALIDATED / PRESERVED；
- R02 / I002：`COMPLETION-P1-T12-R02@5d5a7d72119b` — INVALIDATED / PRESERVED；
- R03 / I003：`COMPLETION-P1-T12-R03@4d4cd5c4c049` — INVALIDATED / PRESERVED；
- R04 / I004：`COMPLETION-P1-T12-R04@923129b1f20d` — INVALIDATED / PRESERVED；失效原因记录于 `review-p1-t12-r08-invalidation.md`；
- R05 / I005：`COMPLETION-P1-T12-R05@304a2156ff5e` — CURRENT / PASSED。

## Current revision

- Design：`DESIGN-R42@P1-T12-REWORK-I005`
- Plan：`TP-P1-COMPILER-F01-R38@P1-T12-REWORK-I005`
- TDD：`TDD-P1-T12-R05@c3a78498e595`
- Architecture：`DEVSKEL-P1-T12-R05@c3a78498e595`
- Development：`DEV-P1-T12-R05@304a2156ff5e`
- Code Review：`CODEREVIEW-P1-T12-R09@304a2156ff5e`
- Testing：`TESTING-P1-T12-R05@304a2156ff5e`
- Completion：`COMPLETION-P1-T12-R05@304a2156ff5e`
- Reviews：`REV-000591`～`REV-000610`
- Evidence：`EVD-000910`～`EVD-000965`

## Current published contract

- 十 Pass 固定名称和顺序；前九 Pass 无 Publication capability；
- final Pass 只准备 candidate，Pipeline 在完整聚合所有 Diagnostic 后唯一 commit；
- 发布前失败 publisher=0；成功路径 publisher=1；PUBLISHED 为不可逆终态；
- Clock/timing overflow、start Deadline、取消及基础设施异常稳定 fail-closed；
- artifact freeze 使用显式 traversal stack、VISITING/FROZEN identity memoization 和 depth/unique/edge/map-entry 预算；
- 共享 DAG 按唯一图线性冻结并复用 frozen identity；
- Frozen List/Set/Map 缓存 Java-compatible hash，Set/Map collision 使用 canonical structural ID；
- Frozen List/Set/Map/Entry 的公开 equality/query 使用显式 pair stack和 identity-pair memo；
- comparison 默认预算为 depth=256、pairs=16384、edges=131072、canonical nodes=16384；
- Set/Map 跨独立 freeze Session 使用双根共享 canonical IDs 完成无序精确比较；
- hash 仅作快速拒绝，hash 相同仍进行受控精确比较；
- 普通外部 Collection query 不调用其容器 equals/hashCode；
- 循环、null、未知对象、collision、conflict、null result/status、publisher exception 和重复 prepare 均稳定处理；
- 不执行 P2～P7 runtime，不实现 T13/T14/T15。

## Validation

- Valid RED：`c3a78498e595d0006334c8ec382c72c830142d19` / Run `30983520984` / 6 failures / 0 errors；
- Clean-code Head：`304a2156ff5e86c2a45213d4e917f17b9a172831`；
- P0 Run：`30984394393` — SUCCESS；
- Artifact：`8921466813`；
- SHA-256：`3a2002648c03c082f649991317e5ef3abbb167df6d99327dfa23c9e787d2fe6d`；
- I005 16/16；T12 99/99；Compiler 418/418；正常测试 538/538；
- Surefire XML 96；Errors/Skipped 0/0；
- Java 8、12 模块 Reactor、故意失败门禁：PASSED；MySQL：`SKIPPED_NOT_APPLICABLE`。

PR #27 未经用户明确授权不得合并；PR #27 合并前 `TASK-P1-T13` 保持 `BLOCKED_UNTIL_PR_27_MERGE`。
