<!-- template: common-develop/work-v4 -->
<!-- common-develop-work-mode: model_code=false page_design=false minimal=false auto=false git_checkpoint=false -->
# V_1.0 工作分工

> 工作模式（wk）：前后端伪代码=关闭；页面设计=关闭；最小流程=关闭；自动执行=关闭；Git阶段提交=关闭。

| 需求编号 | 需求名称 | 功能名称 | 功能编号 | 需求分析 | 页面设计 | 设计 | 架构设计 | 测试用例 | 前端伪代码 | 后端伪代码 | 开发 | 架构代码 | Review | 测试 | 归档 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| P1-COMPILER | 统一 AST、Registry 与 Compiler 骨架 | 统一编译上下文骨架 | P1-COMPILER-F01 | AIW | 无 | AIW | AIW | AIW | 无 | 无 | 待开发 | 待开发 | AIW | 待测试 | 待归档 |

## 使用说明

- 未使用 `wk -mc` 时，“前端伪代码”和“后端伪代码”统一填写为“无”。
- 使用 `wk -mc` 时，前后端伪代码进入工作范围，新增或原为“无”的单元格填写为 `AIW`。
- 未使用 `wk -v` 时，“页面设计”统一填写为“无”。
- 使用 `wk -v` 时，“页面设计”进入工作范围，新增或原为“无”的单元格填写为 `AIW`。
- 使用 `wk -min` 时，“测试用例”“测试”统一填写为“无”，并跳过 `test_design`、`tdd`、`testing` 三个测试阶段。
- 使用 `wk -auto` 时，后续阶段在全部必需门禁、Review、证据和阻塞检查通过后自动推进，不再等待用户确认。
- 使用 `wk -gc/--git-checkpoint` 时，每个阶段门禁通过后由 ProjectManagerAgent 创建本地 Git 检查点；不自动 push 或 merge。
- `wk -mc`、`wk -v`、`wk -min`、`wk -auto`、`wk -gc` 可以任意组合；`wk -v -min` 表示执行页面业务行为设计但跳过测试阶段。自动执行不绕过 Agent 权限、Review、证据、P0/P1 阻塞或安全边界。多个需求始终按声明顺序逐个执行。
- “页面设计”仅指页面业务字段、操作、状态和交互行为设计，不包含视觉风格、高保真原型或像素级设计。
