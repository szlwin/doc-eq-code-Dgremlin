# DEC_COMPILER API 契约

> Revision：API-R02-DRAFT。

## 1. 编译入口

```java
public interface ModelCompiler {
    CompilationResult compile(CompilationRequest request);
}

public final class CompilationRequest {
    SourceReference root;
    DocumentSourceProvider sourceProvider;
    FrontendRegistry frontends;
    CompilationOptions options;
}
```

## 2. 结果

```java
public final class CompilationResult {
    CompilationStatus status;
    List<Diagnostic> diagnostics;
    Optional<CompiledModelSet> modelSet;
    Optional<EngineContext> engineContext;
    String sourceDigest;
    String semanticDigest;
    CompilationMetrics metrics;
}
```

有 ERROR 时 modelSet 和 engineContext 必须为空。

## 3. SourceResolver

```java
public interface SourceGraphResolver {
    SourceGraphResolution resolve(SourceReference root,
                                  DocumentSourceProvider provider,
                                  SourceResolutionOptions options);
}
```

不得通过 API 传入项目绝对目录作为业务事实；测试 Provider 可以映射 fixture。

## 4. Registry

```java
public interface Registry<K extends DefinitionKey, V> {
    Optional<V> find(K key);
    V require(K key);
    List<K> keys();
}
```

Registry 不提供 put/remove/clear。

## 5. Deferred Registry

```java
public interface DeferredRegistry {
    List<DeferredDefinition> requiredBy(RequiredStage stage);
    List<DeferredDefinition> ownedBy(DefinitionKey key);
}
```

## 6. CoreConfigProjection

只提供读取接口，命名中不使用 Adapter。任何旧写 API 由单独拒绝实现承接并标记 deprecated。

## 7. 兼容性

- P1 不保证旧 declaration API；
- `dec-expand-declaration` 类型不出现在公共签名；
- 未来新增定义类型通过版本化 frontend/raw builder/pass 扩展，不能修改已发布 Key 语义。
