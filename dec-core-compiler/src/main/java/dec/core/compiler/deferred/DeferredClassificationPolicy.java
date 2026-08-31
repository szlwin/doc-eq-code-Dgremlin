package dec.core.compiler.deferred;

import dec.core.context.model.DeferredKind;
import dec.core.context.model.RequiredStage;
import java.util.Objects;

/** 冻结仍有效的 DeferredKind 到后续阶段和稳定原因码的唯一映射。 */
public final class DeferredClassificationPolicy {

    /**
     * 判断该类型是否仍属于后续阶段语义。
     *
     * <p>AC-P2-SYSTEM-RULEVIEW-008 已退役权限运行模型，因此 System 权限和
     * model-access 只能保留为编译期兼容事实，不能再进入 Deferred Registry。
     * 需求来源：project_doc/version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/requirement.md</p>
     */
    public boolean isDeferred(DeferredKind kind) {
        DeferredKind checked = Objects.requireNonNull(kind, "kind");
        return checked != DeferredKind.SYSTEM_PERMISSION
                && checked != DeferredKind.MODEL_ACCESS;
    }

    /** 返回指定 DeferredKind 的负责阶段。 */
    public RequiredStage requiredStage(DeferredKind kind) {
        DeferredKind checked = requireActive(kind);
        switch (checked) {
            case SYSTEM_PERMISSION:
            case MODEL_ACCESS:
                throw retired(checked);
            case INFORMATION:
                return RequiredStage.P3;
            case ACTION:
            case PRODUCE:
                return RequiredStage.P4;
            case DIRECTORY:
                return RequiredStage.P5;
            case QUERY:
                return RequiredStage.P6;
            case TRANSACTION:
                return RequiredStage.P7;
            default:
                throw new IllegalStateException("unexpected DeferredKind: " + kind);
        }
    }

    /** 返回指定 DeferredKind 的稳定原因码。 */
    public String reasonCode(DeferredKind kind) {
        DeferredKind checked = requireActive(kind);
        switch (checked) {
            case SYSTEM_PERMISSION:
            case MODEL_ACCESS:
                throw retired(checked);
            case INFORMATION:
                return "information-expression-evaluation";
            case ACTION:
                return "action-execution";
            case PRODUCE:
                return "produce-execution";
            case DIRECTORY:
                return "directory-evaluation";
            case QUERY:
                return "query-planning";
            case TRANSACTION:
                return "transaction-execution";
            default:
                throw new IllegalStateException("unexpected DeferredKind: " + kind);
        }
    }

    private DeferredKind requireActive(DeferredKind kind) {
        DeferredKind checked = Objects.requireNonNull(kind, "kind");
        if (!isDeferred(checked)) {
            throw retired(checked);
        }
        return checked;
    }

    private static IllegalArgumentException retired(DeferredKind kind) {
        return new IllegalArgumentException(
                "retired DeferredKind cannot be classified: " + kind);
    }
}
