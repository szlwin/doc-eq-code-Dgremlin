package dec.core.compiler.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.SourceRef;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * 独立 Review 驱动的 canonical reference、身份域分离和声明位置 Oracle。
 */
class SourceReferenceIdentityReworkTest {
    private static final String ROOT = "classpath:mix/orm-config.xml";
    private static final String DATA_ROOT = "classpath:mix/data/";
    private static final String VIEW_ROOT = "classpath:mix/view/";
    private static final String SYSTEMS = "classpath:mix/system/systems.xml";
    private static final String SYSTEMS_WITH_DOT =
            "classpath:mix/system/./systems.xml";
    private static final String BUSINESS =
            "classpath:mix/business/order-business.xml";
    private static final String USER_RULE =
            "classpath:mix/rule/user-rule.xml";
    private static final String ORDER_RULE =
            "classpath:mix/rule/order-rule.xml";
    private static final String PAYMENT_RULE =
            "classpath:mix/rule/payment-rule.xml";
    private static final AllowedRoot ALLOWED_ROOT = new AllowedRoot(
            URI.create("classpath:mix/"));

    @Test
    void canonicalizesOpaqueDotSegmentsBeforeProviderAndGraphConstruction() {
        RecordingProvider provider = provider(rootXml(SYSTEMS_WITH_DOT, ""));

        SourceGraphResolutionResult result = resolve(provider);

        assertEquals(SourceGraphResolutionStatus.RESOLVED, result.status());
        assertTrue(result.graph().isPresent());
        assertEquals(SYSTEMS, new SourceReference(SYSTEMS_WITH_DOT).value());
        assertFalse(provider.accesses().contains(SYSTEMS_WITH_DOT));
        assertTrue(provider.accesses().contains(SYSTEMS));
        assertTrue(result.graph().get().edges().stream()
                .anyMatch(edge -> edge.edgeType() == SourceEdgeType.ROOT_SYSTEM_FILE
                        && SYSTEMS.equals(edge.targetReference().value())));
    }

    @Test
    void treatsEquivalentReferenceTextsAsOneDuplicateDeclaration() {
        String duplicate = "    <system-file path=\""
                + SYSTEMS_WITH_DOT
                + "\"/>\n";
        RecordingProvider provider = provider(rootXml(SYSTEMS, duplicate));

        SourceGraphResolutionResult result = resolve(provider);

        assertFailedWith(result, DiagnosticCode.MIX_SOURCE_POLICY);
        assertTrue(result.diagnostics().stream()
                .anyMatch(diagnostic -> "source.edge.duplicate"
                        .equals(diagnostic.messageKey())));
        assertEquals(1, provider.accesses().size());
    }

    @Test
    void detectsCycleByCanonicalReferenceWhenSourceIdDiffersFromUri() {
        RecordingProvider provider = provider(rootXml(ROOT, ""));
        provider.putSingle(
                ROOT,
                source("root-config", ROOT, rootXml(ROOT, "")));

        SourceGraphResolutionResult result = resolve(provider);

        assertFailedWith(result, DiagnosticCode.MIX_SOURCE_POLICY);
        assertFalse(result.diagnostics().stream()
                .anyMatch(diagnostic -> diagnostic.code()
                        == DiagnosticCode.MIX_SOURCE_DUPLICATE_ID));
        assertTrue(result.diagnostics().stream()
                .anyMatch(diagnostic -> "source.graph.cycle"
                        .equals(diagnostic.messageKey())));
        assertEquals(1L, provider.accesses().stream()
                .filter(ROOT::equals)
                .count());
    }

    @Test
    void reportsExactStartTagLineColumnAndNodePathForAllSevenEdges() {
        String rootXml = rootXml(SYSTEMS, "");
        String systemsXml = systemsXml();
        RecordingProvider provider = provider(rootXml);
        provider.putSingle(SYSTEMS, source(SYSTEMS, SYSTEMS, systemsXml));

        SourceGraphResolutionResult result = resolve(provider);

        assertEquals(SourceGraphResolutionStatus.RESOLVED, result.status());
        MixSourceGraph graph = result.graph().get();
        Map<String, String> sourceTexts = new HashMap<String, String>();
        sourceTexts.put(ROOT, rootXml);
        sourceTexts.put(SYSTEMS, systemsXml);
        for (SourceGraphEdge edge : graph.edges()) {
            String sourceText = sourceTexts.get(edge.fromSourceId());
            assertTrue(sourceText != null, "声明来源必须属于固定 root 或 systems");
            String token = declarationToken(edge);
            Position expected = positionOf(sourceText, token);
            SourceRef actual = edge.declarationSourceRef();
            assertEquals(edge.fromSourceId(), actual.sourceId());
            assertEquals(expected.line, actual.line(), edge.toString());
            assertEquals(expected.column, actual.column(), edge.toString());
            assertEquals(expectedNodePath(edge.edgeType()), actual.nodePath());
        }
        assertEquals(7, graph.edges().size());
    }

