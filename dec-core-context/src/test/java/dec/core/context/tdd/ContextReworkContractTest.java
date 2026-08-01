package dec.core.context.tdd;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.CoreConfigProjection;
import dec.core.context.EngineContext;
import dec.core.context.model.BusinessScopeKey;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.DataKey;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.DeferredKind;
import dec.core.context.model.DeferredRegistry;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.DigestPair;
import dec.core.context.model.DirectoryKey;
import dec.core.context.model.ImmutableDeferredRegistry;
import dec.core.context.model.ImmutableRegistry;
import dec.core.context.model.NormalizedBody;
import dec.core.context.model.Registry;
import dec.core.context.model.RequiredStage;
import dec.core.context.model.SourceRef;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 追溯 Review 的可执行 RED。测试通过反射跨越旧、新公共构造器，
 * 确保失败来自合同语义缺失，而不是测试源无法编译。
 */
class ContextReworkContractTest {
    private static final String CASE_ID = "CASE-P1-T01-REWORK-I008";

    @Test
    @DisplayName(CASE_ID + " DirectoryKey 使用 BusinessScope 完整命名空间")
    void directoryKeyUsesBusinessScopeNamespace() {
        Constructor<DirectoryKey> constructor = assertDoesNotThrow(
                () -> DirectoryKey.class.getConstructor(BusinessScopeKey.class, String.class));

        DirectoryKey payment = newInstance(
                constructor,
                new BusinessScopeKey("payment"),
                "refund");
        DirectoryKey order = newInstance(
                constructor,
                new BusinessScopeKey("order"),
                "refund");
        DirectoryKey paymentAgain = newInstance(
                constructor,
                new BusinessScopeKey("payment"),
                "refund");

        assertNotEquals(payment, order);
        assertEquals(payment, paymentAgain);
        Method owner = assertDoesNotThrow(() -> DirectoryKey.class.getMethod("owner"));
        assertEquals(new BusinessScopeKey("payment"), invoke(owner, payment));
    }

    @Test
    @DisplayName(CASE_ID + " CompiledModelSet 冻结发布事实闭包")
    void compiledModelSetPublishesManifestAndTypedRegistries() {
        Class<?> manifestType = ContractReflectionAssertions.requireType(
                CASE_ID,
                "dec.core.context.model.PublishedSourceManifest");
        Class<?> typedRegistriesType = ContractReflectionAssertions.requireType(
                CASE_ID,
                "dec.core.context.model.TypedDefinitionRegistries");

        ContractReflectionAssertions.assertStableValueShape(CASE_ID, manifestType);
        ContractReflectionAssertions.assertStableValueShape(CASE_ID, typedRegistriesType);
        ContractReflectionAssertions.requirePublicMethod(
                CASE_ID,
                CompiledModelSet.class,
                "sourceManifest",
                manifestType);
        ContractReflectionAssertions.requirePublicMethod(
                CASE_ID,
                CompiledModelSet.class,
                "typedRegistries",
                typedRegistriesType);
    }

    @Test
    @DisplayName(CASE_ID + " ERROR Diagnostic 不能进入发布模型")
    void compiledModelSetRejectsErrorDiagnostics() {
        Diagnostic error = new Diagnostic(
                DiagnosticCode.MIX_PUBLICATION_BLOCKED,
                DiagnosticSeverity.ERROR,
                "publication.blocked",
                null,
                new SourceRef("test:root", 1, 1, "/root"),
                Collections.<SourceRef>emptyList(),
                "修复错误后重新编译",
                "PublicationPass");

        assertThrows(
                IllegalArgumentException.class,
                () -> newModelSet(
                        Collections.singletonList(error),
                        Collections.<DefinitionKey, CompiledDefinition>emptyMap(),
                        Collections.<DeferredKey, DeferredDefinition>emptyMap()));
    }

    @Test
    @DisplayName(CASE_ID + " Projection 只能由同一 ModelSet 派生")
    void engineContextDerivesProjectionFromSameModelSet() {
        for (Constructor<?> constructor : EngineContext.class.getConstructors()) {
            assertFalse(
                    containsType(constructor.getParameterTypes(), CoreConfigProjection.class),
                    "EngineContext 不得公开 ModelSet 与 Projection 自由组合构造器");
        }
        assertEquals(
                0,
                CoreConfigProjection.class.getConstructors().length,
                "CoreConfigProjection 不得公开任意列表构造器");

        CompiledModelSet modelSet = newModelSet(
                Collections.<Diagnostic>emptyList(),
                Collections.<DefinitionKey, CompiledDefinition>emptyMap(),
                Collections.<DeferredKey, DeferredDefinition>emptyMap());
        EngineContext context = new EngineContext(modelSet);
        assertNotNull(context.projection());

        Method sourceModel = assertDoesNotThrow(
                () -> CoreConfigProjection.class.getMethod("sourceModelSet"));
        assertEquals(modelSet, invoke(sourceModel, context.projection()));
    }

    @Test
    @DisplayName(CASE_ID + " Definition Registry 拒绝身份错配")
    void definitionRegistryRejectsMismatchedIdentity() {
        DataKey mapKey = new DataKey("A");
        CompiledDefinition value = new CompiledDefinition(
                new DataKey("B"),
                new SourceRef("test:root", 1, 1, "/data"),
                new NormalizedBody("canonical", "B"));
        Map<DefinitionKey, CompiledDefinition> definitions =
                new LinkedHashMap<DefinitionKey, CompiledDefinition>();
        definitions.put(mapKey, value);

        assertThrows(
                IllegalArgumentException.class,
                () -> newModelSet(
                        Collections.<Diagnostic>emptyList(),
                        definitions,
                        Collections.<DeferredKey, DeferredDefinition>emptyMap()));
    }

