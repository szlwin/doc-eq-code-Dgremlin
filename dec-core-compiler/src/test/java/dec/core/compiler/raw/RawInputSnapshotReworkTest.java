package dec.core.compiler.raw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.canonical.CanonicalDocumentNode;
import dec.core.compiler.canonical.DocumentFormat;
import dec.core.context.model.SourceRef;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * I003 对 public build 输入批次快照、顺序与 fail-closed 的确定性 Oracle。
 *
 * <p>测试只使用可控 List，不依赖线程调度、执行时间、OOM 或栈溢出。</p>
 */
class RawInputSnapshotReworkTest {

    /**
     * 后续 iterator 返回 unsupported root 时，提取仍只能消费第一次冻结的合法批次。
     */
    @Test
    void ignoresUnsupportedRootFromSecondIterator() {
        SwitchingList documents = new SwitchingList(
                Collections.singletonList(dataDocument("good.xml", "good")),
                Collections.singletonList(node(
                        "evil.xml", "/evil-root", "evil-root", attrs())));

        RawBuildResult result = new RawDefinitionBuilder().build(documents);

        assertEquals(RawBuildStatus.BUILT, result.status());
        assertEquals("good", onlyDefinition(result).name().get());
        assertEquals(1, documents.iteratorCalls());
    }

    /**
     * 后续 iterator 返回 unknown child 时，非法节点不得进入成功 Raw body。
     */
    @Test
    void ignoresUnknownChildFromSecondIterator() {
        CanonicalDocumentNode unknown = node(
                "bad.xml", "/orm-data-mapping/data/mystery",
                "mystery", attrs());
        SwitchingList documents = new SwitchingList(
                Collections.singletonList(dataDocument("good.xml", "good")),
                Collections.singletonList(dataDocument(
                        "bad.xml", "bad", unknown)));

        RawBuildResult result = new RawDefinitionBuilder().build(documents);

        RawDefinition definition = onlyDefinition(result);
        assertEquals("good", definition.name().get());
        assertTrue(definition.body().children().isEmpty());
        assertEquals(1, documents.iteratorCalls());
    }

    /**
     * 第一次迭代结束后修改原始 backing List，不得改变已冻结结果。
     */
    @Test
    void mutationAfterSnapshotDoesNotAffectResult() {
        SnapshotMutationList documents = new SnapshotMutationList(
                Collections.singletonList(dataDocument("good.xml", "good")),
                Collections.singletonList(dataDocument("changed.xml", "changed")));

        RawBuildResult result = new RawDefinitionBuilder().build(documents);

        assertEquals("good", onlyDefinition(result).name().get());
        assertEquals(1, documents.iteratorCalls());
    }

    /**
     * sourceOrdinal 只能来自第一次快照中的文档顺序。
     */
    @Test
    void snapshotOrderDeterminesOrdinals() {
        CanonicalDocumentNode first = dataDocument("b.xml", "b");
        CanonicalDocumentNode second = dataDocument("a.xml", "a");
        SwitchingList documents = new SwitchingList(
                Arrays.asList(first, second),
                Arrays.asList(second, first));

        RawBuildResult result = new RawDefinitionBuilder().build(documents);
        List<RawDefinition> definitions =
                result.rawDefinitionSet().get().definitions();

        assertEquals("b", definitions.get(0).name().get());
        assertEquals(0L, definitions.get(0).sourceOrdinal());
        assertEquals("a", definitions.get(1).name().get());
        assertEquals(1L, definitions.get(1).sourceOrdinal());
        assertEquals(1, documents.iteratorCalls());
    }

    /**
     * unsupported root 位于真实快照时必须 fail closed，不发布成功空集合。
     */
    @Test
    void unsupportedRootInSnapshotFailsWithoutSet() {
        CanonicalDocumentNode unsupported = node(
                "evil.xml", "/evil-root", "evil-root", attrs());

        RawBuildResult result = new RawDefinitionBuilder().build(
                Collections.singletonList(unsupported));

        assertEquals(RawBuildStatus.FAILED, result.status());
        assertFalse(result.rawDefinitionSet().isPresent());
        assertEquals("raw.document.root.unsupported",
                result.diagnostics().get(0).messageKey());
    }

    /**
     * unknown child 位于真实快照时必须 fail closed，不发布非法 body。
     */
    @Test
    void unknownChildInSnapshotFailsWithoutSet() {
        CanonicalDocumentNode unknown = node(
                "bad.xml", "/orm-data-mapping/data/mystery",
                "mystery", attrs());

        RawBuildResult result = new RawDefinitionBuilder().build(
                Collections.singletonList(dataDocument(
                        "bad.xml", "bad", unknown)));

        assertEquals(RawBuildStatus.FAILED, result.status());
        assertFalse(result.rawDefinitionSet().isPresent());
        assertEquals("raw.structure.unknown",
                result.diagnostics().get(0).messageKey());
    }

    /**
     * 快照读取异常必须进入稳定失败边界，catch 不得再次读取原始 List。
     */
    @Test
    void snapshotReadFailureDoesNotReaccessOriginalList() {
        ExplodingSnapshotList documents = new ExplodingSnapshotList(
                dataDocument("good.xml", "good"));

        RawBuildResult result = new RawDefinitionBuilder().build(documents);

        assertEquals(RawBuildStatus.FAILED, result.status());
        assertFalse(result.rawDefinitionSet().isPresent());
        assertEquals("raw.build.failed",
                result.diagnostics().get(0).messageKey());
        assertEquals(1, documents.iteratorCalls());
    }

