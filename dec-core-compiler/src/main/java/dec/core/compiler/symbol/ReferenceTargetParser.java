package dec.core.compiler.symbol;

import dec.core.context.model.RuleViewKey;
import dec.core.context.model.SystemKey;

/**
 * 在构造 Context TypedKey 前校验并规范化 T08 引用 lexical。
 *
 * <p>该类型只执行无副作用的 grammar 解析，Diagnostic 由 Resolver 统一生成，
 * 避免不同引用角色产生不一致的失败边界。</p>
 */
final class ReferenceTargetParser {
    private ReferenceTargetParser() {
    }

    /** 返回 trim 后的非空简单目标；非法输入返回 null。 */
    static String parseSimple(String target) {
        if (target == null) {
            return null;
        }
        String normalized = target.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * 使用显式 System owner 与局部 RuleView 名称构造完整复合身份。
     *
     * <p>本方法故意不提供 bare-name 重载；缺 owner 时调用方必须失败，
     * 不得通过 first/last/local-name lookup 猜测 System。</p>
     */
    static RuleViewKey parseRuleViewKey(SystemKey owner, String target) {
        if (owner == null) {
            return null;
        }
        String normalized = parseSimple(target);
        return normalized == null ? null : new RuleViewKey(owner, normalized);
    }

    /**
     * 解析严格的 system.name；必须恰好包含一个点且两段均非空。
     */
    static QualifiedInformationTarget parseQualifiedInformation(String target) {
        String normalized = parseSimple(target);
        if (normalized == null) {
            return null;
        }
        int separator = normalized.indexOf('.');
        if (separator <= 0
                || separator != normalized.lastIndexOf('.')
                || separator == normalized.length() - 1) {
            return null;
        }
        String system = parseSimple(normalized.substring(0, separator));
        String information = parseSimple(normalized.substring(separator + 1));
        return system == null || information == null
                ? null
                : new QualifiedInformationTarget(system, information);
    }

    /** 严格 qualified Information 的两个规范化 segment。 */
    static final class QualifiedInformationTarget {
        private final String system;
        private final String information;

        private QualifiedInformationTarget(String system, String information) {
            this.system = system;
            this.information = information;
        }

        String system() {
            return system;
        }

        String information() {
            return information;
        }
    }
}
