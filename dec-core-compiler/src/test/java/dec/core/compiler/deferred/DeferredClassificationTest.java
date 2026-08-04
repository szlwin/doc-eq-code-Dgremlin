package dec.core.compiler.deferred;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.model.ActionKey;
import dec.core.context.model.BusinessScopeKey;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKind;
import dec.core.context.model.DeferredRegistry;
import dec.core.context.model.DirectoryKey;
import dec.core.context.model.InformationKey;
import dec.core.context.model.NormalizedBody;
import dec.core.context.model.ProduceKey;
import dec.core.context.model.RequiredStage;
import dec.core.context.model.RuleViewKey;
import dec.core.context.model.SourceRef;
import dec.core.context.model.SystemKey;
import dec.core.context.model.ViewKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T11：验证 P2-P7 Deferred 稳定分类与完整发布。
 */
class DeferredClassificationTest {

    /** 八种后续语义必须完整覆盖 P2-P7。 */
    @Test
    void classifiesEveryDeferredKindIntoFrozenStage() {
        List<DeferredClassificationInput> inputs = completeInputs();

        DeferredClassificationResult result =
                new DeferredDefinitionBuilder().build(inputs);

        assertEquals(DeferredClassificationStatus.CLASSIFIED, result.status(),
                result.diagnostics().toString());
        assertTrue(result.registry().isPresent());
        assertTrue(result.diagnostics().isEmpty());

        DeferredRegistry registry = result.registry().get();
        assertEquals(8, registry.size());
        assertEquals(2, registry.requiredBy(RequiredStage.P2).size());
        assertEquals(1, registry.requiredBy(RequiredStage.P3).size());
        assertEquals(2, registry.requiredBy(RequiredStage.P4).size());
        assertEquals(1, registry.requiredBy(RequiredStage.P5).size());
        assertEquals(1, registry.requiredBy(RequiredStage.P6).size());
        assertEquals(1, registry.requiredBy(RequiredStage.P7).size());

        Map<DeferredKind, RequiredStage> expected = expectedStages();
        for (DeferredClassificationInput input : inputs) {
            List<DeferredDefinition> definitions = registry.ownedBy(
                    input.ownerKey().get());
            assertEquals(1, definitions.size());
            DeferredDefinition definition = definitions.get(0);
            assertEquals(input.kind().get(), definition.kind());
            assertEquals(expected.get(definition.kind()),
                    definition.requiredStage());
            assertEquals(input.reasonCode().get(), definition.reasonCode());
            assertEquals(input.sourceRef().get(), definition.sourceRef());
            assertEquals(input.body().get(), definition.body());
            assertEquals(sorted(input.resolvedReferences()),
                    definition.resolvedReferences());
        }
    }

    /** 输入顺序变化不得改变 Registry 的稳定身份与内容。 */
    @Test
    void producesSameRegistryForReorderedInputs() {
        List<DeferredClassificationInput> first = completeInputs();
        List<DeferredClassificationInput> second =
                new ArrayList<DeferredClassificationInput>(first);
        Collections.reverse(second);

        DeferredClassificationResult left =
                new DeferredDefinitionBuilder().build(first);
        DeferredClassificationResult right =
                new DeferredDefinitionBuilder().build(second);

        assertEquals(DeferredClassificationStatus.CLASSIFIED, left.status());
        assertEquals(DeferredClassificationStatus.CLASSIFIED, right.status());
        assertEquals(left.registry().get(), right.registry().get());
        assertEquals(left.registry().get().keys(), right.registry().get().keys());
    }

    /** 成功结果及 Registry 集合必须保持不可变。 */
    @Test
    void publishesImmutableRegistryAndReferenceLists() {
        DeferredClassificationResult result =
                new DeferredDefinitionBuilder().build(completeInputs());
        DeferredRegistry registry = result.registry().get();

        assertThrows(UnsupportedOperationException.class,
                () -> registry.keys().clear());
        DeferredDefinition first = registry.find(registry.keys().get(0)).get();
        assertThrows(UnsupportedOperationException.class,
                () -> first.resolvedReferences().clear());
    }

    /** Policy 必须固定全部阶段与原因码，不接受调用方隐式覆盖。 */
    @Test
    void exposesStablePolicyForEveryKind() {
        DeferredClassificationPolicy policy =
                new DeferredClassificationPolicy();
        Map<DeferredKind, RequiredStage> expected = expectedStages();
        Map<DeferredKind, String> reasons = expectedReasons();

        for (DeferredKind kind : DeferredKind.values()) {
            assertEquals(expected.get(kind), policy.requiredStage(kind));
            assertEquals(reasons.get(kind), policy.reasonCode(kind));
        }
    }

