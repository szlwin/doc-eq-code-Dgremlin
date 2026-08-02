package dec.core.context.tdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dec.core.context.CoreConfigProjection;
import dec.core.context.ProjectionWriteRejectedException;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.DataKey;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DigestPair;
import dec.core.context.model.ImmutableDeferredRegistry;
import dec.core.context.model.ImmutableRegistry;
import dec.core.context.model.NormalizedBody;
import dec.core.context.model.PublishedSourceManifest;
import dec.core.context.model.SourceRef;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * 冻结 I010 的 Projection 派生 List 与遍历视图写入拒绝合同。
 */
class ContextReworkR04ContractTest {
    private static final String CASE_ID = "CASE-P1-T01-REWORK-I010";

    @Test
    @DisplayName(CASE_ID + " 空 subList 的所有写入口必须稳定拒绝")
    void emptySubListMutationsUseStableRejection() {
        CoreConfigProjection projection = CoreConfigProjection.from(emptyModelSet());
        List<CompiledDefinition> subList = projection.data().subList(0, 0);

        assertRejected(() -> subList.clear(), "data.subList.clear");
        assertRejected(
                () -> subList.removeAll(Collections.emptyList()),
                "data.subList.removeAll");
        assertRejected(
                () -> subList.retainAll(Collections.emptyList()),
                "data.subList.retainAll");
        assertRejected(
                () -> subList.removeIf(value -> true),
                "data.subList.removeIf");
        assertRejected(
                () -> subList.replaceAll(value -> value),
                "data.subList.replaceAll");
        assertRejected(() -> subList.sort(null), "data.subList.sort");

        assertSame(projection.data(), projection.data());
        assertEquals(0, projection.data().size());
        assertEquals(0, subList.size());
    }

    @Test
    @DisplayName(CASE_ID + " 非空 subList 写入必须拒绝且保持快照不变")
    void nonEmptySubListMutationsUseStableRejection() {
        CoreConfigProjection projection = CoreConfigProjection.from(modelSetWithData());
        List<CompiledDefinition> root = projection.data();
        List<CompiledDefinition> subList = root.subList(0, 1);
        CompiledDefinition original = subList.get(0);

        assertRejected(() -> subList.clear(), "data.subList.clear");
        assertRejected(() -> subList.remove(0), "data.subList.remove");
        assertRejected(() -> subList.set(0, original), "data.subList.set");
        assertRejected(() -> subList.add(original), "data.subList.add");

        assertSame(root, projection.data());
        assertSame(original, root.get(0));
        assertSame(original, subList.get(0));
        assertEquals(1, root.size());
        assertEquals(1, subList.size());
    }

    @Test
    @DisplayName(CASE_ID + " 嵌套 subList 继续保持专用拒绝语义")
    void nestedSubListMutationsUseStableRejection() {
        CoreConfigProjection projection = CoreConfigProjection.from(emptyModelSet());
        List<CompiledDefinition> nested = projection.data()
                .subList(0, 0)
                .subList(0, 0);

        assertRejected(
                () -> nested.clear(),
                "data.subList.subList.clear");
        assertRejected(
                () -> nested.replaceAll(value -> value),
                "data.subList.subList.replaceAll");
    }

    @Test
    @DisplayName(CASE_ID + " Iterator 与 ListIterator 写入口必须稳定拒绝")
    void iteratorMutationsUseStableRejection() {
        CoreConfigProjection emptyProjection = CoreConfigProjection.from(emptyModelSet());
        Iterator<CompiledDefinition> emptyIterator = emptyProjection.data().iterator();
        ListIterator<CompiledDefinition> emptyListIterator =
                emptyProjection.data().listIterator();

        assertRejected(
                () -> emptyIterator.remove(),
                "data.iterator.remove");
        assertRejected(
                () -> emptyListIterator.remove(),
                "data.listIterator.remove");
        assertRejected(
                () -> emptyListIterator.set(null),
                "data.listIterator.set");
        assertRejected(
                () -> emptyListIterator.add(null),
                "data.listIterator.add");

        CoreConfigProjection populatedProjection =
                CoreConfigProjection.from(modelSetWithData());
        Iterator<CompiledDefinition> iterator = populatedProjection.data().iterator();
        ListIterator<CompiledDefinition> listIterator =
                populatedProjection.data().listIterator();
        iterator.next();
        listIterator.next();

        assertRejected(() -> iterator.remove(), "data.iterator.remove");
        assertRejected(
                () -> listIterator.remove(),
                "data.listIterator.remove");
        assertRejected(
                () -> listIterator.set(populatedProjection.data().get(0)),
                "data.listIterator.set");
        assertRejected(
                () -> listIterator.add(populatedProjection.data().get(0)),
                "data.listIterator.add");
        assertEquals(1, populatedProjection.data().size());
    }

    /**
     * 验证每一个派生写入口都返回统一的专用异常和稳定错误码。
     */
    private static void assertRejected(
            Executable executable,
            String expectedOperation) {
        ProjectionWriteRejectedException failure = assertThrows(
                ProjectionWriteRejectedException.class,
                executable);
        assertEquals(DiagnosticCode.MIX_PROJECTION_WRITE, failure.diagnosticCode());
        assertEquals(expectedOperation, failure.operation());
        assertEquals(DiagnosticCode.MIX_PROJECTION_WRITE, failure.diagnostic().code());
    }

    /**
     * 创建不包含定义的最小发布模型，用于验证空派生视图。
     */
    private static CompiledModelSet emptyModelSet() {
        return new CompiledModelSet(
                PublishedSourceManifest.empty(),
                new ImmutableRegistry<DefinitionKey, CompiledDefinition>(
                        Collections.<DefinitionKey, CompiledDefinition>emptyMap()),
                new ImmutableDeferredRegistry(
                        Collections.<DeferredKey, DeferredDefinition>emptyMap()),
                Collections.<Diagnostic>emptyList(),
                new DigestPair("source", "semantic"),
                "compiler-1",
                "schema-1",
                "options-1");
    }

    /**
     * 创建包含一个 Data 定义的模型，用于验证非空子列表与遍历器。
     */
    private static CompiledModelSet modelSetWithData() {
        DataKey key = new DataKey("orders");
        CompiledDefinition definition = new CompiledDefinition(
                key,
                new SourceRef("test:root", 1, 1, "/data/orders"),
                new NormalizedBody("canonical", key.canonical()));
        Map<DefinitionKey, CompiledDefinition> definitions =
                new LinkedHashMap<DefinitionKey, CompiledDefinition>();
        definitions.put(key, definition);

        return new CompiledModelSet(
                PublishedSourceManifest.empty(),
                new ImmutableRegistry<DefinitionKey, CompiledDefinition>(definitions),
                new ImmutableDeferredRegistry(
                        Collections.<DeferredKey, DeferredDefinition>emptyMap()),
                Collections.<Diagnostic>emptyList(),
                new DigestPair("source", "semantic"),
                "compiler-1",
                "schema-1",
                "options-1");
    }
}
