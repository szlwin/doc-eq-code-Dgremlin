package dec.core.compiler.source;

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
        assertEquals(10, provider.accessCount());
    }

    @Test
    void remainsStableAcrossFileSetEnumerationOrders() {
        MixSourceGraph forward = resolve(
                SourceTestFixture.FileSetOrder.FORWARD);
        MixSourceGraph reversed = resolve(
                SourceTestFixture.FileSetOrder.REVERSED);
        MixSourceGraph shuffled = resolve(
                SourceTestFixture.FileSetOrder.SHUFFLED);

        assertEquals(forward, reversed);
        assertEquals(forward, shuffled);
        assertEquals(
                forward.manifest().sourceIds(),
                reversed.manifest().sourceIds());
        assertEquals(forward.edges(), shuffled.edges());
    }

    @Test
    void independentlyResolvesEquivalentMainAndTestMirrors() {
        MixSourceGraph mainGraph = resolve(
                SourceTestFixture.FileSetOrder.FORWARD);
        MixSourceGraph testGraph = resolve(
                SourceTestFixture.FileSetOrder.FORWARD);

        assertEquals(mainGraph, testGraph);
        assertEquals(
                mainGraph.manifest().totalBytes(),
                testGraph.manifest().totalBytes());
    }

    /**
     * 使用指定枚举顺序解析固定 fixture，并要求成功图存在。
     */
    private static MixSourceGraph resolve(
            SourceTestFixture.FileSetOrder order) {
        SourceGraphResolutionResult result = new MixSourceResolver().resolve(
                new SourceReference(SourceTestFixture.ROOT),
                SourceTestFixture.provider(order),
                SourceTestFixture.policy());
        assertEquals(SourceGraphResolutionStatus.RESOLVED, result.status());
        assertTrue(result.graph().isPresent());
        return result.graph().get();
    }
}
