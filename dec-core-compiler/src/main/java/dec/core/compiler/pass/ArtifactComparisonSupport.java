package dec.core.compiler.pass;

import java.util.List;
import java.util.Map;

/**
 * 冻结 artifact equality/query 的稳定门面；具体状态由单次 ComparisonOperation 持有。
 */
final class ArtifactComparisonSupport {

    /** 冻结容器通过该接口暴露已缓存的 Java-compatible hash。 */
    interface CachedHash {
        int cachedHash();
    }

    /** 工具类不允许实例化。 */
    private ArtifactComparisonSupport() {
    }

    /** 使用单次 operation 精确比较两个值。 */
    static boolean equalsValues(
            Object left,
            Object right,
            ArtifactSnapshots.ComparisonLimits limits) {
        return ArtifactComparisonOperation.equalsValues(left, right, limits);
    }

    /** 在单次 operation 内查询 List。 */
    static int indexOf(
            List<?> values,
            Object query,
            boolean reverse,
            ArtifactSnapshots.ComparisonLimits limits) {
        return ArtifactComparisonOperation.indexOf(
                values,
                query,
                reverse,
                limits);
    }

    /** 在单次 operation 内查询 Set element。 */
    static boolean containsElement(
            List<?> values,
            Object query,
            ArtifactSnapshots.ComparisonLimits limits) {
        return ArtifactComparisonOperation.containsElement(
                values,
                query,
                limits);
    }

    /** 在单次 operation 内查找 Map key。 */
    static int findEntryByKey(
            List<? extends Map.Entry<?, ?>> entries,
            Object query,
            ArtifactSnapshots.ComparisonLimits limits) {
        return ArtifactComparisonOperation.findEntryByKey(
                entries,
                query,
                limits);
    }

    /** 在单次 operation 内查询 Map value。 */
    static boolean containsValue(
            List<? extends Map.Entry<?, ?>> entries,
            Object query,
            ArtifactSnapshots.ComparisonLimits limits) {
        return ArtifactComparisonOperation.containsValue(
                entries,
                query,
                limits);
    }

    /** 在单次 operation 内查询 Entry。 */
    static boolean containsEntry(
            List<? extends Map.Entry<?, ?>> entries,
            Object query,
            ArtifactSnapshots.ComparisonLimits limits) {
        return ArtifactComparisonOperation.containsEntry(
                entries,
                query,
                limits);
    }
}
