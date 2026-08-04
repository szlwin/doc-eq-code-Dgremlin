package dec.core.compiler.symbol;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionKind;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.raw.RawNodeBody;
import dec.core.compiler.raw.RawReference;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.SourceRef;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T08/I002 lexical、快照绑定与资源复杂度返工 Oracle。
 */
class ReferenceResolverBoundaryReworkTest {

    /** qualified Information 只能由恰好两个非空 segment 组成。 */
    @Test
    void rejectsMultiSegmentQualifiedInformation() {
        assertOwnerInvalid(informationTargetDefinitions("user.active.extra"));
    }

    /** 空白 System segment 必须稳定失败，不能让 SystemKey 异常逃逸。 */
    @Test
    void rejectsBlankQualifiedSystemWithoutThrowing() {
        assertOwnerInvalid(informationTargetDefinitions(" .active"));
    }

    /** 空白 Information segment 必须稳定失败，不能让 InformationKey 异常逃逸。 */
    @Test
    void rejectsBlankQualifiedInformationWithoutThrowing() {
        assertOwnerInvalid(informationTargetDefinitions("user. "));
    }

    /** View target-main 的纯空白值必须 fail-closed。 */
    @Test
    void rejectsBlankViewTargetMainWithoutThrowing() {
        RawDefinitionSet original = ReferenceTestFixture.legalDefinitions();
        RawDefinitionSet definitions = replaceDefinition(
                original,
                4,
                withAttribute(
                        original.definitions().get(4),
                        "target-main",
                        " "));
        assertOwnerInvalid(definitions);
    }

    /** System data-ref@name 的纯空白值必须 fail-closed。 */
    @Test
    void rejectsBlankSystemDataNameWithoutThrowing() {
        assertOwnerInvalid(systemBodyDefinitions("data-ref", "name", " "));
    }

    /** System view-ref@name 的纯空白值必须 fail-closed。 */
    @Test
    void rejectsBlankSystemViewNameWithoutThrowing() {
        assertOwnerInvalid(systemBodyDefinitions("view-ref", "name", " "));
    }

    /** 声明节点缺失 ref/name 时不得静默忽略。 */
    @Test
    void rejectsSystemDeclarationWithoutRefOrName() {
        assertOwnerInvalid(systemBodyDefinitions("data-ref", null, null));
    }

    /** 同 ordinal 但定义 name 不同的快照必须在任何解析前阻断。 */
    @Test
    void rejectsSnapshotWithDifferentName() {
        RawDefinitionSet original = ReferenceTestFixture.legalDefinitions();
        RawDefinition changed = withName(original.definitions().get(4), "ChangedView");
        assertSnapshotMismatch(replaceDefinition(original, 4, changed), original);
    }

    /** 同 ordinal 但 kind 不同的快照不得复用旧 sourceKey。 */
    @Test
    void rejectsSnapshotWithDifferentKind() {
        RawDefinitionSet original = ReferenceTestFixture.legalDefinitions();
        RawDefinition view = original.definitions().get(4);
        RawDefinition changed = copy(
                view,
                view.sourceOrdinal(),
                RawDefinitionKind.DATA,
                view.sourceRef(),
                Optional.<String>empty(),
                view.name(),
                view.attributes(),
                view.references(),
                view.body());
        assertSnapshotMismatch(replaceDefinition(original, 4, changed), original);
    }

    /** sourceRef/body 变化必须被完整值语义识别。 */
    @Test
    void rejectsSnapshotWithDifferentSourceAndBody() {
        RawDefinitionSet original = ReferenceTestFixture.legalDefinitions();
        RawDefinition view = original.definitions().get(4);
        SourceRef changedRef = ref("changed-view.xml", 99);
        RawNodeBody changedBody = new RawNodeBody(
                view.body().name(),
                view.body().attributes(),
                view.body().scalar(),
                view.body().children(),
                changedRef);
        RawDefinition changed = copy(
                view,
                view.sourceOrdinal(),
                view.kind(),
                changedRef,
                view.ownerToken(),
                view.name(),
                view.attributes(),
                view.references(),
                changedBody);
        assertSnapshotMismatch(replaceDefinition(original, 4, changed), original);
    }

