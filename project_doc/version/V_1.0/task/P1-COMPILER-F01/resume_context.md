# P1-COMPILER-F01 恢复上下文

- 当前逻辑任务：`TASK-P1-T12 / I004` 已完成
- 当前有效 Completion：`COMPLETION-P1-T12-R04@923129b1f20d`
- 失效但保留：`COMPLETION-P1-T12-R01@c6a515820972`、`COMPLETION-P1-T12-R02@5d5a7d72119b`、`COMPLETION-P1-T12-R03@4d4cd5c4c049`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- 状态：`COMPLETED / PASSED`
- Base：`dev_all@3f53ea4a31b0b1366aad383f665736b0487d4d00`
- Branch：`feature/p1-t12-compiler-pipeline-20260804-2331`
- PR：`#27`
- Design：`DESIGN-R41@P1-T12-REWORK-I004`
- Plan：`TP-P1-COMPILER-F01-R37@P1-T12-REWORK-I004`
- TDD：`TDD-P1-T12-R04@1270d6f2b829`
- Architecture：`DEVSKEL-P1-T12-R04@c82e0a3023da`
- Development：`DEV-P1-T12-R04@923129b1f20d`
- Code Review：`CODEREVIEW-P1-T12-R07@923129b1f20d`
- Testing：`TESTING-P1-T12-R04@923129b1f20d`
- Reviews：`REV-000570`～`REV-000590`
- Evidence：`EVD-000885`～`EVD-000909`
- Open P0/P1/P2：`0 / 0 / 0`

## Current contract

- final Pass prepare-only，Pipeline 在完整 Diagnostic 门禁后唯一调用 publisher；
- ERROR/cancel/timeout/Clock/timing/Pass 异常和 candidate 缺失路径 publisher=0；
- Warning/Info 保留，成功路径 publisher=1，PUBLISHED 不可逆；
- timing overflow 不越过结果边界；start timestamp 到期不执行 Pass；
- artifact snapshot 使用显式 traversal stack，不依赖 JVM 递归；
- VISITING 检测 cycle，FROZEN memoization 复用共享 DAG identity；
- 默认资源预算为 depth=256、unique=4096、edges=65536、map entries=16384；
- 资源超限稳定返回 FAILED、`pipeline.artifact.resource-exceeded`、publisher=0；
- Set/Map collision 使用 canonical structural ID，Frozen 容器缓存结构 hash；
- Context/Result、循环、null、未知对象及 I001～I003 合同保持；
- 未实现 T13/T14/T15 或 P2～P7 runtime。

## Validation

- Valid RED：`1270d6f2b829a568f7edda4a23e21ba2748d7a50` / Run `30974123330` / `6 failures, 0 errors`
- Hash Review RED：`cbeed46dbf053184f247184ad9976c706d42f500` / Run `30974844132` / `1 failure, 0 errors`
- Clean-code Head：`923129b1f20d6bebe589231b770b5c7675b52737`
- P0 Run：`30975103715` — SUCCESS
- Artifact：`8917961744`
- SHA-256：`df328a44496836e018c4725714adece969f46e0f71a0228c337ff9cadb71a640`
- I004：`17/17`；T12：`83/83`；Compiler：`402/402`；Normal：`522/522`
- Surefire XML：`94`；Errors/Skipped：`0/0`
- 12 modules / Java release 8 / intentional failure gate：PASSED
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Recovery

- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t12-r04/completion-report.json`
- Review：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t12-r07.md`
- Revision Lock：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/revision-lock-p1-t12-r04.json`
- Machine checkpoint：`project_doc/version/V_1.0/tdd_p1_t12_r04_completion.json`
- 所有 `@Override` 独占一行，方法和重要逻辑使用中文注释；
- 仅在用户明确授权后合并 PR #27；
- TASK-P1-T13：`BLOCKED_UNTIL_PR_27_MERGE`。
