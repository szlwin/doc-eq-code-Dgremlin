# P1—P8 研发事实目录

本目录是 `doc-eq-code-Dgremlin` 后续 P1—P8 阶段使用的 `common-develop` 项目事实根目录。

## 开发环境绑定

- 仓库分支：`dev_all`
- Skill 全局入口：`/home/oai/skills/common-develop`
- Skill 活动入口：`/mnt/data/common-develop`
- Skill 持久目录：`/mnt/data/common-develop`
- Skill 版本：`2.44 RC9`
- 项目事实根：`project_doc`

`/mnt/data/common-develop` 为长期持久目录，不得随意删除、移动或覆盖其中的文件、子目录与 `.git`。

不得继续使用旧路径 `/mnt/data/doc-eq-code-Dgremlin-unpacked/doc-eq-code-Dgremlin`、`/mnt/data/codex-env/skills/common-develop` 或 common-develop `2.28/2.34/2.35` 的派生状态作为当前执行事实。

## 阶段顺序

按以下顺序执行，不跨阶段提前实现：

```text
P0 -> P1 -> P2 -> P3 -> P4 -> P5 -> P6 -> P7 -> P8
```

阶段、阶段内任务和 Review 全部串行；同一时刻只允许一个任务或一个 Review 处于运行状态。

每个阶段开始前，由 `ProjectManagerAgent` 按当前 common-develop release contract 初始化或恢复任务，统一从当前任务的机器状态、StageOutcome、Review、Evidence 和 Git checkpoint 恢复，不使用过期摘要代替机器事实。

## 唯一总体计划事实源

- `project_doc/docs/_plans/mix-framework-technical-remediation-plan.md`
- `project_doc/docs/_plans/mix-framework-p0-p8-detailed-task-plan.md`
- `docs/remediation/P0/`

仓库根 `doc/` 和 `docs/` 下的同名计划文件只保留历史链接跳转，不再维护正文。

## 当前状态

- P0：`PASSED`。本地核心验证、MySQL 集成验证、故意失败门禁和静态校验均已有正式 Evidence。
- P1：`PASSED / MERGED / ARCHIVED`。PR #31 已完成 Stage Closure；P1 `wk -d` 归档 PR #32 已合并至 `dev_all@0403cd43325a6290eebfbbdf48604f252707c147`，合并后 P0 Build Gate `31190480938` SUCCESS。
- P2：`READY / REQUEST_INTAKE_REGISTERED`。Target `FEATURE-DESC-3361AD2E54FC` 已按 `NEW_REQUIREMENT / STANDARD_FEATURE_FLOW` 初始化；当前停在 `requirement_confirmation` I001，尚未开始阶段 attempt。
- P3—P8：`TODO`。
- 当前默认 Agent：`ProjectManagerAgent`；下一执行 Agent：`RequirementConfirmationAgent`。下一正式动作是获取 P2 `requirement_confirmation` task context 并启动该阶段，不得直接进入分析、设计或开发。
