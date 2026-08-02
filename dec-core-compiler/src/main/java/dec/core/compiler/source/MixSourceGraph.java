package dec.core.compiler.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 固定 mix Source discovery 的不可变 SourceManifest 与声明边集合。
 */
public final class MixSourceGraph {
    private final SourceManifest manifest;
    private final List<SourceGraphEdge> edges;

    /**
     * 冻结 SourceManifest，并按稳定边键排序声明边。
     */
    public MixSourceGraph(
            SourceManifest manifest,
            List<SourceGraphEdge> edges) {
        this.manifest = Objects.requireNonNull(manifest, "manifest");
        Objects.requireNonNull(edges, "edges");
        List<SourceGraphEdge> edgeCopy = new ArrayList<SourceGraphEdge>(
                edges.size());
        for (SourceGraphEdge edge : edges) {
            edgeCopy.add(Objects.requireNonNull(edge, "edges contains null"));
        }
        Collections.sort(edgeCopy);
        this.edges = Collections.unmodifiableList(edgeCopy);
    }

    public SourceManifest manifest() {
        return manifest;
    }

    public List<SourceGraphEdge> edges() {
        return edges;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MixSourceGraph)) {
            return false;
        }
        MixSourceGraph that = (MixSourceGraph) other;
        return manifest.equals(that.manifest) && edges.equals(that.edges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(manifest, edges);
    }

    @Override
    public String toString() {
        return "MixSourceGraph{manifest=" + manifest + ", edges=" + edges + '}';
    }
}
