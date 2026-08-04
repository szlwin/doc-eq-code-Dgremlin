package dec.core.compiler.pass;

import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.CompilationTiming;
import dec.core.compiler.api.SessionStateTransition;
import dec.core.context.model.Diagnostic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 单次 CompilerPipeline 执行完成后的不可变值快照。
 */
public final class PipelineExecutionResult {
    private final CompilationSessionState state;
    private final List<Diagnostic> diagnostics;
    private final List<String> executedPasses;
    private final List<SessionStateTransition> transitions;
    private final List<CompilationTiming> timings;
    private final Map<String, Object> artifacts;

    /**
     * 从终态 Session 复制全部事实并立即封闭内部构建对象。
     */
    PipelineExecutionResult(CompilationSession session) {
        CompilationSession checked = Objects.requireNonNull(session, "session");
        if (!CompilationSession.isTerminal(checked.state())) {
            throw new IllegalArgumentException("session must be terminal");
        }
        checked.seal();
        this.state = checked.state();
        this.diagnostics = immutableList(checked.diagnostics());
        this.executedPasses = immutableList(checked.executedPasses());
        this.transitions = immutableList(checked.transitions());
        this.timings = immutableList(checked.timings());
        this.artifacts = state == CompilationSessionState.FAILED
                ? Collections.<String, Object>emptyMap()
                : Collections.unmodifiableMap(
                        new LinkedHashMap<String, Object>(checked.artifacts()));
    }

    /** 返回最终状态快照。 */
    public CompilationSessionState state() {
        return state;
    }

    /** 返回稳定排序且不可变的 Diagnostic 快照。 */
    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }

    /** 返回实际执行的 Pass 顺序快照。 */
    public List<String> executedPasses() {
        return executedPasses;
    }

    /** 返回实际发生的状态转换快照。 */
    public List<SessionStateTransition> transitions() {
        return transitions;
    }

    /** 返回每个已执行 Pass 的单调时钟计时快照。 */
    public List<CompilationTiming> timings() {
        return timings;
    }

    /** 返回成功路径 artifact 快照；失败结果固定为空。 */
    public Map<String, Object> artifacts() {
        return artifacts;
    }

    /** 防御性复制 List 并冻结。 */
    private static <T> List<T> immutableList(List<T> values) {
        return Collections.unmodifiableList(new ArrayList<T>(values));
    }
}