    private static RawDefinition onlyDefinition(RawBuildResult result) {
        assertEquals(RawBuildStatus.BUILT, result.status());
        assertTrue(result.rawDefinitionSet().isPresent());
        assertEquals(1, result.rawDefinitionSet().get().size());
        return result.rawDefinitionSet().get().definitions().get(0);
    }

    private static CanonicalDocumentNode dataDocument(
            String sourceId,
            String name,
            CanonicalDocumentNode... dataChildren) {
        CanonicalDocumentNode data = node(
                sourceId,
                "/orm-data-mapping/data",
                "data",
                attrs("name", name),
                dataChildren);
        return node(
                sourceId,
                "/orm-data-mapping",
                "orm-data-mapping",
                attrs(),
                data);
    }

    private static CanonicalDocumentNode node(
            String sourceId,
            String path,
            String name,
            Map<String, String> attributes,
            CanonicalDocumentNode... children) {
        return new CanonicalDocumentNode(
                name,
                attributes,
                Optional.<String>empty(),
                Arrays.asList(children),
                new SourceRef(sourceId, 1, 1, path),
                DocumentFormat.XML,
                "1.0");
    }

    private static Map<String, String> attrs(String... values) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        for (int index = 0; index < values.length; index += 2) {
            result.put(values[index], values[index + 1]);
        }
        return result;
    }

    /**
     * 第一次和后续迭代返回不同批次，确定性复现双遍历绕过。
     */
    private static final class SwitchingList
            extends AbstractList<CanonicalDocumentNode> {
        private final List<CanonicalDocumentNode> first;
        private final List<CanonicalDocumentNode> subsequent;
        private int iteratorCalls;

        private SwitchingList(
                List<CanonicalDocumentNode> first,
                List<CanonicalDocumentNode> subsequent) {
            this.first = new ArrayList<CanonicalDocumentNode>(first);
            this.subsequent = new ArrayList<CanonicalDocumentNode>(subsequent);
        }

        @Override
        public CanonicalDocumentNode get(int index) {
            return first.get(index);
        }

        @Override
        public int size() {
            return first.size();
        }

        @Override
        public Iterator<CanonicalDocumentNode> iterator() {
            iteratorCalls++;
            return (iteratorCalls == 1 ? first : subsequent).iterator();
        }

        private int iteratorCalls() {
            return iteratorCalls;
        }
    }

    /**
     * 第一次 iterator 耗尽时修改 backing List，模拟快照完成后的调用方变化。
     */
    private static final class SnapshotMutationList
            extends AbstractList<CanonicalDocumentNode> {
        private final List<CanonicalDocumentNode> backing;
        private final List<CanonicalDocumentNode> replacement;
        private int iteratorCalls;

        private SnapshotMutationList(
                List<CanonicalDocumentNode> initial,
                List<CanonicalDocumentNode> replacement) {
            this.backing = new ArrayList<CanonicalDocumentNode>(initial);
            this.replacement =
                    new ArrayList<CanonicalDocumentNode>(replacement);
        }

        @Override
        public CanonicalDocumentNode get(int index) {
            return backing.get(index);
        }

        @Override
        public int size() {
            return backing.size();
        }

        @Override
        public Iterator<CanonicalDocumentNode> iterator() {
            iteratorCalls++;
            if (iteratorCalls > 1) {
                return backing.iterator();
            }
            final Iterator<CanonicalDocumentNode> initial =
                    new ArrayList<CanonicalDocumentNode>(backing).iterator();
            return new Iterator<CanonicalDocumentNode>() {
                private boolean replaced;

                @Override
                public boolean hasNext() {
                    boolean hasNext = initial.hasNext();
                    if (!hasNext && !replaced) {
                        backing.clear();
                        backing.addAll(replacement);
                        replaced = true;
                    }
                    return hasNext;
                }

                @Override
                public CanonicalDocumentNode next() {
                    return initial.next();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException(
                            "test iterator is read-only");
                }
            };
        }

        private int iteratorCalls() {
            return iteratorCalls;
        }
    }

    /**
     * 只允许 iterator 单次读取；所有随机访问均抛出 RuntimeException。
     */
    private static final class ExplodingSnapshotList
            extends AbstractList<CanonicalDocumentNode> {
        private final CanonicalDocumentNode first;
        private int iteratorCalls;

        private ExplodingSnapshotList(CanonicalDocumentNode first) {
            this.first = first;
        }

        @Override
        public CanonicalDocumentNode get(int index) {
            throw new IllegalStateException("original list re-accessed by get");
        }

        @Override
        public int size() {
            throw new IllegalStateException("original list re-accessed by size");
        }

        @Override
        public Iterator<CanonicalDocumentNode> iterator() {
            iteratorCalls++;
            return new Iterator<CanonicalDocumentNode>() {
                private int state;

                @Override
                public boolean hasNext() {
                    if (state == 0) {
                        return true;
                    }
                    throw new IllegalStateException("snapshot read failed");
                }

                @Override
                public CanonicalDocumentNode next() {
                    state = 1;
                    return first;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException(
                            "test iterator is read-only");
                }
            };
        }

        private int iteratorCalls() {
            return iteratorCalls;
        }
    }
}
