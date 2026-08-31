package dec.core.compiler.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import dec.core.compiler.api.PublishedCompilationResult;
import dec.core.context.EngineContext;
import dec.core.context.model.CompiledModelSet;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** DEV-09 revision DAG 合同：Compiler 只能通过完整 CompiledModelSet 向 Context/发布结果传递 P2 事实。 */
class P2RevisionDependencyDagContractTest {

    /**
     * EngineContext 只接受完整 CompiledModelSet，禁止把 policy/materialization 作为旁路参数重新拼装。
     */
    @Test
    @DisplayName("CASE-P2-TD-REVISION-DAG-001")
    void engineContextCapturesOnlyTheCompleteCompiledModelSet() throws Exception {
        Constructor<?>[] constructors = EngineContext.class.getConstructors();
        assertEquals(1, constructors.length);
        assertEquals(1, constructors[0].getParameterCount());
        assertEquals(CompiledModelSet.class, constructors[0].getParameterTypes()[0]);

        Method policy = EngineContext.class.getMethod("modelAccessPolicyIndex");
        Method materialization = EngineContext.class.getMethod("viewMaterializationIndex");
        assertNotNull(policy);
        assertNotNull(materialization);
    }

    /**
     * PublishedCompilationResult 必须同时暴露同一发布模型和其 EngineContext，禁止发布后再独立重建 revision 事实。
     */
    @Test
    void publishedResultKeepsModelAndContextInOneClosure() throws Exception {
        Method modelSet = PublishedCompilationResult.class.getMethod("modelSet");
        Method context = PublishedCompilationResult.class.getMethod("engineContext");
        assertSame(CompiledModelSet.class, modelSet.getReturnType());
        assertSame(EngineContext.class, context.getReturnType());
    }
}
