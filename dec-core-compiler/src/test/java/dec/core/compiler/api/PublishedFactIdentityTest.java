package dec.core.compiler.api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;

import dec.core.context.EngineContext;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.DigestPair;
import dec.core.context.model.ImmutableDeferredRegistry;
import dec.core.context.model.ImmutableRegistry;
import dec.core.context.model.PublishedSourceManifest;
import dec.core.context.model.SourceRef;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 验证成功结果精确复用最终 T01 模型中的发布事实。
 */
class PublishedFactIdentityTest {
    @Test
    void publishedResultReusesModelAndDiagnosticInstances() {
        Diagnostic warning = new Diagnostic(
                DiagnosticCode.MIX_PUBLICATION_BLOCKED,
                DiagnosticSeverity.WARNING,
                "publication.warning",
                null,
                new SourceRef("test:root", 1, 1, "/root"),
                Collections.<SourceRef>emptyList(),
                "Review the warning before publication",
                "PublicationPass");
        CompiledModelSet modelSet = modelSet(Collections.singletonList(warning));
        EngineContext engineContext = new EngineContext(modelSet);

        PublishedCompilationResult result = assertDoesNotThrow(
                () -> PublishedCompilationResult.published(
                        modelSet.diagnostics(),
                        modelSet,
                        engineContext,
                        modelSet.digestPair(),
                        modelSet.compilerVersion(),
                        modelSet.schemaVersion(),
                        modelSet.optionsVersion(),
                        "sha-256-v1"));

        // 成功结果必须复用模型和模型已冻结的 Diagnostic 单一事实源。
        assertSame(modelSet, result.modelSet());
        assertSame(engineContext, result.engineContext());
        assertSame(modelSet.diagnostics(), result.diagnostics());
    }

    /**
     * 使用最终 T01 构造合同创建包含 WARNING 的最小发布模型。
     */
    private static CompiledModelSet modelSet(List<Diagnostic> diagnostics) {
        return new CompiledModelSet(
                PublishedSourceManifest.empty(),
                dec.core.context.model.CompiledViewMaterializationIndex.empty(),
                new ImmutableRegistry<DefinitionKey, CompiledDefinition>(
                        Collections.<DefinitionKey, CompiledDefinition>emptyMap()),
                new ImmutableDeferredRegistry(
                        Collections.<DeferredKey, DeferredDefinition>emptyMap()),
                diagnostics,
                new DigestPair("source", "semantic"),
                "compiler-1",
                "schema-1",
                "options-1");
    }
}
