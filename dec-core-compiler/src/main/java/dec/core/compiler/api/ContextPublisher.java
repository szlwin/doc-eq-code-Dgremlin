package dec.core.compiler.api;

import dec.core.context.EngineContext;

/**
 * 负责单次 Compiler 调用中唯一允许的条件发布副作用。
 */
public interface ContextPublisher {
    /**
     * 仅当调用方给出的当前 Context 预期仍与实际暴露状态匹配时，原子发布候选 Context。
     *
     * @param expectedCurrent 可为空的 compare-and-set 预期；空值表示首次发布
     * @param candidate 已完整构造且不可变的候选 Context
     * @return 发布成功或 compare-and-set 冲突
     */
    PublicationResult publish(EngineContext expectedCurrent, EngineContext candidate);
}
