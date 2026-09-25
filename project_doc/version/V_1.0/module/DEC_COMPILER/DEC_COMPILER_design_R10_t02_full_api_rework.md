# DEC_COMPILER Design R10 — TASK-P1-T02 REWORK I003

## 1. 修订原因与事实源

完整规格 Review 证明 `DESIGN-R09@P1-T02-REWORK-I002` 只适配了最终 T01 发布模型，没有验证 `DESIGN-R05@0b37a9b4dd48` 与 `DEC_COMPILER_api_contract.md` 中已经冻结的完整 T02 公共 API。

本修订创建 `TASK-P1-T02/I003`。R09、I002 Completion、Review 与 Evidence 作为历史保留，但其“开放 P0/P1 为 0”和“公共 API 已完成”结论失效。当前 API 唯一事实按以下顺序解释：

1. 本 R10 对冲突点的明确收敛；
2. `DEC_COMPILER_api_contract.md` 的未被本 R10 修改部分；
3. `DESIGN-R05@0b37a9b4dd48`；
4. 最终 T01 发布模型合同。

## 2. 目标

1. 冻结 T03 及后续阶段无需修改的完整 Session 输入边界；
2. 所有 Source、Frontend、Deadline、Clock 和 Observer 均由调用方显式注入，不使用 static、thread-local、系统时钟或隐藏 ModelCompiler 状态；
3. 恢复 Optional 条件发布合同及 PublicationResult/PublicationStatus 分离；
4. 恢复 CompilationResult interface 与完整 Published 事实访问器；
5. 保留 I002 已正确实现的模型实例身份、diagnostics 单一事实源和失败候选隔离；
6. Java release 8 兼容，所有新增或修改方法及重要逻辑使用中文注释，`@Override` 独占一行。

## 3. CompilationRequest Session 边界

`CompilationRequest` 必须是不可变 final value object，并且构造器完整接收：

```java
CompilationRequest(
    SourceReference root,
    DocumentSourceProvider sourceProvider,
    FrontendRegistry frontends,
    CompilationOptions options,
    Optional<Deadline> deadline,
    CancellationToken cancellationToken,
    MonotonicClock clock,
    CompilationObserver observer)
```

对应 accessor 为：`root()`、`sourceProvider()`、`frontends()`、`options()`、`deadline()`、`cancellationToken()`、`clock()`、`observer()`。

所有参数本身均不接受 null；首次无 Deadline 使用 `Optional.empty()`。请求及其依赖不得从全局容器读取 Source、Frontend、Clock 或 Observer。

### 3.1 CompilationOptions

`CompilationOptions` 只保存参与语义身份的：

- `schemaVersion`；
- `optionsDigest`。

Deadline 不属于语义选项，必须从 options 中移除。最终 T01 的 `CompiledModelSet.optionsVersion()` 在 P1 兼容存储中承载同一个规范化 options digest；Published 结果对外统一命名为 `optionsDigest()`。

## 4. Source 与 Frontend 注入接缝

包布局：

```text
dec.core.compiler.source
  SourceReference
  DocumentSource
  SourceResolutionContext
  SourceResolutionResult
  SourceResolutionStatus
  DocumentSourceProvider

dec.core.compiler.canonical
  DocumentFormat
  FrontendOptions
  FrontendResult
  DocumentFrontend
  FrontendRegistry
```

`DocumentSourceProvider` 冻结：

```java
SourceResolutionResult resolve(
    SourceReference reference,
    SourceResolutionContext context);
SourceResolutionResult resolveFileSet(
    SourceReference reference,
    SourceResolutionContext context);
```

`FrontendRegistry` 冻结 `require(DocumentFormat)`；`DocumentFrontend` 冻结 `format()` 与 `parse(DocumentSource, FrontendOptions)`。T02 只冻结接缝和值语义，不实现 T03 SourceGraph 或真实 Frontend 行为。

## 5. Deadline、Clock 与 Observer

`Deadline` 保存绝对单调纳秒值，并通过 `isExpired(long nowNanos)` 判断过期。

`MonotonicClock` 只提供 `nanoTime()`。Deadline 检查和 Timing 必须使用同一个 Request 注入实例。

`CompilationObserver` 提供：

```java
void onTiming(CompilationTiming timing);
void onStateTransition(SessionStateTransition transition);
```

同时冻结 `TimingPhase`、`CompilationTiming`、`CompilationSessionState` 与 `SessionStateTransition` 的不可变公共形状。Observer 不进入 semantic digest，也不得成为隐藏全局状态。

## 6. 条件发布合同

`PublicationRequest` 必须保存非 null `Optional<EngineContext> expectedCurrent` 和非 null `ContextPublisher publisher`。

`ContextPublisher` 冻结：

```java
PublicationResult publish(
    Optional<EngineContext> expectedCurrent,
    EngineContext candidate);
```

`PublicationResult` 是 interface，只暴露 `PublicationStatus status()`；`PublicationStatus` 只包含 `PUBLISHED`、`CONFLICT`。首次发布只能使用 `Optional.empty()`，不得使用 null 表达业务状态。

Publisher 返回 null 或抛异常的转换行为属于后续 Pipeline，但 T02 必须先冻结该签名。

## 7. CompilationResult 合同

`CompilationResult` 必须是 interface，且公共合同仅包含：

```java
CompilationStatus status();
List<Diagnostic> diagnostics();
```

不公开 `sessionId()`、`isPublished()` 或 protected 扩展构造器。

`PublishedCompilationResult` 为 final immutable value object，通过静态工厂 `published(...)` 创建，并暴露：

- `modelSet()`；
- `engineContext()`；
- `digests()`；
- `compilerVersion()`；
- `schemaVersion()`；
- `optionsDigest()`；
- `digestAlgorithmVersion()`。

构造时必须验证：

1. `modelSet == engineContext.compiledModelSet()`；
2. diagnostics 与 `modelSet.diagnostics()` 等值，并复用模型中的同一个不可变实例；
3. digests、compilerVersion、schemaVersion 与模型发布事实一致；
4. optionsDigest 与 `modelSet.optionsVersion()` 一致；
5. digestAlgorithmVersion 非空白。

`FailedCompilationResult.failed(...)` 只保存不可变 diagnostics，要求至少一个 ERROR，不暴露模型、Context、Digest 或版本事实。

## 8. Test Oracle

TDD 必须直接冻结：

1. CompilationRequest 完整 8 参数构造器和全部 accessor；
2. Deadline 与 Clock 分离、options 中不存在 deadline；
3. SourceProvider、FrontendRegistry、Clock、Observer 为实例级注入且无 static mutable state；
4. Optional 条件发布签名；
5. PublicationResult interface 与 PublicationStatus enum；
6. CompilationResult interface 的最终形状；
7. Published 完整 accessor 与所有事实一致性；
8. Failed Result 候选隔离；
9. T03 可通过注入接缝接入，而无需修改 T02 构造器或公共方法。

## 9. 门禁

- 任一开放 P0/P1 阻断 Completion；
- 既有 I002 Completion 标记为被完整规格 Review 推翻，不覆盖或删除；
- 不实现 T03 SourceGraph、真实 Frontend、Pipeline 或发布重试；
- 完整 Reactor、Java 8、故意失败阻断门禁与 Context 回归必须通过；
- PR #17 在本轮 Completion 通过前保持 Draft；
- PR #17 合并前 `TASK-P1-T03` 保持阻断。
