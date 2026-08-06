# DEC_COMPILER DESIGN-R48 — TASK-P1-T14 Candidate EngineContext

- Design ID：`DESIGN-R48@P1-T14-I001`
- Base：`dev_all@3e4da420d2ef5ada8398aefbbeabb37964e384ce`
- Dependency：`COMPLETION-P1-T13-R03@5075793d06cc`
- Status：`FROZEN`

## Goal

实现 Compiler 内部的 `CompiledModelSetBuilder`，将前九阶段已经完成的不可变语义事实按固定顺序收敛为完整 candidate `EngineContext`，并由第十个 `PublicationPass` 只准备 candidate。Pipeline 继续唯一持有 `PublicationRequest` 和 `ContextPublisher`，所有最终门禁通过后才允许外部提交。

## Fixed input order

Builder 只接受以下顺序：

1. `PublishedSourceManifest`；
2. `Registry<DefinitionKey, CompiledDefinition>`；
3. `DeferredRegistry`；
4. `DigestPair`；
5. 使用无 ERROR 的 Diagnostic 快照构造 `CompiledModelSet` 和 `EngineContext`。

越序、重复、缺失或 build 后继续写入均稳定拒绝。

## Frozen input closure

- Registry 在阶段入口立即复制为 `ImmutableRegistry`；
- Deferred 在阶段入口立即复制为 `ImmutableDeferredRegistry`；
- Manifest、DigestPair 和版本文本必须非空；
- 构建阶段不得重新读取调用方 Registry/Deferred 的可变视图；
- 冻结输入作为 `ImmutablePipelineArtifact` 进入 Session；
- artifact key 由 T14 Publication Pass 唯一声明；
- candidate 构建完成前不得调用 publisher。

## Publication boundary

新增 `CandidateContextPublicationPass`：

- 名称固定为 `CompilerPipeline.PUBLICATION_PASS`；
- 从 `PublicationPassContext` 读取 T14 frozen input；
- 读取 Session 当前稳定 Diagnostic 快照；
- 构造 `CompiledModelSet` 和 `EngineContext`；
- 只调用 `context.prepare(candidate)`；
- 不持有 `PublicationRequest`、`ContextPublisher` 或 EngineContext CAS；
- 缺少输入时返回 `MIX_PUBLICATION_BLOCKED / ERROR`；
- 构造异常由 Pipeline 的既有 publication failure 边界 fail-closed。

## Version contract

- `compilerVersion` 由 Builder 显式冻结；
- `schemaVersion` 与 `optionsVersion` 使用当前编译请求的稳定版本事实；
- `DigestPair` 必须与同一冻结输入闭包对应；
- T14 不改变 T13 digest 算法、Timing、Deadline、Observer 或 commit-wins 合同。

## Immutability and identity

- `CompiledModelSet` 继续执行 Definition/Deferred key-value identity 校验；
- 任一 ERROR Diagnostic 阻止 candidate 构造；
- candidate 创建后不保留 Builder、Registry 或 Deferred 可变引用；
- Publisher 接收的对象即最终 candidate，不进行发布后的二次补丁；
- FAILED 结果不暴露 artifact；PUBLISHED 继续 commit-wins。

## Scope exclusions

- 不实现 T15 旧模块退役；
- 不修改 Starter 组装；
- 不修改 `ContextPublisher`、`PublicationRequest` 或 EngineContext CAS；
- 不实现 P2～P7 runtime；
- 不改变十个 Pass 的名称、数量和顺序。

## Required tests

- Builder 固定阶段顺序、重复调用、缺失输入和 one-shot；
- Registry/Deferred 在阶段入口后不再被读取；
- 正常 candidate 包含完整 manifest/registry/deferred/digest/version/diagnostic；
- ERROR Diagnostic 拒绝构建；
- final pass 缺少 frozen input 时 FAILED 且 publisher=0；
- final pass 正常时 publisher=1、candidate 精确一致、状态 PUBLISHED；
- Deadline/Cancel/Observer/T12/T13 全量回归；
- `@Override` 独占一行，方法和重要逻辑使用中文注释。
