# TASK-P1-T12 / I004 — Artifact Snapshot 资源边界返工

- Status：`COMPLETED / PASSED`
- Base：`PR27@cf6e7dbe18d2f172dc4c68c793f45d9ecfbabe9d`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- Invalidated History：`COMPLETION-P1-T12-R01@c6a515820972`、`COMPLETION-P1-T12-R02@5d5a7d72119b`、`COMPLETION-P1-T12-R03@4d4cd5c4c049`
- Branch：`feature/p1-t12-compiler-pipeline-20260804-2331`
- PR：`#27`
- Design：`DESIGN-R41@P1-T12-REWORK-I004`
- Plan：`TP-P1-COMPILER-F01-R37@P1-T12-REWORK-I004`
- TDD：`TDD-P1-T12-R04@1270d6f2b829`
- Architecture：`DEVSKEL-P1-T12-R04@c82e0a3023da`
- Development：`DEV-P1-T12-R04@923129b1f20d`
- Code Review：`CODEREVIEW-P1-T12-R07@923129b1f20d`
- Testing：`TESTING-P1-T12-R04@923129b1f20d`
- Completion：`COMPLETION-P1-T12-R04@923129b1f20d`
- Reviews：`REV-000570`～`REV-000590`
- Evidence：`EVD-000885`～`EVD-000909`
- Findings：`FND-P1-T12-I004-001`～`003` CLOSED
- Open P0/P1/P2：`0 / 0 / 0`

## Delivered contract

- artifact 图使用显式 traversal stack，不依赖 JVM 方法递归；
- VISITING identity 判定循环，FROZEN identity 复用同一 immutable snapshot；
- 默认 depth/unique-container/edge/map-entry 预算为 256/4096/65536/16384；
- 资源超限稳定形成 FAILED、`pipeline.artifact.resource-exceeded`、publisher=0；
- 24 层共享 DAG 按唯一图线性遍历，不产生指数复制；
- Set/Map collision 通过 canonical structural ID 判断；
- Frozen List/Set/Map 缓存结构 hash，目标 Set/Map 不递归展开共享 DAG；
- 深路径不能借助 memoized FROZEN 节点绕过 depth；
- 循环、null、未知可变对象、collision、prepare/commit、Diagnostic、Clock、Deadline、Context/Result 原合同保持；
- 未实现 T13/T14/T15 或 P2～P7 runtime。

## Validation

- Valid RED：`1270d6f2b829...` / Run `30974123330` / 6 expected failures / 0 errors；
- Hash Review RED：`cbeed46dbf05...` / Run `30974844132` / 1 expected failure / 0 errors；
- Clean-code Head：`923129b1f20d6bebe589231b770b5c7675b52737`；
- P0 Run：`30975103715` — SUCCESS；
- Artifact：`8917961744`；
- SHA-256：`df328a44496836e018c4725714adece969f46e0f71a0228c337ff9cadb71a640`；
- I004：17/17；T12：83/83；Compiler：402/402；正常测试：522/522；
- Surefire XML：94；Errors/Skipped：0/0；
- 12 模块 Reactor、Java release 8、故意失败门禁：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

PR #27 未经用户明确授权不得合并；PR #27 合并前 `TASK-P1-T13` 保持 `BLOCKED_UNTIL_PR_27_MERGE`。
