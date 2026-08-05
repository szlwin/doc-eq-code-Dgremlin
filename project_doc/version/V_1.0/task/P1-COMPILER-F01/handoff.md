# P1-COMPILER-F01 阶段交接

> T01～T11 已合并到 `dev_all`。TASK-P1-T12 / I004 已完成，当前有效 Completion 为 `COMPLETION-P1-T12-R04@923129b1f20d`。R01～R03 已失效但全部历史不可变保留。PR #27 尚未合并，T13 保持阻断。

## Completion history

- R01 / I001：`COMPLETION-P1-T12-R01@c6a515820972` — INVALIDATED / PRESERVED；
- R02 / I002：`COMPLETION-P1-T12-R02@5d5a7d72119b` — INVALIDATED / PRESERVED；
- R03 / I003：`COMPLETION-P1-T12-R03@4d4cd5c4c049` — INVALIDATED / PRESERVED；
- R04 / I004：`COMPLETION-P1-T12-R04@923129b1f20d` — CURRENT / PASSED。

## T12 I004

- Base：`PR27@cf6e7dbe18d2f172dc4c68c793f45d9ecfbabe9d`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
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
- Open P0/P1/P2：`0 / 0 / 0`

## Current contract

- final Pass 只准备 candidate，Pipeline 在完整 Diagnostic 门禁后唯一 commit；
- 发布前失败 publisher=0，成功路径 publisher=1，PUBLISHED 不可逆；
- Clock/timing overflow 与 start Deadline 稳定 fail-closed；
- artifact snapshot 使用显式 stack，区分 VISITING cycle 与 FROZEN shared DAG；
- 默认资源预算为 depth=256、unique=4096、edges=65536、map entries=16384；
- 资源超限返回 FAILED、`pipeline.artifact.resource-exceeded`、publisher=0；
- shared DAG 复用同一 frozen identity，Set/Map 构建不递归展开 hash；
- collision 使用 canonical structural ID，Frozen List/Set/Map 缓存结构 hash；
- Context/Result、循环、null、未知对象、conflict/null status 等历史合同保持；
- 未实现 T13/T14/T15 或 P2～P7 runtime。

## Revision Integrity

- R41 first commit/blob：`37dc26b5297dc73d5ac8f167df04ef62c9d3d97e` / `058f60f38649f3d7557eaf821bff3df37a3ea37c`
- R37 first commit/blob：`c06f715d414af37474076efe5bce1cd933248177` / `3d1200fd5a4339c91577aaa78873ac05beb68914`
- R41/R37 均早于有效 RED，最终 blob 未变化。

## Validation

- Valid RED：`1270d6f2b829...` / Run `30974123330` / 6 failures / 0 errors；
- Hash Review RED：`cbeed46dbf05...` / Run `30974844132` / 1 failure / 0 errors；
- Clean-code Head：`923129b1f20d6bebe589231b770b5c7675b52737`；
- P0 Run：`30975103715` — SUCCESS；
- Artifact：`8917961744`；SHA-256：`df328a44496836e018c4725714adece969f46e0f71a0228c337ff9cadb71a640`；
- I004 17/17；T12 83/83；Compiler 402/402；正常测试 522/522；Surefire XML 94；
- Java 8、12 模块 Reactor、故意失败门禁：PASSED；MySQL：`SKIPPED_NOT_APPLICABLE`。

## Recovery and next step

- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t12-r04/completion-report.json`
- Review：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t12-r07.md`
- Revision Lock：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/revision-lock-p1-t12-r04.json`
- Machine checkpoint：`project_doc/version/V_1.0/tdd_p1_t12_r04_completion.json`
- 未经用户明确授权不得合并 PR #27；
- PR #27 合并前 `TASK-P1-T13` 保持 `BLOCKED_UNTIL_PR_27_MERGE`。
