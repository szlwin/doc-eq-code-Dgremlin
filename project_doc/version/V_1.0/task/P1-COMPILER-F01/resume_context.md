# P1-COMPILER-F01 恢复上下文

- 当前逻辑任务：`TASK-P1-T12 / I006` 已完成
- 当前有效 Completion：`COMPLETION-P1-T12-R06@ce8c92523256`
- 失效但保留：`COMPLETION-P1-T12-R01@c6a515820972`、`R02@5d5a7d72119b`、`R03@4d4cd5c4c049`、`R04@923129b1f20d`、`R05@304a2156ff5e`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- 状态：`COMPLETED / PASSED`
- Base：`dev_all@3f53ea4a31b0b1366aad383f665736b0487d4d00`
- Branch：`feature/p1-t12-compiler-pipeline-20260804-2331`
- PR：`#27`
- Design：`DESIGN-R43@P1-T12-REWORK-I006`
- Plan：`TP-P1-COMPILER-F01-R39@P1-T12-REWORK-I006`
- TDD：`TDD-P1-T12-R06@788f475d60e4`
- Architecture：`DEVSKEL-P1-T12-R06@788f475d60e4`
- Development：`DEV-P1-T12-R06@ce8c92523256`
- Code Review：`CODEREVIEW-P1-T12-R11@ce8c92523256`
- Testing：`TESTING-P1-T12-R06@ce8c92523256`
- Reviews：`REV-000611`～`REV-000633`
- Evidence：`EVD-000966`～`EVD-000990`
- Open P0/P1/P2：`0 / 0 / 0`

## Current contract

- final Pass prepare-only，Pipeline 在完整 Diagnostic 门禁后唯一调用 publisher；
- ERROR/cancel/timeout/Clock/timing/Pass 异常和 candidate 缺失路径 publisher=0；
- Warning/Info 保留，成功路径 publisher=1，PUBLISHED 不可逆；
- artifact freeze 使用显式 traversal stack、VISITING/FROZEN memoization 与 snapshot budgets；
- equality/query 使用非递归 operation-level comparison；
- 单次公开查询全部候选共享 pair state、canonical metadata、scalar intern table 和总预算；
- `VISITING/EQUAL/NOT_EQUAL` 已完成 pair 跨候选复用；
- List equality 使用 Iterator，不读取普通 List 的 `size()` 或 `get(index)`；
- List/Set/Map/Entry canonicalization 增量读取，不整体复制外部 Collection；
- edge/node budget 在 `next()`、key/value、metadata 保存和 task push 前生效；
- comparison budgets：depth=256、pairs=16384、edges=131072、canonical nodes=16384；
- Set/Map 使用共享 canonical IDs，hash 仅快速拒绝；
- iterator 业务异常原样传播，预算超限稳定抛 `ComparisonLimitException`；
- I001～I005、Context/Result、Publication 原子性及历史错误合同保持；
- 未实现 T13/T14/T15 或 P2～P7 runtime。

## Validation

- Valid RED：`788f475d60e4864fc6c11bfffee3ff925aa757ac` / Run `30991106416` / `7 failures, 0 errors`
- First GREEN：`91fe23a388d6fc62376222f36a291e8d00544f6a` / Run `30992157198` — SUCCESS
- Clean-code Head：`ce8c9252325642cf45e89f71aaa1f807d4916aca`
- P0 Run：`30992489987` — SUCCESS
- Artifact：`8924724966`
- SHA-256：`f0d5b9ce6c44a922b9bdd534c82f0e235912588f97ced16c117d9b57774a54a4`
- I006：`18/18`；T12：`117/117`；Compiler：`436/436`；Normal：`556/556`
- Surefire XML：`98`；Errors/Skipped：`0/0`
- 12 modules / Java release 8 / intentional failure gate：PASSED
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Recovery

- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t12-r06/completion-report.json`
- Review：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t12-r11.md`
- Revision Lock：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/revision-lock-p1-t12-r06.json`
- Machine checkpoint：`project_doc/version/V_1.0/tdd_p1_t12_r06_completion.json`
- Skill baseline：`common-develop-v2.44-rc8@4787876e135d347e9f37580910e2d28b09ea2ba4`；guard=`DIRTY / HEAD_MATCHES / CRITICAL_DRIFT_0`；
- 所有 `@Override` 独占一行，方法和重要逻辑使用中文注释；
- 仅在用户明确授权后合并 PR #27；
- TASK-P1-T13：`BLOCKED_UNTIL_PR_27_MERGE`。
