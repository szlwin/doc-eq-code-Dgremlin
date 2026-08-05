package dec.core.compiler.pass;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 为冻结 artifact 的公开 equality/query 提供非递归、受预算的图比较。
 */
final class ArtifactComparisonSupport {

    /** 冻结容器通过该接口暴露已缓存的 Java-compatible hash。 */
    interface CachedHash {
        int cachedHash();
    }

    /** 工具类不允许实例化。 */
    private ArtifactComparisonSupport() {
    }

    /** 使用独立比较 Session 精确判断两个值是否相等。 */
    static boolean equalsValues(
            Object left,
            Object right,
            ArtifactSnapshots.ComparisonLimits limits) {
        return new ComparisonSession(
                new ComparisonBudget(Objects.requireNonNull(limits, "limits")))
                .equalsValues(left, right);
    }

    /** 在一个总预算内顺序查询 List，避免调用 query 对象的递归 equals。 */
    static int indexOf(
            List<?> values,
            Object query,
            boolean reverse,
            ArtifactSnapshots.ComparisonLimits limits) {
        ComparisonBudget budget = new ComparisonBudget(limits);
        int start = reverse ? values.size() - 1 : 0;
        int end = reverse ? -1 : values.size();
        int step = reverse ? -1 : 1;
        for (int index = start; index != end; index += step) {
            if (new ComparisonSession(budget).equalsValues(
                    values.get(index), query)) {
                return index;
            }
        }
        return -1;
    }

    /** 在一个总预算内查询 Set element。 */
    static boolean containsElement(
            List<?> values,
            Object query,
            ArtifactSnapshots.ComparisonLimits limits) {
        return indexOf(values, query, false, limits) >= 0;
    }

    /** 在一个总预算内查找 Map key，返回匹配 entry 的索引。 */
    static int findEntryByKey(
            List<? extends Map.Entry<?, ?>> entries,
            Object query,
            ArtifactSnapshots.ComparisonLimits limits) {
        ComparisonBudget budget = new ComparisonBudget(limits);
        for (int index = 0; index < entries.size(); index++) {
            if (new ComparisonSession(budget).equalsValues(
                    entries.get(index).getKey(), query)) {
                return index;
            }
        }
        return -1;
    }

    /** 在一个总预算内查询 Map value。 */
    static boolean containsValue(
            List<? extends Map.Entry<?, ?>> entries,
            Object query,
            ArtifactSnapshots.ComparisonLimits limits) {
        ComparisonBudget budget = new ComparisonBudget(limits);
        for (Map.Entry<?, ?> entry : entries) {
            if (new ComparisonSession(budget).equalsValues(
                    entry.getValue(), query)) {
                return true;
            }
        }
        return false;
    }

