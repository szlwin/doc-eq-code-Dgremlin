package dec.core.compiler.api;

/**
 * Immutable compiler option and monotonic-deadline boundary.
 */
public final class CompilationOptions {
    private final String schemaVersion;
    private final String optionsVersion;
    private final long deadlineNanos;

    /**
     * Defines the stable schema/options versions and absolute monotonic deadline.
     *
     * @param schemaVersion source schema contract version
     * @param optionsVersion normalized option-set version
     * @param deadlineNanos absolute monotonic deadline, or {@link Long#MAX_VALUE}
     */
    public CompilationOptions(
            String schemaVersion,
            String optionsVersion,
            long deadlineNanos) {
        throw new UnsupportedOperationException("Architecture skeleton only");
    }

    public String schemaVersion() {
        throw new UnsupportedOperationException("Architecture skeleton only");
    }

    public String optionsVersion() {
        throw new UnsupportedOperationException("Architecture skeleton only");
    }

    public long deadlineNanos() {
        throw new UnsupportedOperationException("Architecture skeleton only");
    }
}
