package dec.core.compiler.information;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.symbol.SymbolTable;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKind;
import dec.core.context.model.DeferredRegistry;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.InformationKey;
import dec.core.context.model.RequiredStage;
import dec.core.context.model.SystemKey;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * T09 普通 System-owned Information owner、AST 与 Deferred 合同。
 */
class InformationOwnershipTest {

    /** 同 System qualified 引用必须生成精确依赖与 P3 Deferred。 */
    @Test
    void compilesLocalQualifiedExpressionToP3Deferred() {
        RawDefinitionSet definitions = InformationTestFixture.ordinaryDefinitions(
                "order.paySuccessStatus");
        Object result = InformationTestFixture.compile(
                definitions,
                InformationTestFixture.symbols(definitions));

        assertEquals("COMPILED", InformationTestFixture.status(result));
        Object compilation = InformationTestFixture.compilation(result);
        assertNotNull(compilation);
        assertEquals(1, ((Number) InformationTestFixture.call(
                compilation, "size")).intValue());

        DeferredRegistry registry = (DeferredRegistry) InformationTestFixture.call(
                compilation, "deferredRegistry");
        assertEquals(1, registry.size());
        DeferredDefinition deferred = registry.keys().stream()
                .map(key -> registry.find(key).get())
                .findFirst().get();
        assertEquals(RequiredStage.P3, deferred.requiredStage());
        assertEquals(DeferredKind.INFORMATION, deferred.kind());
        assertEquals("information-expression-evaluation", deferred.reasonCode());
        assertEquals("information-expression-ast/v1", deferred.body().format());
        assertEquals("ref(order.paySuccessStatus)", deferred.body().value());
        assertEquals(Collections.singletonList(new InformationKey(
                new SystemKey("order"), "paySuccessStatus")),
                deferred.resolvedReferences());
    }

    /** 普通 System 跨 owner 引用必须使用专用 Diagnostic 拒绝。 */
    @Test
    void rejectsOrdinaryCrossSystemReference() {
        assertFailedWith(
                InformationTestFixture.ordinaryDefinitions("payment.success"),
                "MIX-INFORMATION-CROSS-SYSTEM",
                "information.reference.cross-system");
    }

    /** 普通表达式未限定引用必须 fail-closed。 */
    @Test
    void rejectsUnqualifiedOrdinaryReference() {
        assertFailedWith(
                InformationTestFixture.ordinaryDefinitions("paySuccessStatus"),
                "MIX-INFORMATION-OWNER",
                "information.owner.invalid");
    }

    /** qualified 但不存在的 Information 必须报告 unknown。 */
    @Test
    void rejectsUnknownQualifiedInformation() {
        assertFailedWith(
                InformationTestFixture.ordinaryDefinitions("order.missing"),
                "MIX-REF-UNKNOWN",
                "information.reference.unknown");
    }

    /** Raw owner 与 Symbol 快照身份不一致时不得误发布结果。 */
    @Test
    void rejectsRawOwnerIdentityMismatch() {
        RawDefinitionSet valid = InformationTestFixture.ordinaryDefinitions(
                "order.paySuccessStatus");
        RawDefinitionSet changed = InformationTestFixture.ownerMismatchDefinitions();
        Object result = InformationTestFixture.compile(
                changed,
                InformationTestFixture.symbols(valid));

        assertEquals("FAILED", InformationTestFixture.status(result));
        assertEquals(null, InformationTestFixture.compilation(result));
        assertTrue(InformationTestFixture.diagnostics(result).stream().anyMatch(
                diagnostic -> "MIX-INFORMATION-OWNER".equals(
                        diagnostic.code().code())));
    }

    /** 多个错误必须稳定聚合且失败结果不发布部分 compilation。 */
    @Test
    void aggregatesStableDiagnosticsWithoutPartialPublication() {
        RawDefinitionSet first = InformationTestFixture.commonDefinitions(
                "payment.missing and bare",
                "order.missing");
        SymbolTable symbols = InformationTestFixture.symbols(first);
        Object resultA = InformationTestFixture.compile(first, symbols);
        Object resultB = InformationTestFixture.compile(first, symbols);

        assertEquals("FAILED", InformationTestFixture.status(resultA));
        assertEquals(null, InformationTestFixture.compilation(resultA));
        List<Diagnostic> diagnosticsA = InformationTestFixture.diagnostics(resultA);
        List<Diagnostic> diagnosticsB = InformationTestFixture.diagnostics(resultB);
        assertTrue(diagnosticsA.size() >= 3);
        assertEquals(diagnosticsA, diagnosticsB);
        List<Diagnostic> sorted = new ArrayList<Diagnostic>(diagnosticsA);
        Collections.sort(sorted);
        assertEquals(sorted, diagnosticsA);
    }

