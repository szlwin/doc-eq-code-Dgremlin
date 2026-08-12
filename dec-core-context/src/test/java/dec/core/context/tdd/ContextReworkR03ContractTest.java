package dec.core.context.tdd;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.CoreConfigProjection;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.DigestPair;
import dec.core.context.model.ImmutableDeferredRegistry;
import dec.core.context.model.ImmutableRegistry;
import dec.core.context.model.PublishedSourceDependency;
import dec.core.context.model.PublishedSourceDescriptor;
import dec.core.context.model.PublishedSourceManifest;
import dec.core.context.model.SourceRef;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * 冻结 I009 新增的 Projection 写入拒绝和来源边自洽合同。
 */
class ContextReworkR03ContractTest {
    private static final String CASE_ID = "CASE-P1-T01-REWORK-I009";

    @Test
    @DisplayName(CASE_ID + " Projection 写入产生稳定专用错误")
    void projectionWritesUseStableSpecializedRejection() {
        Class<?> exceptionType = ContractReflectionAssertions.requireType(
                CASE_ID,
                "dec.core.context.ProjectionWriteRejectedException");
        ContractReflectionAssertions.assertStableValueShape(CASE_ID, exceptionType);
        assertTrue(
                UnsupportedOperationException.class.isAssignableFrom(exceptionType),
                "专用异常必须保持 UnsupportedOperationException 兼容性");

        Method diagnosticCode = ContractReflectionAssertions.requirePublicMethod(
                CASE_ID,
                exceptionType,
                "diagnosticCode",
                DiagnosticCode.class);
        Method diagnostic = ContractReflectionAssertions.requirePublicMethod(
                CASE_ID,
                exceptionType,
                "diagnostic",
                Diagnostic.class);
        Method operation = ContractReflectionAssertions.requirePublicMethod(
                CASE_ID,
                exceptionType,
                "operation",
                String.class);

        Method register = ContractReflectionAssertions.requirePublicMethod(
                CASE_ID,
                CoreConfigProjection.class,
                "register",
                void.class,
                CompiledDefinition.class);
        Method replace = ContractReflectionAssertions.requirePublicMethod(
                CASE_ID,
                CoreConfigProjection.class,
                "replace",
                void.class,
                CompiledDefinition.class);
        Method remove = ContractReflectionAssertions.requirePublicMethod(
                CASE_ID,
                CoreConfigProjection.class,
                "remove",
                void.class,
                DefinitionKey.class);
        Method clear = ContractReflectionAssertions.requirePublicMethod(
                CASE_ID,
                CoreConfigProjection.class,
                "clear",
                void.class);
        assertDeprecated(register);
        assertDeprecated(replace);
        assertDeprecated(remove);
        assertDeprecated(clear);

        CompiledModelSet modelSet = emptyModelSet();
        CoreConfigProjection projection = CoreConfigProjection.from(modelSet);
        List<CompiledDefinition> originalData = projection.data();
        List<CompiledDefinition> originalViews = projection.views();
        List<CompiledDefinition> originalRules = projection.rules();

        assertRejected(
                exceptionType,
                diagnosticCode,
                diagnostic,
                operation,
                projection,
                register,
                new Object[] {null},
                "register");
        assertRejected(
                exceptionType,
                diagnosticCode,
                diagnostic,
                operation,
                projection,
                replace,
                new Object[] {null},
                "replace");
        assertRejected(
                exceptionType,
                diagnosticCode,
                diagnostic,
                operation,
                projection,
                remove,
                new Object[] {null},
                "remove");
        assertRejected(
                exceptionType,
                diagnosticCode,
                diagnostic,
                operation,
                projection,
                clear,
                new Object[0],
                "clear");

        // List 暴露面上的所有写入尝试也必须产生同一稳定错误语义。
        assertListRejected(
                exceptionType,
                diagnosticCode,
                operation,
                () -> projection.data().add(null),
                "data.add");
        assertListRejected(
                exceptionType,
                diagnosticCode,
                operation,
                () -> projection.views().clear(),
                "views.clear");
        assertListRejected(
                exceptionType,
                diagnosticCode,
                operation,
                () -> projection.rules().remove(null),
                "rules.remove");
        assertListRejected(
                exceptionType,
                diagnosticCode,
                operation,
                () -> projection.data().sort(null),
                "data.sort");

        // 所有拒绝入口必须保持 Projection 与来源模型的对象身份和值完全不变。
        assertSame(modelSet, projection.sourceModelSet());
        assertSame(originalData, projection.data());
        assertSame(originalViews, projection.views());
        assertSame(originalRules, projection.rules());
        assertTrue(projection.data().isEmpty());
        assertTrue(projection.views().isEmpty());
        assertTrue(projection.rules().isEmpty());
    }

