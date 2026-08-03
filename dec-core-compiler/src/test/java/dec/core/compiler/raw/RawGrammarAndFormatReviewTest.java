package dec.core.compiler.raw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.CanonicalDocumentNode;
import dec.core.compiler.canonical.DocumentFormat;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.SourceRef;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * T06 独立 Review 对完整 Grammar edge 和格式中立行为的补充 Oracle。
 */
class RawGrammarAndFormatReviewTest {

    /**
     * R23 六类文档根的每条合法父子边均必须被严格白名单接受。
     */
    @Test
    void acceptsEveryFrozenGrammarEdge() {
        RawBuildResult result = new RawDefinitionBuilder().build(fullGrammarDocuments());

        assertEquals(RawBuildStatus.BUILT, result.status());
        assertTrue(result.rawDefinitionSet().isPresent());
        assertTrue(result.diagnostics().isEmpty());
        assertEquals(15, result.rawDefinitionSet().get().size());
        assertEquals(2,
                result.rawDefinitionSet().get()
                        .definitions(RawDefinitionKind.ACTION)
                        .size());
    }

    /**
     * 合法节点名放在错误父节点下仍必须 fail closed，不能只按全局名称接受。
     */
    @Test
    void rejectsGrammarNodeRelocatedToWrongParent() {
        CanonicalDocumentNode misplaced = node(
                DocumentFormat.XML,
                "systems.xml",
                "/systems/system/information",
                "information",
                attrs("name", "misplaced"),
                null);
        CanonicalDocumentNode document = node(
                DocumentFormat.XML,
                "systems.xml",
                "/systems",
                "systems",
                attrs(),
                null,
                node(DocumentFormat.XML,
                        "systems.xml",
                        "/systems/system",
                        "system",
                        attrs("name", "user"),
                        null,
                        misplaced));

        RawBuildResult result = new RawDefinitionBuilder().build(
                Collections.singletonList(document));

        assertEquals(RawBuildStatus.FAILED, result.status());
        assertFalse(result.rawDefinitionSet().isPresent());
        assertEquals(DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                result.diagnostics().get(0).code());
        assertEquals("raw.structure.unknown",
                result.diagnostics().get(0).messageKey());
        assertEquals(misplaced.sourceRef(),
                result.diagnostics().get(0).sourceRef());
    }

    /**
     * 语义相同的 XML/YAML Canonical 输入除 format 来源事实外必须产生相同 Raw 事实。
     */
    @Test
    void preservesEquivalentRawSemanticsAcrossCanonicalFormats() {
        RawDefinition xml = buildSingleData(DocumentFormat.XML);
        RawDefinition yaml = buildSingleData(DocumentFormat.YAML);

        assertEquals(xml.kind(), yaml.kind());
        assertEquals(xml.sourceOrdinal(), yaml.sourceOrdinal());
        assertEquals(xml.sourceRef(), yaml.sourceRef());
        assertEquals(xml.ownerToken(), yaml.ownerToken());
        assertEquals(xml.name(), yaml.name());
        assertEquals(xml.attributes(), yaml.attributes());
        assertEquals(xml.references(), yaml.references());
        assertEquals(xml.body(), yaml.body());
        assertEquals(xml.schemaVersion(), yaml.schemaVersion());
        assertEquals(DocumentFormat.XML, xml.format());
        assertEquals(DocumentFormat.YAML, yaml.format());
    }

