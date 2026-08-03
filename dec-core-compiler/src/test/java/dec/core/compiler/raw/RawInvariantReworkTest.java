package dec.core.compiler.raw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.CanonicalDocumentNode;
import dec.core.compiler.canonical.DocumentFormat;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T06 / I002 的 lexical、公开不变量、验证顺序和安全预算 Oracle。
 */
class RawInvariantReworkTest {
    private static final String PASS = "raw-definition-builder";

    /**
     * 六类根和全部 14 Kind 必须保留 owner/name 的原始 lexical token。
     */
    @Test
    void preservesLexicalOwnerAndNameForAllKinds() {
        RawDefinitionSet set = assertBuilt(new RawDefinitionBuilder().build(
                spacedDocuments()));
        Map<RawDefinitionKind, RawDefinition> values = byKind(set);

        assertTokens(values.get(RawDefinitionKind.ROOT_CONFIG), null, "  root  ");
        assertTokens(values.get(RawDefinitionKind.DATA_SOURCE),
                "  root  ", "  dataSource  ");
        assertTokens(values.get(RawDefinitionKind.CONNECTION),
                "  root  ", "  connection  ");
        assertTokens(values.get(RawDefinitionKind.DATA), null, "  data  ");
        assertTokens(values.get(RawDefinitionKind.VIEW), null, "  view  ");
        assertTokens(values.get(RawDefinitionKind.SYSTEM), null, "  system  ");
        assertTokens(values.get(RawDefinitionKind.INFORMATION),
                "  system  ", "  information  ");
        assertTokens(values.get(RawDefinitionKind.MODEL_ACCESS),
                "  system  ", "  model  ");
        assertTokens(values.get(RawDefinitionKind.RULE_VIEW),
                "  system  ", "  ruleView  ");
        assertTokens(values.get(RawDefinitionKind.RULE),
                "  system  /  ruleView  ", "  rule  ");
        assertTokens(values.get(RawDefinitionKind.BUSINESS_SCOPE),
                null, "  business  ");
        assertTokens(values.get(RawDefinitionKind.DIRECTORY),
                "  business  ", "  directory  ");
        assertTokens(values.get(RawDefinitionKind.ACTION),
                "  directory  ", "  action  ");
        assertTokens(values.get(RawDefinitionKind.PRODUCE),
                "  directory  /  action  ", "  produce  ");
    }

    /**
     * name、attributes、body 和 reference target 必须保持同一 lexical 来源事实。
     */
    @Test
    void keepsDefinitionBodyAttributesAndReferencesLexicallyConsistent() {
        RawDefinitionSet set = assertBuilt(new RawDefinitionBuilder().build(
                spacedDocuments()));
        RawDefinition dataSource = set.byKind(RawDefinitionKind.DATA_SOURCE).get(0);
        assertEquals("  dataSource  ", dataSource.name().get());
        assertEquals("  dataSource  ", dataSource.attributes().get("name"));
        assertEquals("  dataSource  ", dataSource.body().attributes().get("name"));

        RawDefinition connection = set.byKind(RawDefinitionKind.CONNECTION).get(0);
        assertEquals("  targetData  ", connection.references().get(0).target());
        RawDefinition information = set.byKind(RawDefinitionKind.INFORMATION).get(0);
        assertEquals("  targetView  ", information.references().get(0).target());
        RawDefinition ruleView = set.byKind(RawDefinitionKind.RULE_VIEW).get(0);
        assertEquals("  targetRuleView  ", ruleView.references().get(0).target());
        RawDefinition produce = set.byKind(RawDefinitionKind.PRODUCE).get(0);
        assertEquals("  produce  ", produce.references().get(0).target());
    }

