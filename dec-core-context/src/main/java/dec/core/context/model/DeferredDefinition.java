package dec.core.context.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 需要由后续阶段处理的完整 Deferred 定义。
 */
public final class DeferredDefinition {
    private final DeferredKey key;
    private final RequiredStage requiredStage;
    private final String reasonCode;
    private final SourceRef sourceRef;
    private final NormalizedBody body;
    private final List<DefinitionKey> resolvedReferences;

    /**
     * 使用单一 DeferredKey 冻结 owner、kind 和 ordinal，避免身份分裂。
     *
     * @param key Deferred 定义的完整身份
     * @param requiredStage 后续负责执行该定义的阶段
     * @param reasonCode Deferred 的稳定原因码
     * @param sourceRef 定义来源位置
     * @param body 已规范化但尚未执行的内容
     * @param resolvedReferences 已完成类型化解析的引用
     */
    public DeferredDefinition(
            DeferredKey key,
            RequiredStage requiredStage,
            String reasonCode,
            SourceRef sourceRef,
            NormalizedBody body,
            List<DefinitionKey> resolvedReferences) {
        this.key = Objects.requireNonNull(key, "key");
        this.requiredStage = Objects.requireNonNull(requiredStage, "requiredStage");
        this.reasonCode = AbstractDefinitionKey.requireText(reasonCode, "reasonCode");
        this.sourceRef = Objects.requireNonNull(sourceRef, "sourceRef");
        this.body = Objects.requireNonNull(body, "body");
        this.resolvedReferences = immutableSortedReferences(resolvedReferences);
    }

    /**
     * 返回 Deferred 定义的完整身份。
     */
    public DeferredKey key() {
        return key;
    }

    /**
     * 返回 Deferred 定义所属对象。
     */
    public DefinitionKey ownerKey() {
        return key.owner();
    }

    /**
     * 返回 Deferred 类型。
     */
    public DeferredKind kind() {
        return key.kind();
    }

    /**
     * 返回同一 owner/kind 下的稳定序号。
     */
    public int ordinal() {
        return key.ordinal();
    }

    /**
     * 返回后续负责阶段。
     */
    public RequiredStage requiredStage() {
        return requiredStage;
    }

    /**
     * 返回 Deferred 原因码。
     */
    public String reasonCode() {
        return reasonCode;
    }

    /**
     * 返回定义来源位置。
     */
    public SourceRef sourceRef() {
        return sourceRef;
    }

    /**
     * 返回规范化定义体。
     */
    public NormalizedBody body() {
        return body;
    }

    /**
     * 返回按 DefinitionKey 稳定排序的已解析引用。
     */
    public List<DefinitionKey> resolvedReferences() {
        return resolvedReferences;
    }

    private static List<DefinitionKey> immutableSortedReferences(
            List<DefinitionKey> values) {
        Objects.requireNonNull(values, "resolvedReferences");
        List<DefinitionKey> copy = new ArrayList<DefinitionKey>(values.size());
        for (DefinitionKey value : values) {
            copy.add(Objects.requireNonNull(value, "resolvedReferences contains null"));
        }
        Collections.sort(copy);
        return Collections.unmodifiableList(copy);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DeferredDefinition)) {
            return false;
        }
        DeferredDefinition that = (DeferredDefinition) other;
        return key.equals(that.key)
                && requiredStage == that.requiredStage
                && reasonCode.equals(that.reasonCode)
                && sourceRef.equals(that.sourceRef)
                && body.equals(that.body)
                && resolvedReferences.equals(that.resolvedReferences);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                key,
                requiredStage,
                reasonCode,
                sourceRef,
                body,
                resolvedReferences);
    }

    @Override
    public String toString() {
        return key + "@" + requiredStage;
    }
}
