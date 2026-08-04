package dec.core.compiler.information;

import static org.junit.jupiter.api.Assertions.fail;

import dec.core.compiler.canonical.CanonicalDocumentNode;
import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.raw.RawBuildResult;
import dec.core.compiler.raw.RawBuildStatus;
import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionBuilder;
import dec.core.compiler.raw.RawDefinitionKind;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.raw.RawNodeBody;
import dec.core.compiler.raw.RawReference;
import dec.core.compiler.symbol.SymbolBuildResult;
import dec.core.compiler.symbol.SymbolBuildStatus;
import dec.core.compiler.symbol.SymbolTable;
import dec.core.compiler.symbol.SymbolTableBuilder;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.SourceRef;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * T09 反射式 RED 与 GREEN 共用夹具。
 *
 * <p>测试在生产边界尚不存在时仍可按 Java 8 编译；缺失或未实现的 T09
 * 类型会被转换为断言失败，而不是测试编译错误或反射 Error。</p>
 */
final class InformationTestFixture {
    private static final String COMPILER_CLASS =
            "dec.core.compiler.information.InformationCompiler";

    private InformationTestFixture() {
    }

    /** 使用公开默认构造器执行 T09 编译入口。 */
    static Object compile(RawDefinitionSet definitions, SymbolTable symbols) {
        try {
            Class<?> compilerType = Class.forName(COMPILER_CLASS);
            Object compiler = compilerType.getConstructor().newInstance();
            Method compile = compilerType.getMethod(
                    "compile",
                    RawDefinitionSet.class,
                    SymbolTable.class);
            return compile.invoke(compiler, definitions, symbols);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause() == null
                    ? exception
                    : exception.getCause();
            fail("T09 编译入口不得抛出未接管异常", cause);
            return null;
        } catch (ReflectiveOperationException exception) {
            fail("T09 InformationCompiler 生产边界尚未实现", exception);
            return null;
        }
    }

    /** 调用无参数方法并保持反射失败为断言失败。 */
    static Object call(Object target, String methodName) {
        try {
            return target.getClass().getMethod(methodName).invoke(target);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause() == null
                    ? exception
                    : exception.getCause();
            fail("调用 " + methodName + " 时不得抛出异常", cause);
            return null;
        } catch (ReflectiveOperationException exception) {
            fail("缺少 T09 合同方法: " + methodName, exception);
            return null;
        }
    }

    /** 将结果状态转为稳定枚举名称。 */
    static String status(Object result) {
        return String.valueOf(call(result, "status"));
    }

    /** 返回结果携带的 Diagnostic。 */
    @SuppressWarnings("unchecked")
    static List<Diagnostic> diagnostics(Object result) {
        return (List<Diagnostic>) call(result, "diagnostics");
    }

    /** 返回成功结果中的 InformationCompilation。 */
    static Object compilation(Object result) {
        @SuppressWarnings("unchecked")
        Optional<Object> compilation =
                (Optional<Object>) call(result, "compilation");
        return compilation.orElse(null);
    }

    /** 按完整合法定义构建 T07 SymbolTable。 */
    static SymbolTable symbols(RawDefinitionSet definitions) {
        SymbolBuildResult result = new SymbolTableBuilder().build(definitions);
        if (result.status() != SymbolBuildStatus.BUILT) {
            throw new AssertionError(
                    "T09 夹具必须先通过 T07: " + result.diagnostics());
        }
        return result.symbolTable().get();
    }

    /** 构造普通 System 的本地 expression 定义集。 */
    static RawDefinitionSet ordinaryDefinitions(String expression) {
        List<RawDefinition> definitions = new ArrayList<RawDefinition>();
        definitions.add(system(0, "order", emptySystemChildren("order", 1)));
        definitions.add(information(1, "order", "paySuccessStatus", null,
                attrs("name", "paySuccessStatus"), emptyChildren(), 10));
        definitions.add(information(2, "order", "paySuccess", expression,
                attrs("name", "paySuccess", "expression", expression),
                emptyChildren(), 11));
        definitions.add(system(3, "payment", emptySystemChildren("payment", 20)));
        definitions.add(information(4, "payment", "success", null,
                attrs("name", "success"), emptyChildren(), 21));
        definitions.add(information(5, "payment", "error", null,
                attrs("name", "error"), emptyChildren(), 22));
        return new RawDefinitionSet(definitions);
    }