    /**
     * 创建包含固定十个 Source 的 Provider，引用键与 Source ID 可独立配置。
     */
    private static RecordingProvider provider(String rootXml) {
        RecordingProvider provider = new RecordingProvider();
        provider.putSingle(ROOT, source(ROOT, ROOT, rootXml));
        provider.putSingle(SYSTEMS, source(SYSTEMS, SYSTEMS, systemsXml()));
        provider.putSingle(BUSINESS, source(BUSINESS, BUSINESS, "<business-scope/>"));
        provider.putSingle(USER_RULE, source(USER_RULE, USER_RULE, "<rule-views/>"));
        provider.putSingle(ORDER_RULE, source(ORDER_RULE, ORDER_RULE, "<rule-views/>"));
        provider.putSingle(
                PAYMENT_RULE,
                source(PAYMENT_RULE, PAYMENT_RULE, "<rule-views/>"));
        provider.putFileSet(DATA_ROOT, Arrays.asList(
                source("data-user", "classpath:mix/data/User.xml", "<orm-data/>"),
                source("data-order", "classpath:mix/data/Order.xml", "<orm-data/>"),
                source("data-pay", "classpath:mix/data/Pay.xml", "<orm-data/>")));
        provider.putFileSet(VIEW_ROOT, Collections.singletonList(
                source("view-main", "classpath:mix/view/orm-view.xml", "<orm-view/>")));
        return provider;
    }

    /**
     * 创建 Source ID 与 URI 可不同的完整文档来源。
     */
    private static DocumentSource source(
            String sourceId,
            String uri,
            String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return new DocumentSource(
                sourceId,
                URI.create(uri),
                DocumentFormat.XML,
                ALLOWED_ROOT,
                bytes,
                "sha256:test-" + sourceId);
    }

