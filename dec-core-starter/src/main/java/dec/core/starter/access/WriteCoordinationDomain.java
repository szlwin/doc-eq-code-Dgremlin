package dec.core.starter.access;

import dec.core.context.model.ModelPath;
import dec.core.context.runtime.ResolvedRuntimeTarget;
import dec.core.context.runtime.RuntimeMutationVersion;
import dec.core.context.runtime.RuntimeObjectId;
import dec.core.context.runtime.RuntimeModelSessionId;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** STARTER-private non-blocking coordination for overlapping writes to one exact runtime object/path. */
final class WriteCoordinationDomain {
    private final ConcurrentMap<CoordinationKey, Boolean> active =
            new ConcurrentHashMap<CoordinationKey, Boolean>();

    Claim tryAcquire(ResolvedRuntimeTarget target, ModelPath path) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(path, "path");
        CoordinationKey key = new CoordinationKey(
                target.sessionId(), target.runtimeObjectId(), path);
        if (active.putIfAbsent(key, Boolean.TRUE) != null) {
            return null;
        }
        return new Claim(this, key);
    }

    private void release(CoordinationKey key) {
        active.remove(key, Boolean.TRUE);
    }

    static final class Claim implements AutoCloseable {
        private final WriteCoordinationDomain owner;
        private final CoordinationKey key;
        private final AtomicBoolean closed = new AtomicBoolean();
        private volatile RuntimeMutationVersion frozenVersion;

        private Claim(WriteCoordinationDomain owner, CoordinationKey key) {
            this.owner = owner;
            this.key = key;
        }

        void freeze(RuntimeMutationVersion version) {
            Objects.requireNonNull(version, "version");
            synchronized (this) {
                if (frozenVersion != null && !frozenVersion.equals(version)) {
                    throw new IllegalStateException("coordination version already frozen");
                }
                frozenVersion = version;
            }
        }

        RuntimeMutationVersion frozenVersion() {
            return frozenVersion;
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                owner.release(key);
            }
        }
    }

    private static final class CoordinationKey {
        private final RuntimeModelSessionId sessionId;
        private final RuntimeObjectId objectId;
        private final ModelPath path;

        private CoordinationKey(
                RuntimeModelSessionId sessionId,
                RuntimeObjectId objectId,
                ModelPath path) {
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
            this.objectId = Objects.requireNonNull(objectId, "objectId");
            this.path = Objects.requireNonNull(path, "path");
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof CoordinationKey)) return false;
            CoordinationKey that = (CoordinationKey) other;
            return sessionId.equals(that.sessionId)
                    && objectId.equals(that.objectId)
                    && path.equals(that.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sessionId, objectId, path);
        }
    }
}
