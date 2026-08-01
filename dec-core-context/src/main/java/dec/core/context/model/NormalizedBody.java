package dec.core.context.model;

import java.util.Objects;

public final class NormalizedBody {
    private final String format;
    private final String value;

    public NormalizedBody(String format, String value) {
        this.format = AbstractDefinitionKey.requireText(format, "format");
        this.value = Objects.requireNonNull(value, "value");
    }
    public String format() { return format; }
    public String value() { return value; }
    @Override public boolean equals(Object other) {
        return this == other || (other instanceof NormalizedBody
                && format.equals(((NormalizedBody) other).format)
                && value.equals(((NormalizedBody) other).value));
    }
    @Override public int hashCode() { return Objects.hash(format, value); }
    @Override public String toString() { return format + ":" + value; }
}
