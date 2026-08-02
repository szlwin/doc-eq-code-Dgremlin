package dec.core.compiler.canonical.yaml;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.resolver.Resolver;

/**
 * 使用固定 SnakeYAML 2.2 Resolver 语法校验标准 scalar tag。
 *
 * <p>该策略直接复用依赖版本公开的 Resolver Pattern，不复制数字语法，
 * 不构造任意精度数值或 Java 业务对象；验证通过后由 Frontend 保留原始词法。</p>
 */
final class YamlScalarLexemePolicy {
    private static final Pattern TIMESTAMP_PARTS = Pattern.compile(
            "^([0-9]{4})-([0-9]{1,2})-([0-9]{1,2})"
                    + "(?:(?:[Tt]|[ \\t]+)"
                    + "([0-9]{1,2}):([0-9]{2}):([0-9]{2})"
                    + "(?:\\.([0-9]*))?"
                    + "(?:[ \\t]*(?:Z|([-+])([0-9]{1,2})"
                    + "(?::([0-9]{2}))?))?)?$");

    private YamlScalarLexemePolicy() {
        throw new AssertionError("No instances");
    }

    /**
     * 判断标准 tag 与原始词法是否符合 SnakeYAML 2.2 Resolver 合同。
     *
     * @param tag SnakeYAML compose 阶段产生的标准 scalar tag
     * @param value 未构造、未替换的 scalar 原始词法
     * @return tag 与固定 Resolver 词法一致时返回 true
     */
    static boolean isValid(Tag tag, String value) {
        if (tag == null || value == null) {
            return false;
        }
        if (Tag.STR.equals(tag)) {
            return true;
        }
        if (Tag.BOOL.equals(tag)) {
            return Resolver.BOOL.matcher(value).matches();
        }
        if (Tag.INT.equals(tag)) {
            return Resolver.INT.matcher(value).matches();
        }
        if (Tag.FLOAT.equals(tag)) {
            return Resolver.FLOAT.matcher(value).matches();
        }
        if (Tag.NULL.equals(tag)) {
            return Resolver.NULL.matcher(value).matches()
                    || Resolver.EMPTY.matcher(value).matches();
        }
        if (Tag.TIMESTAMP.equals(tag)) {
            return Resolver.TIMESTAMP.matcher(value).matches()
                    && isValidTimestampValue(value);
        }
        return false;
    }

    /**
     * 在官方 timestamp 词法匹配后校验真实日期、时间和时区范围。
     *
     * <p>该步骤只收紧不存在的日期或越界时间，不改变 Resolver 对数字、
     * 指数、特殊浮点和 null 的语法定义。</p>
     */
    private static boolean isValidTimestampValue(String value) {
        Matcher matcher = TIMESTAMP_PARTS.matcher(value);
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
        try {
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
        } catch (NumberFormatException failure) {
            return false;
        }
    }
}
