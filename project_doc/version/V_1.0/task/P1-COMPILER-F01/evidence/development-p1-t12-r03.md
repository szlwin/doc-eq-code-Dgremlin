# TASK-P1-T12 I003 Development Evidence

- Development：`DEV-P1-T12-R03@31703c214245`
- Evidence：`EVD-000856`～`EVD-000861`
- Clean production Head：`31703c2142453e0fef4746c852bddd539bf8328f`
- Independent Review Head：`4d4cd5c4c0490e32ae9dc360426696bc0f994c4b`

## Delivered changes

- `PublicationPassContext` 改为 candidate prepare-only，不持有 `PublicationRequest`、`ContextPublisher` 或 delegate；
- `CompilerPipeline` 在 final Pass 返回并完整聚合 Context/PassResult Diagnostic 后才唯一调用 publisher；
- ERROR、取消、超时、Clock/timing 故障、Pass 异常和 candidate 缺失均在 commit 前阻断；
- Warning/Info 在成功 `PipelineExecutionResult` 中保留；
- `Math.subtractExact` 与统一异常边界保证任意 long 差值不越过 Pipeline；
- start timestamp 在 `recordPass` 和 `execute` 前即时复核 Deadline；
- Map key 与 Set element 冻结后 equality collision 稳定 fail-closed；
- Publication Context 关闭前生成 Pipeline 局部 candidate 快照，关闭后全部公开和包内读取均拒绝；
- I002 三个旧“Pass 内提交”Oracle 已迁移为 prepare 后基础设施或 Pass 故障阻断 commit，不降低测试强度。

生产修改仅限 `dec.core.compiler.pass`；未实现 T13/T14/T15 或 P2～P7 runtime。
