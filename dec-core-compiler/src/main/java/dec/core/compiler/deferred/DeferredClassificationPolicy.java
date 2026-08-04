package dec.core.compiler.deferred;

import dec.core.context.model.DeferredKind;
import dec.core.context.model.RequiredStage;
import java.util.Objects;

/**
 * 冻结 DeferredKind 到后续阶段和稳定原因码的唯一映射。
 */
public final class DeferredClassificationPolicy {

    /** 返回指定 DeferredKind 的负责阶段。 */
    public RequiredStage requiredStage(DeferredKind kind) {
        switch (Objects.requireNonNull(kind, "kind")) {
            case SYSTEM_PERMISSION:
            case MODEL_ACCESS:
                return RequiredStage.P2;
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
        switch (Objects.requireNonNull(kind, "kind")) {
            case SYSTEM_PERMISSION:
                return "system-permission-evaluation";
            case MODEL_ACCESS:
                return "model-access-selector-binding";
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
}