    /** 新增或删除定义都必须使旧 SymbolTable 失效。 */
    @Test
    void rejectsAddedAndDeletedDefinitions() {
        RawDefinitionSet original = ReferenceTestFixture.legalDefinitions();
        List<RawDefinition> addedValues = new ArrayList<RawDefinition>(
                original.definitions());
        addedValues.add(definition(
                RawDefinitionKind.DATA,
                addedValues.size(),
                null,
                "lateData",
                attrs("name", "lateData"),
                Collections.<RawReference>emptyList(),
                body("data", attrs("name", "lateData"), ref("late.xml", 1))));
        assertSnapshotMismatch(new RawDefinitionSet(addedValues), original);

        List<RawDefinition> deletedValues = new ArrayList<RawDefinition>(
                original.definitions());
        deletedValues.remove(deletedValues.size() - 1);
        assertSnapshotMismatch(new RawDefinitionSet(deletedValues), original);
    }

    /** 重新编号导致的定义顺序变化不得按旧 ordinal 静默错绑。 */
    @Test
    void rejectsReorderedSourceOrdinals() {
        RawDefinitionSet original = ReferenceTestFixture.legalDefinitions();
        List<RawDefinition> values = new ArrayList<RawDefinition>(
                original.definitions());
        RawDefinition data = values.get(3);
        RawDefinition view = values.get(4);
        values.set(3, copy(
                view,
                3L,
                view.kind(),
                view.sourceRef(),
                view.ownerToken(),
                view.name(),
                view.attributes(),
                view.references(),
                view.body()));
        values.set(4, copy(
                data,
                4L,
                data.kind(),
                data.sourceRef(),
                data.ownerToken(),
                data.name(),
                data.attributes(),
                data.references(),
                data.body()));
        assertSnapshotMismatch(new RawDefinitionSet(values), original);
    }

    /**
     * 小预算计数验证：M 个 owner mismatch 只允许 M 次 lexical 摘要查询，
     * 查询次数不能乘以 N 个同名候选。
     */
    @Test
    void classifiesOwnerMismatchWithLinearLookupCount() {
        int candidateOwners = 12;
        int referenceCount = 9;
        RawDefinitionSet definitions = complexityDefinitions(
                candidateOwners,
                referenceCount);
        AtomicInteger lookups = new AtomicInteger();
        ReferenceResolver resolver = resolverWithLookupCounter(lookups);

        ReferenceResolutionResult result = resolver.resolve(
                definitions,
                ReferenceTestFixture.symbols(definitions));

        assertEquals(ReferenceResolutionStatus.FAILED, result.status());
        assertEquals(referenceCount, result.diagnostics().size());
        assertEquals(referenceCount, lookups.get());
    }

