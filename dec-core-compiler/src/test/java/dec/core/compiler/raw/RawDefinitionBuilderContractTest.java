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
import java.util.ArrayList;
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
 * <p>测试使用反射加载目标 Raw API，使生产类型缺失时测试源码仍能按 Java 8 编译，
 * 并以行为失败形成有效 RED，而不是以测试编译失败伪装 TDD。</p>
 */
class RawDefinitionBuilderContractTest {
    private static final String BUILDER_CLASS =
            "dec.core.compiler.raw.RawDefinitionBuilder";
    private static final String KIND_CLASS =
            "dec.core.compiler.raw.RawDefinitionKind";

    @Test
    void buildsAllFrozenKindsWithStableOrdinals() {
        Object result = build(documents());
        Object set = assertBuilt(result);
        List<?> definitions = definitions(set);

        assertEquals(14, definitions.size());
        List<String> expectedKinds = Arrays.asList(
                "ROOT_CONFIG",
                "DATA_SOURCE",
                "CONNECTION",
                "DATA",
                "VIEW",
                "SYSTEM",
                "INFORMATION",
                "MODEL_ACCESS",
                "RULE_VIEW",
                "RULE",
                "BUSINESS_SCOPE",
                "DIRECTORY",
                "ACTION",
                "PRODUCE");
        for (int index = 0; index < definitions.size(); index++) {
            Object definition = definitions.get(index);
            assertEquals(expectedKinds.get(index),
                    invoke(definition, "kind").toString());
            assertEquals(index,
                    ((Number) invoke(definition, "sourceOrdinal")).longValue());
        }
    }

    @Test
    void preservesSourceFactsAttributesScalarAndOrderedBody() {
        Object set = assertBuilt(build(documents()));
        Object dataSource = definitionsByKind(set, "DATA_SOURCE").get(0);

        assertEquals(Optional.of("mix"), invoke(dataSource, "ownerToken"));
        assertEquals(Optional.of("data1"), invoke(dataSource, "name"));
        assertEquals(DocumentFormat.XML, invoke(dataSource, "format"));
        assertEquals("1.0", invoke(dataSource, "schemaVersion"));
        assertEquals(sourceRef("root.xml", "/orm-config/orm-datasource-info/orm-datasource"),
                invoke(dataSource, "sourceRef"));

        @SuppressWarnings("unchecked")
        Map<String, String> attributes =
                (Map<String, String>) invoke(dataSource, "attributes");
        assertEquals(Collections.singletonMap("name", "data1"), attributes);

        Object body = invoke(dataSource, "body");
        assertEquals("orm-datasource", invoke(body, "name"));
        @SuppressWarnings("unchecked")
        List<Object> children = (List<Object>) invoke(body, "children");
        assertEquals(1, children.size());
        assertEquals("name", invoke(children.get(0), "name"));
        assertEquals(Optional.of("MySQL"), invoke(children.get(0), "scalar"));
    }

    @Test
    void extractsReferencesWithoutResolvingTargets() {
        Object set = assertBuilt(build(documents()));

        assertReference(definitionsByKind(set, "CONNECTION").get(0),
                "/data-source-info/data-source@ref", "data1");
        Object information = definitionsByKind(set, "INFORMATION").get(0);
        assertReference(information, "@view-ref", "UserInfo");
        assertReference(information, "@rule-ref", "isActivated");

        Object action = definitionsByKind(set, "ACTION").get(0);
        assertReference(action, "@system-ref", "order");
        assertReference(action, "@rule-ref", "save-Order");

        Object produce = definitionsByKind(set, "PRODUCE").get(0);
        assertReference(produce, "@ref", "OrderInfo");
        assertReference(produce, "@information-ref", "order.ordered");
    }

    @Test
    void stopsParentReferenceTraversalAtNestedDefinitions() {
        Object set = assertBuilt(build(documents()));
        Object system = definitionsByKind(set, "SYSTEM").get(0);
        List<?> systemReferences = references(system);
        assertEquals(1, systemReferences.size());
        assertEquals("user", invoke(systemReferences.get(0), "target"));

        Object directory = definitionsByKind(set, "DIRECTORY").get(0);
        List<?> directoryReferences = references(directory);
        assertEquals(2, directoryReferences.size());
        assertTrue(directoryReferences.stream()
                .anyMatch(reference -> "order.ordered".equals(
                        invoke(reference, "target"))));
        assertTrue(directoryReferences.stream()
                .anyMatch(reference -> "OrderInfo".equals(
                        invoke(reference, "target"))));
        assertFalse(directoryReferences.stream()
                .anyMatch(reference -> "save-Order".equals(
                        invoke(reference, "target"))));
    }

