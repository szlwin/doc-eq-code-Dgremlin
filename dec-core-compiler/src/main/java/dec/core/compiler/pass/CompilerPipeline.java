package dec.core.compiler.pass;

import dec.core.compiler.api.CompilationObserver;
import dec.core.compiler.api.CompilationRequest;
import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.CompilationTiming;
import dec.core.compiler.api.PublicationRequest;
import dec.core.compiler.api.SessionStateTransition;
import dec.core.compiler.api.TimingPhase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 按 DESIGN-R38 固定顺序协调十个 CompilerPass。
 */
public final class CompilerPipeline {
    public static final String SOURCE_GRAPH_VALIDATION_PASS =
            "SourceGraphValidationPass";
    public static final String STRUCTURAL_VALIDATION_PASS =
            "StructuralValidationPass";
    public static final String SYMBOL_REGISTRATION_PASS =
            "SymbolRegistrationPass";
    public static final String REFERENCE_RESOLUTION_PASS =
            "ReferenceResolutionPass";
    public static final String INFORMATION_OWNERSHIP_PASS =
            "InformationOwnershipPass";
    public static final String MODEL_ACCESS_BINDING_PASS =
            "ModelAccessBindingPass";
    public static final String DEFERRED_CLASSIFICATION_PASS =
            "DeferredClassificationPass";
    public static final String P1_SEMANTIC_VALIDATION_PASS =
            "P1SemanticValidationPass";
    public static final String DIGEST_PASS = "DigestPass";
    public static final String PUBLICATION_PASS = "PublicationPass";

    private static final List<String> FIXED_PASS_ORDER =
            Collections.unmodifiableList(Arrays.asList(
                    SOURCE_GRAPH_VALIDATION_PASS,
                    STRUCTURAL_VALIDATION_PASS,
                    SYMBOL_REGISTRATION_PASS,
                    REFERENCE_RESOLUTION_PASS,
                    INFORMATION_OWNERSHIP_PASS,
                    MODEL_ACCESS_BINDING_PASS,
                    DEFERRED_CLASSIFICATION_PASS,
                    P1_SEMANTIC_VALIDATION_PASS,
                    DIGEST_PASS,
                    PUBLICATION_PASS));

    private final List<CompilerPass> passes;

    /**
     * 防御性复制并验证十个 Pass 的数量、名称和顺序。
     */
    public CompilerPipeline(List<CompilerPass> passes) {
        List<CompilerPass> copy = new ArrayList<CompilerPass>(
                Objects.requireNonNull(passes, "passes"));
        validateFixedOrder(copy);
        this.passes = Collections.unmodifiableList(copy);
    }

    /** 返回 DESIGN-R38 冻结的 Pass 名称顺序。 */
    public static List<String> fixedPassOrder() {
        return FIXED_PASS_ORDER;
    }

    /** 返回防御性复制后的只读 Pass 列表。 */
    public List<CompilerPass> passes() {
        return passes;
    }

    /**
     * 创建新 Session，并按固定顺序执行全部 Pass。
     *
     * <p>该方法保持包内可见，避免形成绕过 {@code ModelCompiler.compileAndPublish}
     * 的公共 compile-only 成功入口。</p>
     */
    PipelineExecutionResult execute(
            CompilationRequest request,
            PublicationRequest publicationRequest) {
        CompilationRequest checkedRequest = Objects.requireNonNull(
                request,
                "request");
        CompilationSession session = new CompilationSession(
                checkedRequest,
                Objects.requireNonNull(publicationRequest, "publicationRequest"));
        PassContext context = new PassContext(session);

        for (int index = 0; index < passes.size(); index++) {
            CompilerPass pass = passes.get(index);
            String passName = FIXED_PASS_ORDER.get(index);
            long startedNanos = 0L;
            boolean timingStarted = false;
            try {
                // Pass 开始前先处理取消和 Deadline，确保零副作用停止。
                if (stopRequested(session, passName)) {
                    return new PipelineExecutionResult(session);
                }

                session.recordPass(passName);
                startedNanos = checkedRequest.clock().nanoTime();
                timingStarted = true;
                PassResult passResult = Objects.requireNonNull(
                        pass.execute(context),
                        "pass result");
                recordTiming(session, passName, startedNanos);
                timingStarted = false;

                session.addDiagnostics(passResult.diagnostics());
                if (session.hasErrors()) {
                    fail(session);
                    return new PipelineExecutionResult(session);
                }

                // Pass 执行后再次检查，阻止状态推进和后续 Publication。
                if (stopRequested(session, passName)) {
                    return new PipelineExecutionResult(session);
                }

                advanceState(session, index);
            } catch (RuntimeException failure) {
                if (timingStarted) {
                    recordTimingSafely(session, passName, startedNanos);
                }
                session.addDiagnostics(Collections.singletonList(
                        PUBLICATION_PASS.equals(passName)
                                ? PipelineDiagnostics.publicationFailure()
                                : PipelineDiagnostics.passFailure(passName)));
                fail(session);
                return new PipelineExecutionResult(session);
            }
        }

        if (session.state() != CompilationSessionState.PUBLISHED) {
            session.addDiagnostics(Collections.singletonList(
                    PipelineDiagnostics.passFailure("CompilerPipeline")));
            fail(session);
        }
        return new PipelineExecutionResult(session);
    }

