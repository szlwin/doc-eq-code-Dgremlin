package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.compiled.CompilerDigestService;
import dec.core.compiler.compiled.SemanticDigestInput;
import dec.core.compiler.source.AllowedRoot;
import dec.core.compiler.source.DocumentSource;
import dec.core.compiler.source.SourceManifest;
import dec.core.compiler.canonical.DocumentFormat;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.DigestPair;
import dec.core.context.model.ImmutableDeferredRegistry;
import dec.core.context.model.ImmutableRegistry;
import dec.core.context.model.PublishedSourceManifest;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T14 / I002：provenance 与当前请求绑定的有效 RED。
 */
class CandidateContextT14I002RedTest {

    /** 摘要服务必须提供原子绑定模型事实与摘要的正式入口。 */
    @Test
    void digestServiceProvidesAtomicBindOperation() {
        assertMethod(
                CompilerDigestService.class,
                "bind",
                SourceManifest.class,
                SemanticDigestInput.class);
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
        boolean exposesDigestPair = false;
        for (Method method : CompiledModelSetBuilder.class.getMethods()) {
            if ("digestPair".equals(method.getName())) {
                exposesDigestPair = true;
            }
        }
        assertFalse(exposesDigestPair);
    }

    /** FrozenInput 的 schema/options 与当前请求不一致时必须在发布前失败。 */
    @Test
    void requestVersionMismatchFailsBeforePublisher() {
        List<CompilerPass> passes = PipelineTestSupport.successfulPasses(
                new ArrayList<String>());
        passes.set(8, inputProducingDigestPass(
                frozenInput("schema-other", "options-other")));
        passes.set(9, new CandidateContextPublicationPass());

        PipelineExecutionResult result = new CompilerPipeline(passes).execute(
                PipelineTestSupport.request(
                        new PipelineTestSupport.MutableClock(),
                        () -> false,
                        Optional.empty(),
                        new PipelineTestSupport.RecordingObserver()),
                PipelineTestSupport.publicationRequest());

        assertEquals(CompilationSessionState.FAILED, result.state());
        assertEquals(0, result.artifacts().size());
    }

    /** 创建把指定 frozen input 放入 Digest Pass artifact 的测试 Pass。 */
    private static CompilerPass inputProducingDigestPass(Object input) {
        return new CompilerPass() {
            @Override
            public String name() {
                return CompilerPipeline.DIGEST_PASS;
            }

            @Override
            public PassResult execute(PassContext context) {
                context.putArtifact(
                        CandidateContextPublicationPass.INPUT_ARTIFACT,
                        input);
                return PassResult.passed();
            }
        };
    }

    /**
     * 优先调用 I002 atomic bind；在 RED 基线尚未实现时回退到 I001 Builder，
     * 从而让 mismatch 形成行为失败而不是 testCompile 失败。
     */
    private static Object frozenInput(String schemaVersion, String optionsDigest) {
        SemanticDigestInput semanticInput = new SemanticDigestInput(
                PublishedSourceManifest.empty(),
                emptyDefinitions(),
                emptyDeferred(),
                Collections.emptyList(),
                "compiler-t14",
                schemaVersion,
                optionsDigest);
        CompilerDigestService service = new CompilerDigestService();
        SourceManifest sources = sourceManifest();
        try {
            Method bind = service.getClass().getMethod(
                    "bind",
                    SourceManifest.class,
                    SemanticDigestInput.class);
            Object bound = bind.invoke(service, sources, semanticInput);
            Constructor<?> constructor = CompiledModelSetBuilder.class
                    .getConstructor(bound.getClass());
            return CompiledModelSetBuilder.class
                    .getMethod("freeze")
                    .invoke(constructor.newInstance(bound));
        } catch (NoSuchMethodException missingI002) {
            return legacyFrozenInput(service.compute(sources, semanticInput));
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("Unable to create I002 frozen input", failure);
        }
    }

    /** 使用 I001 分离式 Builder 构造 RED 对照输入。 */
    private static Object legacyFrozenInput(DigestPair pair) {
        try {
            Object builder = CompiledModelSetBuilder.class
                    .getConstructor(String.class, String.class, String.class)
                    .newInstance("compiler-t14", "schema-other", "options-other");
            invoke(builder, "sourceManifest", PublishedSourceManifest.class,
                    PublishedSourceManifest.empty());
            invoke(builder, "definitions", dec.core.context.model.Registry.class,
                    emptyDefinitions());
            invoke(builder, "deferred", dec.core.context.model.DeferredRegistry.class,
                    emptyDeferred());
            invoke(builder, "digestPair", DigestPair.class, pair);
            return builder.getClass().getMethod("freeze").invoke(builder);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("Unable to create I001 RED input", failure);
        }
    }

    /** 调用单参数 Builder 阶段方法。 */
    private static void invoke(
            Object target,
            String method,
            Class<?> parameterType,
            Object value) throws ReflectiveOperationException {
        target.getClass().getMethod(method, parameterType).invoke(target, value);
    }

    /** 断言公开方法存在，并把缺失转换为稳定测试失败。 */
    private static void assertMethod(
            Class<?> type,
            String name,
            Class<?>... parameterTypes) {
        try {
            type.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException failure) {
            throw new AssertionError("Missing method: " + name, failure);
        }
    }

    /** 创建空 Definition Registry。 */
    private static ImmutableRegistry<DefinitionKey, CompiledDefinition>
            emptyDefinitions() {
        return new ImmutableRegistry<DefinitionKey, CompiledDefinition>(
                Collections.<DefinitionKey, CompiledDefinition>emptyMap());
    }

    /** 创建空 Deferred Registry。 */
    private static ImmutableDeferredRegistry emptyDeferred() {
        return new ImmutableDeferredRegistry(
                Collections.<DeferredKey, DeferredDefinition>emptyMap());
    }

    /** 创建参与真实 T13 Source Digest 的最小 SourceManifest。 */
    private static SourceManifest sourceManifest() {
        DocumentSource source = new DocumentSource(
                "source:root",
                URI.create("memory:/root"),
                DocumentFormat.XML,
                new AllowedRoot(URI.create("memory:/")),
                "<root/>".getBytes(StandardCharsets.UTF_8),
                "fixture-t14-i002");
        return new SourceManifest(Collections.singletonList(source));
    }
}
