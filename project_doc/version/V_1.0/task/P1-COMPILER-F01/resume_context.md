# P1-COMPILER-F01 恢复上下文

- 当前逻辑任务：`TASK-P1-STAGE-CLOSURE / I001`
- 状态：`REWORK_VALIDATED / CI_GREEN / FINAL_REVIEW_PENDING`
- Base：`dev_all@81aa3b40129d10a08b3f1a20ba6312b4015b9079`
- Branch：`rework/p1-stage-closure-20260807`
- PR：`#31 / DRAFT / OPEN / NOT_MERGED`
- Test-only RED Revision：`e565163c746e5b7e1fb09a7fa47912065d6ea627`
- Code/Test GREEN Revision：`b603579d75770ca07760522e2df218047f6708ac`
- GREEN Run：`31147778389`
- Candidate Open P0/P1/P2：`0 / 0 / 1`（P2 为 machine state migration）
- Final independent Review：`PENDING`

## Superseded but retained

- PR #30 / T15 I003 的 Development、Review、Testing、Completion 历史全部保留；PR #30 已在本轮开始前由外部操作合并，当前 `dev_all` 合并提交为 `81aa3b40129d10a08b3f1a20ba6312b4015b9079`；
- Stage Closure 文档中旧的 `DEVELOPMENT_IMPLEMENTED / REMOTE_CI_PENDING`、`Code Review: PENDING`、`Testing: PENDING` 说明已被本次返修/CI 证据 supersede；
- 不创建新的 common-develop Iteration/Attempt ID；本轮继续使用既有 `TASK-P1-STAGE-CLOSURE / I001`，直至正式状态机可恢复。

## Current contract

- Stage Starter 仍必须通过生产 `DocumentSourceProvider`、XML/YAML Frontend、固定十阶段 Pipeline 与显式 CAS Publisher 完成 compile-and-publish；
- 第二次失败编译不得发布、不得污染此前成功 Context；
- exploded-directory classpath 资源不得通过 symlink 逃逸 AllowedRoot 或构造目录循环；
- Provider 必须在完整读取前执行硬字节预算，文件集必须执行累计预算；
- `CompilerBootstrap.sourceBudgets(... maxTotalBytes)` 同时约束 Provider 与 `SourcePolicy`；
- T15 Declaration Runtime retirement 合同继续有效，不得回流旧全局 Starter/Declaration Runtime；
- Stage Completion 前必须完成最终独立 Review 和 Testing/Completion gate。

## Validation

- RED Run：`31147472707` — expected RED；
- GREEN Run：`31147778389` — SUCCESS；
- Core Job：`92770789003`；MySQL Job：`92770789019`；
- Core Artifact：`8982191285`；SHA-256：`2c7103f36ed4aa12e891408a50a855a003b3dee45f87e808754cea9a2078d328`；
- MySQL Artifact：`8982163220`；SHA-256：`5af08b353a68af719700ec14c940a14aebccc5f0534c6d7b64db6978374c17b9`；
- Provider：7/7；Compiler：511/511；Starter：13/13；Stage Closure e2e：3/3；
- MySQL：3/3 passed，3/3 markers，0 failures/errors/skipped；
- Database：order/order-detail/pay/pay-detail/product/user=`1/1/1/1/1/3`；
- T14 provenance / T15 retirement：PASSED。

## Resume order

1. 检查当前 PR #31 Head 与 traceability 文档提交的 CI；
2. 对精确最终 Head 执行独立 Code Review；
3. Review PASS 后，在 Head 未变化前提下收录同一成功 CI 为正式 Testing；
4. 恢复有效 common-develop baseline，将 state/outcome overlay 迁移为正式 machine record 并关闭 `FND-P1-STAGE-002`；
5. 再执行 Completion；
6. 仅将 PR 标记 Ready for Review，不得未经用户明确授权合并。

## Recovery files

- Task：`project_doc/version/V_1.0/task/P1-COMPILER-F01/TASK-P1-STAGE-CLOSURE.md`
- Validation：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/stage-closure-i001-rework-validation.md`
- State：`project_doc/version/V_1.0/task/P1-COMPILER-F01/task_state.md`
- Outcomes：`project_doc/version/V_1.0/task/P1-COMPILER-F01/stage_outcomes.md`
- Handoff：`project_doc/version/V_1.0/task/P1-COMPILER-F01/handoff.md`
