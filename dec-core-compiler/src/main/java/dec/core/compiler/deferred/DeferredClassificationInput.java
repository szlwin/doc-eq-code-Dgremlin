package dec.core.compiler.deferred;

import dec.core.context.model.DefinitionKey;
import dec.core.context.model.DeferredKind;
import dec.core.context.model.NormalizedBody;
import dec.core.context.model.SourceRef;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Deferred 分类前的不可变请求快照。
 *
 * <p>该对象允许字段缺失，以便分类阶段统一产生业务 Diagnostic；它不是可发布模型。</p>
 */
public final class DeferredClassificationInput {
    private final DefinitionKey ownerKey;
    private final DeferredKind kind;
    private final Integer ordinal;
    private final String reasonCode;
    private final SourceRef sourceRef;
    private final NormalizedBody body;
    private final List<DefinitionKey> resolvedReferences;
    private final List<String> unresolvedReferences;
    private final boolean resolvedReferencesProvided;

    private DeferredClassificationInput(Builder builder) {
        this.ownerKey = builder.ownerKey;
        this.kind = builder.kind;
        this.ordinal = builder.ordinal;
        this.reasonCode = builder.reasonCode;
        this.sourceRef = builder.sourceRef;
        this.body = builder.body;
        this.resolvedReferences = immutableCopy(builder.resolvedReferences);
        this.unresolvedReferences = immutableTextCopy(builder.unresolvedReferences);
        this.resolvedReferencesProvided = builder.resolvedReferencesProvided;
    }

    /** 创建新的分类输入 Builder。 */
    public static Builder builder() {
        return new Builder();
    }

    public Optional<DefinitionKey> ownerKey() {
        return Optional.ofNullable(ownerKey);
    }

    public Optional<DeferredKind> kind() {
        return Optional.ofNullable(kind);
    }

    public Optional<Integer> ordinal() {
        return Optional.ofNullable(ordinal);
    }

    public Optional<String> reasonCode() {
        return Optional.ofNullable(reasonCode);
    }

    public Optional<SourceRef> sourceRef() {
        return Optional.ofNullable(sourceRef);
    }

    public Optional<NormalizedBody> body() {
        return Optional.ofNullable(body);
    }

    /** 返回强类型引用；未显式提供时仍返回空列表，并由 provided 标记区分。 */
    public List<DefinitionKey> resolvedReferences() {
        return resolvedReferences;
    }

    public boolean resolvedReferencesProvided() {
        return resolvedReferencesProvided;
    }

    /** 返回仍未类型化的 lexical 引用。成功输入必须为空。 */
    public List<String> unresolvedReferences() {
        return unresolvedReferences;
    }

    private static List<DefinitionKey> immutableCopy(List<DefinitionKey> values) {
        if (values == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<DefinitionKey>(values));
    }

    private static List<String> immutableTextCopy(List<String> values) {
        if (values == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<String>(values));
    }

    /**
     * 逐字段收集分类输入；缺字段由分类器统一诊断，不在 Builder 中提前抛出。
     */
    public static final class Builder {
        private DefinitionKey ownerKey;
        private DeferredKind kind;
        private Integer ordinal;
        private String reasonCode;
        private SourceRef sourceRef;
        private NormalizedBody body;
        private List<DefinitionKey> resolvedReferences;
        private List<String> unresolvedReferences = Collections.emptyList();
        private boolean resolvedReferencesProvided;

        public Builder ownerKey(DefinitionKey value) {
            this.ownerKey = value;
            return this;
        }

        public Builder kind(DeferredKind value) {
            this.kind = value;
            return this;
        }

        public Builder ordinal(Integer value) {
            this.ordinal = value;
            return this;
        }

        public Builder reasonCode(String value) {
            this.reasonCode = value;
            return this;
        }

        public Builder sourceRef(SourceRef value) {
            this.sourceRef = value;
            return this;
        }

        public Builder body(NormalizedBody value) {
            this.body = value;
            return this;
        }

        public Builder resolvedReferences(List<DefinitionKey> values) {
            this.resolvedReferences = values;
            this.resolvedReferencesProvided = true;
            return this;
        }

        public Builder unresolvedReferences(List<String> values) {
            this.unresolvedReferences = values;
            return this;
        }

        public DeferredClassificationInput build() {
            return new DeferredClassificationInput(this);
        }
    }
}
