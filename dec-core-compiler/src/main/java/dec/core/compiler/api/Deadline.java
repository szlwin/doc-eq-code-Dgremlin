package dec.core.compiler.api;

/**
 * 与注入 MonotonicClock 共用纳秒域的绝对截止时间。
 */
public final class Deadline {
    private final long deadlineNanos;

    /**
     * 创建绝对截止时间，并拒绝负纳秒值。
     *
     * @param deadlineNanos 单调时钟域中的绝对纳秒值
     */
    public Deadline(long deadlineNanos) {
        if (deadlineNanos < 0L) {
            throw new IllegalArgumentException("deadlineNanos must be >= 0");
        }
        this.deadlineNanos = deadlineNanos;
    }

    /**
     * 返回单调时钟域中的绝对截止纳秒值。
     */
    public long deadlineNanos() {
        return deadlineNanos;
    }

    /**
     * 判断给定的同域单调时钟值是否已经达到截止时间。
     */
    public boolean isExpired(long nowNanos) {
        return nowNanos >= deadlineNanos;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof Deadline
                && deadlineNanos == ((Deadline) other).deadlineNanos);
    }

    @Override
    public int hashCode() {
        return Long.valueOf(deadlineNanos).hashCode();
    }

    @Override
    public String toString() {
        return "Deadline{deadlineNanos=" + deadlineNanos + '}';
    }
}
