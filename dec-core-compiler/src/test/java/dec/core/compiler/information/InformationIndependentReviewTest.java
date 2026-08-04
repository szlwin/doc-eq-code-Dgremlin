package dec.core.compiler.information;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.symbol.SymbolTable;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredRegistry;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DiagnosticCode;
import dec.core.context.model.DiagnosticSeverity;
import dec.core.context.model.SourceRef;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * T09 独立 Review 对 parser 预算、精确依赖与 seam 短路的补充验证。
 */
class InformationIndependentReviewTest {

    /** 同一引用多次出现只发布一个强类型依赖事实。 */
    @Test
    void deduplicatesRepeatedInformationDependencies() {
        RawDefinitionSet definitions = InformationTestFixture.ordinaryDefinitions(
                "order.paySuccessStatus and order.paySuccessStatus");
        Object result = InformationTestFixture.compile(
                definitions,
                InformationTestFixture.symbols(definitions));

        assertEquals("COMPILED", InformationTestFixture.status(result));
        DeferredRegistry registry = (DeferredRegistry) InformationTestFixture.call(
                InformationTestFixture.compilation(result), "deferredRegistry");
        DeferredDefinition deferred = registry.find(registry.keys().get(0)).get();
        assertEquals(1, deferred.resolvedReferences().size());
    }

    /** common 多段限定引用必须按未限定/非法 lexical fail-closed。 */
    @Test
    void rejectsMultiSegmentCommonReference() {
        RawDefinitionSet definitions = InformationTestFixture.commonDefinitions(
                "payment.success.extra",
                "payment.error");
        Object result = InformationTestFixture.compile(
                definitions,
                InformationTestFixture.symbols(definitions));

        assertEquals("FAILED", InformationTestFixture.status(result));
        assertTrue(InformationTestFixture.diagnostics(result).stream().anyMatch(
                diagnostic -> "MIX-COMMON-UNQUALIFIED".equals(
                        diagnostic.code().code())));
    }

    /** 嵌套超过 128 层必须命中资源 Diagnostic，不得栈溢出。 */
    @Test
    void rejectsParserDepthBudget() {
        StringBuilder expression = new StringBuilder();
        for (int index = 0; index < 129; index++) {
            expression.append('(');
        }
        expression.append("order.paySuccessStatus");
        for (int index = 0; index < 129; index++) {
            expression.append(')');
        }
        assertLimitFailure(expression.toString());
    }

    /** token 超过 1024 时即使长度未超预算也必须稳定失败。 */
    @Test
    void rejectsParserTokenBudget() {
        StringBuilder expression = new StringBuilder("a.b");
        for (int index = 0; index < 512; index++) {
            expression.append(" or a.b");
        }
        assertTrue(expression.length() < 8192);
        assertLimitFailure(expression.toString());
    }

    /** operator 必须严格为小写，禁止静默大小写修复。 */
    @Test
    void rejectsUppercaseOperator() {
        RawDefinitionSet definitions = InformationTestFixture.ordinaryDefinitions(
                "order.paySuccessStatus AND order.paySuccessStatus");
        Object result = InformationTestFixture.compile(
                definitions,
                InformationTestFixture.symbols(definitions));

        assertEquals("FAILED", InformationTestFixture.status(result));
        assertTrue(InformationTestFixture.diagnostics(result).stream().anyMatch(
                diagnostic -> "information.expression.syntax.invalid".equals(
                        diagnostic.messageKey())));
    }

    /** parser 失败后 resolver seam 不得执行，确保阶段短路与原子失败。 */
    @Test
    void stopsBeforeResolverWhenInjectedParserFails() {
        RawDefinitionSet definitions = InformationTestFixture.ordinaryDefinitions(
                "order.paySuccessStatus");
        SymbolTable symbols = InformationTestFixture.symbols(definitions);
        AtomicInteger resolverCalls = new AtomicInteger();
        InformationExpressionParser parser = (expression, sourceRef) ->
                InformationExpressionParseResult.failed(Collections.singletonList(
                        new Diagnostic(
                                DiagnosticCode.MIX_STRUCTURE_UNKNOWN,
                                DiagnosticSeverity.ERROR,
                                "information.expression.syntax.invalid",
                                null,
                                sourceRef,
                                Collections.<SourceRef>emptyList(),
                                "独立 Review 注入 parser 失败",
                                "information-compilation")));
        InformationReferenceResolver resolver = (owner, ast, table, sourceRef) -> {
            resolverCalls.incrementAndGet();
            return InformationReferenceResolutionResult.resolved(
                    Collections.emptyList());
        };

        InformationCompilationResult result =
                new InformationCompiler(parser, resolver).compile(definitions, symbols);
        assertEquals(InformationCompilationStatus.FAILED, result.status());
        assertFalse(result.compilation().isPresent());
        assertEquals(0, resolverCalls.get());
    }

    /** 执行资源上限断言。 */
    private static void assertLimitFailure(String expression) {
        RawDefinitionSet definitions =
                InformationTestFixture.ordinaryDefinitions(expression);
        Object result = InformationTestFixture.compile(
                definitions,
                InformationTestFixture.symbols(definitions));
        assertEquals("FAILED", InformationTestFixture.status(result));
        assertTrue(InformationTestFixture.diagnostics(result).stream().anyMatch(
                diagnostic -> "information.expression.limit.exceeded".equals(
                        diagnostic.messageKey())));
    }
}
