# DEC_COMPILER 详细设计 R07：T01 发布边界第二轮返工

> Revision：`DESIGN-R07@P1-T01-REWORK-I009`  
> 前置：`BM-R05@4ecb1f8c09f4`、`DESIGN-R05@0b37a9b4dd48`、`DESIGN-R06@P1-T01-REWORK-I008`  
> 适用任务：`TASK-P1-T01` REWORK iteration I009

## 1. 返工目标

本 Revision 只关闭 PR #16 完整规格复审新增的两个 P1 Finding：

1. `CoreConfigProjection` 缺少稳定的 `MIX-PROJECTION-WRITE` 写入拒绝语义；
2. `PublishedSourceDependency` 未保证 `declarationSourceRef.sourceId()` 与 `fromSourceId` 一致。

上一轮 I008 已关闭的五个 Finding 必须保持回归通过。本轮不实现 T02/T03 行为，不引入第二 Registry、第二运行时或可成功修改 Projection 的路径。

## 2. Projection 兼容写入拒绝合同

`CoreConfigProjection` 仍然只从一个 `CompiledModelSet` 派生并保存只读列表。为满足 BM-R05 `INV-COMPILER-014` 与 DESIGN-R05 的稳定失败语义，增加以下 deprecated 兼容写入口：

```java
@Deprecated
public void register(CompiledDefinition definition)

@Deprecated
public void replace(CompiledDefinition definition)

@Deprecated
public void remove(DefinitionKey key)

@Deprecated
public void clear()
```

以上入口均不得验证或修改输入事实，也不得产生部分写入；调用后必须立即抛出：

```java
ProjectionWriteRejectedException extends UnsupportedOperationException
```

专用异常位于 `dec.core.context`，为不可变值异常，至少暴露：

```java
public String operation()
public DiagnosticCode diagnosticCode()
public Diagnostic diagnostic()
```

其中 Diagnostic 固定满足：

- code：`DiagnosticCode.MIX_PROJECTION_WRITE`；
- severity：`DiagnosticSeverity.ERROR`；
- messageKey：`projection.write.rejected`；
- sourceRef：`synthetic:core-config-projection:0:0#/compatibility-write/<operation>`；
- pass：`CoreConfigProjection`；
- recoveryHint：调用方重新编译并发布新的 `CompiledModelSet`。

所有兼容写入口调用同一个私有拒绝逻辑，保证错误码、异常类型和消息语义一致。失败前后 `sourceModelSet`、Data、View、Rule 列表必须保持相同对象和值。

`data()`、`views()`、`rules()` 返回的 List 也属于公共写入尝试入口。不能只依赖 `Collections.unmodifiableList`，因为空列表上的 `remove`、`removeAll`、`clear` 等调用可能无操作返回，且其它变更只产生普通集合异常。三个 List 必须使用内部只读实现统一拦截 `add/set/remove/clear/addAll/removeAll/retainAll/removeIf/replaceAll/sort` 等 Java 8 变更入口，并抛出同一个 `ProjectionWriteRejectedException`。operation 使用 `<projection>.<listOperation>`，例如 `data.add`、`views.clear`。

## 3. Source dependency 声明来源一致性

`PublishedSourceDependency` 构造器必须保证：

```java
dependency.fromSourceId()
        .equals(dependency.declarationSourceRef().sourceId())
```

不一致时立即抛出 `IllegalArgumentException`，不得创建内部矛盾的发布边。

该规则同时适用于 synthetic root edge：synthetic root 的 `fromSourceId` 与 `declarationSourceRef.sourceId()` 必须使用同一个 synthetic source identity。`PublishedSourceManifest` 在依赖闭包校验中再次执行防御性验证，防止未来反序列化或替代实现绕过值对象构造器。

## 4. 测试 Oracle

新增 R03 合同测试，必须覆盖：

- 四个 deprecated 兼容写入口均存在；
- 所有兼容入口抛出 `ProjectionWriteRejectedException`；
- Data/View/Rule List 的直接变更入口也抛出同一专用异常；
- 异常携带 `MIX-PROJECTION-WRITE` Diagnostic；
- 写入失败前后模型、Projection 列表和来源引用不变；
- 普通 Source edge 拒绝声明 SourceRef 与 fromSourceId 错配；
- synthetic root edge 的一致场景通过；
- synthetic root edge 的错配场景被拒绝；
- Manifest 防御性校验保持依赖来源一致性；
- I008 的 17 项 Context 测试和 8 项 REWORK 语义测试继续通过。

## 5. 兼容与范围

- Projection 的写入口只用于提供稳定拒绝语义，不恢复可变 Core 配置；
- 不允许捕获专用异常后通过 List 或其它路径修改 Projection；
- 不改变 `CompiledModelSet`、Typed Registry 或 EngineContext 的单一事实源；
- 不启动 `TASK-P1-T02` 重验证或 `TASK-P1-T03`；
- 原 R02 Completion 与 Evidence 保留为被本次完整规格 Review 推翻的历史记录。
