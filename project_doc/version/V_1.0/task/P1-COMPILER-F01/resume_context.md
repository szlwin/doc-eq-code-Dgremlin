# P1-COMPILER-F01 恢复上下文

- 版本：V_1.0
- Schema：2
- 当前阶段：test_design
- 当前轮次：TEST_DESIGN-I007
- 任务状态：PARTIAL
- 执行模式：SEQUENTIAL
- 当前执行 Agent：ProjectManagerAgent
- 项目管理 Agent：ProjectManagerAgent
- 下一 Agent：TestDesignAgent
- 下一动作：开始 test_design 阶段，基于 `DESIGN-R05@0b37a9b4dd48` 形成可执行测试设计
- 最近通过门禁：design
- 最近通过 Revision：`DESIGN-R05@0b37a9b4dd48`
- 当前开放 P0/P1 问题：0

## 当前与最近执行

- 当前运行中的任务：无
- 最近完成任务：`TASK-P1-DESIGN-001`，DESIGN I007，PASSED
- 最近 StageOutcome：`SO-P1-COMPILER-F01-DESIGN-I007`，PASSED
- 设计 Review：`REV-000050`～`REV-000056` 全部 PASSED
- 设计 Evidence：`EVD-000284`
- Git checkpoint：`CP-V_1.0-P1-COMPILER-F01-DESIGN-007`

## 下一可执行任务

- Task：`TASK-P1-R2-005`
- Owner：`TestDesignAgent`
- Iteration：`ITER-P1-COMPILER-F01-TEST-DESIGN-007`
- 输入：`REQCONF-R04`、`REQAN-R05`、`BM-R05`、`DESIGN-R05`
- 输出：新的 TESTDESIGN Revision
- Review：DesignReviewAgent、RequirementReviewAgent、TDDReviewAgent、TestEvidenceReviewAgent，按登记顺序串行执行

## 开放问题

- 无。`ISSUE-MR-0001`～`ISSUE-MR-0004` 已由 `DESIGN-R05` 修复并关闭。

## 活跃决策

- `dec-expand-declaration` 整体退役，不建立 Adapter；退役在 P1 实施阶段执行。
- EngineContext 只允许实例级不可变对象，不使用全局 current Context。
- Information 归属 System；BusinessScope 只编排。
- 跨 System expression 只由 common System 拥有。
- ModelAccess selector 先精确匹配 target-main，再精确回退 property path；禁止模糊或跨 View 搜索。

## 恢复检查

1. 先读取 `task_state.md`、`task_plan.md`、`stage_outcomes.md`、`review_issues.md` 和本文件。
2. 确认工作模式为 SEQUENTIAL，且没有运行中的 Attempt 或 Review。
3. 确认测试设计只绑定 `DESIGN-R05@0b37a9b4dd48`，不得复用 DESIGN-R04/R02 Evidence。
4. 从 `TASK-P1-R2-005` 开始，不重复需求、分析、业务模型或设计阶段。
5. test_design 及其独立 Review 通过前，不得进入 implementation_plan、TDD 或开发。