    private static void assertOwnerInvalid(RawDefinitionSet definitions) {
        ReferenceResolutionResult result = assertDoesNotThrow(
                () -> new ReferenceResolver().resolve(
                        definitions,
                        ReferenceTestFixture.symbols(definitions)));
        assertEquals(ReferenceResolutionStatus.FAILED, result.status());
        assertFalse(result.resolvedReferences().isPresent());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                "reference.owner.invalid".equals(diagnostic.messageKey())));
    }

    private static void assertSnapshotMismatch(
            RawDefinitionSet current,
            RawDefinitionSet symbolSource) {
        ReferenceResolutionResult result = assertDoesNotThrow(
                () -> new ReferenceResolver().resolve(
                        current,
                        ReferenceTestFixture.symbols(symbolSource)));
        assertEquals(ReferenceResolutionStatus.FAILED, result.status());
        assertFalse(result.resolvedReferences().isPresent());
        assertEquals(1, result.diagnostics().size());
        Diagnostic diagnostic = result.diagnostics().get(0);
        assertEquals("reference.input.snapshot-mismatch", diagnostic.messageKey());
    }

    private static RawDefinitionSet informationTargetDefinitions(String target) {
        RawDefinitionSet original = ReferenceTestFixture.legalDefinitions();
        if ("user.active.extra".equals(target)) {
            original = replaceDefinition(
                    original,
                    7,
                    withName(original.definitions().get(7), "active.extra"));
        }
        RawDefinition directory = original.definitions().get(9);
        Map<String, String> attributes = new LinkedHashMap<String, String>(
                directory.attributes());
        attributes.put("information-ref", target);
        RawReference reference = new RawReference(
                "@information-ref",
                target,
                directory.sourceRef());
        RawDefinition changed = copy(
                directory,
                directory.sourceOrdinal(),
                directory.kind(),
                directory.sourceRef(),
                directory.ownerToken(),
                directory.name(),
                attributes,
                Collections.singletonList(reference),
                directory.body());
        return replaceDefinition(original, 9, changed);
    }

    private static RawDefinitionSet systemBodyDefinitions(
            String declarationName,
            String attributeName,
            String attributeValue) {
        RawDefinitionSet original = ReferenceTestFixture.legalDefinitions();
        RawDefinition system = original.definitions().get(6);
        Map<String, String> declarationAttributes = new LinkedHashMap<String, String>();
        if (attributeName != null) {
            declarationAttributes.put(attributeName, attributeValue);
        }
        RawNodeBody declaration = body(
                declarationName,
                declarationAttributes,
                ref("systems-malformed.xml", 12));
        String containerName = "data-ref".equals(declarationName)
                ? "data-info" : "view-info";
        RawNodeBody container = new RawNodeBody(
                containerName,
                attrs(),
                Optional.<String>empty(),
                Collections.singletonList(declaration),
                ref("systems-malformed.xml", 11));
        RawNodeBody systemBody = new RawNodeBody(
                "system",
                system.body().attributes(),
                Optional.<String>empty(),
                Collections.singletonList(container),
                system.sourceRef());
        RawDefinition changed = copy(
                system,
                system.sourceOrdinal(),
                system.kind(),
                system.sourceRef(),
                system.ownerToken(),
                system.name(),
                system.attributes(),
                Collections.<RawReference>emptyList(),
                systemBody);
        return replaceDefinition(original, 6, changed);
    }

    private static RawDefinitionSet complexityDefinitions(
            int candidateOwners,
            int referenceCount) {
        List<RawDefinition> values = new ArrayList<RawDefinition>();
        long ordinal = 0L;
        for (int index = 0; index < candidateOwners; index++) {
            String scope = "candidate-" + index;
            values.add(definition(
                    RawDefinitionKind.BUSINESS_SCOPE,
                    ordinal++,
                    null,
                    scope,
                    attrs("name", scope),
                    Collections.<RawReference>emptyList(),
                    body("business-config", attrs("name", scope),
                            ref("candidate-" + index + ".xml", 1))));
            values.add(definition(
                    RawDefinitionKind.DIRECTORY,
                    ordinal++,
                    scope,
                    "shared",
                    attrs("name", "shared"),
                    Collections.<RawReference>emptyList(),
                    body("directory", attrs("name", "shared"),
                            ref("candidate-" + index + ".xml", 2))));
        }
        for (int index = 0; index < referenceCount; index++) {
            String scope = "source-" + index;
            values.add(definition(
                    RawDefinitionKind.BUSINESS_SCOPE,
                    ordinal++,
                    null,
                    scope,
                    attrs("name", scope),
                    Collections.<RawReference>emptyList(),
                    body("business-config", attrs("name", scope),
                            ref("source-" + index + ".xml", 1))));
            SourceRef sourceRef = ref("source-" + index + ".xml", 2);
            values.add(definition(
                    RawDefinitionKind.DIRECTORY,
                    ordinal++,
                    scope,
                    "origin",
                    attrs("name", "origin"),
                    Collections.singletonList(new RawReference(
                            "/subdirectory-info/subdirectory@rel",
                            "shared",
                            sourceRef)),
                    body("directory", attrs("name", "origin"), sourceRef)));
        }
        return new RawDefinitionSet(values);
    }

    /** 通过反射使用 I002 的 package-private 计数 seam，使 RED 不依赖缺类编译。 */
    private static ReferenceResolver resolverWithLookupCounter(
            final AtomicInteger counter) {
        Class<?> observerType = null;
        for (Class<?> nested : ReferenceResolver.class.getDeclaredClasses()) {
            if ("LookupObserver".equals(nested.getSimpleName())) {
                observerType = nested;
                break;
            }
        }
        assertNotNull(observerType, "I002 必须提供 LookupObserver 计数 seam");
        final Class<?> requiredType = observerType;
        Object observer = Proxy.newProxyInstance(
                requiredType.getClassLoader(),
                new Class<?>[] { requiredType },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if ("onLexicalLookup".equals(method.getName())) {
                            counter.incrementAndGet();
                        }
                        return null;
                    }
                });
        try {
            Constructor<ReferenceResolver> constructor =
                    ReferenceResolver.class.getDeclaredConstructor(requiredType);
            constructor.setAccessible(true);
            return constructor.newInstance(observer);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("I002 LookupObserver 构造 seam 不可用", failure);
        }
    }

    private static RawDefinitionSet replaceDefinition(
            RawDefinitionSet original,
            int ordinal,
            RawDefinition replacement) {
        List<RawDefinition> values = new ArrayList<RawDefinition>(
                original.definitions());
        values.set(ordinal, replacement);
        return new RawDefinitionSet(values);
    }

    private static RawDefinition withAttribute(
            RawDefinition original,
            String name,
            String value) {
        Map<String, String> attributes = new LinkedHashMap<String, String>(
                original.attributes());
        attributes.put(name, value);
        return copy(
                original,
                original.sourceOrdinal(),
                original.kind(),
                original.sourceRef(),
                original.ownerToken(),
                original.name(),
                attributes,
                original.references(),
                original.body());
    }

    private static RawDefinition withName(
            RawDefinition original,
            String name) {
        Map<String, String> attributes = new LinkedHashMap<String, String>(
                original.attributes());
        attributes.put("name", name);
        return copy(
                original,
                original.sourceOrdinal(),
                original.kind(),
                original.sourceRef(),
                original.ownerToken(),
                Optional.of(name),
                attributes,
                original.references(),
                original.body());
    }

    private static RawDefinition copy(
            RawDefinition original,
            long ordinal,
            RawDefinitionKind kind,
            SourceRef sourceRef,
            Optional<String> owner,
            Optional<String> name,
            Map<String, String> attributes,
            List<RawReference> references,
            RawNodeBody body) {
        return new RawDefinition(
                kind,
                ordinal,
                sourceRef,
                owner,
                name,
                attributes,
                references,
                body,
                original.format(),
                original.schemaVersion());
    }

    private static RawDefinition definition(
            RawDefinitionKind kind,
            long ordinal,
            String owner,
            String name,
            Map<String, String> attributes,
            List<RawReference> references,
            RawNodeBody body) {
        return new RawDefinition(
                kind,
                ordinal,
                body.sourceRef(),
                owner == null ? Optional.<String>empty() : Optional.of(owner),
                name == null ? Optional.<String>empty() : Optional.of(name),
                attributes,
                references,
                body,
                DocumentFormat.XML,
                "1.0");
    }

    private static RawNodeBody body(
            String name,
            Map<String, String> attributes,
            SourceRef sourceRef) {
        return new RawNodeBody(
                name,
                attributes,
                Optional.<String>empty(),
                Collections.<RawNodeBody>emptyList(),
                sourceRef);
    }

    private static Map<String, String> attrs(String... values) {
        Map<String, String> attributes = new LinkedHashMap<String, String>();
        for (int index = 0; index < values.length; index += 2) {
            attributes.put(values[index], values[index + 1]);
        }
        return attributes;
    }

    private static SourceRef ref(String sourceId, int line) {
        return new SourceRef(sourceId, line, 1, "/definition[" + line + "]");
    }
}
