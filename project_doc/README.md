# P1—P8 研发事实目录

本目录是 `doc-eq-code-Dgremlin` 后续 P1—P8 阶段使用的 `common-develop` 项目事实根目录。

## 开发环境绑定

- 仓库工作树：`/mnt/data/doc-eq-code-Dgremlin-unpacked/doc-eq-code-Dgremlin`
- 分支：`dev_all`
- Agent 环境：`/mnt/data/codex-env`
- Agent 入口：`/mnt/data/codex-env/AGENTS.md`
- Agent 配置：`/mnt/data/codex-env/agents.toml`
- Prompt 目录：`/mnt/data/codex-env/prompts`
- Skill：`/mnt/data/codex-env/skills/common-develop`
- Skill 版本：`2.28`
- 项目事实根：`project_doc`

## 后续阶段

按以下顺序执行，不跨阶段提前实现：

```text
P1 -> P2 -> P3 -> P4 -> P5 -> P6 -> P7 -> P8
```

每个阶段开始前，由 `ProjectManagerAgent` 按 `common-develop` 规则初始化或恢复任务，使用统一上下文入口：

```bash
python3 /mnt/data/codex-env/skills/common-develop/scripts/ai_context.py resolve \
  -g ProjectManagerAgent \
  --task-type {TASK_TYPE} \
  --phase {PHASE} \
  --prompt "{CURRENT_REQUEST}" \
  --doc-root project_doc \
  --version {VERSION} \
  --target {TARGET_ID} \
  --json
```

## 事实源

P1—P8 的总体范围和阶段任务继续以以下文档为基线：

- `doc/mix-framework-technical-remediation-plan.md`
- `doc/mix-framework-p0-p8-detailed-task-plan.md`
- `docs/remediation/P0/`

本目录只保存后续阶段产生的项目事实、revision、Evidence、Review、状态和 handoff，不复制 Agent 或 Skill 的机器契约。

## 当前状态

- P0：实现已提交远端，动态 CI 回执仍需单独核验。
- P1：尚未启动。
- 当前默认 Agent：`ProjectManagerAgent`。
