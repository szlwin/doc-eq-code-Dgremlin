package dec.core.context.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * SourceGraph 对外发布时使用的 Context 中立只读视图。
 */
public final class PublishedSourceManifest {
    private static final String EMPTY_ROOT_SOURCE_ID = "synthetic:empty-root";

    private final String rootSourceId;
    private final List<PublishedSourceDescriptor> sources;
    private final List<PublishedSourceDependency> dependencies;

    /**
     * 构造稳定排序、不可修改的发布清单。
     *
     * @param rootSourceId 根源标识
     * @param sources 已发现源的中立描述
     * @param dependencies 源之间的依赖边
     */
    public PublishedSourceManifest(
            String rootSourceId,
            List<PublishedSourceDescriptor> sources,
            List<PublishedSourceDependency> dependencies) {
        this.rootSourceId = AbstractDefinitionKey.requireText(rootSourceId, "rootSourceId");
        this.sources = immutableSortedSources(sources);
        this.dependencies = immutableSortedDependencies(dependencies);
    }

    /**
     * 为无定义的测试模型提供稳定的空发布清单。
     */
    public static PublishedSourceManifest empty() {
        return new PublishedSourceManifest(
                EMPTY_ROOT_SOURCE_ID,
                Collections.<PublishedSourceDescriptor>emptyList(),
                Collections.<PublishedSourceDependency>emptyList());
    }

    /**
     * 返回根源标识。
     */
    public String rootSourceId() {
        return rootSourceId;
    }

    /**
     * 返回按 sourceId 稳定排序的源描述。
     */
    public List<PublishedSourceDescriptor> sources() {
        return sources;
    }

    /**
     * 返回按依赖语义稳定排序的边列表。
     */
    public List<PublishedSourceDependency> dependencies() {
        return dependencies;
    }

    private static List<PublishedSourceDescriptor> immutableSortedSources(
            List<PublishedSourceDescriptor> values) {
        Objects.requireNonNull(values, "sources");
        List<PublishedSourceDescriptor> copy =
                new ArrayList<PublishedSourceDescriptor>(values.size());
        for (PublishedSourceDescriptor value : values) {
            copy.add(Objects.requireNonNull(value, "sources contains null"));
        }
        Collections.sort(copy);
        return Collections.unmodifiableList(copy);
    }

    private static List<PublishedSourceDependency> immutableSortedDependencies(
            List<PublishedSourceDependency> values) {
        Objects.requireNonNull(values, "dependencies");
        List<PublishedSourceDependency> copy =
                new ArrayList<PublishedSourceDependency>(values.size());
        for (PublishedSourceDependency value : values) {
            copy.add(Objects.requireNonNull(value, "dependencies contains null"));
        }
        Collections.sort(copy);
        return Collections.unmodifiableList(copy);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PublishedSourceManifest)) {
            return false;
        }
        PublishedSourceManifest that = (PublishedSourceManifest) other;
        return rootSourceId.equals(that.rootSourceId)
                && sources.equals(that.sources)
                && dependencies.equals(that.dependencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootSourceId, sources, dependencies);
    }

    @Override
    public String toString() {
        return "PublishedSourceManifest{"
                + "rootSourceId='" + rootSourceId + '\''
                + ", sources=" + sources.size()
                + ", dependencies=" + dependencies.size()
                + '}';
    }
}
