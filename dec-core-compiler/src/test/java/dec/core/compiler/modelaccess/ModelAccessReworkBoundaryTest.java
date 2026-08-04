package dec.core.compiler.modelaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.CanonicalDocumentNode;
import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.raw.RawBuildResult;
import dec.core.compiler.raw.RawBuildStatus;
import dec.core.compiler.raw.RawDefinitionBuilder;
import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.raw.RawNodeBody;
import dec.core.compiler.symbol.SymbolBuildResult;
import dec.core.compiler.symbol.SymbolBuildStatus;
import dec.core.compiler.symbol.SymbolTable;
import dec.core.compiler.symbol.SymbolTableBuilder;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.SourceRef;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T10 / I002 阻断性边界 Oracle。
 */
class ModelAccessReworkBoundaryTest {

    /** 星号只能作为完整 SharedModelPath，任何嵌入式星号都必须失败。 */
    @Test
    void rejectsEmbeddedWildcardSegments() {
        String[] invalid = {"a.*", "*.a", "a.*.b", "*.*"};
        for (String path : invalid) {
            RawDefinitionSet definitions = withModelAccessPath(
                    canonicalDefinitions(singleSection(property("value")),
                            "value", "main"),
                    path);
            assertStructureRejected(definitions, path);
        }
    }

    /** 非法 wildcard 与具体路径组合不得绕过重叠门禁并发布结果。 */
    @Test
    void rejectsEmbeddedWildcardOverlapBypass() {
        assertStructureRejected(
                withWritePaths("a.*", "a.b"),
                "a.* + a.b");
        assertStructureRejected(
                withWritePaths("*.a", "x.a"),
                "*.a + x.a");
    }

    /** 目标只位于第二个 property-info 时必须成功解析。 */
    @Test
    void resolvesPropertyFromSecondSection() {
        RawDefinitionSet definitions = canonicalDefinitions(
                sections(
                        propertyInfo(property("first")),
                        propertyInfo(property("second"))),
                "second",
                "main");
        ModelAccessCompilation compilation = compileSuccess(definitions);
        assertEquals(1, compilation.bindings().size());
        assertEquals("second",
                compilation.bindings().get(0).resolvedTarget().value());
    }

    /** 跨 property-info 的同名根候选必须报告歧义。 */
    @Test
    void rejectsAmbiguousPropertyAcrossSections() {
        RawDefinitionSet definitions = canonicalDefinitions(
                sections(
                        propertyInfo(property("dup")),
                        propertyInfo(property("dup"))),
                "dup",
                "main");
        assertFailedWith(definitions, "modelaccess.selector.ambiguous");
    }

    /** 空的首个 property-info 不得屏蔽后续合法 section。 */
    @Test
    void resolvesAfterEmptyFirstSection() {
        RawDefinitionSet definitions = canonicalDefinitions(
                sections(propertyInfo(), propertyInfo(property("second"))),
                "second",
                "main");
        assertEquals(1, compileSuccess(definitions).bindings().size());
    }

    /** 多 section 根候选命中后仍应解析该 property 的嵌套路径。 */
    @Test
    void resolvesNestedPropertyFromLaterSection() {
        RawDefinitionSet definitions = canonicalDefinitions(
                sections(
                        propertyInfo(property("first")),
                        propertyInfo(property("outer", property("inner")))),
                "outer.inner",
                "main");
        ModelAccessBinding binding = compileSuccess(definitions)
                .bindings().get(0);
        assertEquals(TargetPropertyPath.Kind.PROPERTY_PATH,
                binding.resolvedTarget().kind());
        assertEquals("outer.inner", binding.resolvedTarget().value());
    }

    /** target-main 必须继续优先于所有 property-info section。 */
    @Test
    void keepsTargetMainPriorityAcrossSections() {
        RawDefinitionSet definitions = canonicalDefinitions(
                sections(
                        propertyInfo(property("main")),
                        propertyInfo(property("main"))),
                "main",
                "main");
        ModelAccessBinding binding = compileSuccess(definitions)
                .bindings().get(0);
        assertEquals(TargetPropertyPath.Kind.TARGET_MAIN,
                binding.resolvedTarget().kind());
    }

