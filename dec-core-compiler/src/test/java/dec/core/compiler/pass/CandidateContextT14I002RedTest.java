package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dec.core.compiler.api.CompilationOptions;
import dec.core.compiler.compiled.CompilerDigestService;
import dec.core.compiler.compiled.DigestBoundCompiledInput;
import dec.core.compiler.source.SourceManifest;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.DeferredRegistry;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.PublishedSourceManifest;
import dec.core.context.model.Registry;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T14 / I002：保留有效 RED 所对应的公开 API 回归合同。
 */
class CandidateContextT14I002RedTest {

    /** 摘要服务必须提供不接受外部 DigestPair 的 atomic bind 入口。 */
    @Test
    void digestServiceProvidesAtomicBindOperation() throws Exception {
        Method bind = CompilerDigestService.class.getMethod(
                "bind",
                SourceManifest.class,
                PublishedSourceManifest.class,
                Registry.class,
                DeferredRegistry.class,
                String.class,
                CompilationOptions.class);

        assertFalse(bind.getReturnType().equals(Object.class));
        assertFalse(bind.getParameterTypes()[0].equals(
                dec.core.context.model.DigestPair.class));
    }

    /** Builder 不得继续公开分别注入版本和任意 DigestPair 的入口。 */
    @Test
    void builderRejectsSplitProvenanceApi() {
        assertThrows(
                NoSuchMethodException.class,
                () -> CompiledModelSetBuilder.class.getConstructor(
                        String.class,
                        String.class,
                        String.class));
        assertThrows(
                NoSuchMethodException.class,
                () -> CompiledModelSetBuilder.class.getMethod(
                        "digestPair",
                        dec.core.context.model.DigestPair.class));
        try {
            CompiledModelSetBuilder.class.getConstructor(
                    DigestBoundCompiledInput.class);
        } catch (NoSuchMethodException failure) {
            throw new AssertionError("atomic Builder constructor missing", failure);
        }
    }
}
