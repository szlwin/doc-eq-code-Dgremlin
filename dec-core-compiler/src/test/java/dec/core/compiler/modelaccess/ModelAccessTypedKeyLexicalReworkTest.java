package dec.core.compiler.modelaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.CanonicalDocumentNode;
import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.raw.RawBuildResult;
import dec.core.compiler.raw.RawBuildStatus;
import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionBuilder;
import dec.core.compiler.raw.RawDefinitionKind;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.raw.RawReference;
import dec.core.compiler.symbol.SymbolBuildResult;
import dec.core.compiler.symbol.SymbolBuildStatus;
import dec.core.compiler.symbol.SymbolTable;
import dec.core.compiler.symbol.SymbolTableBuilder;
import dec.core.context.model.SourceRef;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T10 / I003：验证 Raw reference lexical 保留与 TypedKey 独立规范化。
 */
class ModelAccessTypedKeyLexicalReworkTest {

    /** padded model-ref 应保留 Raw 原值，并由 ViewKey 规范化后成功编译。 */
    @Test
    void compilesPaddedModelRefAndPreservesRawLexical() {
        Pipeline pipeline = compilePipeline(
                "order",
                "OrderInfo",
                " OrderInfo ",
                "UserInfo",
                "UserInfo",
                "user");

        assertCompiled(pipeline);
        RawDefinition modelAccess = modelAccess(pipeline.definitions);
        assertEquals(" OrderInfo ", modelAccess.name().get());
        assertEquals(" OrderInfo ", modelAccess.attributes().get("model-ref"));
        assertEquals(" OrderInfo ", modelAccess.body().attributes().get("model-ref"));
        assertTrue(hasReferenceTarget(modelAccess.references(), " OrderInfo "));

        ModelAccessBinding binding = binding(pipeline.result);
        assertEquals("OrderInfo", binding.sourceModel().name());
        assertEquals("UserInfo", binding.targetView().name());
    }

    /** padded ref@view 应保留 Raw 原值，并由 ViewKey 规范化后成功编译。 */
    @Test
    void compilesPaddedTargetViewAndPreservesRawLexical() {
        Pipeline pipeline = compilePipeline(
                "order",
                "OrderInfo",
                "OrderInfo",
                "UserInfo",
                " UserInfo ",
                "user");

        assertCompiled(pipeline);
        RawDefinition modelAccess = modelAccess(pipeline.definitions);
        assertTrue(hasReferenceTarget(modelAccess.references(), " UserInfo "));
        ModelAccessBinding binding = binding(pipeline.result);
        assertEquals("OrderInfo", binding.sourceModel().name());
        assertEquals("UserInfo", binding.targetView().name());
    }

    /** padded System view-ref@ref 与未填充 ModelAccess ref 应解析到同一 ViewKey。 */
    @Test
    void compilesPaddedSystemViewDeclarationWithPlainModelAccessRef() {
        Pipeline pipeline = compilePipeline(
                "order",
                " UserInfo ",
                "OrderInfo",
                "UserInfo",
                "UserInfo",
                "user");

        assertCompiled(pipeline);
        assertEquals("UserInfo", binding(pipeline.result).targetView().name());
    }

    /** padded System name 与 padded ModelAccess reference 应分别规范化后对齐。 */
    @Test
    void compilesPaddedSystemAndPaddedModelAccessReferences() {
        Pipeline pipeline = compilePipeline(
                " order ",
                " UserInfo ",
                " OrderInfo ",
                "UserInfo",
                " UserInfo ",
                "user");

        assertCompiled(pipeline);
        ModelAccessBinding binding = binding(pipeline.result);
        assertEquals("order", binding.ownerSystem().name());
        assertEquals("OrderInfo", binding.sourceModel().name());
        assertEquals("UserInfo", binding.targetView().name());
    }

    /** ref@property 是精确 selector，前后空格不得由 TypedKey 规则修复。 */
    @Test
    void rejectsPaddedPropertySelector() {
        Pipeline pipeline = compilePipeline(
                "order",
                "UserInfo",
                "OrderInfo",
                "UserInfo",
                "UserInfo",
                " user ");

        assertFailedStructure(pipeline);
    }

    /** read/write@path 是精确路径，前后空格必须继续失败。 */
    @Test
    void rejectsPaddedSourcePath() {
        Pipeline pipeline = compilePipeline(
                "order",
                "UserInfo",
                "OrderInfo",
                "UserInfo",
                "UserInfo",
                "user",
                " user ");

        assertFailedStructure(pipeline);
    }