    /** 错误 ModelAccess root 必须在 resolver 前失败。 */
    @Test
    void rejectsWrongModelAccessRootBeforeResolver() {
        assertRootRejected(copyModelAccess(
                validRawDefinitions(),
                CopyChange.WRONG_ROOT));
    }

    /** 缺失 model-ref 不得从 definition.name 回退。 */
    @Test
    void rejectsMissingModelRefBeforeResolver() {
        assertRootRejected(copyModelAccess(
                validRawDefinitions(),
                CopyChange.MISSING_MODEL_REF));
    }

    /** definition.name 与 model-ref lexical 不一致必须失败。 */
    @Test
    void rejectsNameAndModelRefMismatchBeforeResolver() {
        assertRootRejected(copyModelAccess(
                validRawDefinitions(),
                CopyChange.NAME_MISMATCH));
    }

    /** definition/body attributes 不一致必须失败。 */
    @Test
    void rejectsDefinitionBodyAttributeMismatchBeforeResolver() {
        assertRootRejected(copyModelAccess(
                validRawDefinitions(),
                CopyChange.BODY_ATTRIBUTE_MISMATCH));
    }

    /** root、access、ref 的 scalar 或额外非法结构必须失败。 */
    @Test
    void rejectsScalarAndExtraStructure() {
        CopyChange[] changes = {
            CopyChange.ROOT_SCALAR,
            CopyChange.ACCESS_SCALAR,
            CopyChange.REF_SCALAR,
            CopyChange.REF_CHILD,
            CopyChange.ACCESS_EXTRA_ATTRIBUTE,
            CopyChange.REF_EXTRA_ATTRIBUTE
        };
        for (CopyChange change : changes) {
            assertRootRejected(copyModelAccess(validRawDefinitions(), change));
        }
    }

    /** 编译成功并返回完整结果。 */
    private static ModelAccessCompilation compileSuccess(
            RawDefinitionSet definitions) {
        ModelAccessCompilationResult result = new ModelAccessCompiler().compile(
                definitions,
                symbols(definitions));
        assertEquals(ModelAccessCompilationStatus.COMPILED, result.status(),
                result.diagnostics().toString());
        ModelAccessCompilation compilation = result.compilation().orElse(null);
        assertNotNull(compilation);
        return compilation;
    }

