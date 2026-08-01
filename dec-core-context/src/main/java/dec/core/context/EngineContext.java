package dec.core.context;

import dec.core.context.model.CompiledModelSet;
import java.util.Objects;

public final class EngineContext {
    private final CompiledModelSet compiledModelSet;
    private final CoreConfigProjection projection;

    public EngineContext(CompiledModelSet compiledModelSet) {
        this(compiledModelSet, CoreConfigProjection.empty());
    }

    public EngineContext(CompiledModelSet compiledModelSet, CoreConfigProjection projection) {
        this.compiledModelSet = Objects.requireNonNull(compiledModelSet, "compiledModelSet");
        this.projection = Objects.requireNonNull(projection, "projection");
    }

    public CompiledModelSet compiledModelSet() { return compiledModelSet; }
    public CompiledModelSet modelSet() { return compiledModelSet; }
    public CoreConfigProjection projection() { return projection; }

    @Override public boolean equals(Object other) { return this == other || (other instanceof EngineContext && compiledModelSet.equals(((EngineContext) other).compiledModelSet) && projection.equals(((EngineContext) other).projection)); }
    @Override public int hashCode() { return Objects.hash(compiledModelSet, projection); }
    @Override public String toString() { return "EngineContext{" + compiledModelSet + "}"; }
}
