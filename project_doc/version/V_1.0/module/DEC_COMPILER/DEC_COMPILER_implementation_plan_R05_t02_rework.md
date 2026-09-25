# DEC_COMPILER Implementation Plan R05 — TASK-P1-T02 REWORK I002

## 1. 输入 Revision

- 基线：`dev_all@f88f45731e16868bfacb489b63e3086aae49d018`
- T01 Completion：`COMPLETION-P1-T01-R04@ee99223a243f`
- 设计：`DESIGN-R09@P1-T02-REWORK-I002`
- 被替代 T02 Completion：`COMPLETION-P1-T02-R01@643b44a8b72a`

## 2. 执行模式

`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 3. 允许范围

- `pom.xml`
- `dec-core-compiler/**`
- `project_doc/version/V_1.0/doc/DEC_COMPILER/**`
- `project_doc/version/V_1.0/task/P1-COMPILER-F01/**`
- `project_doc/version/V_1.0/tdd_p1_t02_r02_completion.json`

不得修改 `dec-core-context` 的生产代码，不得启动 T03。

## 4. 串行任务

### TASK-P1-T02-I002-TDD

- Owner：`TddAgent`
- 目标：把旧 T02 API 重放到最新 T01 基线，并增加精确模型身份与 diagnostics 单一事实源测试；
- 预期 RED：生产和测试源码均以 Java release 8 编译成功，新增合同测试失败，既有 T01 测试保持通过；
- 输出：`TDD-P1-T02-R02@<sha>`；
- Reviewer：`TDDReviewAgent`。

### TASK-P1-T02-I002-ARCH

- Owner：`DevelopAgent`
- Depends on：`TASK-P1-T02-I002-TDD`
- 目标：建立成功发布结果的统一验证边界，但以显式 Architecture Skeleton 行为保持受控 RED；
- 输出：`DEVSKEL-P1-T02-R02@<sha>`；
- Reviewer：`ArchitectureReviewAgent`、`SpecComplianceReviewAgent`。

### TASK-P1-T02-I002-DEV

- Owner：`DevelopAgent`
- Depends on：`TASK-P1-T02-I002-ARCH`
- 目标：完成精确身份检查、diagnostics 一致性检查、中文契约注释和 Java 8 兼容实现；
- 输出：`DEV-P1-T02-R02@<sha>`；
- Reviewer：`SpecComplianceReviewAgent`、`EngineeringStandardsReviewAgent`、`ArchitectureReviewAgent`。

### TASK-P1-T02-I002-TEST

- Owner：`TestAgent`
- Depends on：`TASK-P1-T02-I002-DEV`
- 目标：执行 Compiler 合同测试、T01 Context 回归、完整 Reactor 和失败阻断门禁；
- 输出：`TESTING-P1-T02-R02@<sha>`；
- Reviewer：`TDDReviewAgent`、`TestEvidenceReviewAgent`。

### TASK-P1-T02-I002-COMPLETE

- Owner：`CompletionVerificationAgent`
- Depends on：`TASK-P1-T02-I002-TEST`
- 目标：核对所有 Revision、Review、Evidence、开放 Finding 与 PR 状态；
- 输出：`COMPLETION-P1-T02-R02@<sha>`；
- Reviewer：`CompletionVerificationAgent`。

## 5. Acceptance Assertions

- `ASSERT-T02-R09-001`：值相等但不同实例的模型与 Context 组合被拒绝；
- `ASSERT-T02-R09-002`：成功结果 diagnostics 与模型 diagnostics 不一致时被拒绝；
- `ASSERT-T02-R09-003`：合法成功结果复用模型 diagnostics、Digest 与版本事实；
- `ASSERT-T02-R09-004`：失败结果不暴露候选模型或 Context；
- `ASSERT-T02-R09-005`：Compiler 模块只依赖 Context；
- `ASSERT-T02-R09-006`：完整 Reactor、Java 8 与 P0 失败阻断门禁通过。

## 6. 停止条件

- 最新 `dev_all` 不包含 PR #16；
- 需要修改 T01 生产代码；
- 需要扩大到 SourceGraph、Frontend 或 Pipeline 实现；
- 测试无法区分实现缺陷与环境故障；
- 出现未关闭 P0/P1 Finding。

## 7. Git 策略

在新分支 `feature/p1-t02-rework-i002-20260802-1116` 上执行；PR #15 保留为被新 T01 基线替代的历史，在新 PR 可审查后关闭并注明 superseded。新 PR 目标分支为 `dev_all`。