    /** 断言稳定失败且不发布部分 Compilation。 */
    private static void assertFailedWith(
            RawDefinitionSet definitions,
            String messageKey) {
        ModelAccessCompilationResult result = new ModelAccessCompiler().compile(
                definitions,
                symbols(definitions));
        assertEquals(ModelAccessCompilationStatus.FAILED, result.status());
        assertFalse(result.compilation().isPresent());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                messageKey.equals(diagnostic.messageKey())),
                result.diagnostics().toString());
    }

    /** 断言非法 path 转为结构 Diagnostic。 */
    private static void assertStructureRejected(
            RawDefinitionSet definitions,
            String label) {
        ModelAccessCompilationResult result = new ModelAccessCompiler().compile(
                definitions,
                symbols(definitions));
        assertEquals(ModelAccessCompilationStatus.FAILED, result.status(), label);
        assertFalse(result.compilation().isPresent(), label);
        assertTrue(hasMessage(result.diagnostics(),
                        "modelaccess.structure.invalid"),
                label + " -> " + result.diagnostics());
    }

    /** 断言根结构失败先于 resolver，且不发布任何结果。 */
    private static void assertRootRejected(RawDefinitionSet definitions) {
        AtomicInteger resolverCalls = new AtomicInteger();
        ModelAccessSelectorResolver resolver = (owner, sourcePath, targetView,
                selector, symbols) -> {
            resolverCalls.incrementAndGet();
            return ModelAccessResolution.resolved(
                    TargetPropertyPath.propertyPath(selector.value()));
        };
        ModelAccessCompilationResult result = new ModelAccessCompiler(resolver)
                .compile(definitions, symbols(definitions));

        assertEquals(ModelAccessCompilationStatus.FAILED, result.status(),
                result.diagnostics().toString());
        assertFalse(result.compilation().isPresent());
        assertTrue(hasMessage(result.diagnostics(),
                "modelaccess.structure.invalid"), result.diagnostics().toString());
        assertEquals(0, resolverCalls.get(), "根结构失败不得调用 resolver");
    }

    /** 查询稳定 Diagnostic messageKey。 */
    private static boolean hasMessage(
            List<Diagnostic> diagnostics,
            String messageKey) {
        return diagnostics.stream().anyMatch(diagnostic ->
                messageKey.equals(diagnostic.messageKey()));
    }

    /** 构造可通过 T06 的标准多 section 输入。 */
    private static RawDefinitionSet canonicalDefinitions(
            List<CanonicalDocumentNode> propertySections,
            String selector,
            String targetMain) {
        CanonicalDocumentNode view = node(
                "view.xml", "/orm-view-mapping", "orm-view-mapping", attrs(),
                node("view.xml", "/orm-view-mapping/view", "view",
                        attrs("name", "Target", "target-main", targetMain),
                        propertySections.toArray(new CanonicalDocumentNode[0])));
        CanonicalDocumentNode ref = node(
                "systems.xml",
                "/systems/system/model-access-info/model-access/read/ref",
                "ref",
                attrs("view", "Target", "property", selector));
        CanonicalDocumentNode modelAccess = node(
                "systems.xml",
                "/systems/system/model-access-info/model-access",
                "model-access",
                attrs("model-ref", "Target"),
                node("systems.xml",
                        "/systems/system/model-access-info/model-access/read",
                        "read",
                        attrs("path", "source"),
                        ref));
        CanonicalDocumentNode system = node(
                "systems.xml", "/systems/system", "system",
                attrs("name", "owner"),
                node("systems.xml", "/systems/system/data-info",
                        "data-info", attrs()),
                node("systems.xml", "/systems/system/view-info",
                        "view-info", attrs(),
                        node("systems.xml",
                                "/systems/system/view-info/view-ref",
                                "view-ref", attrs("ref", "Target"))),
                node("systems.xml", "/systems/system/rule-file-info",
                        "rule-file-info", attrs()),
                node("systems.xml", "/systems/system/information-info",
                        "information-info", attrs()),
                node("systems.xml", "/systems/system/model-access-info",
                        "model-access-info", attrs(), modelAccess));
        CanonicalDocumentNode systems = node(
                "systems.xml", "/systems", "systems", attrs(), system);
        RawBuildResult result = new RawDefinitionBuilder().build(
                Arrays.asList(view, systems));
        assertEquals(RawBuildStatus.BUILT, result.status(),
                result.diagnostics().toString());
        return result.rawDefinitionSet().get();
    }

    /** 把合法 canonical 输入的 ModelAccess path 替换为指定 lexical。 */
    private static RawDefinitionSet withModelAccessPath(
            RawDefinitionSet definitions,
            String path) {
        return replaceModelAccess(definitions, definition -> {
            RawNodeBody access = definition.body().children().get(0);
            RawNodeBody changedAccess = new RawNodeBody(
                    access.name(),
                    attrs("path", path),
                    access.scalar(),
                    access.children(),
                    access.sourceRef());
            RawNodeBody changedRoot = new RawNodeBody(
                    definition.body().name(),
                    definition.body().attributes(),
                    definition.body().scalar(),
                    Collections.singletonList(changedAccess),
                    definition.body().sourceRef());
            return copy(definition,
                    definition.name(),
                    definition.attributes(),
                    changedRoot);
        });
    }

    /** 构造两个 WRITE path，验证非法通配符不能绕过门禁。 */
    private static RawDefinitionSet withWritePaths(
            String first,
            String second) {
        RawDefinitionSet definitions = canonicalDefinitions(
                singleSection(property("value")), "value", "main");
        return replaceModelAccess(definitions, definition -> {
            RawNodeBody firstWrite = new RawNodeBody(
                    "write", attrs("path", first), Optional.<String>empty(),
                    Collections.<RawNodeBody>emptyList(),
                    source("systems.xml", 40));
            RawNodeBody secondWrite = new RawNodeBody(
                    "write", attrs("path", second), Optional.<String>empty(),
                    Collections.<RawNodeBody>emptyList(),
                    source("systems.xml", 41));
            RawNodeBody root = new RawNodeBody(
                    "model-access", definition.attributes(),
                    Optional.<String>empty(),
                    Arrays.asList(firstWrite, secondWrite),
                    definition.sourceRef());
            return copy(definition,
                    definition.name(), definition.attributes(), root);
        });
    }

    /** 返回标准合法 Raw 输入，用于构造绕过 T06 的异常状态。 */
    private static RawDefinitionSet validRawDefinitions() {
        return canonicalDefinitions(singleSection(property("value")),
                "value", "main");
    }

    /** 根据 Finding 类型复制 malformed ModelAccess。 */
    private static RawDefinitionSet copyModelAccess(
            RawDefinitionSet definitions,
            CopyChange change) {
        return replaceModelAccess(definitions, definition -> {
            Map<String, String> definitionAttributes =
                    new LinkedHashMap<String, String>(definition.attributes());
            Optional<String> name = definition.name();
            RawNodeBody root = definition.body();

            switch (change) {
                case WRONG_ROOT:
                    root = nodeBody("not-model-access", root.attributes(),
                            root.scalar(), root.children(), root.sourceRef());
                    break;
                case MISSING_MODEL_REF:
                    definitionAttributes.clear();
                    root = nodeBody(root.name(), definitionAttributes,
                            root.scalar(), root.children(), root.sourceRef());
                    break;
                case NAME_MISMATCH:
                    name = Optional.of("DifferentTarget");
                    break;
                case BODY_ATTRIBUTE_MISMATCH:
                    root = nodeBody(root.name(), attrs("model-ref", "Other"),
                            root.scalar(), root.children(), root.sourceRef());
                    break;
                case ROOT_SCALAR:
                    root = nodeBody(root.name(), root.attributes(),
                            Optional.of("invalid"), root.children(), root.sourceRef());
                    break;
                case ACCESS_SCALAR:
                    root = replaceAccess(root, access -> nodeBody(
                            access.name(), access.attributes(),
                            Optional.of("invalid"), access.children(),
                            access.sourceRef()));
                    break;
                case REF_SCALAR:
                    root = replaceRef(root, ref -> nodeBody(
                            ref.name(), ref.attributes(), Optional.of("invalid"),
                            ref.children(), ref.sourceRef()));
                    break;
                case REF_CHILD:
                    root = replaceRef(root, ref -> nodeBody(
                            ref.name(), ref.attributes(), ref.scalar(),
                            Collections.singletonList(nodeBody(
                                    "illegal", attrs(), Optional.<String>empty(),
                                    Collections.<RawNodeBody>emptyList(),
                                    source("systems.xml", 88))),
                            ref.sourceRef()));
                    break;
                case ACCESS_EXTRA_ATTRIBUTE:
                    root = replaceAccess(root, access -> nodeBody(
                            access.name(), attrs("path", "source", "extra", "x"),
                            access.scalar(), access.children(), access.sourceRef()));
                    break;
                case REF_EXTRA_ATTRIBUTE:
                    root = replaceRef(root, ref -> nodeBody(
                            ref.name(),
                            attrs("view", "Target", "property", "value",
                                    "extra", "x"),
                            ref.scalar(), ref.children(), ref.sourceRef()));
                    break;
                default:
                    throw new IllegalStateException("unexpected change " + change);
            }
            return copy(definition, name, definitionAttributes, root);
        });
    }

    /** 替换 ModelAccess 的首个 access。 */
    private static RawNodeBody replaceAccess(
            RawNodeBody root,
            BodyChange change) {
        List<RawNodeBody> children = new ArrayList<RawNodeBody>(root.children());
        children.set(0, change.apply(children.get(0)));
        return nodeBody(root.name(), root.attributes(), root.scalar(), children,
                root.sourceRef());
    }

    /** 替换首个 access 的首个 ref。 */
    private static RawNodeBody replaceRef(
            RawNodeBody root,
            BodyChange change) {
        return replaceAccess(root, access -> {
            List<RawNodeBody> refs = new ArrayList<RawNodeBody>(access.children());
            refs.set(0, change.apply(refs.get(0)));
            return nodeBody(access.name(), access.attributes(), access.scalar(),
                    refs, access.sourceRef());
        });
    }

    /** 替换定义集中的唯一 ModelAccess。 */
    private static RawDefinitionSet replaceModelAccess(
            RawDefinitionSet definitions,
            DefinitionChange change) {
        List<RawDefinition> values = new ArrayList<RawDefinition>(
                definitions.definitions());
        for (int index = 0; index < values.size(); index++) {
            RawDefinition definition = values.get(index);
            if (definition.kind()
                    == dec.core.compiler.raw.RawDefinitionKind.MODEL_ACCESS) {
                values.set(index, change.apply(definition));
            }
        }
        return new RawDefinitionSet(values);
    }

    /** 复制 ModelAccess 定义。 */
    private static RawDefinition copy(
            RawDefinition definition,
            Optional<String> name,
            Map<String, String> attributes,
            RawNodeBody body) {
        return new RawDefinition(
                definition.kind(), definition.sourceOrdinal(),
                definition.sourceRef(), definition.ownerToken(), name,
                attributes, definition.references(), body,
                definition.format(), definition.schemaVersion());
    }

    /** 通过 T07 构造与当前 Raw 快照绑定的 SymbolTable。 */
    private static SymbolTable symbols(RawDefinitionSet definitions) {
        SymbolBuildResult result = new SymbolTableBuilder().build(definitions);
        assertEquals(SymbolBuildStatus.BUILT, result.status(),
                result.diagnostics().toString());
        return result.symbolTable().get();
    }

    /** 构造一个 property-info section 列表。 */
    private static List<CanonicalDocumentNode> singleSection(
            CanonicalDocumentNode... properties) {
        return sections(propertyInfo(properties));
    }

    /** 构造多个 section。 */
    private static List<CanonicalDocumentNode> sections(
            CanonicalDocumentNode... sections) {
        return Arrays.asList(sections);
    }

    /** 构造 property-info 节点。 */
    private static CanonicalDocumentNode propertyInfo(
            CanonicalDocumentNode... properties) {
        return node("view.xml", "/orm-view-mapping/view/property-info",
                "property-info", attrs(), properties);
    }

    /** 构造递归 property 节点。 */
    private static CanonicalDocumentNode property(
            String name,
            CanonicalDocumentNode... children) {
        return node("view.xml",
                "/orm-view-mapping/view/property-info/property[" + name + "]",
                "property", attrs("name", name), children);
    }

    /** 构造 Canonical 节点。 */
    private static CanonicalDocumentNode node(
            String sourceId,
            String path,
            String name,
            Map<String, String> attributes,
            CanonicalDocumentNode... children) {
        return new CanonicalDocumentNode(
                name, attributes, Optional.<String>empty(),
                Arrays.asList(children), source(sourceId, path.hashCode()),
                DocumentFormat.XML, "1.0");
    }

    /** 构造 Raw body。 */
    private static RawNodeBody nodeBody(
            String name,
            Map<String, String> attributes,
            Optional<String> scalar,
            List<RawNodeBody> children,
            SourceRef sourceRef) {
        return new RawNodeBody(name, attributes, scalar, children, sourceRef);
    }

    /** 构造稳定属性。 */
    private static Map<String, String> attrs(String... values) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        for (int index = 0; index < values.length; index += 2) {
            result.put(values[index], values[index + 1]);
        }
        return result;
    }

    /** 构造稳定来源位置。 */
    private static SourceRef source(String sourceId, int seed) {
        int line = Math.abs(seed % 10000) + 1;
        return new SourceRef(sourceId, line, 1, "/i002[" + line + "]");
    }

    /** RawDefinition 复制函数。 */
    private interface DefinitionChange {
        RawDefinition apply(RawDefinition definition);
    }

    /** RawNodeBody 复制函数。 */
    private interface BodyChange {
        RawNodeBody apply(RawNodeBody body);
    }

    /** malformed 结构类型。 */
    private enum CopyChange {
        WRONG_ROOT,
        MISSING_MODEL_REF,
        NAME_MISMATCH,
        BODY_ATTRIBUTE_MISMATCH,
        ROOT_SCALAR,
        ACCESS_SCALAR,
        REF_SCALAR,
        REF_CHILD,
        ACCESS_EXTRA_ATTRIBUTE,
        REF_EXTRA_ATTRIBUTE
    }
}
