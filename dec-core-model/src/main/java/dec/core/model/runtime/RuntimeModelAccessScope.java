package dec.core.model.runtime;

import dec.core.context.data.ModelData;
import dec.core.context.data.NullData;
import dec.core.context.model.ModelPath;
import dec.core.context.model.RuntimeBindingPlan;
import dec.core.context.runtime.ProtectedWriteReceipt;
import dec.core.context.runtime.ResolvedProtectedReadAccess;
import dec.core.context.runtime.ResolvedProtectedWriteAccess;
import dec.core.context.runtime.ResolvedRuntimeTarget;
import dec.core.context.runtime.RuntimeFactValue;
import dec.core.context.runtime.RuntimeModelSessionId;
import dec.core.context.runtime.RuntimeMutationStamp;
import dec.core.context.runtime.RuntimeMutationVersion;
import dec.core.context.runtime.RuntimeObjectId;
import dec.core.model.container.Container;
import dec.core.model.container.ResultInfo;
import dec.core.model.execute.rule.exception.ExecuteRuleException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/** MODEL minted cross-module trusted scope；Session/EffectProvider 只能从这个 Scope 派生。 */
public final class RuntimeModelAccessScope {
    private static final AtomicLong SESSION_SEQUENCE = new AtomicLong();
    private static final AtomicLong OBJECT_SEQUENCE = new AtomicLong();

    private final RuntimeModelFrame frame;
    private final boolean exposeRawOperationPort;
    private final RuntimeModelEffectProvider effectProvider = new ScopeEffectProvider(this);
    private final IdentityHashMap<RuntimeModelHandle, ScopeSession> leases =
            new IdentityHashMap<RuntimeModelHandle, ScopeSession>();
    private final IdentityHashMap<RuntimeModelHandle, Map<ModelPath, MutationCell>> mutationCells =
            new IdentityHashMap<RuntimeModelHandle, Map<ModelPath, MutationCell>>();
    private boolean active = true;

    RuntimeModelAccessScope(RuntimeModelFrame frame) {
        this(frame, true);
    }

    /** Production root passes false so ordinary callers cannot extract a usable raw effect port. */
    RuntimeModelAccessScope(RuntimeModelFrame frame, boolean exposeRawOperationPort) {
        this.frame = Objects.requireNonNull(frame, "frame");
        this.exposeRawOperationPort = exposeRawOperationPort;
    }

    boolean exposeRawOperationPort() {
        return exposeRawOperationPort;
    }

    /** 返回 MODEL 冻结的 trusted frame；frame 中只携带 opaque provenance/handle。 */
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

    /** 仅按对象身份判断 Handle 是否属于当前 frame，禁止 equals/名称替代 trusted identity。 */
    private boolean containsHandleIdentity(RuntimeModelHandle handle) {
        for (RuntimeModelHandle candidate : frame.handles()) {
            if (candidate == handle) {
                return true;
            }
        }
        return false;
    }

    /** 为 actual Handle/path 返回唯一协调单元；同一对象路径的版本和写入都在此串行化。 */
    private synchronized MutationCell mutationCell(
            RuntimeModelHandle handle,
            ModelPath modelPath) {
        Map<ModelPath, MutationCell> byPath = mutationCells.get(handle);
        if (byPath == null) {
            byPath = new LinkedHashMap<ModelPath, MutationCell>();
            mutationCells.put(handle, byPath);
        }
        MutationCell cell = byPath.get(modelPath);
        if (cell == null) {
            cell = new MutationCell();
            byPath.put(modelPath, cell);
        }
        return cell;
    }

    /** 注册 actual Handle 的独占 Session lease，防止跨 Session 同时取得同一 ModelData 所有权。 */
    private synchronized boolean acquireLease(RuntimeModelHandle handle, ScopeSession session) {
        ScopeSession current = leases.get(handle);
        if (current != null && current != session) {
            return false;
        }
        leases.put(handle, session);
        return true;
    }

    /** Session 关闭时只释放自身持有的 lease，禁止误释放其他 Session 的 ownership。 */
    private synchronized void releaseLeases(ScopeSession session) {
        List<RuntimeModelHandle> owned = new ArrayList<RuntimeModelHandle>();
        for (Map.Entry<RuntimeModelHandle, ScopeSession> entry : leases.entrySet()) {
            if (entry.getValue() == session) {
                owned.add(entry.getKey());
            }
        }
        for (RuntimeModelHandle handle : owned) {
            leases.remove(handle);
        }
    }

