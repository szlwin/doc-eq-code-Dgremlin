package dec.core.compiler.source;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * T03 SourceGraph 测试使用的固定内存 Provider 与 mix fixture。
 */
final class SourceTestFixture {
    static final String ROOT = "classpath:mix/orm-config.xml";
    static final String DATA_ROOT = "classpath:mix/data/";
    static final String VIEW_ROOT = "classpath:mix/view/";
    static final String SYSTEMS = "classpath:mix/system/systems.xml";
    static final String BUSINESS = "classpath:mix/business/order-business.xml";
    static final String USER_RULE = "classpath:mix/rule/user-rule.xml";
    static final String ORDER_RULE = "classpath:mix/rule/order-rule.xml";
    static final String PAYMENT_RULE = "classpath:mix/rule/payment-rule.xml";

    private static final AllowedRoot ALLOWED_ROOT = new AllowedRoot(
            URI.create("classpath:mix/"));

    private SourceTestFixture() {
        throw new AssertionError("No instances");
    }

    /**
     * 返回 T03 默认安全策略。
     */
    static SourcePolicy policy() {
        return new SourcePolicy(
                Collections.singleton("classpath"),
                ALLOWED_ROOT,
                3,
                20,
                1024L * 1024L);
    }

    /**
     * 返回固定 Source ID 集合；集合顺序不作为实现排序的来源。
     */
    static Set<String> expectedSourceIds() {
        return new LinkedHashSet<String>(Arrays.asList(
                ROOT,
                "classpath:mix/data/User.xml",
                "classpath:mix/data/Order.xml",
                "classpath:mix/data/Pay.xml",
                "classpath:mix/view/orm-view.xml",
                SYSTEMS,
                USER_RULE,
                ORDER_RULE,
                PAYMENT_RULE,
                BUSINESS));
    }

    /**
     * 返回固定声明边的稳定文本键。
     */
    static Set<String> expectedEdgeKeys() {
        return new LinkedHashSet<String>(Arrays.asList(
                key(SourceEdgeType.ROOT_DATA_FILESET, ROOT, DATA_ROOT),
                key(SourceEdgeType.ROOT_VIEW_FILESET, ROOT, VIEW_ROOT),
                key(SourceEdgeType.ROOT_SYSTEM_FILE, ROOT, SYSTEMS),
                key(SourceEdgeType.ROOT_BUSINESS_FILE, ROOT, BUSINESS),
                key(SourceEdgeType.SYSTEM_RULE_FILE, SYSTEMS, USER_RULE),
                key(SourceEdgeType.SYSTEM_RULE_FILE, SYSTEMS, ORDER_RULE),
                key(SourceEdgeType.SYSTEM_RULE_FILE, SYSTEMS, PAYMENT_RULE)));
    }

    /**
     * 创建包含固定十个 Source 的内存 Provider。
     */
    static InMemoryProvider provider(FileSetOrder order) {
        InMemoryProvider provider = new InMemoryProvider(order);
        provider.putSingle(source(ROOT, rootXml()));
        provider.putSingle(source(SYSTEMS, systemsXml()));
        provider.putSingle(source(BUSINESS, "<business-scope/>"));
        provider.putSingle(source(USER_RULE, "<rule-views/>"));
        provider.putSingle(source(ORDER_RULE, "<rule-views/>"));
        provider.putSingle(source(PAYMENT_RULE, "<rule-views/>"));
        provider.putFileSet(DATA_ROOT, Arrays.asList(
                source("classpath:mix/data/User.xml", "<orm-data name=\"user\"/>"),
                source("classpath:mix/data/Order.xml", "<orm-data name=\"order\"/>"),
                source("classpath:mix/data/Pay.xml", "<orm-data name=\"pay\"/>")));
        provider.putFileSet(VIEW_ROOT, Collections.singletonList(
                source("classpath:mix/view/orm-view.xml", "<orm-view-info/>")));
        return provider;
    }

    /**
     * 创建完整 DocumentSource，并以内容字节计算稳定摘要。
     */
    static DocumentSource source(String sourceId, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return new DocumentSource(
                sourceId,
                URI.create(sourceId),
                DocumentFormat.XML,
                ALLOWED_ROOT,
                bytes,
                "sha256:" + sha256(bytes));
    }

    /**
     * 创建第三方自定义 SourceResolutionResult，用于非法结果测试。
     */
    static SourceResolutionResult customResult(
            final SourceResolutionStatus status,
            final List<DocumentSource> sources,
            final List<Diagnostic> diagnostics) {
        return new SourceResolutionResult() {
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
        };
    }

