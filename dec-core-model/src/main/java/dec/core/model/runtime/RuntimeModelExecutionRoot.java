package dec.core.model.runtime;

/** MODEL production lifecycle 的唯一 trusted loading root。 */
public interface RuntimeModelExecutionRoot extends AutoCloseable {
    RuntimeModelLoadResult load(RuntimeModelLoadRequest request);
    RuntimeModelScopeResult accessScope();
    @Override
    void close();
}
