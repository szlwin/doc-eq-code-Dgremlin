package dec.core.compiler.pass;

import dec.core.compiler.api.CompilationObserver;
import dec.core.compiler.api.CompilationRequest;
import dec.core.compiler.api.CompilationSessionState;
import dec.core.compiler.api.CompilationTiming;
import dec.core.compiler.api.PublicationRequest;
import dec.core.compiler.api.SessionStateTransition;
import dec.core.compiler.api.TimingPhase;
import dec.core.context.model.Diagnostic;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 按 DESIGN-R39 固定顺序协调九个普通 Pass 和最终 Publication Pass。
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

    private static final int PUBLICATION_INDEX = 9;
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
     * 防御性复制并验证十个 Pass 的名称、顺序和 capability 类型。
     */
    public CompilerPipeline(List<CompilerPass> passes) {
        List<CompilerPass> copy = new ArrayList<CompilerPass>(
                Objects.requireNonNull(passes, "passes"));
        validateFixedOrder(copy);
        this.passes = Collections.unmodifiableList(copy);
    }

    /** 返回 DESIGN-R39 冻结的 Pass 名称顺序。 */
    public static List<String> fixedPassOrder() {
        return FIXED_PASS_ORDER;
    }

    /** 返回防御性复制后的只读 Pass 列表。 */
    public List<CompilerPass> passes() {
        return passes;
    }

    /**
     * 创建新 Session，并在 execute 栈上局部持有 PublicationRequest。
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
        PublicationRequest checkedPublication = Objects.requireNonNull(
                publicationRequest,
                "publicationRequest");
        CompilationSession session = new CompilationSession(checkedRequest);

        for (int index = 0; index < PUBLICATION_INDEX; index++) {
            if (!executeOrdinaryPass(session, passes.get(index), index)) {
                return new PipelineExecutionResult(session);
            }
        }
        executePublicationPass(
                session,
                (PublicationCompilerPass) passes.get(PUBLICATION_INDEX),
                checkedPublication);
        return new PipelineExecutionResult(session);
    }

    /** 执行一个不持有 Publication capability 的普通 Pass。 */
    private static boolean executeOrdinaryPass(
            CompilationSession session,
            CompilerPass pass,
            int index) {
        String passName = FIXED_PASS_ORDER.get(index);
        if (!preflight(session, passName)) {
            return false;
        }
        Long startedNanos = readClockOrFail(session, passName);
        if (startedNanos == null) {
            return false;
        }

        session.recordPass(passName);
        PassContext context = new PassContext(session);
        PassResult passResult;
        try {
            passResult = Objects.requireNonNull(
                    pass.execute(context),
                    "pass result");
        } catch (RuntimeException failure) {
            addFailureAndStop(session, PipelineDiagnostics.passFailure(passName));
            return false;
        } finally {
            context.close();
        }

        Long endedNanos = readClockOrFail(session, passName);
        if (endedNanos == null) {
            return false;
        }
        recordTiming(session, passName, startedNanos.longValue(), endedNanos.longValue());
        session.addDiagnostics(passResult.diagnostics());
        if (session.hasErrors()) {
            fail(session);
            return false;
        }
        if (!preflight(session, passName)) {
            return false;
        }
        advanceOrdinaryState(session, index);
        return true;
    }

    /**
     * 执行最终 Publication Pass；提交成功后任何后置故障都不得降级状态。
     */
    private static void executePublicationPass(
            CompilationSession session,
            PublicationCompilerPass pass,
            PublicationRequest publicationRequest) {
        String passName = PUBLICATION_PASS;
        if (!preflight(session, passName)) {
            return;
        }
        Long startedNanos = readClockOrFail(session, passName);
        if (startedNanos == null) {
            return;
        }

        session.recordPass(passName);
        PublicationPassContext context = new PublicationPassContext(
                session,
                publicationRequest);
        PassResult passResult = null;
        RuntimeException passFailure = null;
        try {
            passResult = pass.execute(context);
        } catch (RuntimeException failure) {
            passFailure = failure;
        } finally {
            context.close();
        }

        if (context.publicationSucceeded()
                || session.state() == CompilationSessionState.PUBLISHED) {
            // 外部提交已经成功：timing 只做尽力记录，任何后置故障均不改变语义终态。
            recordCommittedTimingSafely(
                    session,
                    passName,
                    startedNanos.longValue());
            return;
        }

        Long endedNanos = readClockOrFail(session, passName);
        if (endedNanos == null) {
            return;
        }
        recordTiming(session, passName, startedNanos.longValue(), endedNanos.longValue());

        if (passFailure != null) {
            if (!session.hasErrors()) {
                session.addDiagnostics(Collections.singletonList(
                        PipelineDiagnostics.publicationFailure()));
            }
            fail(session);
            return;
        }
        if (passResult == null) {
            addFailureAndStop(session, PipelineDiagnostics.publicationFailure());
            return;
        }
        session.addDiagnostics(passResult.diagnostics());
        if (session.hasErrors()) {
            fail(session);
            return;
        }
        if (!context.publishAttempted()) {
            addFailureAndStop(session, PipelineDiagnostics.publicationBlocked());
            return;
        }
        addFailureAndStop(session, PipelineDiagnostics.publicationFailure());
    }

    /** 在执行任何 Pass 前验证完整固定顺序和 capability 类型。 */
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
            if (index < PUBLICATION_INDEX && pass instanceof PublicationCompilerPass) {
                throw new IllegalArgumentException(
                        "ordinary pass must not implement PublicationCompilerPass");
            }
            if (index == PUBLICATION_INDEX
                    && !(pass instanceof PublicationCompilerPass)) {
                throw new IllegalArgumentException(
                        "final pass must implement PublicationCompilerPass");
            }
        }
    }

    /**
     * 在 Pass 前或普通 Pass 后检查 token 与 Deadline，并准确分类基础设施异常。
     */
    private static boolean preflight(
            CompilationSession session,
            String passName) {
        CompilationRequest request = session.request();
        try {
            if (request.cancellationToken().isCancellationRequested()) {
                addFailureAndStop(session, PipelineDiagnostics.cancelled(passName));
                return false;
            }
        } catch (RuntimeException failure) {
            addFailureAndStop(
                    session,
                    PipelineDiagnostics.cancellationTokenFailure(passName));
            return false;
        }
        if (request.deadline().isPresent()) {
            long now;
            try {
                now = request.clock().nanoTime();
            } catch (RuntimeException failure) {
                addFailureAndStop(session, PipelineDiagnostics.clockFailure(passName));
                return false;
            }
            if (request.deadline().get().isExpired(now)) {
                addFailureAndStop(session, PipelineDiagnostics.timedOut(passName));
                return false;
            }
        }
        return true;
    }

    /** 读取 start/end clock；失败时形成独立 Diagnostic 并停止。 */
    private static Long readClockOrFail(
            CompilationSession session,
            String passName) {
        try {
            return Long.valueOf(session.request().clock().nanoTime());
        } catch (RuntimeException failure) {
            addFailureAndStop(session, PipelineDiagnostics.clockFailure(passName));
            return null;
        }
    }

    /** 按 Pass 索引推进前九阶段的唯一成功状态路径。 */
    private static void advanceOrdinaryState(
            CompilationSession session,
            int index) {
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
            default:
                throw new IllegalStateException("unexpected ordinary pass index: " + index);
        }
    }

    /** 推进 Session 状态，并让 Observer 只读观察实际转换。 */
    private static void transition(
            CompilationSession session,
            CompilationSessionState next) {
        SessionStateTransition transition = session.transitionTo(next);
        notifyTransition(session.request().observer(), transition);
    }

    /** 在非终态添加 Diagnostic 并进入 FAILED。 */
    private static void addFailureAndStop(
            CompilationSession session,
            Diagnostic diagnostic) {
        if (!CompilationSession.isTerminal(session.state())) {
            session.addDiagnostics(Collections.singletonList(diagnostic));
            fail(session);
        }
    }

    /** 将当前非终态 Session 统一推进到 FAILED。 */
    private static void fail(CompilationSession session) {
        if (!CompilationSession.isTerminal(session.state())) {
            transition(session, CompilationSessionState.FAILED);
        }
    }

    /** 记录非负 Pass 耗时，并交给 Observer 只读观察。 */
    private static void recordTiming(
            CompilationSession session,
            String passName,
            long startedNanos,
            long endedNanos) {
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

    /** 发布成功后尽力补充 timing，任何异常均不得否定提交。 */
    private static void recordCommittedTimingSafely(
            CompilationSession session,
            String passName,
            long startedNanos) {
        try {
            long endedNanos = session.request().clock().nanoTime();
            recordTiming(session, passName, startedNanos, endedNanos);
        } catch (RuntimeException ignored) {
            // post-commit timing 是非语义观察事实；失败时允许缺失，不降级 PUBLISHED。
        }
    }

    /** Observer timing 回调失败不得改变 Session 状态或结果。 */
    private static void notifyTiming(
            CompilationObserver observer,
            CompilationTiming timing) {
        try {
            observer.onTiming(timing);
        } catch (RuntimeException ignored) {
            // T13 将补充 Observer Failure Diagnostic；T12 只保证不改变语义事实。
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
