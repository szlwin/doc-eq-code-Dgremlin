package dec.core.compiler.pass;

import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.CompilationTiming;
import dec.core.compiler.api.SessionStateTransition;
import dec.core.context.model.Diagnostic;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 单次 CompilerPipeline 执行完成后的只读 Session 事实。
 */
public final class PipelineExecutionResult {
    private final CompilationSession session;

    /** 冻结已经进入终态的 Session 引用。 */
    PipelineExecutionResult(CompilationSession session) {
        this.session = Objects.requireNonNull(session, "session");
        if (session.state() != CompilationSessionState.PUBLISHED
                && session.state() != CompilationSessionState.FAILED) {
            throw new IllegalArgumentException("session must be terminal");
        }
    }

    /** 返回本次执行的独立 Session，公开方法均只读。 */
    public CompilationSession session() {
        return session;
    }

    /** 返回最终状态。 */
    public CompilationSessionState state() {
        return session.state();
    }

    /** 返回稳定排序且不可变的 Diagnostic。 */
    public List<Diagnostic> diagnostics() {
        return session.diagnostics();
    }

    /** 返回实际执行的 Pass 顺序。 */
    public List<String> executedPasses() {
        return session.executedPasses();
    }

    /** 返回实际发生的状态转换。 */
    public List<SessionStateTransition> transitions() {
        return session.transitions();
    }

    /** 返回每个已执行 Pass 的单调时钟计时。 */
    public List<CompilationTiming> timings() {
        return session.timings();
    }

    /** 返回成功路径形成的 Session-local artifact 只读快照。 */
    public Map<String, Object> artifacts() {
        return session.artifacts();
    }
}
