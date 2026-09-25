# DESIGN-R45 — TASK-P1-T13 确定性 Digest、Deadline 与 Observer

- Revision：`DESIGN-R45@P1-T13-I001`
- Status：`PASSED`
- Base：`dev_all@659fb74563bbe1fa1daaf4d3a0e868f702daaec6`
- Dependency：`COMPLETION-P1-T12-R07@74f402287bc4`
- Scope：`TASK-P1-T13 / I001`
- Excludes：`TASK-P1-T14` 候选 Context 构造与 CAS 发布；`TASK-P1-T15` Starter 接入与旧模块退役

## 1. 目标与边界

T13 只交付以下能力：

1. `DEC-SEMANTIC-DIGEST-V1` 语义摘要；
2. Source 原始内容摘要；
3. `SemanticDigestInput` 不可变语义快照；
4. UTF-8 canonical JSON；
5. `MonotonicClock`、Deadline、Cancel 与 Timing 的同一单调纳秒域合同；
6. 只读 `CompilationObserver`，其失败转换为非阻断 `MIX-OBSERVER-FAILURE` Warning。

T13 不构造新的 `CompiledModelSet` 或 `EngineContext` 候选，不新增 Publisher/CAS 行为，不修改十 Pass 名称与顺序，不实现 P2～P7 runtime。

## 2. Digest 结果

`CompilerDigestService.compute(SourceManifest, SemanticDigestInput)` 返回 `DigestPair`：

- `sourceDigest`：按 `sourceId` 稳定排序后，对每个 Source 的 `sourceId`、原始内容长度和原始内容字节执行长度前缀编码，再计算 SHA-256；
- `semanticDigest`：对 `SemanticDigestInput.canonicalJson()` 的 UTF-8 字节计算 SHA-256；
- 两个摘要均使用 64 位小写十六进制文本；
- 摘要计算不读取文件系统枚举顺序、HashMap 插入顺序、线程调度、Clock、Timing 或 Observer。

Source 文本变化必须改变 `sourceDigest`。等价 XML/YAML 或仅物理行列变化可以保持 `semanticDigest`，但不要求保持 `sourceDigest`。

## 3. SemanticDigestInput

`SemanticDigestInput` 是 `dec.core.compiler.compiled` 中的 Java 8 不可变对象，输入闭包为：

- `PublishedSourceManifest` 的语义视图；
- `Registry<DefinitionKey, CompiledDefinition>`；
- `DeferredRegistry`；
- `compilerVersion`；
- `schemaVersion`；
- `optionsDigest`；
- 固定 `digestAlgorithmVersion=DEC-SEMANTIC-DIGEST-V1`。

明确排除：

- `DigestPair`；
- `CompilationTiming`、metrics、Observer 数据；
- `SourceRef.line` 与 `SourceRef.column`；
- Source 原始 URI、AllowedRoot、原始内容字节；
- Publication/CAS 状态。

`SourceRef` 在语义输入中只保留 `sourceId` 与 `nodePath`。Source descriptor 的语义视图只保留稳定 `sourceId`；格式与内容摘要不进入 semantic digest，避免等价 XML/YAML 被格式差异分裂。Dependency 保留 `edgeType/from/target` 与声明的 `sourceId/nodePath`。

Definition 按 `DefinitionKey.canonical()` 排序，编码 key、normalized body format/value 和语义 SourceRef。Deferred 按 `DeferredKey.canonical()` 排序，编码 key、requiredStage、reasonCode、body、语义 SourceRef 和按 canonical key 排序的 resolved references。

## 4. Canonical JSON

canonical JSON 必须满足：

1. UTF-8，无 BOM、无额外空白；
2. Object key 按 Unicode code point 升序；
3. Domain array 按各自 canonical key 排序；
4. String 使用标准 JSON escaping；控制字符使用小写 `\u00xx`；
5. Number 使用无指数、无多余前导零和尾随零的 canonical decimal；
6. Boolean/null 使用 JSON literal；
7. 不接受 NaN、Infinity、未知可变对象、循环结构或重复 canonical object key；
8. 相同输入重复编码必须逐字节相同。

## 5. Timing 与 Deadline

`CompilationRequest.clock()` 是 Deadline 和 Timing 的唯一时间来源。不得读取 `System.currentTimeMillis()`、`System.nanoTime()` 或其他隐式时钟。

十个 Pass 继续记录 `TimingPhase.PASS`。此外：

- `SourceGraphValidationPass` 的同一 elapsed 额外记录 `TimingPhase.DISCOVERY`；
- `StructuralValidationPass` 的同一 elapsed 额外记录 `TimingPhase.PARSE`；
- `DigestPass` 的同一 elapsed 额外记录 `TimingPhase.DIGEST`。

额外阶段不额外读取 Clock，避免观察行为改变 Deadline。完整成功路径因此有 13 个 Timing；Observer 与结果中的 Timing 顺序一致。T12 的 start timestamp Deadline 复核、Cancel、Clock overflow、publisher=0/1 和 commit-wins 合同保持。

## 6. Observer 失败

Observer 只能接收不可变 `CompilationTiming` 和 `SessionStateTransition`。回调抛出 `RuntimeException` 时：

- 不向调用方传播；
- 不改变当前 Session 状态；
- 不改变已准备/已发布 Context；
- 不改变 Digest；
- 追加 `DiagnosticCode.MIX_OBSERVER_FAILURE`、`DiagnosticSeverity.WARNING`；
- timing 回调使用 `pipeline.observer.timing.failure`；
- state 回调使用 `pipeline.observer.transition.failure`；
- subject 使用 Timing phase/pass 或 `FROM->TO`；
- 相同回调失败可逐次记录，不静默丢失。

为支持终态回调失败，`CompilationSession` 新增受控 observation diagnostic 写入口：只允许未 seal Session 写入 `MIX_OBSERVER_FAILURE` 且 severity 非 ERROR 的 Diagnostic。该入口不能写 artifact、不能转换状态、不能绕过 terminal immutability。

## 7. TDD 与 Review Oracle

有效 RED 至少覆盖：

- 新 Digest 类型/服务尚不存在；
- Map/Registry 输入乱序仍产生相同 canonical JSON 和 semantic digest；
- SourceRef 仅 line/column 变化不改变 semantic digest；
- 原始 Source 文本变化改变 sourceDigest；
- Observer timing/state 失败产生 Warning，但 status、context、digest/artefact 不变；
- 完整成功路径 Timing phase 计数为 DISCOVERY=1、PARSE=1、PASS=10、DIGEST=1；
- Deadline/Cancel 仍在发布前 fail-closed。

独立 Review 必须检查 canonical key collision、Unicode supplementary code point 排序、JSON escaping、版本域变化、空 Registry、不可变快照、Clock 读取次数和 T14/T15 未提前实现。

## 8. 停止条件

出现以下任一情况不得 Completion：

- semantic digest 包含 DigestPair、Timing、line/column 或 Source content digest；
- canonical JSON 依赖 Map 插入顺序或 UTF-16 错误排序；
- Observer Warning 改变 PUBLISHED/FAILED 状态或已提交 Context；
- 额外 Timing 造成额外 Clock 读取；
- Deadline/Cancel/Publication 合同回归；
- Open P0/P1/P2 未清零；
- 实现 T14/T15 范围；
- 全 Reactor、Java release 8、Artifact 独立校验未通过。