    @Test
    @DisplayName(CASE_ID + " 普通依赖声明位置必须属于 fromSourceId")
    void dependencyDeclarationSourceMustMatchFromSource() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PublishedSourceDependency(
                        "SYSTEM_RULE_FILE",
                        "source:system-a",
                        "source:rule-a",
                        new SourceRef(
                                "source:rule-a",
                                10,
                                4,
                                "/system/rule-file")));
    }

    @Test
    @DisplayName(CASE_ID + " synthetic root edge 使用同一来源身份")
    void syntheticRootDependencyUsesSameSourceIdentity() {
        PublishedSourceDescriptor root = new PublishedSourceDescriptor(
                "synthetic:root",
                "SYNTHETIC",
                "root-digest");
        PublishedSourceDescriptor child = new PublishedSourceDescriptor(
                "source:child",
                "XML",
                "child-digest");
        PublishedSourceDependency dependency = new PublishedSourceDependency(
                "ROOT_SYSTEM_FILE",
                "synthetic:root",
                "source:child",
                new SourceRef(
                        "synthetic:root",
                        0,
                        0,
                        "/root/system-file"));

        PublishedSourceManifest manifest = new PublishedSourceManifest(
                "synthetic:root",
                Arrays.asList(root, child),
                Collections.singletonList(dependency));

        assertEquals("synthetic:root", manifest.rootSourceId());
        assertEquals(
                manifest.dependencies().get(0).fromSourceId(),
                manifest.dependencies().get(0).declarationSourceRef().sourceId());
    }

    @Test
    @DisplayName(CASE_ID + " synthetic root edge 拒绝声明来源错配")
    void syntheticRootDependencyRejectsMismatchedSourceIdentity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PublishedSourceDependency(
                        "ROOT_SYSTEM_FILE",
                        "synthetic:root",
                        "source:child",
                        new SourceRef(
                                "source:child",
                                0,
                                0,
                                "/root/system-file")));
    }

    private static void assertDeprecated(Method method) {
        assertTrue(
                method.isAnnotationPresent(Deprecated.class),
                "兼容写入口必须明确标记为 deprecated: " + method.getName());
    }

    private static void assertListRejected(
            Class<?> exceptionType,
            Method diagnosticCode,
            Method operation,
            Executable write,
            String expectedOperation) {
        Throwable failure = assertThrows(Throwable.class, write);
        assertTrue(
                exceptionType.isInstance(failure),
                "List 写入必须抛出 ProjectionWriteRejectedException");
        assertEquals(
                DiagnosticCode.MIX_PROJECTION_WRITE,
                invoke(diagnosticCode, failure));
        assertEquals(expectedOperation, invoke(operation, failure));
    }

    private static void assertRejected(
            Class<?> exceptionType,
            Method diagnosticCode,
            Method diagnostic,
            Method operation,
            CoreConfigProjection projection,
            Method writeMethod,
            Object[] arguments,
            String expectedOperation) {
        Throwable failure = assertThrows(
                Throwable.class,
                () -> invokeWrite(writeMethod, projection, arguments));
        assertTrue(
                exceptionType.isInstance(failure),
                "写入必须抛出 ProjectionWriteRejectedException");
        assertEquals(
                DiagnosticCode.MIX_PROJECTION_WRITE,
                invoke(diagnosticCode, failure));
        assertEquals(expectedOperation, invoke(operation, failure));
        Diagnostic value = (Diagnostic) invoke(diagnostic, failure);
        assertEquals(DiagnosticCode.MIX_PROJECTION_WRITE, value.code());
        assertEquals(DiagnosticSeverity.ERROR, value.severity());
        assertEquals("projection.write.rejected", value.messageKey());
        assertEquals("synthetic:core-config-projection", value.sourceRef().sourceId());
        assertEquals(
                "/compatibility-write/" + expectedOperation,
                value.sourceRef().nodePath());
        assertEquals("CoreConfigProjection", value.pass());
        assertTrue(value.recoveryHint().isPresent());
    }

    private static void invokeWrite(
            Method method,
            Object target,
            Object[] arguments) throws Throwable {
        try {
            method.invoke(target, arguments);
        } catch (InvocationTargetException failure) {
            throw failure.getCause();
        }
    }

    private static Object invoke(Method method, Object target) {
        return assertDoesNotThrow(() -> method.invoke(target));
    }

    private static CompiledModelSet emptyModelSet() {
        return new CompiledModelSet(
                PublishedSourceManifest.empty(),
                dec.core.context.model.CompiledViewMaterializationIndex.empty(),
                dec.core.context.model.ModelAccessPolicyIndex.empty(),
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
}