    /**
     * public RawDefinition 构造器必须接受且只接受 R24 的 14 Kind 矩阵。
     */
    @Test
    void enforcesPublicKindOwnerNameMatrix() {
        for (RawDefinitionKind kind : RawDefinitionKind.values()) {
            Optional<String> owner = ownerRequired(kind)
                    ? Optional.of(" owner ")
                    : Optional.<String>empty();
            Optional<String> name = nameRequired(kind)
                    ? Optional.of(" name ")
                    : Optional.<String>empty();
            RawDefinition valid = definition(kind, owner, name,
                    Collections.singletonMap("key", "value"),
                    Collections.<RawReference>emptyList(),
                    body("body", "value"),
                    DocumentFormat.XML,
                    "1.0");
            assertEquals(owner, valid.ownerToken());
            assertEquals(name, valid.name());

            Optional<String> wrongOwner = ownerRequired(kind)
                    ? Optional.<String>empty()
                    : Optional.of("unexpected");
            assertThrows(IllegalArgumentException.class,
                    () -> definition(kind, wrongOwner, name,
                            Collections.<String, String>emptyMap(),
                            Collections.<RawReference>emptyList(),
                            body("body", "value"),
                            DocumentFormat.XML,
                            "1.0"));

            if (nameRequired(kind)) {
                assertThrows(IllegalArgumentException.class,
                        () -> definition(kind, owner, Optional.<String>empty(),
                                Collections.<String, String>emptyMap(),
                                Collections.<RawReference>emptyList(),
                                body("body", "value"),
                                DocumentFormat.XML,
                                "1.0"));
            } else {
                assertTrue(definition(kind, owner, Optional.of(" optional "),
                        Collections.<String, String>emptyMap(),
                        Collections.<RawReference>emptyList(),
                        body("body", "value"),
                        DocumentFormat.XML,
                        "1.0").name().isPresent());
            }
        }
    }

    /**
     * FAILED public factory 必须拒绝错误 code、severity 和 pass。
     */
    @Test
    void enforcesFailedDiagnosticContract() {
        Diagnostic valid = diagnostic(
                DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                DiagnosticSeverity.ERROR,
                PASS);
        assertEquals(RawBuildStatus.FAILED,
                RawBuildResult.failed(Collections.singletonList(valid)).status());

        assertThrows(IllegalArgumentException.class, () -> RawBuildResult.failed(
                Collections.singletonList(diagnostic(
                        DiagnosticCode.MIX_REF_UNKNOWN,
                        DiagnosticSeverity.ERROR,
                        PASS))));
        assertThrows(IllegalArgumentException.class, () -> RawBuildResult.failed(
                Collections.singletonList(diagnostic(
                        DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                        DiagnosticSeverity.WARNING,
                        PASS))));
        assertThrows(IllegalArgumentException.class, () -> RawBuildResult.failed(
                Collections.singletonList(diagnostic(
                        DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                        DiagnosticSeverity.ERROR,
                        "wrong-pass"))));
    }

    /**
     * 空白 reference 必须在第一阶段以声明节点的精确 SourceRef 失败。
     */
    @Test
    void validatesBlankReferenceBeforeExtractionWithExactSourceRef() {
        CanonicalDocumentNode property = node(
                "data.xml",
                "/orm-data-mapping/data/property-info/property",
                "property",
                attrs("name", "id", "ref-property", "   "));
        CanonicalDocumentNode document = node(
                "data.xml", "/orm-data-mapping", "orm-data-mapping", attrs(),
                node("data.xml", "/orm-data-mapping/data", "data",
                        attrs("name", "data"),
                        node("data.xml", "/orm-data-mapping/data/property-info",
                                "property-info", attrs(), property)));

        RawBuildResult result = new RawDefinitionBuilder().build(
                Collections.singletonList(document));
        assertFailed(result, "raw.reference.target.required", property.sourceRef());
    }

    /**
     * Builder 自身深度预算必须在小型边界内成功并在下一层精确失败。
     */
    @Test
    void enforcesInjectedDepthLimitWithoutStackOverflowProbe() {
        CanonicalDocumentNode document = viewDocumentWithPropertyDepth(2);
        assertEquals(RawBuildStatus.BUILT,
                builderWithLimits(5, 16).build(Collections.singletonList(document)).status());
        RawBuildResult failed = builderWithLimits(4, 16).build(
                Collections.singletonList(document));
        assertFailed(failed, "raw.limit.depth",
                document.children().get(0).children().get(0)
                        .children().get(0).children().get(0).sourceRef());
    }

