package dec.core.model.runtime;

import dec.core.context.model.ModelPath;
import dec.core.context.runtime.ResolvedRuntimeTarget;
import dec.core.context.runtime.RuntimeModelSessionId;
import dec.core.context.runtime.RuntimeMutationVersion;
import dec.core.context.runtime.RuntimeObjectId;

/** MODEL minted 的 sealed locator/atomicity scope；不是业务 Session，也不授予权限。 */
public interface RuntimeModelSession extends AutoCloseable {
    RuntimeModelSessionId sessionId();

    /** 注册同 Scope frame 的 trusted Handle，并由 MODEL mint opaque objectId。 */
    RuntimeObjectId register(RuntimeModelHandle handle) throws RuntimeModelSessionException;

    /** 一次性封存注册表；封存后才能用于 resolver/effect provider。 */
    void seal() throws RuntimeModelSessionException;

    /** 只定位同 session/object/binding proof 的已注册对象；不做 fallback。 */
    LocatedRuntimeObject locate(ResolvedRuntimeTarget target);

    /** 返回同 target/path 的当前单调版本；effect 前必须再次比较。 */
    RuntimeMutationVersion currentVersion(ResolvedRuntimeTarget target, ModelPath modelPath);

    @Override
    void close();
}
