package dec.core.context.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * P2 唯一 model-access 授权索引。
 * lookup 只接受完整 ModelAccessRuleKey，不提供 prefix/bare/path fallback。
 */
public final class ModelAccessPolicyIndex {
    private final Map<ModelAccessRuleKey, CompiledModelAccessRule> rules;
    private final Set<ModelAccessRuleKey> keys;

    private ModelAccessPolicyIndex(Collection<CompiledModelAccessRule> values) {
        Objects.requireNonNull(values, "rules");
        List<CompiledModelAccessRule> sorted =
                new ArrayList<CompiledModelAccessRule>(values.size());
        for (CompiledModelAccessRule value : values) {
            sorted.add(Objects.requireNonNull(value, "rules contains null"));
        }
        Collections.sort(sorted);
        Map<ModelAccessRuleKey, CompiledModelAccessRule> copy =
                new LinkedHashMap<ModelAccessRuleKey, CompiledModelAccessRule>();
        for (CompiledModelAccessRule rule : sorted) {
            if (copy.put(rule.key(), rule) != null) {
                throw new IllegalArgumentException(
                        "duplicate model access rule: " + rule.key());
            }
        }
        this.rules = Collections.unmodifiableMap(copy);
        this.keys = Collections.unmodifiableSet(
                new TreeSet<ModelAccessRuleKey>(copy.keySet()));
    }

    /** 从完整、无重复的规则批次创建 exact index。 */
    public static ModelAccessPolicyIndex of(
            Collection<CompiledModelAccessRule> rules) {
        return new ModelAccessPolicyIndex(rules);
    }

    /** 创建合法的空授权索引；空索引意味着所有请求静态 DENY。 */
    public static ModelAccessPolicyIndex empty() {
        return new ModelAccessPolicyIndex(
                Collections.<CompiledModelAccessRule>emptyList());
    }

    /** 只按完整 key 精确查找，不尝试父路径、owner 或 operation 回退。 */
    public Optional<CompiledModelAccessRule> find(ModelAccessRuleKey key) {
        return Optional.ofNullable(
                rules.get(Objects.requireNonNull(key, "key")));
    }

    /** 缺少 exact rule 时确定性返回 STATIC_DENY。 */
    public AccessCompilationStatus classify(ModelAccessRuleKey key) {
        Optional<CompiledModelAccessRule> found = find(key);
        return found.isPresent()
                ? found.get().status()
                : AccessCompilationStatus.STATIC_DENY;
    }

    public Set<ModelAccessRuleKey> keys() {
        return keys;
    }

    /** 返回稳定、排序后的语义文本。 */
    public String canonicalForm() {
        StringBuilder result = new StringBuilder();
        for (CompiledModelAccessRule rule : rules.values()) {
            if (result.length() > 0) {
                result.append('\n');
            }
            result.append(rule.canonicalForm());
        }
        return result.toString();
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof ModelAccessPolicyIndex
                && rules.equals(((ModelAccessPolicyIndex) other).rules);
    }

    @Override
    public int hashCode() {
        return rules.hashCode();
    }

    @Override
    public String toString() {
        return rules.toString();
    }
}
