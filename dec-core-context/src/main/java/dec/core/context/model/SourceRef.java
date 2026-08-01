package dec.core.context.model;

import java.util.Objects;

public final class SourceRef implements Comparable<SourceRef> {
    private final String sourceId;
    private final int line;
    private final int column;
    private final String nodePath;

    public SourceRef(String sourceId, int line, int column, String nodePath) {
        this.sourceId = AbstractDefinitionKey.requireText(sourceId, "sourceId");
        if (line < 0 || column < 0) {
            throw new IllegalArgumentException("line and column must be >= 0");
        }
        this.line = line;
        this.column = column;
        this.nodePath = nodePath == null ? "" : nodePath.trim();
    }

    public String sourceId() { return sourceId; }
    public int line() { return line; }
    public int column() { return column; }
    public String nodePath() { return nodePath; }

    @Override
    public int compareTo(SourceRef other) {
        Objects.requireNonNull(other, "other");
        int comparison = sourceId.compareTo(other.sourceId);
        if (comparison != 0) return comparison;
        comparison = Integer.compare(line, other.line);
        if (comparison != 0) return comparison;
        comparison = Integer.compare(column, other.column);
        if (comparison != 0) return comparison;
        return nodePath.compareTo(other.nodePath);
    }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof SourceRef)) return false;
        SourceRef that = (SourceRef) other;
        return line == that.line && column == that.column
                && sourceId.equals(that.sourceId) && nodePath.equals(that.nodePath);
    }
    @Override public int hashCode() { return Objects.hash(sourceId, line, column, nodePath); }
    @Override public String toString() {
        return sourceId + ":" + line + ":" + column + (nodePath.isEmpty() ? "" : "#" + nodePath);
    }
}
