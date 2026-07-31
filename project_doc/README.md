# P1—P8 研发事实目录

本目录是 `doc-eq-code-Dgremlin` 后续 P1—P8 阶段使用的 `common-develop` 项目事实根目录。

## 开发环境绑定

- 仓库分支：`dev_all`
- Skill 全局入口：`/home/oai/.codex/skills/common-develop`
- Skill 活动入口：`/mnt/data/common-develop-current`
- Skill 持久目录：`/mnt/data/common-develop`
- Skill 版本：`2.44 RC8`
- 项目事实根：`project_doc`

`/mnt/data/common-develop` 为长期持久目录，不得随意删除、移动或覆盖其中的文件、子目录与 `.git`。

不得继续使用旧路径 `/mnt/data/doc-eq-code-Dgremlin-unpacked/doc-eq-code-Dgremlin`、`/mnt/data/codex-env/skills/common-develop` 或 common-develop `2.28/2.34/2.35` 的派生状态作为当前执行事实。

## 阶段顺序

按以下顺序执行，不跨阶段提前实现：

```text
P0 -> P1 -> P2 -> P3 -> P4 -> P5 -> P6 -> P7 -> P8
```

阶段、阶段内任务和 Review 全部串行；同一时刻只允许一个任务或一个 Review 处于运行状态。

每个阶段开始前，由 `ProjectManagerAgent` 按 common-develop 2.44 RC8 初始化或恢复任务，统一从当前任务的机器状态、StageOutcome、Review、Evidence 和 Git checkpoint 恢复，不使用过期摘要代替机器事实。

## 唯一总体计划事实源

- `project_doc/docs/_plans/mix-framework-technical-remediation-plan.md`
- `project_doc/docs/_plans/mix-framework-p0-p8-detailed-task-plan.md`
- `docs/remediation/P0/`

仓库根 `doc/` 和 `docs/` 下的同名计划文件只保留历史链接跳转，不再维护正文。

## 当前状态

- P0：`PASSED`。本地核心验证、MySQL 集成验证、故意失败门禁和静态校验均已有正式 Evidence。
- P1：`IN_PROGRESS`。`REQCONF-R04`、`REQAN-R05`、`BM-R05`、`DESIGN-R05` 均已通过；当前进入 `test_design` I007，输入设计为 `DESIGN-R05@0b37a9b4dd48`。
- P2—P8：`TODO`。
- 当前默认 Agent：`ProjectManagerAgent`；下一阶段执行 Agent：`TestDesignAgent`。
