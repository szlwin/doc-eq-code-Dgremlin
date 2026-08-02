package dec.core.context.tdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 冻结 EngineContext 与 Projection 的单一模型来源边界。
 */
class EngineContextApiTest {
    private static final String CASE_ID = "CASE-P1-T01-ENGINE-CONTEXT-RED-001";

    @Test
    @DisplayName(CASE_ID + " exposes an explicit read-only context boundary")
    void exposesExplicitReadOnlyContextBoundary() {
        Class<?> compiledModelSet = ContractReflectionAssertions.requireType(
                CASE_ID,
                "dec.core.context.model.CompiledModelSet");
        Class<?> engineContext = ContractReflectionAssertions.requireType(
                CASE_ID,
                "dec.core.context.EngineContext");
        Class<?> projection = ContractReflectionAssertions.requireType(
                CASE_ID,
                "dec.core.context.CoreConfigProjection");

        assertEquals(
                "dec.core.context",
                engineContext.getPackage().getName(),
                "TDD RED [" + CASE_ID + "]: EngineContext must remain neutral");
        ContractReflectionAssertions.assertStableValueShape(CASE_ID, engineContext);
        ContractReflectionAssertions.assertNoPublicMutationApi(CASE_ID, engineContext);
        ContractReflectionAssertions.assertNoStaticMutableState(CASE_ID, engineContext);

        Constructor<?>[] constructors = engineContext.getConstructors();
        assertEquals(
                1,
                constructors.length,
                "EngineContext 只能公开一个完整模型构造器");
        assertEquals(
                1,
                constructors[0].getParameterCount(),
                "EngineContext 构造器只能接收 CompiledModelSet");
        assertEquals(compiledModelSet, constructors[0].getParameterTypes()[0]);

        assertEquals(
                0,
                projection.getConstructors().length,
                "Projection 不得公开任意事实组合构造器");
        // Projection 允许存在显式 deprecated 写入口，但这些入口只能稳定拒绝；
        // 具体签名、异常和模型不变性由 R03 合同测试单独冻结。
        ContractReflectionAssertions.assertNoStaticMutableState(CASE_ID, projection);
        ContractReflectionAssertions.requirePublicMethod(
                CASE_ID,
                projection,
                "from",
                projection,
                compiledModelSet);
        ContractReflectionAssertions.requirePublicMethod(
                CASE_ID,
                projection,
                "sourceModelSet",
                compiledModelSet);
        ContractReflectionAssertions.requirePublicMethod(
                CASE_ID,
                projection,
                "data",
                List.class);
        ContractReflectionAssertions.requirePublicMethod(
                CASE_ID,
                projection,
                "views",
                List.class);
        ContractReflectionAssertions.requirePublicMethod(
                CASE_ID,
                projection,
                "rules",
                List.class);

        // from 是唯一公共工厂，但它不保存静态可变状态。
        assertTrue(
                Modifier.isStatic(
                        ContractReflectionAssertions.requirePublicMethod(
                                CASE_ID,
                                projection,
                                "from",
                                projection,
                                compiledModelSet).getModifiers()));
    }
}
