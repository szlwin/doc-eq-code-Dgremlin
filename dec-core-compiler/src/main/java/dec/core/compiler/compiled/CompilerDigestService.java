package dec.core.compiler.compiled;

import dec.core.compiler.source.DocumentSource;
import dec.core.compiler.source.SourceManifest;
import dec.core.context.model.DigestPair;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 计算原始 Source 摘要和 DEC-SEMANTIC-DIGEST-V1 语义摘要。
 */
public final class CompilerDigestService {
    private static final byte[] SOURCE_DOMAIN =
            "DEC-SOURCE-DIGEST-V1".getBytes(StandardCharsets.UTF_8);

    /** 创建无状态摘要服务。 */
    public CompilerDigestService() {
    }

    /**
     * 使用同一不可变输入计算 Source/Semantic 摘要对。
     *
     * @param sources 原始 Source 内容清单
     * @param semanticInput 已冻结的语义输入
     * @return 两个小写 SHA-256 十六进制摘要
     */
    public DigestPair compute(
            SourceManifest sources,
            SemanticDigestInput semanticInput) {
        SourceManifest checkedSources = Objects.requireNonNull(sources, "sources");
        SemanticDigestInput checkedSemantic = Objects.requireNonNull(
                semanticInput,
                "semanticInput");
        return new DigestPair(
                sourceDigest(checkedSources),
                sha256Hex(checkedSemantic.canonicalJson().getBytes(
                        StandardCharsets.UTF_8)));
    }

    /** 对 Source 身份和原始字节执行长度前缀增量摘要。 */
    private static String sourceDigest(SourceManifest manifest) {
        MessageDigest digest = sha256();
        updateBytes(digest, SOURCE_DOMAIN);
        List<DocumentSource> sources = new ArrayList<DocumentSource>(
                manifest.sources());
        Collections.sort(sources, new Comparator<DocumentSource>() {
            @Override
            public int compare(DocumentSource left, DocumentSource right) {
                return CanonicalJsonWriter.codePointOrder().compare(
                        left.sourceId(),
                        right.sourceId());
            }
        });
        updateInt(digest, sources.size());
        for (DocumentSource source : sources) {
            DocumentSource checked = Objects.requireNonNull(source, "source");
            updateBytes(
                    digest,
                    checked.sourceId().getBytes(StandardCharsets.UTF_8));
            updateBytes(digest, checked.content());
        }
        return toHex(digest.digest());
    }

    /** 计算单个字节闭包的 SHA-256。 */
    private static String sha256Hex(byte[] value) {
        MessageDigest digest = sha256();
        digest.update(Objects.requireNonNull(value, "value"));
        return toHex(digest.digest());
    }

    /** 创建 JVM 必须提供的 SHA-256 实例。 */
    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is not available", impossible);
        }
    }

    /** 使用四字节大端长度前缀避免字节串连接歧义。 */
    private static void updateBytes(MessageDigest digest, byte[] value) {
        byte[] checked = Objects.requireNonNull(value, "value");
        updateInt(digest, checked.length);
        digest.update(checked);
    }

    /** 更新四字节大端整数。 */
    private static void updateInt(MessageDigest digest, int value) {
        digest.update(ByteBuffer.allocate(4).putInt(value).array());
    }

    /** 将摘要转换为固定小写十六进制。 */
    private static String toHex(byte[] value) {
        StringBuilder result = new StringBuilder(value.length * 2);
        for (byte item : value) {
            int unsigned = item & 0xff;
            if (unsigned < 0x10) {
                result.append('0');
            }
            result.append(Integer.toHexString(unsigned));
        }
        return result.toString();
    }
}
