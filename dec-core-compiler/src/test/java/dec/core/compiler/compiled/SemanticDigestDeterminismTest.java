package dec.core.compiler.compiled;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.source.AllowedRoot;
import dec.core.compiler.source.DocumentSource;
import dec.core.compiler.source.SourceManifest;
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
import dec.core.context.model.PublishedSourceDependency;
import dec.core.context.model.PublishedSourceDescriptor;
import dec.core.context.model.PublishedSourceManifest;
import dec.core.context.model.Registry;
import dec.core.context.model.RequiredStage;
import dec.core.context.model.SourceRef;
import dec.core.context.model.SystemKey;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T13 / I001：确定性 semantic/source digest 的有效 RED Oracle。
 */
class SemanticDigestDeterminismTest {

    /** T13 必须提供独立 compiled 包中的正式输入和摘要服务。 */
    @Test
    void digestTypesExistWithStablePublicContract() {
        Class<?> inputType = loadClass("dec.core.compiler.compiled.SemanticDigestInput");
        Class<?> serviceType = loadClass("dec.core.compiler.compiled.CompilerDigestService");

        assertNotNull(inputType);
        assertNotNull(serviceType);
        assertNotNull(findMethod(inputType, "canonicalJson"));
        assertNotNull(findMethod(serviceType, "compute", SourceManifest.class, inputType));
    }

    /** Registry 插入顺序、Source 格式和物理行列不得影响 semantic digest。 */
    @Test
    void semanticDigestIgnoresOrderingFormatAndPhysicalCoordinates() {
        Object leftInput = semanticInput(
                publishedManifest("XML", "content-xml", 10, 20),
                definitions(false, 10, 20),
                deferred(false, 10, 20),
                "compiler-1",
                "schema-1",
                "options-1");
        Object rightInput = semanticInput(
                publishedManifest("YAML", "content-yaml", 700, 900),
                definitions(true, 700, 900),
                deferred(true, 700, 900),
                "compiler-1",
                "schema-1",
                "options-1");

        DigestPair left = compute(sourceManifest(false, "same-source"), leftInput);
        DigestPair right = compute(sourceManifest(true, "same-source"), rightInput);

        assertEquals(left.semanticDigest(), right.semanticDigest());
        assertEquals(canonicalJson(leftInput), canonicalJson(rightInput));
    }

    /** 原始 Source 内容变化必须改变 sourceDigest，但语义输入不变时 semanticDigest 保持。 */
    @Test
    void rawSourceChangeOnlyChangesSourceDigest() {
        Object input = semanticInput(
                publishedManifest("XML", "published-content", 1, 1),
                definitions(false, 1, 1),
                deferred(false, 1, 1),
                "compiler-1",
                "schema-1",
                "options-1");

        DigestPair first = compute(sourceManifest(false, "raw-a"), input);
        DigestPair second = compute(sourceManifest(false, "raw-b"), input);

        assertNotEquals(first.sourceDigest(), second.sourceDigest());
        assertEquals(first.semanticDigest(), second.semanticDigest());
    }

    /** compiler/schema/options 版本域任一变化都必须改变 semantic digest。 */
    @Test
    void versionDomainParticipatesInSemanticDigest() {
        PublishedSourceManifest manifest = publishedManifest("XML", "ignored", 1, 1);
        Registry<DefinitionKey, CompiledDefinition> definitions = definitions(false, 1, 1);
        DeferredRegistry deferred = deferred(false, 1, 1);
        String base = compute(sourceManifest(false, "raw"), semanticInput(
                manifest, definitions, deferred,
                "compiler-1", "schema-1", "options-1")).semanticDigest();

        assertNotEquals(base, compute(sourceManifest(false, "raw"), semanticInput(
                manifest, definitions, deferred,
                "compiler-2", "schema-1", "options-1")).semanticDigest());
        assertNotEquals(base, compute(sourceManifest(false, "raw"), semanticInput(
                manifest, definitions, deferred,
                "compiler-1", "schema-2", "options-1")).semanticDigest());
        assertNotEquals(base, compute(sourceManifest(false, "raw"), semanticInput(
                manifest, definitions, deferred,
                "compiler-1", "schema-1", "options-2")).semanticDigest());
    }

