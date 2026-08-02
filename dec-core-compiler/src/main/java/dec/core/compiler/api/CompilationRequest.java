package dec.core.compiler.api;

import dec.core.compiler.canonical.FrontendRegistry;
import dec.core.compiler.source.DocumentSourceProvider;
import dec.core.compiler.source.SourceReference;
import java.util.Objects;
import java.util.Optional;

/**
 * 单次 CompilationSession 的完整不可变输入边界。
 */
public final class CompilationRequest {
    private final SourceReference root;
    private final DocumentSourceProvider sourceProvider;
    private final FrontendRegistry frontends;
    private final CompilationOptions options;
    private final Optional<Deadline> deadline;
    private final CancellationToken cancellationToken;
    private final MonotonicClock clock;
    private final CompilationObserver observer;

    /**
     * 冻结单次 Session 使用的全部显式依赖。
     *
     * <p>Source、Frontend、Deadline、Clock 和 Observer 均由调用方注入，
     * Compiler 不得从 static、thread-local 或系统时钟读取这些依赖。</p>
     *
     * @param root 根 Source 引用
     * @param sourceProvider 文档 Source 解析器
     * @param frontends 可替换 Frontend 注册表
     * @param options 参与语义身份的不可变选项
     * @param deadline 可选绝对单调时钟截止时间
     * @param cancellationToken 会话级协作式取消入口
     * @param clock Deadline 与 Timing 共用的单调时钟
     * @param observer 计时与状态转换观察器
     */
    public CompilationRequest(
            SourceReference root,
            DocumentSourceProvider sourceProvider,
            FrontendRegistry frontends,
            CompilationOptions options,
            Optional<Deadline> deadline,
            CancellationToken cancellationToken,
            MonotonicClock clock,
            CompilationObserver observer) {
        this.root = Objects.requireNonNull(root, "root");
        this.sourceProvider = Objects.requireNonNull(sourceProvider, "sourceProvider");
        this.frontends = Objects.requireNonNull(frontends, "frontends");
        this.options = Objects.requireNonNull(options, "options");
        this.deadline = Objects.requireNonNull(deadline, "deadline");
        this.cancellationToken = Objects.requireNonNull(
                cancellationToken,
                "cancellationToken");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.observer = Objects.requireNonNull(observer, "observer");
    }

    /**
     * 返回根 Source 引用。
     */
    public SourceReference root() {
        return root;
    }

    /**
     * 返回调用方注入的文档 Source Provider。
     */
    public DocumentSourceProvider sourceProvider() {
        return sourceProvider;
    }

    /**
     * 返回调用方注入的 Frontend 注册表。
     */
    public FrontendRegistry frontends() {
        return frontends;
    }

    /**
     * 返回参与语义身份的不可变选项。
     */
    public CompilationOptions options() {
        return options;
    }

    /**
     * 返回可选的绝对单调时钟截止时间。
     */
    public Optional<Deadline> deadline() {
        return deadline;
    }

    /**
     * 返回本次会话的协作式取消令牌。
     */
    public CancellationToken cancellationToken() {
        return cancellationToken;
    }

    /**
     * 返回 Deadline 与 Timing 共用的单调时钟实例。
     */
    public MonotonicClock clock() {
        return clock;
    }

    /**
     * 返回计时与状态转换观察器实例。
     */
    public CompilationObserver observer() {
        return observer;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CompilationRequest)) {
            return false;
        }
        CompilationRequest that = (CompilationRequest) other;
        return root.equals(that.root)
                && sourceProvider.equals(that.sourceProvider)
                && frontends.equals(that.frontends)
                && options.equals(that.options)
                && deadline.equals(that.deadline)
                && cancellationToken.equals(that.cancellationToken)
                && clock.equals(that.clock)
                && observer.equals(that.observer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                root,
                sourceProvider,
                frontends,
                options,
                deadline,
                cancellationToken,
                clock,
                observer);
    }

    @Override
    public String toString() {
        return "CompilationRequest{"
                + "root=" + root
                + ", options=" + options
                + ", deadline=" + deadline
                + '}';
    }
}
