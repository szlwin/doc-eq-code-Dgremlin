# DEVSKEL-P1-T14-R01 — Architecture Skeleton Evidence

- Architecture：`DEVSKEL-P1-T14-R01@94fcc64aa6da`
- Task：`TASK-P1-T14 / I001`
- Design：`DESIGN-R48@P1-T14-I001`
- Plan：`TP-P1-COMPILER-F01-R44@P1-T14-I001`
- Base：`dev_all@3e4da420d2ef5ada8398aefbbeabb37964e384ce`
- Evidence：`EVD-001046`～`EVD-001049`

## Skeleton

Design/Plan 冻结后，先创建：

- `CompiledModelSetBuilder` 的固定四阶段公开形状；
- `FrozenInput implements ImmutablePipelineArtifact`；
- `CandidateContextPublicationPass implements PublicationCompilerPass`；
- 固定 artifact key：`t14.compiled-model-set-input`；
- 第十 Pass 只读取 frozen input 并调用 `prepare()` 的边界。

架构骨架保留稳定 `UnsupportedOperationException`，用于形成可编译行为 RED。骨架没有 Publisher、PublicationRequest、EngineContext CAS 或 Starter 接线。

## Capability boundary

- 前九个 Pass 仍不具备 Publication capability；
- 第十 Pass 只能准备 candidate；
- Pipeline 继续唯一持有外部 Publisher；
- 十个 Pass 的名称、数量和顺序不变；
- T15 与 P2～P7 runtime 未实现。