    /** canonical JSON 不得泄漏 line/column、Source content digest 或格式。 */
    @Test
    void canonicalJsonExcludesPhysicalAndSourceContentFacts() {
        Object input = semanticInput(
                publishedManifest("YAML", "secret-content-digest", 77, 88),
                definitions(false, 77, 88),
                deferred(false, 77, 88),
                "compiler-1",
                "schema-1",
                "options-1");

        String json = canonicalJson(input);

        assertFalse(json.contains("secret-content-digest"));
        assertFalse(json.contains("YAML"));
        assertFalse(json.contains("\"line\""));
        assertFalse(json.contains("\"column\""));
        assertTrue(json.contains("DEC-SEMANTIC-DIGEST-V1"));
    }

    /** 摘要必须是稳定的 64 位小写 SHA-256 十六进制文本。 */
    @Test
    void digestsUseLowercaseSha256Hex() {
        DigestPair pair = compute(
                sourceManifest(false, "raw"),
                semanticInput(
                        publishedManifest("XML", "ignored", 1, 1),
                        definitions(false, 1, 1),
                        deferred(false, 1, 1),
                        "compiler-1",
                        "schema-1",
                        "options-1"));

        assertTrue(pair.sourceDigest().matches("[0-9a-f]{64}"));
        assertTrue(pair.semanticDigest().matches("[0-9a-f]{64}"));
    }

    /** 通过反射实例化尚未存在的 T13 类型，保证 RED 为测试失败而非 testCompile 失败。 */
    private static Object semanticInput(
            PublishedSourceManifest manifest,
            Registry<DefinitionKey, CompiledDefinition> definitions,
            DeferredRegistry deferred,
            String compilerVersion,
            String schemaVersion,
            String optionsDigest) {
        try {
            Class<?> type = loadClass("dec.core.compiler.compiled.SemanticDigestInput");
            Constructor<?> constructor = type.getConstructor(
                    PublishedSourceManifest.class,
                    Registry.class,
                    DeferredRegistry.class,
                    String.class,
                    String.class,
                    String.class);
            return constructor.newInstance(
                    manifest,
                    definitions,
                    deferred,
                    compilerVersion,
                    schemaVersion,
                    optionsDigest);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("T13 SemanticDigestInput contract missing", failure);
        }
    }

    /** 调用正式摘要服务并返回 Context 中立 DigestPair。 */
    private static DigestPair compute(SourceManifest sources, Object input) {
        try {
            Class<?> serviceType = loadClass(
                    "dec.core.compiler.compiled.CompilerDigestService");
            Object service = serviceType.getConstructor().newInstance();
            Method method = serviceType.getMethod(
                    "compute",
                    SourceManifest.class,
                    input.getClass());
            return (DigestPair) method.invoke(service, sources, input);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("T13 digest service contract missing", failure);
        }
    }

    /** 读取 canonical JSON 文本。 */
    private static String canonicalJson(Object input) {
        try {
            return (String) input.getClass().getMethod("canonicalJson").invoke(input);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError("T13 canonicalJson contract missing", failure);
        }
    }