    /** 在执行任何 Pass 前验证完整固定顺序和逐字符精确名称。 */
    private static void validateFixedOrder(List<CompilerPass> values) {
        if (values.size() != FIXED_PASS_ORDER.size()) {
            throw new IllegalArgumentException("pipeline requires exactly ten passes");
        }
        for (int index = 0; index < values.size(); index++) {
            CompilerPass pass = Objects.requireNonNull(
                    values.get(index),
                    "passes contains null");
            String actual = Objects.requireNonNull(pass.name(), "pass name");
            if (actual.trim().isEmpty()) {
                throw new IllegalArgumentException("pass name must not be blank");
            }
            if (!FIXED_PASS_ORDER.get(index).equals(actual)) {
                throw new IllegalArgumentException(
                        "unexpected pass at index " + index + ": " + actual);
            }
        }
    }

    /** 检查取消和 Deadline，并在命中时稳定进入 FAILED。 */
    private static boolean stopRequested(
            CompilationSession session,
            String passName) {
        CompilationRequest request = session.request();
        if (request.cancellationToken().isCancellationRequested()) {
            session.addDiagnostics(Collections.singletonList(
                    PipelineDiagnostics.cancelled(passName)));
            fail(session);
            return true;
        }
        if (request.deadline().isPresent()
                && request.deadline().get().isExpired(
                        request.clock().nanoTime())) {
            session.addDiagnostics(Collections.singletonList(
                    PipelineDiagnostics.timedOut(passName)));
            fail(session);
            return true;
        }
        return false;
    }

    /** 按 Pass 索引推进 DESIGN-R38 冻结的唯一成功状态路径。 */
    private static void advanceState(CompilationSession session, int index) {
        switch (index) {
            case 0:
                transition(session, CompilationSessionState.SOURCES_DISCOVERED);
                break;
            case 1:
                transition(session, CompilationSessionState.PARSED);
                transition(session, CompilationSessionState.RAW_BUILT);
                transition(session,
                        CompilationSessionState.STRUCTURALLY_VALIDATED);
                break;
            case 2:
                transition(session, CompilationSessionState.SYMBOLS_REGISTERED);
                break;
            case 3:
                transition(session, CompilationSessionState.REFERENCES_RESOLVED);
                break;
            case 4:
            case 5:
                // 两个语义绑定 Pass 共享 REFERENCES_RESOLVED 状态，不制造平行状态。
                break;
            case 6:
                transition(session, CompilationSessionState.GRAPH_PREPARED);
                break;
            case 7:
                transition(session,
                        CompilationSessionState.SEMANTICALLY_VALIDATED);
                break;
            case 8:
                // Digest 属于已验证候选事实，不新增 DESIGN-R05 之外的状态。
                break;
            case 9:
                transition(session, CompilationSessionState.PUBLISHED);
                break;
            default:
                throw new IllegalStateException("unexpected pass index: " + index);
        }
    }

    /** 推进 Session 状态，并让 Observer 只读观察实际转换。 */
    private static void transition(
            CompilationSession session,
            CompilationSessionState next) {
        SessionStateTransition transition = session.transitionTo(next);
        notifyTransition(session.request().observer(), transition);
    }

    /** 将当前非终态 Session 统一推进到 FAILED。 */
    private static void fail(CompilationSession session) {
        if (session.state() != CompilationSessionState.FAILED
                && session.state() != CompilationSessionState.PUBLISHED) {
            transition(session, CompilationSessionState.FAILED);
        }
    }

    /** 记录非负 Pass 耗时，并交给 Observer 只读观察。 */
    private static void recordTiming(
            CompilationSession session,
            String passName,
            long startedNanos) {
        long endedNanos = session.request().clock().nanoTime();
        long elapsedNanos = endedNanos >= startedNanos
                ? endedNanos - startedNanos
                : 0L;
        CompilationTiming timing = new CompilationTiming(
                TimingPhase.PASS,
                Optional.of(passName),
                elapsedNanos);
        session.recordTiming(timing);
        notifyTiming(session.request().observer(), timing);
    }

    /** 异常路径尽力记录 timing；Clock 再次失败时不覆盖原始业务失败。 */
    private static void recordTimingSafely(
            CompilationSession session,
            String passName,
            long startedNanos) {
        try {
            recordTiming(session, passName, startedNanos);
        } catch (RuntimeException ignored) {
            // T12 保持原始 Pass 失败为主事实；T13 再补 Observer/Clock 诊断策略。
        }
    }

    /** Observer timing 回调失败不得改变 Session 状态或结果。 */
    private static void notifyTiming(
            CompilationObserver observer,
            CompilationTiming timing) {
        try {
            observer.onTiming(timing);
        } catch (RuntimeException ignored) {
            // T13 将把观察失败转换为非 ERROR；T12 先保证不改变编译事实。
        }
    }

    /** Observer 状态回调失败不得改变 Session 状态或结果。 */
    private static void notifyTransition(
            CompilationObserver observer,
            SessionStateTransition transition) {
        try {
            observer.onStateTransition(transition);
        } catch (RuntimeException ignored) {
            // Observer 只能旁观，不能回滚或推进状态。
        }
    }
}
