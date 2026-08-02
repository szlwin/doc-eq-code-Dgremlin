package dec.core.compiler.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
     * 按 sourceId 稳定排序并冻结 Source，同时拒绝重复身份。
     */
    public SourceManifest(List<DocumentSource> sources) {
        Objects.requireNonNull(sources, "sources");
        List<DocumentSource> sourceCopy = new ArrayList<DocumentSource>(
                sources.size());
        for (DocumentSource source : sources) {
            sourceCopy.add(Objects.requireNonNull(
                    source,
                    "sources contains null"));
        }
        Collections.sort(
                sourceCopy,
                Comparator.comparing(DocumentSource::sourceId));

        List<String> idCopy = new ArrayList<String>(sourceCopy.size());
        long bytes = 0L;
        String previousId = null;
        for (DocumentSource source : sourceCopy) {
            if (source.sourceId().equals(previousId)) {
                throw new IllegalArgumentException(
                        "sourceId must be unique: " + source.sourceId());
            }
            idCopy.add(source.sourceId());
            previousId = source.sourceId();
            bytes += source.content().length;
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
