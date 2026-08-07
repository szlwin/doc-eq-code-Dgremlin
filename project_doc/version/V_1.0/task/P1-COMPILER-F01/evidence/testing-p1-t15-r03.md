# TESTING-P1-T15-R03 — dec-demo MySQL 业务执行验证

- Evidence ID：`EVD-001109`
- Testing：`TESTING-P1-T15-R03@66fa5db14f7b`
- Iteration：`TASK-P1-T15 / I003`
- Code/Test Revision：`66fa5db14f7b0ead00e5d706acd164e1a9f4ff62`
- Status：`PASSED`

## Validation run

- P0 Run：`31119253989`；Attempt 3：`SUCCESS`；
- Core Artifact：`8974119611`；
- GitHub/独立 SHA-256：`929a748eea9b70e5b5e2ac15e5501b204f8149c5a62d099c67d85870d85d0bb7`；
- MySQL Artifact：`8974141953`；
- GitHub/独立 SHA-256：`6a79f5c2140225fcaa4ac7930cd305f959c96b756ec3565f39058a8ea511b7ce`。

Run 中 Core build and tests、T14 provenance mutation、intentional failure gate、T15 declaration runtime retirement、MySQL 8 初始化、三个业务测试、报告门禁、数据库标记门禁和 Artifact upload 全部通过。

## Infrastructure attempts retained

- Attempt 1：`core-verify` 与 `mysql-it` 在 checkout 前因 GitHub Action 下载服务 `503` 失败；
- Attempt 2：`core-verify` 完整成功，`mysql-it` 仍在 checkout 前因相同平台故障失败；
- Attempt 3：使用新的 GitHub Runner，MySQL 容器与所有仓库步骤真实执行并成功。

前两次只登记为基础设施历史，不作为代码失败，也不用于 Completion。

## Core independent parse

- Surefire XML：112；
- All records：639；
- Normal passed：638；
- Intentional failure：1；
- Errors：0；
- Skipped：0；
- 默认 Profile 中 `dec-demo` contract tests：3/3；MySQL tests 按 Tag 正确排除；
- T14 mutation、T15 retirement 和普通失败门禁均通过。

## MySQL independent parse

机器摘要：

- status：`PASSED`；
- required/discovered classes：3/3；
- tests：3；
- failures/errors/skipped：`0/0/0`；
- missing classes：0。

必需 suite：

1. `dec.demo.model.RuleTests` — PASSED；
2. `dec.demo.directory.DirectoryTest` — PASSED；
3. `dec.demo.system.OrderTest` — PASSED。

## Database evidence

三个成功标记均存在：

- `RuleTests.orderRulesWriteExpectedRowsToBothDatabases`；
- `DirectoryTest.directoryRangeReturnsOnlyRegisterAndAuthUsers`；
- `OrderTest.saveOrderWritesCompleteAggregate`。

最终业务表计数：

- `order_info=1`；
- `order_detail_info=1`；
- `pay_info=1`；
- `pay_detail_info=1`；
- `product_info=1`；
- `user_info=3`。

以上计数只用于独立 CI 证据；每个测试内部还执行更细的字段、范围、删除规则、双数据源和关联 ID 断言。

Testing 结论：`PASSED`。
