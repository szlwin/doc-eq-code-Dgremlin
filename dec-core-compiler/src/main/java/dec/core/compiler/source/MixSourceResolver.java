package dec.core.compiler.source;

import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

/**
 * 从固定 mix 根入口安全发现 Source，并构建精确声明图。
 */
public final class MixSourceResolver {
    /**
     * 解析根 Source；Architecture Skeleton 阶段先落实 Provider 前置安全门禁。
     *
     * <p>安全策略失败或 Provider 缺失时直接返回无候选 FAILED，任何
     * 不安全根引用都不会触发 Provider。安全根的完整发现流程仍保持受控 RED。</p>
     */
    public SourceGraphResolutionResult resolve(
            SourceReference root,
            DocumentSourceProvider provider,
            SourcePolicy policy) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(policy, "policy");
        SourceRef rootRef = new SourceRef(root.value(), 0, 0, "/root");
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
                    diagnostic(
                            DiagnosticCode.MIX_SOURCE_POLICY,
                            "source.provider.missing",
                            rootRef,
                            "注入非空 DocumentSourceProvider")));
        }
        throw new AssertionError("Architecture skeleton only");
    }

    /**
     * 创建 Source discovery 合同失败 Diagnostic。
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
