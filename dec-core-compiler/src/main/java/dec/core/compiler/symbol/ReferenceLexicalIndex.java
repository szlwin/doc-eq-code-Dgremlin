package dec.core.compiler.symbol;

import dec.core.context.model.DefinitionKey;
import dec.core.context.model.RuleViewKey;
import java.util.HashMap;
import java.util.Map;

/**
 * 对同 lexical 的 TypedKey 类型进行一次性预聚合，阻断失败分类的候选扫描。
 */
final class ReferenceLexicalIndex {
    private final Map<String, CandidateSummary> summaries =
            new HashMap<String, CandidateSummary>();

    /** 按 SymbolTable 稳定 Key 顺序登记代表 Key。 */
    void add(String lexical, DefinitionKey key) {
        CandidateSummary summary = summaries.get(lexical);
        if (summary == null) {
            summary = new CandidateSummary();
            summaries.put(lexical, summary);
        }
        summary.add(key);
    }

    /** 平均 O(1) 返回 lexical 摘要。 */
    CandidateSummary find(String lexical) {
        return summaries.get(lexical);
    }

    /** 每种 Key 类型只保存稳定首个代表，不保存可被重复扫描的候选 List。 */
    static final class CandidateSummary {
        private final Map<Class<? extends DefinitionKey>, DefinitionKey> byType =
                new HashMap<Class<? extends DefinitionKey>, DefinitionKey>();
        private DefinitionKey first;

        private void add(DefinitionKey key) {
            if (first == null) {
                first = key;
            }
            Class<? extends DefinitionKey> type = key.getClass();
            if (!byType.containsKey(type)) {
                byType.put(type, key);
            }
        }

        boolean hasAny() {
            return first != null;
        }

        boolean hasType(Class<? extends DefinitionKey> type) {
            return byType.containsKey(type);
        }

        boolean hasRuleView() {
            return hasType(RuleViewKey.class);
        }

        DefinitionKey representative(Class<? extends DefinitionKey> type) {
            DefinitionKey value = byType.get(type);
            return value == null ? first : value;
        }
    }
}
