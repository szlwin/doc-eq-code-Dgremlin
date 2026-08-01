package dec.core.context.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * SourceGraph 对外发布时使用的 Context 中立只读视图。
 */
public final class PublishedSourceManifest {
    private static final String EMPTY_ROOT_SOURCE_ID = "synthetic:empty-root";

    private final String rootSourceId;
    private final List<PublishedSourceDescriptor> sources;
    private final List<PublishedSourceDependency> dependencies;

    /**
     * 构造稳定排序、不可修改且引用闭合的发布清单。
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
        validateRootAndUniqueSources(this.rootSourceId, this.sources);
        this.dependencies = immutableSortedDependencies(dependencies);
        validateDependencyClosure(this.sources, this.dependencies);
    }

    /**
     * 为没有业务定义的模型提供仍然自洽的 synthetic 发布清单。
     */
    public static PublishedSourceManifest empty() {
        return new PublishedSourceManifest(
                EMPTY_ROOT_SOURCE_ID,
                Collections.singletonList(
                        new PublishedSourceDescriptor(
                                EMPTY_ROOT_SOURCE_ID,
                                "SYNTHETIC",
                                "empty")),
                Collections.<PublishedSourceDependency>emptyList());
    }

    /** 返回根源标识。 */
    public String rootSourceId() {
        return rootSourceId;
    }

    /** 返回按 sourceId 稳定排序的源描述。 */
    public List<PublishedSourceDescriptor> sources() {
        return sources;
    }

    /** 返回按依赖语义稳定排序的边列表。 */
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

    private static void validateRootAndUniqueSources(
            String rootSourceId,
            List<PublishedSourceDescriptor> sources) {
        Set<String> sourceIds = new HashSet<String>();
        for (PublishedSourceDescriptor source : sources) {
            // sourceId 是发布图的节点身份，重复节点会使摘要和边解析产生歧义。
            if (!sourceIds.add(source.sourceId())) {
                throw new IllegalArgumentException(
                        "Duplicate published sourceId: " + source.sourceId());
            }
        }
        if (!sourceIds.contains(rootSourceId)) {
            throw new IllegalArgumentException(
                    "Published root source is absent from sources: " + rootSourceId);
        }
    }

    private static void validateDependencyClosure(
            List<PublishedSourceDescriptor> sources,
            List<PublishedSourceDependency> dependencies) {
        Set<String> sourceIds = new HashSet<String>();
        for (PublishedSourceDescriptor source : sources) {
            sourceIds.add(source.sourceId());
        }
        for (PublishedSourceDependency dependency : dependencies) {
            // 发布视图只能包含图内边，禁止将未发现或越界源带入运行上下文。
            if (!sourceIds.contains(dependency.fromSourceId())) {
                throw new IllegalArgumentException(
                        "Dependency source is absent from manifest: "
                                + dependency.fromSourceId());
            }
            if (!sourceIds.contains(dependency.targetSourceId())) {
                throw new IllegalArgumentException(
                        "Dependency target is absent from manifest: "
                                + dependency.targetSourceId());
            }
            // Manifest 再次执行防御性校验，避免未来替代构造路径带入矛盾来源。
            if (!dependency.fromSourceId().equals(
                    dependency.declarationSourceRef().sourceId())) {
                throw new IllegalArgumentException(
                        "Dependency declaration source must equal fromSourceId: "
                                + dependency.fromSourceId()
                                + " != "
                                + dependency.declarationSourceRef().sourceId());
            }
        }
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
