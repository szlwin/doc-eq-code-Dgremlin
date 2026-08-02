package dec.core.compiler.source;

import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 从固定 mix 根入口安全发现 Source，并构建精确声明图。
 */
public final class MixSourceResolver {
    private final SourceDeclarationParser declarationParser;

    /**
     * 创建使用安全最小声明解析器的 Resolver。
     */
    public MixSourceResolver() {
        this(new SourceDeclarationParser());
    }

    /**
     * 为包内测试保留可替换声明解析边界，不对外暴露第二套解析入口。
     */
    MixSourceResolver(SourceDeclarationParser declarationParser) {
        this.declarationParser = Objects.requireNonNull(
                declarationParser,
                "declarationParser");
    }

    /**
     * 解析根 Source，并在任何失败时返回不含部分图的稳定 FAILED 结果。
     *
     * <p>所有引用在调用 Provider 前执行 SourcePolicy；Provider typed-result
     * 在登记 Source 前执行防御性验证。目录展开项只进入 Manifest，不伪造
     * 声明边。除显式的 root、policy 参数校验外，根 SourceRef、策略验证、
     * Provider 判空和 Discovery 全部位于统一受控失败边界内。</p>
     */
    public SourceGraphResolutionResult resolve(
            SourceReference root,
            DocumentSourceProvider provider,
            SourcePolicy policy) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(policy, "policy");
        SourceRef rootRef = null;
        try {
            rootRef = new SourceRef(root.value(), 0, 0, "/root");
            Optional<Diagnostic> violation = policy.validateReference(
                    root,
                    0,
                    rootRef);
            if (violation.isPresent()) {
                return SourceGraphResolutionResults.failed(
                        Collections.singletonList(violation.get()));
            }
            if (provider == null) {
                return SourceGraphResolutionResults.failed(Collections.singletonList(
                        policyDiagnostic(
                                "source.provider.missing",
                                rootRef,
                                "注入非空 DocumentSourceProvider")));
            }

            Discovery discovery = new Discovery(
                    root,
                    provider,
                    policy,
                    declarationParser);
            return discovery.resolve();
        } catch (SourceFailure failure) {
            return SourceGraphResolutionResults.failed(failure.diagnostics());
        } catch (RuntimeException unexpected) {
            return SourceGraphResolutionResults.failed(Collections.singletonList(
                    policyDiagnostic(
                            "source.discovery.unexpected",
                            diagnosticRootRef(rootRef),
                            "检查 Source 声明格式和 Provider 返回合同")));
        }
    }

    /**
     * 单次解析调用的隔离可变状态；完成后只发布不可变图。
     */
    private static final class Discovery {
        private final SourceReference root;
        private final DocumentSourceProvider provider;
        private final SourcePolicy policy;
        private final SourceDeclarationParser parser;
        private final Map<String, DocumentSource> sources =
                new LinkedHashMap<String, DocumentSource>();
        private final List<SourceGraphEdge> edges =
                new ArrayList<SourceGraphEdge>();
        private final Set<String> edgeKeys = new LinkedHashSet<String>();
        private final List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
        private long totalBytes;

        private Discovery(
                SourceReference root,
                DocumentSourceProvider provider,
                SourcePolicy policy,
                SourceDeclarationParser parser) {
            this.root = root;
            this.provider = provider;
            this.policy = policy;
            this.parser = parser;
        }

        /**
         * 按 root 声明顺序完成文件集、system/rule 和 business 发现。
         */
        private SourceGraphResolutionResult resolve() {
            SourceRef rootRef = new SourceRef(root.value(), 0, 0, "/root");
            DocumentSource rootSource = resolveSingle(
                    root,
                    rootRef,
                    0,
                    Optional.<String>empty(),
                    Collections.<String>emptySet());
            registerSource(rootSource, 0, rootRef);

            List<SourceGraphEdge> rootEdges = parseRoot(rootSource);
            // 先按 canonical target 登记边，确保等价文本优先映射为重复边。
            for (SourceGraphEdge edge : rootEdges) {
                registerEdge(edge);
            }
            RootDeclarations rootDeclarations = RootDeclarations.from(
                    rootEdges,
                    rootRef);

            Set<String> rootAncestorReferences = singletonAncestor(root.value());
            resolveFileSet(
                    rootDeclarations.dataFileSet(),
                    1,
                    rootSource.sourceId(),
                    rootAncestorReferences);
            resolveFileSet(
                    rootDeclarations.viewFileSet(),
                    1,
                    rootSource.sourceId(),
                    rootAncestorReferences);

            SourceGraphEdge systemEdge = rootDeclarations.systemFile();
            DocumentSource systemsSource = resolveSingle(
                    systemEdge.targetReference(),
                    systemEdge.declarationSourceRef(),
                    1,
                    Optional.of(rootSource.sourceId()),
                    rootAncestorReferences);
            registerSource(
                    systemsSource,
                    1,
                    systemEdge.declarationSourceRef());

            List<SourceGraphEdge> ruleEdges = parseSystems(systemsSource);
            for (SourceGraphEdge edge : ruleEdges) {
                registerEdge(edge);
            }
            Set<String> ruleAncestorReferences = new LinkedHashSet<String>();
            ruleAncestorReferences.add(root.value());
            ruleAncestorReferences.add(systemEdge.targetReference().value());
            for (SourceGraphEdge ruleEdge : ruleEdges) {
                DocumentSource ruleSource = resolveSingle(
                        ruleEdge.targetReference(),
                        ruleEdge.declarationSourceRef(),
                        2,
                        Optional.of(systemsSource.sourceId()),
                        ruleAncestorReferences);
                registerSource(
                        ruleSource,
                        2,
                        ruleEdge.declarationSourceRef());
            }

            SourceGraphEdge businessEdge = rootDeclarations.businessFile();
            DocumentSource businessSource = resolveSingle(
                    businessEdge.targetReference(),
                    businessEdge.declarationSourceRef(),
                    1,
                    Optional.of(rootSource.sourceId()),
                    rootAncestorReferences);
            registerSource(
                    businessSource,
                    1,
                    businessEdge.declarationSourceRef());

            SourceManifest manifest = new SourceManifest(
                    new ArrayList<DocumentSource>(sources.values()));
            MixSourceGraph graph = new MixSourceGraph(manifest, edges);
            return SourceGraphResolutionResults.resolved(graph, diagnostics);
        }

        /**
         * 安全解析单个 Source，并保留 Provider 的业务失败 Diagnostic。
         */
        private DocumentSource resolveSingle(
                SourceReference reference,
                SourceRef declarationRef,
                int depth,
                Optional<String> parentSourceId,
                Set<String> ancestorReferenceKeys) {
            validateBeforeProvider(
                    reference,
                    declarationRef,
                    depth,
                    ancestorReferenceKeys);
            SourceResolutionResult raw;
            try {
                raw = provider.resolve(
                        reference,
                        new ImmutableResolutionContext(
                                root,
                                parentSourceId,
                                depth));
            } catch (RuntimeException providerFailure) {
                throw failure(policyDiagnostic(
                        "source.provider.exception",
                        declarationRef,
                        "修复 DocumentSourceProvider.resolve 实现"));
            }

            SourceResolutionResult checked = validateProviderResult(
                    reference,
                    raw,
                    true,
                    declarationRef);
            if (checked.status() == SourceResolutionStatus.FAILED) {
                throw new SourceFailure(checked.diagnostics());
            }
            diagnostics.addAll(checked.diagnostics());
            return checked.sources().get(0);
        }

        /**
         * 安全展开文件集；返回顺序由 T02 validator 按 sourceId 固定。
         */
        private void resolveFileSet(
                SourceGraphEdge edge,
                int depth,
                String parentSourceId,
                Set<String> ancestorReferenceKeys) {
            SourceReference reference = edge.targetReference();
            validateBeforeProvider(
                    reference,
                    edge.declarationSourceRef(),
                    depth,
                    ancestorReferenceKeys);
            SourceResolutionResult raw;
            try {
                raw = provider.resolveFileSet(
                        reference,
                        new ImmutableResolutionContext(
                                root,
                                Optional.of(parentSourceId),
                                depth));
            } catch (RuntimeException providerFailure) {
                throw failure(policyDiagnostic(
                        "source.provider.fileset.exception",
                        edge.declarationSourceRef(),
                        "修复 DocumentSourceProvider.resolveFileSet 实现"));
            }

            SourceResolutionResult checked = validateProviderResult(
                    reference,
                    raw,
                    false,
                    edge.declarationSourceRef());
            if (checked.status() == SourceResolutionStatus.FAILED) {
                throw new SourceFailure(checked.diagnostics());
            }
            diagnostics.addAll(checked.diagnostics());
            for (DocumentSource source : checked.sources()) {
                registerSource(source, depth, edge.declarationSourceRef());
            }
        }

        /**
         * 在 Provider 调用前执行路径、scheme、深度和 canonical 引用环路验证。
         */
        private void validateBeforeProvider(
                SourceReference reference,
                SourceRef declarationRef,
                int depth,
                Set<String> ancestorReferenceKeys) {
            Optional<Diagnostic> violation = policy.validateReference(
                    reference,
                    depth,
                    declarationRef);
            if (violation.isPresent()) {
                throw failure(violation.get());
            }
            if (ancestorReferenceKeys.contains(reference.value())) {
                throw failure(policyDiagnostic(
                        "source.graph.cycle",
                        declarationRef,
                        "移除 Source 声明环路"));
            }
        }

        /**
         * 在调用 T02 validator 前快照第三方结果，并为重复 ID 保留专用错误码。
         */
        private SourceResolutionResult validateProviderResult(
                SourceReference reference,
                SourceResolutionResult raw,
                boolean single,
                SourceRef declarationRef) {
            ProviderResultSnapshot snapshot = snapshot(
                    raw,
                    single,
                    declarationRef);
            if (!single) {
                String duplicateId = duplicateSourceId(snapshot.sources());
                if (duplicateId != null) {
                    throw failure(duplicateDiagnostic(
                            duplicateId,
                            declarationRef));
                }
            }

            SourceResolutionResult checked = single
                    ? SourceResolutionResults.validateSingle(reference, snapshot)
                    : SourceResolutionResults.validateFileSet(reference, snapshot);
            if (checked.status() == SourceResolutionStatus.FAILED
                    && snapshot.status() == SourceResolutionStatus.RESOLVED) {
                throw failure(policyDiagnostic(
                        "source.provider.resolved-contract",
                        declarationRef,
                        "修复 Provider 的 RESOLVED 基数、候选或 Diagnostic"));
            }
            return checked;
        }

        /**
         * 防御性快照第三方结果，并在调用公共 validator 前校验基本不变量。
         */
        private ProviderResultSnapshot snapshot(
                SourceResolutionResult raw,
                boolean single,
                SourceRef declarationRef) {
            try {
                if (raw == null) {
                    throw new IllegalArgumentException("provider result is null");
                }
                SourceResolutionStatus status = Objects.requireNonNull(
                        raw.status(),
                        "provider result status");
                List<DocumentSource> sourceCopy = new ArrayList<DocumentSource>(
                        Objects.requireNonNull(
                                raw.sources(),
                                "provider result sources"));
                List<Diagnostic> diagnosticCopy = new ArrayList<Diagnostic>(
                        Objects.requireNonNull(
                                raw.diagnostics(),
                                "provider result diagnostics"));
                for (DocumentSource source : sourceCopy) {
                    Objects.requireNonNull(source, "provider sources contains null");
                }
                for (Diagnostic diagnostic : diagnosticCopy) {
                    Objects.requireNonNull(
                            diagnostic,
                            "provider diagnostics contains null");
                }

                if (status == SourceResolutionStatus.RESOLVED) {
                    if (single && sourceCopy.size() != 1) {
                        throw new IllegalArgumentException(
                                "single result must contain exactly one source");
                    }
                    if (!single && sourceCopy.isEmpty()) {
                        throw new IllegalArgumentException(
                                "file set result must contain at least one source");
                    }
                    if (hasError(diagnosticCopy)) {
                        throw new IllegalArgumentException(
                                "resolved result must not contain ERROR");
                    }
                } else {
                    if (!sourceCopy.isEmpty() || !hasError(diagnosticCopy)) {
                        throw new IllegalArgumentException(
                                "failed result must have no source and at least one ERROR");
                    }
                }
                return new ProviderResultSnapshot(
                        status,
                        sourceCopy,
                        diagnosticCopy);
            } catch (RuntimeException contractFailure) {
                throw failure(policyDiagnostic(
                        "source.provider.contract",
                        declarationRef,
                        "修复 DocumentSourceProvider 返回合同"));
            }
        }

        /**
         * 登记 Source，并在写入可变状态前检查根边界、重复身份和资源预算。
         */
        private void registerSource(
                DocumentSource source,
                int depth,
                SourceRef declarationRef) {
            SourceReference sourceReference = new SourceReference(
                    source.uri().toString());
            Optional<Diagnostic> violation = policy.validateReference(
                    sourceReference,
                    depth,
                    declarationRef);
            if (violation.isPresent()) {
                throw failure(violation.get());
            }
            if (sources.containsKey(source.sourceId())) {
                throw failure(duplicateDiagnostic(
                        source.sourceId(),
                        declarationRef));
            }

            int nextCount = sources.size() + 1;
            long nextBytes = totalBytes + source.content().length;
            if (nextCount > policy.maxSources()) {
                throw failure(policyDiagnostic(
                        "source.policy.max-sources",
                        declarationRef,
                        "减少 Source 数量或提高 SourcePolicy.maxSources"));
            }
            if (nextBytes > policy.maxTotalBytes()) {
                throw failure(policyDiagnostic(
                        "source.policy.max-total-bytes",
                        declarationRef,
                        "减少 Source 内容或提高 SourcePolicy.maxTotalBytes"));
            }
            sources.put(source.sourceId(), source);
            totalBytes = nextBytes;
        }

        /**
         * 登记真实声明边，并拒绝 canonical from/type/target 相同的重复声明。
         */
        private void registerEdge(SourceGraphEdge edge) {
            String key = edge.edgeType()
                    + "|" + edge.fromSourceId()
                    + "|" + edge.targetReference().value();
            if (!edgeKeys.add(key)) {
                throw failure(policyDiagnostic(
                        "source.edge.duplicate",
                        edge.declarationSourceRef(),
                        "移除重复 Source 声明"));
            }
            edges.add(edge);
        }

        /**
         * 安全提取 root 声明，并把 XML/结构错误映射为 Source policy 失败。
         */
        private List<SourceGraphEdge> parseRoot(DocumentSource source) {
            try {
                return parser.parseRoot(source);
            } catch (SourceDeclarationParser.SourceDeclarationException parseFailure) {
                throw failure(policyDiagnostic(
                        "source.root.declaration.invalid",
                        declarationRef(source),
                        "修复 root Source 声明 XML"));
            }
        }

        /**
         * 安全提取 system rule 声明，并把 XML/结构错误映射为 Source policy 失败。
         */
        private List<SourceGraphEdge> parseSystems(DocumentSource source) {
            try {
                return parser.parseSystems(source);
            } catch (SourceDeclarationParser.SourceDeclarationException parseFailure) {
                throw failure(policyDiagnostic(
                        "source.system.declaration.invalid",
                        declarationRef(source),
                        "修复 systems Source 中的 rule-file 声明"));
            }
        }

        /**
         * 返回 Source 文档级 synthetic 声明位置。
         */
        private static SourceRef declarationRef(DocumentSource source) {
            return new SourceRef(source.sourceId(), 0, 0, "/declarations");
        }
    }

    /**
     * root 必须恰好提供四类阻断声明；缺失或重复均不能产生部分图。
     */
    private static final class RootDeclarations {
        private final SourceGraphEdge dataFileSet;
        private final SourceGraphEdge viewFileSet;
        private final SourceGraphEdge systemFile;
        private final SourceGraphEdge businessFile;

        private RootDeclarations(
                SourceGraphEdge dataFileSet,
                SourceGraphEdge viewFileSet,
                SourceGraphEdge systemFile,
                SourceGraphEdge businessFile) {
            this.dataFileSet = dataFileSet;
            this.viewFileSet = viewFileSet;
            this.systemFile = systemFile;
            this.businessFile = businessFile;
        }

        private static RootDeclarations from(
                List<SourceGraphEdge> edges,
                SourceRef rootRef) {
            return new RootDeclarations(
                    requireSingle(edges, SourceEdgeType.ROOT_DATA_FILESET, rootRef),
                    requireSingle(edges, SourceEdgeType.ROOT_VIEW_FILESET, rootRef),
                    requireSingle(edges, SourceEdgeType.ROOT_SYSTEM_FILE, rootRef),
                    requireSingle(edges, SourceEdgeType.ROOT_BUSINESS_FILE, rootRef));
        }

        private static SourceGraphEdge requireSingle(
                List<SourceGraphEdge> edges,
                SourceEdgeType type,
                SourceRef rootRef) {
            SourceGraphEdge result = null;
            for (SourceGraphEdge edge : edges) {
                if (edge.edgeType() == type) {
                    if (result != null) {
                        throw failure(policyDiagnostic(
                                "source.root.declaration.duplicate",
                                edge.declarationSourceRef(),
                                "每类 root Source 只声明一次"));
                    }
                    result = edge;
                }
            }
            if (result == null) {
                throw failure(policyDiagnostic(
                        "source.root.declaration.missing",
                        rootRef,
                        "补齐 root 的 data、view、system 和 business 声明"));
            }
            return result;
        }

        private SourceGraphEdge dataFileSet() {
            return dataFileSet;
        }

        private SourceGraphEdge viewFileSet() {
            return viewFileSet;
        }

        private SourceGraphEdge systemFile() {
            return systemFile;
        }

        private SourceGraphEdge businessFile() {
            return businessFile;
        }
    }

    /**
     * Provider 返回值的不可变快照，防止验证期间被第三方修改。
     */
    private static final class ProviderResultSnapshot
            implements SourceResolutionResult {
        private final SourceResolutionStatus status;
        private final List<DocumentSource> sources;
        private final List<Diagnostic> diagnostics;

        private ProviderResultSnapshot(
                SourceResolutionStatus status,
                List<DocumentSource> sources,
                List<Diagnostic> diagnostics) {
            this.status = status;
            this.sources = Collections.unmodifiableList(
                    new ArrayList<DocumentSource>(sources));
            List<Diagnostic> diagnosticCopy = new ArrayList<Diagnostic>(diagnostics);
            Collections.sort(diagnosticCopy);
            this.diagnostics = Collections.unmodifiableList(diagnosticCopy);
        }

        @Override
        public SourceResolutionStatus status() {
            return status;
        }

        @Override
        public List<DocumentSource> sources() {
            return sources;
        }

        @Override
        public List<Diagnostic> diagnostics() {
            return diagnostics;
        }
    }

    /**
     * Provider 解析时使用的不可变 Session 上下文。
     */
    private static final class ImmutableResolutionContext
            implements SourceResolutionContext {
        private final SourceReference root;
        private final Optional<String> parentSourceId;
        private final int depth;

        private ImmutableResolutionContext(
                SourceReference root,
                Optional<String> parentSourceId,
                int depth) {
            this.root = root;
            this.parentSourceId = parentSourceId;
            this.depth = depth;
        }

        @Override
        public SourceReference root() {
            return root;
        }

        @Override
        public Optional<String> parentSourceId() {
            return parentSourceId;
        }

        @Override
        public int depth() {
            return depth;
        }
    }

    /**
     * 内部受控失败，只携带最终需要发布的稳定 Diagnostic。
     */
    private static final class SourceFailure extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final List<Diagnostic> diagnostics;

        private SourceFailure(List<Diagnostic> diagnostics) {
            super("Source discovery failed");
            this.diagnostics = Collections.unmodifiableList(
                    new ArrayList<Diagnostic>(diagnostics));
        }

        private List<Diagnostic> diagnostics() {
            return diagnostics;
        }
    }

    /**
     * 根 SourceRef 尚未成功创建时，返回不会再次触发值对象异常的稳定诊断位置。
     */
    private static SourceRef diagnosticRootRef(SourceRef rootRef) {
        return rootRef == null
                ? new SourceRef("source-root", 0, 0, "/root")
                : rootRef;
    }

    /**
     * 返回只有一个祖先 canonical 引用键的稳定集合。
     */
    private static Set<String> singletonAncestor(String referenceKey) {
        return Collections.singleton(referenceKey);
    }

    /**
     * 判断 Diagnostic 集合是否包含 ERROR。
     */
    private static boolean hasError(List<Diagnostic> diagnostics) {
        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.severity() == DiagnosticSeverity.ERROR) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回集合中的首个重复 sourceId；无重复时返回 null。
     */
    private static String duplicateSourceId(List<DocumentSource> sources) {
        Set<String> ids = new HashSet<String>();
        for (DocumentSource source : sources) {
            if (!ids.add(source.sourceId())) {
                return source.sourceId();
            }
        }
        return null;
    }

    /**
     * 创建只含一个 Diagnostic 的内部受控失败。
     */
    private static SourceFailure failure(Diagnostic diagnostic) {
        return new SourceFailure(Collections.singletonList(diagnostic));
    }

    /**
     * 创建 Source policy 合同失败 Diagnostic。
     */
    private static Diagnostic policyDiagnostic(
            String messageKey,
            SourceRef sourceRef,
            String recoveryHint) {
        return diagnostic(
                DiagnosticCode.MIX_SOURCE_POLICY,
                messageKey,
                sourceRef,
                recoveryHint);
    }

    /**
     * 创建重复 Source 身份 Diagnostic。
     */
    private static Diagnostic duplicateDiagnostic(
            String sourceId,
            SourceRef sourceRef) {
        return diagnostic(
                DiagnosticCode.MIX_SOURCE_DUPLICATE_ID,
                "source.duplicate-id." + sourceId,
                sourceRef,
                "为每个 Source 提供唯一且稳定的 sourceId");
    }

    /**
     * 创建 Source discovery 阶段的稳定 ERROR Diagnostic。
     */
    private static Diagnostic diagnostic(
            DiagnosticCode code,
            String messageKey,
            SourceRef sourceRef,
            String recoveryHint) {
        return new Diagnostic(
                code,
                DiagnosticSeverity.ERROR,
                messageKey,
                null,
                sourceRef,
                Collections.<SourceRef>emptyList(),
                recoveryHint,
                "SourceDiscoveryPass");
    }
}
