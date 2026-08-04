package dec.core.compiler.deferred;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.model.DefinitionKey;
import dec.core.context.model.DeferredKind;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.NormalizedBody;
import dec.core.context.model.SourceRef;
import dec.core.context.model.SystemKey;
import dec.core.context.model.ViewKey;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T11 独立 Review：验证分类器的防御性、确定性与资源边界。
 */
class DeferredIndependentReviewTest {

    /** null 批次必须转换为稳定 Diagnostic，不得抛出异常。 */
    @Test
    void rejectsNullBatchWithStableDiagnostic() {
        DeferredClassificationResult result =
                new DeferredDefinitionBuilder().build(null);

        assertEquals(DeferredClassificationStatus.FAILED, result.status());
        assertFalse(result.registry().isPresent());
        assertEquals(1, result.diagnostics().size());
        assertEquals(DiagnosticCode.MIX_DEFERRED_INCOMPLETE,
                result.diagnostics().get(0).code());
        assertEquals("deferred.incomplete.inputs",
                result.diagnostics().get(0).messageKey());
    }

    /** 输入快照必须防御性复制强类型引用和未解析 lexical。 */
    @Test
    void defensivelyCopiesReferenceCollections() {
        List<DefinitionKey> resolved = new ArrayList<DefinitionKey>();
        resolved.add(new ViewKey("OrderInfo"));
        List<String> unresolved = new ArrayList<String>();
        unresolved.add("LegacyView");

        DeferredClassificationInput input = baseBuilder()
                .resolvedReferences(resolved)
                .unresolvedReferences(unresolved)
                .build();
        resolved.clear();
        unresolved.clear();

        assertEquals(1, input.resolvedReferences().size());
        assertEquals(1, input.unresolvedReferences().size());
        DeferredClassificationResult result =
                new DeferredDefinitionBuilder().build(
                        Collections.singletonList(input));
        assertEquals(DeferredClassificationStatus.FAILED, result.status());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                "deferred.incomplete.unresolved-reference".equals(
                        diagnostic.messageKey())));
    }

    /** 单个输入的多个缺口必须全部聚合，且 messageKey 顺序稳定。 */
    @Test
    void aggregatesMultipleMissingFieldsDeterministically() {
        DeferredClassificationInput input =
                DeferredClassificationInput.builder()
                        .ordinal(-1)
                        .reasonCode("   ")
                        .build();

        DeferredClassificationResult result =
                new DeferredDefinitionBuilder().build(
                        Collections.singletonList(input));
        Set<String> keys = new TreeSet<String>();
        result.diagnostics().forEach(diagnostic ->
                keys.add(diagnostic.messageKey()));

        assertEquals(new TreeSet<String>(Arrays.asList(
                "deferred.incomplete.body",
                "deferred.incomplete.kind",
                "deferred.incomplete.ordinal",
                "deferred.incomplete.owner",
                "deferred.incomplete.reason",
                "deferred.incomplete.resolved-references",
                "deferred.incomplete.source-ref")), keys);
    }

    /** 新增生产类型不得携带 static/thread-local 可变状态。 */
    @Test
    void hasNoStaticMutableState() {
        List<Class<?>> types = Arrays.<Class<?>>asList(
                DeferredClassificationPolicy.class,
                DeferredClassificationInput.class,
                DeferredClassificationResult.class,
                DeferredDefinitionBuilder.class,
                DeferredDiagnostics.class);
        for (Class<?> type : types) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    assertTrue(Modifier.isFinal(field.getModifiers()),
                            type.getName() + "#" + field.getName());
                }
                assertFalse(ThreadLocal.class.isAssignableFrom(field.getType()),
                        type.getName() + "#" + field.getName());
            }
        }
    }

    /** 4096 个唯一输入应在一次结构遍历中形成完整 Registry。 */
    @Test
    void classifiesLargeUniqueBatchWithoutPartialLoss() {
        List<DeferredClassificationInput> inputs =
                new ArrayList<DeferredClassificationInput>();
        for (int index = 0; index < 4096; index++) {
            inputs.add(DeferredClassificationInput.builder()
                    .ownerKey(new SystemKey("query-owner-" + index))
                    .kind(DeferredKind.QUERY)
                    .ordinal(0)
                    .reasonCode("query-planning")
                    .sourceRef(new SourceRef(
                            "query.xml", index + 1, 1,
                            "/queries/query[" + index + "]"))
                    .body(new NormalizedBody(
                            "query-plan/v1", "index=" + index))
                    .resolvedReferences(Collections.<DefinitionKey>singletonList(
                            new ViewKey("View" + index)))
                    .unresolvedReferences(Collections.<String>emptyList())
                    .build());
        }

        DeferredClassificationResult result =
                new DeferredDefinitionBuilder().build(inputs);

        assertEquals(DeferredClassificationStatus.CLASSIFIED, result.status(),
                result.diagnostics().toString());
        assertTrue(result.registry().isPresent());
        assertEquals(4096, result.registry().get().size());
    }

    /** 构造字段完整的 ModelAccess 分类输入。 */
    private static DeferredClassificationInput.Builder baseBuilder() {
        return DeferredClassificationInput.builder()
                .ownerKey(new SystemKey("order"))
                .kind(DeferredKind.MODEL_ACCESS)
                .ordinal(0)
                .reasonCode("model-access-selector-binding")
                .sourceRef(new SourceRef(
                        "systems.xml", 1, 1,
                        "/systems/system/model-access-info/model-access"))
                .body(new NormalizedBody(
                        "model-access-binding/v1", "owner=order"));
    }
}
