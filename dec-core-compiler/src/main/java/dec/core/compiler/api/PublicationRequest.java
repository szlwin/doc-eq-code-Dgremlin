package dec.core.compiler.api;

import dec.core.context.EngineContext;
import java.util.Objects;
import java.util.Optional;

/**
 * 调用方提供的不可变条件发布边界。
 */
public final class PublicationRequest {
    private final Optional<EngineContext> expectedCurrent;
    private final ContextPublisher publisher;

    /**
     * 绑定当前 Context 预期与唯一允许的发布副作用。
     *
     * <p>{@link Optional#empty()} 明确表示首次发布；Optional 本身不接受 null，
     * 从而区分首次发布与调用方漏传参数。</p>
     *
     * @param expectedCurrent 当前 Context 的显式可选预期
     * @param publisher 非空原子发布器
     */
    public PublicationRequest(
            Optional<EngineContext> expectedCurrent,
            ContextPublisher publisher) {
        this.expectedCurrent = Objects.requireNonNull(
                expectedCurrent,
                "expectedCurrent");
        this.publisher = Objects.requireNonNull(publisher, "publisher");
    }

    /**
     * 返回调用方提供的 compare-and-set 预期。
     */
    public Optional<EngineContext> expectedCurrent() {
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
        return expectedCurrent.equals(that.expectedCurrent)
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
