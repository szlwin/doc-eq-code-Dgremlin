package dec.core.compiler.pass;

import dec.core.compiler.api.CompilationRequest;
import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.PublicationRequest;
import dec.core.context.model.Diagnostic;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * CompilerPass 访问当前 Session 局部事实的唯一入口。
 */
public final class PassContext {
    private final CompilationSession session;

    /** 绑定当前 Session；不同 Session 不共享任何可变容器。 */
    PassContext(CompilationSession session) {
        this.session = Objects.requireNonNull(session, "session");
    }

    /** 返回当前编译请求。 */
    public CompilationRequest request() {
        return session.request();
    }

    /** 返回当前条件发布请求。 */
    public PublicationRequest publicationRequest() {
        return session.publicationRequest();
    }

    /** 返回 Pass 开始执行时的当前 Session 状态。 */
    public CompilationSessionState state() {
        return session.state();
    }

    /** 写入当前 Session 专属 artifact。 */
    public void putArtifact(String key, Object value) {
        session.putArtifact(key, value);
    }

    /** 按类型读取当前 Session 专属 artifact。 */
    public <T> Optional<T> artifact(String key, Class<T> type) {
        return session.artifact(key, type);
    }

    /** 向当前 Session 登记单个 Diagnostic。 */
    public void addDiagnostic(Diagnostic diagnostic) {
        session.addDiagnostics(Collections.singletonList(
                Objects.requireNonNull(diagnostic, "diagnostic")));
    }

    /** 向当前 Session 聚合一个 Diagnostic 批次。 */
    public void addDiagnostics(List<Diagnostic> diagnostics) {
        session.addDiagnostics(diagnostics);
    }
}