    /**
     * Builder 自身节点预算必须在创建 Raw 对象前受控失败。
     */
    @Test
    void enforcesInjectedNodeCountLimit() {
        CanonicalDocumentNode document = node(
                "data.xml", "/orm-data-mapping", "orm-data-mapping", attrs(),
                node("data.xml", "/orm-data-mapping/data", "data",
                        attrs("name", "data")));
        assertEquals(RawBuildStatus.BUILT,
                builderWithLimits(4, 2).build(Collections.singletonList(document)).status());
        RawBuildResult failed = builderWithLimits(4, 1).build(
                Collections.singletonList(document));
        assertFailed(failed, "raw.limit.node-count", document.children().get(0).sourceRef());
    }

    /**
     * toString 必须覆盖所有 equals/hashCode 语义字段并表现差异。
     */
    @Test
    void rawDefinitionToStringCoversAllSemanticFields() {
        RawDefinition first = definition(
                RawDefinitionKind.DATA,
                Optional.<String>empty(),
                Optional.of("data"),
                Collections.singletonMap("a", "1"),
                Collections.singletonList(new RawReference(
                        "@ref", "one", ref("a.xml", "/data"))),
                body("body", "one"),
                DocumentFormat.XML,
                "1.0");
        RawDefinition second = definition(
                RawDefinitionKind.DATA,
                Optional.<String>empty(),
                Optional.of("data"),
                Collections.singletonMap("a", "2"),
                Collections.singletonList(new RawReference(
                        "@ref", "two", ref("a.xml", "/data"))),
                body("body", "two"),
                DocumentFormat.YAML,
                "2.0");

        assertFalse(first.equals(second));
        assertFalse(first.toString().equals(second.toString()));
        for (String field : Arrays.asList(
                "kind=", "sourceOrdinal=", "sourceRef=", "ownerToken=", "name=",
                "attributes=", "references=", "body=", "format=", "schemaVersion=")) {
            assertTrue(first.toString().contains(field), field);
        }
    }

