package dec.core.compiler.pass;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.RandomAccess;
import java.util.Set;

/**
 * 对跨 Result 边界暴露的 artifact 值执行有预算的不可变图快照。
 */
final class ArtifactSnapshots {
    private static final Limits DEFAULT_LIMITS = new Limits(
            256,
            4096,
            65536,
            16384);

    /** 工具类不允许实例化。 */
    private ArtifactSnapshots() {
    }

    /**
     * 使用生产默认预算冻结 artifact 图，并复用已完成的共享子图快照。
     */
    static Object freeze(Object value) {
        return freeze(value, DEFAULT_LIMITS);
    }

    /**
     * 使用指定内部预算冻结 artifact 图；该入口仅供同包验证边界使用。
     */
    static Object freeze(Object value, Limits limits) {
        return new SnapshotSession(
                Objects.requireNonNull(limits, "limits"))
                .freeze(Objects.requireNonNull(value, "value"));
    }

    /** 判断值是否属于明确不可变的标量集合。 */
    private static boolean isImmutableScalar(Object value) {
        return value instanceof String
                || value instanceof Boolean
                || value instanceof Character
                || value instanceof Byte
                || value instanceof Short
                || value instanceof Integer
                || value instanceof Long
                || value instanceof Float
                || value instanceof Double
                || value instanceof BigInteger
                || value instanceof BigDecimal
                || value instanceof Enum<?>;
    }

    /** 判断值是否属于需要遍历和冻结的受支持容器。 */
    private static boolean isSupportedContainer(Object value) {
        return value instanceof Optional<?>
                || value instanceof List<?>
                || value instanceof Set<?>
                || value instanceof Map<?, ?>;
    }

    /** I004 内部预算，不形成 Compiler 公共 API。 */
    static final class Limits {
        private final int maxDepth;
        private final int maxUniqueContainers;
        private final int maxEdges;
        private final int maxMapEntries;

        /** 创建严格为正的四类 snapshot 预算。 */
        Limits(
                int maxDepth,
                int maxUniqueContainers,
                int maxEdges,
                int maxMapEntries) {
            this.maxDepth = positive(maxDepth, "maxDepth");
            this.maxUniqueContainers = positive(
                    maxUniqueContainers,
                    "maxUniqueContainers");
            this.maxEdges = positive(maxEdges, "maxEdges");
            this.maxMapEntries = positive(maxMapEntries, "maxMapEntries");
        }

        /** 拒绝无效预算，避免 0 或负值产生含糊边界。 */
        private static int positive(int value, String name) {
            if (value <= 0) {
                throw new IllegalArgumentException(name + " must be > 0");
            }
            return value;
        }
    }

    /** 资源预算超限的内部稳定异常类型。 */
    static final class ResourceLimitException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;