    @Test
    void repeatedBuildIsDeterministicAndStateless() {
        Object builder = newBuilder();
        Object first = build(builder, documents());
        Object second = build(builder, documents());
        Object third = build(newBuilder(), documents());

        assertEquals(assertBuilt(first), assertBuilt(second));
        assertEquals(assertBuilt(first), assertBuilt(third));
    }

    @Test
    void rejectsUnknownDocumentRootWithoutPartialSet() {
        CanonicalDocumentNode unknown = node(
                "unknown.xml",
                "/unsupported-root",
                "unsupported-root",
                attrs("name", "bad"),
                null);

        assertFailed(build(Collections.singletonList(unknown)),
                "raw.document.root.unsupported",
                unknown.sourceRef());
    }

    @Test
    void rejectsUnknownChildWithoutPartialSet() {
        CanonicalDocumentNode unknownChild = node(
                "data.xml",
                "/orm-data-mapping/data/mystery",
                "mystery",
                Collections.<String, String>emptyMap(),
                null);
        CanonicalDocumentNode data = node(
                "data.xml",
                "/orm-data-mapping/data",
                "data",
                attrs("name", "user"),
                null,
                unknownChild);
        CanonicalDocumentNode document = node(
                "data.xml",
                "/orm-data-mapping",
                "orm-data-mapping",
                Collections.<String, String>emptyMap(),
                null,
                data);

        assertFailed(build(Collections.singletonList(document)),
                "raw.structure.unknown",
                unknownChild.sourceRef());
    }

    @Test
    void rejectsMissingNameOrOwnerWithoutPartialSet() {
        CanonicalDocumentNode missingName = node(
                "data.xml",
                "/orm-data-mapping/data",
                "data",
                Collections.<String, String>emptyMap(),
                null);
        CanonicalDocumentNode dataDocument = node(
                "data.xml",
                "/orm-data-mapping",
                "orm-data-mapping",
                Collections.<String, String>emptyMap(),
                null,
                missingName);
        assertFailed(build(Collections.singletonList(dataDocument)),
                "raw.definition.name.required",
                missingName.sourceRef());

        CanonicalDocumentNode ruleView = node(
                "rule.xml",
                "/orm-rule-mapping/rule-view-info",
                "rule-view-info",
                attrs("name", "isActivated", "view-ref", "UserInfo"),
                null);
        CanonicalDocumentNode ruleDocument = node(
                "rule.xml",
                "/orm-rule-mapping",
                "orm-rule-mapping",
                Collections.<String, String>emptyMap(),
                null,
                ruleView);
        assertFailed(build(Collections.singletonList(ruleDocument)),
                "raw.definition.owner.required",
                ruleView.sourceRef());
    }

    @Test
    void rejectsNullInputAndNullDocumentWithoutPartialSet() {
        assertFailed(build((List<CanonicalDocumentNode>) null),
                "raw.input.required",
                null);
        assertFailed(build(Collections.singletonList(null)),
                "raw.document.required",
                null);
    }

    @Test
    void publishedCollectionsAreImmutable() {
        Object set = assertBuilt(build(documents()));
        @SuppressWarnings("unchecked")
        List<Object> definitions = (List<Object>) definitions(set);
        assertThrows(UnsupportedOperationException.class,
                () -> definitions.add(definitions.get(0)));

        Object definition = definitions.get(0);
        @SuppressWarnings("unchecked")
        Map<String, String> attributes =
                (Map<String, String>) invoke(definition, "attributes");
        assertThrows(UnsupportedOperationException.class,
                () -> attributes.put("mutated", "true"));

        @SuppressWarnings("unchecked")
        List<Object> references =
                (List<Object>) invoke(definition, "references");
        assertThrows(UnsupportedOperationException.class,
                () -> references.add(new Object()));
    }

