package dec.core.compiler.modelaccess;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T10 / I003 独立 Review：冻结 lexical 策略分离和结构验证器无状态合同。
 */
class ModelAccessI003IndependentReviewTest {

    /** TypedKey reference 允许 nonblank padded lexical，但继续拒绝 null 与纯空白。 */
    @Test
    void typedKeyReferencePolicyPreservesPaddedLexical() throws Exception {
        assertTrue(invokePolicy(
                "hasTypedKeyReferenceLexical", " OrderInfo "));
        assertTrue(invokePolicy(
                "hasTypedKeyReferenceLexical", "OrderInfo"));
        assertFalse(invokePolicy(
                "hasTypedKeyReferenceLexical", "   "));
        assertFalse(invokePolicy(
                "hasTypedKeyReferenceLexical", null));
    }

    /** 精确 path/selector 仍要求输入已经 trim，不得被 reference 策略放宽。 */
    @Test
    void exactPathPolicyRemainsStrict() throws Exception {
        assertTrue(invokePolicy("hasExactPathLexical", "order.user"));
        assertFalse(invokePolicy("hasExactPathLexical", " order.user"));
        assertFalse(invokePolicy("hasExactPathLexical", "order.user "));
        assertFalse(invokePolicy("hasExactPathLexical", "   "));
        assertFalse(invokePolicy("hasExactPathLexical", null));
    }

    /** 结构验证器不得持有 compilation 跨调用状态或静态可变字段。 */
    @Test
    void structureValidatorRemainsStateless() {
        for (Field field : ModelAccessStructureValidator.class
                .getDeclaredFields()) {
            if (field.isSynthetic()) {
                continue;
            }
            assertTrue(Modifier.isStatic(field.getModifiers()),
                    field.getName() + " must not be instance state");
            assertTrue(Modifier.isFinal(field.getModifiers()),
                    field.getName() + " must be final");
        }
    }

    /** 调用冻结的私有 lexical 策略并返回布尔结果。 */
    private static boolean invokePolicy(String name, String lexical)
            throws Exception {
        Method method = ModelAccessStructureValidator.class
                .getDeclaredMethod(name, String.class);
        method.setAccessible(true);
        return ((Boolean) method.invoke(null, lexical)).booleanValue();
    }
}
