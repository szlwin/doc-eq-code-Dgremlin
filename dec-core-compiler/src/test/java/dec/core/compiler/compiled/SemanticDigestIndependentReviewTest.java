package dec.core.compiler.compiled;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.source.AllowedRoot;
import dec.core.compiler.source.DocumentSource;
import dec.core.compiler.source.SourceManifest;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.DeferredRegistry;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.DigestPair;
import dec.core.context.model.PublishedSourceManifest;
import dec.core.context.model.Registry;
import java.net.URI;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T13 独立 Review：复核 canonical JSON、摘要与严格 Source identity 边界。
 */
class SemanticDigestIndependentReviewTest {

    /** Object key 必须按 Unicode code point，而不是 UTF-16 code unit 排序。 */
    @Test
    void objectKeysUseUnicodeCodePointOrder() {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        values.put("\uD800\uDC00", "supplementary");
        values.put("\uE000", "bmp");

        assertEquals(
                "{\"\uE000\":\"bmp\",\"\uD800\uDC00\":\"supplementary\"}",
                CanonicalJsonWriter.write(values));
    }

    /** quote、反斜杠、控制字符和 decimal 必须使用稳定 canonical 编码。 */
    @Test
    void stringsAndNumbersUseCanonicalEncoding() {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        values.put("decimal", new java.math.BigDecimal("12.3400"));
        values.put("negativeZero", Double.valueOf(-0.0d));
        values.put("text", "quote=\" slash=\\ line=\n control=\u0001");

        assertEquals(
                "{\"decimal\":12.34,\"negativeZero\":0,"
                        + "\"text\":\"quote=\\\" slash=\\\\ line=\\n control=\\u0001\"}",
                CanonicalJsonWriter.write(values));
    }

