package dec.core.compiler.api;

import dec.core.context.EngineContext;
import java.util.Objects;

/**
 * 调用方提供的不可变条件发布边界。
 */
public final class PublicationRequest {
    private final EngineContext expectedCurrent;
    private final ContextPublisher publisher;

    /**
     * 绑定当前 Context 预期与唯一允许的发布副作用。
     *
     * <p>{@code expectedCurrent} 为空表示首次发布；非空时由 Publisher 执行 compare-and-set。</p>
     *
     * @param expectedCurrent 可为空的当前 Context 预期
     * @param publisher 非空原子发布器
     */
    public PublicationRequest(EngineContext expectedCurrent, ContextPublisher publisher) {
        this.expectedCurrent = expectedCurrent;
        this.publisher = Objects.requireNonNull(publisher, "publisher");
    }

    /**
     * 返回调用方提供的可空 compare-and-set 预期。
     */
    public EngineContext expectedCurrent() {
        return expectedCurrent;
    }

    /**
     * 返回唯一允许暴露候选 Context 的发布器。
     */
    public ContextPublisher publisher() {
        return publisher;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PublicationRequest)) {
            return false;
        }
        PublicationRequest that = (PublicationRequest) other;
        return Objects.equals(expectedCurrent, that.expectedCurrent)
                && publisher.equals(that.publisher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expectedCurrent, publisher);
    }

    @Override
    public String toString() {
        return "PublicationRequest{"
                + "expectedCurrent=" + expectedCurrent
                + ", publisher=" + publisher.getClass().getName()
                + '}';
    }
}
