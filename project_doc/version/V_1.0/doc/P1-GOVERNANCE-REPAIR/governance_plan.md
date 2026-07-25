# P1-GOVERNANCE-REPAIR 治理计划

## 目标

在不改写 P1 历史 Revision、Evidence 和 Review 的前提下，修正当前执行环境绑定、总体计划事实源和串行调度规则，使后续 AI 只读取一套当前事实。

## 任务状态

| 任务 | 内容 | 状态 | 主要产物 |
|---|---|---|---|
| GOV-T01 | 更新仓库、分支、Skill 版本和当前状态绑定 | PASSED | `project_doc/README.md`、`project_process.md`、`docs/remediation/status.md` |
| GOV-T02 | 统一 P0—P8 总体计划唯一事实源 | PASSED | `project_doc/docs/_plans/`；旧路径改为跳转说明 |
| GOV-T03 | 阶段、任务和 Review 全部改为串行 | PASSED | 详细任务计划与项目过程说明 |
| GOV-T04 | 形成 P0/P1 越序决策记录 | DEFERRED | 用户本轮未要求执行 |

## 执行约束

- 不修改 P1 已有 Revision、Evidence、Review 和不可变快照。
- 原计划路径继续存在，但只作为历史链接兼容层。
- 后续所有 Agent 以 `project_doc/docs/_plans/` 为总体计划唯一事实源。
- 同一时刻只允许一个任务或一个 Review 处于运行状态。