    /**
     * 创建稳定 Source 失败 Diagnostic。
     */
    static Diagnostic diagnostic(
            DiagnosticCode code,
            String sourceId,
            String messageKey) {
        return new Diagnostic(
                code,
                DiagnosticSeverity.ERROR,
                messageKey,
                null,
                new SourceRef(sourceId, 1, 1, "/source"),
                Collections.<SourceRef>emptyList(),
                "检查 Source 声明和 Provider 配置",
                "SourceDiscoveryPass");
    }

    /**
     * 转换边为不包含物理位置的稳定比较键。
     */
    static String key(SourceGraphEdge edge) {
        return key(
                edge.edgeType(),
                edge.fromSourceId(),
                edge.targetReference().value());
    }

    private static String key(
            SourceEdgeType edgeType,
            String from,
            String target) {
        return edgeType + "|" + from + "|" + target;
    }

    /**
     * 固定 root 声明 fixture。
     */
    private static String rootXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<orm-config name=\"mix\">\n"
                + "  <orm-data-file-info>\n"
                + "    <orm-file path=\"" + DATA_ROOT + "\"/>\n"
                + "  </orm-data-file-info>\n"
                + "  <orm-view-file-info>\n"
                + "    <orm-file path=\"" + VIEW_ROOT + "\"/>\n"
                + "  </orm-view-file-info>\n"
                + "  <system-file-info>\n"
                + "    <system-file path=\"" + SYSTEMS + "\"/>\n"
                + "  </system-file-info>\n"
                + "  <business-file-info>\n"
                + "    <business-file path=\"" + BUSINESS + "\"/>\n"
                + "  </business-file-info>\n"
                + "</orm-config>\n";
    }

    /**
     * 固定 system → rule 声明 fixture。
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
     * 计算 Java 8 环境中的十六进制 SHA-256。
     */
    private static String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] value = digest.digest(bytes);
            StringBuilder result = new StringBuilder(value.length * 2);
            for (byte current : value) {
                result.append(String.format("%02x", current & 0xff));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new AssertionError("SHA-256 must exist", impossible);
        }
    }

    enum FileSetOrder {
        FORWARD,
        REVERSED,
        SHUFFLED
    }

    /**
     * 可控制枚举顺序、异常和非法 typed result 的内存 Provider。
     */
    static final class InMemoryProvider implements DocumentSourceProvider {
        private final Map<String, SourceResolutionResult> singles =
                new HashMap<String, SourceResolutionResult>();
        private final Map<String, List<DocumentSource>> fileSets =
                new HashMap<String, List<DocumentSource>>();
        private final Set<String> throwingReferences =
                new LinkedHashSet<String>();
        private final FileSetOrder order;
        private int accessCount;

        InMemoryProvider(FileSetOrder order) {
            this.order = order;
        }

        void putSingle(DocumentSource source) {
            singles.put(
                    source.sourceId(),
                    SourceResolutionResults.resolvedSingle(
                            source,
                            Collections.<Diagnostic>emptyList()));
        }

        void putSingleResult(String reference, SourceResolutionResult result) {
            singles.put(reference, result);
        }

        void removeSingle(String reference) {
            singles.remove(reference);
        }

        void putFileSet(String reference, List<DocumentSource> sources) {
            fileSets.put(reference, new ArrayList<DocumentSource>(sources));
        }

        void throwOn(String reference) {
            throwingReferences.add(reference);
        }

        int accessCount() {
            return accessCount;
        }

        @Override
        public SourceResolutionResult resolve(
                SourceReference reference,
                SourceResolutionContext context) {
            accessCount++;
            if (throwingReferences.contains(reference.value())) {
                throw new IllegalStateException("provider failure");
            }
            SourceResolutionResult result = singles.get(reference.value());
            if (result != null) {
                return result;
            }
            return SourceResolutionResults.failed(Collections.singletonList(
                    diagnostic(
                            DiagnosticCode.MIX_SOURCE_NOT_FOUND,
                            reference.value(),
                            "source.not.found")));
        }

        @Override
        public SourceResolutionResult resolveFileSet(
                SourceReference reference,
                SourceResolutionContext context) {
            accessCount++;
            if (throwingReferences.contains(reference.value())) {
                throw new IllegalStateException("provider failure");
            }
            List<DocumentSource> configured = fileSets.get(reference.value());
            if (configured == null) {
                return SourceResolutionResults.failed(Collections.singletonList(
                        diagnostic(
                                DiagnosticCode.MIX_SOURCE_NOT_FOUND,
                                reference.value(),
                                "source.fileset.not.found")));
            }
            List<DocumentSource> output = new ArrayList<DocumentSource>(configured);
            if (order == FileSetOrder.REVERSED) {
                Collections.reverse(output);
            } else if (order == FileSetOrder.SHUFFLED) {
                Collections.shuffle(output, new Random(7L));
            }
            return customResult(
                    SourceResolutionStatus.RESOLVED,
                    output,
                    Collections.<Diagnostic>emptyList());
        }
    }
}
