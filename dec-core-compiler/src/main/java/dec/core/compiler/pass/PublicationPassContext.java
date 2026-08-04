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
     * <p>该方法只能调用一次。真正调用 publisher 前重新检查 token、Deadline
     * 和 ERROR，消除 PublicationPass 进入与外部提交之间的 TOCTOU 窗口。
     * 发布成功后 Context 立即关闭，之后的取消、Clock、Observer 或 Pass 异常
     * 都不能否定已经发生的外部提交。</p>
     */
    public PublicationResult publish(EngineContext candidate) {
        requireActive();
        if (publishAttempted) {
            throw new IllegalStateException("publication may be attempted only once");
        }
        publishAttempted = true;
        Objects.requireNonNull(candidate, "candidate");
        requireValidatedSession();
        requirePrecommitInfrastructure();

        PublicationResult result;
        try {
            result = publicationRequest.publisher().publish(
                    publicationRequest.expectedCurrent(),
                    candidate);
        } catch (RuntimeException failure) {
            addPublicationFailure(PipelineDiagnostics.publicationFailure());
            throw new IllegalStateException("publication failed", failure);
        }
        if (result == null) {
            addPublicationFailure(PipelineDiagnostics.publicationFailure());
            throw new IllegalStateException("publisher returned null result");
        }

        PublicationStatus status;
        try {
            // PublicationStatus 只读取一次，避免不稳定实现产生分裂判断。
            status = result.status();
        } catch (RuntimeException failure) {
            addPublicationFailure(PipelineDiagnostics.publicationFailure());
            throw new IllegalStateException("publication status failed", failure);
        }
        if (status == null) {
            addPublicationFailure(PipelineDiagnostics.publicationFailure());
            throw new IllegalStateException("publisher returned null status");
        }
        if (status == PublicationStatus.CONFLICT) {
            addPublicationFailure(PipelineDiagnostics.publicationConflict());
            return result;
        }
        if (status != PublicationStatus.PUBLISHED) {
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

    /** 确认 Session 已完成语义门禁且没有 ERROR。 */
    private void requireValidatedSession() {
        if (session.state() != CompilationSessionState.SEMANTICALLY_VALIDATED
                || session.hasErrors()) {
            addPublicationFailure(PipelineDiagnostics.publicationBlocked());
            throw new IllegalStateException(
                    "publication requires a validated error-free session");
        }
    }

    /**
     * 在外部提交的最后一刻检查取消和 Deadline，并准确分类基础设施异常。
     */
    private void requirePrecommitInfrastructure() {
        CompilationRequest request = session.request();
        boolean cancelled;
        try {
            cancelled = request.cancellationToken().isCancellationRequested();
        } catch (RuntimeException failure) {
            addPublicationFailure(PipelineDiagnostics.cancellationTokenFailure(
                    CompilerPipeline.PUBLICATION_PASS));
            throw new IllegalStateException("cancellation token failed", failure);
        }
        if (cancelled) {
            addPublicationFailure(PipelineDiagnostics.cancelled(
                    CompilerPipeline.PUBLICATION_PASS));
            throw new IllegalStateException("publication cancelled");
        }

        if (request.deadline().isPresent()) {
            long now;
            try {
                now = request.clock().nanoTime();
            } catch (RuntimeException failure) {
                addPublicationFailure(PipelineDiagnostics.clockFailure(
                        CompilerPipeline.PUBLICATION_PASS));
                throw new IllegalStateException("publication clock failed", failure);
            }
            if (request.deadline().get().isExpired(now)) {
                addPublicationFailure(PipelineDiagnostics.timedOut(
                        CompilerPipeline.PUBLICATION_PASS));
                throw new IllegalStateException("publication timed out");
            }
        }
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
