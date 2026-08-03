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
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * I004 对 snapshot 分配前节点硬上限和完整树预算的确定性 Oracle。
 */
class RawSnapshotBudgetReworkTest {

    /**
     * 文档数等于节点预算时，两个单根文档应进入完整树验证并成功构建。
     */
    @Test
    void allowsTwoSingleNodeDocumentsAtSnapshotLimit() {
        GuardedDocumentList documents = GuardedDocumentList.finite(
                rootDocument("first.xml"),
                rootDocument("second.xml"));

        RawBuildResult result = builderWithNodeLimit(2).build(documents);

        assertEquals(RawBuildStatus.BUILT, result.status());
        assertTrue(result.rawDefinitionSet().isPresent());
        assertEquals(0, result.rawDefinitionSet().get().size());
    }

    /**
     * 第三个文档必须在加入 snapshot 前触发节点上限。
     */
    @Test
    void rejectsThirdDocumentDuringSnapshot() {
        GuardedDocumentList documents = GuardedDocumentList.failAfterThird(
                rootDocument("first.xml"),
                rootDocument("second.xml"),
                rootDocument("third.xml"),
                rootDocument("fourth.xml"));

        RawBuildResult result = builderWithNodeLimit(2).build(documents);

        assertEquals(RawBuildStatus.FAILED, result.status());
        assertEquals("raw.limit.node-count",
                result.diagnostics().get(0).messageKey());
    }

    /**
     * snapshot 上限 Diagnostic 必须绑定触发限制的第三个文档。
     */
    @Test
    void reportsThirdDocumentSourceRef() {
        CanonicalDocumentNode third = rootDocument("third.xml");
        GuardedDocumentList documents = GuardedDocumentList.failAfterThird(
                rootDocument("first.xml"),
                rootDocument("second.xml"),
                third,
                rootDocument("fourth.xml"));

        RawBuildResult result = builderWithNodeLimit(2).build(documents);

        assertEquals("raw.limit.node-count",
                result.diagnostics().get(0).messageKey());
        assertEquals(third.sourceRef(),
                result.diagnostics().get(0).sourceRef());
    }

    /**
     * 取得第三个文档并发现超限后不得继续请求第四项。
     */
    @Test
    void stopsIteratorImmediatelyAfterThirdDocument() {
        GuardedDocumentList documents = GuardedDocumentList.failAfterThird(
                rootDocument("first.xml"),
                rootDocument("second.xml"),
                rootDocument("third.xml"),
                rootDocument("fourth.xml"));

        RawBuildResult result = builderWithNodeLimit(2).build(documents);

        assertEquals("raw.limit.node-count",
                result.diagnostics().get(0).messageKey());
        assertEquals(3, documents.nextCalls());
        assertFalse(documents.readPastThird());
    }

    /**
     * 单个文档的后代节点仍必须由完整树 ValidationBudget 累计检查。
     */
    @Test
    void keepsFullTreeNodeBudgetAfterSnapshot() {
        CanonicalDocumentNode propertyInfo = node(
                "tree.xml",
                "/orm-data-mapping/data/property-info",
                "property-info",
                Collections.singletonList(node(
                        "tree.xml",
                        "/orm-data-mapping/data/property-info/property",
                        "property",
                        Collections.<CanonicalDocumentNode>emptyList())));
        CanonicalDocumentNode data = node(
                "tree.xml",
                "/orm-data-mapping/data",
                "data",
                Collections.singletonList(propertyInfo),
                "name",
                "data");
        CanonicalDocumentNode document = node(
                "tree.xml",
                "/orm-data-mapping",
                "orm-data-mapping",
                Collections.singletonList(data));

        RawBuildResult result = builderWithNodeLimit(2).build(
                Collections.singletonList(document));

        assertEquals(RawBuildStatus.FAILED, result.status());
        assertEquals("raw.limit.node-count",
                result.diagnostics().get(0).messageKey());
        assertEquals(propertyInfo.sourceRef(),
                result.diagnostics().get(0).sourceRef());
    }

    /**
     * snapshot 资源失败不得发布成功集合或部分集合。
     */
    @Test
    void snapshotBudgetFailurePublishesNoSet() {
        GuardedDocumentList documents = GuardedDocumentList.failAfterThird(
                rootDocument("first.xml"),
                rootDocument("second.xml"),
                rootDocument("third.xml"),
                rootDocument("fourth.xml"));

        RawBuildResult result = builderWithNodeLimit(2).build(documents);

        assertEquals(RawBuildStatus.FAILED, result.status());
        assertFalse(result.rawDefinitionSet().isPresent());
        assertEquals("raw.limit.node-count",
                result.diagnostics().get(0).messageKey());
    }

