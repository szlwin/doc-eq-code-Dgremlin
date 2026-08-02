package dec.core.compiler.api;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.CanonicalDocumentNode;
import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.canonical.DocumentFrontend;
import dec.core.compiler.canonical.FrontendOptions;
import dec.core.compiler.canonical.FrontendRegistry;
import dec.core.compiler.canonical.FrontendResult;
import dec.core.compiler.canonical.FrontendResults;
import dec.core.compiler.canonical.FrontendStatus;
import dec.core.compiler.source.AllowedRoot;
import dec.core.compiler.source.DocumentSource;
import dec.core.compiler.source.DocumentSourceProvider;
import dec.core.compiler.source.SourceReference;
import dec.core.compiler.source.SourceResolutionContext;
import dec.core.compiler.source.SourceResolutionResult;
import dec.core.compiler.source.SourceResolutionStatus;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * 验证 TASK-P1-T02 I004 的 Source 安全事实、Canonical 值语义和最小数据流。
 */
class CompilerSourceFrontendClosureBehaviorR04Test {
    @Test
    void documentSourceFreezesFormatUriAllowedRootAndContent() {
        AllowedRoot allowedRoot = new AllowedRoot(
                URI.create("file:///workspace/config/"));
        byte[] mutableContent = new byte[] {1, 2, 3};
        DocumentSource source = new DocumentSource(
                "source:mix",
                URI.create("file:///workspace/config/./mix.xml"),
                DocumentFormat.XML,
                allowedRoot,
                mutableContent,
                "sha256:abc");

        mutableContent[0] = 9;
        byte[] returnedContent = source.content();
        returnedContent[1] = 9;

        assertEquals("source:mix", source.sourceId());
        assertEquals(URI.create("file:///workspace/config/mix.xml"), source.uri());
        assertEquals(DocumentFormat.XML, source.format());
        assertSame(allowedRoot, source.allowedRoot());
        assertArrayEquals(new byte[] {1, 2, 3}, source.content());
        assertEquals("sha256:abc", source.contentDigest());
        assertTrue(allowedRoot.contains(source.uri()));
        assertFalse(allowedRoot.contains(
                URI.create("file:///workspace/configuration/mix.xml")));

        assertThrows(
                IllegalArgumentException.class,
                () -> new DocumentSource(
                        "source:outside",
                        URI.create("file:///workspace/other/mix.xml"),
                        DocumentFormat.XML,
                        allowedRoot,
                        new byte[] {1},
                        "sha256:outside"));
        assertThrows(
                IllegalArgumentException.class,
                () -> new AllowedRoot(URI.create("relative/config/")));
    }

    @Test
    void canonicalNodeDefensivelyFreezesFormatNeutralTree() {
        CanonicalDocumentNode child = node("child", "/root/child");
        Map<String, String> mutableAttributes = new HashMap<String, String>();
        mutableAttributes.put("z", "last");
        mutableAttributes.put("a", "first");
        List<CanonicalDocumentNode> mutableChildren =
                new ArrayList<CanonicalDocumentNode>();
        mutableChildren.add(child);

        CanonicalDocumentNode root = new CanonicalDocumentNode(
                "root",
                mutableAttributes,
                Optional.of("value"),
                mutableChildren,
                new SourceRef("source:mix", 1, 1, "/root"),
                DocumentFormat.XML,
                "schema-1");
        mutableAttributes.clear();
        mutableChildren.clear();

        assertEquals("root", root.name());
        assertEquals(Arrays.asList("a", "z"),
                new ArrayList<String>(root.attributes().keySet()));
        assertEquals(Optional.of("value"), root.scalar());
        assertEquals(Collections.singletonList(child), root.children());
        assertEquals("source:mix", root.sourceRef().sourceId());
        assertEquals(DocumentFormat.XML, root.format());
        assertEquals("schema-1", root.schemaVersion());
        assertThrows(
                UnsupportedOperationException.class,
                () -> root.attributes().put("new", "value"));
        assertThrows(
                UnsupportedOperationException.class,
                () -> root.children().add(child));
    }

    @Test
    void frontendFactoriesEnforceCanonicalSuccessAndDiagnosticFailure() {
        CanonicalDocumentNode canonicalRoot = node("root", "/root");
        Diagnostic warning = diagnostic(DiagnosticSeverity.WARNING);
        Diagnostic error = diagnostic(DiagnosticSeverity.ERROR);

        FrontendResult parsed = FrontendResults.parsed(
                canonicalRoot,
                Collections.singletonList(warning));
        assertEquals(FrontendStatus.PARSED, parsed.status());
        assertSame(canonicalRoot, parsed.canonicalRoot().get());
        assertEquals(Collections.singletonList(warning), parsed.diagnostics());
        assertThrows(
                IllegalArgumentException.class,
                () -> FrontendResults.parsed(
                        canonicalRoot,
                        Collections.singletonList(error)));

        FrontendResult failed = FrontendResults.failed(
                Collections.singletonList(error));
        assertEquals(FrontendStatus.FAILED, failed.status());
        assertFalse(failed.canonicalRoot().isPresent());
        assertEquals(Collections.singletonList(error), failed.diagnostics());
        assertThrows(
                IllegalArgumentException.class,
                () -> FrontendResults.failed(
                        Collections.singletonList(warning)));
        assertThrows(
                UnsupportedOperationException.class,
                () -> failed.diagnostics().add(error));
    }

