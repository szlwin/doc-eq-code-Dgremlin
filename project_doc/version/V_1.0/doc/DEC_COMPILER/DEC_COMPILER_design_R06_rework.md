# DEC_COMPILER 详细设计 R06：T01 公共合同返工

> Revision：`DESIGN-R06@P1-T01-REWORK-I008`  
> 前置：`DESIGN-R05@0b37a9b4dd48`、追溯 Review `T01-RETRO-REVIEW-20260801`  
> 适用任务：`TASK-P1-T01` REWORK iteration I008

## 1. 返工目标

本 Revision 只修复 `dec-core-context` 中 Compiler 与运行上下文共享的中立公共合同，不实现 SourceGraph 发现算法、Frontend、Compiler Pass 或 P2～P7 运行语义。

需要关闭以下 P1 Finding：

1. `DirectoryKey` 缺少 `BusinessScopeKey` owner；
2. `CompiledModelSet` 缺少中立 SourceManifest 发布视图和已发布 Typed Registry 结构；
3. ERROR Diagnostic 可以进入发布模型，且 Projection 可与 ModelSet 自由组合；
4. Registry Key 与 Definition 身份可以不一致；
5. T01 测试只验证通用形状，未冻结业务身份和发布聚合不变量。

## 2. DirectoryKey 身份

`DirectoryKey` 的完整身份固定为：

```java
DirectoryKey(BusinessScopeKey owner, String name)
```

canonical 形式固定为：

```text
directory:<businessScopeCanonical>:<directoryName>
```

因此不同 BusinessScope 下同名 Directory 不冲突；同一 BusinessScope 下同名 Directory 具有相同身份。`ActionKey` 和 `ProduceKey` 继续从该完整身份派生。

## 3. 中立 SourceManifest 发布视图

新增位于 `dec.core.context.model` 的中立不可变类型：

```text
PublishedSourceManifest
PublishedSourceDescriptor
PublishedSourceDependency
```

职责如下：

- `PublishedSourceManifest` 保存 rootSourceId、稳定排序的 source 列表和依赖边列表；
- `PublishedSourceDescriptor` 保存 sourceId、format、contentDigest；
- `PublishedSourceDependency` 保存 edgeType、fromSourceId、targetSourceId、declarationSourceRef；
- 以上类型不依赖 compiler 包或具体 XML/YAML 解析库；
- T03 负责发现并构建这些事实，T01 只冻结发布合同。

## 4. 已发布 Typed Registry

新增 `TypedDefinitionRegistries`，由完整 definitions Registry 确定性派生并保存以下只读 Registry：

- DataSourceKey
- ConnectionKey
- DataKey
- ViewKey
- SystemKey
- RuleViewKey
- BusinessScopeKey
- InformationKey
- DirectoryKey
- ActionKey
- ProduceKey

未知 DefinitionKey 类型必须拒绝，不允许静默落入无类型 Registry。为兼容统一遍历，`CompiledModelSet` 仍可暴露完整 definitions Registry，但 Typed Registry 是正式发布事实闭包的一部分。

## 5. CompiledModelSet 聚合不变量

`CompiledModelSet` 构造输入固定包含：

```text
PublishedSourceManifest
Registry<DefinitionKey, CompiledDefinition>
DeferredRegistry
List<Diagnostic>
DigestPair
compilerVersion
schemaVersion
optionsVersion
```

构造时必须验证：

1. diagnostics 中不存在 `DiagnosticSeverity.ERROR`；
2. definitions 的 Map/Registry key 必须等于 `CompiledDefinition.key()`；
3. deferred key 必须等于 `DeferredDefinition.key()`；
4. 所有输入集合均防御性复制并稳定排序；
5. Typed Registry 只能由已验证 definitions 确定性派生；
6. 发布后整个可达对象闭包不可变。

## 6. DeferredDefinition 身份

`DeferredDefinition` 改为直接持有 `DeferredKey`：

```java
DeferredDefinition(
    DeferredKey key,
    RequiredStage requiredStage,
    String reasonCode,
    SourceRef sourceRef,
    NormalizedBody body,
    List<DefinitionKey> resolvedReferences)
```

`ownerKey()`、`kind()`、`ordinal()` 均从 `DeferredKey` 读取，避免 Map key 与 definition 内部 owner/kind/ordinal 分裂。

## 7. Projection 同源性

`CoreConfigProjection` 不再暴露公共任意 List 构造器。

唯一创建方式为：

```java
CoreConfigProjection.from(CompiledModelSet modelSet)
```

Projection 从同一个 `CompiledModelSet` 的 Typed Registry 确定性派生 Data、View、RuleView 只读列表。`EngineContext` 只公开：

```java
EngineContext(CompiledModelSet compiledModelSet)
```

并在内部生成 Projection，不允许调用方自由组合 ModelSet 与 Projection。

## 8. 测试 Oracle

新增 REWORK 合同测试，必须覆盖：

- 不同 BusinessScope 下同名 Directory 不冲突；
- 同一 BusinessScope 下相同 Directory 身份一致；
- CompiledModelSet 公开 SourceManifest 和 Typed Registry；
- ERROR Diagnostic 被拒绝；
- EngineContext 不存在 ModelSet + Projection 公共组合构造器；
- Projection 由同一 ModelSet 派生；
- definitions key/definition.key 错配被拒绝；
- deferred key/definition.key 错配被拒绝；
- 所有新类型与集合保持 Java 8 不可变合同。

## 9. 兼容与范围

这是上游公共合同修复，允许对尚未进入 `dev_all` 的 T02 分支造成编译影响。PR #15 在本返工合并前不得合并；T01 合并后 T02 应基于最新 `dev_all` rebase 并适配新构造器。

本 Revision 不启动 `TASK-P1-T03`，也不宣称 SourceGraph 已实现。