    @Test
    @DisplayName(CASE_ID + " Deferred Registry 拒绝身份错配")
    void deferredRegistryRejectsMismatchedIdentity() {
        DeferredKey mapKey = new DeferredKey(
                new DataKey("A"),
                DeferredKind.INFORMATION,
                0);
        DeferredKey valueKey = new DeferredKey(
                new DataKey("B"),
                DeferredKind.INFORMATION,
                1);
        DeferredDefinition value = newDeferredDefinition(valueKey);
        Map<DeferredKey, DeferredDefinition> deferred =
                new LinkedHashMap<DeferredKey, DeferredDefinition>();
        deferred.put(mapKey, value);

        assertThrows(
                IllegalArgumentException.class,
                () -> newModelSet(
                        Collections.<Diagnostic>emptyList(),
                        Collections.<DefinitionKey, CompiledDefinition>emptyMap(),
                        deferred));
    }

    private static CompiledModelSet newModelSet(
            List<Diagnostic> diagnostics,
            Map<DefinitionKey, CompiledDefinition> definitions,
            Map<DeferredKey, DeferredDefinition> deferred) {
        try {
            for (Constructor<?> constructor : CompiledModelSet.class.getConstructors()) {
                Object[] arguments = modelSetArguments(
                        constructor.getParameterTypes(),
                        diagnostics,
                        definitions,
                        deferred);
                if (arguments != null) {
                    return (CompiledModelSet) constructor.newInstance(arguments);
                }
            }
            throw new AssertionError("没有可识别的 CompiledModelSet 公共构造器");
        } catch (InvocationTargetException failure) {
            throw rethrow(failure.getCause());
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("创建 CompiledModelSet 失败", failure);
        }
    }

    private static Object[] modelSetArguments(
            Class<?>[] parameterTypes,
            List<Diagnostic> diagnostics,
            Map<DefinitionKey, CompiledDefinition> definitions,
            Map<DeferredKey, DeferredDefinition> deferred) throws ReflectiveOperationException {
        Object[] arguments = new Object[parameterTypes.length];
        int versionIndex = 0;
        for (int index = 0; index < parameterTypes.length; index++) {
            Class<?> parameterType = parameterTypes[index];
            if (parameterType.getName().equals(
                    "dec.core.context.model.PublishedSourceManifest")) {
                arguments[index] = parameterType.getMethod("empty").invoke(null);
            } else if (Registry.class.isAssignableFrom(parameterType)) {
                arguments[index] = new ImmutableRegistry<DefinitionKey, CompiledDefinition>(
                        definitions);
            } else if (DeferredRegistry.class.isAssignableFrom(parameterType)) {
                arguments[index] = new ImmutableDeferredRegistry(deferred);
            } else if (List.class.equals(parameterType)) {
                arguments[index] = diagnostics;
            } else if (DigestPair.class.equals(parameterType)) {
                arguments[index] = new DigestPair("source", "semantic");
            } else if (String.class.equals(parameterType)) {
                arguments[index] = "v" + (++versionIndex);
            } else {
                return null;
            }
        }
        return arguments;
    }

    private static DeferredDefinition newDeferredDefinition(DeferredKey key) {
        try {
            for (Constructor<?> constructor : DeferredDefinition.class.getConstructors()) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length == 6
                        && DeferredKey.class.equals(parameterTypes[0])) {
                    return (DeferredDefinition) constructor.newInstance(
                            key,
                            RequiredStage.P3,
                            "MIX-INFORMATION-OWNER",
                            new SourceRef("test:root", 2, 1, "/information"),
                            new NormalizedBody("expression", "A"),
                            Collections.<DefinitionKey>emptyList());
                }
                if (parameterTypes.length == 7
                        && DefinitionKey.class.isAssignableFrom(parameterTypes[0])) {
                    return (DeferredDefinition) constructor.newInstance(
                            key.owner(),
                            key.kind(),
                            RequiredStage.P3,
                            "MIX-INFORMATION-OWNER",
                            new SourceRef("test:root", 2, 1, "/information"),
                            new NormalizedBody("expression", "A"),
                            Collections.<DefinitionKey>emptyList());
                }
            }
            throw new AssertionError("没有可识别的 DeferredDefinition 公共构造器");
        } catch (InvocationTargetException failure) {
            throw rethrow(failure.getCause());
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("创建 DeferredDefinition 失败", failure);
        }
    }

    private static <T> T newInstance(
            Constructor<T> constructor,
            Object... arguments) {
        try {
            return constructor.newInstance(arguments);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("创建测试对象失败", failure);
        }
    }

    private static Object invoke(Method method, Object target) {
        try {
            return method.invoke(target);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("调用合同方法失败", failure);
        }
    }

    private static boolean containsType(Class<?>[] types, Class<?> expected) {
        for (Class<?> type : types) {
            if (expected.equals(type)) {
                return true;
            }
        }
        return false;
    }

    private static RuntimeException rethrow(Throwable cause) {
        if (cause instanceof RuntimeException) {
            return (RuntimeException) cause;
        }
        if (cause instanceof Error) {
            throw (Error) cause;
        }
        return new RuntimeException(cause);
    }
}
