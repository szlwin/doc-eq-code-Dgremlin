package dec.core.compiler.raw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.CanonicalDocumentNode;
import dec.core.compiler.canonical.DocumentFormat;
import dec.core.context.model.SourceRef;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * I002 独立 Review 增补的 reference 优先级、生产预算和公开矩阵边界。
 */
class RawInvariantIndependentReviewTest {

    /**
     * 定义节点自身的空白 reference 必须在第一阶段定位到该定义节点。
     */
    @Test
    void rejectsBlankReferenceOnDefinitionNodeWithExactSourceRef() {
        CanonicalDocumentNode information = node(
                "systems.xml",
                "/systems/system/information-info/information",
                "information",
                attrs("name", "information", "view-ref", "   "));
        CanonicalDocumentNode document = node(
                "systems.xml", "/systems", "systems", attrs(),
                node("systems.xml", "/systems/system", "system",
                        attrs("name", "system"),
                        node("systems.xml", "/systems/system/information-info",
                                "information-info", attrs(), information)));

        assertFailed(
                new RawDefinitionBuilder().build(Collections.singletonList(document)),
                "raw.reference.target.required",
                information.sourceRef());
    }

    /**
     * PRODUCE 的可选空白 ref 按 R24 映射为 absent，不创建 RawReference。
     */
    @Test
    void treatsBlankOptionalProduceReferenceAsAbsent() {
        CanonicalDocumentNode produce = node(
                "business.xml",
                "/business-config/directory-info/directory/action-info/action/produce-info/produce",
                "produce",
                attrs("ref", "   "));
        CanonicalDocumentNode document = node(
                "business.xml", "/business-config", "business-config",
                attrs("name", "business"),
                node("business.xml", "/business-config/directory-info",
                        "directory-info", attrs(),
                        node("business.xml", "/business-config/directory-info/directory",
                                "directory", attrs("name", "directory"),
                                node("business.xml", "/business-config/directory-info/directory/action-info",
                                        "action-info", attrs(),
                                        node("business.xml", "/business-config/directory-info/directory/action-info/action",
                                                "action", attrs("name", "action"),
                                                node("business.xml", "/business-config/directory-info/directory/action-info/action/produce-info",
                                                        "produce-info", attrs(), produce))))));

        RawBuildResult result = new RawDefinitionBuilder().build(
                Collections.singletonList(document));
        assertEquals(RawBuildStatus.BUILT, result.status());
        RawDefinition value = result.rawDefinitionSet().get()
                .definitions(RawDefinitionKind.PRODUCE).get(0);
        assertFalse(value.name().isPresent());
        assertTrue(value.references().isEmpty());
    }

    /**
     * 生产预算必须冻结为 256/65536，所有非正输入必须拒绝。
     */
    @Test
    void freezesProductionLimitsAndRejectsNonPositiveValues() throws Exception {
        Class<?> type = Class.forName("dec.core.compiler.raw.RawBuilderLimits");
        Method production = type.getDeclaredMethod("production");
        production.setAccessible(true);
        Object limits = production.invoke(null);
        Method depth = type.getDeclaredMethod("maxCanonicalDepth");
        Method nodes = type.getDeclaredMethod("maxCanonicalNodeCount");
        depth.setAccessible(true);
        nodes.setAccessible(true);
        assertEquals(256, depth.invoke(limits));
        assertEquals(65_536, nodes.invoke(limits));

        Constructor<?> constructor = type.getDeclaredConstructor(
                int.class,
                int.class);
        constructor.setAccessible(true);
        assertThrowsReflectiveIllegalArgument(constructor, 0, 1);
        assertThrowsReflectiveIllegalArgument(constructor, 1, 0);
        assertThrowsReflectiveIllegalArgument(constructor, -1, 1);
        assertThrowsReflectiveIllegalArgument(constructor, 1, -1);
    }

    /**
     * 所有 present-but-blank 的 owner/name token 都必须在公开构造边界拒绝。
     */
    @Test
    void rejectsBlankPublicOwnerAndNameTokens() {
        assertThrows(IllegalArgumentException.class, () -> definition(
                RawDefinitionKind.DATA_SOURCE,
                Optional.of("   "),
                Optional.of("name")));
        assertThrows(IllegalArgumentException.class, () -> definition(
                RawDefinitionKind.DATA_SOURCE,
                Optional.of("owner"),
                Optional.of("   ")));
        assertThrows(IllegalArgumentException.class, () -> definition(
                RawDefinitionKind.PRODUCE,
                Optional.of("owner"),
                Optional.of("   ")));
    }

    private static void assertThrowsReflectiveIllegalArgument(
            Constructor<?> constructor,
            int depth,
            int nodes) {
        Exception failure = assertThrows(Exception.class,
                () -> constructor.newInstance(depth, nodes));
        assertTrue(failure.getCause() instanceof IllegalArgumentException);
    }

    private static RawDefinition definition(
            RawDefinitionKind kind,
            Optional<String> owner,
            Optional<String> name) {
        return new RawDefinition(
                kind,
                0L,
                ref("value.xml", "/value"),
                owner,
                name,
                Collections.<String, String>emptyMap(),
                Collections.<RawReference>emptyList(),
                new RawNodeBody(
                        "value",
                        Collections.<String, String>emptyMap(),
                        Optional.<String>empty(),
                        Collections.<RawNodeBody>emptyList(),
                        ref("value.xml", "/value")),
                DocumentFormat.XML,
                "1.0");
    }

    private static void assertFailed(
            RawBuildResult result,
            String messageKey,
            SourceRef sourceRef) {
        assertEquals(RawBuildStatus.FAILED, result.status());
        assertFalse(result.rawDefinitionSet().isPresent());
        assertEquals(1, result.diagnostics().size());
        assertEquals(messageKey, result.diagnostics().get(0).messageKey());
        assertEquals(sourceRef, result.diagnostics().get(0).sourceRef());
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
