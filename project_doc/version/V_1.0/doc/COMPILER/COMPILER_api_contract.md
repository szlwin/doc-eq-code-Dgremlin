# DEC_COMPILER API 契约

> 候选 Revision：`DESIGN-R05@0b37a9b4dd48`。`DESIGN-R04@1c14c8e89779` 已被 `REV-000038` 退回；当前为 DESIGN I007 返修候选，不复用旧 Review/Evidence。所有公共 API 使用显式输入输出、Java 8 兼容的不可变值对象和稳定错误语义。

Java 8 值对象统一使用 `final class + final field + constructor + read accessor`，构造时执行 `Objects.requireNonNull`，集合执行防御性复制并返回不可变视图，不提供 setter。所有值对象必须基于全部语义字段实现 `equals/hashCode/toString`；下列代码省略这三个标准方法。

## 1. 编译入口 {#api-compiler}

```java
public interface ModelCompiler {
    CompilationResult compileAndPublish(
        CompilationRequest request,
        PublicationRequest publicationRequest);
}

public final class PublicationRequest {
    private final Optional<EngineContext> expectedCurrent;
    private final ContextPublisher publisher;

    public PublicationRequest(
            Optional<EngineContext> expectedCurrent,
            ContextPublisher publisher) {
        this.expectedCurrent = Objects.requireNonNull(expectedCurrent, "expectedCurrent");
        this.publisher = Objects.requireNonNull(publisher, "publisher");
    }

    public Optional<EngineContext> expectedCurrent() { return expectedCurrent; }
    public ContextPublisher publisher() { return publisher; }
}

public final class CompilationRequest {
    private final SourceReference root;
    private final DocumentSourceProvider sourceProvider;
    private final FrontendRegistry frontends;
    private final CompilationOptions options;
    private final Optional<Deadline> deadline;
    private final CancellationToken cancellationToken;
    private final MonotonicClock clock;
    private final CompilationObserver observer;

    public CompilationRequest(
            SourceReference root,
            DocumentSourceProvider sourceProvider,
            FrontendRegistry frontends,
            CompilationOptions options,
            Optional<Deadline> deadline,
            CancellationToken cancellationToken,
            MonotonicClock clock,
            CompilationObserver observer) {
        this.root = Objects.requireNonNull(root, "root");
        this.sourceProvider = Objects.requireNonNull(sourceProvider, "sourceProvider");
        this.frontends = Objects.requireNonNull(frontends, "frontends");
        this.options = Objects.requireNonNull(options, "options");
        this.deadline = Objects.requireNonNull(deadline, "deadline");
        this.cancellationToken =
            Objects.requireNonNull(cancellationToken, "cancellationToken");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.observer = Objects.requireNonNull(observer, "observer");
    }

    public SourceReference root() { return root; }
    public DocumentSourceProvider sourceProvider() { return sourceProvider; }
    public FrontendRegistry frontends() { return frontends; }
    public CompilationOptions options() { return options; }
    public Optional<Deadline> deadline() { return deadline; }
    public CancellationToken cancellationToken() { return cancellationToken; }
    public MonotonicClock clock() { return clock; }
    public CompilationObserver observer() { return observer; }
}
```

`PublicationRequest` 包含调用方显式持有的 `expectedCurrent` 与 `ContextPublisher`。构造器的 null 是编程错误，按 Java 8 契约立即抛 `NullPointerException`，发生在 CompilationSession 创建前，不属于 CompilationResult 状态机；已构造请求中的未知 scheme、找不到 Source、超过预算等业务输入错误返回 FailedCompilationResult 与稳定 Diagnostic。不读取全局 Config，也不提供 compile 后再次 publish 的公共成功路径。

## 2. CompilationResult {#api-result}

