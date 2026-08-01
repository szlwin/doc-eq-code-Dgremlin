package dec.core.compiler.api;

import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticSeverity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Centralizes validation and defensive-copy rules shared by the public API.
 */
final class ApiContracts {
    private ApiContracts() {
        throw new AssertionError("No instances");
    }

    /**
     * Normalizes a required identifier while rejecting null and blank values.
     */
    static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return normalized;
    }

    /**
     * Copies diagnostics into deterministic order and prevents caller mutation.
     */
    static List<Diagnostic> immutableDiagnostics(List<Diagnostic> diagnostics) {
        Objects.requireNonNull(diagnostics, "diagnostics");
        List<Diagnostic> copy = new ArrayList<Diagnostic>(diagnostics.size());
        for (Diagnostic diagnostic : diagnostics) {
            copy.add(Objects.requireNonNull(diagnostic, "diagnostics contains null"));
        }
        Collections.sort(copy);
        return Collections.unmodifiableList(copy);
    }

    /**
     * Validates that a successful publication result does not carry an ERROR.
     */
    static List<Diagnostic> publishedDiagnostics(List<Diagnostic> diagnostics) {
        List<Diagnostic> copy = immutableDiagnostics(diagnostics);
        for (Diagnostic diagnostic : copy) {
            if (diagnostic.severity() == DiagnosticSeverity.ERROR) {
                throw new IllegalArgumentException(
                        "published diagnostics must not contain ERROR entries");
            }
        }
        return copy;
    }

    /**
     * Validates that a failed result has at least one explicit ERROR cause.
     */
    static List<Diagnostic> failedDiagnostics(List<Diagnostic> diagnostics) {
        List<Diagnostic> copy = immutableDiagnostics(diagnostics);
        if (copy.isEmpty()) {
            throw new IllegalArgumentException("failed diagnostics must not be empty");
        }
        for (Diagnostic diagnostic : copy) {
            if (diagnostic.severity() == DiagnosticSeverity.ERROR) {
                return copy;
            }
        }
        throw new IllegalArgumentException(
                "failed diagnostics must contain at least one ERROR entry");
    }
}