    /** 在一个总预算内查询 entrySet，key/value 均使用受控比较。 */
    static boolean containsEntry(
            List<? extends Map.Entry<?, ?>> entries,
            Object query,
            ArtifactSnapshots.ComparisonLimits limits) {
        if (!(query instanceof Map.Entry<?, ?>)) {
            return false;
        }
        ComparisonBudget budget = new ComparisonBudget(limits);
        for (Map.Entry<?, ?> entry : entries) {
            if (new ComparisonSession(budget).equalsValues(entry, query)) {
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

        /** 首次访问 identity pair 时消耗 pair 预算。 */
        private void consumePair() {
            if (visitedPairs >= limits.maxPairs) {
                throw new ArtifactSnapshots.ComparisonLimitException(
                        "visited-pairs");
            }
            visitedPairs++;
        }

        /** 在读取或入栈下一条比较边之前消耗 edge 预算。 */
        private void consumeEdge() {
            if (traversedEdges >= limits.maxEdges) {
                throw new ArtifactSnapshots.ComparisonLimitException(
                        "traversed-edges");
            }
            traversedEdges++;
        }

        /** 首次 canonicalize 容器 identity 时消耗 node 预算。 */
        private void consumeCanonicalNode() {
            if (canonicalNodes >= limits.maxCanonicalNodes) {
                throw new ArtifactSnapshots.ComparisonLimitException(
                        "canonical-nodes");
            }
            canonicalNodes++;
        }

        /** 每次处理 value 或 pair 时检查逻辑深度。 */
        private void checkDepth(int depth) {
            if (depth > limits.maxDepth) {
                throw new ArtifactSnapshots.ComparisonLimitException(
                        "comparison-depth");
            }
        }
    }

    /** 单次精确比较的 pair memo 与跨双根 canonical 索引。 */
    private static final class ComparisonSession {
        private final ComparisonBudget budget;
        private final IdentityHashMap<Object, IdentityHashMap<Object, Boolean>>
                visitedPairs =
                new IdentityHashMap<Object, IdentityHashMap<Object, Boolean>>();
        private final CanonicalSession canonical;
        private boolean equal = true;

        private ComparisonSession(ComparisonBudget budget) {
            this.budget = budget;
            this.canonical = new CanonicalSession(budget);
        }

        /** 通过显式 pair stack 执行精确比较。 */
        private boolean equalsValues(Object left, Object right) {
            Deque<PairTask> stack = new ArrayDeque<PairTask>();
            stack.push(new PairTask(left, right, 1));
            while (equal && !stack.isEmpty()) {
                stack.pop().execute(this, stack);
            }
            return equal;
        }

        /** 标记 identity pair；重复 pair 直接复用已验证路径。 */
        private boolean markPair(Object left, Object right) {
            IdentityHashMap<Object, Boolean> rights = visitedPairs.get(left);
            if (rights != null && rights.containsKey(right)) {
                return false;
            }
            budget.consumePair();
            if (rights == null) {
                rights = new IdentityHashMap<Object, Boolean>();
                visitedPairs.put(left, rights);
            }
            rights.put(right, Boolean.TRUE);
            return true;
        }

        /** 将当前精确比较标记为不相等。 */
        private void mismatch() {
            equal = false;
        }

        /** 两个 Set 使用同一 canonical Session 进行无序精确匹配。 */
        private boolean setsEqual(Set<?> left, Set<?> right, int depth) {
            Set<Integer> leftIds = canonicalSet(left, depth);
            Set<Integer> rightIds = canonicalSet(right, depth);
            return leftIds != null
                    && rightIds != null
                    && leftIds.equals(rightIds);
        }

        /** 将 Set element 转为 canonical ID；结构重复表示非法相等碰撞。 */
        private Set<Integer> canonicalSet(Set<?> values, int depth) {
            Set<Integer> ids = new HashSet<Integer>();
            for (Object value : values) {
                budget.consumeEdge();
                int id = canonical.canonicalId(value, depth + 1);
                if (!ids.add(Integer.valueOf(id))) {
                    return null;
                }
            }
            return ids;
        }

        /** 两个 Map 使用 canonical key/value ID 完成无序精确比较。 */
        private boolean mapsEqual(Map<?, ?> left, Map<?, ?> right, int depth) {
            Map<Integer, Integer> leftIds = canonicalMap(left, depth);
            Map<Integer, Integer> rightIds = canonicalMap(right, depth);
            return leftIds != null
                    && rightIds != null
                    && leftIds.equals(rightIds);
        }

        /** 将 Map entry 转为 canonical key/value ID；重复 key 表示非法碰撞。 */
        private Map<Integer, Integer> canonicalMap(Map<?, ?> values, int depth) {
            Map<Integer, Integer> entries = new HashMap<Integer, Integer>();
            for (Map.Entry<?, ?> entry : values.entrySet()) {
                budget.consumeEdge();
                int keyId = canonical.canonicalId(entry.getKey(), depth + 1);
                budget.consumeEdge();
                int valueId = canonical.canonicalId(entry.getValue(), depth + 1);
                Integer previous = entries.put(
                        Integer.valueOf(keyId),
                        Integer.valueOf(valueId));
                if (previous != null) {
                    return null;
                }
            }
            return entries;
        }
    }

    /** 显式 pair 栈工作项。 */
    private static final class PairTask {
        private final Object left;
        private final Object right;
        private final int depth;

        private PairTask(Object left, Object right, int depth) {
            this.left = left;
            this.right = right;
            this.depth = depth;
        }

        /** 比较当前 pair，并将有序子 pair 压入显式栈。 */
        private void execute(
                ComparisonSession session,
                Deque<PairTask> stack) {
            session.budget.checkDepth(depth);
            if (!session.markPair(left, right)) {
                return;
            }
            if (left == right) {
                return;
            }
            if (left == null || right == null) {
                session.mismatch();
                return;
            }
            if (left instanceof CachedHash && right instanceof CachedHash
                    && ((CachedHash) left).cachedHash()
                    != ((CachedHash) right).cachedHash()) {
                session.mismatch();
                return;
            }

            if (left instanceof Optional<?> || right instanceof Optional<?>) {
                compareOptional(session, stack);
                return;
            }
            if (left instanceof List<?> || right instanceof List<?>) {
                compareList(session, stack);
                return;
            }
            if (left instanceof Set<?> || right instanceof Set<?>) {
                compareSet(session);
                return;
            }
            if (left instanceof Map<?, ?> || right instanceof Map<?, ?>) {
                compareMap(session);
                return;
            }
            if (left instanceof Map.Entry<?, ?>
                    || right instanceof Map.Entry<?, ?>) {
                compareEntry(session, stack);
                return;
            }
            if (!Objects.equals(left, right)) {
                session.mismatch();
            }
        }

        /** Optional 仅在 presence 相同后比较唯一 value。 */
        private void compareOptional(
                ComparisonSession session,
                Deque<PairTask> stack) {
            if (!(left instanceof Optional<?>)
                    || !(right instanceof Optional<?>)) {
                session.mismatch();
                return;
            }
            Optional<?> leftOptional = (Optional<?>) left;
            Optional<?> rightOptional = (Optional<?>) right;
            if (leftOptional.isPresent() != rightOptional.isPresent()) {
                session.mismatch();
                return;
            }
            if (leftOptional.isPresent()) {
                session.budget.consumeEdge();
                stack.push(new PairTask(
                        leftOptional.get(),
                        rightOptional.get(),
                        depth + 1));
            }
        }

        /** List 按索引顺序比较，shared pair 由 identity memo 去重。 */
        private void compareList(
                ComparisonSession session,
                Deque<PairTask> stack) {
            if (!(left instanceof List<?>) || !(right instanceof List<?>)) {
                session.mismatch();
                return;
            }
            List<?> leftList = (List<?>) left;
            List<?> rightList = (List<?>) right;
            if (leftList.size() != rightList.size()) {
                session.mismatch();
                return;
            }
            for (int index = leftList.size() - 1; index >= 0; index--) {
                session.budget.consumeEdge();
                stack.push(new PairTask(
                        leftList.get(index),
                        rightList.get(index),
                        depth + 1));
            }
        }

        /** Set 通过双根共享 canonical IDs 完成无序精确比较。 */
        private void compareSet(ComparisonSession session) {
            if (!(left instanceof Set<?>) || !(right instanceof Set<?>)) {
                session.mismatch();
                return;
            }
            Set<?> leftSet = (Set<?>) left;
            Set<?> rightSet = (Set<?>) right;
            if (leftSet.size() != rightSet.size()
                    || !session.setsEqual(leftSet, rightSet, depth)) {
                session.mismatch();
            }
        }

        /** Map 通过 canonical key/value IDs 完成无序精确比较。 */
        private void compareMap(ComparisonSession session) {
            if (!(left instanceof Map<?, ?>) || !(right instanceof Map<?, ?>)) {
                session.mismatch();
                return;
            }
            Map<?, ?> leftMap = (Map<?, ?>) left;
            Map<?, ?> rightMap = (Map<?, ?>) right;
            if (leftMap.size() != rightMap.size()
                    || !session.mapsEqual(leftMap, rightMap, depth)) {
                session.mismatch();
            }
        }

        /** Entry 的 key/value 均进入同一 pair traversal。 */
        private void compareEntry(
                ComparisonSession session,
                Deque<PairTask> stack) {
            if (!(left instanceof Map.Entry<?, ?>)
                    || !(right instanceof Map.Entry<?, ?>)) {
                session.mismatch();
                return;
            }
            Map.Entry<?, ?> leftEntry = (Map.Entry<?, ?>) left;
            Map.Entry<?, ?> rightEntry = (Map.Entry<?, ?>) right;
            session.budget.consumeEdge();
            stack.push(new PairTask(
                    leftEntry.getValue(),
                    rightEntry.getValue(),
                    depth + 1));
            session.budget.consumeEdge();
            stack.push(new PairTask(
                    leftEntry.getKey(),
                    rightEntry.getKey(),
                    depth + 1));
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

    /** 将 canonical ID 写入父节点。 */
    private interface IdAssignment {
        void assign(int id);
    }

    /** 显式 canonical traversal 工作项。 */
    private interface CanonicalTask {
        void execute(CanonicalSession session, Deque<CanonicalTask> stack);
    }

    /** 双根共享的 canonical intern table。 */
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

        /** 使用显式栈为任意支持值生成跨双根可比较的 canonical ID。 */
        private int canonicalId(Object root, int depth) {
            IdRoot result = new IdRoot();
            Deque<CanonicalTask> stack = new ArrayDeque<CanonicalTask>();
            stack.push(new CanonicalizeTask(root, depth, result));
            while (!stack.isEmpty()) {
                stack.pop().execute(this, stack);
            }
            return result.value();
        }

        /** 标量按 Java equals/hash 语义在同一比较 Session 内归一化。 */
        private int scalarId(Object value) {
            Integer identity = scalarIdsByIdentity.get(value);
            if (identity != null) {
                return identity.intValue();
            }
            int hash = value == null ? 0 : value.hashCode();
            ScalarKey key = new ScalarKey(value, hash);
            Integer existing = scalarIds.get(key);
            int id;
            if (existing == null) {
                id = nextId++;
                scalarIds.put(key, Integer.valueOf(id));
            } else {
                id = existing.intValue();
            }
            if (value != null) {
                scalarIdsByIdentity.put(value, Integer.valueOf(id));
            }
            return id;
        }

        /** 对相同 canonical type/child IDs 分配同一 ID。 */
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

        /** 首次进入容器 identity 时登记状态与 node 预算。 */
        private void enter(Object value) {
            budget.consumeCanonicalNode();
            states.put(value, CanonicalState.VISITING);
        }

        /** 完成容器并缓存其 canonical ID。 */
        private void complete(Object value, int id, IdAssignment target) {
            states.put(value, CanonicalState.FROZEN);
            idsByIdentity.put(value, Integer.valueOf(id));
            target.assign(id);
        }
    }

    /** 处理一个 canonical value；容器展开为子任务和完成任务。 */
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
                expandList(session, stack);
            } else if (value instanceof Set<?>) {
                expandSet(session, stack);
            } else if (value instanceof Map<?, ?>) {
                expandMap(session, stack);
            } else {
                expandEntry(session, stack);
            }
        }

        /** 判断值是否由 comparison canonicalizer 显式遍历。 */
        private static boolean isContainer(Object value) {
            return value instanceof Optional<?>
                    || value instanceof List<?>
                    || value instanceof Set<?>
                    || value instanceof Map<?, ?>
                    || value instanceof Map.Entry<?, ?>;
        }

        /** 展开 Optional。 */
        private void expandOptional(
                CanonicalSession session,
                Deque<CanonicalTask> stack) {
            Optional<?> optional = (Optional<?>) value;
            if (!optional.isPresent()) {
                session.complete(
                        value,
                        session.nodeId(CanonicalType.OPTIONAL_EMPTY, new int[0]),
                        target);
                return;
            }
            session.budget.consumeEdge();
            IdHolder child = new IdHolder();
            stack.push(new FinishOptional(value, child, target));
            stack.push(new CanonicalizeTask(
                    optional.get(),
                    depth + 1,
                    child));
        }

        /** 展开 List。 */
        private void expandList(
                CanonicalSession session,
                Deque<CanonicalTask> stack) {
            List<?> values = (List<?>) value;
            List<Integer> ids = new ArrayList<Integer>(values.size());
            stack.push(new FinishSequence(
                    value,
                    ids,
                    CanonicalType.LIST,
                    false,
                    target));
            for (int index = values.size() - 1; index >= 0; index--) {
                session.budget.consumeEdge();
                stack.push(new CanonicalizeTask(
                        values.get(index),
                        depth + 1,
                        new IdListAssignment(ids)));
            }
        }

        /** 展开 Set。 */
        private void expandSet(
                CanonicalSession session,
                Deque<CanonicalTask> stack) {
            List<Object> values = new ArrayList<Object>((Set<?>) value);
            List<Integer> ids = new ArrayList<Integer>(values.size());
            stack.push(new FinishSequence(
                    value,
                    ids,
                    CanonicalType.SET,
                    true,
                    target));
            for (int index = values.size() - 1; index >= 0; index--) {
                session.budget.consumeEdge();
                stack.push(new CanonicalizeTask(
                        values.get(index),
                        depth + 1,
                        new IdListAssignment(ids)));
            }
        }

        /** 展开 Map。 */
        private void expandMap(
                CanonicalSession session,
                Deque<CanonicalTask> stack) {
            List<Map.Entry<?, ?>> entries =
                    new ArrayList<Map.Entry<?, ?>>(((Map<?, ?>) value).entrySet());
            List<IdPair> pairs = new ArrayList<IdPair>(entries.size());
            stack.push(new FinishPairs(
                    value,
                    pairs,
                    CanonicalType.MAP,
                    true,
                    target));
            for (int index = entries.size() - 1; index >= 0; index--) {
                Map.Entry<?, ?> entry = entries.get(index);
                IdPair pair = new IdPair();
                pairs.add(pair);
                session.budget.consumeEdge();
                stack.push(new CanonicalizeTask(
                        entry.getValue(),
                        depth + 1,
                        new PairValueAssignment(pair)));
                session.budget.consumeEdge();
                stack.push(new CanonicalizeTask(
                        entry.getKey(),
                        depth + 1,
                        new PairKeyAssignment(pair)));
            }
        }

        /** 展开 Map.Entry。 */
        private void expandEntry(
                CanonicalSession session,
                Deque<CanonicalTask> stack) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) value;
            IdPair pair = new IdPair();
            List<IdPair> pairs = new ArrayList<IdPair>(1);
            pairs.add(pair);
            stack.push(new FinishPairs(
                    value,
                    pairs,
                    CanonicalType.ENTRY,
                    false,
                    target));
            session.budget.consumeEdge();
            stack.push(new CanonicalizeTask(
                    entry.getValue(),
                    depth + 1,
                    new PairValueAssignment(pair)));
            session.budget.consumeEdge();
            stack.push(new CanonicalizeTask(
                    entry.getKey(),
                    depth + 1,
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
                throw new IllegalStateException("comparison root assigned twice");
            }
            this.value = value;
            this.assigned = true;
        }

