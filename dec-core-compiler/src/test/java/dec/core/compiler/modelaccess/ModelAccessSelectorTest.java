package dec.core.compiler.modelaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKind;
import dec.core.context.model.DeferredRegistry;
import dec.core.context.model.RequiredStage;
import dec.core.context.model.ViewKey;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T10 target-main、property path、Binding 与 Deferred Oracle。
 */
class ModelAccessSelectorTest {

    /** target-main 必须优先于同名 property path。 */
    @Test
    void resolvesTargetMainBeforeSameNameProperty() {
        Object compilation = compile(ModelAccessTestFixture.targetMainPriority());
        Object binding = ModelAccessTestFixture.bindings(compilation).get(0);
        Object target = ModelAccessTestFixture.call(binding, "resolvedTarget");
        assertEquals("TARGET_MAIN", String.valueOf(
                ModelAccessTestFixture.call(target, "kind")));
        assertEquals("user", ModelAccessTestFixture.call(target, "value"));
    }

    /** target-main 未命中后必须逐段解析同一 View 的嵌套属性。 */
    @Test
    void resolvesNestedPropertyPathAfterTargetMainMiss() {
        Object compilation = compile(ModelAccessTestFixture.nestedPropertyFallback());
        Object binding = ModelAccessTestFixture.bindings(compilation).get(0);
        Object target = ModelAccessTestFixture.call(binding, "resolvedTarget");
        assertEquals("PROPERTY_PATH", String.valueOf(
                ModelAccessTestFixture.call(target, "kind")));
        assertEquals("payInfo.payDetailList",
                ModelAccessTestFixture.call(target, "value"));
    }

    /** 多个 ref 必须独立生成稳定 Binding，source path 与 selector 不得混淆。 */
    @Test
    void createsStableBindingsForMultipleReferences() {
        Object compilation = compile(ModelAccessTestFixture.multiBinding());
        List<Object> bindings = ModelAccessTestFixture.bindings(compilation);
        assertEquals(2, bindings.size());
        assertEquals("user", ModelAccessTestFixture.call(
                ModelAccessTestFixture.call(bindings.get(0), "sourcePath"), "value"));
        assertEquals("payInfo.payDetailList", ModelAccessTestFixture.call(
                ModelAccessTestFixture.call(bindings.get(0), "selector"), "value"));
        assertEquals("user", ModelAccessTestFixture.call(
                ModelAccessTestFixture.call(bindings.get(1), "sourcePath"), "value"));
        assertEquals("user", ModelAccessTestFixture.call(
                ModelAccessTestFixture.call(bindings.get(1), "selector"), "value"));
    }

    /** 无 ref 的直接 read/write 不制造伪 Binding。 */
    @Test
    void keepsDirectAccessWithoutSyntheticBinding() {
        Object compilation = compile(ModelAccessTestFixture.multiBinding());
        assertEquals(2, ModelAccessTestFixture.bindings(compilation).size());
        assertEquals(1, ((Number) ModelAccessTestFixture.call(
                ModelAccessTestFixture.call(compilation, "deferredRegistry"),
                "size")).intValue());
    }

    /** 成功 ModelAccess 必须生成字段完整的 P2 Deferred。 */
    @Test
    void publishesCompleteP2Deferred() {
        Object compilation = compile(ModelAccessTestFixture.multiBinding());
        DeferredRegistry registry = (DeferredRegistry) ModelAccessTestFixture.call(
                compilation, "deferredRegistry");
        assertEquals(1, registry.size());
        DeferredDefinition deferred = registry.find(registry.keys().get(0)).get();
        assertEquals(DeferredKind.MODEL_ACCESS, deferred.kind());
        assertEquals(RequiredStage.P2, deferred.requiredStage());
        assertEquals("model-access-selector-binding", deferred.reasonCode());
        assertEquals("model-access-binding/v1", deferred.body().format());
        assertEquals(2, deferred.resolvedReferences().size());
        assertEquals(new ViewKey("OrderInfo"), deferred.resolvedReferences().get(0));
    }

    /** Binding 与发布集合必须不可修改。 */
    @Test
    void freezesBindingsAndDeferredCollections() {
        Object compilation = compile(ModelAccessTestFixture.multiBinding());
        List<Object> bindings = ModelAccessTestFixture.bindings(compilation);
        assertThrows(UnsupportedOperationException.class,
                () -> bindings.add(new Object()));
        DeferredRegistry registry = (DeferredRegistry) ModelAccessTestFixture.call(
                compilation, "deferredRegistry");
        assertThrows(UnsupportedOperationException.class,
                () -> registry.keys().add(registry.keys().get(0)));
    }

    /** Matching Raw/Symbol 快照必须正常发布。 */
    @Test
    void acceptsMatchingRawAndSymbolSnapshots() {
        RawDefinitionSet definitions = ModelAccessTestFixture.targetMainPriority();
        Object result = ModelAccessTestFixture.compile(
                definitions,
                ModelAccessTestFixture.symbols(definitions));
        assertEquals("COMPILED", ModelAccessTestFixture.status(result));
        assertNotNull(ModelAccessTestFixture.compilation(result));
    }

    /** 执行合法编译并返回完整 Compilation。 */
    private static Object compile(RawDefinitionSet definitions) {
        Object result = ModelAccessTestFixture.compile(
                definitions,
                ModelAccessTestFixture.symbols(definitions));
        assertEquals("COMPILED", ModelAccessTestFixture.status(result),
                ModelAccessTestFixture.diagnostics(result).toString());
        Object compilation = ModelAccessTestFixture.compilation(result);
        assertNotNull(compilation);
        return compilation;
    }
}