    /** 构造一个 Raw owner 与 T07 快照不一致的输入。 */
    static RawDefinitionSet ownerMismatchDefinitions() {
        RawDefinitionSet valid = ordinaryDefinitions("order.paySuccessStatus");
        List<RawDefinition> changed = new ArrayList<RawDefinition>(
                valid.definitions());
        changed.set(2, information(2, "payment", "paySuccess",
                "payment.success",
                attrs("name", "paySuccess", "expression", "payment.success"),
                emptyChildren(), 11));
        return new RawDefinitionSet(changed);
    }

    /** 构造 payment/order/common 的完整 expression 定义集。 */
    static RawDefinitionSet commonDefinitions(
            String paySuccessExpression,
            String payErrorExpression) {
        List<RawDefinition> definitions = new ArrayList<RawDefinition>();
        definitions.add(system(0, "payment", emptySystemChildren("payment", 1)));
        definitions.add(information(1, "payment", "success", null,
                attrs("name", "success"), emptyChildren(), 2));
        definitions.add(information(2, "payment", "error", null,
                attrs("name", "error"), emptyChildren(), 3));
        definitions.add(system(3, "order", emptySystemChildren("order", 10)));
        definitions.add(information(4, "order", "paySuccessStatus", null,
                attrs("name", "paySuccessStatus"), emptyChildren(), 11));
        definitions.add(information(5, "order", "payErrorStatus", null,
                attrs("name", "payErrorStatus"), emptyChildren(), 12));
        definitions.add(system(6, "common", emptySystemChildren("common", 20)));
        definitions.add(information(7, "common", "paySuccess",
                paySuccessExpression,
                attrs("name", "paySuccess", "expression", paySuccessExpression),
                emptyChildren(), 21));
        definitions.add(information(8, "common", "payError",
                payErrorExpression,
                attrs("name", "payError", "expression", payErrorExpression),
                emptyChildren(), 22));
        return new RawDefinitionSet(definitions);
    }

    /** 构造 common Information 含被禁止成员的输入。 */
    static RawDefinitionSet commonInformationWithMember() {
        RawDefinitionSet valid = commonDefinitions(
                "payment.success and order.paySuccessStatus",
                "payment.error and order.payErrorStatus");
        List<RawDefinition> changed = new ArrayList<RawDefinition>(
                valid.definitions());
        changed.set(7, information(7, "common", "paySuccess",
                "payment.success",
                attrs("name", "paySuccess", "expression", "payment.success",
                        "view-ref", "PaymentView"),
                Collections.singletonList(body(
                        "change-data",
                        attrs("name", "forbidden"),
                        emptyChildren(),
                        ref("systems.xml", 24))),
                21));
        return new RawDefinitionSet(changed);
    }

    /** 构造 common System 自身包含被禁止 data-info 成员的输入。 */
    static RawDefinitionSet commonSystemWithDataMember() {
        RawDefinitionSet valid = commonDefinitions(
                "payment.success",
                "payment.error");
        List<RawDefinition> changed = new ArrayList<RawDefinition>(
                valid.definitions());
        RawNodeBody dataRef = body(
                "data-ref",
                attrs("ref", "payment"),
                emptyChildren(),
                ref("systems.xml", 20));
        RawNodeBody dataInfo = body(
                "data-info",
                attrs(),
                Collections.singletonList(dataRef),
                ref("systems.xml", 19));
        changed.set(6, system(6, "common",
                Collections.singletonList(dataInfo)));
        return new RawDefinitionSet(changed);
    }

    /** 通过真实 CanonicalDocumentNode 与 T06 Builder 构造 common 示例。 */
    static RawDefinitionSet canonicalCommonDefinitions() {
        CanonicalDocumentNode systems = node(
                "systems.xml",
                "/systems",
                "systems",
                attrs(),
                systemNode("systems.xml", "/systems/system[1]", "payment",
                        informationNode("systems.xml", 3, "success", null),
                        informationNode("systems.xml", 4, "error", null)),
                systemNode("systems.xml", "/systems/system[2]", "order",
                        informationNode("systems.xml", 11, "paySuccessStatus", null),
                        informationNode("systems.xml", 12, "payErrorStatus", null)),
                systemNode("systems.xml", "/systems/system[3]", "common",
                        informationNode("systems.xml", 21, "paySuccess",
                                "payment.success and order.paySuccessStatus"),
                        informationNode("systems.xml", 22, "payError",
                                "payment.error and order.payErrorStatus")));
        RawBuildResult result = new RawDefinitionBuilder().build(
                Collections.singletonList(systems));
        if (result.status() != RawBuildStatus.BUILT) {
            throw new AssertionError(
                    "Canonical common 夹具必须通过 T06: "
                            + result.diagnostics());
        }
        return result.rawDefinitionSet().get();
    }