        /** 返回已完成的根 ID。 */
        private int value() {
            if (!assigned) {
                throw new IllegalStateException("comparison root was not assigned");
            }
            return value;
        }
    }

    /** 保存单个 child ID。 */
    private static final class IdHolder implements IdAssignment {
        private int value;
        private boolean assigned;

        @Override
        public void assign(int value) {
            if (assigned) {
                throw new IllegalStateException("comparison child assigned twice");
            }
            this.value = value;
            this.assigned = true;
        }
    }

    /** 按原遍历顺序追加 child ID。 */
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

    /** 保存 key/value canonical IDs。 */
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

    /** 完成 Optional canonical node。 */
    private static final class FinishOptional implements CanonicalTask {
        private final Object source;
        private final IdHolder child;
        private final IdAssignment target;

        private FinishOptional(
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
                throw new IllegalStateException("comparison optional incomplete");
            }
            session.complete(
                    source,
                    session.nodeId(
                            CanonicalType.OPTIONAL_PRESENT,
                            new int[]{child.value}),
                    target);
        }
    }

    /** 完成 List/Set canonical node。 */
    private static final class FinishSequence implements CanonicalTask {
        private final Object source;
        private final List<Integer> ids;
        private final CanonicalType type;
        private final boolean sort;
        private final IdAssignment target;

        private FinishSequence(
                Object source,
                List<Integer> ids,
                CanonicalType type,
                boolean sort,
                IdAssignment target) {
            this.source = source;
            this.ids = ids;
            this.type = type;
            this.sort = sort;
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
            if (sort) {
                Arrays.sort(parts);
            }
            session.complete(
                    source,
                    session.nodeId(type, parts),
                    target);
        }
    }

    /** 完成 Map/Entry canonical node。 */
    private static final class FinishPairs implements CanonicalTask {
        private final Object source;
        private final List<IdPair> pairs;
        private final CanonicalType type;
        private final boolean sort;
        private final IdAssignment target;

        private FinishPairs(
                Object source,
                List<IdPair> pairs,
                CanonicalType type,
                boolean sort,
                IdAssignment target) {
            this.source = source;
            this.pairs = pairs;
            this.type = type;
            this.sort = sort;
            this.target = target;
        }

        @Override
        public void execute(
                CanonicalSession session,
                Deque<CanonicalTask> stack) {
            for (IdPair pair : pairs) {
                if (!pair.keyAssigned || !pair.valueAssigned) {
                    throw new IllegalStateException("comparison pair incomplete");
                }
            }
            if (sort) {
                Collections.sort(pairs, new Comparator<IdPair>() {
                    @Override
                    public int compare(IdPair left, IdPair right) {
                        int key = Integer.compare(left.key, right.key);
                        return key != 0
                                ? key
                                : Integer.compare(left.value, right.value);
                    }
                });
            }
            int[] parts = new int[pairs.size() * 2];
            for (int index = 0; index < pairs.size(); index++) {
                parts[index * 2] = pairs.get(index).key;
                parts[index * 2 + 1] = pairs.get(index).value;
            }
            session.complete(
                    source,
                    session.nodeId(type, parts),
                    target);
        }
    }

    /** 标量 canonical key，hash 只计算一次。 */
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
            return Objects.equals(value, ((ScalarKey) other).value);
        }
    }

    /** canonical type 与 immediate child IDs 组成的精确 key。 */
    private static final class CanonicalNodeKey {
        private final CanonicalType type;
        private final int[] parts;
        private final int hash;

        private CanonicalNodeKey(CanonicalType type, int[] parts) {
            this.type = type;
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