    /** and 优先级与括号必须产生稳定 canonical AST。 */
    @Test
    void preservesAndPrecedenceAndParentheses() {
        RawDefinitionSet definitions = InformationTestFixture.ordinaryDefinitions(
                "order.paySuccessStatus or (order.paySuccessStatus and order.paySuccessStatus)");
        Object result = InformationTestFixture.compile(
                definitions,
                InformationTestFixture.symbols(definitions));

        assertEquals("COMPILED", InformationTestFixture.status(result));
        DeferredRegistry registry = (DeferredRegistry) InformationTestFixture.call(
                InformationTestFixture.compilation(result), "deferredRegistry");
        String canonical = registry.find(registry.keys().get(0)).get()
                .body().value();
        assertEquals(
                "or(ref(order.paySuccessStatus),and(ref(order.paySuccessStatus),ref(order.paySuccessStatus)))",
                canonical);
    }

    /** 超出 expression 字符预算必须稳定失败而非递归或内存异常。 */
    @Test
    void rejectsExpressionLengthLimit() {
        StringBuilder expression = new StringBuilder("order.paySuccessStatus");
        while (expression.length() <= 8192) {
            expression.append(" or order.paySuccessStatus");
        }
        assertFailedWith(
                InformationTestFixture.ordinaryDefinitions(expression.toString()),
                null,
                "information.expression.limit.exceeded");
    }

    /** T09 类型必须不可变且不得暴露求值、缓存或全局 current 状态。 */
    @Test
    void keepsAstAndCompilerImmutableWithoutEvaluationState() throws Exception {
        Class<?> compiler = assertDoesNotThrow(() -> Class.forName(
                "dec.core.compiler.information.InformationCompiler"));
        Class<?> ast = assertDoesNotThrow(() -> Class.forName(
                "dec.core.compiler.information.InformationExpressionAst"));

        assertTrue(Modifier.isFinal(compiler.getModifiers()));
        assertTrue(Modifier.isFinal(ast.getModifiers()));
        for (Field field : compiler.getDeclaredFields()) {
            if (field.isSynthetic() || field.getName().startsWith("$jacoco")) {
                continue;
            }
            assertFalse(Modifier.isStatic(field.getModifiers())
                    && !Modifier.isFinal(field.getModifiers()),
                    "InformationCompiler 不得包含 static mutable state");
        }
        for (Field field : ast.getDeclaredFields()) {
            assertTrue(Modifier.isFinal(field.getModifiers()),
                    "AST 字段必须全部 final: " + field.getName());
        }
        for (Method method : ast.getMethods()) {
            String name = method.getName().toLowerCase();
            assertFalse(name.contains("evaluate")
                    || name.contains("execute")
                    || name.contains("cache")
                    || name.contains("current"));
        }

        RawDefinitionSet definitions = InformationTestFixture.ordinaryDefinitions(
                "order.paySuccessStatus");
        Object result = InformationTestFixture.compile(
                definitions,
                InformationTestFixture.symbols(definitions));
        assertEquals("COMPILED", InformationTestFixture.status(result));
        Object compilation = InformationTestFixture.compilation(result);
        @SuppressWarnings("unchecked")
        List<Object> expressions = (List<Object>) InformationTestFixture.call(
                compilation, "expressions");
        assertThrows(UnsupportedOperationException.class,
                () -> expressions.add(new Object()));
    }

    /** 执行失败断言并检查稳定 code/messageKey。 */
    private static void assertFailedWith(
            RawDefinitionSet definitions,
            String expectedCode,
            String expectedMessageKey) {
        Object result = InformationTestFixture.compile(
                definitions,
                InformationTestFixture.symbols(definitions));
        assertEquals("FAILED", InformationTestFixture.status(result));
        assertEquals(null, InformationTestFixture.compilation(result));
        List<Diagnostic> diagnostics = InformationTestFixture.diagnostics(result);
        assertFalse(diagnostics.isEmpty());
        assertTrue(diagnostics.stream().anyMatch(diagnostic ->
                (expectedCode == null
                        || expectedCode.equals(diagnostic.code().code()))
                        && expectedMessageKey.equals(diagnostic.messageKey())),
                diagnostics.toString());
    }
}
