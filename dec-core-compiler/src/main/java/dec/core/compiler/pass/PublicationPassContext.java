package dec.core.compiler.pass;

import dec.core.compiler.api.CompilationRequest;
import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.PublicationRequest;
import dec.core.compiler.api.PublicationResult;
import dec.core.compiler.api.PublicationStatus;
import dec.core.compiler.api.SessionStateTransition;
import dec.core.context.EngineContext;
import dec.core.context.model.Diagnostic;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

/**
 * 仅第十阶段 Publication Pass 可获得的一次性发布能力边界。
 */
public final class PublicationPassContext {
    private final CompilationSession session;
    private final PublicationRequest publicationRequest;
    private boolean active = true;
    private boolean publishAttempted;
    private boolean publicationSucceeded;

    /** 绑定当前 Session 与仅在最终阶段可见的 PublicationRequest。 */
    PublicationPassContext(
            CompilationSession session,
            PublicationRequest publicationRequest) {
        this.session = Objects.requireNonNull(session, "session");
        this.publicationRequest = Objects.requireNonNull(
                publicationRequest,
                "publicationRequest");
    }

    /** 返回当前编译请求。 */
    public CompilationRequest request() {
        requireActive();
        return session.request();
    }

    /** 返回当前 Session 状态。 */
    public CompilationSessionState state() {
        requireActive();
        return session.state();
    }

    /** 按类型读取当前 Session 的不可变 artifact。 */
    public <T> Optional<T> artifact(String key, Class<T> type) {
        requireActive();
        return session.artifact(key, type);
    }

    /** 在最终发布前登记 Diagnostic。 */
    public void addDiagnostic(Diagnostic diagnostic) {
        requireActive();
        session.addDiagnostics(Collections.singletonList(
                Objects.requireNonNull(diagnostic, "diagnostic")));
    }

    /**
     * 原子发布候选 Context，并在 PUBLISHED 结果返回后立即锁定终态。
     *
     * <p>该方法只能调用一次。发布成功后 Context 立即关闭，之后的取消、
     * Clock、Observer 或 Pass 异常都不能否定已经发生的外部提交。</p>
     */
    public PublicationResult publish(EngineContext candidate) {
        requireActive();
        if (publishAttempted) {
            throw new IllegalStateException("publication may be attempted only once");
        }
        publishAttempted = true;
        Objects.requireNonNull(candidate, "candidate");
        if (session.state() != CompilationSessionState.SEMANTICALLY_VALIDATED
                || session.hasErrors()) {
            addPublicationFailure(PipelineDiagnostics.publicationBlocked());
            throw new IllegalStateException(
                    "publication requires a validated error-free session");
        }

        PublicationResult result;
        try {
            result = publicationRequest.publisher().publish(
                    publicationRequest.expectedCurrent(),
                    candidate);
        } catch (RuntimeException failure) {
            addPublicationFailure(PipelineDiagnostics.publicationFailure());
            throw new IllegalStateException("publication failed", failure);
        }
        if (result == null || result.status() == null) {
            addPublicationFailure(PipelineDiagnostics.publicationFailure());
            throw new IllegalStateException("publisher returned an invalid result");
        }
        if (result.status() == PublicationStatus.CONFLICT) {
            addPublicationFailure(PipelineDiagnostics.publicationConflict());
            return result;
        }
        if (result.status() != PublicationStatus.PUBLISHED) {
            addPublicationFailure(PipelineDiagnostics.publicationFailure());
            throw new IllegalStateException("unsupported publication status");
        }

        SessionStateTransition transition = session.transitionTo(
                CompilationSessionState.PUBLISHED);
        publicationSucceeded = true;
        active = false;
        notifyTransition(transition);
        return result;
    }

    /** 返回 publisher 是否已经成功提交并锁定 PUBLISHED。 */
    boolean publicationSucceeded() {
        return publicationSucceeded;
    }

    /** 返回本 Context 是否已尝试调用 publisher。 */
    boolean publishAttempted() {
        return publishAttempted;
    }

    /** 关闭 Context，防止 Pass 保留引用后继续使用。 */
    void close() {
        active = false;
    }

    /** 在发布提交前登记稳定失败 Diagnostic。 */
    private void addPublicationFailure(Diagnostic diagnostic) {
        session.addDiagnostics(Collections.singletonList(diagnostic));
    }

    /** Observer 只能旁观已完成的原子状态转换。 */
    private void notifyTransition(SessionStateTransition transition) {
        try {
            session.request().observer().onStateTransition(transition);
        } catch (RuntimeException ignored) {
            // 已提交状态不可被 Observer 故障回滚；完整诊断策略留给 T13。
        }
    }

    /** 拒绝关闭后的任何访问。 */
    private void requireActive() {
        if (!active) {
            throw new IllegalStateException("publication context is closed");
        }
    }
}
