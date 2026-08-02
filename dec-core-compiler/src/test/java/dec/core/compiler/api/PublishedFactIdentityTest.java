package dec.core.compiler.api;

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
 * 验证成功结果精确复用 T01 模型中的 Diagnostic 发布事实。
 */
class PublishedFactIdentityTest {
    @Test
    void publishedResultReusesModelDiagnosticInstance() {
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
        PublishedCompilationResult result = new PublishedCompilationResult(
                "session-published",
                modelSet,
                new EngineContext(modelSet),
                modelSet.diagnostics());

        // 成功结果不得再复制一份等值列表，必须复用模型已经冻结的发布事实。
        assertSame(modelSet.diagnostics(), result.diagnostics());
    }

    /**
     * 使用最终 T01 构造合同创建包含 WARNING 的最小发布模型。
     */
    private static CompiledModelSet modelSet(List<Diagnostic> diagnostics) {
        return new CompiledModelSet(
                PublishedSourceManifest.empty(),
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