```java
public interface CompilationResult {
    CompilationStatus status();
    List<Diagnostic> diagnostics();
}

public final class PublishedCompilationResult implements CompilationResult {
    private final List<Diagnostic> diagnostics;
    private final CompiledModelSet modelSet;
    private final EngineContext engineContext;
    private final DigestPair digests;
    private final String compilerVersion;
    private final String schemaVersion;
    private final String optionsDigest;
    private final String digestAlgorithmVersion;

    private PublishedCompilationResult(
            List<Diagnostic> diagnostics,
            CompiledModelSet modelSet,
            EngineContext engineContext,
            DigestPair digests,
            String compilerVersion,
            String schemaVersion,
            String optionsDigest,
            String digestAlgorithmVersion) {
        this.diagnostics = Collections.unmodifiableList(
            new ArrayList<Diagnostic>(
                Objects.requireNonNull(diagnostics, "diagnostics")));
        this.modelSet = Objects.requireNonNull(modelSet, "modelSet");
        this.engineContext = Objects.requireNonNull(engineContext, "engineContext");
        this.digests = Objects.requireNonNull(digests, "digests");
        this.compilerVersion = Objects.requireNonNull(compilerVersion, "compilerVersion");
        this.schemaVersion = Objects.requireNonNull(schemaVersion, "schemaVersion");
        this.optionsDigest = Objects.requireNonNull(optionsDigest, "optionsDigest");
        this.digestAlgorithmVersion =
            Objects.requireNonNull(digestAlgorithmVersion, "digestAlgorithmVersion");
    }

    public static PublishedCompilationResult published(
            List<Diagnostic> diagnostics,
            CompiledModelSet modelSet,
            EngineContext engineContext,
            DigestPair digests,
            String compilerVersion,
            String schemaVersion,
            String optionsDigest,
            String digestAlgorithmVersion) {
        return new PublishedCompilationResult(
            diagnostics, modelSet, engineContext, digests,
            compilerVersion, schemaVersion, optionsDigest, digestAlgorithmVersion);
    }

    public CompilationStatus status() { return CompilationStatus.PUBLISHED; }
    public List<Diagnostic> diagnostics() { return diagnostics; }
    public CompiledModelSet modelSet() { return modelSet; }
    public EngineContext engineContext() { return engineContext; }
    public DigestPair digests() { return digests; }
    public String compilerVersion() { return compilerVersion; }
    public String schemaVersion() { return schemaVersion; }
    public String optionsDigest() { return optionsDigest; }
    public String digestAlgorithmVersion() { return digestAlgorithmVersion; }
}

public final class FailedCompilationResult implements CompilationResult {
    private final List<Diagnostic> diagnostics;

    private FailedCompilationResult(List<Diagnostic> diagnostics) {
        this.diagnostics = Collections.unmodifiableList(
            new ArrayList<Diagnostic>(
                Objects.requireNonNull(diagnostics, "diagnostics")));
    }

    public static FailedCompilationResult failed(List<Diagnostic> diagnostics) {
        return new FailedCompilationResult(diagnostics);
    }

    public CompilationStatus status() { return CompilationStatus.FAILED; }
    public List<Diagnostic> diagnostics() { return diagnostics; }
}
```

约束：`CompilationStatus` 只包含 `PUBLISHED|FAILED`。PublishedCompilationResult 必须同时包含 modelSet/context/digests；FailedCompilationResult 只暴露 FAILED 与不可变 Diagnostic。cancel、timeout、CAS conflict 和构造失败均以 FAILED 加稳定 Diagnostic code 表达，不增加平行终态。

## 3. Source 与 Frontend {#api-source-frontend}

```java
public interface DocumentSourceProvider {
    SourceResolutionResult resolve(
        SourceReference reference, SourceResolutionContext context);
    SourceResolutionResult resolveFileSet(
        SourceReference reference, SourceResolutionContext context);
}
public interface SourceResolutionResult {
    SourceResolutionStatus status();
    List<DocumentSource> sources();
    List<Diagnostic> diagnostics();
}
public enum SourceResolutionStatus {
    RESOLVED, FAILED
}
public interface DocumentFrontend {
    DocumentFormat format();
    FrontendResult parse(DocumentSource source, FrontendOptions options);
}
public final class SourceGraphEdge {
    private final SourceEdgeType edgeType;
    private final String fromSourceId;
    private final SourceReference target;
    private final SourceRef declarationSourceRef;
    // Java 8 constructor/accessors/value semantics follow the common rule.
}
public enum SourceEdgeType {
    ROOT_DATA_FILESET,
    ROOT_VIEW_FILESET,
    ROOT_SYSTEM_FILE,
    ROOT_BUSINESS_FILE,
    SYSTEM_RULE_FILE
}
```

`resolve()` 的 RESOLVED 必须恰好携带 1 个 Source；`resolveFileSet()` 的 RESOLVED 必须携带至少 1 个 Source。两者都按规范化 sourceId 排序且无重复，并且 diagnostics 不含 ERROR；`FAILED` 必须携带至少一个 ERROR 且 sources 为空。Provider 不返回 null，也不以异常表达可预期的找不到、策略拒绝或 IO 错误；违反 Provider 契约转为 `MIX-SOURCE-POLICY`。FrontendResult 为 Canonical 或 Diagnostic，不暴露 DOM/YAML Node。每条 SourceGraphEdge 必须携带声明位置 SourceRef；根边使用 synthetic root ref。

## 4. Registry 与 Key {#api-registry}

