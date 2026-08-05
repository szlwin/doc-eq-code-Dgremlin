# P1-COMPILER-F01 恢复上下文

- 当前逻辑任务：`TASK-P1-T12 / I005` 已完成
- 当前有效 Completion：`COMPLETION-P1-T12-R05@304a2156ff5e`
- 失效但保留：`COMPLETION-P1-T12-R01@c6a515820972`、`COMPLETION-P1-T12-R02@5d5a7d72119b`、`COMPLETION-P1-T12-R03@4d4cd5c4c049`、`COMPLETION-P1-T12-R04@923129b1f20d`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- 状态：`COMPLETED / PASSED`
- Base：`dev_all@3f53ea4a31b0b1366aad383f665736b0487d4d00`
- Branch：`feature/p1-t12-compiler-pipeline-20260804-2331`
- PR：`#27`
- Design：`DESIGN-R42@P1-T12-REWORK-I005`
- Plan：`TP-P1-COMPILER-F01-R38@P1-T12-REWORK-I005`
- TDD：`TDD-P1-T12-R05@c3a78498e595`
- Architecture：`DEVSKEL-P1-T12-R05@c3a78498e595`
- Development：`DEV-P1-T12-R05@304a2156ff5e`
- Code Review：`CODEREVIEW-P1-T12-R09@304a2156ff5e`
- Testing：`TESTING-P1-T12-R05@304a2156ff5e`
- Reviews：`REV-000591`～`REV-000610`
- Evidence：`EVD-000910`～`EVD-000965`
- Open P0/P1/P2：`0 / 0 / 0`

## Current contract

- final Pass prepare-only，Pipeline 在完整 Diagnostic 门禁后唯一调用 publisher；
- ERROR/cancel/timeout/Clock/timing/Pass 异常和 candidate 缺失路径 publisher=0；
- Warning/Info 保留，成功路径 publisher=1，PUBLISHED 不可逆；
- artifact freeze 使用显式 traversal stack、VISITING/FROZEN memoization 与四类 snapshot budgets；
- shared DAG 按唯一图线性冻结并复用 frozen identity；
- collision 使用 canonical structural ID，Frozen 容器缓存 Java-compatible hash；
- Frozen List/Set/Map/Entry 的 equality/query 使用显式 pair traversal；
- identity-pair memo 使同一共享 pair 只比较一次；
- comparison budgets：depth=256、pairs=16384、edges=131072、canonical nodes=16384；
- Set/Map 使用跨双根 canonical IDs，List/Optional 按顺序精确比较；
- hash 只作快速拒绝，hash collision 后继续精确比较；
- Frozen receiver 不调用普通外部 Collection 的容器 equals/hashCode；
- comparison 超限稳定抛出 `ComparisonLimitException`，不依赖 JVM Error；
- I001～I004、Context/Result、循环、null、未知对象及 Publication 原子性合同保持；
- 未实现 T13/T14/T15 或 P2～P7 runtime。

## Validation

- Valid RED：`c3a78498e595d0006334c8ec382c72c830142d19` / Run `30983520984` / `6 failures, 0 errors`
- First GREEN：`6e3cb1dca3c55ad32aac335c51c552be37457f5d` / Run `30984182632` — SUCCESS
- Clean-code Head：`304a2156ff5e86c2a45213d4e917f17b9a172831`
- P0 Run：`30984394393` — SUCCESS
- Artifact：`8921466813`
- SHA-256：`3a2002648c03c082f649991317e5ef3abbb167df6d99327dfa23c9e787d2fe6d`
- I005：`16/16`；T12：`99/99`；Compiler：`418/418`；Normal：`538/538`
- Surefire XML：`96`；Errors/Skipped：`0/0`
- 12 modules / Java release 8 / intentional failure gate：PASSED
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Recovery

- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t12-r05/completion-report.json`
- Review：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t12-r09.md`
- Revision Lock：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/revision-lock-p1-t12-r05.json`
- Machine checkpoint：`project_doc/version/V_1.0/tdd_p1_t12_r05_completion.json`
- 所有 `@Override` 独占一行，方法和重要逻辑使用中文注释；
- 仅在用户明确授权后合并 PR #27；
- TASK-P1-T13：`BLOCKED_UNTIL_PR_27_MERGE`。
