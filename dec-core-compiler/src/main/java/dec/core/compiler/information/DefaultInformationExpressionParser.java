package dec.core.compiler.information;

import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.SourceRef;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Java 8 单遍 tokenizer 与递归下降 Information expression parser。
 */
final class DefaultInformationExpressionParser
        implements InformationExpressionParser {
    private static final int MAX_LENGTH = 8192;
    private static final int MAX_TOKENS = 1024;
    private static final int MAX_DEPTH = 128;

    @Override
    public InformationExpressionParseResult parse(
            String expression,
            SourceRef sourceRef) {
        if (expression == null || expression.length() > MAX_LENGTH) {
            return failed(
                    "information.expression.limit.exceeded",
                    sourceRef,
                    "expression 长度不得超过 8192");
        }
        try {
            ParserState state = new ParserState(tokenize(expression));
            /* 根表达式不计入括号嵌套深度，只有进入左括号时才递增。 */
            InformationExpressionAst ast = state.parseExpression(0);
            if (state.hasNext()) {
                throw new ParseFailure("unexpected trailing token");
            }
            return InformationExpressionParseResult.parsed(ast);
        } catch (LimitFailure failure) {
            return failed(
                    "information.expression.limit.exceeded",
                    sourceRef,
                    "expression token 或嵌套深度超过预算");
        } catch (RuntimeException failure) {
            return failed(
                    "information.expression.syntax.invalid",
                    sourceRef,
                    "请使用 qualified Information、and、or 与括号");
        }
    }

    /** 将输入切分为引用、operator 与括号；禁止隐式字符修复。 */
    private static List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        for (int index = 0; index < expression.length(); index++) {
            char value = expression.charAt(index);
            if (Character.isWhitespace(value)) {
                flush(current, tokens);
            } else if (value == '(' || value == ')') {
                flush(current, tokens);
                tokens.add(String.valueOf(value));
            } else {
                current.append(value);
            }
            if (tokens.size() > MAX_TOKENS) {
                throw new LimitFailure();
            }
        }
        flush(current, tokens);
        if (tokens.isEmpty()) {
            throw new ParseFailure("expression is empty");
        }
        if (tokens.size() > MAX_TOKENS) {
            throw new LimitFailure();
        }
        return Collections.unmodifiableList(tokens);
    }

    /** 将当前 lexical token 加入 token 序列。 */
    private static void flush(
            StringBuilder current,
            List<String> tokens) {
        if (current.length() > 0) {
            tokens.add(current.toString());
            current.setLength(0);
        }
    }

    /** 创建单个 parser 失败结果。 */
    private static InformationExpressionParseResult failed(
            String messageKey,
            SourceRef sourceRef,
            String hint) {
        return InformationExpressionParseResult.failed(
                Collections.singletonList(InformationDiagnostics.create(
                        DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                        messageKey,
                        null,
                        sourceRef,
                        hint)));
    }

    /** 单次 parse 调用拥有的游标状态。 */
    private static final class ParserState {
        private final List<String> tokens;
        private int index;

        private ParserState(List<String> tokens) {
            this.tokens = tokens;
        }

        /** expression 入口使用 or 最低优先级。 */
        private InformationExpressionAst parseExpression(int depth) {
            checkDepth(depth);
            return parseOr(depth);
        }

        /** or 左结合，且低于 and。 */
        private InformationExpressionAst parseOr(int depth) {
            InformationExpressionAst left = parseAnd(depth);
            while (accept("or")) {
                left = InformationExpressionAst.or(left, parseAnd(depth));
            }
            return left;
        }

        /** and 左结合，且高于 or。 */
        private InformationExpressionAst parseAnd(int depth) {
            InformationExpressionAst left = parsePrimary(depth);
            while (accept("and")) {
                left = InformationExpressionAst.and(left, parsePrimary(depth));
            }
            return left;
        }

        /** primary 只允许限定引用或完整括号表达式。 */
        private InformationExpressionAst parsePrimary(int depth) {
            checkDepth(depth);
            if (accept("(")) {
                InformationExpressionAst nested = parseExpression(depth + 1);
                require(")");
                return nested;
            }
            String token = next();
            if ("and".equals(token)
                    || "or".equals(token)
                    || ")".equals(token)) {
                throw new ParseFailure("expected reference");
            }
            return InformationExpressionAst.reference(token);
        }

        /** 接受当前精确 token。 */
        private boolean accept(String expected) {
            if (hasNext() && expected.equals(tokens.get(index))) {
                index++;
                return true;
            }
            return false;
        }

        /** 强制出现指定 token。 */
        private void require(String expected) {
            if (!accept(expected)) {
                throw new ParseFailure("missing " + expected);
            }
        }

        /** 返回下一个 token。 */
        private String next() {
            if (!hasNext()) {
                throw new ParseFailure("unexpected end");
            }
            return tokens.get(index++);
        }

        /** 判断是否仍有 token。 */
        private boolean hasNext() {
            return index < tokens.size();
        }

        /** 在进入下一层括号前执行硬深度预算。 */
        private static void checkDepth(int depth) {
            if (depth > MAX_DEPTH) {
                throw new LimitFailure();
            }
        }
    }

    /** parser 语法失败的轻量内部控制流。 */
    private static final class ParseFailure extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private ParseFailure(String message) {
            super(message);
        }
    }

    /** parser 资源预算失败的轻量内部控制流。 */
    private static final class LimitFailure extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