    /**
     * 构造覆盖六类根全部冻结 Grammar edge 的 Canonical 文档集合。
     */
    private static List<CanonicalDocumentNode> fullGrammarDocuments() {
        CanonicalDocumentNode rootConfig = node(
                DocumentFormat.XML,
                "root.xml",
                "/orm-config",
                "orm-config",
                attrs("name", "mix"),
                null,
                container("root.xml", "/orm-config/orm-datasource-info",
                        "orm-datasource-info",
                        node(DocumentFormat.XML, "root.xml",
                                "/orm-config/orm-datasource-info/orm-datasource",
                                "orm-datasource", attrs("name", "ds"), null,
                                scalar("root.xml",
                                        "/orm-config/orm-datasource-info/orm-datasource/name",
                                        "name", "MySQL"))),
                container("root.xml", "/orm-config/orm-data-file-info",
                        "orm-data-file-info",
                        leaf("root.xml",
                                "/orm-config/orm-data-file-info/orm-file",
                                "orm-file", attrs("path", "data.xml"))),
                container("root.xml", "/orm-config/orm-view-file-info",
                        "orm-view-file-info",
                        leaf("root.xml",
                                "/orm-config/orm-view-file-info/orm-file",
                                "orm-file", attrs("path", "view.xml"))),
                container("root.xml", "/orm-config/system-file-info",
                        "system-file-info",
                        leaf("root.xml",
                                "/orm-config/system-file-info/system-file",
                                "system-file", attrs("path", "systems.xml"))),
                container("root.xml", "/orm-config/business-file-info",
                        "business-file-info",
                        leaf("root.xml",
                                "/orm-config/business-file-info/business-file",
                                "business-file", attrs("path", "business.xml"))),
                container("root.xml", "/orm-config/orm-connection-info",
                        "orm-connection-info",
                        node(DocumentFormat.XML, "root.xml",
                                "/orm-config/orm-connection-info/orm-connection",
                                "orm-connection", attrs("name", "con"), null,
                                container("root.xml",
                                        "/orm-config/orm-connection-info/orm-connection/data-source-info",
                                        "data-source-info",
                                        leaf("root.xml",
                                                "/orm-config/orm-connection-info/orm-connection/data-source-info/data-source",
                                                "data-source", attrs("ref", "ds"))))));

        CanonicalDocumentNode data = node(
                DocumentFormat.XML,
                "data.xml",
                "/orm-data-mapping",
                "orm-data-mapping",
                attrs(),
                null,
                node(DocumentFormat.XML, "data.xml",
                        "/orm-data-mapping/data",
                        "data", attrs("name", "user"), null,
                        container("data.xml",
                                "/orm-data-mapping/data/property-info",
                                "property-info",
                                leaf("data.xml",
                                        "/orm-data-mapping/data/property-info/property",
                                        "property", attrs("name", "id"))),
                        container("data.xml",
                                "/orm-data-mapping/data/table-info",
                                "table-info",
                                node(DocumentFormat.XML, "data.xml",
                                        "/orm-data-mapping/data/table-info/table",
                                        "table", attrs("name", "users"), null,
                                        leaf("data.xml",
                                                "/orm-data-mapping/data/table-info/table/column",
                                                "column", attrs("name", "id"))))));

        CanonicalDocumentNode view = node(
                DocumentFormat.XML,
                "view.xml",
                "/orm-view-mapping",
                "orm-view-mapping",
                attrs(),
                null,
                node(DocumentFormat.XML, "view.xml",
                        "/orm-view-mapping/view",
                        "view", attrs("name", "UserInfo"), null,
                        container("view.xml",
                                "/orm-view-mapping/view/property-info",
                                "property-info",
                                node(DocumentFormat.XML, "view.xml",
                                        "/orm-view-mapping/view/property-info/property",
                                        "property", attrs("name", "user"), null,
                                        leaf("view.xml",
                                                "/orm-view-mapping/view/property-info/property/property",
                                                "property", attrs("name", "id"))))));

        CanonicalDocumentNode systems = node(
                DocumentFormat.XML,
                "systems.xml",
                "/systems",
                "systems",
                attrs(),
                null,
                node(DocumentFormat.XML, "systems.xml",
                        "/systems/system",
                        "system", attrs("name", "user"), null,
                        container("systems.xml", "/systems/system/data-info",
                                "data-info",
                                leaf("systems.xml", "/systems/system/data-info/data-ref",
                                        "data-ref", attrs("ref", "user"))),
                        container("systems.xml", "/systems/system/view-info",
                                "view-info",
                                leaf("systems.xml", "/systems/system/view-info/view-ref",
                                        "view-ref", attrs("ref", "UserInfo"))),
                        container("systems.xml", "/systems/system/rule-file-info",
                                "rule-file-info",
                                leaf("systems.xml",
                                        "/systems/system/rule-file-info/rule-file",
                                        "rule-file", attrs("path", "rule.xml"))),
                        container("systems.xml", "/systems/system/information-info",
                                "information-info",
                                node(DocumentFormat.XML, "systems.xml",
                                        "/systems/system/information-info/information",
                                        "information",
                                        attrs("name", "active", "view-ref", "UserInfo"),
                                        null,
                                        leaf("systems.xml",
                                                "/systems/system/information-info/information/change-data",
                                                "change-data", attrs("property", "status")))),
                        container("systems.xml", "/systems/system/model-access-info",
                                "model-access-info",
                                node(DocumentFormat.XML, "systems.xml",
                                        "/systems/system/model-access-info/model-access",
                                        "model-access", attrs("model-ref", "Model"), null,
                                        node(DocumentFormat.XML, "systems.xml",
                                                "/systems/system/model-access-info/model-access/read",
                                                "read", attrs("path", "user"), null,
                                                leaf("systems.xml",
                                                        "/systems/system/model-access-info/model-access/read/ref",
                                                        "ref", attrs("view", "UserInfo"))),
                                        node(DocumentFormat.XML, "systems.xml",
                                                "/systems/system/model-access-info/model-access/write",
                                                "write", attrs("path", "user"), null,
                                                leaf("systems.xml",
                                                        "/systems/system/model-access-info/model-access/write/ref",
                                                        "ref", attrs("view", "UserInfo"))))))));

