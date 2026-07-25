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

## 当前状态

- P0：`BLOCKED`。静态整改已完成，Maven Wrapper、失败测试阻断和 GitHub Actions `core-verify` 仍需动态回执。
- P1：需求确认、需求分析、业务模型和设计 Revision 已完成；当前门禁阻断，不能进入 `test_design` 或开发。
- P2—P8：`TODO`。
- 当前默认 Agent：`ProjectManagerAgent`。
