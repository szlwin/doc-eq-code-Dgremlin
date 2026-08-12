package dec.core.context.model;

import java.util.Objects;

/** P2 唯一授权身份：owner System + target + exact path + READ/WRITE。 */
public final class ModelAccessRuleKey implements Comparable<ModelAccessRuleKey> {
    private final SystemKey owner;
    private final TargetKey target;
    private final ModelPath path;
    private final AccessOperation operation;
    private ModelAccessRuleKey(SystemKey owner, TargetKey target, ModelPath path, AccessOperation operation) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.target = Objects.requireNonNull(target, "target");
        this.path = Objects.requireNonNull(path, "path");
        this.operation = Objects.requireNonNull(operation, "operation");
    }
    /** 创建精确授权 Key；四个维度均不可省略或推断。 */
    public static ModelAccessRuleKey of(SystemKey owner, TargetKey target, ModelPath path, AccessOperation operation) { return new ModelAccessRuleKey(owner, target, path, operation); }
    public SystemKey owner() { return owner; }
    public TargetKey target() { return target; }
    public ModelPath path() { return path; }
    public AccessOperation operation() { return operation; }
    @Override
    public int compareTo(ModelAccessRuleKey other) { Objects.requireNonNull(other, "other"); int v=owner.compareTo(other.owner); if(v!=0)return v; v=target.compareTo(other.target); if(v!=0)return v; v=path.compareTo(other.path); return v!=0?v:operation.compareTo(other.operation); }
    @Override
    public boolean equals(Object other) { if(this==other)return true; if(!(other instanceof ModelAccessRuleKey))return false; ModelAccessRuleKey that=(ModelAccessRuleKey)other; return owner.equals(that.owner)&&target.equals(that.target)&&path.equals(that.path)&&operation==that.operation; }
    @Override
    public int hashCode() { return Objects.hash(owner,target,path,operation); }
    @Override
    public String toString() { return owner.canonical()+"/"+target+"/"+path+"/"+operation; }
}
