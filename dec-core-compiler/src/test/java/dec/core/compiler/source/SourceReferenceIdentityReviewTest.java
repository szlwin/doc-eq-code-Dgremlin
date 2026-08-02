package dec.core.compiler.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.context.model.SourceRef;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * I002 独立 Review 使用的真实 fixture 与安全证据保留测试。
 */
class SourceReferenceIdentityReviewTest {
    @Test
    void verifiesExactDeclarationOriginsAgainstMountedMainFixture() {
        SourceTestFixture.InMemoryProvider provider =
                SourceTestFixture.providerFromClasspath(
                        "main-fixture/",
                        SourceTestFixture.FileSetOrder.FORWARD);
        SourceGraphResolutionResult result = new MixSourceResolver().resolve(
                new SourceReference(SourceTestFixture.ROOT),
                provider,
                SourceTestFixture.policy());

        assertEquals(SourceGraphResolutionStatus.RESOLVED, result.status());
        MixSourceGraph graph = result.graph().get();
        Map<String, String> sourceTexts = new HashMap<String, String>();
        sourceTexts.put(
                SourceTestFixture.ROOT,
                readFixture("main-fixture/mix/orm-config.xml"));
        sourceTexts.put(
                SourceTestFixture.SYSTEMS,
                readFixture("main-fixture/mix/system/systems.xml"));

        assertEquals(7, graph.edges().size());
        for (SourceGraphEdge edge : graph.edges()) {
            String sourceText = sourceTexts.get(edge.fromSourceId());
            assertTrue(sourceText != null, "声明来源必须是 root 或 systems fixture");
            Position expected = positionOf(sourceText, declarationToken(edge));
            SourceRef actual = edge.declarationSourceRef();
            assertEquals(expected.line, actual.line(), edge.toString());
            assertEquals(expected.column, actual.column(), edge.toString());
            assertEquals(expectedNodePath(edge.edgeType()), actual.nodePath());
        }
    }

    @Test
    void preservesTraversalAndQueryEvidenceForSourcePolicy() {
        SourceReference traversal = new SourceReference(
                "classpath:mix/system/../orm-config.xml");
        SourceReference encodedTraversal = new SourceReference(
                "classpath:mix/system/%2e%2e/orm-config.xml");
        SourceReference query = new SourceReference(
                "classpath:mix/system/systems.xml?variant=1");

        assertTrue(traversal.value().contains(".."));
        assertTrue(encodedTraversal.value().contains("%2e%2e"));
        assertTrue(query.value().contains("?variant=1"));

        SourceTestFixture.InMemoryProvider provider =
                SourceTestFixture.provider(SourceTestFixture.FileSetOrder.FORWARD);
        int accessCountBefore = provider.accessCount();
        assertEquals(
                SourceGraphResolutionStatus.FAILED,
                new MixSourceResolver().resolve(
                        traversal,
                        provider,
                        SourceTestFixture.policy()).status());
        assertEquals(accessCountBefore, provider.accessCount());
    }

    /**
     * 从 CI 挂载的固定资源读取原始 UTF-8 文本。
     */
    private static String readFixture(String resourceName) {
        InputStream input = SourceReferenceIdentityReviewTest.class
                .getClassLoader()
                .getResourceAsStream(resourceName);
        if (input == null) {
            throw new AssertionError("fixture resource not found: " + resourceName);
        }
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                output.write(buffer, 0, read);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException readFailure) {
            throw new AssertionError(
                    "unable to read fixture resource: " + resourceName,
                    readFailure);
        } finally {
            try {
                input.close();
            } catch (IOException ignored) {
                // 测试资源关闭失败不改变已读取的固定文本事实。
            }
        }
    }

    /**
     * 返回当前边在原始 XML 中对应的声明 start-tag 前缀。
     */
    private static String declarationToken(SourceGraphEdge edge) {
        switch (edge.edgeType()) {
            case ROOT_DATA_FILESET:
            case ROOT_VIEW_FILESET:
                return "<orm-file path=\"" + edge.targetReference().value() + "\"";
            case ROOT_SYSTEM_FILE:
                return "<system-file path=\""
                        + edge.targetReference().value()
                        + "\"";
            case ROOT_BUSINESS_FILE:
                return "<business-file path=\""
                        + edge.targetReference().value()
                        + "\"";
            case SYSTEM_RULE_FILE:
                return "<rule-file path=\""
                        + edge.targetReference().value()
                        + "\"";
            default:
                throw new AssertionError("unexpected edge type: " + edge.edgeType());
        }
    }

    /**
     * 返回每类声明冻结的节点路径。
     */
    private static String expectedNodePath(SourceEdgeType edgeType) {
        switch (edgeType) {
            case ROOT_DATA_FILESET:
                return "/orm-config/orm-data-file-info/orm-file";
            case ROOT_VIEW_FILESET:
                return "/orm-config/orm-view-file-info/orm-file";
            case ROOT_SYSTEM_FILE:
                return "/orm-config/system-file-info/system-file";
            case ROOT_BUSINESS_FILE:
                return "/orm-config/business-file-info/business-file";
            case SYSTEM_RULE_FILE:
                return "/systems/system/rule-file-info/rule-file";
            default:
                throw new AssertionError("unexpected edge type: " + edgeType);
        }
    }

    /**
     * 独立按原始文本索引计算声明 `<` 的 1-based 行和列。
     */
    private static Position positionOf(String source, String token) {
        int offset = source.indexOf(token);
        if (offset < 0) {
            throw new AssertionError("declaration token not found: " + token);
        }
        int line = 1;
        int lineStart = 0;
        for (int index = 0; index < offset; index++) {
            char current = source.charAt(index);
            if (current == '\r') {
                if (index + 1 < offset && source.charAt(index + 1) == '\n') {
                    index++;
                }
                line++;
                lineStart = index + 1;
            } else if (current == '\n') {
                line++;
                lineStart = index + 1;
            }
        }
        return new Position(line, offset - lineStart + 1);
    }

    /**
     * 原始文本位置值对象。
     */
    private static final class Position {
        private final int line;
        private final int column;

        private Position(int line, int column) {
            this.line = line;
            this.column = column;
        }
    }
}
