package dec.core.compiler.pass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.api.CompilationOptions;
import dec.core.compiler.api.ContextPublisher;
import dec.core.compiler.api.PublicationRequest;
import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.compiled.CompilerDigestService;
import dec.core.compiler.compiled.DigestBoundCompiledInput;
import dec.core.compiler.source.AllowedRoot;
import dec.core.compiler.source.DocumentSource;
import dec.core.compiler.source.SourceManifest;
import dec.core.context.EngineContext;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.DataKey;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.DeferredKind;
import dec.core.context.model.DeferredRegistry;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.DigestPair;
import dec.core.context.model.ImmutableDeferredRegistry;
import dec.core.context.model.ImmutableRegistry;
import dec.core.context.model.NormalizedBody;
import dec.core.context.model.PublishedSourceManifest;
import dec.core.context.model.Registry;
import dec.core.context.model.RequiredStage;
import dec.core.context.model.SourceRef;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T14 / I002 独立 Review：provenance、快照完整性和能力边界。
 */
class CandidateContextT14IndependentReviewTest {

    /** Definition 与 Deferred 的负 size 必须分别 fail-closed。 */
    @Test
    void negativeRegistrySizesFailClosed() {
        assertThrows(IllegalArgumentException.class,
                () -> bind(new TestRegistry(-1, -1, Collections.emptyList()),
                        emptyDeferred()));
        assertThrows(IllegalArgumentException.class,
                () -> bind(emptyDefinitions(),
                        new TestDeferredRegistry(-1, -1, Collections.emptyList())));
    }

    /** keys 数量与声明 size 不一致时必须分别拒绝。 */
    @Test
    void keyEnumerationMismatchFailsClosed() {
        assertThrows(IllegalArgumentException.class,
                () -> bind(new TestRegistry(1, 1, Collections.emptyList()),
                        emptyDeferred()));
        assertThrows(IllegalArgumentException.class,
                () -> bind(emptyDefinitions(),
                        new TestDeferredRegistry(1, 1, Collections.emptyList())));
    }

    /** Definition 与 Deferred 的重复 key 必须分别拒绝。 */
    @Test
    void duplicateKeysFailClosed() {
        DefinitionKey key = key("alpha");
        CompiledDefinition definition = definition(key);
        TestRegistry definitions = new TestRegistry(
                2, 2, Arrays.asList(key, key));
        definitions.values.put(key, definition);

        DeferredDefinition deferredDefinition = deferred(key, 0);
        TestDeferredRegistry deferred = new TestDeferredRegistry(
                2, 2, Arrays.asList(
                        deferredDefinition.key(),
                        deferredDefinition.key()));
        deferred.values.put(deferredDefinition.key(), deferredDefinition);

        assertThrows(IllegalArgumentException.class,
                () -> bind(definitions, emptyDeferred()));
        assertThrows(IllegalArgumentException.class,
                () -> bind(emptyDefinitions(), deferred));
    }

    /** keys 中存在但无对应 value 时必须分别拒绝。 */
    @Test
    void missingValuesFailClosed() {
        DefinitionKey key = key("alpha");
        TestRegistry definitions = new TestRegistry(
                1, 1, Collections.singletonList(key));
        DeferredKey deferredKey = new DeferredKey(
                key, DeferredKind.INFORMATION, 0);
        TestDeferredRegistry deferred = new TestDeferredRegistry(
                1, 1, Collections.singletonList(deferredKey));

        assertThrows(IllegalArgumentException.class,
                () -> bind(definitions, emptyDeferred()));
        assertThrows(IllegalArgumentException.class,
                () -> bind(emptyDefinitions(), deferred));
    }

    /** 外部 map key 与 Definition 内部 identity 不一致时必须分别拒绝。 */
    @Test
    void identityMismatchFailsClosed() {
        DefinitionKey alpha = key("alpha");
        DefinitionKey beta = key("beta");
        TestRegistry definitions = new TestRegistry(
                1, 1, Collections.singletonList(alpha));
        definitions.values.put(alpha, definition(beta));

        DeferredDefinition betaDeferred = deferred(beta, 0);
        DeferredKey alphaDeferredKey = new DeferredKey(
                alpha, DeferredKind.INFORMATION, 0);
        TestDeferredRegistry deferred = new TestDeferredRegistry(
                1, 1, Collections.singletonList(alphaDeferredKey));
        deferred.values.put(alphaDeferredKey, betaDeferred);

        assertThrows(IllegalArgumentException.class,
                () -> bind(definitions, emptyDeferred()));
        assertThrows(IllegalArgumentException.class,
                () -> bind(emptyDefinitions(), deferred));
    }

