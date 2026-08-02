package dec.core.compiler.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 单次 Source discovery 冻结的不可变 Source 清单。
 */
public final class SourceManifest {
    private final List<DocumentSource> sources;
    private final List<String> sourceIds;
    private final long totalBytes;

    /**
     * 按调用方已确定的稳定顺序冻结 Source，并计算总字节数。
     */
    public SourceManifest(List<DocumentSource> sources) {
        Objects.requireNonNull(sources, "sources");
        List<DocumentSource> sourceCopy = new ArrayList<DocumentSource>(
                sources.size());
        List<String> idCopy = new ArrayList<String>(sources.size());
        long bytes = 0L;
        for (DocumentSource source : sources) {
            DocumentSource checked = Objects.requireNonNull(
                    source,
                    "sources contains null");
            sourceCopy.add(checked);
            idCopy.add(checked.sourceId());
            bytes += checked.content().length;
        }
        this.sources = Collections.unmodifiableList(sourceCopy);
        this.sourceIds = Collections.unmodifiableList(idCopy);
        this.totalBytes = bytes;
    }

    public List<DocumentSource> sources() {
        return sources;
    }

    public List<String> sourceIds() {
        return sourceIds;
    }

    public long totalBytes() {
        return totalBytes;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof SourceManifest
                && sources.equals(((SourceManifest) other).sources));
    }

    @Override
    public int hashCode() {
        return sources.hashCode();
    }

    @Override
    public String toString() {
        return "SourceManifest{sources=" + sourceIds
                + ", totalBytes=" + totalBytes + '}';
    }
}