    /**
     * 创建覆盖全部定义 Kind 和主要 reference scope 的带空白 Canonical 文档。
     */
    private static List<CanonicalDocumentNode> spacedDocuments() {
        CanonicalDocumentNode root = node(
                "root.xml", "/orm-config", "orm-config", attrs("name", "  root  "),
                node("root.xml", "/orm-config/orm-datasource-info",
                        "orm-datasource-info", attrs(),
                        node("root.xml",
                                "/orm-config/orm-datasource-info/orm-datasource",
                                "orm-datasource", attrs("name", "  dataSource  "))),
                node("root.xml", "/orm-config/orm-connection-info",
                        "orm-connection-info", attrs(),
                        node("root.xml",
                                "/orm-config/orm-connection-info/orm-connection",
                                "orm-connection", attrs("name", "  connection  "),
                                node("root.xml",
                                        "/orm-config/orm-connection-info/orm-connection/data-source-info",
                                        "data-source-info", attrs(),
                                        node("root.xml",
                                                "/orm-config/orm-connection-info/orm-connection/data-source-info/data-source",
                                                "data-source", attrs("ref", "  targetData  "))))));

        CanonicalDocumentNode data = node(
                "data.xml", "/orm-data-mapping", "orm-data-mapping", attrs(),
                node("data.xml", "/orm-data-mapping/data", "data",
                        attrs("name", "  data  "),
                        node("data.xml", "/orm-data-mapping/data/property-info",
                                "property-info", attrs(),
                                node("data.xml",
                                        "/orm-data-mapping/data/property-info/property",
                                        "property", attrs("name", "id",
                                                "ref-property", "  childTarget  ")))));

        CanonicalDocumentNode view = node(
                "view.xml", "/orm-view-mapping", "orm-view-mapping", attrs(),
                node("view.xml", "/orm-view-mapping/view", "view",
                        attrs("name", "  view  "),
                        node("view.xml", "/orm-view-mapping/view/property-info",
                                "property-info", attrs(),
                                node("view.xml",
                                        "/orm-view-mapping/view/property-info/property",
                                        "property", attrs("name", "id")))));

        CanonicalDocumentNode systems = node(
                "systems.xml", "/systems", "systems", attrs(),
                node("systems.xml", "/systems/system", "system",
                        attrs("name", "  system  "),
                        node("systems.xml", "/systems/system/information-info",
                                "information-info", attrs(),
                                node("systems.xml",
                                        "/systems/system/information-info/information",
                                        "information", attrs("name", "  information  ",
                                                "view-ref", "  targetView  "))),
                        node("systems.xml", "/systems/system/model-access-info",
                                "model-access-info", attrs(),
                                node("systems.xml",
                                        "/systems/system/model-access-info/model-access",
                                        "model-access", attrs("model-ref", "  model  ")))));

        CanonicalDocumentNode rules = node(
                "rules.xml", "/orm-rule-mapping", "orm-rule-mapping", attrs(),
                node("rules.xml", "/orm-rule-mapping/rule-view-info",
                        "rule-view-info", attrs("system", "  system  ",
                                "name", "  ruleView  ",
                                "view-ref", "  targetRuleView  "),
                        node("rules.xml", "/orm-rule-mapping/rule-view-info/rule",
                                "rule", attrs("name", "  rule  "))));

        CanonicalDocumentNode business = node(
                "business.xml", "/business-config", "business-config",
                attrs("name", "  business  "),
                node("business.xml", "/business-config/directory-info",
                        "directory-info", attrs(),
                        node("business.xml", "/business-config/directory-info/directory",
                                "directory", attrs("name", "  directory  "),
                                node("business.xml",
                                        "/business-config/directory-info/directory/action-info",
                                        "action-info", attrs(),
                                        node("business.xml",
                                                "/business-config/directory-info/directory/action-info/action",
                                                "action", attrs("name", "  action  "),
                                                node("business.xml",
                                                        "/business-config/directory-info/directory/action-info/action/produce-info",
                                                        "produce-info", attrs(),
                                                        node("business.xml",
                                                                "/business-config/directory-info/directory/action-info/action/produce-info/produce",
                                                                "produce", attrs("ref", "  produce  "))))))));
        return Arrays.asList(root, data, view, systems, rules, business);
    }

    /** 创建合法 view property 递归树，depth 表示 property 嵌套层数。 */
    private static CanonicalDocumentNode viewDocumentWithPropertyDepth(int depth) {
        CanonicalDocumentNode property = node(
                "view.xml",
                "/orm-view-mapping/view/property-info/property",
                "property", attrs("name", "p0"));
        for (int index = 1; index < depth; index++) {
            property = node(
                    "view.xml",
                    "/orm-view-mapping/view/property-info/property/property" + index,
                    "property", attrs("name", "p" + index), property);
        }
        return node(
                "view.xml", "/orm-view-mapping", "orm-view-mapping", attrs(),
                node("view.xml", "/orm-view-mapping/view", "view",
                        attrs("name", "view"),
                        node("view.xml", "/orm-view-mapping/view/property-info",
                                "property-info", attrs(), property)));
    }

    /** 通过反射加载 package-private limits 和测试构造器。 */
    private static RawDefinitionBuilder builderWithLimits(int depth, int nodes) {
        try {
            Class<?> limitsType = Class.forName(
                    "dec.core.compiler.raw.RawBuilderLimits");
            Constructor<?> limitsConstructor = limitsType.getDeclaredConstructor(
                    int.class, int.class);
            limitsConstructor.setAccessible(true);
            Object limits = limitsConstructor.newInstance(depth, nodes);
            Constructor<RawDefinitionBuilder> builderConstructor =
                    RawDefinitionBuilder.class.getDeclaredConstructor(limitsType);
            builderConstructor.setAccessible(true);
            return builderConstructor.newInstance(limits);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("RawBuilderLimits injectable seam must exist", failure);
        }
    }

