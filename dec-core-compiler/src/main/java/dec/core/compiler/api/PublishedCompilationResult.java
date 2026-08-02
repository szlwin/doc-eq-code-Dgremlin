package dec.core.compiler.api;

import dec.core.context.EngineContext;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DigestPair;
import java.util.List;
import java.util.Objects;

/**
 * 成功发布终态，包含 Publisher 实际暴露的模型与 Context。
 */
public final class PublishedCompilationResult extends CompilationResult {
    private final CompiledModelSet compiledModelSet;
    private final EngineContext context;

    /**
     * 冻结成功发布的模型、Context 和非 ERROR Diagnostic。
     *
     * <p>当前实现先按旧 T02 行为重放，用于让 I002 新合同测试形成有效 RED；
     * Development 阶段将按 DESIGN-R09 收紧为精确发布事实。</p>
     *
     * @param sessionId 完成编译会话的稳定身份
     * @param compiledModelSet 声称已经发布的不可变模型
     * @param context Publisher 实际暴露的不可变 Context
     * @param diagnostics 不含 ERROR 的稳定 Diagnostic 快照
     */
    public PublishedCompilationResult(
            String sessionId,
            CompiledModelSet compiledModelSet,
            EngineContext context,
            List<Diagnostic> diagnostics) {
        super(sessionId, ApiContracts.publishedDiagnostics(diagnostics));
        this.compiledModelSet = Objects.requireNonNull(
                compiledModelSet,
                "compiledModelSet");
        this.context = Objects.requireNonNull(context, "context");
        // I002 RED：旧实现只比较值相等，尚未证明二者属于同一发布聚合实例。
        if (!compiledModelSet.equals(context.compiledModelSet())) {
            throw new IllegalArgumentException(
                    "context must be backed by the published compiledModelSet");
        }
    }

    /**
     * 返回 Compiler 产生的完整不可变模型。
     */
    public CompiledModelSet compiledModelSet() {
        return compiledModelSet;
    }

    /**
     * 返回 Publisher 已暴露的不可变 Context。
     */
    public EngineContext context() {
        return context;
    }

    /**
     * 返回确定性的源 Digest 与语义 Digest。
     */
    public DigestPair digests() {
        return compiledModelSet.digestPair();
    }

    /**
     * 返回参与发布语义身份的 Compiler 版本。
     */
    public String compilerVersion() {
        return compiledModelSet.compilerVersion();
    }

    /**
     * 返回本次编译使用的 Schema 版本。
     */
    public String schemaVersion() {
        return compiledModelSet.schemaVersion();
    }

    /**
     * 返回本次编译使用的规范化选项版本。
     */
    public String optionsVersion() {
        return compiledModelSet.optionsVersion();
    }

    @Override
    public CompilationStatus status() {
        return CompilationStatus.PUBLISHED;
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
        return sessionId().equals(that.sessionId())
                && compiledModelSet.equals(that.compiledModelSet)
                && context.equals(that.context)
                && diagnostics().equals(that.diagnostics());
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId(), compiledModelSet, context, diagnostics());
    }

    @Override
    public String toString() {
        return "PublishedCompilationResult{"
                + "sessionId='" + sessionId() + '\''
                + ", semanticDigest='" + digests().semanticDigest() + '\''
                + ", diagnostics=" + diagnostics().size()
                + '}';
    }
}
