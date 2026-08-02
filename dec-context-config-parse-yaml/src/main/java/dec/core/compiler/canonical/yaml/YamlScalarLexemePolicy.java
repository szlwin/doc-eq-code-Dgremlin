package dec.core.compiler.canonical.yaml;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.yaml.snakeyaml.nodes.Tag;

/**
 * 冻结 YAML 标准 scalar tag 的无对象构造词法合同。
 *
 * <p>该策略只校验原始词法，不把 scalar 构造成 Java 对象，
 * 因而既能拒绝显式非法 typed tag，也保持 Canonical 中的原始文本事实。</p>
 */
final class YamlScalarLexemePolicy {
    private static final Pattern BOOL = Pattern.compile(
            "^(?:yes|Yes|YES|no|No|NO|true|True|TRUE|false|False|FALSE|"
                    + "on|On|ON|off|Off|OFF)$");
    private static final Pattern NULL = Pattern.compile(
            "^(?:~|null|Null|NULL|)$");
    private static final Pattern INT = Pattern.compile(
            "^[-+]?(?:0|0b[0-1_]+|0[0-7_]+|0x[0-9a-fA-F_]+|"
                    + "[1-9][0-9_]*|[1-9][0-9_]*(?::[0-5]?[0-9])+)$");
    private static final Pattern DECIMAL_FLOAT = Pattern.compile(
            "^[-+]?(?:(?:(?:[0-9][0-9_]*)?\\.[0-9_]+|"
                    + "[0-9][0-9_]*\\.)(?:[eE][-+][0-9]+)?|"
                    + "[0-9][0-9_]*(?:[eE][-+][0-9]+))$");
    private static final Pattern SPECIAL_FLOAT = Pattern.compile(
            "^[-+]?\\.(?:inf|Inf|INF|nan|NaN|NAN)$");
    private static final Pattern SEXAGESIMAL_FLOAT = Pattern.compile(
            "^[-+]?[0-9][0-9_]*(?::[0-5]?[0-9])+\\.[0-9_]+$");
    private static final Pattern TIMESTAMP = Pattern.compile(
            "^([0-9]{4})-([0-9]{1,2})-([0-9]{1,2})"
                    + "(?:(?:[Tt]|[ \\t]+)"
                    + "([0-9]{1,2}):([0-9]{2}):([0-9]{2})"
                    + "(?:\\.([0-9]+))?"
                    + "(?:[ \\t]*(?:Z|([-+])([0-9]{1,2})"
                    + "(?::([0-9]{2}))?))?)?$");

    private YamlScalarLexemePolicy() {
        throw new AssertionError("No instances");
    }

    /**
     * 判断指定标准 tag 与原始词法是否符合 R21 来源事实合同。
     *
     * @param tag SnakeYAML compose 阶段产生的标准 scalar tag
     * @param value 未构造、未替换的 scalar 原始词法
     * @return tag 与词法一致时返回 true
     */
    static boolean isValid(Tag tag, String value) {
        if (tag == null || value == null) {
            return false;
        }
        if (Tag.STR.equals(tag)) {
            return true;
        }
        if (Tag.BOOL.equals(tag)) {
            return BOOL.matcher(value).matches();
        }
        if (Tag.INT.equals(tag)) {
            return INT.matcher(value).matches();
        }
        if (Tag.FLOAT.equals(tag)) {
            return isValidFloat(value);
        }
        if (Tag.NULL.equals(tag)) {
            return NULL.matcher(value).matches();
        }
        if (Tag.TIMESTAMP.equals(tag)) {
            return isValidTimestamp(value);
        }
        return false;
    }

    /**
     * 校验普通、小数指数、特殊值和六十进制浮点词法。
     */
    private static boolean isValidFloat(String value) {
        if (SPECIAL_FLOAT.matcher(value).matches()
                || SEXAGESIMAL_FLOAT.matcher(value).matches()) {
            return true;
        }
        if (!DECIMAL_FLOAT.matcher(value).matches()) {
            return false;
        }
        try {
            new BigDecimal(value.replace("_", ""));
            return true;
        } catch (NumberFormatException failure) {
            return false;
        }
    }

    /**
     * 校验 timestamp 的结构、真实日期、时间和时区范围。
     */
    private static boolean isValidTimestamp(String value) {
        Matcher matcher = TIMESTAMP.matcher(value);
        if (!matcher.matches()) {
            return false;
        }
        try {
            LocalDate.of(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3)));
        } catch (DateTimeException | NumberFormatException failure) {
            return false;
        }

        if (matcher.group(4) == null) {
            return true;
        }
        int hour = Integer.parseInt(matcher.group(4));
        int minute = Integer.parseInt(matcher.group(5));
        int second = Integer.parseInt(matcher.group(6));
        if (hour > 23 || minute > 59 || second > 59) {
            return false;
        }
        if (matcher.group(9) == null) {
            return true;
        }
        int offsetHour = Integer.parseInt(matcher.group(9));
        int offsetMinute = matcher.group(10) == null
                ? 0
                : Integer.parseInt(matcher.group(10));
        return offsetHour <= 23 && offsetMinute <= 59;
    }
}