    @Test
    void doesNotExposeParserTypesOrMutableRegistrationApi() {
        for (String className : Arrays.asList(
                "dec.core.compiler.raw.RawReference",
                "dec.core.compiler.raw.RawNodeBody",
                "dec.core.compiler.raw.RawDefinition",
                "dec.core.compiler.raw.RawDefinitionSet",
                "dec.core.compiler.raw.RawBuildResult",
                BUILDER_CLASS)) {
            Class<?> type = load(className);
            for (Field field : type.getDeclaredFields()) {
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
                String name = method.getName();
                assertFalse(name.equals("add")
                                || name.equals("put")
                                || name.equals("register")
                                || name.equals("remove")
                                || name.equals("clear"),
                        "不得暴露可变注册 API: " + method);
            }
        }
    }

    /**
     * 构造覆盖 14 类定义的最小格式中立 Canonical 文档集合。
     */
    private static List<CanonicalDocumentNode> documents() {
        CanonicalDocumentNode dataSource = node(
                "root.xml",
                "/orm-config/orm-datasource-info/orm-datasource",
                "orm-datasource",
                attrs("name", "data1"),
                null,
                node("root.xml",
                        "/orm-config/orm-datasource-info/orm-datasource/name",
                        "name",
                        Collections.<String, String>emptyMap(),
                        "MySQL"));
        CanonicalDocumentNode connection = node(
                "root.xml",
                "/orm-config/orm-connection-info/orm-connection",
                "orm-connection",
                attrs("name", "con1"),
                null,
                node("root.xml",
                        "/orm-config/orm-connection-info/orm-connection/data-source-info",
                        "data-source-info",
                        Collections.<String, String>emptyMap(),
                        null,
                        node("root.xml",
                                "/orm-config/orm-connection-info/orm-connection/data-source-info/data-source",
                                "data-source",
                                attrs("ref", "data1"),
                                null)));
        CanonicalDocumentNode root = node(
                "root.xml",
                "/orm-config",
                "orm-config",
                attrs("name", "mix"),
                null,
                node("root.xml",
                        "/orm-config/orm-datasource-info",
                        "orm-datasource-info",
                        Collections.<String, String>emptyMap(),
                        null,
                        dataSource),
                node("root.xml",
                        "/orm-config/orm-connection-info",
                        "orm-connection-info",
                        Collections.<String, String>emptyMap(),
                        null,
                        connection));

        CanonicalDocumentNode data = node(
                "data.xml",
                "/orm-data-mapping/data",
                "data",
                attrs("name", "user"),
                null,
                node("data.xml",
                        "/orm-data-mapping/data/property-info",
                        "property-info",
                        Collections.<String, String>emptyMap(),
                        null,
                        node("data.xml",
                                "/orm-data-mapping/data/property-info/property",
                                "property",
                                attrs("name", "id", "type", "int"),
                                null)));
        CanonicalDocumentNode dataDocument = node(
                "data.xml",
                "/orm-data-mapping",
                "orm-data-mapping",
                Collections.<String, String>emptyMap(),
                null,
                data);

        CanonicalDocumentNode view = node(
                "view.xml",
                "/orm-view-mapping/view",
                "view",
                attrs("name", "UserInfo", "target-main", "user"),
                null,
                node("view.xml",
                        "/orm-view-mapping/view/property-info",
                        "property-info",
                        Collections.<String, String>emptyMap(),
                        null,
                        node("view.xml",
                                "/orm-view-mapping/view/property-info/property",
                                "property",
                                attrs("name", "id", "ref-property", "id"),
                                null)));
        CanonicalDocumentNode viewDocument = node(
                "view.xml",
                "/orm-view-mapping",
                "orm-view-mapping",
                Collections.<String, String>emptyMap(),
                null,
                view);

        CanonicalDocumentNode information = node(
                "systems.xml",
                "/systems/system/information-info/information",
                "information",
                attrs("name", "activated",
                        "view-ref", "UserInfo",
                        "rule-ref", "isActivated"),
                null);
        CanonicalDocumentNode modelAccess = node(
                "systems.xml",
                "/systems/system/model-access-info/model-access",
                "model-access",
                attrs("model-ref", "OrderInfo"),
                null,
                node("systems.xml",
                        "/systems/system/model-access-info/model-access/read",
                        "read",
                        attrs("path", "user"),
                        null,
                        node("systems.xml",
                                "/systems/system/model-access-info/model-access/read/ref",
                                "ref",
                                attrs("view", "UserInfo", "property", "user"),
                                null)));
        CanonicalDocumentNode system = node(
                "systems.xml",
                "/systems/system",
                "system",
                attrs("name", "user"),
                null,
                node("systems.xml",
                        "/systems/system/data-info",
                        "data-info",
                        Collections.<String, String>emptyMap(),
                        null,
                        node("systems.xml",
                                "/systems/system/data-info/data-ref",
                                "data-ref",
                                attrs("ref", "user"),
                                null)),
                node("systems.xml",
                        "/systems/system/information-info",
                        "information-info",
                        Collections.<String, String>emptyMap(),
                        null,
                        information),
                node("systems.xml",
                        "/systems/system/model-access-info",
                        "model-access-info",
                        Collections.<String, String>emptyMap(),
                        null,
                        modelAccess));
        CanonicalDocumentNode systemsDocument = node(
                "systems.xml",
                "/systems",
                "systems",
                Collections.<String, String>emptyMap(),
                null,
                system);

        CanonicalDocumentNode rule = node(
                "rule.xml",
                "/orm-rule-mapping/rule-view-info/rule",
                "rule",
                attrs("name", "checkActivated",
                        "type", "checkPattern",
                        "property", "user.status"),
                null);
        CanonicalDocumentNode ruleView = node(
                "rule.xml",
                "/orm-rule-mapping/rule-view-info",
                "rule-view-info",
                attrs("system", "user",
                        "name", "isActivated",
                        "view-ref", "UserInfo"),
                null,
                rule);
        CanonicalDocumentNode ruleDocument = node(
                "rule.xml",
                "/orm-rule-mapping",
                "orm-rule-mapping",
                Collections.<String, String>emptyMap(),
                null,
                ruleView);

        CanonicalDocumentNode produce = node(
                "business.xml",
                "/business-config/directory-info/directory/action-info/action/produce-info/produce",
                "produce",
                attrs("ref", "OrderInfo",
                        "information-ref", "order.ordered"),
                null);
        CanonicalDocumentNode action = node(
                "business.xml",
                "/business-config/directory-info/directory/action-info/action",
                "action",
                attrs("name", "saveOrder",
                        "system-ref", "order",
                        "rule-ref", "save-Order"),
                null,
                node("business.xml",
                        "/business-config/directory-info/directory/action-info/action/produce-info",
                        "produce-info",
                        Collections.<String, String>emptyMap(),
                        null,
                        produce));
        CanonicalDocumentNode directory = node(
                "business.xml",
                "/business-config/directory-info/directory",
                "directory",
                attrs("name", "ordered",
                        "information-ref", "order.ordered",
                        "model-ref", "OrderInfo"),
                null,
                node("business.xml",
                        "/business-config/directory-info/directory/action-info",
                        "action-info",
                        Collections.<String, String>emptyMap(),
                        null,
                        action));
        CanonicalDocumentNode businessDocument = node(
                "business.xml",
                "/business-config",
                "business-config",
                attrs("name", "order-payment"),
                null,
                node("business.xml",
                        "/business-config/directory-info",
                        "directory-info",
                        Collections.<String, String>emptyMap(),
                        null,
                        directory));

        return Arrays.asList(
                root,
                dataDocument,
                viewDocument,
                systemsDocument,
                ruleDocument,
                businessDocument);
    }

