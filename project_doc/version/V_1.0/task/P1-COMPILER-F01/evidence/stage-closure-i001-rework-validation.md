# TASK-P1-STAGE-CLOSURE I001 — Rework Validation Evidence

- Logical Task：`TASK-P1-STAGE-CLOSURE`
- Iteration：`I001`
- Base：`dev_all@81aa3b40129d10a08b3f1a20ba6312b4015b9079`
- PR：`#31`
- Test-only RED Revision：`e565163c746e5b7e1fb09a7fa47912065d6ea627`
- Code/Test GREEN Revision：`b603579d75770ca07760522e2df218047f6708ac`
- 本次返修验证采集时状态（历史快照，已由本文末尾 Completion 收口结论取代）：`CI_GREEN / FINAL_REVIEW_PENDING`

## RED evidence

P0 Run `31147472707` 在 test-only Revision 上真实失败。`dec-core-compiler` testCompile 报告新增负向测试要求三参数 `ClasspathDocumentSourceProvider(ClassLoader, AllowedRoot, long)`，而生产实现尚未提供该 API。

该 RED 只包含测试变化，用来证明以下四个 Finding oracle 在实现前不可满足：

1. AllowedRoot 内 symlink 单文件不得逃逸到 root 外；
2. 文件集 symlink cycle 不得被递归跟随；
3. 单 Source 超过读取预算时必须在完整读取前失败；
4. 同一次文件集解析的累计读取不得超过预算。

## GREEN evidence

P0 Run `31147778389` 在 Code/Test Revision `b603579d75770ca07760522e2df218047f6708ac` 上完成：

- `core-verify` Job `92770789003` — SUCCESS；
- `mysql-it` Job `92770789019` — SUCCESS；
- Core Artifact `8982191285` — SHA-256 `2c7103f36ed4aa12e891408a50a855a003b3dee45f87e808754cea9a2078d328`；
- MySQL Artifact `8982163220` — SHA-256 `5af08b353a68af719700ec14c940a14aebccc5f0534c6d7b64db6978374c17b9`。

### Core

- `ClasspathDocumentSourceProviderTest`：7/7 passed；
- `dec-core-compiler`：511 tests，0 failures/errors/skipped；
- `dec-core-starter`：13 tests，0 failures/errors/skipped；
- `CompilerBootstrapStageClosureTest`：3/3 passed；
- T14 provenance gate：PASSED；
- T15 retirement gate：PASSED；
- intentional failure gate：PASSED；
- JaCoCo/report/provenance artifact steps：PASSED。

### MySQL

- MySQL 8.0.46 service：healthy；
- `RuleTests`、`DirectoryTest`、`OrderTest`：3/3 passed；
- Failures/Errors/Skipped：0/0/0；
- `verify_dec_demo_mysql_it.py`：`PASSED`；
- 必需 execution markers：3/3；
- 最终业务表计数 order/order-detail/pay/pay-detail/product/user=`1/1/1/1/1/3`。

## Finding disposition before final Review

- `FND-P1-STAGE-001`：CLOSED by Stage Closure e2e regression；
- `FND-P1-STAGE-003`：CLOSED by physical path/symlink fail-closed + two negative tests；
- `FND-P1-STAGE-004`：CLOSED by streaming/aggregate byte budget + two negative tests；
- 历史采集点的 `FND-P1-STAGE-002`：当时 human-readable repository traceability 已同步，但正式 machine record 尚未迁移，因此仍为 OPEN/P2；该状态已在后续 canonical machine-state 迁移中关闭，详见本文末尾 Completion 收口记录。

本 Evidence 不替代独立 Code Review，也不自行创建新的 common-develop Attempt/Revision ID。

## Independent Review and canonical machine closure — 2026-08-07

后续独立复核在精确 reviewed Head `75559ecc2e4791eddee166cf3010128130e27078` 上确认：

- `FND-P1-STAGE-003` / `FND-P1-STAGE-004` 保持 CLOSED；没有新的代码级 P0/P1；
- P0 Run `31148550742`：`core-verify` 与 `mysql-it` 均 SUCCESS；
- Artifact `8982454725` 的 SHA-256 为 `a1d04b81b259bd83a42a75ee180556748d135de82ae984dd8dd6c4db6a4431ac`，下载后复算一致；
- Provider 7/7、Compiler 511/511、Starter 13/13、Stage Closure 3/3；T14、intentional failing-test gate、T15 均 PASSED。

正式 common-develop machine-state 已完成迁移：

- Code Review I008：`CODEREVIEW-P1-STAGE-CLOSURE-R01@75559ecc2e47`，`REV-000077`～`REV-000083` 全部 PASSED；
- Testing I009：`TESTING-P1-STAGE-CLOSURE-R01@75559ecc2e47`，`REV-000084` PASSED；
- Completion I009：`COMPLETION-P1-STAGE-CLOSURE-R01@75559ecc2e47` PASSED；
- Current StageOutcome：`SO-P1-COMPILER-F01-CODE_REVIEW-I008`、`SO-P1-COMPILER-F01-TESTING-I009`、`SO-P1-COMPILER-F01-COMPLETION_VERIFICATION-I009`；
- `long_task validate`、`risk_detect validate`、`evidence validate`、`acceptance validate` 均 PASSED；
- Open P0/P1/P2=`0/0/0`；临时 export workflow 已删除；reviewed Head 之后未引入 project_doc 之外的新差异。

因此 `FND-P1-STAGE-002 / P2` 已由 canonical 状态机迁移正式关闭。本文件继续作为外部/历史证据，不替代 machine state。
