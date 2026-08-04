package dec.core.compiler.modelaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.CanonicalDocumentNode;
import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.raw.RawBuildResult;
import dec.core.compiler.raw.RawBuildStatus;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.raw.RawDefinitionBuilder;
import dec.core.compiler.symbol.SymbolBuildResult;
import dec.core.compiler.symbol.SymbolBuildStatus;
import dec.core.compiler.symbol.SymbolTable;
import dec.core.compiler.symbol.SymbolTableBuilder;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.SourceRef;
import dec.core.context.model.SystemKey;
import dec.core.context.model.ViewKey;
import java.lang.reflect.Field;
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
 * TASK-P1-T10 独立 Review：真实 Canonical 路径、精确边界与值语义。
 */
class ModelAccessIndependentReviewTest {

    /** 真实 Canonical → T06 → T07 → T10 必须识别 view-ref@ref。 */
    @Test
    void compilesRealCanonicalModelAccessPath() {
        RawDefinitionSet definitions = canonicalDefinitions();
        ModelAccessCompilationResult result = new ModelAccessCompiler().compile(
                definitions,
                symbols(definitions));

        assertEquals(ModelAccessCompilationStatus.COMPILED, result.status(),
                result.diagnostics().toString());
        ModelAccessCompilation compilation = result.compilation().orElse(null);
        assertNotNull(compilation);
        assertEquals(2, compilation.bindings().size());
        assertEquals(TargetPropertyPath.Kind.PROPERTY_PATH,
                compilation.bindings().get(0).resolvedTarget().kind());
        assertEquals(TargetPropertyPath.Kind.TARGET_MAIN,
                compilation.bindings().get(1).resolvedTarget().kind());
    }

    /** 通配 WRITE 必须与任意具体 WRITE 重叠。 */
    @Test
    void rejectsWildcardWriteOverlap() {
        assertFailedWith(
                ModelAccessTestFixture.wildcardOverlappingWrite(),
                "modelaccess.write.overlap");
    }

    /** selector 不得在其它已声明 View 中回退搜索。 */
    @Test
    void rejectsCrossViewSelectorFallback() {
        assertFailedWith(
                ModelAccessTestFixture.crossViewFallbackCandidate(),
                "modelaccess.selector.not-found");
    }

    /** SourceRef 不同不能掩盖语义重复 Binding。 */
    @Test
    void rejectsSemanticDuplicateAcrossDifferentSourceRefs() {
        assertFailedWith(
                ModelAccessTestFixture.duplicateReferenceWithDifferentSources(),
                "modelaccess.binding.duplicate");
    }

    /** 调用方绕过 T06 提供非法 path 时也必须 fail-closed。 */
    @Test
    void rejectsMalformedPathWithoutLeakingInputException() {
        assertFailedWith(
                ModelAccessTestFixture.malformedSourcePath(),
                "modelaccess.structure.invalid");
    }

    /** Comparable 必须与包含 SourceRef 的 equals 值语义保持一致。 */
    @Test
    void keepsBindingComparisonConsistentWithValueSemantics() {
        ModelAccessBinding first = binding(new SourceRef(
                "systems.xml", 10, 1, "/systems/ref[1]"));
        ModelAccessBinding second = binding(new SourceRef(
                "systems.xml", 11, 1, "/systems/ref[2]"));

        assertFalse(first.equals(second));
        assertTrue(first.compareTo(second) != 0);
        assertFalse(first.toString().equals(second.toString()));
    }

    /** 公共值对象不可变，且不得暴露运行时执行、查询或缓存入口。 */
    @Test
    void keepsModelAccessTypesImmutableWithoutRuntimeState() {
        Class<?>[] types = {
            ModelAccessBinding.class,
            ModelAccessCompilation.class,
            ModelAccessCompilationResult.class,
            ModelAccessResolution.class,
            SharedModelPath.class,
            SystemViewSelector.class,
            TargetPropertyPath.class
        };
        for (Class<?> type : types) {
            assertTrue(Modifier.isFinal(type.getModifiers()), type.getName());
            for (Field field : type.getDeclaredFields()) {
                if (field.isSynthetic() || field.getName().startsWith("$jacoco")) {
                    continue;
                }
                assertTrue(Modifier.isFinal(field.getModifiers()),
                        type.getSimpleName() + "." + field.getName());
            }
            for (Method method : type.getMethods()) {
                String name = method.getName().toLowerCase();
                assertTrue(!(name.contains("execute")
                                || name.contains("query")
                                || name.contains("sql")
                                || name.contains("cache")
                                || name.contains("current")),
                        type.getSimpleName() + "." + method.getName());
            }
        }
    }

    /** 构造字段完整、仅 SourceRef 不同的 Binding。 */
    private static ModelAccessBinding binding(SourceRef sourceRef) {
        return new ModelAccessBinding(
                new SystemKey("payment"),
                new ViewKey("OrderInfo"),
                new SharedModelPath("payInfo"),
                AccessMode.READ,
                new ViewKey("OrderInfo"),
                new SystemViewSelector("payInfo"),
                TargetPropertyPath.propertyPath("payInfo"),
                sourceRef);
    }

