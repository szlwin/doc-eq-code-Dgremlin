# P1-COMPILER-CR01：整体退役 `dec-expand-declaration`

- 变更类型：范围与架构边界变更
- 关联原需求：`P1-COMPILER`
- 状态：已纳入 `REQCONF-R02` 正式确认范围，待同 Revision 必需 Review 与 StageOutcome

## 变更原因

用户确认 `dec-expand-declaration` 是临时项目，System 和目标核心运行时不依赖该模块。后续以 `dec-demo/src/main/resources/mix` 为唯一业务配置契约，该模块整体废弃。

## 变更内容

1. P1 增加模块、依赖、源码和正式入口的整体删除；
2. 不抽取旧代码、不建立 LegacyDeclarationAdapter、不迁移旧 declaration XML/YAML；
3. Business 只作为统一 AST/EngineContext 的逻辑作用域，不是独立项目；
4. P2—P6 的能力直接依据 `mix` 重写；
5. P7 删除旧模块能力抽取和 Adapter 任务，改为核心 Session/事务/资源生命周期收敛；
6. P8 增加仓库、Reactor、依赖树、artifact 和文档残留验收。

## 影响

- 原需求确认、需求分析、业务模型和设计 Revision 仍保留为历史 Evidence，但因范围发生实质变化，不得直接进入 `test_design`；
- 必须重新执行 requirement_confirmation、requirement_analysis、business_model、design 及串行 Review；
- `dependency_impact.yaml`、追踪矩阵、测试设计和实施计划需在重新分析时生成新 Revision。

## 验收

- 仓库与 Reactor 不含 `dec-expand-declaration`；
- `dec-demo` 不依赖该 artifact；
- 不存在 LegacyDeclarationAdapter；
- 必要业务场景只基于 `mix` 重写；
- P1—P8 文档与模块职责一致。
