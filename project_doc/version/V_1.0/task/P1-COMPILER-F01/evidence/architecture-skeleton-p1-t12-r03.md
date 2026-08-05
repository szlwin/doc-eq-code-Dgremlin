# TASK-P1-T12 I003 Architecture Skeleton Evidence

- Architecture：`DEVSKEL-P1-T12-R03@31236a0d4779`
- Design：`DESIGN-R40@P1-T12-REWORK-I003`
- Plan：`TP-P1-COMPILER-F01-R36@P1-T12-REWORK-I003`
- Evidence：`EVD-000845`～`EVD-000849`

## Frozen skeleton

- `PublicationPassContext` 仅保存 Session-local candidate，不保存 `PublicationRequest` 或 `ContextPublisher`；
- `PublicationCompilerPass.execute()` 保持返回完整 `PassResult`，Pipeline 在聚合全部 Diagnostic 后才调用 publisher；
- `CompilerPipeline` 增加 start timestamp Deadline 即时复核和受控 timing 计算/登记边界；
- `ArtifactSnapshots` 增加 Map key 与 Set element 冻结后 equality collision 检测；
- I001/I002 的 capability、Context 生命周期、Result 快照、conflict/null、commit-wins 和循环图合同继续作为回归约束；
- 尚未修改生产行为，必须先形成 I003 有效 RED。

Architecture checkpoint：`PASSED_FOR_RED`。