    /** 快照结束时 size 漂移必须分别拒绝。 */
    @Test
    void finalSizeDriftFailsClosed() {
        DefinitionKey key = key("alpha");
        TestRegistry definitions = new TestRegistry(
                1, 2, Collections.singletonList(key));
        definitions.values.put(key, definition(key));
        DeferredDefinition deferredDefinition = deferred(key, 0);
        TestDeferredRegistry deferred = new TestDeferredRegistry(
                1, 2, Collections.singletonList(deferredDefinition.key()));
        deferred.values.put(deferredDefinition.key(), deferredDefinition);

        assertThrows(IllegalArgumentException.class,
                () -> bind(definitions, emptyDeferred()));
        assertThrows(IllegalArgumentException.class,
                () -> bind(emptyDefinitions(), deferred));
    }

    /** atomic bind 完成后 candidate 构造不得重新读取原 Registry。 */
    @Test
    void originalRegistriesAreNeverReadAfterBind() {
        DefinitionKey key = key("alpha");
        TestRegistry definitions = new TestRegistry(
                1, 1, Collections.singletonList(key));
        definitions.values.put(key, definition(key));
        DeferredDefinition deferredDefinition = deferred(key, 0);
        TestDeferredRegistry deferred = new TestDeferredRegistry(
                1, 1, Collections.singletonList(deferredDefinition.key()));
        deferred.values.put(deferredDefinition.key(), deferredDefinition);

        DigestBoundCompiledInput bound = bind(definitions, deferred);
        definitions.rejectFurtherReads = true;
        deferred.rejectFurtherReads = true;

        EngineContext candidate = new CompiledModelSetBuilder(bound)
                .freeze()
                .candidate(Collections.emptyList());

        assertEquals(1, candidate.compiledModelSet().definitions().size());
        assertEquals(1, candidate.compiledModelSet().deferred().size());
    }

    /** 正式 provenance 构造边界必须拒绝非 64 位小写 SHA-256。 */
    @Test
    void invalidDigestFormatFailsClosed() throws Exception {
        Constructor<DigestBoundCompiledInput> constructor =
                DigestBoundCompiledInput.class.getDeclaredConstructor(
                        PublishedSourceManifest.class,
                        ImmutableRegistry.class,
                        ImmutableDeferredRegistry.class,
                        String.class,
                        String.class,
                        String.class,
                        DigestPair.class);
        constructor.setAccessible(true);

        InvocationTargetException failure = assertThrows(
                InvocationTargetException.class,
                () -> constructor.newInstance(
                        PublishedSourceManifest.empty(),
                        emptyDefinitions(),
                        emptyDeferred(),
                        "compiler",
                        "schema-v1",
                        "options-v1",
                        new DigestPair("not-sha", "also-not-sha")));

        assertTrue(failure.getCause() instanceof IllegalArgumentException);
    }

    /** 最终 Pass 自身不得持有 Publisher 或 PublicationRequest capability。 */
    @Test
    void finalPassDoesNotHoldExternalPublicationCapability() {
        for (Field field : CandidateContextPublicationPass.class
                .getDeclaredFields()) {
            assertFalse(ContextPublisher.class.isAssignableFrom(field.getType()));
            assertFalse(PublicationRequest.class.isAssignableFrom(field.getType()));
        }
    }

    /** Publication Context 关闭后所有只读入口继续拒绝访问。 */
    @Test
    void diagnosticSnapshotRejectsUseAfterContextClose() {
        CompilationSession session = new CompilationSession(
                PipelineTestSupport.request(
                        new PipelineTestSupport.MutableClock(),
                        () -> false,
                        Optional.empty(),
                        new PipelineTestSupport.RecordingObserver()));
        PublicationPassContext context = new PublicationPassContext(session);

        context.close();

        assertThrows(IllegalStateException.class, context::diagnostics);
        assertThrows(IllegalStateException.class, context::request);
    }

    /** 使用指定 Registry 创建真实摘要绑定输入。 */
    private static DigestBoundCompiledInput bind(
            Registry<DefinitionKey, CompiledDefinition> definitions,
            DeferredRegistry deferred) {
        return new CompilerDigestService().bind(
                sourceManifest(),
                PublishedSourceManifest.empty(),
                definitions,
                deferred,
                "compiler-review",
                new CompilationOptions("schema-v1", "options-v1"));
    }

