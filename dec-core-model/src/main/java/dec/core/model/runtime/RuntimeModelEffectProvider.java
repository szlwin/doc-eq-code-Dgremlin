package dec.core.model.runtime;

/** Scope-owned provider；只能绑定该 Scope 自己创建且已经 seal 的 Session。 */
public interface RuntimeModelEffectProvider {
    RuntimeModelEffectBindingResult bind(RuntimeModelSession sealedSession);
}
