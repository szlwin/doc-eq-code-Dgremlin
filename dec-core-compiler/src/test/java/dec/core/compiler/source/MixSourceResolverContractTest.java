package dec.core.compiler.source;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * 验证固定 mix 入口的精确 SourceManifest、声明边和确定性。
 */
class MixSourceResolverContractTest {
    @Test
    void resolvesExactManifestAndEdges() {
        SourceTestFixture.InMemoryProvider provider = SourceTestFixture.provider(
                SourceTestFixture.FileSetOrder.FORWARD);

        SourceGraphResolutionResult result = new MixSourceResolver().resolve(
                new SourceReference(SourceTestFixture.ROOT),
                provider,
                SourceTestFixture.policy());

        assertEquals(SourceGraphResolutionStatus.RESOLVED, result.status());
        assertTrue(result.graph().isPresent());
        assertFalse(result.diagnostics().stream()
                .anyMatch(diagnostic -> diagnostic.severity()
                        == dec.core.context.model.DiagnosticSeverity.ERROR));

        MixSourceGraph graph = result.graph().get();
        assertEquals(10, graph.manifest().sources().size());
        assertEquals(
                SourceTestFixture.expectedSourceIds(),
                new LinkedHashSet<String>(graph.manifest().sourceIds()));
        assertEquals(7, graph.edges().size());

        Set<String> edgeKeys = graph.edges().stream()
                .map(SourceTestFixture::key)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        assertEquals(SourceTestFixture.expectedEdgeKeys(), edgeKeys);
        for (SourceGraphEdge edge : graph.edges()) {
            assertEquals(edge.fromSourceId(), edge.declarationSourceRef().sourceId());
            assertTrue(edge.declarationSourceRef().line() > 0);
            assertTrue(edge.declarationSourceRef().column() > 0);
        }

        // Provider 按声明解析：root、两个文件集、system、三个 rule、business。
        assertEquals(8, provider.accessCount());
    }

    @Test
    void remainsStableAcrossFileSetEnumerationOrders() {
        MixSourceGraph forward = resolve(
                SourceTestFixture.provider(SourceTestFixture.FileSetOrder.FORWARD));
        MixSourceGraph reversed = resolve(
                SourceTestFixture.provider(SourceTestFixture.FileSetOrder.REVERSED));
        MixSourceGraph shuffled = resolve(
                SourceTestFixture.provider(SourceTestFixture.FileSetOrder.SHUFFLED));

        assertEquals(forward, reversed);
        assertEquals(forward, shuffled);
        assertEquals(
                forward.manifest().sourceIds(),
                reversed.manifest().sourceIds());
        assertEquals(forward.edges(), shuffled.edges());
    }

    @Test
    void independentlyResolvesEquivalentMainAndTestMirrors() {
        MixSourceGraph mainGraph = resolve(SourceTestFixture.providerFromClasspath(
                "main-fixture/",
                SourceTestFixture.FileSetOrder.REVERSED));
        MixSourceGraph testGraph = resolve(SourceTestFixture.providerFromClasspath(
                "test-fixture/",
                SourceTestFixture.FileSetOrder.SHUFFLED));

        assertEquals(mainGraph, testGraph);
        assertEquals(
                mainGraph.manifest().totalBytes(),
                testGraph.manifest().totalBytes());
        assertEquals(10, mainGraph.manifest().sources().size());
        for (int index = 0; index < mainGraph.manifest().sources().size(); index++) {
            DocumentSource mainSource = mainGraph.manifest().sources().get(index);
            DocumentSource testSource = testGraph.manifest().sources().get(index);
            assertEquals(mainSource.sourceId(), testSource.sourceId());
            assertArrayEquals(mainSource.content(), testSource.content());
        }
    }

    /**
     * 使用指定 Provider 解析固定 fixture，并要求成功图存在。
     */
    private static MixSourceGraph resolve(DocumentSourceProvider provider) {
        SourceGraphResolutionResult result = new MixSourceResolver().resolve(
                new SourceReference(SourceTestFixture.ROOT),
                provider,
                SourceTestFixture.policy());
        assertEquals(
                SourceGraphResolutionStatus.RESOLVED,
                result.status(),
                result.diagnostics().toString());
        assertTrue(result.graph().isPresent());
        return result.graph().get();
    }
}
