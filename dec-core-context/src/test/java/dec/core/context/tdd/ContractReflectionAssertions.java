package dec.core.context.tdd;

import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

final class ContractReflectionAssertions {
    private ContractReflectionAssertions() {
    }

    static Class<?> requireType(String caseId, String typeName) {
        try {
            return Class.forName(typeName);
        } catch (ClassNotFoundException missing) {
            Assertions.fail("TDD RED [" + caseId + "]: missing production contract type " + typeName);
            throw new AssertionError("unreachable", missing);
        }
    }

    static Method requirePublicMethod(String caseId, Class<?> type, String name, Class<?> returnType,
                                      Class<?>... parameterTypes) {
        try {
            Method method = type.getMethod(name, parameterTypes);
            Assertions.assertEquals(returnType, method.getReturnType(),
                    () -> "TDD RED [" + caseId + "]: " + type.getName() + "." + name
                            + " must return " + returnType.getTypeName());
            return method;
        } catch (NoSuchMethodException missing) {
            Assertions.fail("TDD RED [" + caseId + "]: missing public method " + type.getName() + "."
                    + name + Arrays.toString(parameterTypes));
            throw new AssertionError("unreachable", missing);
        }
    }

    static void assertStableValueShape(String caseId, Class<?> type) {
        int modifiers = type.getModifiers();
        Assertions.assertTrue(type.isInterface() || type.isEnum() || Modifier.isFinal(modifiers),
                () -> "TDD RED [" + caseId + "]: value contract must be final/interface/enum: "
                        + type.getName());
        for (Field field : type.getDeclaredFields()) {
            if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Assertions.assertTrue(Modifier.isPrivate(field.getModifiers()),
                    () -> "TDD RED [" + caseId + "]: instance field must be private: " + field);
            Assertions.assertTrue(Modifier.isFinal(field.getModifiers()),
                    () -> "TDD RED [" + caseId + "]: instance field must be final: " + field);
        }
    }

    static void assertNoPublicMutationApi(String caseId, Class<?> type) {
        List<String> forbidden = new ArrayList<String>();
        for (Method method : type.getMethods()) {
            if (method.getDeclaringClass() == Object.class || Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            String name = method.getName().toLowerCase(Locale.ROOT);
            if (name.startsWith("set") || name.startsWith("add") || name.startsWith("put")
                    || name.startsWith("remove") || name.startsWith("clear")
                    || name.startsWith("register") || name.startsWith("update")) {
                forbidden.add(method.toGenericString());
            }
        }
        Assertions.assertTrue(forbidden.isEmpty(),
                () -> "TDD RED [" + caseId + "]: public mutation API is forbidden on "
                        + type.getName() + ": " + forbidden);
    }

    static void assertNoStaticMutableState(String caseId, Class<?> type) {
        List<String> mutable = new ArrayList<String>();
        for (Field field : type.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers)) {
                mutable.add(field.toGenericString());
            }
        }
        Assertions.assertTrue(mutable.isEmpty(),
                () -> "TDD RED [" + caseId + "]: static mutable state is forbidden on "
                        + type.getName() + ": " + mutable);
    }
}
