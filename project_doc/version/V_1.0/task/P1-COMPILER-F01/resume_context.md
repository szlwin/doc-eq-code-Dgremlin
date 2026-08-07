# P1-COMPILER-F01 恢复上下文

- 当前逻辑任务：`TASK-P1-STAGE-CLOSURE / I001`（外部恢复记录）
- Canonical Target：`P1-COMPILER-F01`
- 状态：`STAGE_COMPLETED / MACHINE_SYNCED / MERGED / ARCHIVED`
- Base：`dev_all@81aa3b40129d10a08b3f1a20ba6312b4015b9079`
- Branch：`rework/p1-stage-closure-20260807`
- PR：`#31 / MERGED / 7f001bb0d7e529f49344a8b38224bde8e3b9d28e`
- Reviewed Head：`75559ecc2e4791eddee166cf3010128130e27078`
- Stage Closure Published Head / Fact Sync Source Head：`06e70cbb9fd81f9e7e96c840f29ffc7e67ce53b6`
- Final P0 Run：`31177897571` — SUCCESS（dev_all merge commit）
- Reviewed P0 Run：`31148550742` — SUCCESS
- Code Review：`I008 / CODEREVIEW-P1-STAGE-CLOSURE-R01@75559ecc2e47 / PASSED`
- Testing：`I009 / TESTING-P1-STAGE-CLOSURE-R01@75559ecc2e47 / PASSED`
- Completion：`I009 / COMPLETION-P1-STAGE-CLOSURE-R01@75559ecc2e47 / PASSED`
- Open P0/P1/P2：`0 / 0 / 0`

## Retained history

- PR #30 / T15 I003 的 Development、Review、Testing、Completion 历史全部保留；
- 旧 T01 Completion `COMPLETION-P1-T01-R01@7be02cd9af4c` 保留在历史中，不再作为 Stage Closure 当前 Completion；
- Stage Closure 的外部 overlay I001 保留用于说明 baseline 不可用时期的恢复边界，但已经被 canonical I008/I009 machine history supersede；
- `reopen-phase` stale/reopen 历史与旧 Review/Outcome 均保留，没有手工覆盖 machine JSON。

## Current contract

- Stage Starter 继续通过生产 `DocumentSourceProvider`、XML/YAML Frontend、固定十阶段 Pipeline 与显式 CAS Publisher 完成 compile-and-publish；
- 第二次失败编译不得发布、不得污染此前成功 Context；
- exploded-directory classpath 资源不得通过 symlink 逃逸 AllowedRoot 或构造目录循环；
- Provider 必须在完整读取前执行硬字节预算，文件集必须执行累计预算；
- `CompilerBootstrap.sourceBudgets(... maxTotalBytes)` 同时约束 Provider 与 `SourcePolicy`；
- T15 Declaration Runtime retirement 合同继续有效；
- `FND-P1-STAGE-001/002/003/004` 均 CLOSED。

## Final evidence

- Code Review：`REV-000077`～`REV-000083` 全部 PASSED；
- Testing Evidence Review：`REV-000084` PASSED；
- P0 Run：`31148550742`，`core-verify` / `mysql-it` SUCCESS；
- Artifact：`8982454725`，SHA-256 `a1d04b81b259bd83a42a75ee180556748d135de82ae984dd8dd6c4db6a4431ac`；
- Provider 7/7、Compiler 511/511、Starter 13/13、Stage Closure 3/3；
- `long_task` / `risk` / `evidence` / `acceptance` 均 PASSED；
- 临时 export workflow 已删除，reviewed Head 之后没有新增生产 Java 变化。

## Resume order

Stage Closure、Fact Sync、PR #31 merge 与 P1 wk -d archive 均已完成。恢复时以 `dev_all@7f001bb0d7e529f49344a8b38224bde8e3b9d28e` 和 `project_doc/archive_manifest.yaml` 为当前 P1 闭环事实；P2 尚未启动，下一正式生命周期为 P2 requirement_confirmation。

## Recovery files

- Task：`project_doc/version/V_1.0/task/P1-COMPILER-F01/TASK-P1-STAGE-CLOSURE.md`
- Validation：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/stage-closure-i001-rework-validation.md`
- State：`project_doc/version/V_1.0/task/P1-COMPILER-F01/task_state.md`
- Outcomes：`project_doc/version/V_1.0/task/P1-COMPILER-F01/stage_outcomes.md`
- Handoff：`project_doc/version/V_1.0/task/P1-COMPILER-F01/handoff.md`