    /** blank model-ref 必须在 Raw reference 验证阶段失败。 */
    @Test
    void rejectsBlankModelRef() {
        RawBuildResult result = buildRaw(
                "order",
                "UserInfo",
                "   ",
                "UserInfo",
                "UserInfo",
                "user",
                "user");

        assertEquals(RawBuildStatus.FAILED, result.status());
        assertFalse(result.rawDefinitionSet().isPresent());
    }

    /** blank ref@view 必须在 Raw reference 验证阶段失败。 */
    @Test
    void rejectsBlankTargetView() {
        RawBuildResult result = buildRaw(
                "order",
                "UserInfo",
                "OrderInfo",
                "UserInfo",
                "   ",
                "user",
                "user");

        assertEquals(RawBuildStatus.FAILED, result.status());
        assertFalse(result.rawDefinitionSet().isPresent());
    }

    /** 原始 definition.name 与 model-ref 不一致时仍必须 fail-closed。 */
    @Test
    void rejectsRawNameAndModelRefMismatch() {
        Pipeline baseline = compilePipeline(
                "order",
                "UserInfo",
                "OrderInfo",
                "UserInfo",
                "UserInfo",
                "user");
        List<RawDefinition> values = new ArrayList<RawDefinition>(
                baseline.definitions.definitions());
        for (int index = 0; index < values.size(); index++) {
            RawDefinition definition = values.get(index);
            if (definition.kind() == RawDefinitionKind.MODEL_ACCESS) {
                values.set(index, new RawDefinition(
                        definition.kind(),
                        definition.sourceOrdinal(),
                        definition.sourceRef(),
                        definition.ownerToken(),
                        Optional.of("DifferentRawLexical"),
                        definition.attributes(),
                        definition.references(),
                        definition.body(),
                        definition.format(),
                        definition.schemaVersion()));
            }
        }
        RawDefinitionSet changed = new RawDefinitionSet(values);
        SymbolBuildResult symbols = new SymbolTableBuilder().build(changed);
        assertEquals(SymbolBuildStatus.BUILT, symbols.status(),
                symbols.diagnostics().toString());
        ModelAccessCompilationResult result = new ModelAccessCompiler().compile(
                changed,
                symbols.symbolTable().get());
        assertEquals(ModelAccessCompilationStatus.FAILED, result.status());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                "modelaccess.structure.invalid".equals(diagnostic.messageKey())));
    }

    /** 执行完整 Raw、Symbol 与 T10 pipeline。 */
    private static Pipeline compilePipeline(
            String systemName,
            String declaredTargetView,
            String modelRef,
            String targetViewName,
            String targetViewRef,
            String property) {
        return compilePipeline(
                systemName,
                declaredTargetView,
                modelRef,
                targetViewName,
                targetViewRef,
                property,
                "user");
    }

    /** 执行可指定 source path 的完整 pipeline。 */
    private static Pipeline compilePipeline(
            String systemName,
            String declaredTargetView,
            String modelRef,
            String targetViewName,
            String targetViewRef,
            String property,
            String sourcePath) {
        RawBuildResult raw = buildRaw(
                systemName,
                declaredTargetView,
                modelRef,
                targetViewName,
                targetViewRef,
                property,
                sourcePath);
        assertEquals(RawBuildStatus.BUILT, raw.status(),
                raw.diagnostics().toString());
        RawDefinitionSet definitions = raw.rawDefinitionSet().get();
        SymbolBuildResult symbolResult = new SymbolTableBuilder().build(definitions);
        assertEquals(SymbolBuildStatus.BUILT, symbolResult.status(),
                symbolResult.diagnostics().toString());
        SymbolTable symbols = symbolResult.symbolTable().get();
        return new Pipeline(
                definitions,
                new ModelAccessCompiler().compile(definitions, symbols));
    }

    /** 构造真实 Canonical 文档并执行 T06 RawDefinitionBuilder。 */
    private static RawBuildResult buildRaw(
            String systemName,
            String declaredTargetView,
            String modelRef,
            String targetViewName,
            String targetViewRef,
            String property,
            String sourcePath) {
        CanonicalDocumentNode sourceView = view("OrderInfo", "order");
        CanonicalDocumentNode targetView = view(targetViewName, "user");
        CanonicalDocumentNode views = node(
                "view.xml",
                "/orm-view-mapping",
                "orm-view-mapping",
                attrs(),
                sourceView,
                targetView);

        CanonicalDocumentNode declaration = node(
                "systems.xml",
                "/systems/system/view-info/view-ref",
                "view-ref",
                attrs("ref", declaredTargetView));
        CanonicalDocumentNode ref = node(
                "systems.xml",
                "/systems/system/model-access-info/model-access/read/ref",
                "ref",
                attrs("view", targetViewRef, "property", property));
        CanonicalDocumentNode read = node(
                "systems.xml",
                "/systems/system/model-access-info/model-access/read",
                "read",
                attrs("path", sourcePath),
                ref);
        CanonicalDocumentNode modelAccess = node(
                "systems.xml",
                "/systems/system/model-access-info/model-access",
                "model-access",
                attrs("model-ref", modelRef),
                read);
        CanonicalDocumentNode system = node(
                "systems.xml",
                "/systems/system",
                "system",
                attrs("name", systemName),
                node("systems.xml", "/systems/system/view-info", "view-info",
                        attrs(), declaration),
                node("systems.xml", "/systems/system/model-access-info",
                        "model-access-info", attrs(), modelAccess));
        CanonicalDocumentNode systems = node(
                "systems.xml",
                "/systems",
                "systems",
                attrs(),
                system);

        return new RawDefinitionBuilder().build(Arrays.asList(views, systems));
    }

    /** 构造带 target-main 与同名 property 的合法 View。 */
    private static CanonicalDocumentNode view(String name, String targetMain) {
        CanonicalDocumentNode property = node(
                "view.xml",
                "/orm-view-mapping/view/property-info/property",
                "property",
                attrs("name", targetMain));
        return node(
                "view.xml",
                "/orm-view-mapping/view",
                "view",
                attrs("name", name, "target-main", targetMain),
                node("view.xml", "/orm-view-mapping/view/property-info",
                        "property-info", attrs(), property));
    }

    /** 断言 T10 成功并发布唯一 Binding。 */
    private static void assertCompiled(Pipeline pipeline) {
        assertEquals(ModelAccessCompilationStatus.COMPILED,
                pipeline.result.status(), pipeline.result.diagnostics().toString());
        assertTrue(pipeline.result.compilation().isPresent());
        assertEquals(1, pipeline.result.compilation().get().bindings().size());
    }

    /** 断言精确 lexical 结构失败。 */
    private static void assertFailedStructure(Pipeline pipeline) {
        assertEquals(ModelAccessCompilationStatus.FAILED,
                pipeline.result.status());
        assertFalse(pipeline.result.compilation().isPresent());
        assertTrue(pipeline.result.diagnostics().stream().anyMatch(diagnostic ->
                "modelaccess.structure.invalid".equals(diagnostic.messageKey())));
    }

    /** 返回唯一 Binding。 */
    private static ModelAccessBinding binding(ModelAccessCompilationResult result) {
        return result.compilation().get().bindings().get(0);
    }

    /** 返回唯一 ModelAccess RawDefinition。 */
    private static RawDefinition modelAccess(RawDefinitionSet definitions) {
        List<RawDefinition> values = definitions.definitions(
                RawDefinitionKind.MODEL_ACCESS);
        assertEquals(1, values.size());
        return values.get(0);
    }

    /** 查询 Raw reference 是否保留指定 lexical target。 */
    private static boolean hasReferenceTarget(
            List<RawReference> references,
            String target) {
        return references.stream().anyMatch(reference ->
                target.equals(reference.target()));
    }

    /** 创建保持输入 lexical 的不可变 Canonical 节点。 */
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
                new SourceRef(sourceId, 1, 1, path),
                DocumentFormat.XML,
                "1.0");
    }

    /** 创建保持插入顺序的属性 Map。 */
    private static Map<String, String> attrs(String... values) {
        Map<String, String> attributes = new LinkedHashMap<String, String>();
        for (int index = 0; index < values.length; index += 2) {
            attributes.put(values[index], values[index + 1]);
        }
        return Collections.unmodifiableMap(attributes);
    }

    /** 冻结一次完整 pipeline 的 Raw 与 T10 结果。 */
    private static final class Pipeline {
        private final RawDefinitionSet definitions;
        private final ModelAccessCompilationResult result;

        private Pipeline(
                RawDefinitionSet definitions,
                ModelAccessCompilationResult result) {
            this.definitions = definitions;
            this.result = result;
        }
    }
}
