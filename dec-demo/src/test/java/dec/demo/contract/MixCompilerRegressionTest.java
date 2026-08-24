package dec.demo.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import dec.core.compiler.api.CompilationOptions;
import dec.core.compiler.api.CompilationResult;
import dec.core.compiler.api.CompilationStatus;
import dec.core.compiler.api.ContextPublisher;
import dec.core.compiler.api.PublicationResult;
import dec.core.compiler.api.PublicationStatus;
import dec.core.compiler.api.PublishedCompilationResult;
import dec.core.compiler.source.SourceReference;
import dec.core.context.EngineContext;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.DeferredKind;
import dec.core.starter.CompilerBootstrap;
import java.io.File;
import java.net.URLClassLoader;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** 真实 mix 配置的编译规模和简化 P2 Deferred 边界回归。 */
class MixCompilerRegressionTest {

    /**
     * AC-P2-SYSTEM-RULEVIEW-008：model-access 保留静态兼容元数据，
     * 不再产生 SYSTEM_PERMISSION 或 MODEL_ACCESS Deferred。
    */
    @Test
    void compilesCompleteMixWithoutRetiredP2Deferred() throws Exception {
        CompilationResult result;
        // Demo 同时保留 main/test 两套 mix 镜像；隔离到 main 输出目录，
        // 让生产 Provider 的重复 classpath Source 门禁继续保持严格。
        try (URLClassLoader resources = new URLClassLoader(
                new java.net.URL[] {
                        new File("target/classes").toURI().toURL()
                },
                null)) {
            result = CompilerBootstrap.builder()
                    .classLoader(resources)
                    .allowedRoot("classpath:mix/")
                    .publisher(alwaysPublish())
                    .build()
                    .compileAndPublish(
                            new SourceReference("classpath:mix/orm-config.xml"),
                            new CompilationOptions("1.0", "mix-regression"),
                            Optional.<EngineContext>empty());
        }

        assertEquals(CompilationStatus.PUBLISHED, result.status(),
                result.diagnostics().toString());
        CompiledModelSet model = ((PublishedCompilationResult) result).modelSet();
        assertEquals(4, model.typedRegistries().systems().size());
        assertEquals(14, model.typedRegistries().ruleViews().size());
        assertEquals(16, model.typedRegistries().information().size());
        assertEquals(97, model.modelAccessPolicyIndex().keys().size());
        assertEquals(22, model.deferred().size());
        assertFalse(model.deferred().keys().stream().anyMatch(key ->
                key.kind() == DeferredKind.SYSTEM_PERMISSION
                        || key.kind() == DeferredKind.MODEL_ACCESS));
    }

    private static ContextPublisher alwaysPublish() {
        return new ContextPublisher() {
            @Override
            public PublicationResult publish(
                    Optional<EngineContext> expectedCurrent,
                    EngineContext candidate) {
                return new PublicationResult() {
                    @Override
                    public PublicationStatus status() {
                        return PublicationStatus.PUBLISHED;
                    }
                };
            }
        };
    }
}
