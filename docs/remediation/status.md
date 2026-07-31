# 整改阶段状态

| 阶段 | 状态 | 说明 |
|---|---|---|
| P0 | PASSED | 本地完整正式验证通过；核心、MySQL、故意失败门禁和静态校验均已形成同一提交的 Evidence |
| P1 | IN_PROGRESS | `REQCONF-R04`、`REQAN-R05`、`BM-R05`、`DESIGN-R05` 已通过；当前进入 `test_design` I007，输入为 `DESIGN-R05@0b37a9b4dd48` |
| P2-P8 | TODO | P1 完成并通过退出门禁前不得初始化实施任务 |

> GitHub Actions 保留为辅助回归；P1 生命周期以 `project_doc/version/V_1.0/task/P1-COMPILER-F01/` 下的 Task、StageOutcome、Review、Evidence 和 Git checkpoint 为准。