    /** 创建真实 T13 Source Digest 所需的最小 SourceManifest。 */
    private static SourceManifest sourceManifest() {
        DocumentSource source = new DocumentSource(
                "source:root",
                URI.create("memory:/root"),
                DocumentFormat.XML,
                new AllowedRoot(URI.create("memory:/")),
                "<root/>".getBytes(StandardCharsets.UTF_8),
                "fixture-review");
        return new SourceManifest(Collections.singletonList(source));
    }

    /** 创建稳定 DefinitionKey。 */
    private static DefinitionKey key(String value) {
        return new DataKey(value);
    }

    /** 创建内部 identity 与参数一致的 Definition。 */
    private static CompiledDefinition definition(DefinitionKey key) {
        return new CompiledDefinition(
                key,
                new SourceRef("source:root", 1, 1, "/data/" + key.canonical()),
                new NormalizedBody("json", "{}"));
    }

    /** 创建完整 Deferred Definition。 */
    private static DeferredDefinition deferred(
            DefinitionKey owner,
            int ordinal) {
        return new DeferredDefinition(
                new DeferredKey(owner, DeferredKind.INFORMATION, ordinal),
                RequiredStage.P3,
                "P3_INFORMATION",
                new SourceRef("source:root", 2, 1, "/information/test"),
                new NormalizedBody("expression", "test"),
                Collections.singletonList(owner));
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

    /** 可制造 size、枚举、value、identity 和阶段漂移反例的 Registry。 */
    private static final class TestRegistry
            implements Registry<DefinitionKey, CompiledDefinition> {
        private final int initialSize;
        private final int finalSize;
        private final List<DefinitionKey> keys;
        private final Map<DefinitionKey, CompiledDefinition> values =
                new LinkedHashMap<DefinitionKey, CompiledDefinition>();
        private int sizeReads;
        private boolean rejectFurtherReads;

        private TestRegistry(
                int initialSize,
                int finalSize,
                List<DefinitionKey> keys) {
            this.initialSize = initialSize;
            this.finalSize = finalSize;
            this.keys = keys;
        }

        @Override
        public Optional<CompiledDefinition> find(DefinitionKey key) {
            rejectIfClosed();
            return Optional.ofNullable(values.get(key));
        }

        @Override
        public CompiledDefinition require(DefinitionKey key) {
            rejectIfClosed();
            CompiledDefinition value = values.get(key);
            if (value == null) {
                throw new IllegalArgumentException("missing definition");
            }
            return value;
        }

        @Override
        public List<DefinitionKey> keys() {
            rejectIfClosed();
            return keys;
        }

        @Override
        public int size() {
            rejectIfClosed();
            return sizeReads++ == 0 ? initialSize : finalSize;
        }

        private void rejectIfClosed() {
            if (rejectFurtherReads) {
                throw new AssertionError("definitions reread after bind");
            }
        }
    }

    /** 可制造同类反例的 Deferred Registry。 */
    private static final class TestDeferredRegistry
            implements DeferredRegistry {
        private final int initialSize;
        private final int finalSize;
        private final List<DeferredKey> keys;
        private final Map<DeferredKey, DeferredDefinition> values =
                new LinkedHashMap<DeferredKey, DeferredDefinition>();
        private int sizeReads;
        private boolean rejectFurtherReads;

        private TestDeferredRegistry(
                int initialSize,
                int finalSize,
                List<DeferredKey> keys) {
            this.initialSize = initialSize;
            this.finalSize = finalSize;
            this.keys = keys;
        }

        @Override
        public List<DeferredDefinition> requiredBy(RequiredStage stage) {
            rejectIfClosed();
            return Collections.emptyList();
        }

        @Override
        public List<DeferredDefinition> ownedBy(DefinitionKey key) {
            rejectIfClosed();
            return Collections.emptyList();
        }

        @Override
        public Optional<DeferredDefinition> find(DeferredKey key) {
            rejectIfClosed();
            return Optional.ofNullable(values.get(key));
        }

        @Override
        public List<DeferredKey> keys() {
            rejectIfClosed();
            return keys;
        }

        @Override
        public int size() {
            rejectIfClosed();
            return sizeReads++ == 0 ? initialSize : finalSize;
        }

        private void rejectIfClosed() {
            if (rejectFurtherReads) {
                throw new AssertionError("deferred reread after bind");
            }
        }
    }
}
