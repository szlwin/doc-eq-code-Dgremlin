package dec.core.compiler.api;

import dec.core.context.EngineContext;
import java.util.Optional;

/**
 * 负责单次 Compiler 调用中唯一允许的条件发布副作用。
 */
public interface ContextPublisher {
    /**
     * 仅当调用方给出的当前 Context 预期仍与实际状态匹配时，原子发布候选 Context。
     *
     * @param expectedCurrent 显式可选的 compare-and-set 预期
     * @param candidate 已完整构造且不可变的候选 Context
     * @return 包含独立 PublicationStatus 的发布结果
     */
    PublicationResult publish(
            Optional<EngineContext> expectedCurrent,
            EngineContext candidate);
}
