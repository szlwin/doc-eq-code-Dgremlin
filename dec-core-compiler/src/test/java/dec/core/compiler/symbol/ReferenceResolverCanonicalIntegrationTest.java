package dec.core.compiler.symbol;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.CanonicalDocumentNode;
import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.raw.RawBuildResult;
import dec.core.compiler.raw.RawBuildStatus;
import dec.core.compiler.raw.RawDefinitionBuilder;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.context.model.DataKey;
import dec.core.context.model.InformationKey;
import dec.core.context.model.RuleViewKey;
import dec.core.context.model.SourceRef;
import dec.core.context.model.SystemKey;
import dec.core.context.model.ViewKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * 真实 Canonical → T06 → T07 → T08 引用解析集成 Oracle。
 */
class ReferenceResolverCanonicalIntegrationTest {

    /** 真实 body 路径必须覆盖 ref/name、nested owner 与前向 RuleView。 */
    @Test
    void resolvesRealCanonicalBodyPaths() {
        RawDefinitionSet definitions = buildRaw(legalDocuments("UserInfo"));
        ReferenceResolutionResult result = new ReferenceResolver().resolve(
                definitions,
                buildSymbols(definitions));

        assertEquals(ReferenceResolutionStatus.RESOLVED, result.status());
        assertTrue(result.resolvedReferences().isPresent());
        ResolvedReferenceSet references = result.resolvedReferences().get();
        assertTrue(references.size() >= 16);
        assertTrue(references.references().stream().anyMatch(reference ->
                reference.targetKey().equals(new DataKey("user"))
                        && reference.role().endsWith("@target-main")));
        assertTrue(references.references().stream().anyMatch(reference ->
                reference.targetKey().equals(new DataKey("user"))
                        && reference.role().endsWith("@ref-property")));
        assertTrue(references.references().stream().anyMatch(reference ->
                reference.targetKey().equals(new ViewKey("UserInfo"))
                        && reference.role().endsWith("@name")));
        assertTrue(references.references().stream().anyMatch(reference ->
                reference.sourceKey().equals(new RuleViewKey(
                        new SystemKey("user"), "check"))));
        assertTrue(references.references().stream().anyMatch(reference ->
                reference.targetKey().equals(new InformationKey(
                        new SystemKey("user"), "active"))));
    }

    /** 多段 qualified target 即使存在同名 Information 也必须拒绝。 */
    @Test
    void rejectsRealCanonicalMultiSegmentInformation() {
        List<CanonicalDocumentNode> documents = Arrays.asList(
                systemsDocument("active.extra"),
                businessDocument("user.active.extra"));
        assertOwnerInvalid(documents);
    }

    /** 空白 qualified System segment 必须由结果边界接管。 */
    @Test
    void rejectsRealCanonicalBlankQualifiedSystem() {
        assertOwnerInvalid(Arrays.asList(
                systemsDocument("active"),
                businessDocument(" .active")));
    }

    /** 空白 qualified Information segment 必须由结果边界接管。 */
    @Test
    void rejectsRealCanonicalBlankQualifiedInformation() {
        assertOwnerInvalid(Arrays.asList(
                systemsDocument("active"),
                businessDocument("user. ")));
    }

    /** target-main 不属于 T06 通用引用白名单，T08 必须自行 fail-closed。 */
    @Test
    void rejectsRealCanonicalBlankTargetMain() {
        assertOwnerInvalid(Arrays.asList(
                dataDocument(),
                viewDocument("UserInfo", " ")));
    }

    /** data-ref@name 的空白值能够进入 Raw body，T08 不得抛异常。 */
    @Test
    void rejectsRealCanonicalBlankDataRefName() {
        assertOwnerInvalid(Arrays.asList(
                dataDocument(),
                systemDeclarationDocument("data-ref", "name", " ")));
    }

    /** view-ref@name 的空白值能够进入 Raw body，T08 不得抛异常。 */
    @Test
    void rejectsRealCanonicalBlankViewRefName() {
        assertOwnerInvalid(Arrays.asList(
                viewDocument("UserInfo", "user"),
                systemDeclarationDocument("view-ref", "name", " ")));
    }

    /** data-ref/view-ref 缺失 ref/name 时必须在真实 body SourceRef 失败。 */
    @Test
    void rejectsRealCanonicalMissingDeclarationTarget() {
        assertOwnerInvalid(Arrays.asList(
                dataDocument(),
                systemDeclarationDocument("data-ref", null, null)));
    }

