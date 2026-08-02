package dec.core.compiler.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.source.AllowedRoot;
import java.net.URI;
import org.junit.jupiter.api.Test;

/**
 * 验证 AllowedRoot 对等价根、兄弟前缀和编码穿越的词法安全边界。
 */
class CompilerSourceSecurityR04Test {
    @Test
    void treatsTrailingSlashVariantsAsTheSameRootBoundary() {
        AllowedRoot root = new AllowedRoot(
                URI.create("file:///workspace/config/"));

        assertTrue(root.contains(URI.create("file:///workspace/config")));
        assertTrue(root.contains(URI.create("file:///workspace/config/")));
        assertTrue(root.contains(URI.create("file:///workspace/config/mix.xml")));
        assertFalse(root.contains(
                URI.create("file:///workspace/configuration/mix.xml")));
    }

    @Test
    void rejectsEncodedTraversalQueryFragmentAndDifferentAuthority() {
        AllowedRoot root = new AllowedRoot(
                URI.create("https://example.test/config/"));

        assertFalse(root.contains(
                URI.create("https://example.test/config/%2e%2e/secret.xml")));
        assertFalse(root.contains(
                URI.create("https://example.test/config/mix.xml?raw=true")));
        assertFalse(root.contains(
                URI.create("https://example.test/config/mix.xml#fragment")));
        assertFalse(root.contains(
                URI.create("https://other.test/config/mix.xml")));
        assertThrows(
                IllegalArgumentException.class,
                () -> new AllowedRoot(
                        URI.create("https://example.test/config/?raw=true")));
    }
}
