# DESIGN-R40 — TASK-P1-T12 最终诊断门禁、Timing 与 Artifact 保真返工

- Revision：`DESIGN-R40@P1-T12-REWORK-I003`
- Status：`PASSED`
- Supersedes：`DESIGN-R39@P1-T12-REWORK-I002`
- Base：`PR27@749d010e47fe23f283d119a48a7904ebcf0f64d2`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- Invalidated Completion：`COMPLETION-P1-T12-R02@5d5a7d72119b`
- Independent Review：`NEEDS_CHANGES / REWORK`，Open P0/P1/P2=`0/2/3`

## 1. 历史与范围

R38/R39、R34/R35、I001/I002 的 RED、Architecture、Development、Review、Completion、CI 与 Artifact 必须作为不可变历史保留。I003 只修复以下五个 Findings：

- `FND-P1-T12-I003-001`：最终 Pass 可在返回 ERROR 前直接提交；
- `FND-P1-T12-I003-002`：单调 Clock 差值溢出可让异常越过 Pipeline；
- `FND-P1-T12-I003-003`：start-clock 已到 Deadline 仍执行普通 Pass；
- `FND-P1-T12-I003-004`：artifact 冻结可静默合并 Map/Set 事实；
- `FND-P1-T12-I003-005`：缺少上述阻断 Oracle。

不得实现 T13 Digest/Observer 完整策略、T14 Context CAS 业务、T15 Starter 或 P2～P7 runtime。

## 2. Publication prepare / commit 两阶段边界

### 2.1 Publication Pass 只负责准备

第十阶段仍实现 `PublicationCompilerPass` 并返回完整 `PassResult`，但 `PublicationPassContext` 不得持有或间接引用：

- `PublicationRequest`
- `ContextPublisher`
- publisher delegate、supplier 或回调

`PublicationPassContext` 只允许读取 Session 事实、登记 Diagnostic，并通过：

```text
prepare(candidate)
```

登记一个候选 `EngineContext`。`prepare` 只保存 Session-local candidate，不执行任何外部提交；同一 Context 只能登记一次，candidate 不得为 null。

### 2.2 Pipeline 唯一持有 commit capability

最终阶段固定顺序：

```text
preflight cancel/deadline
→ start-clock
→ 使用 start timestamp 复核 Deadline
→ recordPass
→ PublicationPass.execute(read/write preparation context)
→ finally close context
→ end-clock 与 timing 安全登记
→ 聚合 Context Diagnostic
→ 聚合 PassResult Diagnostic
→ 检查 ERROR
→ 检查 candidate 已准备
→ 最后一次 cancel/deadline/ERROR 门禁
→ Pipeline 唯一调用 ContextPublisher.publish
→ 读取一次 PublicationStatus
→ PUBLISHED 或 FAILED/CONFLICT
```

任何最终 Pass `PassResult` ERROR、Context ERROR、取消、超时、Clock/timing 故障、null result、未准备 candidate 或 Pass 异常均要求 publisher 调用数精确为 0。

WARNING/INFO Diagnostic 必须保留在最终 `PipelineExecutionResult` 中，不得因成功发布被丢弃。

### 2.3 Commit-wins

publisher 返回 `PublicationStatus.PUBLISHED` 后立即执行：

```text
SEMANTICALLY_VALIDATED → PUBLISHED
```

该终态不可逆。提交完成后的 Observer 回调异常或其他只读观察故障不得降级为 FAILED。publisher 抛异常、返回 null、status 抛异常、null status 或 CONFLICT 均发生在确认提交前，必须 fail-closed。

## 3. Clock 与 timing 完整异常边界

所有 Pass 的 start/end Clock 读取、elapsed 计算、`CompilationTiming` 构造和 Session timing 登记都必须位于受控结果边界内。

elapsed 规则：

- `ended < started`：使用 `0`，保持现有非递减容错；
- `ended >= started`：使用溢出检查减法；
- 差值超出 `long` 非负范围：形成 `MIX-OBSERVER-FAILURE / pipeline.clock.failure`，进入 FAILED；
- 任何 timing 构造或登记 RuntimeException：同样形成稳定 Clock failure；
- RuntimeException、ArithmeticException 或 IllegalArgumentException 不得越过 `CompilerPipeline.execute()`。

发布成功后的 Observer 回调仍遵循 commit-wins。

## 4. start timestamp Deadline 门禁

当请求存在 Deadline 时：

1. preflight 可先读取 Clock 做早期拒绝；
2. start-clock 成功后，必须用同一个 `startedNanos` 再次调用 `Deadline.isExpired(startedNanos)`；
3. 若已经到期，必须在 `recordPass()` 和 `pass.execute()` 前进入 FAILED；
4. 此时真实 Pass 调用数、executedPasses、timings 和 publisher 调用数均为 0。

普通 Pass 与 Publication Pass 均适用该即时门禁。

## 5. Artifact 快照事实保真

List、Set、Map、Optional 继续递归复制并冻结，identity active-path 继续阻断循环图。

对于 Set：

- 每个元素冻结后写入目标 Set；
- 如果冻结后的 equality 造成重复，必须抛出稳定 `IllegalArgumentException`；
- 不得静默去重。

对于 Map：

- key/value 均先递归冻结；
- 写入前检查目标 Map 是否已存在 equality 相同的冻结 key；
- 发生碰撞时必须抛出稳定 `IllegalArgumentException`；
- 不得覆盖前一个 value。

碰撞异常由当前 Pass 的统一异常边界收敛为 `MIX-PUBLICATION-BLOCKED / pipeline.pass.failure`，publisher 调用数为 0。

## 6. 阻断 Oracle

I003 至少新增：

- final Pass 先准备 candidate 再返回 ERROR，publisher=0；
- final Pass 返回 WARNING，publisher=1 且 WARNING 保留；
- Clock `Long.MIN_VALUE → Long.MAX_VALUE` 不抛出边界，返回 FAILED/clock failure；
- Deadline=10，preflight=9、start=10 时真实 Pass 调用数与 executedPasses 均为 0；
- IdentityHashMap 两个冻结后相等 key 必须 fail-closed；
- identity-backed Set 两个冻结后相等元素必须 fail-closed；
- 原 I001/I002 capability、Context、Result、conflict/null/double publish、循环 artifact 和 commit-wins Oracle 继续通过。

## 7. 编码与 Gate

- Java release 8；
- 所有 `@Override` 注解独占一行；
- 公开方法、构造器和重要 prepare/commit、Clock、Deadline、collision 逻辑使用中文注释；
- Open P0/P1/P2 必须为 `0/0/0`；
- Completion 前必须完成有效 RED、Architecture、GREEN、独立 Review、全量 P0、Artifact 独立解析和 Revision Integrity；
- PR #27 不自动合并；合并前 TASK-P1-T13 保持阻断。
