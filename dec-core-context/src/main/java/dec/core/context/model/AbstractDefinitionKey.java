package dec.core.context.model;

import java.util.Objects;

abstract class AbstractDefinitionKey implements DefinitionKey {
    private final String canonical;

    AbstractDefinitionKey(String canonical) {
        this.canonical = requireText(canonical, "canonical");
    }

    @Override
    public final String canonical() {
        return canonical;
    }

    @Override
    public final int compareTo(DefinitionKey other) {
        Objects.requireNonNull(other, "other");
        int comparison = canonical.compareTo(other.canonical());
        return comparison != 0
                ? comparison
                : getClass().getName().compareTo(other.getClass().getName());
    }

    @Override
    public final boolean equals(Object other) {
        return this == other
                || (other != null
                && getClass() == other.getClass()
                && canonical.equals(((AbstractDefinitionKey) other).canonical));
    }

    @Override
    public final int hashCode() {
        return 31 * getClass().hashCode() + canonical.hashCode();
    }

    @Override
    public final String toString() {
        return canonical;
    }

    static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return normalized;
    }
}
