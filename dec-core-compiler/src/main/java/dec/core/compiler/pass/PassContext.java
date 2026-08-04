package dec.core.compiler.pass;

import dec.core.compiler.api.CompilationRequest;
import dec.core.compiler.api.CompilationSessionState;
import dec.core.context.model.Diagnostic;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 普通 CompilerPass 访问当前 Session 局部事实的生命周期边界。
 *
 * <p>该 Context 不持有 PublicationRequest 或 ContextPublisher。</p>
 */
public final class PassContext {
    private final CompilationSession session;
    private boolean active = true;

    /** 绑定当前 Session；每个 Pass 必须创建独立 Context。 */
    PassContext(CompilationSession session) {
        this.session = Objects.requireNonNull(session, "session");
    }

    /** 返回当前编译请求。 */
    public CompilationRequest request() {
        requireActive();
        return session.request();
    }

    /** 返回当前 Pass 执行期间的 Session 状态。 */
    public CompilationSessionState state() {
        requireActive();
        return session.state();
    }

    /** 写入当前 Session 专属、受控快照的 artifact。 */
    public void putArtifact(String key, Object value) {
        requireActive();
        session.putArtifact(key, value);
    }

    /** 按类型读取当前 Session 专属 artifact。 */
    public <T> Optional<T> artifact(String key, Class<T> type) {
        requireActive();
        return session.artifact(key, type);
    }

    /** 向当前 Session 登记单个 Diagnostic。 */
    public void addDiagnostic(Diagnostic diagnostic) {
        requireActive();
        session.addDiagnostics(Collections.singletonList(
                Objects.requireNonNull(diagnostic, "diagnostic")));
    }

    /** 向当前 Session 聚合一个 Diagnostic 批次。 */
    public void addDiagnostics(List<Diagnostic> diagnostics) {
        requireActive();
        session.addDiagnostics(diagnostics);
    }

    /** 由 Pipeline 在 Pass 返回或抛异常后立即关闭。 */
    void close() {
        active = false;
    }

    /** 拒绝 retained Context 在 Pass 生命周期外继续读写。 */
    private void requireActive() {
        if (!active) {
            throw new IllegalStateException("pass context is closed");
        }
    }
}
