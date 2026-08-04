package dec.core.compiler.pass;

/**
 * 标记可安全跨 Pipeline 结果边界复用的不可变领域 artifact。
 *
 * <p>实现类型必须保证实例创建后全部可观察状态不可改变。</p>
 */
public interface ImmutablePipelineArtifact {
}
