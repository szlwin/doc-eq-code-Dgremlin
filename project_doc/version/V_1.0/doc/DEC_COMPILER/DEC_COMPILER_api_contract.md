# DEC_COMPILER P1 API 契约

- Revision：`DESIGN-R01@a7a6820a381e`
- 语言基线：Java 8（不使用 record/sealed types）

## 1. 公共入口

```java
public interface BusinessCompiler {
    CompilationResult compile(List<DocumentSource> sources,
                              CompilationOptions options,
                              FrontendRegistry frontends);
}
```

约束：每次调用创建独立 Session；不得读取隐式默认目录或全局 Config；返回值非 null。

## 2. Frontend SPI

```java
public interface DocumentFrontend {
    boolean supports(DocumentFormat format, String schemaVersion);
    FrontendResult parse(DocumentSource source, FrontendOptions options);
}

public final class FrontendResult {
    List<CanonicalDocumentNode> roots();
    List<Diagnostic> diagnostics();
}
```

Frontend 只产出 Canonical/format diagnostics，不解析跨文件引用、不写 Registry。

## 3. Canonical 与 Source

```java
public final class DocumentSource {
    String sourceId();
    DocumentFormat format();
    String schemaVersion();
    ByteSource content();
    Digest sourceDigest();
}

public final class CanonicalDocumentNode {
    String nodeType();
    List<CanonicalAttribute> attributes();
    Optional<String> scalar();
    List<CanonicalDocumentNode> children();
    SourceRef sourceRef();
}
```

所有集合返回不可变视图；构造时防御性复制；`sourceId` 必须唯一且非空。

## 4. Diagnostic

```java
public final class Diagnostic implements Comparable<Diagnostic> {
    Severity severity();
    DiagnosticCode code();
    String safeMessage();
    Optional<SourceRef> sourceRef();
    Optional<TypedKey> entityKey();
    CompilerPassId passId();
}
```

`compareTo` 与 documented stable ordering 完全一致；message 不参与唯一业务 Key，敏感值不得进入 safeMessage。

## 5. Key

```java
public interface TypedKey {
    KeyNamespace namespace();
    String canonicalValue();
}
```

每个 Key 为 final 值对象。`RuleViewKey` 构造包含 `system` 与 `name`，P1 仅预留结构；P2 才启用完整 System 校验。

## 6. CompilationResult

```java
public final class CompilationResult {
    List<Diagnostic> diagnostics();
    Optional<EngineContext> engineContext();
    Digest sourceDigest();
    Optional<Digest> semanticDigest();
    CompilationMetadata metadata();

    boolean hasErrors();
}
```

硬约束：`hasErrors()==true` 时 `engineContext` 和 `semanticDigest` 均为空；无 ERROR 且发布成功时二者存在。

## 7. EngineContext 与 Registry

```java
public final class EngineContext {
    ContextId id();
    CompiledBusiness business();
    CompiledRegistries registries();
    Digest sourceDigest();
    Digest semanticDigest();
    String schemaVersion();
    String compilerVersion();
    LegacyConfigView legacyView();
}
```

Registry 只暴露 `find/get/iterate`，不暴露 mutable Map 或注册 API。对象构造完成后线程安全只读。

## 8. Legacy 契约

```java
@Deprecated
public interface LegacyConfigView {
    Optional<Object> find(LegacyConfigType type, String name);
    List<Object> list(LegacyConfigType type);
}
```

旧签名若包含写方法，adapter 实现必须抛 `LegacyWriteUnsupportedException`；禁止返回可修改内部集合/对象。

## 9. Pass 契约

```java
interface CompilerPass<I, O> {
    CompilerPassId id();
    PassResult<O> execute(I input, CompilationSessionContext context);
}
```

Pass 不抛出用于普通配置错误的异常；配置问题进入 Diagnostic。基础设施异常包装为稳定 Diagnostic/cause，禁止吞掉。

## 10. 兼容与版本

- 公共 Diagnostic code、Key canonical form、digest encoding 和 frontend contract 版本化。
- P1 新 API 可与旧读取 API 并存；不保证旧写入继续可用。
- API 不依赖 DOM4J、SnakeYAML、JDBC、MySQL 或 demo 类型。
