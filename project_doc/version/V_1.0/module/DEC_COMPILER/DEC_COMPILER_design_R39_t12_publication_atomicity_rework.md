# DESIGN-R39 — TASK-P1-T12 Publication 原子终态与 Session 冻结返工

- Revision：`DESIGN-R39@P1-T12-REWORK-I002`
- Status：`PASSED`
- Supersedes：`DESIGN-R38@P1-T12-I001`
- Base：`PR27@49b9beee65dbc5e5db77302a7128a34a2ab77386`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- Invalidated Completion：`COMPLETION-P1-T12-R01@c6a515820972`
- Independent Review：`NEEDS_CHANGES / REWORK`，Open P0/P1/P2=`0/3/2`

## 1. 历史与范围

R38、R34、I001 RED、Architecture、Review、Completion、CI 与 Artifact 全部作为不可变历史保留。I002 只修复以下五个 Findings：

- `FND-P1-T12-I002-001`：Publication capability 暴露给普通 Pass；
- `FND-P1-T12-I002-002`：Context 逃逸和终态后 Session 可变；
- `FND-P1-T12-I002-003`：发布成功后仍可降级为 FAILED；
- `FND-P1-T12-I002-004`：start-clock 失败仍记录 Pass 已执行；
- `FND-P1-T12-I002-005`：缺少对应负向 Oracle。

不得实现 T13 Digest/Observer 完整策略、T14 候选 Context 构造/CAS 业务、T15 Starter 或 P2～P7 runtime。

## 2. Publication capability 隔离

### 2.1 普通 Pass

前九个 Pass 只能获得 `PassContext`。`PassContext` 不得暴露或间接持有：

- `PublicationRequest`
- `ContextPublisher`
- publisher 的 delegate、supplier 或可反射访问的 Session 字段

`CompilationSession` 不保存 `PublicationRequest`。Pipeline 将 PublicationRequest 保留为 execute 栈上的局部值。

### 2.2 最终发布 Pass

第十个 Pass 必须实现专用 `PublicationCompilerPass`，Pipeline 只向它提供 `PublicationPassContext`。构造门禁要求：

- 前九个元素不得实现 `PublicationCompilerPass`；
- 第十个元素必须实现 `PublicationCompilerPass`；
- 普通 `CompilerPass.execute(PassContext)` 不得获得发布能力。

`PublicationPassContext.publish(EngineContext)` 是唯一发布入口：

- 只允许调用一次；
- 只允许在 `SEMANTICALLY_VALIDATED` 且无 ERROR 时调用；
- candidate 非 null；
- `PublicationResult` 和 `status()` 非 null；
- `CONFLICT` 使用 `MIX-PUBLICATION-CONFLICT`；
- publisher 抛异常或返回非法结果使用 `MIX-PUBLICATION-FAILURE`。

完整成功路径 publisher 调用数必须精确为 1；任一发布前失败路径必须精确为 0。

## 3. PassContext 生命周期

每个 Pass 创建独立 Context，禁止十个 Pass 共用一个 Context。生命周期为：

```text
CREATE → ACTIVE → execute() 返回或抛异常 → CLOSED
```

Pipeline 必须在 `finally` 中关闭 Context。关闭后全部读写方法均拒绝调用。普通 Context 和 Publication Context 均不得跨 Pass、跨 Session 或跨 execute 重用。

## 4. Session 终态与结果冻结

`CompilationSession` 只保存当前 execute 的语义构建事实，不保存 Publication capability。所有语义 mutator 必须拒绝：

- `PUBLISHED`
- `FAILED`
- 已封闭 Session

至少包括 Diagnostic 聚合、artifact 写入、Pass 执行记录和状态转换。

`PipelineExecutionResult` 构造时复制并冻结 state、Diagnostic、artifact、timing、transition 和 executedPasses。结果对象不得保存 `CompilationSession` 字段或 getter。后续 execute、旧 Context、调用方集合或 mutable artifact 容器均不得改变已返回结果。

### 4.1 artifact 策略

artifact 写入时执行受控快照：

- String、Number、Boolean、Character、Enum 直接作为不可变值；
- List、Set、Map、Optional 递归复制并冻结；
- 显式实现 `ImmutablePipelineArtifact` 的领域值可复用；
- 数组及未知可变对象拒绝写入。

## 5. Publication commit 原子终态

第十 Pass 进入前必须完成已有 ERROR、cancellation、deadline、start-clock 和前九个 Pass 状态推进门禁。

当 publisher 返回 `PublicationStatus.PUBLISHED` 时：

1. 立即形成 `SEMANTICALLY_VALIDATED → PUBLISHED` 转换；
2. 关闭 Publication Context；
3. 该状态不可逆且不得降级；
4. 之后发生的 token 变化、end-clock 失败、Observer 异常或 Pass 返回后的异常均不得把结果改为 FAILED；
5. post-commit timing 无法完成时允许缺少 PublicationPass timing，但不得生成语义 ERROR。

若 PublicationPass 在 publish 成功前失败，则进入 FAILED；若 publish 已成功，提交事实优先于后续非原子观察故障。

## 6. Clock、token 与执行事实

普通 Pass 顺序冻结为：

```text
preflight cancel/deadline
→ start-clock 成功
→ recordPass
→ 创建 ACTIVE Context
→ pass.execute
→ finally close Context
→ end-clock
→ 聚合结果
→ postflight cancel/deadline
→ 状态推进
```

start-clock 失败时，`pass.execute()` 调用数、executedPasses 和 timings 均为 0。

异常分类：

- publisher 调用异常：`MIX-PUBLICATION-FAILURE / pipeline.publication.failure`；
- clock 读取异常：`MIX-OBSERVER-FAILURE / pipeline.clock.failure`；
- cancellation token 调用异常：`MIX-OBSERVER-FAILURE / pipeline.cancellation-token.failure`；
- Observer 回调异常：不改变语义结果，完整 Diagnostic 策略留给 T13；
- 普通 Pass 异常：`MIX-PUBLICATION-BLOCKED / pipeline.pass.failure`。

## 7. 阻断 Oracle

I002 至少新增：

- 普通 PassContext 不存在 PublicationRequest/ContextPublisher 能力；
- 早期 Pass 返回 ERROR 前尝试发布，publisher 调用数仍为 0；
- ERROR、cancel、timeout、clock/token 异常路径 publisher 调用数为 0；
- 完整成功 publisher 调用数为 1；
- retained Context 在 PUBLISHED/FAILED 后全部读写失败；
- 第二次 execute 不能修改第一次结果；
- PipelineExecutionResult 不持有 Session，getter 永久稳定；
- mutable List/Map artifact 在调用方后续修改后结果不变；
- publish 成功后 end-clock 失败仍为 PUBLISHED；
- publish 成功后 token 变化仍为 PUBLISHED；
- publish 成功后 PublicationPass 抛异常仍为 PUBLISHED；
- start-clock 失败时真实调用数、executedPasses、timing 均为 0；
- conflict、null PublicationResult、null status、double publish 均 fail-closed。

## 8. 编码与 Gate

- Java release 8；
- 所有 `@Override` 注解独占一行；
- 公开方法、构造器及重要状态、原子提交和失败逻辑使用中文注释；
- Open P0/P1 必须为 0；
- I002 Completion 前必须完成有效 RED、Architecture、GREEN、独立 Review、全量 P0、Artifact 独立解析和 Revision Integrity；
- PR #27 不自动合并；合并前 T13 保持阻断。
