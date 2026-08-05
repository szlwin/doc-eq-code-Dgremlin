# TASK-P1-T12 — 十阶段 Compiler Pipeline 与 Publication 原子终态

- Current Iteration：`I006`
- Status：`COMPLETED / PASSED`
- Base：`dev_all@3f53ea4a31b0b1366aad383f665736b0487d4d00`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- Branch：`feature/p1-t12-compiler-pipeline-20260804-2331`
- PR：`#27`
- Current Completion：`COMPLETION-P1-T12-R06@ce8c92523256`
- Open P0/P1/P2：`0 / 0 / 0`

## Completion history

- R01 / I001：`COMPLETION-P1-T12-R01@c6a515820972` — INVALIDATED / PRESERVED；
- R02 / I002：`COMPLETION-P1-T12-R02@5d5a7d72119b` — INVALIDATED / PRESERVED；
- R03 / I003：`COMPLETION-P1-T12-R03@4d4cd5c4c049` — INVALIDATED / PRESERVED；
- R04 / I004：`COMPLETION-P1-T12-R04@923129b1f20d` — INVALIDATED / PRESERVED；
- R05 / I005：`COMPLETION-P1-T12-R05@304a2156ff5e` — INVALIDATED / PRESERVED；失效原因记录于 `review-p1-t12-r10-invalidation.md`；
- R06 / I006：`COMPLETION-P1-T12-R06@ce8c92523256` — CURRENT / PASSED。

## Current revision

- Design：`DESIGN-R43@P1-T12-REWORK-I006`
- Plan：`TP-P1-COMPILER-F01-R39@P1-T12-REWORK-I006`
- TDD：`TDD-P1-T12-R06@788f475d60e4`
- Architecture：`DEVSKEL-P1-T12-R06@788f475d60e4`
- Development：`DEV-P1-T12-R06@ce8c92523256`
- Code Review：`CODEREVIEW-P1-T12-R11@ce8c92523256`
- Testing：`TESTING-P1-T12-R06@ce8c92523256`
- Completion：`COMPLETION-P1-T12-R06@ce8c92523256`
- Reviews：`REV-000611`～`REV-000633`
- Evidence：`EVD-000966`～`EVD-000990`

## Current published contract

- 十 Pass 固定名称和顺序；前九 Pass 无 Publication capability；
- final Pass prepare-only，Pipeline 在完整 Diagnostic 门禁后唯一 commit；
- 发布前失败 publisher=0；成功路径 publisher=1；PUBLISHED 为不可逆终态；
- Clock/timing overflow、Deadline、取消及基础设施异常稳定 fail-closed；
- artifact freeze 使用显式 traversal stack、VISITING/FROZEN identity memo 和四类 snapshot 预算；
- Frozen List/Set/Map 缓存 Java-compatible hash，collision 使用 canonical structural ID；
- equality/query 使用显式非递归比较，默认预算 depth=256、pairs=16384、edges=131072、canonical nodes=16384；
- 一个公开查询内全部候选共享 `ComparisonOperation`、pair result 与 canonical metadata；
- List equality 使用 Iterator，不假设 RandomAccess；
- 外部 List/Set/Map/Entry incremental canonicalization 不读取 size、不整体复制；
- 预算在外部 `next()`、key/value 读取、临时 metadata 保存和子任务调度前生效；
- Set/Map 跨独立 freeze Session 使用同一 operation 的 canonical IDs 无序比较；
- hash 仅作快速拒绝，hash 相同继续精确比较；
- iterator 业务异常原样传播，资源超限稳定抛 `ComparisonLimitException`；
- 循环、null、未知对象、conflict/null result/status、publisher exception 和重复 prepare 均稳定处理；
- 不执行 P2～P7 runtime，不实现 T13/T14/T15。

## Validation

- Valid RED：`788f475d60e4864fc6c11bfffee3ff925aa757ac` / Run `30991106416` / 7 failures / 0 errors；
- First GREEN：`91fe23a388d6fc62376222f36a291e8d00544f6a` / Run `30992157198` — SUCCESS；
- Clean-code Head：`ce8c9252325642cf45e89f71aaa1f807d4916aca`；
- P0 Run：`30992489987` — SUCCESS；
- Artifact：`8924724966`；
- SHA-256：`f0d5b9ce6c44a922b9bdd534c82f0e235912588f97ced16c117d9b57774a54a4`；
- I006 18/18；T12 117/117；Compiler 436/436；正常测试 556/556；Surefire XML 98；
- Errors/Skipped 0/0；Java 8、12 模块 Reactor、故意失败门禁：PASSED；MySQL：`SKIPPED_NOT_APPLICABLE`。

PR #27 未经用户明确授权不得合并；PR #27 合并前 `TASK-P1-T13` 保持 `BLOCKED_UNTIL_PR_27_MERGE`。