    /** P1 分类只产生数据，不暴露 evaluate/execute/query/commit 等运行入口。 */
    @Test
    void doesNotExposeRuntimeExecutionMethods() {
        List<Class<?>> types = Arrays.<Class<?>>asList(
                DeferredClassificationPolicy.class,
                DeferredClassificationInput.class,
                DeferredDefinitionBuilder.class,
                DeferredClassificationResult.class);
        for (Class<?> type : types) {
            Arrays.stream(type.getDeclaredMethods()).forEach(method -> {
                String name = method.getName().toLowerCase();
                assertFalse(name.contains("evaluate"), method.toString());
                assertFalse(name.contains("execute"), method.toString());
                assertFalse(name.contains("query"), method.toString());
                assertFalse(name.contains("commit"), method.toString());
                assertFalse(name.contains("rollback"), method.toString());
            });
        }
    }

    /** 构造覆盖全部 DeferredKind 的完整分类输入。 */
    static List<DeferredClassificationInput> completeInputs() {
        SystemKey order = new SystemKey("order");
        SystemKey payment = new SystemKey("payment");
        InformationKey information = new InformationKey(order, "payStatus");
        BusinessScopeKey scope = new BusinessScopeKey("checkout");
        DirectoryKey directory = new DirectoryKey(scope, "pay");
        ActionKey action = new ActionKey(directory, "submit");
        ProduceKey produce = new ProduceKey(action, 7);
        RuleViewKey ruleView = new RuleViewKey(order, "submitRule");

        return Arrays.asList(
                input(order, DeferredKind.SYSTEM_PERMISSION, 0,
                        "system-permission-evaluation",
                        refs(ruleView)),
                input(payment, DeferredKind.MODEL_ACCESS, 0,
                        "model-access-selector-binding",
                        refs(new ViewKey("PaymentInfo"))),
                input(information, DeferredKind.INFORMATION, 0,
                        "information-expression-evaluation",
                        refs(new InformationKey(payment, "success"))),
                input(action, DeferredKind.ACTION, 0,
                        "action-execution",
                        refs(ruleView)),
                input(produce, DeferredKind.PRODUCE, 0,
                        "produce-execution",
                        refs(information)),
                input(directory, DeferredKind.DIRECTORY, 0,
                        "directory-evaluation",
                        refs(information)),
                input(new SystemKey("query-owner"), DeferredKind.QUERY, 0,
                        "query-planning",
                        refs(new ViewKey("OrderInfo"))),
                input(new SystemKey("transaction-owner"),
                        DeferredKind.TRANSACTION, 0,
                        "transaction-execution",
                        refs(action)));
    }

    /** 创建一个字段完整、引用已类型化的分类输入。 */
    static DeferredClassificationInput input(
            DefinitionKey owner,
            DeferredKind kind,
            int ordinal,
            String reason,
            List<DefinitionKey> references) {
        return DeferredClassificationInput.builder()
                .ownerKey(owner)
                .kind(kind)
                .ordinal(ordinal)
                .reasonCode(reason)
                .sourceRef(new SourceRef(
                        kind.name().toLowerCase() + ".xml",
                        ordinal + 1,
                        1,
                        "/deferred/" + kind.name().toLowerCase()))
                .body(new NormalizedBody(
                        "deferred-" + kind.name().toLowerCase() + "/v1",
                        "kind=" + kind.name()))
                .resolvedReferences(references)
                .unresolvedReferences(Collections.<String>emptyList())
                .build();
    }

    private static List<DefinitionKey> refs(DefinitionKey... values) {
        return Arrays.asList(values);
    }

    private static List<DefinitionKey> sorted(List<DefinitionKey> values) {
        List<DefinitionKey> result = new ArrayList<DefinitionKey>(values);
        Collections.sort(result);
        return result;
    }

    private static Map<DeferredKind, RequiredStage> expectedStages() {
        Map<DeferredKind, RequiredStage> result =
                new EnumMap<DeferredKind, RequiredStage>(DeferredKind.class);
        result.put(DeferredKind.SYSTEM_PERMISSION, RequiredStage.P2);
        result.put(DeferredKind.MODEL_ACCESS, RequiredStage.P2);
        result.put(DeferredKind.INFORMATION, RequiredStage.P3);
        result.put(DeferredKind.ACTION, RequiredStage.P4);
        result.put(DeferredKind.PRODUCE, RequiredStage.P4);
        result.put(DeferredKind.DIRECTORY, RequiredStage.P5);
        result.put(DeferredKind.QUERY, RequiredStage.P6);
        result.put(DeferredKind.TRANSACTION, RequiredStage.P7);
        return result;
    }

    private static Map<DeferredKind, String> expectedReasons() {
        Map<DeferredKind, String> result =
                new EnumMap<DeferredKind, String>(DeferredKind.class);
        result.put(DeferredKind.SYSTEM_PERMISSION,
                "system-permission-evaluation");
        result.put(DeferredKind.MODEL_ACCESS,
                "model-access-selector-binding");
        result.put(DeferredKind.INFORMATION,
                "information-expression-evaluation");
        result.put(DeferredKind.ACTION, "action-execution");
        result.put(DeferredKind.PRODUCE, "produce-execution");
        result.put(DeferredKind.DIRECTORY, "directory-evaluation");
        result.put(DeferredKind.QUERY, "query-planning");
        result.put(DeferredKind.TRANSACTION, "transaction-execution");
        return result;
    }
}
