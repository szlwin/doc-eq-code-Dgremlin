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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T06 的 Canonical → RawDefinitionSet 行为与架构 Oracle。
 *
 * <p>测试通过反射加载目标 Raw API，使生产类型缺失时仍能按 Java 8 编译并形成
 * 行为 RED。架构检查会忽略 JaCoCo 等工具注入的 synthetic 字段，只审查业务字段。</p>
 */
class RawDefinitionBuilderContractTest {
    private static final String BUILDER =
            "dec.core.compiler.raw.RawDefinitionBuilder";
    private static final String KIND =
            "dec.core.compiler.raw.RawDefinitionKind";

    @Test
    void buildsAllFrozenKindsWithStableOrdinals() {
        List<?> values = definitions(assertBuilt(build(documents())));
        List<String> kinds = Arrays.asList(
                "ROOT_CONFIG", "DATA_SOURCE", "CONNECTION", "DATA", "VIEW",
                "SYSTEM", "INFORMATION", "MODEL_ACCESS", "RULE_VIEW", "RULE",
                "BUSINESS_SCOPE", "DIRECTORY", "ACTION", "PRODUCE");
        assertEquals(kinds.size(), values.size());
        for (int index = 0; index < values.size(); index++) {
            assertEquals(kinds.get(index), invoke(values.get(index), "kind").toString());
            assertEquals((long) index,
                    ((Number) invoke(values.get(index), "sourceOrdinal")).longValue());
        }
    }

    @Test
    void preservesSourceFactsAttributesScalarAndOrderedBody() {
        Object definition = byKind(assertBuilt(build(documents())), "DATA_SOURCE").get(0);
        assertEquals(Optional.of("mix"), invoke(definition, "ownerToken"));
        assertEquals(Optional.of("data1"), invoke(definition, "name"));
        assertEquals(DocumentFormat.XML, invoke(definition, "format"));
        assertEquals("1.0", invoke(definition, "schemaVersion"));
        assertEquals(ref("root.xml",
                        "/orm-config/orm-datasource-info/orm-datasource"),
                invoke(definition, "sourceRef"));
        assertEquals(Collections.singletonMap("name", "data1"),
                invoke(definition, "attributes"));
        Object body = invoke(definition, "body");
        assertEquals("orm-datasource", invoke(body, "name"));
        List<?> children = (List<?>) invoke(body, "children");
        assertEquals(1, children.size());
        assertEquals(Optional.of("MySQL"), invoke(children.get(0), "scalar"));
    }

    @Test
    void extractsReferencesWithoutResolvingTargets() {
        Object set = assertBuilt(build(documents()));
        assertReference(byKind(set, "CONNECTION").get(0),
                "/data-source-info/data-source@ref", "data1");
        assertReference(byKind(set, "INFORMATION").get(0),
                "@view-ref", "UserInfo");
        assertReference(byKind(set, "INFORMATION").get(0),
                "@rule-ref", "isActivated");
        assertReference(byKind(set, "ACTION").get(0),
                "@system-ref", "order");
        assertReference(byKind(set, "ACTION").get(0),
                "@rule-ref", "save-Order");
        assertReference(byKind(set, "PRODUCE").get(0),
                "@ref", "OrderInfo");
    }

    @Test
    void stopsParentReferenceTraversalAtNestedDefinitions() {
        Object set = assertBuilt(build(documents()));
        List<?> system = refs(byKind(set, "SYSTEM").get(0));
        assertEquals(1, system.size());
        assertEquals("user", invoke(system.get(0), "target"));
        List<?> directory = refs(byKind(set, "DIRECTORY").get(0));
        assertEquals(2, directory.size());
        assertFalse(directory.stream().anyMatch(value ->
                "save-Order".equals(invoke(value, "target"))));
    }

    @Test
    void repeatedBuildIsDeterministicAndStateless() {
        Object builder = builder();
        Object first = assertBuilt(build(builder, documents()));
        assertEquals(first, assertBuilt(build(builder, documents())));
        assertEquals(first, assertBuilt(build(builder(), documents())));
    }

    @Test
    void rejectsUnknownDocumentRootWithoutPartialSet() {
        CanonicalDocumentNode root = node(
                "bad.xml", "/bad", "bad", attrs("name", "bad"), null);
        assertFailed(build(Collections.singletonList(root)),
                "raw.document.root.unsupported", root.sourceRef());
    }

