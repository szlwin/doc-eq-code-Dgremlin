package dec.core.context.model;

import java.util.Objects;

/** 编译期已经确认的单个物化字段。P2 运行时只读取这里的精确路径，不重新解析配置文本。 */
public final class CompiledMaterializationNode implements Comparable<CompiledMaterializationNode> {
    private final ModelPath path;
    private CompiledMaterializationNode(ModelPath path) { this.path=Objects.requireNonNull(path,"path"); }
    public static CompiledMaterializationNode of(ModelPath path) { return new CompiledMaterializationNode(path); }
    public ModelPath path() { return path; }
    @Override
    public int compareTo(CompiledMaterializationNode other) { return path.compareTo(Objects.requireNonNull(other,"other").path); }
    @Override
    public boolean equals(Object other) { return this==other || other instanceof CompiledMaterializationNode && path.equals(((CompiledMaterializationNode)other).path); }
    @Override
    public int hashCode() { return path.hashCode(); }
    @Override
    public String toString() { return path.toString(); }
}
