package dec.core.context;

import dec.core.context.model.CompiledModelSet;
import java.util.Objects;

/**
 * 实例级、不可变的运行时读取入口。
 */
public final class EngineContext {
    private final CompiledModelSet compiledModelSet;
    private final CoreConfigProjection projection;

    /**
     * 使用完整模型构造 Context，并由该模型确定性派生兼容 Projection。
     *
     * @param compiledModelSet 已通过发布边界验证的完整模型
     */
    public EngineContext(CompiledModelSet compiledModelSet) {
        this.compiledModelSet = Objects.requireNonNull(
                compiledModelSet,
                "compiledModelSet");
        this.projection = CoreConfigProjection.from(compiledModelSet);
    }

    /**
     * 返回 Context 持有的完整模型。
     */
    public CompiledModelSet compiledModelSet() {
        return compiledModelSet;
    }

    /**
     * 兼容旧调用方的模型读取别名。
     */
    public CompiledModelSet modelSet() {
        return compiledModelSet;
    }

    /**
     * 返回从同一个模型派生的只读 Projection。
     */
    public CoreConfigProjection projection() {
        return projection;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof EngineContext
                && compiledModelSet.equals(((EngineContext) other).compiledModelSet));
    }

    @Override
    public int hashCode() {
        return compiledModelSet.hashCode();
    }

    @Override
    public String toString() {
        return "EngineContext{" + compiledModelSet + "}";
    }
}