```java
public interface DefinitionKey { String canonical(); }
public interface Registry<K extends DefinitionKey,V> {
    Optional<V> find(K key);
    V require(K key);
    List<K> keys();
    int size();
}
public final class InformationKey implements DefinitionKey {
    private final SystemKey owner;
    private final String name;

    public InformationKey(SystemKey owner, String name) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.name = Objects.requireNonNull(name, "name");
    }

    public SystemKey owner() { return owner; }
    public String name() { return name; }
    public String canonical() { return owner.canonical() + "." + name; }
}
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
        Optional<EngineContext> expectedCurrent,
        EngineContext candidate);
}

public interface PublicationResult {
    PublicationStatus status();
}

public enum PublicationStatus {
    PUBLISHED, CONFLICT
}
```

ContextPublisher 只能由 `ModelCompiler.compileAndPublish` 在同一 Session 的最终转换中调用一次。只有非 null `PublicationResult.status()==PUBLISHED` 才使 Session 进入 PUBLISHED；CONFLICT 产生 `MIX-PUBLICATION-CONFLICT`，null 或异常产生 `MIX-PUBLICATION-FAILURE`，都返回 FailedCompilationResult 且旧 Context 不变。Publisher 不隐式重试，调用方不得在方法返回后再次 publish。

## 9. CoreConfigProjection {#api-projection}

只提供 `data()/views()/rules()` 读取。任何旧写方法明确 deprecated 并抛 `ProjectionWriteRejectedException(MIX-PROJECTION-WRITE)`；名称不使用 Adapter，返回值不暴露 mutable collection。

## 10. Diagnostic 与兼容 {#api-diagnostic}

Diagnostic code 是稳定公共契约，message 可本地化；BM-R05 的 23 个 code 原样复用，不允许设计层重命名。技术状态补充 code 固定为 `MIX-STRUCTURE-UNKNOWN`、`MIX-COMPILATION-CANCELLED`、`MIX-COMPILATION-TIMED-OUT`、`MIX-CONTEXT-CONSTRUCTION-FAILED`、`MIX-PUBLICATION-CONFLICT`、`MIX-PUBLICATION-FAILURE`、`MIX-OBSERVER-FAILURE`。SourceRef/relatedRefs/recoveryHint 可选但 ERROR 必须有 code 和 SourceRef（非源级错误可使用 synthetic root ref）。排序字段 `entityKey` 固定取 `definitionKey.canonical()`，无 definitionKey 时取空字符串；最后一列统一命名为 `pass`。公共签名不得出现 `dec-expand-declaration` 类型。新增定义类型通过版本化 frontend/raw builder/pass 扩展，不改变既有 Key canonical 语义。

### 10.1 Timing Observer {#api-timing}

```java
public interface MonotonicClock { long nanoTime(); }
public interface CompilationObserver {
    void onTiming(CompilationTiming timing);
    void onStateTransition(SessionStateTransition transition);
}
public final class Deadline {
    private final long deadlineNanos;
    public Deadline(long deadlineNanos) { this.deadlineNanos = deadlineNanos; }
    public long deadlineNanos() { return deadlineNanos; }
    public boolean isExpired(long nowNanos) { return nowNanos >= deadlineNanos; }
}
public final class CompilationTiming {
    private final TimingPhase phase; // DISCOVERY|PARSE|PASS|DIGEST
    private final Optional<String> pass;
    private final long elapsedNanos;
    // Java 8 constructor/accessors/value semantics follow the common rule.
}
public final class SessionStateTransition {
    private final CompilationSessionState from;
    private final CompilationSessionState to;
    // Java 8 constructor/accessors/value semantics follow the common rule.
}
```

每次编译必须发送 discovery、parse、每个 pass、digest 的非负计时，并发送所有实际状态转换。Deadline 与 timing 共用注入的 MonotonicClock 纳秒域，测试不读取墙钟。Observer、Timing、Transition 和真实时钟值不进入 semanticDigest；Observer 异常只增加非 ERROR `MIX-OBSERVER-FAILURE`，不改变 status/context/digest。

## 11. 超时、取消、幂等 {#api-resilience}

- deadline/cancel 在 Pass 边界检查；
- cancel/timeout 返回 FAILED，不发布，并分别产生 `MIX-COMPILATION-CANCELLED`、`MIX-COMPILATION-TIMED-OUT`；
- compileAndPublish 不做隐式 IO retry；
- 相同 source/options/compiler version 的结果按 semanticDigest 幂等；
- Observer/Timing 不参与 semanticDigest；
- 调用方可用新 request 显式重试。

## 12. API 测试契约 {#api-testing}

每个 API 必须覆盖 success、typed failure、null/非法输入、不可变性、稳定顺序、timeout/cancel 和并发 Session 隔离。二进制兼容不承诺旧 declaration API；源码迁移以编译错误显式暴露。
