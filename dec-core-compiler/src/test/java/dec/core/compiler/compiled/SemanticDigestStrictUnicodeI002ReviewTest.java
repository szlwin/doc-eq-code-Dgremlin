package dec.core.compiler.compiled;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.source.AllowedRoot;
import dec.core.compiler.source.DocumentSource;
import dec.core.compiler.source.SourceManifest;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.ImmutableDeferredRegistry;
import dec.core.context.model.ImmutableRegistry;
import dec.core.context.model.PublishedSourceManifest;
import java.net.URI;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T13 / I002 独立 Review：严格 Unicode 编码的排列、恢复和并发边界。
 */
class SemanticDigestStrictUnicodeI002ReviewTest {
    private static final String VALID_VECTOR =
            "b1bc9c4009cd2228c1b40f484036b6488f07d0e043e5fa26cec9ea5dd531ae67";

    /** 未配对 surrogate 位于文本首、中、尾时都必须使用同一稳定错误合同。 */
    @Test
    void allUnpairedSurrogatePositionsFailClosed() {
        String high = String.valueOf((char) 0xD800);
        String low = String.valueOf((char) 0xDC00);
        List<String> malformed = new ArrayList<String>();
        malformed.add(high + "tail");
        malformed.add("head" + high + "tail");
        malformed.add("head" + high);
        malformed.add(low + "tail");
        malformed.add("head" + low + "tail");
        malformed.add("head" + low);

        for (String sourceId : malformed) {
            IllegalArgumentException failure = assertThrows(
                    IllegalArgumentException.class,
                    () -> sourceDigest(sourceId, "same-content"));
            assertEquals(
                    "sourceId must contain valid Unicode",
                    failure.getMessage());
            assertTrue(failure.getCause() instanceof CharacterCodingException);
        }
    }

    /** 一次严格编码失败不得污染同一无状态服务的后续合法摘要计算。 */
    @Test
    void failedEncodingDoesNotPoisonSubsequentValidDigest() {
        CompilerDigestService service = new CompilerDigestService();
        assertThrows(
                IllegalArgumentException.class,
                () -> sourceDigest(
                        service,
                        String.valueOf((char) 0xD801),
                        "same-content"));

        assertEquals(VALID_VECTOR, sourceDigest(service, "ascii", "content"));
    }

    /** 并发复用同一服务时，每个调用必须拥有独立 Encoder 状态。 */
    @Test
    void strictEncodingIsStatelessAcrossConcurrentCalls() throws Exception {
        final CompilerDigestService service = new CompilerDigestService();
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Callable<String>> tasks = new ArrayList<Callable<String>>();
            for (int index = 0; index < 64; index++) {
                final int ordinal = index;
                tasks.add(new Callable<String>() {
                    @Override
                    public String call() {
                        if ((ordinal & 1) == 0) {
                            return sourceDigest(service, "ascii", "content");
                        }
                        IllegalArgumentException failure = assertThrows(
                                IllegalArgumentException.class,
                                () -> sourceDigest(
                                        service,
                                        "id-" + String.valueOf((char) 0xDC01),
                                        "content"));
                        return failure.getMessage();
                    }
                });
            }

            List<Future<String>> results = executor.invokeAll(tasks);
            for (int index = 0; index < results.size(); index++) {
                String actual = results.get(index).get();
                assertEquals(
                        (index & 1) == 0
                                ? VALID_VECTOR
                                : "sourceId must contain valid Unicode",
                        actual);
            }
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    /** 使用新服务计算单 Source 的 sourceDigest。 */
    private static String sourceDigest(String sourceId, String content) {
        return sourceDigest(new CompilerDigestService(), sourceId, content);
    }

    /** 使用指定服务计算单 Source 的 sourceDigest，用于验证无状态复用。 */
    private static String sourceDigest(
            CompilerDigestService service,
            String sourceId,
            String content) {
        DocumentSource source = new DocumentSource(
                sourceId,
                URI.create("memory:/" + Integer.toHexString(sourceId.hashCode())),
                DocumentFormat.XML,
                new AllowedRoot(URI.create("memory:/")),
                content.getBytes(StandardCharsets.UTF_8),
                "fixture-digest");
        return service.compute(
                new SourceManifest(Collections.singletonList(source)),
                semanticInput()).sourceDigest();
    }

    /** 构造不影响 Source digest 的稳定空语义输入。 */
    private static SemanticDigestInput semanticInput() {
        return new SemanticDigestInput(
                PublishedSourceManifest.empty(),
                new ImmutableRegistry<DefinitionKey, CompiledDefinition>(
                        Collections.<DefinitionKey, CompiledDefinition>emptyMap()),
                new ImmutableDeferredRegistry(
                        Collections.<DeferredKey, DeferredDefinition>emptyMap()),
                "compiler-1",
                "schema-1",
                "options-1");
    }
}
