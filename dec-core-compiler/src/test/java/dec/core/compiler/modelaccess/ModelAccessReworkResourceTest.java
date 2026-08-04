package dec.core.compiler.modelaccess;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T10 / I002 WRITE overlap 资源与语义 Oracle。
 */
class ModelAccessReworkResourceTest {

    /** N 条互不重叠路径的结构查询数必须随 segment 总数近似线性增长。 */
    @Test
    void keepsWriteOverlapIndexNearLinear() {
        Object index = newIndex();
        Method add = method(index, "add", SharedModelPath.class);
        Method operations = method(index, "operationCount");
        int count = 4096;
        for (int current = 0; current < count; current++) {
            boolean overlap = (Boolean) invoke(
                    add, index, new SharedModelPath("root" + current + ".leaf"));
            assertFalse(overlap, "互不重叠路径不得误报");
        }
        int actual = (Integer) invoke(operations, index);
        assertTrue(actual <= count * 4,
                "结构查询数不得退化为 N(N-1)/2，actual=" + actual);
    }

    /** trie 必须识别完整重复、祖先、后代与全局 wildcard。 */
    @Test
    void detectsAllWriteOverlapRelations() {
        Object index = newIndex();
        Method add = method(index, "add", SharedModelPath.class);
        assertFalse((Boolean) invoke(add, index,
                new SharedModelPath("a.b")));
        assertTrue((Boolean) invoke(add, index,
                new SharedModelPath("a.b.c")));

        index = newIndex();
        add = method(index, "add", SharedModelPath.class);
        assertFalse((Boolean) invoke(add, index,
                new SharedModelPath("a.b.c")));
        assertTrue((Boolean) invoke(add, index,
                new SharedModelPath("a.b")));

        index = newIndex();
        add = method(index, "add", SharedModelPath.class);
        assertFalse((Boolean) invoke(add, index,
                new SharedModelPath("a.b")));
        assertTrue((Boolean) invoke(add, index,
                new SharedModelPath("a.b")));

        index = newIndex();
        add = method(index, "add", SharedModelPath.class);
        assertFalse((Boolean) invoke(add, index,
                new SharedModelPath("*")));
        assertTrue((Boolean) invoke(add, index,
                new SharedModelPath("other")));
    }

    /** 通过反射创建 package-private overlap index，缺失边界转为普通 RED。 */
    private static Object newIndex() {
        try {
            Class<?> type = Class.forName(
                    "dec.core.compiler.modelaccess.WritePathOverlapIndex");
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object value = constructor.newInstance();
            assertNotNull(value);
            return value;
        } catch (ReflectiveOperationException failure) {
            fail("I002 WritePathOverlapIndex Architecture seam 尚未实现", failure);
            return null;
        }
    }

    /** 获取 package-private 方法。 */
    private static Method method(
            Object target,
            String name,
            Class<?>... parameters) {
        try {
            Method method = target.getClass().getDeclaredMethod(name, parameters);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException failure) {
            fail("缺少 I002 overlap 合同方法: " + name, failure);
            return null;
        }
    }

    /** 调用反射方法并把异常转为普通测试失败。 */
    private static Object invoke(
            Method method,
            Object target,
            Object... arguments) {
        try {
            return method.invoke(target, arguments);
        } catch (InvocationTargetException failure) {
            fail("overlap 合同不得抛出未接管异常", failure.getCause());
            return null;
        } catch (ReflectiveOperationException failure) {
            fail("无法调用 overlap 合同", failure);
            return null;
        }
    }
}
