# P1-COMPILER-F01 恢复上下文

- 当前逻辑任务：`TASK-P1-T15 / I003` 已完成
- 当前有效 Completion：`COMPLETION-P1-T15-R03@66fa5db14f7b`
- 状态：`COMPLETED / PASSED`
- Base：`dev_all@665dd364975505bb01263885a25b3bb1be767d2b`
- Dependency：`COMPLETION-P1-T14-R03@37fb814b39c5`
- Branch：`feature/p1-t15-retire-declaration-20260806-1354`
- PR：`#30 / OPEN / READY_FOR_REVIEW / NOT_MERGED`
- TDD：`TDD-P1-T15-R01@bff67b86fb55` — VALID
- Development：`DEV-P1-T15-R03@66fa5db14f7b`
- Code Review：`CODEREVIEW-P1-T15-R03@66fa5db14f7b`
- Testing：`TESTING-P1-T15-R03@66fa5db14f7b`
- Open P0/P1/P2：`0 / 0 / 0`

## Superseded but retained

- `CODEREVIEW-P1-T15-R02@7c901332b8e5`；
- `TESTING-P1-T15-R02@7c901332b8e5`；
- `COMPLETION-P1-T15-R02@7c901332b8e5`。

失效原因：`FND-P1-T15-I002-003` 证明 R02 的 MySQL 结论只覆盖环境/Schema，没有业务执行测试。R02 的生产退役实现、retirement gate、TDD 与架构结论不失效。

## Current contract

- Starter、Projection 和 Declaration Runtime 退役合同保持 I001/I002 实现；
- `dec-demo` 增加测试专用 MySQL 装配，不修改生产源码；
- 必需业务场景为 `RuleTests`、`DirectoryTest`、`OrderTest`；
- 三个场景必须执行真实 `ModelContainer`/`DirectoryContainer` 和 JDBC 结果断言；
- Surefire 必需 suite、测试计数和数据库执行标记全部 fail-closed；
- `TestOrderBusiness` 不恢复，禁止 Declaration Runtime 回流。

## Validation

- Code/Test Revision：`66fa5db14f7b0ead00e5d706acd164e1a9f4ff62`
- Run / Attempt：`31119253989 / 3` — SUCCESS
- Core Artifact：`8974119611`；SHA-256：`929a748eea9b70e5b5e2ac15e5501b204f8149c5a62d099c67d85870d85d0bb7`
- MySQL Artifact：`8974141953`；SHA-256：`6a79f5c2140225fcaa4ac7930cd305f959c96b756ec3565f39058a8ea511b7ce`
- Core：112 XML；639 total；638 normal passed；1 intentional failure；0 errors/skipped
- MySQL：3/3 passed；3/3 markers；0 failures/errors/skipped
- Database：order/order-detail/pay/pay-detail/product/user=`1/1/1/1/1/3`

## Recovery files

- Task：`project_doc/version/V_1.0/task/P1-COMPILER-F01/TASK-P1-T15.md`
- Development：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/development-p1-t15-r03.md`
- Review：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t15-r03.md`
- Testing：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/testing-p1-t15-r03.md`
- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t15-r03/completion-report.json`
- Revision Lock：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/revision-lock-p1-t15-r03.md`

仅在用户明确授权后合并 PR #30。