    /**
     * 创建可注入等价 system 声明的固定 root XML。
     */
    private static String rootXml(String systemReference, String extraSystem) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<orm-config>\n"
                + "  <orm-data-file-info>\n"
                + "    <orm-file path=\"" + DATA_ROOT + "\"/>\n"
                + "  </orm-data-file-info>\n"
                + "  <orm-view-file-info>\n"
                + "    <orm-file path=\"" + VIEW_ROOT + "\"/>\n"
                + "  </orm-view-file-info>\n"
                + "  <system-file-info>\n"
                + "    <system-file path=\"" + systemReference + "\"/>\n"
                + extraSystem
                + "  </system-file-info>\n"
                + "  <business-file-info>\n"
                + "    <business-file path=\"" + BUSINESS + "\"/>\n"
                + "  </business-file-info>\n"
                + "</orm-config>\n";
    }

    /**
     * 创建三条 rule 声明使用的固定 systems XML。
     */
    private static String systemsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<systems>\n"
                + "  <system name=\"user\">\n"
                + "    <rule-file-info><rule-file path=\"" + USER_RULE
                + "\"/></rule-file-info>\n"
                + "  </system>\n"
                + "  <system name=\"order\">\n"
                + "    <rule-file-info><rule-file path=\"" + ORDER_RULE
                + "\"/></rule-file-info>\n"
                + "  </system>\n"
                + "  <system name=\"payment\">\n"
                + "    <rule-file-info><rule-file path=\"" + PAYMENT_RULE
                + "\"/></rule-file-info>\n"
                + "  </system>\n"
                + "</systems>\n";
    }

    /**
     * 根据边类型和目标生成原始文本中的完整 start-tag 前缀。
     */
    private static String declarationToken(SourceGraphEdge edge) {
        switch (edge.edgeType()) {
            case ROOT_DATA_FILESET:
            case ROOT_VIEW_FILESET:
                return "<orm-file path=\"" + edge.targetReference().value() + "\"";
            case ROOT_SYSTEM_FILE:
                return "<system-file path=\"" + edge.targetReference().value() + "\"";
            case ROOT_BUSINESS_FILE:
                return "<business-file path=\"" + edge.targetReference().value() + "\"";
            case SYSTEM_RULE_FILE:
                return "<rule-file path=\"" + edge.targetReference().value() + "\"";
            default:
                throw new AssertionError("unexpected edge type: " + edge.edgeType());
        }
    }

    /**
     * 返回每类声明冻结的节点路径。
     */
    private static String expectedNodePath(SourceEdgeType edgeType) {
        switch (edgeType) {
            case ROOT_DATA_FILESET:
                return "/orm-config/orm-data-file-info/orm-file";
            case ROOT_VIEW_FILESET:
                return "/orm-config/orm-view-file-info/orm-file";
            case ROOT_SYSTEM_FILE:
                return "/orm-config/system-file-info/system-file";
            case ROOT_BUSINESS_FILE:
                return "/orm-config/business-file-info/business-file";
            case SYSTEM_RULE_FILE:
                return "/systems/system/rule-file-info/rule-file";
            default:
                throw new AssertionError("unexpected edge type: " + edgeType);
        }
    }

    /**
     * 独立按原始文本索引计算 `<` 的 1-based 行和列。
     */
    private static Position positionOf(String source, String token) {
        int offset = source.indexOf(token);
        if (offset < 0) {
            throw new AssertionError("declaration token not found: " + token);
        }
        int line = 1;
        int lineStart = 0;
        for (int index = 0; index < offset; index++) {
            if (source.charAt(index) == '\n') {
                line++;
                lineStart = index + 1;
            }
        }
        return new Position(line, offset - lineStart + 1);
    }

    /**
     * 执行固定根入口解析。
     */
    private static SourceGraphResolutionResult resolve(RecordingProvider provider) {
        return new MixSourceResolver().resolve(
                new SourceReference(ROOT),
                provider,
                policy());
    }

    /**
     * 返回 T03 默认策略。
     */
    private static SourcePolicy policy() {
        return new SourcePolicy(
                Collections.singleton("classpath"),
                ALLOWED_ROOT,
                3,
                20,
                1024L * 1024L);
    }

    /**
     * 断言失败结果包含指定错误且不暴露部分图。
     */
    private static void assertFailedWith(
            SourceGraphResolutionResult result,
            DiagnosticCode code) {
        assertEquals(SourceGraphResolutionStatus.FAILED, result.status());
        assertFalse(result.graph().isPresent());
        assertTrue(result.diagnostics().stream()
                .anyMatch(diagnostic -> diagnostic.code() == code));
    }

    /**
     * 原始文本位置值对象。
     */
    private static final class Position {
        private final int line;
        private final int column;

        private Position(int line, int column) {
            this.line = line;
            this.column = column;
        }
    }

    /**
     * 只按 canonical reference key 查找 Source 的内存 Provider。
     */
    private static final class RecordingProvider implements DocumentSourceProvider {
        private final Map<String, DocumentSource> singles =
                new LinkedHashMap<String, DocumentSource>();
        private final Map<String, List<DocumentSource>> fileSets =
                new LinkedHashMap<String, List<DocumentSource>>();
        private final List<String> accesses = new ArrayList<String>();

        private void putSingle(String reference, DocumentSource source) {
            singles.put(reference, source);
        }

        private void putFileSet(String reference, List<DocumentSource> sources) {
            fileSets.put(reference, new ArrayList<DocumentSource>(sources));
        }

        private List<String> accesses() {
            return Collections.unmodifiableList(accesses);
        }

        @Override
        public SourceResolutionResult resolve(
                SourceReference reference,
                SourceResolutionContext context) {
            accesses.add(reference.value());
            DocumentSource source = singles.get(reference.value());
            if (source == null) {
                return SourceResolutionResults.failed(Collections.singletonList(
                        notFound(reference)));
            }
            return SourceResolutionResults.resolvedSingle(
                    source,
                    Collections.<Diagnostic>emptyList());
        }

        @Override
        public SourceResolutionResult resolveFileSet(
                SourceReference reference,
                SourceResolutionContext context) {
            accesses.add(reference.value());
            List<DocumentSource> sources = fileSets.get(reference.value());
            if (sources == null) {
                return SourceResolutionResults.failed(Collections.singletonList(
                        notFound(reference)));
            }
            return SourceResolutionResults.resolvedFileSet(
                    sources,
                    Collections.<Diagnostic>emptyList());
        }

        /**
         * 创建稳定缺失 Source Diagnostic。
         */
        private static Diagnostic notFound(SourceReference reference) {
            return SourceTestFixture.diagnostic(
                    DiagnosticCode.MIX_SOURCE_NOT_FOUND,
                    reference.value(),
                    "source.not.found");
        }
    }
}