    @Test
    void rejectsUnknownChildWithoutPartialSet() {
        CanonicalDocumentNode unknown = node(
                "data.xml", "/orm-data-mapping/data/mystery",
                "mystery", attrs(), null);
        CanonicalDocumentNode root = node(
                "data.xml", "/orm-data-mapping", "orm-data-mapping", attrs(), null,
                node("data.xml", "/orm-data-mapping/data", "data",
                        attrs("name", "user"), null, unknown));
        assertFailed(build(Collections.singletonList(root)),
                "raw.structure.unknown", unknown.sourceRef());
    }

    @Test
    void rejectsMissingNameOrOwnerWithoutPartialSet() {
        CanonicalDocumentNode data = node(
                "data.xml", "/orm-data-mapping/data", "data", attrs(), null);
        assertFailed(build(Collections.singletonList(node(
                        "data.xml", "/orm-data-mapping", "orm-data-mapping",
                        attrs(), null, data))),
                "raw.definition.name.required", data.sourceRef());

        CanonicalDocumentNode ruleView = node(
                "rule.xml", "/orm-rule-mapping/rule-view-info", "rule-view-info",
                attrs("name", "isActivated", "view-ref", "UserInfo"), null);
        assertFailed(build(Collections.singletonList(node(
                        "rule.xml", "/orm-rule-mapping", "orm-rule-mapping",
                        attrs(), null, ruleView))),
                "raw.definition.owner.required", ruleView.sourceRef());
    }

    @Test
    void rejectsNullInputAndNullDocumentWithoutPartialSet() {
        assertFailed(build((List<CanonicalDocumentNode>) null),
                "raw.input.required", null);
        assertFailed(build(Collections.singletonList(null)),
                "raw.document.required", null);
    }

    @Test
    void publishedCollectionsAreImmutable() {
        Object set = assertBuilt(build(documents()));
        @SuppressWarnings("unchecked")
        List<Object> definitions = (List<Object>) definitions(set);
        assertThrows(UnsupportedOperationException.class,
                () -> definitions.add(definitions.get(0)));
        @SuppressWarnings("unchecked")
        Map<String, String> attributes = (Map<String, String>)
                invoke(definitions.get(0), "attributes");
        assertThrows(UnsupportedOperationException.class,
                () -> attributes.put("changed", "true"));
        @SuppressWarnings("unchecked")
        List<Object> references = (List<Object>)
                invoke(definitions.get(0), "references");
        assertThrows(UnsupportedOperationException.class,
                () -> references.add(new Object()));
    }

    @Test
    void doesNotExposeParserTypesOrMutableRegistrationApi() {
        for (String name : Arrays.asList(
                "dec.core.compiler.raw.RawReference",
                "dec.core.compiler.raw.RawNodeBody",
                "dec.core.compiler.raw.RawDefinition",
                "dec.core.compiler.raw.RawDefinitionSet",
                "dec.core.compiler.raw.RawBuildResult",
                BUILDER)) {
            Class<?> type = load(name);
            for (Field field : type.getDeclaredFields()) {
                if (field.isSynthetic() || "$jacocoData".equals(field.getName())) {
                    continue;
                }
                String fieldType = field.getGenericType().getTypeName();
                assertFalse(fieldType.startsWith("org.w3c.dom"));
                assertFalse(fieldType.startsWith("org.xml.sax"));
                assertFalse(fieldType.startsWith("org.yaml.snakeyaml"));
                assertFalse(fieldType.startsWith("javax.xml"));
                if (Modifier.isStatic(field.getModifiers())) {
                    assertTrue(Modifier.isFinal(field.getModifiers()),
                            "不得存在 static 可变 Raw registry: " + field);
                }
            }
            for (Method method : type.getMethods()) {
                assertFalse(Arrays.asList("add", "put", "register", "remove", "clear")
                        .contains(method.getName()),
                        "不得暴露可变注册 API: " + method);
            }
        }
    }