    /** Canonical 产生的不同 Raw 快照不得与上一 revision SymbolTable 混用。 */
    @Test
    void rejectsRealCanonicalSnapshotMismatch() {
        RawDefinitionSet original = buildRaw(legalDocuments("UserInfo"));
        RawDefinitionSet changed = buildRaw(legalDocuments("ChangedView"));

        ReferenceResolutionResult result = assertDoesNotThrow(
                () -> new ReferenceResolver().resolve(
                        changed,
                        buildSymbols(original)));

        assertEquals(ReferenceResolutionStatus.FAILED, result.status());
        assertFalse(result.resolvedReferences().isPresent());
        assertEquals(1, result.diagnostics().size());
        assertEquals("reference.input.snapshot-mismatch",
                result.diagnostics().get(0).messageKey());
    }

    private static void assertOwnerInvalid(List<CanonicalDocumentNode> documents) {
        RawDefinitionSet definitions = buildRaw(documents);
        ReferenceResolutionResult result = assertDoesNotThrow(
                () -> new ReferenceResolver().resolve(
                        definitions,
                        buildSymbols(definitions)));
        assertEquals(ReferenceResolutionStatus.FAILED, result.status());
        assertFalse(result.resolvedReferences().isPresent());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                "reference.owner.invalid".equals(diagnostic.messageKey())));
    }

    private static RawDefinitionSet buildRaw(
            List<CanonicalDocumentNode> documents) {
        RawBuildResult result = new RawDefinitionBuilder().build(documents);
        assertEquals(RawBuildStatus.BUILT, result.status(), result.diagnostics().toString());
        assertTrue(result.rawDefinitionSet().isPresent());
        return result.rawDefinitionSet().get();
    }

    private static SymbolTable buildSymbols(RawDefinitionSet definitions) {
        SymbolBuildResult result = new SymbolTableBuilder().build(definitions);
        assertEquals(SymbolBuildStatus.BUILT, result.status(), result.diagnostics().toString());
        assertTrue(result.symbolTable().isPresent());
        return result.symbolTable().get();
    }

    private static List<CanonicalDocumentNode> legalDocuments(String viewName) {
        List<CanonicalDocumentNode> documents = new ArrayList<CanonicalDocumentNode>();
        documents.add(rootDocument());
        documents.add(dataDocument());
        documents.add(viewDocument(viewName, "user"));
        // 规则文档故意位于 System 文档之前，验证不依赖发现顺序。
        documents.add(ruleDocument(viewName));
        documents.add(systemsDocument("active", viewName));
        documents.add(businessDocument("user.active"));
        return documents;
    }

    private static CanonicalDocumentNode rootDocument() {
        return node("root.xml", "/orm-config", "orm-config", attrs("name", "mix"),
                node("root.xml", "/orm-config/orm-datasource-info",
                        "orm-datasource-info", attrs(),
                        node("root.xml", "/orm-config/orm-datasource-info/orm-datasource",
                                "orm-datasource", attrs("name", "data1"))),
                node("root.xml", "/orm-config/orm-connection-info",
                        "orm-connection-info", attrs(),
                        node("root.xml", "/orm-config/orm-connection-info/orm-connection",
                                "orm-connection", attrs("name", "con1"),
                                node("root.xml", "/orm-config/orm-connection-info/orm-connection/data-source-info",
                                        "data-source-info", attrs(),
                                        node("root.xml", "/orm-config/orm-connection-info/orm-connection/data-source-info/data-source",
                                                "data-source", attrs("ref", "data1"))))));
    }

    private static CanonicalDocumentNode dataDocument() {
        return node("data.xml", "/orm-data-mapping", "orm-data-mapping", attrs(),
                node("data.xml", "/orm-data-mapping/data", "data", attrs("name", "user"),
                        node("data.xml", "/orm-data-mapping/data/property-info",
                                "property-info", attrs(),
                                node("data.xml", "/orm-data-mapping/data/property-info/property[1]",
                                        "property", attrs("name", "id")),
                                node("data.xml", "/orm-data-mapping/data/property-info/property[2]",
                                        "property", attrs("name", "status")))));
    }

    private static CanonicalDocumentNode viewDocument(
            String viewName,
            String targetMain) {
        return node("view.xml", "/orm-view-mapping", "orm-view-mapping", attrs(),
                node("view.xml", "/orm-view-mapping/view", "view",
                        attrs("name", viewName, "target-main", targetMain),
                        node("view.xml", "/orm-view-mapping/view/property-info",
                                "property-info", attrs(),
                                node("view.xml", "/orm-view-mapping/view/property-info/property",
                                        "property", attrs(
                                                "name", "id",
                                                "data", "user",
                                                "ref-property", "id")))));
    }

    private static CanonicalDocumentNode ruleDocument(String viewName) {
        return node("rules.xml", "/orm-rule-mapping", "orm-rule-mapping", attrs(),
                node("rules.xml", "/orm-rule-mapping/rule-view-info",
                        "rule-view-info", attrs(
                                "system", "user",
                                "name", "check",
                                "view-ref", viewName)));
    }

    private static CanonicalDocumentNode systemsDocument(String informationName) {
        return systemsDocument(informationName, null);
    }

    private static CanonicalDocumentNode systemsDocument(
            String informationName,
            String viewName) {
        List<CanonicalDocumentNode> children = new ArrayList<CanonicalDocumentNode>();
        if (viewName != null) {
            children.add(node("systems.xml", "/systems/system/data-info",
                    "data-info", attrs(),
                    node("systems.xml", "/systems/system/data-info/data-ref[1]",
                            "data-ref", attrs("ref", "user")),
                    node("systems.xml", "/systems/system/data-info/data-ref[2]",
                            "data-ref", attrs("name", "user"))));
            children.add(node("systems.xml", "/systems/system/view-info",
                    "view-info", attrs(),
                    node("systems.xml", "/systems/system/view-info/view-ref[1]",
                            "view-ref", attrs("ref", viewName)),
                    node("systems.xml", "/systems/system/view-info/view-ref[2]",
                            "view-ref", attrs("name", viewName))));
        }
        children.add(node("systems.xml", "/systems/system/information-info",
                "information-info", attrs(),
                node("systems.xml", "/systems/system/information-info/information",
                        "information", attrs("name", informationName))));
        return node("systems.xml", "/systems", "systems", attrs(),
                node("systems.xml", "/systems/system", "system",
                        attrs("name", "user"),
                        children.toArray(new CanonicalDocumentNode[children.size()])));
    }

    private static CanonicalDocumentNode systemDeclarationDocument(
            String declarationName,
            String attributeName,
            String attributeValue) {
        Map<String, String> declarationAttributes = attrs();
        if (attributeName != null) {
            declarationAttributes.put(attributeName, attributeValue);
        }
        String containerName = "data-ref".equals(declarationName)
                ? "data-info" : "view-info";
        return node("systems-malformed.xml", "/systems", "systems", attrs(),
                node("systems-malformed.xml", "/systems/system", "system",
                        attrs("name", "user"),
                        node("systems-malformed.xml", "/systems/system/" + containerName,
                                containerName, attrs(),
                                node("systems-malformed.xml",
                                        "/systems/system/" + containerName + "/" + declarationName,
                                        declarationName,
                                        declarationAttributes))));
    }

    private static CanonicalDocumentNode businessDocument(String informationTarget) {
        return node("business.xml", "/business-config", "business-config",
                attrs("name", "scope"),
                node("business.xml", "/business-config/directory-info",
                        "directory-info", attrs(),
                        node("business.xml", "/business-config/directory-info/directory[1]",
                                "directory", attrs("name", "start")),
                        node("business.xml", "/business-config/directory-info/directory[2]",
                                "directory", attrs(
                                        "name", "ordered",
                                        "information-ref", informationTarget),
                                node("business.xml", "/business-config/directory-info/directory[2]/subdirectory-info",
                                        "subdirectory-info", attrs(),
                                        node("business.xml", "/business-config/directory-info/directory[2]/subdirectory-info/subdirectory",
                                                "subdirectory", attrs("rel", "start"))),
                                node("business.xml", "/business-config/directory-info/directory[2]/action-info",
                                        "action-info", attrs(),
                                        node("business.xml", "/business-config/directory-info/directory[2]/action-info/action",
                                                "action", attrs(
                                                        "name", "save",
                                                        "system-ref", "user",
                                                        "rule-ref", "check"),
                                                node("business.xml", "/business-config/directory-info/directory[2]/action-info/action/produce-info",
                                                        "produce-info", attrs(),
                                                        node("business.xml", "/business-config/directory-info/directory[2]/action-info/action/produce-info/produce",
                                                                "produce", attrs(
                                                                        "information-ref", informationTarget))))))));
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
        Map<String, String> attributes = new LinkedHashMap<String, String>();
        for (int index = 0; index < values.length; index += 2) {
            attributes.put(values[index], values[index + 1]);
        }
        return attributes;
    }

    private static SourceRef ref(String sourceId, String path) {
        return new SourceRef(sourceId, 1, 1, path);
    }
}
