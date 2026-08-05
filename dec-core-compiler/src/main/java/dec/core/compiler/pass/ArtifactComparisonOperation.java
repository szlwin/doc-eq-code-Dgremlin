package dec.core.compiler.pass;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 实现单次公开 equality/query 的 operation-level 非递归、受预算图比较。
 */
final class ArtifactComparisonOperation {

    /** 工具类不允许实例化。 */
    private ArtifactComparisonOperation() {
    }

    /** 使用单次 operation 的共享 pair/canonical cache 精确比较两个值。 */
    static boolean equalsValues(
            Object left,
            Object right,
            ArtifactSnapshots.ComparisonLimits limits) {
        return new ComparisonOperation(limits).equalsValues(left, right);
    }

    /**
     * 在一个 operation 内查询 List；全部候选共享 pair 结果、canonical metadata 和预算。
     */
    static int indexOf(
            List<?> values,
            Object query,
            boolean reverse,
            ArtifactSnapshots.ComparisonLimits limits) {
        ComparisonOperation operation = new ComparisonOperation(limits);
        if (reverse) {
            ListIterator<?> iterator = values.listIterator(values.size());
            int index = values.size() - 1;
            while (iterator.hasPrevious()) {
                if (operation.equalsValues(iterator.previous(), query)) {
                    return index;
                }
                index--;
            }
            return -1;
        }
        Iterator<?> iterator = values.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            if (operation.equalsValues(iterator.next(), query)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /** 在一个 operation 内查询 Set element。 */
    static boolean containsElement(
            List<?> values,
            Object query,
            ArtifactSnapshots.ComparisonLimits limits) {
        return indexOf(values, query, false, limits) >= 0;
    }

    /** 在一个 operation 内查找 Map key，返回匹配 entry 的索引。 */
    static int findEntryByKey(
            List<? extends Map.Entry<?, ?>> entries,
            Object query,
            ArtifactSnapshots.ComparisonLimits limits) {
        ComparisonOperation operation = new ComparisonOperation(limits);
        int index = 0;
        for (Map.Entry<?, ?> entry : entries) {
            if (operation.equalsValues(entry.getKey(), query)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /** 在一个 operation 内查询 Map value。 */
    static boolean containsValue(
            List<? extends Map.Entry<?, ?>> entries,
            Object query,
            ArtifactSnapshots.ComparisonLimits limits) {
        ComparisonOperation operation = new ComparisonOperation(limits);
        for (Map.Entry<?, ?> entry : entries) {
            if (operation.equalsValues(entry.getValue(), query)) {
                return true;
            }
        }
        return false;
    }

    /** 在一个 operation 内查询 entrySet，key/value 均使用受控比较。 */
    static boolean containsEntry(
            List<? extends Map.Entry<?, ?>> entries,
            Object query,
            ArtifactSnapshots.ComparisonLimits limits) {
        if (!(query instanceof Map.Entry<?, ?>)) {
            return false;
        }
        ComparisonOperation operation = new ComparisonOperation(limits);
        for (Map.Entry<?, ?> entry : entries) {
            if (operation.equalsValues(entry, query)) {
                return true;
            }
        }
        return false;
    }

    /** 单次公开操作共享的比较预算。 */
    private static final class ComparisonBudget {
        private final ArtifactSnapshots.ComparisonLimits limits;
        private int visitedPairs;
        private int traversedEdges;
        private int canonicalNodes;

        private ComparisonBudget(ArtifactSnapshots.ComparisonLimits limits) {
            this.limits = Objects.requireNonNull(limits, "limits");
        }

        /** 新 identity pair 登记前消耗 pair 预算。 */
        private void consumePair() {
            if (visitedPairs >= limits.maxPairs) {
                throw new ArtifactSnapshots.ComparisonLimitException(
                        "visited-pairs");
            }
            visitedPairs++;
        }

        /** 外部元素读取、保存或子任务压栈前消耗 edge 预算。 */
        private void consumeEdge() {
            if (traversedEdges >= limits.maxEdges) {
                throw new ArtifactSnapshots.ComparisonLimitException(
                        "traversed-edges");
            }
            traversedEdges++;
        }

        /** 首次建立容器 canonical 临时节点前消耗 node 预算。 */
        private void consumeCanonicalNode() {
            if (canonicalNodes >= limits.maxCanonicalNodes) {
                throw new ArtifactSnapshots.ComparisonLimitException(
                        "canonical-nodes");
            }
            canonicalNodes++;
        }

        /** 每次处理 pair/value 时检查逻辑深度。 */
        private void checkDepth(int depth) {
            if (depth > limits.maxDepth) {
                throw new ArtifactSnapshots.ComparisonLimitException(
                        "comparison-depth");
            }
        }
    }

    /** pair 的 operation-level 完成状态。 */
    private enum PairState {
        VISITING,
        EQUAL,
        NOT_EQUAL
    }

    /** 一个 identity pair 的共享比较记录。 */
    private static final class PairRecord {
        private PairState state = PairState.VISITING;
    }

    /**
     * 单次公开 equality/query 的共享状态；候选根结论独立，但子 pair 和 canonical metadata 复用。
     */
    private static final class ComparisonOperation {
        private final ComparisonBudget budget;
        private final IdentityHashMap<Object, IdentityHashMap<Object, PairRecord>>
                pairs =
                new IdentityHashMap<Object, IdentityHashMap<Object, PairRecord>>();
        private final CanonicalSession canonical;

        private ComparisonOperation(
                ArtifactSnapshots.ComparisonLimits limits) {
            this.budget = new ComparisonBudget(
                    Objects.requireNonNull(limits, "limits"));
            this.canonical = new CanonicalSession(budget);
        }

        /** 对一个候选根执行比较，并复用本 operation 已完成的 pair。 */
        private boolean equalsValues(Object left, Object right) {
            BooleanRoot result = new BooleanRoot();
            Deque<ComparisonTask> stack = new ArrayDeque<ComparisonTask>();
            stack.push(new CompareTask(left, right, 1, result));
            while (!stack.isEmpty()) {
                stack.pop().execute(this, stack);
            }
            return result.value();
        }

        /** 查找已存在的 pair 记录。 */
        private PairRecord findPair(Object left, Object right) {
            IdentityHashMap<Object, PairRecord> rights = pairs.get(left);
            return rights == null ? null : rights.get(right);
        }

        /** 新建 pair，并同时登记反向 identity pair 以复用对称比较事实。 */
        private PairRecord startPair(Object left, Object right) {
            budget.consumePair();
            PairRecord record = new PairRecord();
            putPair(left, right, record);
            if (left != right) {
                putPair(right, left, record);
            }
            return record;
        }

        /** 在 nested IdentityHashMap 中写入 pair。 */
        private void putPair(Object left, Object right, PairRecord record) {
            IdentityHashMap<Object, PairRecord> rights = pairs.get(left);
            if (rights == null) {
                rights = new IdentityHashMap<Object, PairRecord>();
                pairs.put(left, rights);
            }
            rights.put(right, record);
        }

        /** 完成 pair 并向当前根聚合器返回结果。 */
        private void complete(
                PairRecord record,
                boolean equal,
                BooleanAssignment target) {
            record.state = equal ? PairState.EQUAL : PairState.NOT_EQUAL;
            target.assign(equal);
        }
    }

    /** 显式比较栈工作项。 */
    private interface ComparisonTask {
        void execute(
                ComparisonOperation operation,
                Deque<ComparisonTask> stack);
    }

    /** 将子比较结果写入根或父聚合器。 */
    private interface BooleanAssignment {
        void assign(boolean value);
    }

    /** 单次候选根结果只允许写入一次。 */
    private static final class BooleanRoot implements BooleanAssignment {
        private boolean value;
        private boolean assigned;

        @Override
        public void assign(boolean value) {
            if (assigned) {
                throw new IllegalStateException(
                        "comparison root assigned twice");
            }
            this.value = value;
            this.assigned = true;
        }

        /** 返回已完成的候选根结论。 */
        private boolean value() {
            if (!assigned) {
                throw new IllegalStateException(
                        "comparison root was not assigned");
            }
            return value;
        }
    }

    /** 一个父 pair 的子结果聚合器。 */
    private static final class PairAggregate {
        private boolean equal = true;
    }

    /** 子 pair 失败时将父聚合器标记为不相等。 */
    private static final class AggregateAssignment
            implements BooleanAssignment {
        private final PairAggregate aggregate;

        private AggregateAssignment(PairAggregate aggregate) {
            this.aggregate = aggregate;
        }

        @Override
        public void assign(boolean value) {
            if (!value) {
                aggregate.equal = false;
            }
        }
    }

    /** 所有必要子 pair 完成后提交父 pair 状态。 */
    private static final class FinishPairTask
            implements ComparisonTask {
        private final PairRecord record;
        private final PairAggregate aggregate;
        private final BooleanAssignment target;

        private FinishPairTask(
                PairRecord record,
                PairAggregate aggregate,
                BooleanAssignment target) {
            this.record = record;
            this.aggregate = aggregate;
            this.target = target;
        }

        @Override
        public void execute(
                ComparisonOperation operation,
                Deque<ComparisonTask> stack) {
            operation.complete(record, aggregate.equal, target);
        }
    }

    /** 比较一个 identity pair，并按容器类型调度非递归子任务。 */
    private static final class CompareTask implements ComparisonTask {
        private final Object left;
        private final Object right;
        private final int depth;
        private final BooleanAssignment target;

        private CompareTask(
                Object left,
                Object right,
                int depth,
                BooleanAssignment target) {
            this.left = left;
            this.right = right;
            this.depth = depth;
            this.target = target;
        }

        @Override
        public void execute(
                ComparisonOperation operation,
                Deque<ComparisonTask> stack) {
            operation.budget.checkDepth(depth);
            PairRecord existing = operation.findPair(left, right);
            if (existing != null) {
                if (existing.state == PairState.VISITING) {
                    throw new IllegalArgumentException(
                            "artifact comparison graph must not be cyclic");
                }
                target.assign(existing.state == PairState.EQUAL);
                return;
            }

            PairRecord record = operation.startPair(left, right);
            if (left == right) {
                operation.complete(record, true, target);
                return;
            }
            if (left == null || right == null) {
                operation.complete(record, false, target);
                return;
            }
            if (left instanceof ArtifactComparisonSupport.CachedHash
                    && right instanceof ArtifactComparisonSupport.CachedHash
                    && ((ArtifactComparisonSupport.CachedHash) left).cachedHash()
                    != ((ArtifactComparisonSupport.CachedHash) right).cachedHash()) {
                operation.complete(record, false, target);
                return;
            }

            if (left instanceof Optional<?> || right instanceof Optional<?>) {
                compareOptional(operation, stack, record);
                return;
            }
            if (left instanceof List<?> || right instanceof List<?>) {
                compareList(operation, stack, record);
                return;
            }
            if (left instanceof Set<?> || right instanceof Set<?>) {
                compareSet(operation, record);
                return;
            }
            if (left instanceof Map<?, ?> || right instanceof Map<?, ?>) {
                compareMap(operation, record);
                return;
            }
            if (left instanceof Map.Entry<?, ?>
                    || right instanceof Map.Entry<?, ?>) {
                compareEntry(operation, stack, record);
                return;
            }
            operation.complete(record, Objects.equals(left, right), target);
        }

        /** Optional 只在 presence 一致时调度唯一 child pair。 */
        private void compareOptional(
                ComparisonOperation operation,
                Deque<ComparisonTask> stack,
                PairRecord record) {
            if (!(left instanceof Optional<?>)
                    || !(right instanceof Optional<?>)) {
                operation.complete(record, false, target);
                return;
            }
            Optional<?> leftOptional = (Optional<?>) left;
            Optional<?> rightOptional = (Optional<?>) right;
            if (leftOptional.isPresent() != rightOptional.isPresent()) {
                operation.complete(record, false, target);
                return;
            }
            if (!leftOptional.isPresent()) {
                operation.complete(record, true, target);
                return;
            }
            PairAggregate aggregate = new PairAggregate();
            stack.push(new FinishPairTask(record, aggregate, target));
            operation.budget.consumeEdge();
            stack.push(new CompareTask(
                    leftOptional.get(),
                    rightOptional.get(),
                    depth + 1,
                    new AggregateAssignment(aggregate)));
        }

        /** List 通过 iterator continuation 顺序比较，不调用外部 size/get。 */
        private void compareList(
                ComparisonOperation operation,
                Deque<ComparisonTask> stack,
                PairRecord record) {
            if (!(left instanceof List<?>) || !(right instanceof List<?>)) {
                operation.complete(record, false, target);
                return;
            }
            PairAggregate aggregate = new PairAggregate();
            stack.push(new FinishPairTask(record, aggregate, target));
            stack.push(new IterateListPairTask(
                    ((List<?>) left).iterator(),
                    ((List<?>) right).iterator(),
                    depth + 1,
                    aggregate));
        }

        /** Set 使用同一 operation 的增量 canonical metadata 无序比较。 */
        private void compareSet(
                ComparisonOperation operation,
                PairRecord record) {
            if (!(left instanceof Set<?>) || !(right instanceof Set<?>)) {
                operation.complete(record, false, target);
                return;
            }
            int leftId = operation.canonical.canonicalId(left, depth);
            int rightId = operation.canonical.canonicalId(right, depth);
            operation.complete(record, leftId == rightId, target);
        }

        /** Map 使用同一 operation 的增量 canonical key/value metadata 比较。 */
        private void compareMap(
                ComparisonOperation operation,
                PairRecord record) {
            if (!(left instanceof Map<?, ?>) || !(right instanceof Map<?, ?>)) {
                operation.complete(record, false, target);
                return;
            }
            int leftId = operation.canonical.canonicalId(left, depth);
            int rightId = operation.canonical.canonicalId(right, depth);
            operation.complete(record, leftId == rightId, target);
        }

        /** Entry 的 key 通过后才读取并比较 value。 */
        private void compareEntry(
                ComparisonOperation operation,
                Deque<ComparisonTask> stack,
                PairRecord record) {
            if (!(left instanceof Map.Entry<?, ?>)
                    || !(right instanceof Map.Entry<?, ?>)) {
                operation.complete(record, false, target);
                return;
            }
            Map.Entry<?, ?> leftEntry = (Map.Entry<?, ?>) left;
            Map.Entry<?, ?> rightEntry = (Map.Entry<?, ?>) right;
            PairAggregate aggregate = new PairAggregate();
            stack.push(new FinishPairTask(record, aggregate, target));
            operation.budget.consumeEdge();
            stack.push(new EntryValueContinuationTask(
                    aggregate,
                    leftEntry,
                    rightEntry,
                    depth + 1));
            stack.push(new CompareTask(
                    leftEntry.getKey(),
                    rightEntry.getKey(),
                    depth + 1,
                    new AggregateAssignment(aggregate)));
        }
    }

    /** List iterator 每次只读取一个 child pair，再继续下一项。 */
    private static final class IterateListPairTask
            implements ComparisonTask {
        private final Iterator<?> left;
        private final Iterator<?> right;
        private final int childDepth;
        private final PairAggregate aggregate;

        private IterateListPairTask(
                Iterator<?> left,
                Iterator<?> right,
                int childDepth,
                PairAggregate aggregate) {
            this.left = left;
            this.right = right;
            this.childDepth = childDepth;
            this.aggregate = aggregate;
        }

        @Override
        public void execute(
                ComparisonOperation operation,
                Deque<ComparisonTask> stack) {
            if (!aggregate.equal) {
                return;
            }
            boolean leftNext = left.hasNext();
            boolean rightNext = right.hasNext();
            if (leftNext != rightNext) {
                aggregate.equal = false;
                return;
            }
            if (!leftNext) {
                return;
            }
            operation.budget.consumeEdge();
            Object leftValue = left.next();
            Object rightValue = right.next();
            stack.push(this);
            stack.push(new CompareTask(
                    leftValue,
                    rightValue,
                    childDepth,
                    new AggregateAssignment(aggregate)));
        }
    }

    /** key 相等后才读取 Entry value，避免无效路径额外工作。 */
    private static final class EntryValueContinuationTask
            implements ComparisonTask {
        private final PairAggregate aggregate;
        private final Map.Entry<?, ?> left;
        private final Map.Entry<?, ?> right;
        private final int depth;

        private EntryValueContinuationTask(
                PairAggregate aggregate,
                Map.Entry<?, ?> left,
                Map.Entry<?, ?> right,
                int depth) {
            this.aggregate = aggregate;
            this.left = left;
            this.right = right;
            this.depth = depth;
        }

        @Override
        public void execute(
                ComparisonOperation operation,
                Deque<ComparisonTask> stack) {
            if (!aggregate.equal) {
                return;
            }
            operation.budget.consumeEdge();
            stack.push(new CompareTask(
                    left.getValue(),
                    right.getValue(),
                    depth,
                    new AggregateAssignment(aggregate)));
        }
    }

    /** canonicalization 当前 identity 状态。 */
    private enum CanonicalState {
        VISITING,
        FROZEN
    }

    /** comparison canonical 节点类型。 */
    private enum CanonicalType {
        OPTIONAL_EMPTY,
        OPTIONAL_PRESENT,
        LIST,
        SET,
        MAP,
        ENTRY
    }

    /** 将 canonical ID 写入根或父节点。 */
    private interface IdAssignment {
        void assign(int id);
    }

    /** 显式 canonical traversal 工作项。 */
    private interface CanonicalTask {
        void execute(CanonicalSession session, Deque<CanonicalTask> stack);
    }

    /** 单次 operation 共享的 canonical intern table。 */
    private static final class CanonicalSession {
        private final ComparisonBudget budget;
        private final IdentityHashMap<Object, CanonicalState> states =
                new IdentityHashMap<Object, CanonicalState>();
        private final IdentityHashMap<Object, Integer> idsByIdentity =
                new IdentityHashMap<Object, Integer>();
        private final IdentityHashMap<Object, Integer> scalarIdsByIdentity =
                new IdentityHashMap<Object, Integer>();
        private final Map<ScalarKey, Integer> scalarIds =
                new HashMap<ScalarKey, Integer>();
        private final Map<CanonicalNodeKey, Integer> nodeIds =
                new HashMap<CanonicalNodeKey, Integer>();
        private int nextId = 1;

        private CanonicalSession(ComparisonBudget budget) {
            this.budget = budget;
        }

        /** 使用显式栈增量生成 canonical ID，不整体复制外部容器。 */
        private int canonicalId(Object root, int depth) {
            budget.checkDepth(depth);
            Integer cached = cachedId(root);
            if (cached != null) {
                return cached.intValue();
            }
            IdRoot result = new IdRoot();
            Deque<CanonicalTask> stack = new ArrayDeque<CanonicalTask>();
            stack.push(new CanonicalizeTask(root, depth, result));
            while (!stack.isEmpty()) {
                stack.pop().execute(this, stack);
            }
            return result.value();
        }

        /** 返回 container 或 scalar identity 已缓存的 canonical ID。 */
        private Integer cachedId(Object value) {
            Integer container = idsByIdentity.get(value);
            return container != null
                    ? container
                    : scalarIdsByIdentity.get(value);
        }

        /** 标量按 equals/hash 语义在 operation 内归一化。 */
        private int scalarId(Object value) {
            Integer identity = scalarIdsByIdentity.get(value);
            if (identity != null) {
                return identity.intValue();
            }
            ScalarKey key = new ScalarKey(value);
            Integer existing = scalarIds.get(key);
            int id;
            if (existing == null) {
                id = nextId++;
                scalarIds.put(key, Integer.valueOf(id));
            } else {
                id = existing.intValue();
            }
            scalarIdsByIdentity.put(value, Integer.valueOf(id));
            return id;
        }

        /** 首次进入 container identity 前先消耗 canonical-node 预算。 */
        private void enter(Object value) {
            budget.consumeCanonicalNode();
            states.put(value, CanonicalState.VISITING);
        }

        /** 完成 container canonical ID 并写入父节点。 */
        private void complete(
                Object source,
                int id,
                IdAssignment target) {
            states.put(source, CanonicalState.FROZEN);
            idsByIdentity.put(source, Integer.valueOf(id));
            target.assign(id);
        }

        /** 对 immediate child ID 序列分配稳定 canonical ID。 */
        private int nodeId(CanonicalType type, int[] parts) {
            CanonicalNodeKey key = new CanonicalNodeKey(type, parts);
            Integer existing = nodeIds.get(key);
            if (existing != null) {
                return existing.intValue();
            }
            int id = nextId++;
            nodeIds.put(key, Integer.valueOf(id));
            return id;
        }
    }

    /** 处理一个 canonical value；容器通过 iterator task 增量展开。 */
    private static final class CanonicalizeTask implements CanonicalTask {
        private final Object value;
        private final int depth;
        private final IdAssignment target;

        private CanonicalizeTask(
                Object value,
                int depth,
                IdAssignment target) {
            this.value = value;
            this.depth = depth;
            this.target = target;
        }

        @Override
        public void execute(
                CanonicalSession session,
                Deque<CanonicalTask> stack) {
            session.budget.checkDepth(depth);
            if (!isContainer(value)) {
                target.assign(session.scalarId(value));
                return;
            }
            CanonicalState state = session.states.get(value);
            if (state == CanonicalState.VISITING) {
                throw new IllegalArgumentException(
                        "artifact comparison graph must not be cyclic");
            }
            if (state == CanonicalState.FROZEN) {
                target.assign(session.idsByIdentity.get(value).intValue());
                return;
            }

            session.enter(value);
            if (value instanceof Optional<?>) {
                expandOptional(session, stack);
            } else if (value instanceof List<?>) {
                expandSequence(
                        session,
                        stack,
                        ((List<?>) value).iterator(),
                        CanonicalType.LIST,
                        false);
            } else if (value instanceof Set<?>) {
                expandSequence(
                        session,
                        stack,
                        ((Set<?>) value).iterator(),
                        CanonicalType.SET,
                        true);
            } else if (value instanceof Map<?, ?>) {
                expandMap(session, stack);
            } else {
                expandEntry(session, stack);
            }
        }

        /** 判断值是否由 canonicalizer 显式遍历。 */
        private static boolean isContainer(Object value) {
            return value instanceof Optional<?>
                    || value instanceof List<?>
                    || value instanceof Set<?>
                    || value instanceof Map<?, ?>
                    || value instanceof Map.Entry<?, ?>;
        }

        /** Optional 在读取 present value 前先扣 edge 预算。 */
        private void expandOptional(
                CanonicalSession session,
                Deque<CanonicalTask> stack) {
            Optional<?> optional = (Optional<?>) value;
            if (!optional.isPresent()) {
                session.complete(
                        value,
                        session.nodeId(
                                CanonicalType.OPTIONAL_EMPTY,
                                new int[0]),
                        target);
                return;
            }
            session.budget.consumeEdge();
            IdHolder child = new IdHolder();
            stack.push(new FinishOptionalTask(value, child, target));
            stack.push(new CanonicalizeTask(
                    optional.get(),
                    depth + 1,
                    child));
        }

        /** List/Set 使用 iterator continuation，不读取 size 或整体复制。 */
        private void expandSequence(
                CanonicalSession session,
                Deque<CanonicalTask> stack,
                Iterator<?> iterator,
                CanonicalType type,
                boolean unordered) {
            List<Integer> ids = new ArrayList<Integer>();
            stack.push(new FinishSequenceTask(
                    value,
                    ids,
                    type,
                    unordered,
                    target));
            stack.push(new IterateCanonicalValuesTask(
                    iterator,
                    depth + 1,
                    ids));
        }

        /** Map 通过 entry iterator 增量展开，不复制 entrySet。 */
        private void expandMap(
                CanonicalSession session,
                Deque<CanonicalTask> stack) {
            List<IdPair> pairs = new ArrayList<IdPair>();
            stack.push(new FinishPairsTask(
                    value,
                    pairs,
                    CanonicalType.MAP,
                    true,
                    target));
            stack.push(new IterateCanonicalMapTask(
                    ((Map<?, ?>) value).entrySet().iterator(),
                    depth + 1,
                    pairs));
        }

        /** 单个 Entry 的 key/value 也按预算顺序增量读取。 */
        private void expandEntry(
                CanonicalSession session,
                Deque<CanonicalTask> stack) {
            IdPair pair = new IdPair();
            List<IdPair> pairs = new ArrayList<IdPair>();
            pairs.add(pair);
            stack.push(new FinishPairsTask(
                    value,
                    pairs,
                    CanonicalType.ENTRY,
                    false,
                    target));
            stack.push(new CanonicalizeEntryTask(
                    (Map.Entry<?, ?>) value,
                    depth + 1,
                    pair));
        }
    }

    /** 每次只读取并保存一个 List/Set child。 */
    private static final class IterateCanonicalValuesTask
            implements CanonicalTask {
        private final Iterator<?> iterator;
        private final int childDepth;
        private final List<Integer> ids;

        private IterateCanonicalValuesTask(
                Iterator<?> iterator,
                int childDepth,
                List<Integer> ids) {
            this.iterator = iterator;
            this.childDepth = childDepth;
            this.ids = ids;
        }

        @Override
        public void execute(
                CanonicalSession session,
                Deque<CanonicalTask> stack) {
            if (!iterator.hasNext()) {
                return;
            }
            session.budget.consumeEdge();
            Object child = iterator.next();
            stack.push(this);
            stack.push(new CanonicalizeTask(
                    child,
                    childDepth,
                    new IdListAssignment(ids)));
        }
    }

    /** 每次只读取并保存一个 Map entry 的 key/value metadata。 */
    private static final class IterateCanonicalMapTask
            implements CanonicalTask {
        private final Iterator<? extends Map.Entry<?, ?>> iterator;
        private final int childDepth;
        private final List<IdPair> pairs;

        private IterateCanonicalMapTask(
                Iterator<? extends Map.Entry<?, ?>> iterator,
                int childDepth,
                List<IdPair> pairs) {
            this.iterator = iterator;
            this.childDepth = childDepth;
            this.pairs = pairs;
        }

        @Override
        public void execute(
                CanonicalSession session,
                Deque<CanonicalTask> stack) {
            if (!iterator.hasNext()) {
                return;
            }
            session.budget.consumeEdge();
            Map.Entry<?, ?> entry = iterator.next();
            Object key = entry.getKey();
            session.budget.consumeEdge();
            Object value = entry.getValue();
            IdPair pair = new IdPair();
            pairs.add(pair);
            stack.push(this);
            stack.push(new CanonicalizeTask(
                    value,
                    childDepth,
                    new PairValueAssignment(pair)));
            stack.push(new CanonicalizeTask(
                    key,
                    childDepth,
                    new PairKeyAssignment(pair)));
        }
    }

    /** 单个 Entry 的 key/value 按预算顺序压入 canonical 栈。 */
    private static final class CanonicalizeEntryTask
            implements CanonicalTask {
        private final Map.Entry<?, ?> entry;
        private final int childDepth;
        private final IdPair pair;

        private CanonicalizeEntryTask(
                Map.Entry<?, ?> entry,
                int childDepth,
                IdPair pair) {
            this.entry = entry;
            this.childDepth = childDepth;
            this.pair = pair;
        }

        @Override
        public void execute(
                CanonicalSession session,
                Deque<CanonicalTask> stack) {
            session.budget.consumeEdge();
            Object key = entry.getKey();
            session.budget.consumeEdge();
            Object value = entry.getValue();
            stack.push(new CanonicalizeTask(
                    value,
                    childDepth,
                    new PairValueAssignment(pair)));
            stack.push(new CanonicalizeTask(
                    key,
                    childDepth,
                    new PairKeyAssignment(pair)));
        }
    }

    /** canonical 根 ID 只允许写入一次。 */
    private static final class IdRoot implements IdAssignment {
        private int value;
        private boolean assigned;

        @Override
        public void assign(int value) {
            if (assigned) {
                throw new IllegalStateException(
                        "comparison root assigned twice");
            }
            this.value = value;
            this.assigned = true;
        }

        private int value() {
            if (!assigned) {
                throw new IllegalStateException(
                        "comparison root was not assigned");
            }
            return value;
        }
    }

    /** 保存一个 child ID。 */
    private static final class IdHolder implements IdAssignment {
        private int value;
        private boolean assigned;

        @Override
        public void assign(int value) {
            if (assigned) {
                throw new IllegalStateException(
                        "comparison child assigned twice");
            }
            this.value = value;
            this.assigned = true;
        }
    }

    /** 按 iterator 顺序追加 child ID。 */
    private static final class IdListAssignment implements IdAssignment {
        private final List<Integer> target;

        private IdListAssignment(List<Integer> target) {
            this.target = target;
        }

        @Override
        public void assign(int value) {
            target.add(Integer.valueOf(value));
        }
    }

    /** 保存 Map/Entry 的 key/value canonical IDs。 */
    private static final class IdPair {
        private int key;
        private int value;
        private boolean keyAssigned;
        private boolean valueAssigned;
    }

    /** 写入 pair key ID。 */
    private static final class PairKeyAssignment implements IdAssignment {
        private final IdPair pair;

        private PairKeyAssignment(IdPair pair) {
            this.pair = pair;
        }

        @Override
        public void assign(int value) {
            pair.key = value;
            pair.keyAssigned = true;
        }
    }

    /** 写入 pair value ID。 */
    private static final class PairValueAssignment implements IdAssignment {
        private final IdPair pair;

        private PairValueAssignment(IdPair pair) {
            this.pair = pair;
        }

        @Override
        public void assign(int value) {
            pair.value = value;
            pair.valueAssigned = true;
        }
    }

    /** 完成 Optional canonical 节点。 */
    private static final class FinishOptionalTask implements CanonicalTask {
        private final Object source;
        private final IdHolder child;
        private final IdAssignment target;

        private FinishOptionalTask(
                Object source,
                IdHolder child,
                IdAssignment target) {
            this.source = source;
            this.child = child;
            this.target = target;
        }

        @Override
        public void execute(
                CanonicalSession session,
                Deque<CanonicalTask> stack) {
            if (!child.assigned) {
                throw new IllegalStateException(
                        "comparison optional child missing");
            }
            session.complete(
                    source,
                    session.nodeId(
                            CanonicalType.OPTIONAL_PRESENT,
                            new int[]{child.value}),
                    target);
        }
    }

    /** 完成有序 List 或无序 Set canonical 节点。 */
    private static final class FinishSequenceTask
            implements CanonicalTask {
        private final Object source;
        private final List<Integer> ids;
        private final CanonicalType type;
        private final boolean unordered;
        private final IdAssignment target;

        private FinishSequenceTask(
                Object source,
                List<Integer> ids,
                CanonicalType type,
                boolean unordered,
                IdAssignment target) {
            this.source = source;
            this.ids = ids;
            this.type = type;
            this.unordered = unordered;
            this.target = target;
        }

        @Override
        public void execute(
                CanonicalSession session,
                Deque<CanonicalTask> stack) {
            int[] parts = new int[ids.size()];
            for (int index = 0; index < ids.size(); index++) {
                parts[index] = ids.get(index).intValue();
            }
            if (unordered) {
                Arrays.sort(parts);
                if (type == CanonicalType.SET) {
                    // 排序后相邻 ID 相同表示 identity-backed Set 含 equality 重复元素。
                    for (int index = 1; index < parts.length; index++) {
                        if (parts[index - 1] == parts[index]) {
                            throw new ArtifactSnapshots.CanonicalCollisionException(
                                    "set-element");
                        }
                    }
                }
            }
            session.complete(
                    source,
                    session.nodeId(type, parts),
                    target);
        }
    }

    /** 完成 Map 或 Entry canonical 节点。 */
    private static final class FinishPairsTask implements CanonicalTask {
        private final Object source;
        private final List<IdPair> pairs;
        private final CanonicalType type;
        private final boolean unordered;
        private final IdAssignment target;

        private FinishPairsTask(
                Object source,
                List<IdPair> pairs,
                CanonicalType type,
                boolean unordered,
                IdAssignment target) {
            this.source = source;
            this.pairs = pairs;
            this.type = type;
            this.unordered = unordered;
            this.target = target;
        }

        @Override
        public void execute(
                CanonicalSession session,
                Deque<CanonicalTask> stack) {
            List<IdPair> ordered = new ArrayList<IdPair>(pairs);
            for (IdPair pair : ordered) {
                if (!pair.keyAssigned || !pair.valueAssigned) {
                    throw new IllegalStateException(
                            "comparison map entry metadata missing");
                }
            }
            if (unordered) {
                Collections.sort(ordered, new Comparator<IdPair>() {
                    @Override
                    public int compare(IdPair left, IdPair right) {
                        int keyComparison = Integer.compare(left.key, right.key);
                        return keyComparison != 0
                                ? keyComparison
                                : Integer.compare(left.value, right.value);
                    }
                });
            }
            if (unordered && type == CanonicalType.MAP) {
                // 相邻 canonical key 相同表示 Map 含 equality 重复 key，禁止形成合法节点。
                for (int index = 1; index < ordered.size(); index++) {
                    if (ordered.get(index - 1).key == ordered.get(index).key) {
                        throw new ArtifactSnapshots.CanonicalCollisionException(
                                "map-key");
                    }
                }
            }
            int[] parts = new int[ordered.size() * 2];
            for (int index = 0; index < ordered.size(); index++) {
                IdPair pair = ordered.get(index);
                parts[index * 2] = pair.key;
                parts[index * 2 + 1] = pair.value;
            }
            session.complete(
                    source,
                    session.nodeId(type, parts),
                    target);
        }
    }

    /** 标量 equals/hash 的稳定 key；hash 只计算一次。 */
    private static final class ScalarKey {
        private final Object value;
        private final int hash;

        private ScalarKey(Object value) {
            this.value = value;
            this.hash = Objects.hashCode(value);
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
            return Objects.equals(value, ((ScalarKey) other).value);
        }
    }

    /** canonical type 与 immediate child IDs 组成的 intern key。 */
    private static final class CanonicalNodeKey {
        private final CanonicalType type;
        private final int[] parts;
        private final int hash;

        private CanonicalNodeKey(CanonicalType type, int[] parts) {
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
            if (!(other instanceof CanonicalNodeKey)) {
                return false;
            }
            CanonicalNodeKey that = (CanonicalNodeKey) other;
            return type == that.type && Arrays.equals(parts, that.parts);
        }
    }
}
