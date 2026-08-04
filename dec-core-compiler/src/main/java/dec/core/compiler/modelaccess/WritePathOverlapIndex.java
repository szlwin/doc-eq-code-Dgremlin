package dec.core.compiler.modelaccess;

import java.util.Map;
import java.util.TreeMap;

/**
 * 使用路径段 trie 检测同一 ModelAccess 内的 WRITE 路径重叠。
 *
 * <p>索引仅在单次 definition 编译期间存在，不跨调用保存状态。每个路径只按其
 * segment 数遍历一次，避免合法节点预算下的两两比较放大。</p>
 */
final class WritePathOverlapIndex {
    private final Node root = new Node();
    private boolean wildcard;
    private boolean concretePath;
    private int operationCount;

    /**
     * 登记一个 WRITE 路径，并返回它是否与已登记路径相同或形成祖先/后代关系。
     */
    boolean add(SharedModelPath path) {
        if (path == null) {
            throw new NullPointerException("path");
        }
        operationCount++;
        if ("*".equals(path.value())) {
            boolean overlap = wildcard || concretePath;
            wildcard = true;
            return overlap;
        }

        boolean overlap = wildcard;
        concretePath = true;
        Node current = root;
        for (String segment : path.segments()) {
            operationCount++;
            // 插入途中遇到 terminal，说明已有路径是当前路径的祖先。
            if (current.terminal) {
                overlap = true;
            }
            Node child = current.children.get(segment);
            if (child == null) {
                child = new Node();
                current.children.put(segment, child);
            }
            current = child;
        }
        // terminal 表示完全重复；已有 child 表示当前路径是既有路径的祖先。
        if (current.terminal || !current.children.isEmpty()) {
            overlap = true;
        }
        current.terminal = true;
        return overlap;
    }

    /** 返回本次局部 compilation 已执行的结构查询次数。 */
    int operationCount() {
        return operationCount;
    }

    /** trie 节点只保存直接子 segment 与路径终止标记。 */
    private static final class Node {
        private final Map<String, Node> children = new TreeMap<String, Node>();
        private boolean terminal;
    }
}
