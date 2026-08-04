package dec.core.compiler.information;

import dec.core.context.model.SystemKey;
import java.util.Optional;

/**
 * 统一 Information 编译阶段的 canonical System 身份判定。
 */
final class InformationIdentity {
    private static final String COMMON_SYSTEM = "common";

    private InformationIdentity() {
    }

    /**
     * 仅根据规范化后的 SystemKey 判定 common 身份，避免 raw lexical 与权限规则分裂。
     */
    static boolean isCommon(SystemKey key) {
        return key != null && COMMON_SYSTEM.equals(key.name());
    }

    /**
     * 将原始 System name 安全转换为 canonical 身份；非法 lexical 仅返回 false。
     */
    static boolean isCommonSystemName(Optional<String> rawName) {
        return rawName.isPresent() && isCommon(systemKey(rawName.get()));
    }

    /**
     * 将原始 owner token 安全转换为 canonical 身份；原始字符串不会被修改。
     */
    static boolean isCommonOwner(Optional<String> rawOwner) {
        return rawOwner.isPresent() && isCommon(systemKey(rawOwner.get()));
    }

    /**
     * 安全创建 SystemKey，输入不合法时返回 null，由调用方继续生成稳定 Diagnostic。
     */
    private static SystemKey systemKey(String rawValue) {
        try {
            return new SystemKey(rawValue);
        } catch (IllegalArgumentException failure) {
            return null;
        }
    }
}
