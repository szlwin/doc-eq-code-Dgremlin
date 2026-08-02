package dec.core.compiler.api;

/**
 * 为 Deadline 和 Timing 提供同一可测试纳秒域的单调时钟。
 */
public interface MonotonicClock {
    /**
     * 返回只保证单调性的纳秒值，不表示墙钟时间。
     */
    long nanoTime();
}
