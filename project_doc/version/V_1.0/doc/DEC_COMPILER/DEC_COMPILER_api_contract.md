# DEC_COMPILER API 契约

> Revision：`DESIGN-R04@1c14c8e89779`。所有公共 API 使用显式输入输出、不可变值对象和稳定错误语义。

## 1. 编译入口 {#api-compiler}

```java
public interface ModelCompiler {
    CompilationResult compile(CompilationRequest request);
}

public record CompilationRequest(
    SourceReference root,
    DocumentSourceProvider sourceProvider,
    FrontendRegistry frontends,
    CompilationOptions options,
    Optional<Deadline> deadline,
    CancellationToken cancellationToken) {}
```

空 root/provider/frontend、未知 scheme、超过预算均返回 Diagnostic；不读取全局 Config。

## 2. CompilationResult {#api-result}

```java
public record CompilationResult(
    CompilationStatus status,
    List<Diagnostic> diagnostics,
    Optional<CompiledModelSet> modelSet,
    Optional<EngineContext> engineContext,
    DigestPair digests,
    CompilationMetrics metrics,
    String compilerVersion,
    String schemaVersion,
    String optionsDigest) {}
```

约束：SUCCESS 必须同时包含 modelSet/context；FAILED/CANCELLED/TIMED_OUT 时二者为空。Diagnostic list 已稳定排序且不可变。

## 3. Source 与 Frontend {#api-source-frontend}

```java
public interface DocumentSourceProvider {
    DocumentSource resolve(SourceReference reference, SourceResolutionContext context);
    List<DocumentSource> resolveFileSet(SourceReference reference, SourceResolutionContext context);
}
public interface DocumentFrontend {
    DocumentFormat format();
    FrontendResult parse(DocumentSource source, FrontendOptions options);
}
```

Provider 不返回 null；找不到、策略拒绝和 IO 错误转为 typed failure。FrontendResult 为 Canonical 或 Diagnostic，不暴露 DOM/YAML Node。

## 4. Registry 与 Key {#api-registry}

```java
public interface DefinitionKey { String canonical(); }
public interface Registry<K extends DefinitionKey,V> {
    Optional<V> find(K key);
    V require(K key);
    List<K> keys();
    int size();
}
public record InformationKey(SystemKey owner, String name) implements DefinitionKey {}
```

Registry 不提供 put/remove/clear；keys 按 canonical 顺序返回。

## 5. Information expression {#api-information}

```java
public interface InformationExpressionParser {
    ExpressionParseResult parse(String expression, SourceRef sourceRef);
}
public interface InformationReferenceResolver {
    ResolvedInformationExpression resolve(
        SystemKey owner, InformationExpressionAst ast, SymbolTable symbols);
}
```

Resolved 结果包含 AST、依赖 `InformationKey` 和 Diagnostic。P1 API 不提供 evaluate。

## 6. ModelAccess {#api-model-access}

```java
public interface ModelAccessSelectorResolver {
    ModelAccessResolution resolve(
        SystemKey owner,
        SharedModelPath sourcePath,
        ViewKey targetView,
        SystemViewSelector selector,
        SymbolTable symbols);
}
```

返回唯一 binding 或 Diagnostic；无 Optional fallback、无模糊候选列表供调用方自行猜测。

## 7. Deferred Registry {#api-deferred}

```java
public interface DeferredRegistry {
    List<DeferredDefinition> requiredBy(RequiredStage stage);
    List<DeferredDefinition> ownedBy(DefinitionKey key);
    Optional<DeferredDefinition> find(DeferredKey key);
}
```

结果不可变且稳定排序。RequiredStage 不接受 UNKNOWN。

## 8. EngineContext 发布 {#api-publication}

```java
public interface ContextPublisher {
    PublicationResult publish(
        EngineContext expectedCurrent,
        EngineContext candidate);
}
```

只有 SUCCESS CompilationResult 的完整 candidate 可发布。compare-and-set 失败返回 CONFLICT，旧 Context 不变；不隐式重试。

## 9. CoreConfigProjection {#api-projection}

只提供 `data()/views()/rules()` 读取。任何旧写方法明确 deprecated 并抛 `ProjectionWriteRejectedException(MIX-PROJECTION-001)`；名称不使用 Adapter，返回值不暴露 mutable collection。

## 10. Diagnostic 与兼容 {#api-diagnostic}

Diagnostic code 是稳定公共契约，message 可本地化；SourceRef/relatedRefs/recoveryHint 可选但 ERROR 必须有 code 和 SourceRef（非源级错误可使用 synthetic root ref）。公共签名不得出现 `dec-expand-declaration` 类型。新增定义类型通过版本化 frontend/raw builder/pass 扩展，不改变既有 Key canonical 语义。

## 11. 超时、取消、幂等 {#api-resilience}

- deadline/cancel 在 Pass 边界检查；
- cancel/timeout 返回非 SUCCESS，不发布；
- compile 不做隐式 IO retry；
- 相同 source/options/compiler version 的结果按 semanticDigest 幂等；
- Metrics 不参与 semanticDigest；
- 调用方可用新 request 显式重试。

## 12. API 测试契约 {#api-testing}

每个 API 必须覆盖 success、typed failure、null/非法输入、不可变性、稳定顺序、timeout/cancel 和并发 Session 隔离。二进制兼容不承诺旧 declaration API；源码迁移以编译错误显式暴露。
