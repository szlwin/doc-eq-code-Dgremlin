package dec.core.context;

import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.Registry;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * 从同一个 CompiledModelSet 确定性派生的旧 Core 配置只读投影。
 */
public final class CoreConfigProjection {
    private final CompiledModelSet sourceModelSet;
    private final List<CompiledDefinition> data;
    private final List<CompiledDefinition> views;
    private final List<CompiledDefinition> rules;

    private CoreConfigProjection(
            CompiledModelSet sourceModelSet,
            List<CompiledDefinition> data,
            List<CompiledDefinition> views,
            List<CompiledDefinition> rules) {
        this.sourceModelSet = Objects.requireNonNull(sourceModelSet, "sourceModelSet");
        this.data = Objects.requireNonNull(data, "data");
        this.views = Objects.requireNonNull(views, "views");
        this.rules = Objects.requireNonNull(rules, "rules");
    }

    /**
     * 从模型中的 Typed Registry 确定性生成 Projection。
     * 调用方无法传入独立列表，因此 Projection 不会成为第二事实源。
     *
     * @param modelSet Projection 唯一来源模型
     * @return 与该模型同源的只读 Projection
     */
    public static CoreConfigProjection from(CompiledModelSet modelSet) {
        CompiledModelSet source = Objects.requireNonNull(modelSet, "modelSet");
        return new CoreConfigProjection(
                source,
                immutableValues("data", source.typedRegistries().data()),
                immutableValues("views", source.typedRegistries().views()),
                immutableValues("rules", source.typedRegistries().ruleViews()));
    }

    /** 返回生成当前 Projection 的唯一模型来源。 */
    public CompiledModelSet sourceModelSet() {
        return sourceModelSet;
    }

    /** 返回 Data 定义只读投影。 */
    public List<CompiledDefinition> data() {
        return data;
    }

    /** 返回 View 定义只读投影。 */
    public List<CompiledDefinition> views() {
        return views;
    }

    /** 返回 RuleView 定义只读投影。 */
    public List<CompiledDefinition> rules() {
        return rules;
    }

    /**
     * 兼容旧注册入口，但 Projection 永远不允许成为可变事实源。
     *
     * @param definition 被旧调用方尝试注册的定义
     * @throws ProjectionWriteRejectedException 每次调用都拒绝写入
     */
    @Deprecated
    public void register(CompiledDefinition definition) {
        throw rejectWrite("register");
    }

    /**
     * 兼容旧替换入口，但不允许替换已发布模型中的任何事实。
     *
     * @param definition 被旧调用方尝试替换的定义
     * @throws ProjectionWriteRejectedException 每次调用都拒绝写入
     */
    @Deprecated
    public void replace(CompiledDefinition definition) {
        throw rejectWrite("replace");
    }

    /**
     * 兼容旧删除入口，但不允许删除已发布模型中的任何事实。
     *
     * @param key 被旧调用方尝试删除的定义身份
     * @throws ProjectionWriteRejectedException 每次调用都拒绝写入
     */
    @Deprecated
    public void remove(DefinitionKey key) {
        throw rejectWrite("remove");
    }

    /**
     * 兼容旧清空入口，但不允许清空已发布模型。
     *
     * @throws ProjectionWriteRejectedException 每次调用都拒绝写入
     */
    @Deprecated
    public void clear() {
        throw rejectWrite("clear");
    }

    /**
     * 统一创建写入拒绝异常，避免不同兼容入口产生平行失败语义。
     */
    private ProjectionWriteRejectedException rejectWrite(String operation) {
        return new ProjectionWriteRejectedException(operation);
    }

    private static <K extends DefinitionKey> List<CompiledDefinition> immutableValues(
            String projectionName,
            Registry<K, CompiledDefinition> registry) {
        Objects.requireNonNull(registry, "registry");
        List<CompiledDefinition> values =
                new ArrayList<CompiledDefinition>(registry.size());
        // Registry 的 key 已稳定排序，按 key 顺序读取即可得到确定性 Projection。
        for (K key : registry.keys()) {
            values.add(Objects.requireNonNull(
                    registry.require(key),
                    "registry contains null definition"));
        }
        return new ProjectionReadOnlyList<CompiledDefinition>(projectionName, values);
    }

    /**
     * 对 List 的所有变更入口提供统一的 Projection 专用拒绝语义。
     *
     * <p>不能只使用 Collections.unmodifiableList，因为空列表上的 remove、
     * removeAll 或 clear 可能表现为普通异常或无操作，无法产生稳定错误码。</p>
     */
    private static final class ProjectionReadOnlyList<E> extends AbstractList<E> {
        private final String projectionName;
        private final List<E> values;