    /** 执行失败断言并核对稳定 messageKey。 */
    private static void assertFailedWith(
            RawDefinitionSet definitions,
            String messageKey) {
        ModelAccessCompilationResult result = new ModelAccessCompiler().compile(
                definitions,
                symbols(definitions));
        assertEquals(ModelAccessCompilationStatus.FAILED, result.status());
        assertFalse(result.compilation().isPresent());
        List<Diagnostic> diagnostics = result.diagnostics();
        assertTrue(diagnostics.stream().anyMatch(diagnostic ->
                messageKey.equals(diagnostic.messageKey())),
                diagnostics.toString());
    }

    /** 通过 T07 构造与 Raw 输入绑定的 SymbolTable。 */
    private static SymbolTable symbols(RawDefinitionSet definitions) {
        SymbolBuildResult result = new SymbolTableBuilder().build(definitions);
        assertEquals(SymbolBuildStatus.BUILT, result.status(),
                result.diagnostics().toString());
        return result.symbolTable().get();
    }

    /** 构造真实 Canonical ModelAccess 文档并通过 T06。 */
    private static RawDefinitionSet canonicalDefinitions() {
        CanonicalDocumentNode orderView = node(
                "order-view.xml",
                "/orm-view-mapping",
                "orm-view-mapping",
                attrs(),
                node("order-view.xml", "/orm-view-mapping/view", "view",
                        attrs("name", "OrderInfo", "target-main", "order"),
                        node("order-view.xml",
                                "/orm-view-mapping/view/property-info",
                                "property-info",
                                attrs(),
                                node("order-view.xml",
                                        "/orm-view-mapping/view/property-info/property",
                                        "property",
                                        attrs("name", "payInfo"),
                                        node("order-view.xml",
                                                "/orm-view-mapping/view/property-info/property/property",
                                                "property",
                                                attrs("name", "payDetailList"))))));
        CanonicalDocumentNode userView = node(
                "user-view.xml",
                "/orm-view-mapping",
                "orm-view-mapping",
                attrs(),
                node("user-view.xml", "/orm-view-mapping/view", "view",
                        attrs("name", "UserInfo", "target-main", "user"),
                        node("user-view.xml",
                                "/orm-view-mapping/view/property-info",
                                "property-info",
                                attrs(),
                                node("user-view.xml",
                                        "/orm-view-mapping/view/property-info/property",
                                        "property",
                                        attrs("name", "user")))));
        CanonicalDocumentNode nestedRef = node(
                "systems.xml",
                "/systems/system/model-access-info/model-access/read[1]/ref",
                "ref",
                attrs("view", "OrderInfo",
                        "property", "payInfo.payDetailList"));
        CanonicalDocumentNode mainRef = node(
                "systems.xml",
                "/systems/system/model-access-info/model-access/read[2]/ref",
                "ref",
                attrs("view", "UserInfo", "property", "user"));
        CanonicalDocumentNode modelAccess = node(
                "systems.xml",
                "/systems/system/model-access-info/model-access",
                "model-access",
                attrs("model-ref", "OrderInfo"),
                node("systems.xml",
                        "/systems/system/model-access-info/model-access/read[1]",
                        "read",
                        attrs("path", "payInfo.payDetailList"),
                        nestedRef),
                node("systems.xml",
                        "/systems/system/model-access-info/model-access/read[2]",
                        "read",
                        attrs("path", "user"),
                        mainRef));
        CanonicalDocumentNode system = node(
                "systems.xml",
                "/systems/system",
                "system",
                attrs("name", "payment"),
                node("systems.xml", "/systems/system/data-info",
                        "data-info", attrs()),
                node("systems.xml", "/systems/system/view-info",
                        "view-info", attrs(),
                        node("systems.xml",
                                "/systems/system/view-info/view-ref[1]",
                                "view-ref", attrs("ref", "OrderInfo")),
                        node("systems.xml",
                                "/systems/system/view-info/view-ref[2]",
                                "view-ref", attrs("ref", "UserInfo"))),
                node("systems.xml", "/systems/system/rule-file-info",
                        "rule-file-info", attrs()),
                node("systems.xml", "/systems/system/information-info",
                        "information-info", attrs()),
                node("systems.xml", "/systems/system/model-access-info",
                        "model-access-info", attrs(), modelAccess));
        CanonicalDocumentNode systems = node(
                "systems.xml",
                "/systems",
                "systems",
                attrs(),
                system);
        RawBuildResult result = new RawDefinitionBuilder().build(
                Arrays.asList(orderView, userView, systems));
        assertEquals(RawBuildStatus.BUILT, result.status(),
                result.diagnostics().toString());
        return result.rawDefinitionSet().get();
    }

    /** 构造格式中立 Canonical 节点。 */
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

    /** 构造保持输入顺序的属性映射。 */
    private static Map<String, String> attrs(String... values) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        for (int index = 0; index < values.length; index += 2) {
            result.put(values[index], values[index + 1]);
        }
        return result;
    }
}