    /** 构造覆盖全部 14 类定义的最小 Canonical 文档。 */
    private static List<CanonicalDocumentNode> documents() {
        CanonicalDocumentNode dataSource = node(
                "root.xml", "/orm-config/orm-datasource-info/orm-datasource",
                "orm-datasource", attrs("name", "data1"), null,
                node("root.xml",
                        "/orm-config/orm-datasource-info/orm-datasource/name",
                        "name", attrs(), "MySQL"));
        CanonicalDocumentNode connection = node(
                "root.xml", "/orm-config/orm-connection-info/orm-connection",
                "orm-connection", attrs("name", "con1"), null,
                node("root.xml",
                        "/orm-config/orm-connection-info/orm-connection/data-source-info",
                        "data-source-info", attrs(), null,
                        node("root.xml",
                                "/orm-config/orm-connection-info/orm-connection/data-source-info/data-source",
                                "data-source", attrs("ref", "data1"), null)));
        CanonicalDocumentNode root = node(
                "root.xml", "/orm-config", "orm-config", attrs("name", "mix"), null,
                node("root.xml", "/orm-config/orm-datasource-info",
                        "orm-datasource-info", attrs(), null, dataSource),
                node("root.xml", "/orm-config/orm-connection-info",
                        "orm-connection-info", attrs(), null, connection));

        CanonicalDocumentNode dataDocument = node(
                "data.xml", "/orm-data-mapping", "orm-data-mapping", attrs(), null,
                node("data.xml", "/orm-data-mapping/data", "data",
                        attrs("name", "user"), null,
                        node("data.xml", "/orm-data-mapping/data/property-info",
                                "property-info", attrs(), null,
                                node("data.xml",
                                        "/orm-data-mapping/data/property-info/property",
                                        "property", attrs("name", "id", "type", "int"),
                                        null))));

        CanonicalDocumentNode viewDocument = node(
                "view.xml", "/orm-view-mapping", "orm-view-mapping", attrs(), null,
                node("view.xml", "/orm-view-mapping/view", "view",
                        attrs("name", "UserInfo", "target-main", "user"), null,
                        node("view.xml", "/orm-view-mapping/view/property-info",
                                "property-info", attrs(), null,
                                node("view.xml",
                                        "/orm-view-mapping/view/property-info/property",
                                        "property",
                                        attrs("name", "id", "ref-property", "id"),
                                        null))));

        CanonicalDocumentNode information = node(
                "systems.xml", "/systems/system/information-info/information",
                "information",
                attrs("name", "activated", "view-ref", "UserInfo",
                        "rule-ref", "isActivated"), null);
        CanonicalDocumentNode modelAccess = node(
                "systems.xml", "/systems/system/model-access-info/model-access",
                "model-access", attrs("model-ref", "OrderInfo"), null,
                node("systems.xml", "/systems/system/model-access-info/model-access/read",
                        "read", attrs("path", "user"), null,
                        node("systems.xml",
                                "/systems/system/model-access-info/model-access/read/ref",
                                "ref", attrs("view", "UserInfo", "property", "user"),
                                null)));
        CanonicalDocumentNode systems = node(
                "systems.xml", "/systems", "systems", attrs(), null,
                node("systems.xml", "/systems/system", "system",
                        attrs("name", "user"), null,
                        node("systems.xml", "/systems/system/data-info", "data-info",
                                attrs(), null,
                                node("systems.xml", "/systems/system/data-info/data-ref",
                                        "data-ref", attrs("ref", "user"), null)),
                        node("systems.xml", "/systems/system/information-info",
                                "information-info", attrs(), null, information),
                        node("systems.xml", "/systems/system/model-access-info",
                                "model-access-info", attrs(), null, modelAccess)));

        CanonicalDocumentNode rules = node(
                "rule.xml", "/orm-rule-mapping", "orm-rule-mapping", attrs(), null,
                node("rule.xml", "/orm-rule-mapping/rule-view-info",
                        "rule-view-info",
                        attrs("system", "user", "name", "isActivated",
                                "view-ref", "UserInfo"), null,
                        node("rule.xml", "/orm-rule-mapping/rule-view-info/rule",
                                "rule",
                                attrs("name", "checkActivated",
                                        "type", "checkPattern",
                                        "property", "user.status"), null)));

        CanonicalDocumentNode produce = node(
                "business.xml",
                "/business-config/directory-info/directory/action-info/action/produce-info/produce",
                "produce", attrs("ref", "OrderInfo",
                        "information-ref", "order.ordered"), null);
        CanonicalDocumentNode action = node(
                "business.xml",
                "/business-config/directory-info/directory/action-info/action",
                "action", attrs("name", "saveOrder", "system-ref", "order",
                        "rule-ref", "save-Order"), null,
                node("business.xml",
                        "/business-config/directory-info/directory/action-info/action/produce-info",
                        "produce-info", attrs(), null, produce));
        CanonicalDocumentNode business = node(
                "business.xml", "/business-config", "business-config",
                attrs("name", "order-payment"), null,
                node("business.xml", "/business-config/directory-info",
                        "directory-info", attrs(), null,
                        node("business.xml",
                                "/business-config/directory-info/directory",
                                "directory",
                                attrs("name", "ordered",
                                        "information-ref", "order.ordered",
                                        "model-ref", "OrderInfo"), null,
                                node("business.xml",
                                        "/business-config/directory-info/directory/action-info",
                                        "action-info", attrs(), null, action))));

        return Arrays.asList(root, dataDocument, viewDocument, systems, rules, business);
    }