    /** NaN、Infinity、未知对象和循环结构必须稳定拒绝。 */
    @Test
    void invalidCanonicalValuesFailClosed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> CanonicalJsonWriter.write(Double.valueOf(Double.NaN)));
        assertThrows(
                IllegalArgumentException.class,
                () -> CanonicalJsonWriter.write(Double.valueOf(Double.POSITIVE_INFINITY)));
        assertThrows(
                IllegalArgumentException.class,
                () -> CanonicalJsonWriter.write(new Object()));

        List<Object> cycle = new ArrayList<Object>();
        cycle.add(cycle);
        assertThrows(
                IllegalArgumentException.class,
                () -> CanonicalJsonWriter.write(cycle));
    }

    /** 非标准 Map 暴露重复 Object key 时不得静默覆盖。 */
    @Test
    void duplicateObjectKeysAreRejected() {
        Map<String, Object> duplicate = new DuplicateEntryMap();

        IllegalArgumentException failure = assertThrows(
                IllegalArgumentException.class,
                () -> CanonicalJsonWriter.write(duplicate));

        assertTrue(failure.getMessage().contains("duplicate canonical JSON object key"));
    }

    /** 空语义输入仍必须形成含版本域的稳定快照。 */
    @Test
    void emptySemanticInputIsStableAndVersioned() {
        SemanticDigestInput first = emptyInput("compiler-1", "schema-1", "options-1");
        SemanticDigestInput second = emptyInput("compiler-1", "schema-1", "options-1");

        assertEquals(first, second);
        assertEquals(first.canonicalJson(), second.canonicalJson());
        assertTrue(first.canonicalJson().contains("DEC-SEMANTIC-DIGEST-V1"));
        assertNotEquals(
                new CompilerDigestService().compute(emptySources(), first).semanticDigest(),
                new CompilerDigestService().compute(
                        emptySources(),
                        emptyInput("compiler-2", "schema-1", "options-1"))
                        .semanticDigest());
    }

    /** SemanticDigestInput 构造后不得继续读取调用方可变 Registry key 列表。 */
    @Test
    void semanticInputDefensivelySnapshotsRegistryViews() {
        MutableEmptyRegistry definitions = new MutableEmptyRegistry();
        MutableEmptyDeferredRegistry deferred = new MutableEmptyDeferredRegistry();
        SemanticDigestInput input = new SemanticDigestInput(
                PublishedSourceManifest.empty(),
                definitions,
                deferred,
                "compiler-1",
                "schema-1",
                "options-1");
        String frozen = input.canonicalJson();

        definitions.keys.add(null);
        deferred.keys.add(null);

        assertEquals(frozen, input.canonicalJson());
    }

    /** 长度前缀必须区分简单拼接相同的 sourceId/content 组合。 */
    @Test
    void sourceDigestUsesLengthPrefixes() {
        CompilerDigestService service = new CompilerDigestService();
        SemanticDigestInput semantic = emptyInput(
                "compiler-1", "schema-1", "options-1");

        DigestPair first = service.compute(
                new SourceManifest(Collections.singletonList(source("a", "bc"))),
                semantic);
        DigestPair second = service.compute(
                new SourceManifest(Collections.singletonList(source("ab", "c"))),
                semantic);

        assertNotEquals(first.sourceDigest(), second.sourceDigest());
        assertEquals(first.semanticDigest(), second.semanticDigest());
    }

    /** Source 输入顺序和 supplementary sourceId 不得改变 Source digest。 */
    @Test
    void sourceDigestIsOrderIndependentForUnicodeIds() {
        DocumentSource bmp = source("\uE000", "bmp");
        DocumentSource supplementary = source("\uD800\uDC00", "supplementary");
        SemanticDigestInput semantic = emptyInput(
                "compiler-1", "schema-1", "options-1");
        CompilerDigestService service = new CompilerDigestService();

        String left = service.compute(
                new SourceManifest(Arrays.asList(bmp, supplementary)),
                semantic).sourceDigest();
        String right = service.compute(
                new SourceManifest(Arrays.asList(supplementary, bmp)),
                semantic).sourceDigest();

        assertEquals(left, right);
    }

    /** 不同单独 high surrogate 必须在摘要前稳定拒绝，不能进入替代字节碰撞。 */
    @Test
    void malformedHighSurrogateSourceIdsFailClosed() {
        assertInvalidSourceId(String.valueOf((char) 0xD800));
        assertInvalidSourceId(String.valueOf((char) 0xD801));
    }

    /** 不同单独 low surrogate 必须在摘要前稳定拒绝，不能进入替代字节碰撞。 */
    @Test
    void malformedLowSurrogateSourceIdsFailClosed() {
        assertInvalidSourceId(String.valueOf((char) 0xDC00));
        assertInvalidSourceId(String.valueOf((char) 0xDC01));
    }

    /** malformed sourceId 必须返回稳定异常类型、消息和编码原因。 */
    @Test
    void malformedSourceIdUsesStableFailure() {
        IllegalArgumentException failure = assertInvalidSourceId(
                "prefix-" + String.valueOf((char) 0xD800) + "-suffix");

        assertEquals("sourceId must contain valid Unicode", failure.getMessage());
        assertTrue(failure.getCause() instanceof CharacterCodingException);
    }

    /** 合法 surrogate pair 必须继续以标准 UTF-8 进入 sourceDigest。 */
    @Test
    void legalSupplementarySourceIdRemainsSupported() {
        String digest = sourceDigest("\uD800\uDC00", "supplementary");

        assertEquals(
                "5d3487a17ddf02979b10ba3a08dde03e6181de5edab776174c1f99e0d7e10288",
                digest);
    }

    /** 严格编码不得改变 ASCII、BMP 和 supplementary 的既有摘要向量。 */
    @Test
    void validSourceDigestVectorsRemainStable() {
        assertEquals(
                "b1bc9c4009cd2228c1b40f484036b6488f07d0e043e5fa26cec9ea5dd531ae67",
                sourceDigest("ascii", "content"));
        assertEquals(
                "de7683b7fd49f38c3236e89e6f5b5ce3ce171b725bc74d61b997bbef53573505",
                sourceDigest("\uE000", "bmp"));
        assertEquals(
                "5d3487a17ddf02979b10ba3a08dde03e6181de5edab776174c1f99e0d7e10288",
                sourceDigest("\uD800\uDC00", "supplementary"));
    }

    /** 断言单个 malformed Source identity 被严格编码边界拒绝。 */
    private static IllegalArgumentException assertInvalidSourceId(String sourceId) {
        return assertThrows(
                IllegalArgumentException.class,
                () -> sourceDigest(sourceId, "same-content"));
    }

    /** 计算单 Source 的 sourceDigest，隔离 semantic digest 版本域。 */
    private static String sourceDigest(String sourceId, String content) {
        return new CompilerDigestService().compute(
                new SourceManifest(Collections.singletonList(source(sourceId, content))),
                emptyInput("compiler-1", "schema-1", "options-1"))
                .sourceDigest();
    }

    /** 构造空语义输入。 */
    private static SemanticDigestInput emptyInput(
            String compilerVersion,
            String schemaVersion,
            String optionsDigest) {
        return new SemanticDigestInput(
                PublishedSourceManifest.empty(),
                new MutableEmptyRegistry(),
                new MutableEmptyDeferredRegistry(),
                compilerVersion,
                schemaVersion,
                optionsDigest);
    }

    /** 构造空 SourceManifest。 */
    private static SourceManifest emptySources() {
        return new SourceManifest(Collections.<DocumentSource>emptyList());
    }

    /** 构造确定性内存 Source。 */
    private static DocumentSource source(String sourceId, String content) {
        return new DocumentSource(
                sourceId,
                URI.create("memory:/" + Integer.toHexString(sourceId.hashCode())),
                DocumentFormat.XML,
                new AllowedRoot(URI.create("memory:/")),
                content.getBytes(StandardCharsets.UTF_8),
                "fixture-digest");
    }

    /** 暴露两个相同 key entry 的非标准 Map。 */
    private static final class DuplicateEntryMap
            extends AbstractMap<String, Object> {
        @Override
        public Set<Map.Entry<String, Object>> entrySet() {
            return new AbstractSet<Map.Entry<String, Object>>() {
                @Override
                public Iterator<Map.Entry<String, Object>> iterator() {
                    return Arrays.<Map.Entry<String, Object>>asList(
                            new SimpleImmutableEntry<String, Object>("same", "a"),
                            new SimpleImmutableEntry<String, Object>("same", "b"))
                            .iterator();
                }

                @Override
                public int size() {
                    return 2;
                }
            };
        }
    }

    /** 可变空 Registry，用于证明构造后不再读取调用方视图。 */
    private static final class MutableEmptyRegistry
            implements Registry<DefinitionKey, CompiledDefinition> {
        private final List<DefinitionKey> keys = new ArrayList<DefinitionKey>();

        @Override
        public Optional<CompiledDefinition> find(DefinitionKey key) {
            return Optional.empty();
        }

        @Override
        public CompiledDefinition require(DefinitionKey key) {
            throw new IllegalStateException("empty registry");
        }

        @Override
        public List<DefinitionKey> keys() {
            return keys;
        }

        @Override
        public int size() {
            return keys.size();
        }
    }

    /** 可变空 DeferredRegistry，用于证明构造后不再读取调用方视图。 */
    private static final class MutableEmptyDeferredRegistry
            implements DeferredRegistry {
        private final List<DeferredKey> keys = new ArrayList<DeferredKey>();

        @Override
        public List<DeferredDefinition> requiredBy(
                dec.core.context.model.RequiredStage stage) {
            return Collections.emptyList();
        }

        @Override
        public List<DeferredDefinition> ownedBy(DefinitionKey key) {
            return Collections.emptyList();
        }

        @Override
        public Optional<DeferredDefinition> find(DeferredKey key) {
            return Optional.empty();
        }

        @Override
        public List<DeferredKey> keys() {
            return keys;
        }

        @Override
        public int size() {
            return keys.size();
        }
    }
}