    @Test
    void providerFrontendCanonicalFlowUsesOnlyFrozenPublicContracts() {
        SourceReference rootReference = new SourceReference("file:/workspace/config/mix.xml");
        AllowedRoot allowedRoot = new AllowedRoot(
                URI.create("file:///workspace/config/"));
        DocumentSource source = new DocumentSource(
                "source:mix",
                URI.create("file:///workspace/config/mix.xml"),
                DocumentFormat.XML,
                allowedRoot,
                new byte[] {1, 2, 3},
                "sha256:abc");
        CanonicalDocumentNode canonicalRoot = node("root", "/root");
        FrontendResult parsedResult = parsedResult(canonicalRoot);
        SourceResolutionResult resolutionResult = resolvedResult(source);
        DocumentSourceProvider provider = provider(resolutionResult);
        DocumentFrontend frontend = frontend(parsedResult);
        FrontendRegistry registry = format -> {
            assertEquals(DocumentFormat.XML, format);
            return frontend;
        };

        SourceResolutionResult resolved = provider.resolve(
                rootReference,
                resolutionContext(rootReference));
        DocumentSource resolvedSource = resolved.sources().get(0);
        DocumentFrontend selectedFrontend = registry.require(resolvedSource.format());
        FrontendResult frontendResult = selectedFrontend.parse(
                resolvedSource,
                new FrontendOptions("schema-1"));
        CanonicalDocumentNode resolvedCanonical = frontendResult.canonicalRoot().get();

        assertEquals(SourceResolutionStatus.RESOLVED, resolved.status());
        assertSame(source, resolvedSource);
        assertSame(canonicalRoot, resolvedCanonical);
        assertEquals(resolvedSource.sourceId(), resolvedCanonical.sourceRef().sourceId());
    }

    /**
     * 创建格式中立测试节点。
     */
    private static CanonicalDocumentNode node(String name, String path) {
        return new CanonicalDocumentNode(
                name,
                Collections.<String, String>emptyMap(),
                Optional.<String>empty(),
                Collections.<CanonicalDocumentNode>emptyList(),
                new SourceRef("source:mix", 1, 1, path),
                DocumentFormat.XML,
                "schema-1");
    }

    /**
     * 创建用于成功或失败不变量验证的 Diagnostic。
     */
    private static Diagnostic diagnostic(DiagnosticSeverity severity) {
        return new Diagnostic(
                DiagnosticCode.MIX_FRONTEND_XML_UNSAFE,
                severity,
                "frontend.test",
                null,
                new SourceRef("source:mix", 1, 1, "/root"),
                Collections.<SourceRef>emptyList(),
                "Review frontend input",
                "FrontendPass");
    }

    /**
     * 创建不依赖工厂实现的 PARSED 结果，用于验证公共数据流形状。
     */
    private static FrontendResult parsedResult(
            final CanonicalDocumentNode canonicalRoot) {
        return new FrontendResult() {
            @Override
            public FrontendStatus status() {
                return FrontendStatus.PARSED;
            }

            @Override
            public Optional<CanonicalDocumentNode> canonicalRoot() {
                return Optional.of(canonicalRoot);
            }

            @Override
            public List<Diagnostic> diagnostics() {
                return Collections.emptyList();
            }
        };
    }

    /**
     * 创建恰好返回一个 Source 的 RESOLVED 结果。
     */
    private static SourceResolutionResult resolvedResult(
            final DocumentSource source) {
        return new SourceResolutionResult() {
            @Override
            public SourceResolutionStatus status() {
                return SourceResolutionStatus.RESOLVED;
            }

            @Override
            public List<DocumentSource> sources() {
                return Collections.singletonList(source);
            }

            @Override
            public List<Diagnostic> diagnostics() {
                return Collections.emptyList();
            }
        };
    }

    /**
     * 创建返回稳定解析结果的 Source Provider。
     */
    private static DocumentSourceProvider provider(
            final SourceResolutionResult resolutionResult) {
        return new DocumentSourceProvider() {
            @Override
            public SourceResolutionResult resolve(
                    SourceReference reference,
                    SourceResolutionContext context) {
                return resolutionResult;
            }

            @Override
            public SourceResolutionResult resolveFileSet(
                    SourceReference reference,
                    SourceResolutionContext context) {
                return resolutionResult;
            }
        };
    }

    /**
     * 创建返回预构造 Canonical 结果的 Frontend。
     */
    private static DocumentFrontend frontend(final FrontendResult result) {
        return new DocumentFrontend() {
            @Override
            public DocumentFormat format() {
                return DocumentFormat.XML;
            }

            @Override
            public FrontendResult parse(
                    DocumentSource source,
                    FrontendOptions options) {
                assertEquals(DocumentFormat.XML, source.format());
                assertEquals("schema-1", options.schemaVersion());
                return result;
            }
        };
    }

    /**
     * 创建根 Source 的会话级解析上下文。
     */
    private static SourceResolutionContext resolutionContext(
            final SourceReference root) {
        return new SourceResolutionContext() {
            @Override
            public SourceReference root() {
                return root;
            }

            @Override
            public Optional<String> parentSourceId() {
                return Optional.empty();
            }

            @Override
            public int depth() {
                return 0;
            }
        };
    }
}
