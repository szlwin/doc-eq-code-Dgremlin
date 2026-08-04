package dec.core.compiler.deferred;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T11 / I002：阻断 null 引用容器和未快照批次输入。
 */
class DeferredI002ReworkTest {

    /** null 容器必须保持“未提供”语义，不能伪装成显式空列表。 */
    @Test
    void rejectsNullResolvedReferencesContainer() {
        DeferredClassificationInput input = validBuilder(0)
                .resolvedReferences(null)
                .build();

        assertIncomplete(build(Collections.singletonList(input)),
                "resolved-references");
        assertFalse(input.resolvedReferencesProvided());
        assertTrue(input.resolvedReferences().isEmpty());
    }

    /** Builder 先设置合法列表再设置 null 时，最终状态必须回到未提供。 */
    @Test
    void nullContainerOverridesPreviouslyProvidedReferences() {
        DeferredClassificationInput input = validBuilder(0)
                .resolvedReferences(Collections.<DefinitionKey>singletonList(
                        new ViewKey("OrderInfo")))
                .resolvedReferences(null)
                .build();

        assertIncomplete(build(Collections.singletonList(input)),
                "resolved-references");
        assertFalse(input.resolvedReferencesProvided());
    }

    /** 显式空列表是合法完成状态，必须与 null 容器产生不同结果。 */
    @Test
    void acceptsExplicitEmptyResolvedReferences() {
        DeferredClassificationInput input = validBuilder(0)
                .resolvedReferences(Collections.<DefinitionKey>emptyList())
                .build();

        DeferredClassificationResult result =
                build(Collections.singletonList(input));

        assertEquals(DeferredClassificationStatus.CLASSIFIED, result.status(),
                result.diagnostics().toString());
        assertTrue(result.registry().isPresent());
        assertEquals(1, result.registry().get().size());
    }

    /** 非 null 容器中的 null 元素继续使用独立 Diagnostic。 */
    @Test
    void keepsNullElementDiagnosticDistinctFromNullContainer() {
        DeferredClassificationInput input = validBuilder(0)
                .resolvedReferences(Arrays.<DefinitionKey>asList(
                        new ViewKey("OrderInfo"), null))
                .build();

        assertIncomplete(build(Collections.singletonList(input)),
                "resolved-reference-null");
    }

    /** null 容器必须阻断同批次其他合法 Deferred，不能发布部分 Registry。 */
    @Test
    void nullContainerFailsWholeBatchAtomically() {
        DeferredClassificationInput invalid = validBuilder(0)
                .resolvedReferences(null)
                .build();
        DeferredClassificationInput valid = validBuilder(1).build();

        DeferredClassificationResult result =
                build(Arrays.asList(valid, invalid));

        assertIncomplete(result, "resolved-references");
        assertFalse(result.registry().isPresent());
    }

    /** build 必须先复制批次；复制完成后的原 List 变化不能丢失输入。 */
    @Test
    void snapshotsBatchBeforeClassificationTraversal() {
        List<DeferredClassificationInput> inputs =
                new SnapshotProbeList(Arrays.asList(
                        validBuilder(0).build(),
                        validBuilder(1).build()));

        DeferredClassificationResult result = build(inputs);

        assertEquals(DeferredClassificationStatus.CLASSIFIED, result.status(),
                result.diagnostics().toString());
        assertTrue(result.registry().isPresent());
        assertEquals(2, result.registry().get().size());
        assertEquals(0, inputs.size());
    }

    /** 自定义 List 的迭代异常不得越过结果边界；快照路径应避开其迭代器。 */
    @Test
    void doesNotExposeCallerIteratorFailure() {
        List<DeferredClassificationInput> inputs =
                new ThrowingIteratorList(Collections.singletonList(
                        validBuilder(0).build()));

        DeferredClassificationResult result = assertDoesNotThrow(
                () -> build(inputs));

        assertEquals(DeferredClassificationStatus.CLASSIFIED, result.status(),
                result.diagnostics().toString());
        assertTrue(result.registry().isPresent());
        assertEquals(1, result.registry().get().size());
    }

    /** 构造字段完整且默认包含一个强类型引用的输入 Builder。 */
    private static DeferredClassificationInput.Builder validBuilder(int ordinal) {
        return DeferredClassificationInput.builder()
                .ownerKey(new SystemKey("order"))
                .kind(DeferredKind.MODEL_ACCESS)
                .ordinal(ordinal)
                .reasonCode("model-access-selector-binding")
                .sourceRef(new SourceRef(
                        "systems.xml", ordinal + 1, 1,
                        "/systems/system/model-access-info/model-access["
                                + ordinal + "]"))
                .body(new NormalizedBody(
                        "model-access-binding/v1", "ordinal=" + ordinal))
                .resolvedReferences(Collections.<DefinitionKey>singletonList(
                        new ViewKey("OrderInfo")))
                .unresolvedReferences(Collections.<String>emptyList());
    }

    /** 执行批量分类。 */
    private static DeferredClassificationResult build(
            List<DeferredClassificationInput> inputs) {
        return new DeferredDefinitionBuilder().build(inputs);
    }

    /** 验证失败结果使用稳定完整性 Diagnostic 且不携带 Registry。 */
    private static void assertIncomplete(
            DeferredClassificationResult result,
            String field) {
        assertEquals(DeferredClassificationStatus.FAILED, result.status());
        assertFalse(result.registry().isPresent());
        assertTrue(result.diagnostics().stream().anyMatch(diagnostic ->
                DiagnosticCode.MIX_DEFERRED_INCOMPLETE.equals(
                        diagnostic.code())
                        && ("deferred.incomplete." + field).equals(
                                diagnostic.messageKey())),
                result.diagnostics().toString());
    }

    /**
     * `toArray()` 返回完整快照后立即清空调用方列表；直接迭代会只观察到部分输入。
     */
    private static final class SnapshotProbeList
            extends AbstractList<DeferredClassificationInput> {
        private final List<DeferredClassificationInput> values;

        private SnapshotProbeList(List<DeferredClassificationInput> values) {
            this.values = new ArrayList<DeferredClassificationInput>(values);
        }

        @Override
        public DeferredClassificationInput get(int index) {
            return values.get(index);
        }

        @Override
        public int size() {
            return values.size();
        }

        @Override
        public Object[] toArray() {
            Object[] snapshot = values.toArray();
            values.clear();
            return snapshot;
        }

        @Override
        public Iterator<DeferredClassificationInput> iterator() {
            return new Iterator<DeferredClassificationInput>() {
                private boolean consumed;

                @Override
                public boolean hasNext() {
                    return !consumed && !values.isEmpty();
                }

                @Override
                public DeferredClassificationInput next() {
                    DeferredClassificationInput value = values.get(0);
                    consumed = true;
                    values.clear();
                    return value;
                }
            };
        }
    }

    /** `toArray()` 可稳定复制，但直接迭代会模拟调用方并发修改失败。 */
    private static final class ThrowingIteratorList
            extends AbstractList<DeferredClassificationInput> {
        private final List<DeferredClassificationInput> values;

        private ThrowingIteratorList(List<DeferredClassificationInput> values) {
            this.values = new ArrayList<DeferredClassificationInput>(values);
        }

        @Override
        public DeferredClassificationInput get(int index) {
            return values.get(index);
        }

        @Override
        public int size() {
            return values.size();
        }

        @Override
        public Object[] toArray() {
            return values.toArray();
        }

        @Override
        public Iterator<DeferredClassificationInput> iterator() {
            throw new ConcurrentModificationException("caller-list-mutated");
        }
    }
}
