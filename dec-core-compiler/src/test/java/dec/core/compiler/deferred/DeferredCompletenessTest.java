package dec.core.compiler.deferred;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.model.DefinitionKey;
import dec.core.context.model.DeferredKind;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.NormalizedBody;
import dec.core.context.model.SourceRef;
import dec.core.context.model.SystemKey;
import dec.core.context.model.ViewKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T11：验证不完整 Deferred 必须 fail-closed 且不发布部分 Registry。
 */
class DeferredCompletenessTest {

    /** 缺 owner 必须使用 MIX-DEFERRED-INCOMPLETE 阻断。 */
    @Test
    void rejectsMissingOwner() {
        assertIncomplete(builder -> builder.ownerKey(null), "owner");
    }

    /** 缺 kind 必须阻断。 */
    @Test
    void rejectsMissingKind() {
        assertIncomplete(builder -> builder.kind(null), "kind");
    }

    /** 缺 ordinal 必须阻断。 */
    @Test
    void rejectsMissingOrdinal() {
        assertIncomplete(builder -> builder.ordinal(null), "ordinal");
    }

    /** 负 ordinal 必须阻断。 */
    @Test
    void rejectsNegativeOrdinal() {
        assertIncomplete(builder -> builder.ordinal(-1), "ordinal");
    }

    /** 缺 reason 必须阻断。 */
    @Test
    void rejectsMissingReason() {
        assertIncomplete(builder -> builder.reasonCode(null), "reason");
    }

    /** 空白 reason 必须阻断。 */
    @Test
    void rejectsBlankReason() {
        assertIncomplete(builder -> builder.reasonCode("   "), "reason");
    }

    /** reason 与 kind 的冻结策略不一致必须阻断。 */
    @Test
    void rejectsReasonMismatch() {
        assertIncomplete(builder -> builder.reasonCode("other-reason"),
                "reason-policy");
    }

    /** 缺 SourceRef 必须使用稳定 fallback Diagnostic，不得抛出空指针。 */
    @Test
    void rejectsMissingSourceRef() {
        DeferredClassificationInput input = validBuilder()
                .sourceRef(null)
                .build();
        DeferredClassificationResult result = build(input);

        assertFailed(result, "source-ref");
        assertEquals("<deferred>",
                result.diagnostics().get(0).sourceRef().sourceId());
    }

    /** 缺 NormalizedBody 必须阻断。 */
    @Test
    void rejectsMissingBody() {
        assertIncomplete(builder -> builder.body(null), "body");
    }

    /** 未显式提供 resolvedReferences 容器必须阻断。 */
    @Test
    void rejectsMissingResolvedReferencesContainer() {
        DeferredClassificationInput input = DeferredClassificationInput.builder()
                .ownerKey(new SystemKey("order"))
                .kind(DeferredKind.MODEL_ACCESS)
                .ordinal(0)
                .reasonCode("model-access-selector-binding")
                .sourceRef(source())
                .body(body())
                .build();
        assertFailed(build(input), "resolved-references");
    }

    /** 强类型引用列表包含 null 必须阻断。 */
    @Test
    void rejectsNullTypedReference() {
        List<DefinitionKey> values = new ArrayList<DefinitionKey>();
        values.add(new ViewKey("OrderInfo"));
        values.add(null);
        assertIncomplete(builder -> builder.resolvedReferences(values),
                "resolved-reference-null");
    }

    /** 任一未类型化 lexical 引用必须阻断。 */
    @Test
    void rejectsUnresolvedLexicalReference() {
        assertIncomplete(builder -> builder.unresolvedReferences(
                Arrays.asList("OrderInfo")), "unresolved-reference");
    }

    /** 输入列表包含 null 必须转为 Diagnostic，而不是发布或抛异常。 */
    @Test
    void rejectsNullInputElement() {
        DeferredClassificationResult result =
                new DeferredDefinitionBuilder().build(
                        Arrays.asList(validBuilder().build(), null));
        assertFailed(result, "input-null");
    }

    /** 重复 DeferredKey 必须阻断整批。 */
    @Test
    void rejectsDuplicateDeferredKey() {
        DeferredClassificationInput first = validBuilder().build();
        DeferredClassificationInput second = validBuilder().build();
        DeferredClassificationResult result =
                new DeferredDefinitionBuilder().build(
                        Arrays.asList(first, second));
        assertFailed(result, "duplicate-key");
    }

    /** 任一错误必须让整个批次失败且 Registry 缺席。 */
    @Test
    void doesNotPublishPartialRegistryWhenOneInputIsInvalid() {
        List<DeferredClassificationInput> inputs =
                new ArrayList<DeferredClassificationInput>(
                        DeferredClassificationTest.completeInputs());
        inputs.add(validBuilder().body(null).build());

        DeferredClassificationResult result =
                new DeferredDefinitionBuilder().build(inputs);

        assertEquals(DeferredClassificationStatus.FAILED, result.status());
        assertFalse(result.registry().isPresent());
        assertFalse(result.diagnostics().isEmpty());
    }

    /** 空批次是完整的空 Registry，而不是失败或 null。 */
    @Test
    void classifiesEmptyBatchAsEmptyRegistry() {
        DeferredClassificationResult result =
                new DeferredDefinitionBuilder().build(
                        Collections.<DeferredClassificationInput>emptyList());
        assertEquals(DeferredClassificationStatus.CLASSIFIED, result.status());
        assertTrue(result.registry().isPresent());
        assertEquals(0, result.registry().get().size());
    }

    private static void assertIncomplete(
            Consumer<DeferredClassificationInput.Builder> mutation,
            String field) {
        DeferredClassificationInput.Builder builder = validBuilder();
        mutation.accept(builder);
        assertFailed(build(builder.build()), field);
    }

    private static DeferredClassificationResult build(
            DeferredClassificationInput input) {
        return new DeferredDefinitionBuilder().build(
                Collections.singletonList(input));
    }

    private static void assertFailed(
            DeferredClassificationResult result,
            String field) {
        assertEquals(DeferredClassificationStatus.FAILED, result.status());
        assertFalse(result.registry().isPresent());
        assertFalse(result.diagnostics().isEmpty());
        for (Diagnostic diagnostic : result.diagnostics()) {
            assertEquals(DiagnosticCode.MIX_DEFERRED_INCOMPLETE,
                    diagnostic.code());
            assertEquals("DeferredClassificationPass", diagnostic.pass());
        }
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                ("deferred.incomplete." + field).equals(
                        diagnostic.messageKey())),
                result.diagnostics().toString());
    }

    private static DeferredClassificationInput.Builder validBuilder() {
        return DeferredClassificationInput.builder()
                .ownerKey(new SystemKey("order"))
                .kind(DeferredKind.MODEL_ACCESS)
                .ordinal(0)
                .reasonCode("model-access-selector-binding")
                .sourceRef(source())
                .body(body())
                .resolvedReferences(Arrays.<DefinitionKey>asList(
                        new ViewKey("OrderInfo")))
                .unresolvedReferences(Collections.<String>emptyList());
    }

    private static SourceRef source() {
        return new SourceRef("systems.xml", 1, 1,
                "/systems/system/model-access-info/model-access");
    }

    private static NormalizedBody body() {
        return new NormalizedBody("model-access-binding/v1", "owner=order");
    }
}
