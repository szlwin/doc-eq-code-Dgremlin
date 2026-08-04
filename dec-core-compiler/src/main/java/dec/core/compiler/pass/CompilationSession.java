package dec.core.compiler.pass;

import dec.core.compiler.api.CompilationRequest;
import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.CompilationTiming;
import dec.core.compiler.api.SessionStateTransition;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticSeverity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 单次 Pipeline 调用专属的语义构建状态；终态后语义事实不可修改。
 */
public final class CompilationSession {
    private final CompilationRequest request;
    private CompilationSessionState state = CompilationSessionState.CREATED;
    private final List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
    private final List<String> executedPasses = new ArrayList<String>();
    private final List<SessionStateTransition> transitions =
            new ArrayList<SessionStateTransition>();
    private final List<CompilationTiming> timings =
            new ArrayList<CompilationTiming>();
    private final Map<String, Object> artifacts =
            new LinkedHashMap<String, Object>();
    private boolean sealed;

    /** 创建一个完全隔离、初始状态为 CREATED 的 Session。 */
    CompilationSession(CompilationRequest request) {
        this.request = Objects.requireNonNull(request, "request");
    }

    /** 返回当前 Session 的不可变编译请求。 */
    public CompilationRequest request() {
        return request;
    }

    /** 返回当前状态。 */
    public CompilationSessionState state() {
        return state;
    }

    /** 返回稳定排序且不可变的 Diagnostic 快照。 */
    public List<Diagnostic> diagnostics() {
        List<Diagnostic> copy = new ArrayList<Diagnostic>(diagnostics);
        Collections.sort(copy);
        return Collections.unmodifiableList(copy);
    }

    /** 返回已执行 Pass 名称的不可变顺序快照。 */
    public List<String> executedPasses() {
        return Collections.unmodifiableList(
                new ArrayList<String>(executedPasses));
    }

    /** 返回实际状态转换的不可变顺序快照。 */
    public List<SessionStateTransition> transitions() {
        return Collections.unmodifiableList(
                new ArrayList<SessionStateTransition>(transitions));
    }

    /** 返回每个 Pass timing 的不可变顺序快照。 */
    public List<CompilationTiming> timings() {
        return Collections.unmodifiableList(
                new ArrayList<CompilationTiming>(timings));
    }

    /** 返回 Session-local artifact 的不可变快照。 */
    public Map<String, Object> artifacts() {
        return Collections.unmodifiableMap(
                new LinkedHashMap<String, Object>(artifacts));
    }

    /** 按类型读取 Session-local artifact。 */
    public <T> Optional<T> artifact(String key, Class<T> type) {
        Objects.requireNonNull(type, "type");
        Object value = artifacts.get(requireKey(key));
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(type.cast(value));
    }

    /**
     * 按冻结状态机推进状态，并返回可直接交给 Observer 的不可变转换事实。
     */
    SessionStateTransition transitionTo(CompilationSessionState next) {
        requireSemanticMutationAllowed();
        Objects.requireNonNull(next, "next");
        if (next != CompilationSessionState.FAILED
                && next != expectedNext(state)) {
            throw new IllegalStateException(
                    "illegal session transition: " + state + " -> " + next);
        }
        SessionStateTransition transition = new SessionStateTransition(state, next);
        transitions.add(transition);
        state = next;
        return transition;
    }

    /** 仅允许 Pipeline 在真实调用 Pass 前登记执行事实。 */
    void recordPass(String passName) {
        requireSemanticMutationAllowed();
        executedPasses.add(requireKey(passName));
    }

    /**
     * 记录 Pipeline 独占的观察 timing。
     *
     * <p>Publication commit 后可在 Result 封闭前尽力补充 timing；Context 无此能力。</p>
     */
    void recordTiming(CompilationTiming timing) {
        requireNotSealed();
        timings.add(Objects.requireNonNull(timing, "timing"));
    }

    /** 仅允许 ACTIVE Context 向非终态 Session 聚合 Diagnostic。 */
    void addDiagnostics(List<Diagnostic> values) {
        requireSemanticMutationAllowed();
        List<Diagnostic> copy = new ArrayList<Diagnostic>(
                Objects.requireNonNull(values, "values"));
        if (copy.contains(null)) {
            throw new NullPointerException("diagnostics contains null");
        }
        diagnostics.addAll(copy);
    }

    /** 判断当前 Session 是否已经包含阻断 ERROR。 */
    boolean hasErrors() {
        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.severity() == DiagnosticSeverity.ERROR) {
                return true;
            }
        }
        return false;
    }

    /** 仅允许 ACTIVE Context 写入受控不可变 artifact。 */
    void putArtifact(String key, Object value) {
        requireSemanticMutationAllowed();
        artifacts.put(requireKey(key), ArtifactSnapshots.freeze(value));
    }

    /**
     * 在构造 Result 前最终封闭 Session，后续任何内部写入均拒绝。
     */
    void seal() {
        if (!isTerminal(state)) {
            throw new IllegalStateException("only terminal session can be sealed");
        }
        sealed = true;
    }

    /** 判断状态是否为不可继续推进的终态。 */
    static boolean isTerminal(CompilationSessionState value) {
        return value == CompilationSessionState.PUBLISHED
                || value == CompilationSessionState.FAILED;
    }

    /** 拒绝终态或已封闭 Session 的语义写入。 */
    private void requireSemanticMutationAllowed() {
        requireNotSealed();
        if (isTerminal(state)) {
            throw new IllegalStateException("terminal session is immutable");
        }
    }

    /** 拒绝 Result 已冻结后的任何内部写入。 */
    private void requireNotSealed() {
        if (sealed) {
            throw new IllegalStateException("session is sealed");
        }
    }

    /** 返回冻结成功路径中的唯一下一状态。 */
    private static CompilationSessionState expectedNext(
            CompilationSessionState value) {
        switch (value) {
            case CREATED:
                return CompilationSessionState.SOURCES_DISCOVERED;
            case SOURCES_DISCOVERED:
                return CompilationSessionState.PARSED;
            case PARSED:
                return CompilationSessionState.RAW_BUILT;
            case RAW_BUILT:
                return CompilationSessionState.STRUCTURALLY_VALIDATED;
            case STRUCTURALLY_VALIDATED:
                return CompilationSessionState.SYMBOLS_REGISTERED;
            case SYMBOLS_REGISTERED:
                return CompilationSessionState.REFERENCES_RESOLVED;
            case REFERENCES_RESOLVED:
                return CompilationSessionState.GRAPH_PREPARED;
            case GRAPH_PREPARED:
                return CompilationSessionState.SEMANTICALLY_VALIDATED;
            case SEMANTICALLY_VALIDATED:
                return CompilationSessionState.PUBLISHED;
            default:
                throw new IllegalStateException("terminal state has no next state");
        }
    }

    /** 校验 artifact key 与 Pass 名称均为非空白稳定文本。 */
    private static String requireKey(String value) {
        String checked = Objects.requireNonNull(value, "key").trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        return checked;
    }
}
