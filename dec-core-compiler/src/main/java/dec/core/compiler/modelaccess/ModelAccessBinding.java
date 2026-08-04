package dec.core.compiler.modelaccess;

import dec.core.context.model.SourceRef;
import dec.core.context.model.SystemKey;
import dec.core.context.model.ViewKey;
import java.util.Objects;

/**
 * 共享模型源路径与当前 System 本地 View 目标之间的显式绑定。
 */
public final class ModelAccessBinding implements Comparable<ModelAccessBinding> {
    private final SystemKey ownerSystem;
    private final ViewKey sourceModel;
    private final SharedModelPath sourcePath;
    private final AccessMode accessMode;
    private final ViewKey targetView;
    private final SystemViewSelector selector;
    private final TargetPropertyPath resolvedTarget;
    private final SourceRef sourceRef;

    /** 冻结字段完整的 ModelAccess 结构事实。 */
    public ModelAccessBinding(
            SystemKey ownerSystem,
            ViewKey sourceModel,
            SharedModelPath sourcePath,
            AccessMode accessMode,
            ViewKey targetView,
            SystemViewSelector selector,
            TargetPropertyPath resolvedTarget,
            SourceRef sourceRef) {
        this.ownerSystem = Objects.requireNonNull(ownerSystem, "ownerSystem");
        this.sourceModel = Objects.requireNonNull(sourceModel, "sourceModel");
        this.sourcePath = Objects.requireNonNull(sourcePath, "sourcePath");
        this.accessMode = Objects.requireNonNull(accessMode, "accessMode");
        this.targetView = Objects.requireNonNull(targetView, "targetView");
        this.selector = Objects.requireNonNull(selector, "selector");
        this.resolvedTarget = Objects.requireNonNull(resolvedTarget, "resolvedTarget");
        this.sourceRef = Objects.requireNonNull(sourceRef, "sourceRef");
    }

    /** 返回拥有该 ModelAccess 的当前 System。 */
    public SystemKey ownerSystem() {
        return ownerSystem;
    }

    /** 返回共享模型来源 View。 */
    public ViewKey sourceModel() {
        return sourceModel;
    }

    /** 返回共享模型中的精确源路径。 */
    public SharedModelPath sourcePath() {
        return sourcePath;
    }

    /** 返回 READ 或 WRITE 结构事实。 */
    public AccessMode accessMode() {
        return accessMode;
    }

    /** 返回当前 System 已声明的目标 View。 */
    public ViewKey targetView() {
        return targetView;
    }

    /** 返回未经模糊修复的精确 selector。 */
    public SystemViewSelector selector() {
        return selector;
    }

    /** 返回 target-main 或 property path 唯一解析结果。 */
    public TargetPropertyPath resolvedTarget() {
        return resolvedTarget;
    }

    /** 返回声明该 Binding 的来源位置。 */
    public SourceRef sourceRef() {
        return sourceRef;
    }

    @Override
    public int compareTo(ModelAccessBinding other) {
        Objects.requireNonNull(other, "other");
        int comparison = ownerSystem.compareTo(other.ownerSystem);
        if (comparison != 0) {
            return comparison;
        }
        comparison = sourceModel.compareTo(other.sourceModel);
        if (comparison != 0) {
            return comparison;
        }
        comparison = accessMode.compareTo(other.accessMode);
        if (comparison != 0) {
            return comparison;
        }
        comparison = sourcePath.compareTo(other.sourcePath);
        if (comparison != 0) {
            return comparison;
        }
        comparison = targetView.compareTo(other.targetView);
        if (comparison != 0) {
            return comparison;
        }
        comparison = selector.compareTo(other.selector);
        if (comparison != 0) {
            return comparison;
        }
        comparison = resolvedTarget.compareTo(other.resolvedTarget);
        return comparison == 0
                ? sourceRef.compareTo(other.sourceRef)
                : comparison;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ModelAccessBinding)) {
            return false;
        }
        ModelAccessBinding that = (ModelAccessBinding) other;
        return ownerSystem.equals(that.ownerSystem)
                && sourceModel.equals(that.sourceModel)
                && sourcePath.equals(that.sourcePath)
                && accessMode == that.accessMode
                && targetView.equals(that.targetView)
                && selector.equals(that.selector)
                && resolvedTarget.equals(that.resolvedTarget)
                && sourceRef.equals(that.sourceRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ownerSystem, sourceModel, sourcePath, accessMode,
                targetView, selector, resolvedTarget, sourceRef);
    }

    @Override
    public String toString() {
        return ownerSystem + ":" + accessMode + ":" + sourcePath
                + "->" + targetView + "#" + resolvedTarget
                + "@" + sourceRef;
    }
}
