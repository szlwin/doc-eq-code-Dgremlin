package dec.core.compiler.api;

import java.util.Objects;

/**
 * CompilationObserver 可观察的不可变 Session 状态转换。
 */
public final class SessionStateTransition {
    private final CompilationSessionState from;
    private final CompilationSessionState to;

    /**
     * 创建状态转换，并拒绝空值和原地转换。
     *
     * @param from 转换前状态
     * @param to 转换后状态
     */
    public SessionStateTransition(
            CompilationSessionState from,
            CompilationSessionState to) {
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
        if (from == to) {
            throw new IllegalArgumentException("state transition must change state");
        }
    }

    /**
     * 返回转换前状态。
     */
    public CompilationSessionState from() {
        return from;
    }

    /**
     * 返回转换后状态。
     */
    public CompilationSessionState to() {
        return to;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SessionStateTransition)) {
            return false;
        }
        SessionStateTransition that = (SessionStateTransition) other;
        return from == that.from && to == that.to;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public String toString() {
        return "SessionStateTransition{from=" + from + ", to=" + to + '}';
    }
}
