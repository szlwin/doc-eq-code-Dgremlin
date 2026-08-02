package dec.core.compiler.api;

import dec.core.context.EngineContext;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DigestPair;
import java.util.List;
import java.util.Objects;

/**
 * 成功发布终态，包含 Publisher 实际暴露的完整发布事实。
 */
public final class PublishedCompilationResult implements CompilationResult {
    private final List<Diagnostic> diagnostics;
    private final CompiledModelSet modelSet;
    private final EngineContext engineContext;
    private final DigestPair digests;
    private final String compilerVersion;
    private final String schemaVersion;
    private final String optionsDigest;
    private final String digestAlgorithmVersion;

    /**
     * 冻结并校验成功发布的完整事实。
     */
    private PublishedCompilationResult(
            List<Diagnostic> diagnostics,
            CompiledModelSet modelSet,
            EngineContext engineContext,
            DigestPair digests,
            String compilerVersion,
            String schemaVersion,
            String optionsDigest,
            String digestAlgorithmVersion) {
        this.modelSet = Objects.requireNonNull(modelSet, "modelSet");
        this.engineContext = Objects.requireNonNull(engineContext, "engineContext");
        this.digests = Objects.requireNonNull(digests, "digests");
        this.compilerVersion = ApiContracts.requireText(
                compilerVersion,
                "compilerVersion");
        this.schemaVersion = ApiContracts.requireText(schemaVersion, "schemaVersion");
        this.optionsDigest = ApiContracts.requireText(optionsDigest, "optionsDigest");
        this.digestAlgorithmVersion = ApiContracts.requireText(
                digestAlgorithmVersion,
                "digestAlgorithmVersion");

        // 成功结果必须包装 Publisher 实际暴露 Context 所持有的同一个模型实例。
        if (modelSet != engineContext.compiledModelSet()) {
            throw new IllegalArgumentException(
                    "engineContext must reference the exact published modelSet instance");
        }

        List<Diagnostic> validated = ApiContracts.publishedDiagnostics(diagnostics);
        if (!modelSet.diagnostics().equals(validated)) {
            throw new IllegalArgumentException(
                    "published diagnostics must match modelSet diagnostics");
        }
        if (!modelSet.digestPair().equals(digests)) {
            throw new IllegalArgumentException("digests must match modelSet digestPair");
        }
        requireEqualText(
                modelSet.compilerVersion(),
                this.compilerVersion,
                "compilerVersion");
        requireEqualText(modelSet.schemaVersion(), this.schemaVersion, "schemaVersion");
        // T01 的 optionsVersion 兼容字段在 P1 中承载同一个规范化 options digest。
        requireEqualText(modelSet.optionsVersion(), this.optionsDigest, "optionsDigest");

        // 模型已完成排序和不可变冻结，成功结果必须复用同一 Diagnostic 事实实例。
        this.diagnostics = modelSet.diagnostics();
    }

    /**
     * 创建成功发布结果，并确保所有公开事实均来自同一个 T01 发布模型。
     *
     * @param diagnostics 与模型 Diagnostic 等值的成功诊断集合
     * @param modelSet Compiler 产生的完整不可变模型
     * @param engineContext Publisher 实际暴露的 Context
     * @param digests 与模型一致的源摘要和语义摘要
     * @param compilerVersion 与模型一致的 Compiler 版本
     * @param schemaVersion 与模型一致的 Schema 版本
     * @param optionsDigest 与模型兼容字段一致的规范化选项摘要
     * @param digestAlgorithmVersion 非空白 Digest 算法合同版本
     * @return 完整且不可重新拼接的成功发布事实
     */
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
                diagnostics,
                modelSet,
                engineContext,
                digests,
                compilerVersion,
                schemaVersion,
                optionsDigest,
                digestAlgorithmVersion);
    }

    /**
     * 校验调用方提供的版本事实必须与模型事实一致。
     */
    private static void requireEqualText(
            String expected,
            String actual,
            String fieldName) {
        if (!expected.equals(actual)) {
            throw new IllegalArgumentException(
                    fieldName + " must match modelSet published fact");
        }
    }

    @Override
    public CompilationStatus status() {
        return CompilationStatus.PUBLISHED;
    }

    @Override
    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }

    /**
     * 返回 Compiler 产生的完整不可变模型。
     */
    public CompiledModelSet modelSet() {
        return modelSet;
    }

    /**
     * 返回 Publisher 实际暴露的不可变 EngineContext。
     */
    public EngineContext engineContext() {
        return engineContext;
    }

    /**
     * 返回确定性的源摘要和语义摘要。
     */
    public DigestPair digests() {
        return digests;
    }

    /**
     * 返回参与发布语义身份的 Compiler 版本。
     */
    public String compilerVersion() {
        return compilerVersion;
    }

    /**
     * 返回解释输入源时使用的 Schema 版本。
     */
    public String schemaVersion() {
        return schemaVersion;
    }

    /**
     * 返回规范化编译选项摘要。
     */
    public String optionsDigest() {
        return optionsDigest;
    }

    /**
     * 返回 Digest 算法合同版本。
     */
    public String digestAlgorithmVersion() {
        return digestAlgorithmVersion;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PublishedCompilationResult)) {
            return false;
        }
        PublishedCompilationResult that = (PublishedCompilationResult) other;
        return diagnostics.equals(that.diagnostics)
                && modelSet.equals(that.modelSet)
                && engineContext.equals(that.engineContext)
                && digests.equals(that.digests)
                && compilerVersion.equals(that.compilerVersion)
                && schemaVersion.equals(that.schemaVersion)
                && optionsDigest.equals(that.optionsDigest)
                && digestAlgorithmVersion.equals(that.digestAlgorithmVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                diagnostics,
                modelSet,
                engineContext,
                digests,
                compilerVersion,
                schemaVersion,
                optionsDigest,
                digestAlgorithmVersion);
    }

    @Override
    public String toString() {
        return "PublishedCompilationResult{"
                + "semanticDigest='" + digests.semanticDigest() + '\''
                + ", compilerVersion='" + compilerVersion + '\''
                + ", schemaVersion='" + schemaVersion + '\''
                + ", optionsDigest='" + optionsDigest + '\''
                + ", digestAlgorithmVersion='" + digestAlgorithmVersion + '\''
                + ", diagnostics=" + diagnostics.size()
                + '}';
    }
}
