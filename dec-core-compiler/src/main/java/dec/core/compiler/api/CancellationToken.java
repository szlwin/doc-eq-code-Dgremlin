package dec.core.compiler.api;

/**
 * Supplies cooperative cancellation state for one compilation session.
 */
public interface CancellationToken {
    /**
     * Returns whether the current compilation should stop before its next
     * observable state transition.
     *
     * @return {@code true} when cancellation has been requested
     */
    boolean isCancellationRequested();
}