        CanonicalDocumentNode rule = node(
                DocumentFormat.XML,
                "rule.xml",
                "/orm-rule-mapping",
                "orm-rule-mapping",
                attrs(),
                null,
                node(DocumentFormat.XML, "rule.xml",
                        "/orm-rule-mapping/rule-view-info",
                        "rule-view-info",
                        attrs("system", "user", "name", "active",
                                "view-ref", "UserInfo"),
                        null,
                        node(DocumentFormat.XML, "rule.xml",
                                "/orm-rule-mapping/rule-view-info/rule",
                                "rule", attrs("name", "check"), null,
                                leaf("rule.xml",
                                        "/orm-rule-mapping/rule-view-info/rule/customer-process",
                                        "customer-process", attrs("ref", "custom"))))));

        CanonicalDocumentNode backAction = node(
                DocumentFormat.XML,
                "business.xml",
                "/business-config/directory-info/directory/subdirectory-info/subdirectory/back/action-info/action",
                "action",
                attrs("name", "backAction"),
                null);
        CanonicalDocumentNode directAction = node(
                DocumentFormat.XML,
                "business.xml",
                "/business-config/directory-info/directory/action-info/action",
                "action",
                attrs("name", "run", "system-ref", "user"),
                null,
                container("business.xml",
                        "/business-config/directory-info/directory/action-info/action/produce-info",
                        "produce-info",
                        leaf("business.xml",
                                "/business-config/directory-info/directory/action-info/action/produce-info/produce",
                                "produce", attrs("ref", "Model"))));
        CanonicalDocumentNode business = node(
                DocumentFormat.XML,
                "business.xml",
                "/business-config",
                "business-config",
                attrs("name", "scope"),
                null,
                container("business.xml", "/business-config/directory-info",
                        "directory-info",
                        node(DocumentFormat.XML, "business.xml",
                                "/business-config/directory-info/directory",
                                "directory", attrs("name", "dir"), null,
                                container("business.xml",
                                        "/business-config/directory-info/directory/subdirectory-info",
                                        "subdirectory-info",
                                        node(DocumentFormat.XML, "business.xml",
                                                "/business-config/directory-info/directory/subdirectory-info/subdirectory",
                                                "subdirectory", attrs("rel", "child"), null,
                                                node(DocumentFormat.XML, "business.xml",
                                                        "/business-config/directory-info/directory/subdirectory-info/subdirectory/back",
                                                        "back", attrs("name", "back"), null,
                                                        container("business.xml",
                                                                "/business-config/directory-info/directory/subdirectory-info/subdirectory/back/action-info",
                                                                "action-info", backAction))))),
                                container("business.xml",
                                        "/business-config/directory-info/directory/dependency-info",
                                        "dependency-info",
                                        leaf("business.xml",
                                                "/business-config/directory-info/directory/dependency-info/dependency",
                                                "dependency",
                                                attrs("information-ref", "user.active"))),
                                container("business.xml",
                                        "/business-config/directory-info/directory/action-info",
                                        "action-info", directAction),
                                leaf("business.xml",
                                        "/business-config/directory-info/directory/change-info",
                                        "change-info",
                                        attrs("information-ref", "user.active")))));

        return Arrays.asList(rootConfig, data, view, systems, rule, business);
    }

    /**
     * 以指定 format 构建单个 DATA 定义。
     */
    private static RawDefinition buildSingleData(DocumentFormat format) {
        CanonicalDocumentNode document = node(
                format,
                "data.source",
                "/orm-data-mapping",
                "orm-data-mapping",
                attrs(),
                null,
                node(format,
                        "data.source",
                        "/orm-data-mapping/data",
                        "data",
                        attrs("name", "user"),
                        null));
        RawBuildResult result = new RawDefinitionBuilder().build(
                Collections.singletonList(document));
        assertEquals(RawBuildStatus.BUILT, result.status());
        return result.rawDefinitionSet().get().definitions().get(0);
    }

    /**
     * 创建无 scalar 的容器节点。
     */
    private static CanonicalDocumentNode container(
            String sourceId,
            String path,
            String name,
            CanonicalDocumentNode... children) {
        return node(DocumentFormat.XML, sourceId, path, name, attrs(), null, children);
    }

    /**
     * 创建无 children 的叶节点。
     */
    private static CanonicalDocumentNode leaf(
            String sourceId,
            String path,
            String name,
            Map<String, String> attributes) {
        return node(DocumentFormat.XML, sourceId, path, name, attributes, null);
    }

    /**
     * 创建带 scalar 的叶节点。
     */
    private static CanonicalDocumentNode scalar(
            String sourceId,
            String path,
            String name,
            String value) {
        return node(DocumentFormat.XML, sourceId, path, name, attrs(), value);
    }

    /**
     * 创建不可变 Canonical 节点。
     */
    private static CanonicalDocumentNode node(
            DocumentFormat format,
            String sourceId,
            String path,
            String name,
            Map<String, String> attributes,
            String scalar,
            CanonicalDocumentNode... children) {
        return new CanonicalDocumentNode(
                name,
                attributes,
                scalar == null ? Optional.<String>empty() : Optional.of(scalar),
                Arrays.asList(children),
                new SourceRef(sourceId, 1, 1, path),
                format,
                "1.0");
    }

    /**
     * 创建保持输入顺序的属性 Map；Canonical 构造器会负责稳定排序。
     */
    private static Map<String, String> attrs(String... values) {
        Map<String, String> attributes = new LinkedHashMap<String, String>();
        for (int index = 0; index < values.length; index += 2) {
            attributes.put(values[index], values[index + 1]);
        }
        return attributes;
    }
}
