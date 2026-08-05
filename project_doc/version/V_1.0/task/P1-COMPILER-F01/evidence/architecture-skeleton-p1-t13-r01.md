# TASK-P1-T13 / R01 Architecture Skeleton

- Architecture：`DEVSKEL-P1-T13-R01@4f3d444f779f`
- Status：`PASSED`
- Base：`dev_all@659fb74563bbe1fa1daaf4d3a0e868f702daaec6`
- Dependency：`COMPLETION-P1-T12-R07@74f402287bc4`
- Design：`DESIGN-R45@P1-T13-I001`
- Plan：`TP-P1-COMPILER-F01-R41@P1-T13-I001`
- TDD：`TDD-P1-T13-R01@4f3d444f779f`
- Valid RED Run：`31005889102`
- Valid RED Artifact：`8930284340`
- Valid RED SHA-256：`fe03a8fea61ff6ecbcd2a45f8ddba3f91ac37629cf8c9ff1a583777dc5fa5946`
- RED Result：`13 tests / 11 expected failures / 2 passing controls / 0 errors`

## 1. Package boundary

新增包 `dec.core.compiler.compiled`，只负责 T13 的确定性摘要：

```text
SemanticDigestInput
CanonicalJsonWriter
CompilerDigestService
```

该包可以依赖 `dec.core.compiler.source` 与 `dec.core.context.model` 的不可变发布事实，不得依赖 `CompilerPipeline`、`CompilationSession`、Publisher、Starter 或运行时 P2～P7 类型。

## 2. Public contract

`SemanticDigestInput` 与 `CompilerDigestService` 为 public final Java 8 类型：

```java
new SemanticDigestInput(
    PublishedSourceManifest,
    Registry<DefinitionKey, CompiledDefinition>,
    DeferredRegistry,
    compilerVersion,
    schemaVersion,
    optionsDigest)

String canonicalJson()
String digestAlgorithmVersion()

new CompilerDigestService()
DigestPair compute(SourceManifest, SemanticDigestInput)
```

`CanonicalJsonWriter` 为 package-private final 工具，不形成 Compiler 公共 API。

## 3. Semantic snapshot

构造 `SemanticDigestInput` 时立即形成 canonical JSON 字符串，之后不保留调用方传入的可变容器引用。语义字段包括：

- 固定 `DEC-SEMANTIC-DIGEST-V1`；
- compiler/schema/options 版本；
- Published Source 的稳定 `sourceId` 与 dependency 语义边；
- CompiledDefinition 的 canonical key、NormalizedBody 与语义 SourceRef；
- DeferredDefinition 的 canonical key、stage、reason、body、语义 SourceRef 与强类型引用。

SourceRef 只编码 `sourceId/nodePath`；不得编码 line/column。Source descriptor 不编码 format/contentDigest，保证等价 XML/YAML 与原始内容差异不会污染 semantic digest。

## 4. Canonical JSON engine

`CanonicalJsonWriter` 只接受：null、String、Boolean、有限 Number、Map<String, ?>、Iterable 与数组。未知类型、循环结构、重复 object key、NaN/Infinity 必须稳定拒绝。

关键不变量：

- Object key 使用 Unicode code point 顺序；
- domain array 在进入 writer 前按 canonical key 排序；
- string 使用 JSON escaping，控制字符使用小写 `\u00xx`；
- decimal 无指数、无冗余前导/尾随零；
- UTF-8、无 BOM、无额外空白。

## 5. Source digest

`CompilerDigestService` 对 SourceManifest sources 按 sourceId 排序，按长度前缀写入 sourceId 与原始 UTF-8 内容，再计算 SHA-256。长度前缀必须避免简单连接产生歧义。输出为 64 字符小写 hex。

semantic digest 只对 `canonicalJson()` 的 UTF-8 字节计算 SHA-256，不读取 SourceManifest 的原始内容、contentDigest、format、AllowedRoot 或物理 URI。

## 6. Pipeline integration

T12 已存在 MonotonicClock、Deadline、Cancel 与十个 PASS Timing。T13 不新增 Clock 读取：同一个 Pass 的 started/ended/elapsed 同时用于：

- `SourceGraphValidationPass`：PASS + DISCOVERY；
- `StructuralValidationPass`：PASS + PARSE；
- `DigestPass`：PASS + DIGEST；
- 其他 Pass：PASS。

完整成功路径共 13 个 Timing。supplemental timing 在对应 PASS 后立即登记，顺序稳定，Observer 接收顺序与结果一致。

## 7. Observer failure boundary

`CompilationSession` 增加 package-private observation diagnostic 写入口，只允许：

- `DiagnosticCode.MIX_OBSERVER_FAILURE`；
- severity 为 WARNING 或 INFO；
- Session 尚未 seal。

它不得写 artifact、不得改变状态、不得调用 publisher。

`CompilerPipeline` 的 timing/state 回调统一经 fail-open helper 执行：Observer 抛 RuntimeException 时只尝试登记稳定 Warning；Warning 登记自身失败也不得改变原编译状态或向调用方传播。

状态回调在 transition 已写入 Session 后执行；终态回调失败不能回滚 PUBLISHED/FAILED。结果 seal 必须发生在终态回调与对应 Warning 登记之后。

## 8. Diagnostic contract

```text
MIX-OBSERVER-FAILURE / WARNING
pipeline.observer.timing.failure
subject = <phase>:<passName>

MIX-OBSERVER-FAILURE / WARNING
pipeline.observer.transition.failure
subject = <FROM>-><TO>
```

Observer 异常文本不得直接作为不稳定 messageKey；异常类和 message 可作为受控 detail，但不能影响排序主键或摘要。

## 9. Compatibility and exclusions

- 十 Pass 名称、数量与顺序不变；
- PublicationPass 仍 prepare-only，Pipeline 仍唯一调用 publisher；
- Deadline、Cancel、Clock overflow、Diagnostic、commit-wins 与 Session isolation 保持；
- 不把 DigestPair 写回 SemanticDigestInput；
- 不构造 T14 的 CompiledModelSet/EngineContext 候选；
- 不修改 ContextPublisher/CAS；
- 不接入 T15 Starter，不退役旧模块；
- 不执行 P2～P7 runtime。

## 10. Style gate

- 所有 `@Override` 必须独占一行；
- public/package-private 方法、canonical 排序、JSON escaping、SHA-256、observer fail-open 和 supplemental timing 逻辑必须使用中文注释；
- 不允许未使用 private task、静态可变缓存、ThreadLocal 或默认 Charset。