    /** 构造 Canonical System，并显式保留空 data/view/rule sections。 */
    private static CanonicalDocumentNode systemNode(
            String sourceId,
            String path,
            String name,
            CanonicalDocumentNode... information) {
        return node(sourceId, path, "system", attrs("name", name),
                node(sourceId, path + "/data-info", "data-info", attrs()),
                node(sourceId, path + "/view-info", "view-info", attrs()),
                node(sourceId, path + "/rule-file-info", "rule-file-info", attrs()),
                node(sourceId, path + "/information-info", "information-info",
                        attrs(), information));
    }

    /** 构造 Canonical Information。 */
    private static CanonicalDocumentNode informationNode(
            String sourceId,
            int line,
            String name,
            String expression) {
        Map<String, String> values = expression == null
                ? attrs("name", name)
                : attrs("name", name, "expression", expression);
        return node(sourceId,
                "/systems/system/information-info/information[" + line + "]",
                "information",
                values);
    }

    /** 构造普通或 common System RawDefinition。 */
    private static RawDefinition system(
            long ordinal,
            String name,
            List<RawNodeBody> children) {
        return definition(
                RawDefinitionKind.SYSTEM,
                ordinal,
                null,
                name,
                attrs("name", name),
                body("system", attrs("name", name), children,
                        ref("systems.xml", (int) ordinal + 1)));
    }

    /** 构造 Information RawDefinition。 */
    private static RawDefinition information(
            long ordinal,
            String owner,
            String name,
            String expression,
            Map<String, String> attributes,
            List<RawNodeBody> children,
            int line) {
        return definition(
                RawDefinitionKind.INFORMATION,
                ordinal,
                owner,
                name,
                attributes,
                body("information", attributes, children,
                        ref("systems.xml", line)));
    }

    /** 构造符合 T06 公开矩阵的 RawDefinition。 */
    private static RawDefinition definition(
            RawDefinitionKind kind,
            long ordinal,
            String owner,
            String name,
            Map<String, String> attributes,
            RawNodeBody body) {
        return new RawDefinition(
                kind,
                ordinal,
                body.sourceRef(),
                owner == null ? Optional.<String>empty() : Optional.of(owner),
                name == null ? Optional.<String>empty() : Optional.of(name),
                attributes,
                Collections.<RawReference>emptyList(),
                body,
                DocumentFormat.XML,
                "1.0");
    }

    /** 为合法 System 生成三个空 section，供 common 完整性校验复用。 */
    private static List<RawNodeBody> emptySystemChildren(
            String system,
            int line) {
        return Arrays.asList(
                body("data-info", attrs(), emptyChildren(),
                        ref("systems.xml", line)),
                body("view-info", attrs(), emptyChildren(),
                        ref("systems.xml", line + 1)),
                body("rule-file-info", attrs(), emptyChildren(),
                        ref("systems.xml", line + 2)),
                body("information-info", attrs(), emptyChildren(),
                        ref("systems.xml", line + 3)));
    }

    /** 构造 RawNodeBody。 */
    private static RawNodeBody body(
            String name,
            Map<String, String> attributes,
            List<RawNodeBody> children,
            SourceRef sourceRef) {
        return new RawNodeBody(
                name,
                attributes,
                Optional.<String>empty(),
                children,
                sourceRef);
    }

    /** 构造 CanonicalDocumentNode。 */
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

    /** 构造稳定顺序属性。 */
    private static Map<String, String> attrs(String... values) {
        Map<String, String> attributes = new LinkedHashMap<String, String>();
        for (int index = 0; index < values.length; index += 2) {
            attributes.put(values[index], values[index + 1]);
        }
        return attributes;
    }

    /** 返回空不可变 children。 */
    private static List<RawNodeBody> emptyChildren() {
        return Collections.emptyList();
    }

    /** 构造稳定来源位置。 */
    private static SourceRef ref(String sourceId, int line) {
        return new SourceRef(sourceId, line, 1,
                "/systems/definition[" + line + "]");
    }
}