    /** 加载 T13 类型；不存在时转换为稳定测试失败。 */
    private static Class<?> loadClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException failure) {
            throw new AssertionError("Missing T13 type: " + name, failure);
        }
    }

    /** 查找正式公开方法。 */
    private static Method findMethod(
            Class<?> type,
            String name,
            Class<?>... parameterTypes) {
        try {
            return type.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException failure) {
            throw new AssertionError("Missing T13 method: " + name, failure);
        }
    }

    /** 构造两个 Source，输入顺序可反转但 SourceManifest 会稳定排序。 */
    private static SourceManifest sourceManifest(boolean reverse, String rootContent) {
        DocumentSource root = source("source:root", rootContent);
        DocumentSource child = source("source:child", "child");
        List<DocumentSource> values = reverse
                ? Arrays.asList(root, child)
                : Arrays.asList(child, root);
        return new SourceManifest(values);
    }

    /** 构造最小 Source。 */
    private static DocumentSource source(String id, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return new DocumentSource(
                id,
                URI.create("memory:/" + id.substring(id.indexOf(':') + 1)),
                DocumentFormat.XML,
                new AllowedRoot(URI.create("memory:/")),
                bytes,
                "fixture-" + content);
    }

    /** 构造格式/content digest/物理坐标不同但语义相同的发布视图。 */
    private static PublishedSourceManifest publishedManifest(
            String format,
            String contentDigest,
            int line,
            int column) {
        List<PublishedSourceDescriptor> sources = Arrays.asList(
                new PublishedSourceDescriptor("source:root", format, contentDigest),
                new PublishedSourceDescriptor("source:child", format, contentDigest + "-child"));
        List<PublishedSourceDependency> dependencies = Collections.singletonList(
                new PublishedSourceDependency(
                        "ROOT_SYSTEM_FILE",
                        "source:root",
                        "source:child",
                        new SourceRef("source:root", line, column, "/root/system")));
        return new PublishedSourceManifest("source:root", sources, dependencies);
    }

    /** 构造插入顺序和物理坐标可变化的 Definition Registry。 */
    private static Registry<DefinitionKey, CompiledDefinition> definitions(
            boolean reverse,
            int line,
            int column) {
        DefinitionKey alpha = new DataKey("alpha");
        DefinitionKey beta = new DataKey("beta");
        CompiledDefinition alphaDefinition = new CompiledDefinition(
                alpha,
                new SourceRef("source:root", line, column, "/data/alpha"),
                new NormalizedBody("json", "{\"name\":\"alpha\"}"));
        CompiledDefinition betaDefinition = new CompiledDefinition(
                beta,
                new SourceRef("source:child", line + 1, column + 1, "/data/beta"),
                new NormalizedBody("json", "{\"name\":\"beta\"}"));
        Map<DefinitionKey, CompiledDefinition> values =
                new LinkedHashMap<DefinitionKey, CompiledDefinition>();
        if (reverse) {
            values.put(beta, betaDefinition);
            values.put(alpha, alphaDefinition);
        } else {
            values.put(alpha, alphaDefinition);
            values.put(beta, betaDefinition);
        }
        return new ImmutableRegistry<DefinitionKey, CompiledDefinition>(values);
    }

    /** 构造插入顺序和物理坐标可变化的 Deferred Registry。 */
    private static DeferredRegistry deferred(
            boolean reverse,
            int line,
            int column) {
        DefinitionKey owner = new SystemKey("common");
        DeferredDefinition first = deferredDefinition(owner, 0, "first", line, column);
        DeferredDefinition second = deferredDefinition(
                owner,
                1,
                "second",
                line + 1,
                column + 1);
        Map<DeferredKey, DeferredDefinition> values =
                new LinkedHashMap<DeferredKey, DeferredDefinition>();
        if (reverse) {
            values.put(second.key(), second);
            values.put(first.key(), first);
        } else {
            values.put(first.key(), first);
            values.put(second.key(), second);
        }
        return new ImmutableDeferredRegistry(values);
    }

    /** 创建完整 Deferred Definition。 */
    private static DeferredDefinition deferredDefinition(
            DefinitionKey owner,
            int ordinal,
            String name,
            int line,
            int column) {
        return new DeferredDefinition(
                new DeferredKey(owner, DeferredKind.INFORMATION, ordinal),
                RequiredStage.P3,
                "P3_INFORMATION",
                new SourceRef("source:root", line, column, "/information/" + name),
                new NormalizedBody("expression", name),
                new ArrayList<DefinitionKey>(Collections.singletonList(owner)));
    }
}