    /** Session 生命周期与 trusted object table 的具体实现。 */
    private static final class ScopeSession implements RuntimeModelSession {
        private final RuntimeModelAccessScope ownerScope;
        private final RuntimeModelSessionId sessionId;
        private final Map<RuntimeObjectId, RuntimeModelHandle> objects =
                new LinkedHashMap<RuntimeObjectId, RuntimeModelHandle>();
        private final IdentityHashMap<RuntimeModelHandle, RuntimeObjectId> objectIds =
                new IdentityHashMap<RuntimeModelHandle, RuntimeObjectId>();
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

        /** 注册 exact frame Handle；重复、跨 Session ownership 或生命周期错误全部 fail closed。 */
        @Override
        public synchronized RuntimeObjectId register(RuntimeModelHandle handle)
                throws RuntimeModelSessionException {
            Objects.requireNonNull(handle, "handle");
            requireRegisterable();
            if (!ownerScope.containsHandleIdentity(handle)) {
                throw new RuntimeModelSessionException(
                        RuntimeModelSessionFailureCode.OWNERSHIP_CONFLICT);
            }
            if (objectIds.containsKey(handle)) {
                throw new RuntimeModelSessionException(
                        RuntimeModelSessionFailureCode.DUPLICATE_REGISTRATION);
            }
            if (!ownerScope.acquireLease(handle, this)) {
                throw new RuntimeModelSessionException(
                        RuntimeModelSessionFailureCode.OWNERSHIP_CONFLICT);
            }

            RuntimeObjectId objectId = RuntimeObjectId.of(
                    "model-object-" + OBJECT_SEQUENCE.incrementAndGet());
            objects.put(objectId, handle);
            objectIds.put(handle, objectId);
            return objectId;
        }

        /** 一次性冻结 object table；重复 seal 或已关闭 Session 都稳定拒绝。 */
        @Override
        public synchronized void seal() throws RuntimeModelSessionException {
            if (closed) {
                throw new RuntimeModelSessionException(RuntimeModelSessionFailureCode.SESSION_CLOSED);
            }
            if (!ownerScope.active()) {
                throw new RuntimeModelSessionException(RuntimeModelSessionFailureCode.SCOPE_INACTIVE);
            }
            if (sealed) {
                throw new RuntimeModelSessionException(
                        RuntimeModelSessionFailureCode.SESSION_ALREADY_SEALED);
            }
            sealed = true;
        }

        /** exact 定位 session/object/target/binding proof；任一不一致都返回未定位而不做 fallback。 */
        @Override
        public synchronized LocatedRuntimeObject locate(ResolvedRuntimeTarget target) {
            return registeredHandle(target) == null ? null : new LocatedRuntimeObject(target);
        }

        /** 返回同 actual Handle/path 的当前单调版本；无效或 stale target 不泄露版本。 */
        @Override
        public RuntimeMutationVersion currentVersion(
                ResolvedRuntimeTarget target,
                ModelPath modelPath) {
            Objects.requireNonNull(modelPath, "modelPath");
            RuntimeModelHandle handle = registeredHandle(target);
            if (handle == null) {
                return null;
            }
            MutationCell cell = ownerScope.mutationCell(handle, modelPath);
            synchronized (cell) {
                return RuntimeMutationVersion.of(cell.version);
            }
        }

        /** 关闭 Session 并释放其 actual Handle leases；重复 close 保持幂等。 */
        @Override
        public synchronized void close() {
            if (closed) {
                return;
            }
            closed = true;
            ownerScope.releaseLeases(this);
        }

        /** effect port 每次操作都通过此方法重验 sealed/session/object/binding exact identity。 */
        private synchronized RuntimeModelHandle registeredHandle(ResolvedRuntimeTarget target) {
            Objects.requireNonNull(target, "target");
            if (closed || !sealed || !ownerScope.active()) {
                return null;
            }
            if (!sessionId.equals(target.sessionId())) {
                return null;
            }
            RuntimeModelHandle handle = objects.get(target.runtimeObjectId());
            if (handle == null) {
                return null;
            }
            RuntimeBindingPlan plan = handle.provenance().runtimeBindingPlan();
            if (!plan.sourceTargetKey().equals(target.targetKey())) {
                return null;
            }
            if (!plan.equals(target.bindingProof().runtimeBindingPlan())) {
                return null;
            }
            return handle;
        }