    private static void assertTokens(
            RawDefinition definition,
            String owner,
            String name) {
        assertEquals(owner == null ? Optional.<String>empty() : Optional.of(owner),
                definition.ownerToken());
        assertEquals(name == null ? Optional.<String>empty() : Optional.of(name),
                definition.name());
    }

    private static Map<RawDefinitionKind, RawDefinition> byKind(RawDefinitionSet set) {
        Map<RawDefinitionKind, RawDefinition> result =
                new EnumMap<RawDefinitionKind, RawDefinition>(RawDefinitionKind.class);
        for (RawDefinition definition : set.definitions()) {
            result.put(definition.kind(), definition);
        }
        return result;
    }

    private static boolean ownerRequired(RawDefinitionKind kind) {
        switch (kind) {
            case DATA_SOURCE:
            case CONNECTION:
            case INFORMATION:
            case MODEL_ACCESS:
            case RULE_VIEW:
            case RULE:
            case DIRECTORY:
            case ACTION:
            case PRODUCE:
                return true;
            default:
                return false;
        }
    }

    private static boolean nameRequired(RawDefinitionKind kind) {
        return kind != RawDefinitionKind.PRODUCE;
    }

    private static RawDefinition definition(
            RawDefinitionKind kind,
            Optional<String> owner,
            Optional<String> name,
            Map<String, String> attributes,
            List<RawReference> references,
            RawNodeBody body,
            DocumentFormat format,
            String schemaVersion) {
        return new RawDefinition(
                kind,
                0L,
                ref("definition.xml", "/definition"),
                owner,
                name,
                attributes,
                references,
                body,
                format,
                schemaVersion);
    }

    private static RawNodeBody body(String name, String value) {
        return new RawNodeBody(
                name,
                Collections.singletonMap("value", value),
                Optional.of(value),
                Collections.<RawNodeBody>emptyList(),
                ref("body.xml", "/body"));
    }

    private static Diagnostic diagnostic(
            DiagnosticCode code,
            DiagnosticSeverity severity,
            String pass) {
        return new Diagnostic(
                code,
                severity,
                "raw.test",
                null,
                ref("diagnostic.xml", "/diagnostic"),
                Collections.<SourceRef>emptyList(),
                "fix",
                pass);
    }

    private static RawDefinitionSet assertBuilt(RawBuildResult result) {
        assertEquals(RawBuildStatus.BUILT, result.status());
        assertTrue(result.rawDefinitionSet().isPresent());
        assertTrue(result.diagnostics().isEmpty());
        return result.rawDefinitionSet().get();
    }

    private static void assertFailed(
            RawBuildResult result,
            String messageKey,
            SourceRef sourceRef) {
        assertEquals(RawBuildStatus.FAILED, result.status());
        assertFalse(result.rawDefinitionSet().isPresent());
        assertEquals(1, result.diagnostics().size());
        assertEquals(messageKey, result.diagnostics().get(0).messageKey());
        if (sourceRef != null) {
            assertEquals(sourceRef, result.diagnostics().get(0).sourceRef());
        }
        assertEquals(DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                result.diagnostics().get(0).code());
        assertEquals(DiagnosticSeverity.ERROR,
                result.diagnostics().get(0).severity());
        assertEquals(PASS, result.diagnostics().get(0).pass());
    }

    private static CanonicalDocumentNode node(
            String sourceId,
            String path,
            String name,
            Map<String, String> attributes,
            CanonicalDocumentNode... children) {
        return new CanonicalDocumentNode(
                name,
                attributes,
                Optional.<String>empty(),
                Arrays.asList(children),
                ref(sourceId, path),
                DocumentFormat.XML,
                "1.0");
    }

    private static Map<String, String> attrs(String... values) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        for (int index = 0; index < values.length; index += 2) {
            result.put(values[index], values[index + 1]);
        }
        return result;
    }

    private static SourceRef ref(String sourceId, String path) {
        return new SourceRef(sourceId, 1, 1, path);
    }
}
