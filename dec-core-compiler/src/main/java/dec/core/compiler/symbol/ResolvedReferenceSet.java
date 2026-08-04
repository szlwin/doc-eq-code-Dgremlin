package dec.core.compiler.symbol;

import dec.core.context.model.DefinitionKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 成功解析后发布的完整、稳定有序且不可变的引用集合。
 */
public final class ResolvedReferenceSet {
    private final List<ResolvedReference> references;

    /**
     * 防御复制并冻结全部引用。
     */
    public ResolvedReferenceSet(List<ResolvedReference> references) {
        Objects.requireNonNull(references, "references");
        List<ResolvedReference> copy =
                new ArrayList<ResolvedReference>(references.size());
        for (ResolvedReference reference : references) {
            copy.add(Objects.requireNonNull(
                    reference,
                    "references contains null"));
        }
        Collections.sort(copy);
        this.references = Collections.unmodifiableList(copy);
    }

    public List<ResolvedReference> references() {
        return references;
    }

    /**
     * 返回指定来源定义的稳定引用子集。
     */
    public List<ResolvedReference> referencesFrom(DefinitionKey sourceKey) {
        DefinitionKey required = Objects.requireNonNull(sourceKey, "sourceKey");
        List<ResolvedReference> values = new ArrayList<ResolvedReference>();
        for (ResolvedReference reference : references) {
            if (reference.sourceKey().equals(required)) {
                values.add(reference);
            }
        }
        return Collections.unmodifiableList(values);
    }

    public int size() {
        return references.size();
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof ResolvedReferenceSet
                && references.equals(((ResolvedReferenceSet) other).references);
    }

    @Override
    public int hashCode() {
        return references.hashCode();
    }

    @Override
    public String toString() {
        return "ResolvedReferenceSet" + references;
    }
}
