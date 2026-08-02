package dec.core.compiler.source;

import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 单次 Source discovery 的不可变安全边界和资源预算。
 */
public final class SourcePolicy {
    private final Set<String> allowedSchemes;
    private final AllowedRoot allowedRoot;
    private final int maxDepth;
    private final int maxSources;
    private final long maxTotalBytes;

    /**
     * 冻结 scheme 白名单、允许根和资源限制。
     */
    public SourcePolicy(
            Set<String> allowedSchemes,
            AllowedRoot allowedRoot,
            int maxDepth,
            int maxSources,
            long maxTotalBytes) {
        Objects.requireNonNull(allowedSchemes, "allowedSchemes");
        List<String> normalized = new ArrayList<String>(allowedSchemes.size());
        for (String scheme : allowedSchemes) {
            String value = Objects.requireNonNull(
                    scheme,
                    "allowedSchemes contains null").trim().toLowerCase(Locale.ROOT);
            if (value.isEmpty()) {
                throw new IllegalArgumentException(
                        "allowedSchemes must not contain blank values");
            }
            normalized.add(value);
        }
        Collections.sort(normalized);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("allowedSchemes must not be empty");
        }
        this.allowedSchemes = Collections.unmodifiableSet(
                new LinkedHashSet<String>(normalized));
        this.allowedRoot = Objects.requireNonNull(allowedRoot, "allowedRoot");
        if (maxDepth < 0) {
            throw new IllegalArgumentException("maxDepth must be >= 0");
        }
        if (maxSources <= 0) {
            throw new IllegalArgumentException("maxSources must be > 0");
        }
        if (maxTotalBytes <= 0L) {
            throw new IllegalArgumentException("maxTotalBytes must be > 0");
        }
        this.maxDepth = maxDepth;
        this.maxSources = maxSources;
        this.maxTotalBytes = maxTotalBytes;
    }

    public Set<String> allowedSchemes() {
        return allowedSchemes;
    }

    public AllowedRoot allowedRoot() {
        return allowedRoot;
    }

    public int maxDepth() {
        return maxDepth;
    }

    public int maxSources() {
        return maxSources;
    }

    public long maxTotalBytes() {
        return maxTotalBytes;
    }

    /**
     * 在调用 Provider 前验证引用的 scheme、根边界和解析深度。
     *
     * <p>路径类违规必须在任何 IO 或 Provider 访问前返回，因此该方法
     * 不执行规范化后的容错访问，也不会尝试其它同名资源。</p>
     *
     * @param reference 待解析的 Source 引用
     * @param depth 当前引用相对根 Source 的深度
     * @param declarationSourceRef 声明该引用的位置
     * @return 无违规时为空，否则返回稳定 ERROR Diagnostic
     */
    Optional<Diagnostic> validateReference(
            SourceReference reference,
            int depth,
            SourceRef declarationSourceRef) {
        Objects.requireNonNull(reference, "reference");
        Objects.requireNonNull(declarationSourceRef, "declarationSourceRef");
        if (depth > maxDepth) {
            return Optional.of(diagnostic(
                    DiagnosticCode.MIX_SOURCE_POLICY,
                    "source.policy.max-depth",
                    declarationSourceRef,
                    "降低声明深度或提高 SourcePolicy.maxDepth"));
        }

        try {
            URI uri = URI.create(reference.value());
            String scheme = uri.getScheme();
            boolean allowedScheme = scheme != null
                    && allowedSchemes.contains(scheme.toLowerCase(Locale.ROOT));
            if (!uri.isAbsolute()
                    || !allowedScheme
                    || !allowedRoot.contains(uri)) {
                return Optional.of(pathEscape(declarationSourceRef));
            }
        } catch (IllegalArgumentException invalidUri) {
            return Optional.of(pathEscape(declarationSourceRef));
        }
        return Optional.empty();
    }

    /**
     * 创建路径越界 Diagnostic；调用方不得在返回后继续访问 Provider。
     */
    private static Diagnostic pathEscape(SourceRef sourceRef) {
        return diagnostic(
                DiagnosticCode.MIX_SOURCE_PATH_ESCAPE,
                "source.path.escape",
                sourceRef,
                "仅声明允许根内且 scheme 已授权的绝对 Source URI");
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SourcePolicy)) {
            return false;
        }
        SourcePolicy that = (SourcePolicy) other;
        return maxDepth == that.maxDepth
                && maxSources == that.maxSources
                && maxTotalBytes == that.maxTotalBytes
                && allowedSchemes.equals(that.allowedSchemes)
                && allowedRoot.equals(that.allowedRoot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                allowedSchemes,
                allowedRoot,
                maxDepth,
                maxSources,
                maxTotalBytes);
    }

    @Override
    public String toString() {
        return "SourcePolicy{"
                + "allowedSchemes=" + allowedSchemes
                + ", allowedRoot=" + allowedRoot
                + ", maxDepth=" + maxDepth
                + ", maxSources=" + maxSources
                + ", maxTotalBytes=" + maxTotalBytes
                + '}';
    }
}
