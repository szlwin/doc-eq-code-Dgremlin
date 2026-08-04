package dec.core.compiler.pass;

import dec.core.compiler.api.CompilationRequest;
import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.PublicationRequest;
import dec.core.compiler.api.PublicationResult;
import dec.core.context.EngineContext;
import dec.core.context.model.Diagnostic;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

/**
 * 仅第十阶段 Publication Pass 可获得的发布能力边界。
 */
public final class PublicationPassContext {
    private final CompilationSession session;
    private final PublicationRequest publicationRequest;
    private boolean active = true;

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

    /** 按类型读取当前 Session 的 artifact。 */
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
     * 发布候选 Context。
     *
     * <p>架构 checkpoint 暂不接入 Pipeline；有效 RED 后实现一次提交和原子终态。</p>
     */
    public PublicationResult publish(EngineContext candidate) {
        requireActive();
        Objects.requireNonNull(candidate, "candidate");
        throw new UnsupportedOperationException(
                "PublicationPassContext publish is not implemented");
    }

    /** 关闭 Context，防止 Pass 保留引用后继续使用。 */
    void close() {
        active = false;
    }

    /** 拒绝关闭后的任何访问。 */
    private void requireActive() {
        if (!active) {
            throw new IllegalStateException("publication context is closed");
        }
    }
}
