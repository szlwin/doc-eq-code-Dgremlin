package dec.core.model.runtime;

import dec.core.context.model.ModelPath;
import dec.core.context.runtime.ResolvedRuntimeTarget;
import dec.core.context.runtime.RuntimeModelSessionId;
import dec.core.context.runtime.RuntimeMutationVersion;
import dec.core.context.runtime.RuntimeObjectId;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/** MODEL minted cross-module trusted scope；Session/EffectProvider 只能从这个 Scope 派生。 */
public final class RuntimeModelAccessScope {
    private static final AtomicLong SESSION_SEQUENCE = new AtomicLong();
    private final RuntimeModelFrame frame;
    private final RuntimeModelEffectProvider effectProvider = new ScopeEffectProvider(this);
    private boolean active = true;

    RuntimeModelAccessScope(RuntimeModelFrame frame) {
        this.frame = Objects.requireNonNull(frame, "frame");
    }

    public RuntimeModelFrame frame() {
        return frame;
    }

    /** 创建绑定当前 Scope 的 Session；inactive Scope 必须在注册任何 Handle 前 fail closed。 */
    public synchronized RuntimeModelSession beginSession() throws RuntimeModelSessionException {
        if (!active) {
            throw new RuntimeModelSessionException(RuntimeModelSessionFailureCode.SCOPE_INACTIVE);
        }
        return new ScopeSession(
                this,
                RuntimeModelSessionId.of("model-session-" + SESSION_SEQUENCE.incrementAndGet()));
    }

    /** 返回当前 Scope 唯一 provider；provider 本身不接受外部 operation port 注入。 */
    public RuntimeModelEffectProvider effectProvider() {
        return effectProvider;
    }

    /** root 关闭或新 trusted load 使 Scope 失活；caller 不能恢复。 */
    synchronized void deactivate() {
        active = false;
    }

    synchronized boolean active() {
        return active;
    }

    /** DEV-06 skeleton：冻结 Session 生命周期入口，具体注册表/lease/version 算法待 Review 后实现。 */
    private static final class ScopeSession implements RuntimeModelSession {
        private final RuntimeModelAccessScope ownerScope;
        private final RuntimeModelSessionId sessionId;
        private boolean sealed;
        private boolean closed;

        private ScopeSession(RuntimeModelAccessScope ownerScope, RuntimeModelSessionId sessionId) {
            this.ownerScope = Objects.requireNonNull(ownerScope, "ownerScope");
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        }

        @Override
        public RuntimeModelSessionId sessionId() {
            return sessionId;
        }

        /** 注册算法必须检查 frame membership、duplicate 和跨 Session identity lease。 */
        @Override
        public RuntimeObjectId register(RuntimeModelHandle handle) throws RuntimeModelSessionException {
            throw new UnsupportedOperationException("ARCHITECTURE_SKELETON: trusted handle registration not implemented");
        }

        /** seal 必须一次性冻结 object table；重复 seal 稳定失败。 */
        @Override
        public void seal() throws RuntimeModelSessionException {
            throw new UnsupportedOperationException("ARCHITECTURE_SKELETON: session sealing not implemented");
        }

        /** locator 必须 exact 匹配 session/object/binding proof；不存在或 stale 不允许 fallback。 */
        @Override
        public LocatedRuntimeObject locate(ResolvedRuntimeTarget target) {
            throw new UnsupportedOperationException("ARCHITECTURE_SKELETON: exact object locator not implemented");
        }

        /** 版本按同一 actual ModelData/path 协调单元读取，供 Guard 后 effect 重验。 */
        @Override
        public RuntimeMutationVersion currentVersion(
                ResolvedRuntimeTarget target,
                ModelPath modelPath) {
            throw new UnsupportedOperationException("ARCHITECTURE_SKELETON: mutation version lookup not implemented");
        }

        /** close 必须释放该 Session 持有的全部 actual ModelData leases。 */
        @Override
        public void close() {
            closed = true;
        }

        private boolean sealed() {
            return sealed;
        }

        private boolean closed() {
            return closed;
        }

        private boolean belongsTo(RuntimeModelAccessScope scope) {
            return ownerScope == scope;
        }
    }

    /** DEV-06 skeleton：先冻结 bind 的四类 fail-closed 检查，再实现私有 operation port。 */
    private static final class ScopeEffectProvider implements RuntimeModelEffectProvider {
        private final RuntimeModelAccessScope ownerScope;

        private ScopeEffectProvider(RuntimeModelAccessScope ownerScope) {
            this.ownerScope = Objects.requireNonNull(ownerScope, "ownerScope");
        }

        @Override
        public RuntimeModelEffectBindingResult bind(RuntimeModelSession sealedSession) {
            Objects.requireNonNull(sealedSession, "sealedSession");
            if (!ownerScope.active()) {
                return RuntimeModelEffectBindingResult.failed(
                        RuntimeModelEffectBindingFailureCode.SCOPE_INACTIVE);
            }
            if (!(sealedSession instanceof ScopeSession)
                    || !((ScopeSession) sealedSession).belongsTo(ownerScope)) {
                return RuntimeModelEffectBindingResult.failed(
                        RuntimeModelEffectBindingFailureCode.SESSION_SCOPE_MISMATCH);
            }
            ScopeSession session = (ScopeSession) sealedSession;
            if (session.closed()) {
                return RuntimeModelEffectBindingResult.failed(
                        RuntimeModelEffectBindingFailureCode.SESSION_CLOSED);
            }
            if (!session.sealed()) {
                return RuntimeModelEffectBindingResult.failed(
                        RuntimeModelEffectBindingFailureCode.SESSION_NOT_SEALED);
            }
            throw new UnsupportedOperationException(
                    "ARCHITECTURE_SKELETON: same-session operation port not implemented");
        }
    }
}
