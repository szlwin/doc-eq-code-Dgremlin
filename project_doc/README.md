# P1—P8 研发事实目录

本目录是 `doc-eq-code-Dgremlin` 后续 P1—P8 阶段使用的 `common-develop` 项目事实根目录。

## 开发环境绑定

- 仓库工作树：`/mnt/data/doc-eq-code-Dgremlin`
- 当前分支：`dev_all`
- 当前提交：以本地 `git rev-parse HEAD` 为准
- Skill 活动入口：`/mnt/data/common-develop-current`
- Skill 实际目录：`/mnt/data/skills/common-develop`
- Skill 版本：`2.35`
- 项目事实根：`project_doc`

不得继续使用旧路径 `/mnt/data/doc-eq-code-Dgremlin-unpacked/doc-eq-code-Dgremlin`、`/mnt/data/codex-env/skills/common-develop` 或 common-develop `2.28/2.34` 的派生状态作为当前执行事实。

## 后续阶段

按以下顺序执行，不跨阶段提前实现：

```text
P0 -> P1 -> P2 -> P3 -> P4 -> P5 -> P6 -> P7 -> P8
```

阶段、阶段内任务和 Review 全部串行；同一时刻只允许一个任务或一个 Review 处于运行状态。

每个阶段开始前，由 `ProjectManagerAgent` 按 common-develop 2.35 初始化或恢复任务，使用统一上下文入口：

```bash
python3 /mnt/data/common-develop-current/scripts/ai_context.py resolve \
  -g ProjectManagerAgent \
  --task-type {TASK_TYPE} \
  --phase {PHASE} \
  --prompt "{CURRENT_REQUEST}" \
  --doc-root project_doc \
  --version {VERSION} \
  --target {TARGET_ID} \
  --json
```

## 唯一总体计划事实源

- `project_doc/docs/_plans/mix-framework-technical-remediation-plan.md`
- `project_doc/docs/_plans/mix-framework-p0-p8-detailed-task-plan.md`
- `docs/remediation/P0/`

仓库根 `doc/` 和 `docs/` 下的同名计划文件只保留历史链接跳转，不再维护正文。

## 已确认架构决策

- `dec-expand-declaration` 为历史临时模块，从 P1 起整体删除；
- 不抽取旧代码、不建立 Adapter、不迁移旧 declaration 配置；
- `dec-demo/src/main/resources/mix` 是 P1—P8 唯一目标业务契约；
- Business 仅是统一编译模型中的逻辑作用域，不是独立项目。

详见 `docs/_plans/dec-expand-declaration-retirement-decision.md`。

## 当前状态

- P0：`PASSED`。本地核心与 MySQL 正式验证已通过；GitHub Actions 仅作非阻断辅助回归。
- P1：已登记 `P1-COMPILER-CR01` 范围变更；原需求确认、分析、业务模型和设计 Revision 转为历史输入，必须从 requirement_confirmation 重新执行，不能进入 `test_design` 或开发。
- P2—P8：`TODO`。
- 当前默认 Agent：`ProjectManagerAgent`。
