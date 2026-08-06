# DEC_COMPILER DESIGN-R49 — TASK-P1-T14 Provenance-bound Candidate

- Design ID：`DESIGN-R49@P1-T14-REWORK-I002`
- Base：`dev_all@3e4da420d2ef5ada8398aefbbeabb37964e384ce`
- Rework Input：独立 Review `FND-P1-T14-I002-001`、`FND-P1-T14-I002-002`
- Status：`FROZEN`

## Goal

修复 T14 candidate 发布事实可被任意拆分组合的问题。Digest、Manifest、Definitions、Deferred 与版本域必须由同一次 T13 语义摘要计算原子绑定，最终 Publication Pass 必须再次绑定当前 `CompilationRequest` 中实际存在的 schema/options 事实，任何 provenance 或版本不一致均 fail-closed。

## Atomic provenance artifact

新增 `DigestBoundCompiledInput`，只能由 `CompilerDigestService.bind(SourceManifest, SemanticDigestInput)` 创建。该对象原子持有：

- `PublishedSourceManifest`；
- 不可变 Definition Registry；
- 不可变 Deferred Registry；
- compilerVersion；
- schemaVersion；
- optionsDigest；
- 由同一个 `SemanticDigestInput` 和原始 `SourceManifest` 计算出的 `DigestPair`。

构造路径不暴露分别注入模型事实、版本字符串或 `DigestPair` 的入口。`DigestPair` 在该正式边界必须满足固定 64 位小写十六进制 SHA-256 格式。

## Builder contract

`CompiledModelSetBuilder` 只接受 `DigestBoundCompiledInput`，执行一次性 `freeze()`：

- 不允许调用方重新提供 Manifest、Registry、Deferred、版本或 Digest；
- Builder freeze 后永久封闭；
- FrozenInput 只保存 provenance-bound immutable facts；
- candidate 构造继续由 `CompiledModelSet` 校验 ERROR Diagnostic、身份和不可变性。

I001 的分阶段 Builder 历史保留，但不再作为当前生产入口。

## Publication request binding

`CompilationOptions` 当前只公开 schemaVersion/optionsDigest，不公开 compilerVersion。因此 Publication Pass 在 `prepare()` 前必须验证 FrozenInput：

- `schemaVersion == request.options().schemaVersion()`；
- `optionsDigest == request.options().optionsDigest()`。

compilerVersion 由同一个 `SemanticDigestInput` 纳入 T13 canonical input 并封入 atomic provenance，不允许在 T14 再单独替换。任一 request mismatch 返回稳定 `MIX_PUBLICATION_PROVENANCE_MISMATCH / ERROR`，Pipeline 进入 FAILED，publisher=0，artifacts empty。缺少输入继续返回 `MIX_PUBLICATION_BLOCKED / ERROR`。

## Snapshot integrity

Definition 与 Deferred 快照必须分别验证：

- size 非负；
- keys 数量与声明 size 一致；
- duplicate key；
- key 有对应 value；
- 外部 key 与内部 identity 一致；
- 阶段结束 size 无漂移；
- 快照完成后不再读取原 Registry。

## Required Oracle

- request schema/options mismatch；
- compilerVersion 与其余语义事实由同一 atomic provenance 持有；
- 任意 DigestPair 注入入口不存在；
- 非法 Digest 格式拒绝；
- Definition/Deferred 全部快照负向边界；
- 非空 Manifest、Definition、Deferred、Warning、真实 Digest 的完整 candidate；
- publisher 收到完整模型与预期对象精确一致；
- missing input 与 mismatch 的精确 Diagnostic identity；
- T14、T13、T12、Compiler、全 Reactor 与 intentional failure gate 回归。

## Scope exclusions

- 不修改 `ContextPublisher`、`PublicationRequest` 或 EngineContext CAS；
- 不实现 T15、Starter 组装或 P2～P7 runtime；
- 不改变十个 Pass 名称、数量与顺序；
- 所有新增 `@Override` 独占一行，方法和重要逻辑使用中文注释。
