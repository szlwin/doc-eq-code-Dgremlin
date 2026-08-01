package dec.core.compiler.api;

import java.util.Objects;

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
        if (deadlineNanos < 0L) {
            throw new IllegalArgumentException("deadlineNanos must be >= 0");
        }
        this.schemaVersion = ApiContracts.requireText(schemaVersion, "schemaVersion");
        this.optionsVersion = ApiContracts.requireText(optionsVersion, "optionsVersion");
        this.deadlineNanos = deadlineNanos;
    }

    /**
     * Returns the schema contract used to interpret the requested sources.
     */
    public String schemaVersion() {
        return schemaVersion;
    }

    /**
     * Returns the normalized option-set version included in semantic identity.
     */
    public String optionsVersion() {
        return optionsVersion;
    }

    /**
     * Returns the absolute deadline in the injected monotonic-clock domain.
     */
    public long deadlineNanos() {
        return deadlineNanos;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CompilationOptions)) {
            return false;
        }
        CompilationOptions that = (CompilationOptions) other;
        return deadlineNanos == that.deadlineNanos
                && schemaVersion.equals(that.schemaVersion)
                && optionsVersion.equals(that.optionsVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schemaVersion, optionsVersion, deadlineNanos);
    }

    @Override
    public String toString() {
        return "CompilationOptions{"
                + "schemaVersion='" + schemaVersion + '\''
                + ", optionsVersion='" + optionsVersion + '\''
                + ", deadlineNanos=" + deadlineNanos
                + '}';
    }
}
