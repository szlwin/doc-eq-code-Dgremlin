# TASK-P1-T14 — Candidate EngineContext Builder

- Current Iteration：`I001`
- Status：`IN_PROGRESS / DESIGN_PLAN_FROZEN`
- Base：`dev_all@3e4da420d2ef5ada8398aefbbeabb37964e384ce`
- Dependency：`COMPLETION-P1-T13-R03@5075793d06cc`
- Branch：`feature/p1-t14-candidate-context-20260805-2324`
- Design：`DESIGN-R48@P1-T14-I001`
- Plan：`TP-P1-COMPILER-F01-R44@P1-T14-I001`
- PR：`PENDING`
- Open P0/P1/P2：`0 / 0 / 0`

## Goal

按固定四阶段冻结前九个 Pass 生成的发布事实，构造完整 candidate `EngineContext`；最终 Publication Pass 只准备 candidate，Pipeline 保持唯一 publisher capability。

## Scope

- `CompiledModelSetBuilder`；
- frozen candidate input artifact；
- `CandidateContextPublicationPass`；
- `PublicationPassContext` 只读 Diagnostic 快照；
- T14 定向和独立 Review 测试；
- Review/Evidence/Completion。

## Excluded

- T15 旧模块退役；
- Starter 组装；
- ContextPublisher、PublicationRequest、EngineContext CAS；
- P2～P7 runtime。

## Gate

Design/Plan 已冻结。下一阶段为架构骨架与有效 RED。未经用户明确授权不得合并后续 PR；T15 在 T14 PR 合并前保持阻断。
