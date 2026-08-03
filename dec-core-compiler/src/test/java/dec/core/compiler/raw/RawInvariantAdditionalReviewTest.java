package dec.core.compiler.raw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dec.core.compiler.canonical.CanonicalDocumentNode;
import dec.core.compiler.canonical.DocumentFormat;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * I002 最终独立 Review 补充的 INFO、双重 lexical 与集合表现 Oracle。
 */
class RawInvariantAdditionalReviewTest {

    /**
     * INFO Diagnostic 也不得进入公开 FAILED 结果。
     */
    @Test
    void rejectsInfoDiagnosticInFailedResult() {
        Diagnostic diagnostic = new Diagnostic(
                DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                DiagnosticSeverity.INFO,
                "raw.info.invalid",
                null,
                ref("diagnostic.xml", "/diagnostic"),
                Collections.<SourceRef>emptyList(),
                "fix",
                "raw-definition-builder");
        assertThrows(IllegalArgumentException.class,
                () -> RawBuildResult.failed(Collections.singletonList(diagnostic)));
    }

    /**
     * model-ref 同时承担 name 与 reference 时，空白必须优先报告 reference 精确位置。
     */
    @Test
    void prioritizesBlankModelReferenceAtDefinitionNode() {
        CanonicalDocumentNode modelAccess = node(
                "systems.xml",
                "/systems/system/model-access-info/model-access",
                "model-access",
                attrs("model-ref", "   "));
        CanonicalDocumentNode document = node(
                "systems.xml", "/systems", "systems", attrs(),
                node("systems.xml", "/systems/system", "system",
                        attrs("name", "system"),
                        node("systems.xml", "/systems/system/model-access-info",
                                "model-access-info", attrs(), modelAccess)));

        RawBuildResult result = new RawDefinitionBuilder().build(
                Collections.singletonList(document));
        assertEquals(RawBuildStatus.FAILED, result.status());
        assertEquals("raw.reference.target.required",
                result.diagnostics().get(0).messageKey());
        assertEquals(modelAccess.sourceRef(),
                result.diagnostics().get(0).sourceRef());
    }

    /**
     * RawDefinitionSet.toString 必须继承 RawDefinition 的全部语义差异。
     */
    @Test
    void rawDefinitionSetToStringReflectsDefinitionSemantics() {
        RawDefinition first = definition("one", DocumentFormat.XML, "1.0");
        RawDefinition second = definition("two", DocumentFormat.YAML, "2.0");
        RawDefinitionSet firstSet = new RawDefinitionSet(
                Collections.singletonList(first));
        RawDefinitionSet secondSet = new RawDefinitionSet(
                Collections.singletonList(second));

        assertFalse(firstSet.equals(secondSet));
        assertFalse(firstSet.toString().equals(secondSet.toString()));
    }

    private static RawDefinition definition(
            String value,
            DocumentFormat format,
            String schemaVersion) {
        return new RawDefinition(
                RawDefinitionKind.DATA,
                0L,
                ref("data.xml", "/data"),
                Optional.<String>empty(),
                Optional.of("data"),
                Collections.singletonMap("value", value),
                Collections.singletonList(new RawReference(
                        "@ref", value, ref("data.xml", "/data"))),
                new RawNodeBody(
                        "data",
                        Collections.singletonMap("value", value),
                        Optional.of(value),
                        Collections.<RawNodeBody>emptyList(),
                        ref("data.xml", "/data")),
                format,
                schemaVersion);
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