    /** 创建不可变 Canonical 节点。 */
    private static CanonicalDocumentNode node(
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

    private static Object builder() {
        try {
            Constructor<?> constructor = load(BUILDER).getConstructor();
            return constructor.newInstance();
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("RawDefinitionBuilder 必须提供 public 无参构造器", failure);
        }
    }

    private static Object build(List<CanonicalDocumentNode> documents) {
        return build(builder(), documents);
    }

    private static Object build(Object builder, List<CanonicalDocumentNode> documents) {
        try {
            return builder.getClass().getMethod("build", List.class)
                    .invoke(builder, new Object[] {documents});
        } catch (InvocationTargetException failure) {
            throw new AssertionError("Raw build 不得泄漏异常", failure.getCause());
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("缺少 build(List) 接缝", failure);
        }
    }

    private static Object assertBuilt(Object result) {
        assertEquals("BUILT", invoke(result, "status").toString());
        Optional<?> set = (Optional<?>) invoke(result, "rawDefinitionSet");
        assertTrue(set.isPresent());
        assertTrue(((List<?>) invoke(result, "diagnostics")).isEmpty());
        return set.get();
    }

    private static void assertFailed(Object result, String messageKey, SourceRef sourceRef) {
        assertEquals("FAILED", invoke(result, "status").toString());
        assertFalse(((Optional<?>) invoke(result, "rawDefinitionSet")).isPresent());
        @SuppressWarnings("unchecked")
        List<Diagnostic> diagnostics = (List<Diagnostic>) invoke(result, "diagnostics");
        Diagnostic diagnostic = diagnostics.get(0);
        assertEquals(DiagnosticCode.MIX_STRUCTURE_UNKNOWN, diagnostic.code());
        assertEquals(DiagnosticSeverity.ERROR, diagnostic.severity());
        assertEquals(messageKey, diagnostic.messageKey());
        if (sourceRef != null) {
            assertEquals(sourceRef, diagnostic.sourceRef());
        }
    }

    private static List<?> definitions(Object set) {
        return (List<?>) invoke(set, "definitions");
    }

    private static List<?> byKind(Object set, String value) {
        try {
            Class<?> kindType = load(KIND);
            @SuppressWarnings({"rawtypes", "unchecked"})
            Object kind = Enum.valueOf((Class) kindType, value);
            return (List<?>) set.getClass().getMethod("definitions", kindType)
                    .invoke(set, kind);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("缺少按 Kind 读取接缝", failure);
        }
    }

    private static List<?> refs(Object definition) {
        return (List<?>) invoke(definition, "references");
    }

    private static void assertReference(Object definition, String role, String target) {
        assertTrue(refs(definition).stream().anyMatch(value ->
                role.equals(invoke(value, "role"))
                        && target.equals(invoke(value, "target"))),
                "缺少引用 " + role + " -> " + target);
    }

    private static Object invoke(Object target, String method) {
        try {
            return target.getClass().getMethod(method).invoke(target);
        } catch (InvocationTargetException failure) {
            throw new AssertionError("访问器不得抛出异常: " + method,
                    failure.getCause());
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("缺少访问器: " + method, failure);
        }
    }

    private static Class<?> load(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException failure) {
            throw new AssertionError("T06 目标生产类型缺失: " + className, failure);
        }
    }
}
