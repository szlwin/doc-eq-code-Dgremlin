<!-- template: common-develop/work-v4 -->
<!-- common-develop-work-mode: model_code=false page_design=false minimal=false auto=true architecture_review=true git_checkpoint=true git_push=true -->
<!-- common-develop-mutation-policy: review_only=false -->
# V_1.0 工作分工

> 工作模式（wk）：前后端伪代码=关闭；页面设计=关闭；最小流程=关闭；自动执行=启用；两阶段开发Review=启用；Git阶段提交=启用；Git自动推送=启用；只Review写入边界=关闭。

> 当前事实：P1 Stage Completion 已 PASSED；PR #31 已合并至 `dev_all@7f001bb0d7e529f49344a8b38224bde8e3b9d28e`；`common-develop -d` 增量归档已完成。

> P2 当前处于 RC23 docs/Evidence-only formalization：允许更新 task facts / project docs，禁止修改 tests、config 与 production code；如后续进入需要代码修复的 remediation，必须先显式退出 review-only 并重新同步 work mode。

| 需求编号 | 需求名称 | 功能名称 | 功能编号 | 需求分析 | 页面设计 | 设计 | 架构设计 | 测试用例 | 前端伪代码 | 后端伪代码 | 开发 | 架构代码 | Review | 测试 | 归档 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| P1-COMPILER | 统一 AST、Registry 与 Compiler 骨架 | 统一编译上下文骨架 | P1-COMPILER-F01 | 已完成 | 无 | 已完成 | 已完成 | 已完成 | 无 | 无 | 已完成 | 已完成 | 已完成 | 已完成 | 已完成 |

## 使用说明

- 未使用 `wk -mc` 时，“前端伪代码”和“后端伪代码”统一填写为“无”。
- 使用 `wk -mc` 时，前后端伪代码进入工作范围，新增或原为“无”的单元格填写为 `AIW`。
- 未使用 `wk -v` 时，“页面设计”统一填写为“无”。
- 使用 `wk -v` 时，“页面设计”进入工作范围，新增或原为“无”的单元格填写为 `AIW`。
- 使用 `wk -min` 时，“测试用例”“测试”统一填写为“无”，并跳过 `test_design`、`tdd`、`testing` 三个测试阶段。
- 使用 `wk -auto` 时，后续阶段在全部必需门禁、Review、证据和阻塞检查通过后自动推进，不再等待用户确认。
- 使用 `wk -gc/--git-checkpoint` 时，每个阶段门禁通过后由 ProjectManagerAgent 创建本地 Git 检查点；不自动 push 或 merge。
- 使用 `wk -review-only` 时，只允许 task facts / project docs 写入；tests、config、production code 写入被拒绝。该模式不改变 Review 结论，也不绕过 exact-revision、Evidence 或 P0/P1 门禁。
- `wk -mc`、`wk -v`、`wk -min`、`wk -auto`、`wk -gc`、`wk -review-only` 可以按契约组合；自动执行不绕过 Agent 权限、Review、证据、P0/P1 阻塞或安全边界。多个需求始终按声明顺序逐个执行。
- “页面设计”仅指页面业务字段、操作、状态和交互行为设计，不包含视觉风格、高保真原型或像素级设计。
