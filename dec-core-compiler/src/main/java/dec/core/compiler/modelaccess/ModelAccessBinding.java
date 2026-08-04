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

    public SystemKey ownerSystem() {
        return ownerSystem;
    }

    public ViewKey sourceModel() {
        return sourceModel;
    }

    public SharedModelPath sourcePath() {
        return sourcePath;
    }

    public AccessMode accessMode() {
        return accessMode;
    }

    public ViewKey targetView() {
        return targetView;
    }

    public SystemViewSelector selector() {
        return selector;
    }

    public TargetPropertyPath resolvedTarget() {
        return resolvedTarget;
    }

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
        return comparison == 0
                ? resolvedTarget.compareTo(other.resolvedTarget)
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
                + "->" + targetView + "#" + resolvedTarget;
    }
}