    /**
     * 原始 List 只能创建一次 iterator。
     */
    @Test
    void readsOriginalListWithOneIterator() {
        GuardedDocumentList documents = GuardedDocumentList.failAfterThird(
                rootDocument("first.xml"),
                rootDocument("second.xml"),
                rootDocument("third.xml"),
                rootDocument("fourth.xml"));

        RawBuildResult result = builderWithNodeLimit(2).build(documents);

        assertEquals("raw.limit.node-count",
                result.diagnostics().get(0).messageKey());
        assertEquals(1, documents.iteratorCalls());
    }

    /**
     * Builder 不得调用原始 List 的随机访问、批量转换或 Stream 入口。
     */
    @Test
    void avoidsAllNonIteratorListEntrypoints() {
        GuardedDocumentList documents = GuardedDocumentList.finite(
                rootDocument("first.xml"),
                rootDocument("second.xml"));

        RawBuildResult result = builderWithNodeLimit(2).build(documents);

        assertEquals(RawBuildStatus.BUILT, result.status());
        assertEquals(0, documents.forbiddenCalls());
        assertEquals(1, documents.iteratorCalls());
    }

    private static RawDefinitionBuilder builderWithNodeLimit(int nodeLimit) {
        return new RawDefinitionBuilder(new RawBuilderLimits(8, nodeLimit));
    }

    private static CanonicalDocumentNode rootDocument(String sourceId) {
        return node(
                sourceId,
                "/orm-data-mapping",
                "orm-data-mapping",
                Collections.<CanonicalDocumentNode>emptyList());
    }

    private static CanonicalDocumentNode node(
            String sourceId,
            String path,
            String name,
            List<CanonicalDocumentNode> children,
            String... attributes) {
        java.util.Map<String, String> values =
                new java.util.LinkedHashMap<String, String>();
        for (int index = 0; index < attributes.length; index += 2) {
            values.put(attributes[index], attributes[index + 1]);
        }
        return new CanonicalDocumentNode(
                name,
                values,
                Optional.<String>empty(),
                children,
                new SourceRef(sourceId, 1, 1, path),
                DocumentFormat.XML,
                "1.0");
    }

    /**
     * 只允许单次 iterator；其他 List 入口全部记录后抛错。
     */
    private static final class GuardedDocumentList
            extends AbstractList<CanonicalDocumentNode> {
        private final List<CanonicalDocumentNode> documents;
        private final boolean failAfterThird;
        private int iteratorCalls;
        private int nextCalls;
        private int forbiddenCalls;
        private boolean readPastThird;

        private GuardedDocumentList(
                List<CanonicalDocumentNode> documents,
                boolean failAfterThird) {
            this.documents = new ArrayList<CanonicalDocumentNode>(documents);
            this.failAfterThird = failAfterThird;
        }

        private static GuardedDocumentList finite(
                CanonicalDocumentNode... documents) {
            return new GuardedDocumentList(Arrays.asList(documents), false);
        }

        private static GuardedDocumentList failAfterThird(
                CanonicalDocumentNode... documents) {
            return new GuardedDocumentList(Arrays.asList(documents), true);
        }

        @Override
        public Iterator<CanonicalDocumentNode> iterator() {
            iteratorCalls++;
            return new Iterator<CanonicalDocumentNode>() {
                private int index;

                @Override
                public boolean hasNext() {
                    if (failAfterThird && index >= 3) {
                        readPastThird = true;
                        throw new IllegalStateException(
                                "builder read beyond the third document");
                    }
                    return index < documents.size();
                }

                @Override
                public CanonicalDocumentNode next() {
                    nextCalls++;
                    return documents.get(index++);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException(
                            "test iterator is read-only");
                }
            };
        }

        @Override
        public CanonicalDocumentNode get(int index) {
            throw forbidden("get");
        }

        @Override
        public int size() {
            throw forbidden("size");
        }

        @Override
        public boolean isEmpty() {
            throw forbidden("isEmpty");
        }

        @Override
        public Object[] toArray() {
            throw forbidden("toArray");
        }

        @Override
        public <T> T[] toArray(T[] values) {
            throw forbidden("toArray(T[])");
        }

        @Override
        public Spliterator<CanonicalDocumentNode> spliterator() {
            throw forbidden("spliterator");
        }

        @Override
        public Stream<CanonicalDocumentNode> stream() {
            throw forbidden("stream");
        }

        @Override
        public Stream<CanonicalDocumentNode> parallelStream() {
            throw forbidden("parallelStream");
        }

        private IllegalStateException forbidden(String operation) {
            forbiddenCalls++;
            return new IllegalStateException(
                    "forbidden List operation: " + operation);
        }

        private int iteratorCalls() {
            return iteratorCalls;
        }

        private int nextCalls() {
            return nextCalls;
        }

        private int forbiddenCalls() {
            return forbiddenCalls;
        }

        private boolean readPastThird() {
            return readPastThird;
        }
    }
}