        private ProjectionReadOnlyList(String projectionName, List<E> values) {
            this.projectionName = Objects.requireNonNull(projectionName, "projectionName");
            this.values = Collections.unmodifiableList(new ArrayList<E>(values));
        }

        @Override
        public E get(int index) {
            return values.get(index);
        }

        @Override
        public int size() {
            return values.size();
        }

        /**
         * 返回继续保持 Projection 写入拒绝语义的防御性子列表快照。
         */
        @Override
        public List<E> subList(int fromIndex, int toIndex) {
            return new ProjectionReadOnlyList<E>(
                    projectionName + ".subList",
                    values.subList(fromIndex, toIndex));
        }

        /**
         * 返回受控 Iterator，避免空列表 remove 先抛出普通状态异常。
         */
        @Override
        public Iterator<E> iterator() {
            return new ProjectionReadOnlyListIterator<E>(
                    projectionName + ".iterator",
                    values.listIterator());
        }

        /**
         * 返回从起点开始的受控 ListIterator。
         */
        @Override
        public ListIterator<E> listIterator() {
            return listIterator(0);
        }

        /**
         * 返回指定位置开始的受控 ListIterator，并保留标准索引校验。
         */
        @Override
        public ListIterator<E> listIterator(int index) {
            return new ProjectionReadOnlyListIterator<E>(
                    projectionName + ".listIterator",
                    values.listIterator(index));
        }

        @Override
        public E set(int index, E element) {
            throw rejected("set");
        }

        @Override
        public boolean add(E element) {
            throw rejected("add");
        }

        @Override
        public void add(int index, E element) {
            throw rejected("add");
        }

        @Override
        public boolean addAll(Collection<? extends E> values) {
            throw rejected("addAll");
        }

        @Override
        public boolean addAll(int index, Collection<? extends E> values) {
            throw rejected("addAll");
        }

        @Override
        public E remove(int index) {
            throw rejected("remove");
        }

        @Override
        public boolean remove(Object value) {
            throw rejected("remove");
        }

        @Override
        public boolean removeAll(Collection<?> values) {
            throw rejected("removeAll");
        }

        @Override
        public boolean retainAll(Collection<?> values) {
            throw rejected("retainAll");
        }

        @Override
        public void clear() {
            throw rejected("clear");
        }

        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            throw rejected("removeIf");
        }

        @Override
        public void replaceAll(UnaryOperator<E> operator) {
            throw rejected("replaceAll");
        }

        @Override
        public void sort(Comparator<? super E> comparator) {
            throw rejected("sort");
        }

        /**
         * 生成包含投影分类与具体 List 操作的稳定拒绝事实。
         */
        private ProjectionWriteRejectedException rejected(String operation) {
            return new ProjectionWriteRejectedException(
                    projectionName + "." + operation);
        }
    }

    /**
     * 冻结 Iterator/ListIterator 的公共结构；写入拒绝在 Development 阶段完成。
     */
    private static final class ProjectionReadOnlyListIterator<E>
            implements ListIterator<E> {
        private final String operationPrefix;
        private final ListIterator<E> delegate;

        private ProjectionReadOnlyListIterator(
                String operationPrefix,
                ListIterator<E> delegate) {
            this.operationPrefix = Objects.requireNonNull(
                    operationPrefix,
                    "operationPrefix");
            this.delegate = Objects.requireNonNull(delegate, "delegate");
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public E next() {
            return delegate.next();
        }

        @Override
        public boolean hasPrevious() {
            return delegate.hasPrevious();
        }

        @Override
        public E previous() {
            return delegate.previous();
        }

        @Override
        public int nextIndex() {
            return delegate.nextIndex();
        }

        @Override
        public int previousIndex() {
            return delegate.previousIndex();
        }

        @Override
        public void remove() {
            throw architectureSkeleton("remove");
        }

        @Override
        public void set(E element) {
            throw architectureSkeleton("set");
        }

        @Override
        public void add(E element) {
            throw architectureSkeleton("add");
        }

        /**
         * 显式标记架构骨架尚未完成，禁止返回伪成功。
         */
        private UnsupportedOperationException architectureSkeleton(String operation) {
            return new UnsupportedOperationException(
                    "Architecture skeleton only: "
                            + operationPrefix
                            + "."
                            + operation);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CoreConfigProjection)) {
            return false;
        }
        CoreConfigProjection that = (CoreConfigProjection) other;
        return sourceModelSet.equals(that.sourceModelSet)
                && data.equals(that.data)
                && views.equals(that.views)
                && rules.equals(that.rules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceModelSet, data, views, rules);
    }

    @Override
    public String toString() {
        return "CoreConfigProjection{"
                + "data=" + data.size()
                + ", views=" + views.size()
                + ", rules=" + rules.size()
                + '}';
    }
}
