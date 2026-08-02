package dec.core.compiler.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * 单次 Source discovery 的不可变安全边界和资源预算。
 */
public final class SourcePolicy {
    private final Set<String> allowedSchemes;
    private final AllowedRoot allowedRoot;
    private final int maxDepth;
    private final int maxSources;
    private final long maxTotalBytes;

    /**
     * 冻结 scheme 白名单、允许根和资源限制。
     */
    public SourcePolicy(
            Set<String> allowedSchemes,
            AllowedRoot allowedRoot,
            int maxDepth,
            int maxSources,
            long maxTotalBytes) {
        Objects.requireNonNull(allowedSchemes, "allowedSchemes");
        List<String> normalized = new ArrayList<String>(allowedSchemes.size());
        for (String scheme : allowedSchemes) {
            String value = Objects.requireNonNull(
                    scheme,
                    "allowedSchemes contains null").trim().toLowerCase(Locale.ROOT);
            if (value.isEmpty()) {
                throw new IllegalArgumentException(
                        "allowedSchemes must not contain blank values");
            }
            normalized.add(value);
        }
        Collections.sort(normalized);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("allowedSchemes must not be empty");
        }
        this.allowedSchemes = Collections.unmodifiableSet(
                new LinkedHashSet<String>(normalized));
        this.allowedRoot = Objects.requireNonNull(allowedRoot, "allowedRoot");
        if (maxDepth < 0) {
            throw new IllegalArgumentException("maxDepth must be >= 0");
        }
        if (maxSources <= 0) {
            throw new IllegalArgumentException("maxSources must be > 0");
        }
        if (maxTotalBytes <= 0L) {
            throw new IllegalArgumentException("maxTotalBytes must be > 0");
        }
        this.maxDepth = maxDepth;
        this.maxSources = maxSources;
        this.maxTotalBytes = maxTotalBytes;
    }

    public Set<String> allowedSchemes() {
        return allowedSchemes;
    }

    public AllowedRoot allowedRoot() {
        return allowedRoot;
    }

    public int maxDepth() {
        return maxDepth;
    }

    public int maxSources() {
        return maxSources;
    }

    public long maxTotalBytes() {
        return maxTotalBytes;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SourcePolicy)) {
            return false;
        }
        SourcePolicy that = (SourcePolicy) other;
        return maxDepth == that.maxDepth
                && maxSources == that.maxSources
                && maxTotalBytes == that.maxTotalBytes
                && allowedSchemes.equals(that.allowedSchemes)
                && allowedRoot.equals(that.allowedRoot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                allowedSchemes,
                allowedRoot,
                maxDepth,
                maxSources,
                maxTotalBytes);
    }

    @Override
    public String toString() {
        return "SourcePolicy{"
                + "allowedSchemes=" + allowedSchemes
                + ", allowedRoot=" + allowedRoot
                + ", maxDepth=" + maxDepth
                + ", maxSources=" + maxSources
                + ", maxTotalBytes=" + maxTotalBytes
                + '}';
    }
}
