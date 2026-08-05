package dec.core.compiler.pass;

import dec.core.compiler.api.CompilationRequest;
import dec.core.compiler.api.CompilationSessionState;
import dec.core.context.EngineContext;
import dec.core.context.model.Diagnostic;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

/**
 * 第十阶段专用的候选 Context 准备边界，不持有任何外部发布能力。
 */
public final class PublicationPassContext {
    private final CompilationSession session;
    private boolean active = true;
    private boolean candidatePrepared;
    private EngineContext candidate;

    /** 绑定当前 Session，候选 Context 仅保存在本次执行内。 */
    PublicationPassContext(CompilationSession session) {
        this.session = Objects.requireNonNull(session, "session");
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

    /** 在最终提交门禁前登记 Diagnostic。 */
    public void addDiagnostic(Diagnostic diagnostic) {
        requireActive();
        session.addDiagnostics(Collections.singletonList(
                Objects.requireNonNull(diagnostic, "diagnostic")));
    }

    /**
     * 准备一个候选 Context，但不调用 publisher。
     *
     * <p>Pipeline 会在本 Pass 返回、完整聚合 Context 与 PassResult Diagnostic、
     * 完成 Clock、Deadline 和取消门禁后，才使用该候选执行唯一外部提交。</p>
     */
    public void prepare(EngineContext preparedCandidate) {
        requireActive();
        if (candidatePrepared) {
            throw new IllegalStateException(
                    "publication candidate may be prepared only once");
        }
        candidate = Objects.requireNonNull(
                preparedCandidate,
                "preparedCandidate");
        candidatePrepared = true;
    }

    /**
     * I002 包内兼容入口；只准备 candidate，不具备外部发布副作用。
     */
    void publish(EngineContext preparedCandidate) {
        prepare(preparedCandidate);
    }

    /** 返回是否已经准备候选 Context；关闭后同样拒绝读取。 */
    boolean candidatePrepared() {
        requireActive();
        return candidatePrepared;
    }

    /** 返回已准备候选；Pipeline 必须在关闭 Context 前生成局部快照。 */
    Optional<EngineContext> preparedCandidate() {
        requireActive();
        return Optional.ofNullable(candidate);
    }

    /** 关闭 Context，防止 Pass 保留引用后继续读写或读取 candidate。 */
    void close() {
        active = false;
    }

    /** 拒绝关闭后的任何公开或包内访问。 */
    private void requireActive() {
        if (!active) {
            throw new IllegalStateException("publication context is closed");
        }
    }
}