        /** 记录超限维度，供 Pipeline 映射为稳定 Diagnostic。 */
        private ResourceLimitException(String dimension) {
            super("artifact snapshot resource limit exceeded: " + dimension);
        }
    }

    /** source identity 当前遍历状态。 */
    private enum VisitState {
        VISITING,
        FROZEN
    }

    /** canonical 结构节点类型，避免不同容器结构被错误合并。 */
    private enum NodeType {
        OPTIONAL_EMPTY,
        OPTIONAL_PRESENT,
        LIST,
        SET,
        MAP
    }

    /** 将冻结结果写入根或父容器的目标。 */
    private interface Assignment {
        void assign(Object value);
    }

    /** 显式遍历栈中的工作项。 */
    private interface Task {
        void execute(SnapshotSession session, Deque<Task> stack);
    }

    /** 单次 freeze 的预算、identity 状态和 canonical 结构索引。 */
    private static final class SnapshotSession {
        private final Limits limits;
        private final IdentityHashMap<Object, VisitState> states =
                new IdentityHashMap<Object, VisitState>();
        private final IdentityHashMap<Object, Object> frozenBySource =
                new IdentityHashMap<Object, Object>();
        private final IdentityHashMap<Object, Integer> structuralIdsByFrozen =
                new IdentityHashMap<Object, Integer>();
        private final IdentityHashMap<Object, Integer> structuralHashesByFrozen =
                new IdentityHashMap<Object, Integer>();
        private final IdentityHashMap<Object, Integer> scalarIdsByIdentity =
                new IdentityHashMap<Object, Integer>();
        private final IdentityHashMap<Object, Integer> scalarHashesByIdentity =
                new IdentityHashMap<Object, Integer>();
        private final Map<ScalarKey, Integer> scalarIds =
                new HashMap<ScalarKey, Integer>();
        private final Map<NodeKey, Integer> nodeIds =
                new HashMap<NodeKey, Integer>();
        private int nextStructuralId = 1;
        private int uniqueContainers;
        private int traversedEdges;
        private int mapEntries;

        private SnapshotSession(Limits limits) {
            this.limits = limits;
        }

        /** 通过显式工作栈冻结根值，避免消耗 JVM 方法递归栈。 */
        private Object freeze(Object root) {
            RootAssignment result = new RootAssignment();
            Deque<Task> stack = new ArrayDeque<Task>();
            stack.push(new FreezeTask(root, 1, result, "value"));
            while (!stack.isEmpty()) {
                stack.pop().execute(this, stack);
            }
            return result.value();
        }

        /** 每次容器引用都检查逻辑嵌套深度。 */
        private void checkDepth(int depth) {
            if (depth > limits.maxDepth) {
                throw new ResourceLimitException("nesting-depth");
            }
        }

        /** 首次进入 source identity 时登记唯一容器预算。 */
        private void enter(Object source) {
            if (uniqueContainers >= limits.maxUniqueContainers) {
                throw new ResourceLimitException("unique-containers");
            }
            uniqueContainers++;
            states.put(source, VisitState.VISITING);
        }

        /** 在读取或入栈下一条容器边之前检查 edge 预算。 */
        private void consumeEdge() {
            if (traversedEdges >= limits.maxEdges) {
                throw new ResourceLimitException("traversed-edges");
            }
            traversedEdges++;
        }

        /** 在读取下一条 Map entry 之前检查 entry 预算。 */
        private void consumeMapEntry() {
            if (mapEntries >= limits.maxMapEntries) {
                throw new ResourceLimitException("map-entries");
            }
            mapEntries++;
        }

        /**
         * 保存完成态快照、canonical ID 与缓存 hash，并写入父容器。
         */
        private void complete(
                Object source,
                Object frozen,
                int structuralId,
                int structuralHash,
                Assignment target) {
            states.put(source, VisitState.FROZEN);
            frozenBySource.put(source, frozen);
            structuralIdsByFrozen.put(frozen, Integer.valueOf(structuralId));
            structuralHashesByFrozen.put(frozen, Integer.valueOf(structuralHash));
            target.assign(frozen);
        }

        /** 返回冻结值的 canonical 结构 ID，标量按 equals 语义归一化。 */
        private int structuralId(Object value) {
            Integer containerId = structuralIdsByFrozen.get(value);
            if (containerId != null) {
                return containerId.intValue();
            }
            Integer identityId = scalarIdsByIdentity.get(value);
            if (identityId != null) {
                return identityId.intValue();
            }
            if (!isImmutableScalar(value)
                    && !(value instanceof ImmutablePipelineArtifact)) {
                throw new IllegalStateException(
                        "frozen container has no structural id");
            }
            int hash = value.hashCode();
            ScalarKey key = new ScalarKey(value, hash);
            Integer existing = scalarIds.get(key);
            int id;
            if (existing == null) {
                id = nextStructuralId++;
                scalarIds.put(key, Integer.valueOf(id));
            } else {
                id = existing.intValue();
            }
            scalarIdsByIdentity.put(value, Integer.valueOf(id));
            scalarHashesByIdentity.put(value, Integer.valueOf(hash));
            return id;
        }

        /** 返回与 Java 容器合同一致、但不会递归展开共享 DAG 的缓存 hash。 */
        private int structuralHash(Object value) {
            Integer containerHash = structuralHashesByFrozen.get(value);
            if (containerHash != null) {
                return containerHash.intValue();
            }
            Integer scalarHash = scalarHashesByIdentity.get(value);
            if (scalarHash != null) {
                return scalarHash.intValue();
            }
            structuralId(value);
            return scalarHashesByIdentity.get(value).intValue();
        }

        /** 对相同 immediate child ID 序列分配同一 canonical 节点 ID。 */
        private int nodeId(NodeType type, int[] parts) {
            NodeKey key = new NodeKey(type, parts);
            Integer existing = nodeIds.get(key);
            if (existing != null) {
                return existing.intValue();
            }
            int id = nextStructuralId++;
            nodeIds.put(key, Integer.valueOf(id));
            return id;
        }
    }

    /** 处理一个值；容器会展开为子任务和完成任务。 */
    private static final class FreezeTask implements Task {
        private final Object source;
        private final int depth;
        private final Assignment target;
        private final String nullMessage;

        private FreezeTask(
                Object source,
                int depth,
                Assignment target,
                String nullMessage) {
            this.source = source;
            this.depth = depth;
            this.target = target;
            this.nullMessage = nullMessage;
        }

        @Override
        public void execute(SnapshotSession session, Deque<Task> stack) {
            Object checked = Objects.requireNonNull(source, nullMessage);
            if (isImmutableScalar(checked)
                    || checked instanceof ImmutablePipelineArtifact) {
                session.structuralId(checked);
                target.assign(checked);
                return;
            }
            if (!isSupportedContainer(checked)) {
                throw new IllegalArgumentException(
                        "artifact value must be immutable or a supported container: "
                                + checked.getClass().getName());
            }

            session.checkDepth(depth);
            VisitState state = session.states.get(checked);
            if (state == VisitState.VISITING) {
                throw new IllegalArgumentException(
                        "artifact graph must not be cyclic");
            }
            if (state == VisitState.FROZEN) {
                target.assign(session.frozenBySource.get(checked));
                return;
            }

            session.enter(checked);
            if (checked instanceof Optional<?>) {
                expandOptional(session, stack, checked, depth, target);
            } else if (checked instanceof List<?>) {
                expandList(session, stack, checked, depth, target);
            } else if (checked instanceof Set<?>) {
                expandSet(session, stack, checked, depth, target);
            } else {
                expandMap(session, stack, checked, depth, target);
            }
        }
    }

    /** 展开 Optional，present value 作为一条受预算约束的边。 */
    private static void expandOptional(
            SnapshotSession session,
            Deque<Task> stack,
            Object source,
            int depth,
            Assignment target) {
        Optional<?> optional = (Optional<?>) source;
        if (!optional.isPresent()) {
            int id = session.nodeId(NodeType.OPTIONAL_EMPTY, new int[0]);
            session.complete(source, Optional.empty(), id, 0, target);
            return;
        }
        session.consumeEdge();
        ValueHolder holder = new ValueHolder();
        stack.push(new FinishOptionalTask(source, holder, target));
        stack.push(new FreezeTask(
                optional.get(),
                depth + 1,
                holder,
                "artifact optional value"));
    }

    /** 展开 List，并在入栈前完成 edge 计数和 null 校验。 */
    private static void expandList(
            SnapshotSession session,
            Deque<Task> stack,
            Object source,
            int depth,
            Assignment target) {
        List<Object> items = new ArrayList<Object>();
        for (Object item : (List<?>) source) {
            session.consumeEdge();
            items.add(Objects.requireNonNull(item, "artifact list item"));
        }
        List<Object> copy = new ArrayList<Object>(items.size());
        stack.push(new FinishListTask(source, copy, target));
        for (int index = items.size() - 1; index >= 0; index--) {
            stack.push(new FreezeTask(
                    items.get(index),
                    depth + 1,
                    new ListAssignment(copy),
                    "artifact list item"));
        }
    }

    /** 展开 Set，collision 使用 canonical ID 判断，不调用递归 hashCode。 */
    private static void expandSet(
            SnapshotSession session,
            Deque<Task> stack,
            Object source,
            int depth,
            Assignment target) {
        List<Object> items = new ArrayList<Object>();
        for (Object item : (Set<?>) source) {
            session.consumeEdge();
            items.add(Objects.requireNonNull(item, "artifact set item"));
        }
        SetBuilder copy = new SetBuilder();
        stack.push(new FinishSetTask(source, copy, target));
        for (int index = items.size() - 1; index >= 0; index--) {
            stack.push(new FreezeTask(
                    items.get(index),
                    depth + 1,
                    new SetAssignment(session, copy),
                    "artifact set item"));
        }
    }

    /** 展开 Map，并分别预算 entry、key edge 和 value edge。 */
    private static void expandMap(
            SnapshotSession session,
            Deque<Task> stack,
            Object source,
            int depth,
            Assignment target) {
        List<SourceMapEntry> entries = new ArrayList<SourceMapEntry>();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) source).entrySet()) {
            session.consumeMapEntry();
            session.consumeEdge();
            Object key = Objects.requireNonNull(
                    entry.getKey(),
                    "artifact map key");
            session.consumeEdge();
            Object value = Objects.requireNonNull(
                    entry.getValue(),
                    "artifact map value");
            entries.add(new SourceMapEntry(key, value));
        }
        MapBuilder copy = new MapBuilder();
        stack.push(new FinishMapTask(source, copy, target));
        for (int index = entries.size() - 1; index >= 0; index--) {
            SourceMapEntry entry = entries.get(index);
            PendingMapEntry pending = new PendingMapEntry(session, copy);
            stack.push(new FreezeTask(
                    entry.value,
                    depth + 1,
                    new MapValueAssignment(pending),
                    "artifact map value"));
            stack.push(new FreezeTask(
                    entry.key,
                    depth + 1,
                    new MapKeyAssignment(pending),
                    "artifact map key"));
        }
    }

    /** 根结果只允许写入一次。 */
    private static final class RootAssignment implements Assignment {
        private Object value;
        private boolean assigned;

        @Override
        public void assign(Object value) {
            if (assigned) {
                throw new IllegalStateException("artifact root assigned twice");
            }
            this.value = value;
            this.assigned = true;
        }

        /** 返回已完成的根快照。 */
        private Object value() {
            if (!assigned) {
                throw new IllegalStateException("artifact root was not assigned");
            }
            return value;
        }
    }

    /** 保存 Optional 的唯一子结果。 */
    private static final class ValueHolder implements Assignment {
        private Object value;
        private boolean assigned;

        @Override
        public void assign(Object value) {
            if (assigned) {
                throw new IllegalStateException("artifact value assigned twice");
            }
            this.value = value;
            this.assigned = true;
        }
    }

    /** 按原迭代顺序追加 List 子结果。 */
    private static final class ListAssignment implements Assignment {
        private final List<Object> target;

        private ListAssignment(List<Object> target) {
            this.target = target;
        }

        @Override
        public void assign(Object value) {
            target.add(value);
        }
    }

    /** Set 构建器同时保存值、canonical ID 和缓存 hash。 */
    private static final class SetBuilder {
        private final List<Object> values = new ArrayList<Object>();
        private final List<Integer> ids = new ArrayList<Integer>();
        private final Set<Integer> uniqueIds = new HashSet<Integer>();
        private int hash;

        /** 写入一个元素并拒绝冻结后 equality collision。 */
        private void add(SnapshotSession session, Object value) {
            int id = session.structuralId(value);
            if (!uniqueIds.add(Integer.valueOf(id))) {
                throw new IllegalArgumentException(
                        "artifact set elements collide after freezing");
            }
            values.add(value);
            ids.add(Integer.valueOf(id));
            hash += session.structuralHash(value);
        }
    }

    /** 将冻结 Set 元素写入无 hash 放大的构建器。 */
    private static final class SetAssignment implements Assignment {
        private final SnapshotSession session;
        private final SetBuilder target;

        private SetAssignment(SnapshotSession session, SetBuilder target) {
            this.session = session;
            this.target = target;
        }

        @Override
        public void assign(Object value) {
            target.add(session, value);
        }
    }

    /** 保存原始 Map entry 的 key/value。 */
    private static final class SourceMapEntry {
        private final Object key;
        private final Object value;

        private SourceMapEntry(Object key, Object value) {
            this.key = key;
            this.value = value;
        }
    }

    /** Map 构建器以 canonical key ID 拒绝 collision，不调用 key.hashCode。 */
    private static final class MapBuilder {
        private final List<FrozenMapEntry> entries =
                new ArrayList<FrozenMapEntry>();
        private final Set<Integer> keyIds = new HashSet<Integer>();
        private int hash;

        /** 原子写入已冻结 key/value。 */
        private void add(
                SnapshotSession session,
                Object key,
                Object value) {
            int keyId = session.structuralId(key);
            if (!keyIds.add(Integer.valueOf(keyId))) {
                throw new IllegalArgumentException(
                        "artifact map keys collide after freezing");
            }
            int valueId = session.structuralId(value);
            int keyHash = session.structuralHash(key);
            int valueHash = session.structuralHash(value);
            entries.add(new FrozenMapEntry(
                    key,
                    value,
                    keyId,
                    valueId));
            hash += keyHash ^ valueHash;
        }
    }

    /** 等待 key/value 均冻结后再原子写入 Map 构建器。 */
    private static final class PendingMapEntry {
        private final SnapshotSession session;
        private final MapBuilder target;
        private Object key;
        private Object value;
        private boolean keyAssigned;
        private boolean valueAssigned;

        private PendingMapEntry(
                SnapshotSession session,
                MapBuilder target) {
            this.session = session;
            this.target = target;
        }

        /** 保存冻结 key。 */
        private void assignKey(Object key) {
            this.key = key;
            this.keyAssigned = true;
            commitIfReady();
        }

        /** 保存冻结 value。 */
        private void assignValue(Object value) {
            this.value = value;
            this.valueAssigned = true;
            commitIfReady();
        }

        /** key/value 均完成后检查 collision 并写入。 */
        private void commitIfReady() {
            if (!keyAssigned || !valueAssigned) {
                return;
            }
            target.add(session, key, value);
        }
    }

    /** 将冻结 key 写入待提交 Map entry。 */
    private static final class MapKeyAssignment implements Assignment {
        private final PendingMapEntry pending;

        private MapKeyAssignment(PendingMapEntry pending) {
            this.pending = pending;
        }

        @Override
        public void assign(Object value) {
            pending.assignKey(value);
        }
    }

    /** 将冻结 value 写入待提交 Map entry。 */
    private static final class MapValueAssignment implements Assignment {
        private final PendingMapEntry pending;

        private MapValueAssignment(PendingMapEntry pending) {
            this.pending = pending;
        }

        @Override
        public void assign(Object value) {
            pending.assignValue(value);
        }
    }

    /** 完成 Optional 并登记 FROZEN identity。 */
    private static final class FinishOptionalTask implements Task {
        private final Object source;
        private final ValueHolder holder;
        private final Assignment target;

        private FinishOptionalTask(
                Object source,
                ValueHolder holder,
                Assignment target) {
            this.source = source;
            this.holder = holder;
            this.target = target;
        }

        @Override
        public void execute(SnapshotSession session, Deque<Task> stack) {
            if (!holder.assigned) {
                throw new IllegalStateException("optional child was not frozen");
            }
            int childId = session.structuralId(holder.value);
            int childHash = session.structuralHash(holder.value);
            int id = session.nodeId(
                    NodeType.OPTIONAL_PRESENT,
                    new int[]{childId});
            session.complete(
                    source,
                    Optional.of(holder.value),
                    id,
                    childHash,
                    target);
        }
    }

    /** 完成 List，缓存标准 List hash 并登记 FROZEN identity。 */
    private static final class FinishListTask implements Task {
        private final Object source;
        private final List<Object> copy;
        private final Assignment target;

        private FinishListTask(
                Object source,
                List<Object> copy,
                Assignment target) {
            this.source = source;
            this.copy = copy;
            this.target = target;
        }

        @Override
        public void execute(SnapshotSession session, Deque<Task> stack) {
            int[] ids = new int[copy.size()];
            int hash = 1;
            for (int index = 0; index < copy.size(); index++) {
                Object value = copy.get(index);
                ids[index] = session.structuralId(value);
                hash = 31 * hash + session.structuralHash(value);
            }
            int id = session.nodeId(NodeType.LIST, ids);
            FrozenList frozen = new FrozenList(copy, hash);
            session.complete(source, frozen, id, hash, target);
        }
    }

    /** 完成 Set，canonical ID 与 hash 均基于 immediate child metadata。 */
    private static final class FinishSetTask implements Task {
        private final Object source;
        private final SetBuilder copy;
        private final Assignment target;

        private FinishSetTask(
                Object source,
                SetBuilder copy,
                Assignment target) {
            this.source = source;
            this.copy = copy;
            this.target = target;
        }

        @Override
        public void execute(SnapshotSession session, Deque<Task> stack) {
            int[] ids = new int[copy.ids.size()];
            for (int index = 0; index < ids.length; index++) {
                ids[index] = copy.ids.get(index).intValue();
            }
            Arrays.sort(ids);
            int id = session.nodeId(NodeType.SET, ids);
            FrozenSet frozen = new FrozenSet(copy.values, copy.hash);
            session.complete(source, frozen, id, copy.hash, target);
        }
    }

    /** 完成 Map，entry canonical 顺序与原 Map 迭代顺序解耦。 */
    private static final class FinishMapTask implements Task {
        private final Object source;
        private final MapBuilder copy;
        private final Assignment target;

        private FinishMapTask(
                Object source,
                MapBuilder copy,
                Assignment target) {
            this.source = source;
            this.copy = copy;
            this.target = target;
        }

        @Override
        public void execute(SnapshotSession session, Deque<Task> stack) {
            List<int[]> pairs = new ArrayList<int[]>(copy.entries.size());
            for (FrozenMapEntry entry : copy.entries) {
                pairs.add(new int[]{entry.keyId, entry.valueId});
            }
            Collections.sort(pairs, new Comparator<int[]>() {
                @Override
                public int compare(int[] left, int[] right) {
                    int keyComparison = Integer.compare(left[0], right[0]);
                    return keyComparison != 0
                            ? keyComparison
                            : Integer.compare(left[1], right[1]);
                }
            });
            int[] parts = new int[pairs.size() * 2];
            for (int index = 0; index < pairs.size(); index++) {
                parts[index * 2] = pairs.get(index)[0];
                parts[index * 2 + 1] = pairs.get(index)[1];
            }
            int id = session.nodeId(NodeType.MAP, parts);
            FrozenMap frozen = new FrozenMap(copy.entries, copy.hash);
            session.complete(source, frozen, id, copy.hash, target);
        }
    }

    /** 标量 equals/hash 的稳定 key；hash 只计算一次。 */
    private static final class ScalarKey {
        private final Object value;
        private final int hash;

        private ScalarKey(Object value, int hash) {
            this.value = value;
            this.hash = hash;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ScalarKey)) {
                return false;
            }
            ScalarKey that = (ScalarKey) other;
            return Objects.equals(value, that.value);
        }
    }

    /** immediate child ID 组成的 canonical 容器结构 key。 */
    private static final class NodeKey {
        private final NodeType type;
        private final int[] parts;
        private final int hash;

        private NodeKey(NodeType type, int[] parts) {
            this.type = Objects.requireNonNull(type, "type");
            this.parts = Arrays.copyOf(parts, parts.length);
            this.hash = 31 * type.hashCode() + Arrays.hashCode(this.parts);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof NodeKey)) {
                return false;
            }
            NodeKey that = (NodeKey) other;
            return type == that.type && Arrays.equals(parts, that.parts);
        }
    }

    /** 缓存 hash 的不可变 List，避免共享 DAG hash 递归展开。 */
    private static final class FrozenList extends AbstractList<Object>
            implements RandomAccess {
        private final List<Object> values;
        private final int hash;

        private FrozenList(List<Object> values, int hash) {
            this.values = Collections.unmodifiableList(
                    new ArrayList<Object>(values));
            this.hash = hash;
        }

        @Override
        public Object get(int index) {
            return values.get(index);
        }

        @Override
        public int size() {
            return values.size();
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    /** 缓存 hash、保持迭代顺序的不可变 Set。 */
    private static final class FrozenSet extends AbstractSet<Object> {
        private final List<Object> values;
        private final int hash;

        private FrozenSet(List<Object> values, int hash) {
            this.values = Collections.unmodifiableList(
                    new ArrayList<Object>(values));
            this.hash = hash;
        }

        @Override
        public Iterator<Object> iterator() {
            return values.iterator();
        }

        @Override
        public int size() {
            return values.size();
        }

        @Override
        public boolean contains(Object value) {
            return values.contains(value);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    /** 已冻结 Map entry 及其 canonical key/value ID。 */
    private static final class FrozenMapEntry {
        private final Object key;
        private final Object value;
        private final int keyId;
        private final int valueId;

        private FrozenMapEntry(
                Object key,
                Object value,
                int keyId,
                int valueId) {
            this.key = key;
            this.value = value;
            this.keyId = keyId;
            this.valueId = valueId;
        }
    }

    /** 缓存 hash、保持 entry 顺序的不可变 Map。 */
    private static final class FrozenMap extends AbstractMap<Object, Object> {
        private final List<Map.Entry<Object, Object>> entries;
        private final Set<Map.Entry<Object, Object>> entrySet;
        private final int hash;

        private FrozenMap(List<FrozenMapEntry> values, int hash) {
            List<Map.Entry<Object, Object>> copy =
                    new ArrayList<Map.Entry<Object, Object>>(values.size());
            for (FrozenMapEntry value : values) {
                copy.add(new SimpleImmutableEntry<Object, Object>(
                        value.key,
                        value.value));
            }
            this.entries = Collections.unmodifiableList(copy);
            this.entrySet = new FrozenEntrySet(entries, hash);
            this.hash = hash;
        }

        @Override
        public Set<Map.Entry<Object, Object>> entrySet() {
            return entrySet;
        }

        @Override
        public Object get(Object key) {
            for (Map.Entry<Object, Object> entry : entries) {
                if (Objects.equals(entry.getKey(), key)) {
                    return entry.getValue();
                }
            }
            return null;
        }

        @Override
        public boolean containsKey(Object key) {
            for (Map.Entry<Object, Object> entry : entries) {
                if (Objects.equals(entry.getKey(), key)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int size() {
            return entries.size();
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    /** List-backed 不可变 entrySet，构造期间不触发 key.hashCode。 */
    private static final class FrozenEntrySet
            extends AbstractSet<Map.Entry<Object, Object>> {
        private final List<Map.Entry<Object, Object>> entries;
        private final int hash;

        private FrozenEntrySet(
                List<Map.Entry<Object, Object>> entries,
                int hash) {
            this.entries = entries;
            this.hash = hash;
        }

        @Override
        public Iterator<Map.Entry<Object, Object>> iterator() {
            return entries.iterator();
        }

        @Override
        public int size() {
            return entries.size();
        }

        @Override
        public boolean contains(Object value) {
            return entries.contains(value);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
