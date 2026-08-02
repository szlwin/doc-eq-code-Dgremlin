# TASK-P1-T02 REWORK I003

- 任务：`TASK-P1-T02`
- Iteration：`I003`
- 状态：`REWORK`
- 分支：`feature/p1-t02-rework-i002-20260802-1116`
- PR：`#17`，当前保持 Draft
- 基线：`dev_all@f88f45731e16868bfacb489b63e3086aae49d018`
- 当前 Head 输入：`fc269c7d3d47e494e8a8ea413bfacf741f0b3c3f`
- 被推翻 Completion：`COMPLETION-P1-T02-R02@8847b3c7dfac`
- 设计：`DESIGN-R10@P1-T02-REWORK-I003`
- 实施计划：`TP-P1-COMPILER-F01-R06@P1-T02-REWORK-I003`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 重开原因

完整规格 Review 发现 I002 只验证了最终 T01 发布聚合适配，未验证 `DESIGN-R05` 与 `DEC_COMPILER_api_contract.md` 的完整公共 API。I002 Completion、Review、Evidence 和 PR 历史均保留，但“任务完成”和“开放 P0/P1 为 0”结论失效。

## 开放 Finding

- `FND-P1-T02-I003-001` — P1：CompilationRequest 缺少 Source、Frontend、Deadline、Clock 与 Observer 完整 Session 边界；
- `FND-P1-T02-I003-002` — P1：条件发布使用 nullable EngineContext 且 PublicationResult/Status 未分离；
- `FND-P1-T02-I003-003` — P1：CompilationResult 类型形状和 Published accessor 与冻结 API 不一致；
- `FND-P1-T02-I003-004` — P1：Test Oracle 与 Completion 未验证完整 DESIGN-R05/API Contract。

## 门禁

1. 必须先更新设计与测试 Oracle，再修改生产代码；
2. 有效 RED 必须在 Java 8 编译成功后形成；
3. 不修改 `dec-core-context` 生产代码；
4. 不实现 T03 行为；
5. 开放 P0/P1 阻断 Completion；
6. 所有 `@Override` 单独一行；
7. 方法、构造器和重要逻辑使用中文注释；
8. PR #17 完成前保持 Draft；
9. PR #17 合并前 TASK-P1-T03 保持阻断。
