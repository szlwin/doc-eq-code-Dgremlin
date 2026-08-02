package dec.core.compiler.canonical.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.resolver.Resolver;

/**
 * I003 独立 Architecture / Security Review 的 scalar policy 结构 Oracle。
 */
class YamlScalarPolicyArchitectureReviewTest {

    /**
     * 编译产物必须依赖 SnakeYAML Resolver，且不能引用任意精度数值构造类。
     */
    @Test
    void usesResolverWithoutArbitraryPrecisionNumberConstruction() throws IOException {
        byte[] classBytes = readClassBytes(YamlScalarLexemePolicy.class);
        String constantPool = new String(classBytes, StandardCharsets.ISO_8859_1);

        assertTrue(constantPool.contains(
                "org/yaml/snakeyaml/resolver/Resolver"));
        assertFalse(constantPool.contains("java/math/BigDecimal"));
        assertFalse(constantPool.contains("java/math/BigInteger"));
    }

    /**
     * 代表性 int/float 词法必须与固定 SnakeYAML 2.2 Resolver Pattern 一致。
     */
    @Test
    void matchesFixedResolverForRepresentativeNumericLexemes() {
        List<String> integers = Arrays.asList(
                "0",
                "42",
                "0b101",
                "0x2a",
                "077",
                "09",
                "0b_",
                "0x_",
                "0_");
        for (String value : integers) {
            assertEquals(
                    Resolver.INT.matcher(value).matches(),
                    YamlScalarLexemePolicy.isValid(Tag.INT, value),
                    value);
        }

        List<String> floats = Arrays.asList(
                "1e3",
                "1.2e3",
                "1e+3",
                "1e-3",
                ".5",
                "1.",
                ".inf",
                ".NaN",
                ".",
                "not-a-float");
        for (String value : floats) {
            assertEquals(
                    Resolver.FLOAT.matcher(value).matches(),
                    YamlScalarLexemePolicy.isValid(Tag.FLOAT, value),
                    value);
        }
    }

    /**
     * 读取指定类的编译字节，用于稳定检查常量池依赖而不读取源码文件。
     */
    private static byte[] readClassBytes(Class<?> type) throws IOException {
        String resourceName = "/" + type.getName().replace('.', '/') + ".class";
        InputStream input = type.getResourceAsStream(resourceName);
        assertNotNull(input, resourceName);
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1_024];
            int count;
            while ((count = input.read(buffer)) >= 0) {
                output.write(buffer, 0, count);
            }
            return output.toByteArray();
        } finally {
            input.close();
        }
    }
}
