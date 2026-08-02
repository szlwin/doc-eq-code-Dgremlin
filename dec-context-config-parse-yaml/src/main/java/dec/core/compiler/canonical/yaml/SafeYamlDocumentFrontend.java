package dec.core.compiler.canonical.yaml;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.canonical.DocumentFrontend;
import dec.core.compiler.canonical.FrontendOptions;
import dec.core.compiler.canonical.FrontendResult;
import dec.core.compiler.canonical.FrontendResults;
import dec.core.compiler.source.DocumentSource;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.util.Collections;
import java.util.Objects;

/**
 * 将不可信 YAML 文档转换为 compiler-owned Canonical 树的安全 Frontend。
 *
 * <p>当前类在 Architecture Skeleton 阶段先冻结公共 API、资源策略和失败不变量；
 * 后续 Development 阶段补齐安全 compose、Canonical 映射和资源门禁。</p>
 */
public final class SafeYamlDocumentFrontend implements DocumentFrontend {
    private static final String PASS = "yaml-frontend";
    private final YamlFrontendLimits limits;

    /**
     * 使用 Design R20 冻结的生产预算创建 YAML Frontend。
     */
    public SafeYamlDocumentFrontend() {
        this(YamlFrontendLimits.production());
    }

    /**
     * 使用显式小型预算创建 Frontend，仅供同包资源 Oracle 使用。
     *
     * @param limits 不可变 YAML 资源预算
     */
    SafeYamlDocumentFrontend(YamlFrontendLimits limits) {
        this.limits = Objects.requireNonNull(limits, "limits");
    }

    /**
     * 当前 Frontend 唯一支持 YAML 文档。
     */
    @Override
    public DocumentFormat format() {
        return DocumentFormat.YAML;
    }

    /**
     * 建立稳定失败边界；Development 阶段将在此边界内部完成安全解析。
     *
     * @param source Provider 返回的不可变 YAML Source
     * @param options 当前 Session 的显式 Frontend 选项
     * @return 当前 Skeleton 的稳定失败结果
     */
    @Override
    public FrontendResult parse(
            DocumentSource source,
            FrontendOptions options) {
        if (source == null) {
            return failed(null, "yaml.frontend.source.required");
        }
        if (options == null) {
            return failed(source, "yaml.frontend.options.required");
        }
        if (source.format() != DocumentFormat.YAML) {
            return failed(source, "yaml.frontend.format.unsupported");
        }
        return failed(source, "yaml.frontend.skeleton.not-implemented");
    }

    /**
     * 创建不携带部分 Canonical root 的稳定 YAML 安全失败结果。
     */
    private static FrontendResult failed(
            DocumentSource source,
            String messageKey) {
        String sourceId = source == null ? "<unknown-yaml-source>" : source.sourceId();
        SourceRef sourceRef = new SourceRef(sourceId, 0, 0, "/");
        Diagnostic diagnostic = new Diagnostic(
                DiagnosticCode.MIX_FRONTEND_YAML_UNSAFE,
                DiagnosticSeverity.ERROR,
                messageKey,
                null,
                sourceRef,
                Collections.<SourceRef>emptyList(),
                "请提供符合 Design R20 安全合同的 YAML 文档",
                PASS);
        return FrontendResults.failed(Collections.singletonList(diagnostic));
    }
}
