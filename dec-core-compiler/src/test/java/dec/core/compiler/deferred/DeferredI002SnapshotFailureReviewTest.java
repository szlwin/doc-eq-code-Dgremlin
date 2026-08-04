package dec.core.compiler.deferred;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.model.DiagnosticCode;
import java.util.AbstractList;
import java.util.ConcurrentModificationException;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T11 / I002 独立 Review：验证批次复制失败也不能泄露运行时异常。
 */
class DeferredI002SnapshotFailureReviewTest {

    /** 调用方 List 在复制阶段失败时，必须返回稳定 Diagnostic 且不发布 Registry。 */
    @Test
    void convertsSnapshotFailureToStableDiagnostic() {
        List<DeferredClassificationInput> inputs =
                new SnapshotFailureList();

        DeferredClassificationResult result = assertDoesNotThrow(
                () -> new DeferredDefinitionBuilder().build(inputs));

        assertEquals(DeferredClassificationStatus.FAILED, result.status());
        assertFalse(result.registry().isPresent());
        assertEquals(1, result.diagnostics().size());
        assertEquals(DiagnosticCode.MIX_DEFERRED_INCOMPLETE,
                result.diagnostics().get(0).code());
        assertTrue("deferred.incomplete.inputs-snapshot".equals(
                result.diagnostics().get(0).messageKey()));
    }

    /** 在 `toArray()` 中模拟调用方批次并发变化。 */
    private static final class SnapshotFailureList
            extends AbstractList<DeferredClassificationInput> {

        @Override
        public DeferredClassificationInput get(int index) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public Object[] toArray() {
            throw new ConcurrentModificationException(
                    "caller-list-changed-during-snapshot");
        }
    }
}