        private void requireRegisterable() throws RuntimeModelSessionException {
            if (closed) {
                throw new RuntimeModelSessionException(RuntimeModelSessionFailureCode.SESSION_CLOSED);
            }
            if (!ownerScope.active()) {
                throw new RuntimeModelSessionException(RuntimeModelSessionFailureCode.SCOPE_INACTIVE);
            }
            if (sealed) {
                throw new RuntimeModelSessionException(
                        RuntimeModelSessionFailureCode.SESSION_ALREADY_SEALED);
            }
        }

        private synchronized boolean sealed() {
            return sealed;
        }

        private synchronized boolean closed() {
            return closed;
        }

        private boolean belongsTo(RuntimeModelAccessScope scope) {
            return ownerScope == scope;
        }
    }

    /** EffectProvider 只为同 Scope、已封存且仍有效的 Session 创建私有 operation port。 */
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
            BoundOperationPort port = new BoundOperationPort(ownerScope, session);
            return RuntimeModelEffectBindingResult.bound(
                    port, port, ownerScope.exposeRawOperationPort());
        }
    }

    /**
     * MODEL 私有 effect port：每次调用重新验证 trusted target，并在写入前原子重验 mutation stamp。
     */
    private static final class BoundOperationPort
            implements RuntimeModelOperationPort, RuntimeModelGuardedOperationPort {
        private final RuntimeModelAccessScope ownerScope;
        private final ScopeSession session;

        private BoundOperationPort(
                RuntimeModelAccessScope ownerScope,
                ScopeSession session) {
            this.ownerScope = Objects.requireNonNull(ownerScope, "ownerScope");
            this.session = Objects.requireNonNull(session, "session");
        }

        /** READ 只返回深不可变 RuntimeFactValue；无法精确定位或不支持的 live 类型均 fail closed。 */
        @Override
        public RuntimeFactValue read(ResolvedProtectedReadAccess access) {
            if (!ownerScope.exposeRawOperationPort()) {
                return null;
            }
            return readInternal(access);
        }

        @Override
        public RuntimeFactValue readAuthorized(ResolvedProtectedReadAccess access) {
            return readInternal(access);
        }

        private RuntimeFactValue readInternal(ResolvedProtectedReadAccess access) {
            Objects.requireNonNull(access, "access");
            RuntimeModelHandle handle = session.registeredHandle(access.target());
            if (handle == null) {
                return null;
            }
            PathValue pathValue = PathValue.read(handle.modelData(), access.modelPath());
            if (!pathValue.exists) {
                return null;
            }
            return snapshotValue(pathValue.value);
        }

        /**
         * WRITE 在同 actual Handle/path 协调单元内完成 stamp 重验、真实 Container effect 与版本递增；
         * 失败不创建 receipt，并恢复本次写入前的 ModelData 路径值。
         */
        @Override
        public ProtectedWriteReceipt write(ResolvedProtectedWriteAccess access) {
            if (!ownerScope.exposeRawOperationPort()) {
                return null;
            }
            return writeInternal(access);
        }

        @Override
        public ProtectedWriteReceipt writeAuthorized(ResolvedProtectedWriteAccess access) {
            return writeInternal(access);
        }

        private ProtectedWriteReceipt writeInternal(ResolvedProtectedWriteAccess access) {
            Objects.requireNonNull(access, "access");
            RuntimeModelHandle handle = session.registeredHandle(access.target());
            if (handle == null || !matchesStamp(access)) {
                return null;
            }

            MutationCell cell = ownerScope.mutationCell(handle, access.modelPath());
            synchronized (cell) {
                if (cell.version != access.mutationStamp().version().value()) {
                    return null;
                }
                PathValue pathValue = PathValue.read(handle.modelData(), access.modelPath());
                if (!pathValue.exists) {
                    return null;
                }

                Object replacement;
                try {
                    replacement = RuntimeFactValueCodec.toJavaValue(access.value());
                } catch (IllegalArgumentException invalidValue) {
                    return null;
                }
                Object previous = pathValue.value;
                pathValue.parent.put(pathValue.leaf, replacement);

                if (!executeSuccessfully(handle.container())) {
                    pathValue.parent.put(pathValue.leaf, previous);
                    return null;
                }

                cell.version++;
                return ProtectedWriteReceipt.of(
                        access.target(),
                        access.modelPath(),
                        RuntimeMutationVersion.of(cell.version));
            }
        }

        /** stamp 必须与 resolver 冻结的同一 session/object/path 完全一致。 */
        private boolean matchesStamp(ResolvedProtectedWriteAccess access) {
            RuntimeMutationStamp stamp = access.mutationStamp();
            return stamp.sessionId().equals(access.target().sessionId())
                    && stamp.runtimeObjectId().equals(access.target().runtimeObjectId())
                    && stamp.modelPath().equals(access.modelPath());
        }

        /** 真实 Container 只有 execute 正常返回且 ResultInfo.success 才视为 effect 成功。 */
        private boolean executeSuccessfully(Container container) {
            try {
                Container executed = container.execute();
                ResultInfo result = executed == null ? null : executed.getResult();
                return result != null && result.isSuccess();
            } catch (ExecuteRuleException | RuntimeException failure) {
                return false;
            }
        }
    }

    /** actual Handle/path 的唯一并发协调单元。 */
    private static final class MutationCell {
        private long version;
    }

    /** 精确路径定位结果；parent/leaf 仅在 MODEL 私有写事务内使用。 */
    private static final class PathValue {
        private final boolean exists;
        private final Object value;
        private final Map<String, Object> parent;
        private final String leaf;

        private PathValue(
                boolean exists,
                Object value,
                Map<String, Object> parent,
                String leaf) {
            this.exists = exists;
            this.value = value;
            this.parent = parent;
            this.leaf = leaf;
        }

        /** 按规范 ModelPath 精确遍历 Map；禁止父级、通配或字符串 fallback。 */
        @SuppressWarnings("unchecked")
        private static PathValue read(ModelData modelData, ModelPath modelPath) {
            Objects.requireNonNull(modelData, "modelData");
            Objects.requireNonNull(modelPath, "modelPath");
            Map<String, Object> current = modelData.getValues();
            List<String> segments = modelPath.segments();
            for (int index = 0; index < segments.size() - 1; index++) {
                String segment = segments.get(index);
                if (!current.containsKey(segment)) {
                    return new PathValue(false, null, null, null);
                }
                Object nested = current.get(segment);
                if (!(nested instanceof Map)) {
                    return new PathValue(false, null, null, null);
                }
                current = (Map<String, Object>) nested;
            }
            String leaf = segments.get(segments.size() - 1);
            if (!current.containsKey(leaf)) {
                return new PathValue(false, null, null, null);
            }
            return new PathValue(true, current.get(leaf), current, leaf);
        }
    }

    /** 把真实 ModelData 值深拷贝成封闭 RuntimeFactValue；未知 live Object 不允许跨边界泄漏。 */
    @SuppressWarnings("unchecked")
    private static RuntimeFactValue snapshotValue(Object value) {
        if (value == null || value instanceof NullData) {
            return RuntimeFactValue.nullValue();
        }
        if (value instanceof Boolean) {
            return RuntimeFactValue.boolValue(((Boolean) value).booleanValue());
        }
        if (value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Long) {
            return RuntimeFactValue.integerValue(((Number) value).longValue());
        }
        if (value instanceof BigDecimal) {
            return RuntimeFactValue.decimalValue((BigDecimal) value);
        }
        if (value instanceof Float || value instanceof Double) {
            return RuntimeFactValue.decimalValue(new BigDecimal(String.valueOf(value)));
        }
        if (value instanceof String || value instanceof Character) {
            return RuntimeFactValue.stringValue(String.valueOf(value));
        }
        if (value instanceof Collection) {
            List<RuntimeFactValue> copy = new ArrayList<RuntimeFactValue>();
            for (Object item : (Collection<Object>) value) {
                RuntimeFactValue snapshot = snapshotValue(item);
                if (snapshot == null) {
                    return null;
                }
                copy.add(snapshot);
            }
            return RuntimeFactValue.listValue(copy);
        }
        if (value instanceof Map) {
            Map<String, RuntimeFactValue> copy = new LinkedHashMap<String, RuntimeFactValue>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (!(entry.getKey() instanceof String)) {
                    return null;
                }
                RuntimeFactValue snapshot = snapshotValue(entry.getValue());
                if (snapshot == null) {
                    return null;
                }
                copy.put((String) entry.getKey(), snapshot);
            }
            return RuntimeFactValue.objectValue(copy);
        }
        return null;
    }

    /**
     * RuntimeFactValue 反序列化仅服务 MODEL effect adapter；业务 evaluator 仍不得解析 canonicalForm。
     * Parser 只接受 RuntimeFactValue 自身能够产生的封闭 JSON 子集，遇到额外语法立即拒绝。
     */
    private static final class RuntimeFactValueCodec {
        private RuntimeFactValueCodec() { }

        private static Object toJavaValue(RuntimeFactValue value) {
            Objects.requireNonNull(value, "value");
            Parser parser = new Parser(value.canonicalForm());
            Object result = parser.parseValue();
            parser.requireEnd();
            return result;
        }

        /** 仅实现 canonicalForm 的 null/bool/number/string/list/object 子集。 */
        private static final class Parser {
            private final String text;
            private int index;

            private Parser(String text) {
                this.text = Objects.requireNonNull(text, "text");
            }

            private Object parseValue() {
                if (startsWith("null")) {
                    index += 4;
                    return null;
                }
                if (startsWith("true")) {
                    index += 4;
                    return Boolean.TRUE;
                }
                if (startsWith("false")) {
                    index += 5;
                    return Boolean.FALSE;
                }
                char current = current();
                if (current == '"') {
                    return parseString();
                }
                if (current == '[') {
                    return parseList();
                }
                if (current == '{') {
                    return parseObject();
                }
                return parseNumber();
            }

            private List<Object> parseList() {
                expect('[');
                List<Object> values = new ArrayList<Object>();
                if (peek(']')) {
                    index++;
                    return values;
                }
                while (true) {
                    values.add(parseValue());
                    if (peek(']')) {
                        index++;
                        return values;
                    }
                    expect(',');
                }
            }

            private Map<String, Object> parseObject() {
                expect('{');
                Map<String, Object> values = new LinkedHashMap<String, Object>();
                if (peek('}')) {
                    index++;
                    return values;
                }
                while (true) {
                    String key = parseString();
                    expect(':');
                    values.put(key, parseValue());
                    if (peek('}')) {
                        index++;
                        return values;
                    }
                    expect(',');
                }
            }

            private String parseString() {
                expect('"');
                StringBuilder result = new StringBuilder();
                while (index < text.length()) {
                    char value = text.charAt(index++);
                    if (value == '"') {
                        return result.toString();
                    }
                    if (value == '\\') {
                        if (index >= text.length()) {
                            throw invalid();
                        }
                        char escaped = text.charAt(index++);
                        if (escaped != '\\' && escaped != '"') {
                            throw invalid();
                        }
                        result.append(escaped);
                    } else {
                        result.append(value);
                    }
                }
                throw invalid();
            }

            private Number parseNumber() {
                int start = index;
                if (peek('-')) {
                    index++;
                }
                requireDigit();
                while (index < text.length() && Character.isDigit(text.charAt(index))) {
                    index++;
                }
                boolean decimal = false;
                if (peek('.')) {
                    decimal = true;
                    index++;
                    requireDigit();
                    while (index < text.length() && Character.isDigit(text.charAt(index))) {
                        index++;
                    }
                }
                String number = text.substring(start, index);
                try {
                    return decimal ? new BigDecimal(number) : Long.valueOf(number);
                } catch (NumberFormatException invalid) {
                    throw invalid();
                }
            }

            private void requireDigit() {
                if (index >= text.length() || !Character.isDigit(text.charAt(index))) {
                    throw invalid();
                }
            }

            private void requireEnd() {
                if (index != text.length()) {
                    throw invalid();
                }
            }

            private boolean startsWith(String value) {
                return text.startsWith(value, index);
            }

            private boolean peek(char expected) {
                return index < text.length() && text.charAt(index) == expected;
            }

            private char current() {
                if (index >= text.length()) {
                    throw invalid();
                }
                return text.charAt(index);
            }

            private void expect(char expected) {
                if (!peek(expected)) {
                    throw invalid();
                }
                index++;
            }

            private IllegalArgumentException invalid() {
                return new IllegalArgumentException("invalid RuntimeFactValue canonical form");
            }
        }
    }
}