    /**
     * 创建不可变 Canonical 节点。
     */
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
                sourceRef(sourceId, path),
                DocumentFormat.XML,
                "1.0");
    }

    /**
     * 创建保持插入输入但由 CanonicalNode 稳定排序的属性 Map。
     */
    private static Map<String, String> attrs(String... values) {
        Map<String, String> attributes = new LinkedHashMap<String, String>();
        for (int index = 0; index < values.length; index += 2) {
            attributes.put(values[index], values[index + 1]);
        }
        return attributes;
    }

    /**
     * 创建固定一基 SourceRef。
     */
    private static SourceRef sourceRef(String sourceId, String path) {
        return new SourceRef(sourceId, 1, 1, path);
    }

    /**
     * 创建目标 Builder；缺失生产类时形成可诊断 RED。
     */
    private static Object newBuilder() {
        Class<?> builderType = load(BUILDER_CLASS);
        try {
            Constructor<?> constructor = builderType.getConstructor();
            return constructor.newInstance();
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(
                    "T06 RawDefinitionBuilder 必须提供 public 无参构造器",
                    failure);
        }
    }

    /**
     * 使用新 Builder 构建。
     */
    private static Object build(List<CanonicalDocumentNode> documents) {
        return build(newBuilder(), documents);
    }

    /**
     * 调用冻结的 build(List) 接缝。
     */
    private static Object build(
            Object builder,
            List<CanonicalDocumentNode> documents) {
        try {
            Method method = builder.getClass().getMethod("build", List.class);
            return method.invoke(builder, new Object[] {documents});
        } catch (InvocationTargetException failure) {
            Throwable cause = failure.getCause();
            throw new AssertionError("Raw build 不得向调用方泄漏异常", cause);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(
                    "T06 RawDefinitionBuilder 必须提供 build(List)",
                    failure);
        }
    }

    /**
     * 断言成功结果并返回 RawDefinitionSet。
     */
    private static Object assertBuilt(Object result) {
        assertEquals("BUILT", invoke(result, "status").toString());
        @SuppressWarnings("unchecked")
        Optional<Object> set =
                (Optional<Object>) invoke(result, "rawDefinitionSet");
        assertTrue(set.isPresent());
        assertTrue(((List<?>) invoke(result, "diagnostics")).isEmpty());
        return set.get();
    }

    /**
     * 断言失败结果不包含部分集合。
     */
    private static void assertFailed(
            Object result,
            String messageKey,
            SourceRef expectedRef) {
        assertEquals("FAILED", invoke(result, "status").toString());
        assertFalse(((Optional<?>) invoke(result, "rawDefinitionSet")).isPresent());
        @SuppressWarnings("unchecked")
        List<Diagnostic> diagnostics =
                (List<Diagnostic>) invoke(result, "diagnostics");
        assertFalse(diagnostics.isEmpty());
        Diagnostic diagnostic = diagnostics.get(0);
        assertEquals(DiagnosticCode.MIX_STRUCTURE_UNKNOWN, diagnostic.code());
        assertEquals(DiagnosticSeverity.ERROR, diagnostic.severity());
        assertEquals(messageKey, diagnostic.messageKey());
        if (expectedRef != null) {
            assertEquals(expectedRef, diagnostic.sourceRef());
        }
    }

    /**
     * 返回全部定义。
     */
    private static List<?> definitions(Object set) {
        return (List<?>) invoke(set, "definitions");
    }

    /**
     * 按冻结 Kind 返回定义。
     */
    private static List<?> definitionsByKind(Object set, String kind) {
        try {
            Class<?> kindType = load(KIND_CLASS);
            @SuppressWarnings({"rawtypes", "unchecked"})
            Object enumValue = Enum.valueOf((Class) kindType, kind);
            Method method = set.getClass().getMethod("definitions", kindType);
            return (List<?>) method.invoke(set, enumValue);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("RawDefinitionSet 必须支持按 Kind 读取", failure);
        }
    }

    /**
     * 返回定义引用。
     */
    private static List<?> references(Object definition) {
        return (List<?>) invoke(definition, "references");
    }

    /**
     * 断言定义包含指定 role/target 引用。
     */
    private static void assertReference(
            Object definition,
            String role,
            String target) {
        assertTrue(references(definition).stream()
                .anyMatch(reference -> role.equals(invoke(reference, "role"))
                        && target.equals(invoke(reference, "target"))),
                "缺少引用 " + role + " -> " + target);
    }

    /**
     * 调用无参访问器。
     */
    private static Object invoke(Object target, String methodName) {
        try {
            return target.getClass().getMethod(methodName).invoke(target);
        } catch (InvocationTargetException failure) {
            throw new AssertionError("访问器不得抛出异常: " + methodName,
                    failure.getCause());
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("缺少冻结访问器: " + methodName, failure);
        }
    }

    /**
     * 加载目标类；缺失时以稳定 AssertionError 形成 RED。
     */
    private static Class<?> load(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException failure) {
            throw new AssertionError("T06 目标生产类型缺失: " + className, failure);
        }
    }
}
