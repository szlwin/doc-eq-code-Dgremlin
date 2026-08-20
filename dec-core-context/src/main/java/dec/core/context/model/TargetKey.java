package dec.core.context.model;

import java.util.Objects;

/**
 * P2 运行目标身份。目标只保存共享 ViewKey，System owner 必须由授权 Key 独立保存。
 */
public final class TargetKey implements Comparable<TargetKey> {
    private final ViewKey sourceViewKey;

    private TargetKey(ViewKey sourceViewKey) {
        this.sourceViewKey = Objects.requireNonNull(sourceViewKey, "sourceViewKey");
    }

    /** 从共享 ViewKey 创建精确、大小写敏感的目标身份。 */
    public static TargetKey of(ViewKey sourceViewKey) { return new TargetKey(sourceViewKey); }
    public ViewKey sourceViewKey() { return sourceViewKey; }

    @Override
    public int compareTo(TargetKey other) { return sourceViewKey.compareTo(Objects.requireNonNull(other, "other").sourceViewKey); }
    @Override
    public boolean equals(Object other) { return this == other || other instanceof TargetKey && sourceViewKey.equals(((TargetKey) other).sourceViewKey); }
    @Override
    public int hashCode() { return sourceViewKey.hashCode(); }
    @Override
    public String toString() { return "target:" + sourceViewKey.canonical(); }
}
