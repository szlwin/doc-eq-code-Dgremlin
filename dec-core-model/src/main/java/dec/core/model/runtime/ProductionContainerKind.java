package dec.core.model.runtime;

/** 生产 root 只接受受控 Container 类型，不接受 caller 注入 Container 实例。 */
public enum ProductionContainerKind { COMMIT, SYNCHRONIZED }
