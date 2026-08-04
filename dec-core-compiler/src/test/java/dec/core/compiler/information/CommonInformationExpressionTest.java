package dec.core.compiler.information;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredRegistry;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.InformationKey;
import dec.core.context.model.SystemKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * T09 common 跨 System expression 与结构限制合同。
 */
class CommonInformationExpressionTest {

    /** common.paySuccess/payError 必须产生四个精确依赖事实和两个 Deferred。 */
    @Test
    void compilesCommonExpressionsToFourExactDependencies() {
        RawDefinitionSet definitions = InformationTestFixture.commonDefinitions(
                "payment.success and order.paySuccessStatus",
                "payment.error and order.payErrorStatus");
        Object result = InformationTestFixture.compile(
                definitions,
                InformationTestFixture.symbols(definitions));

        assertEquals("COMPILED", InformationTestFixture.status(result));
        Object compilation = InformationTestFixture.compilation(result);
        assertNotNull(compilation);
        DeferredRegistry registry = (DeferredRegistry) InformationTestFixture.call(
                compilation, "deferredRegistry");
        assertEquals(2, registry.size());

        List<InformationKey> actual = new ArrayList<InformationKey>();
        for (DeferredDefinition deferred : registry.requiredBy(
                dec.core.context.model.RequiredStage.P3)) {
            for (dec.core.context.model.DefinitionKey key
                    : deferred.resolvedReferences()) {
                actual.add((InformationKey) key);
            }
        }
        Collections.sort(actual);
        List<InformationKey> expected = Arrays.asList(
                new InformationKey(new SystemKey("order"), "payErrorStatus"),
                new InformationKey(new SystemKey("order"), "paySuccessStatus"),
                new InformationKey(new SystemKey("payment"), "error"),
                new InformationKey(new SystemKey("payment"), "success"));
        Collections.sort(expected);
        assertEquals(expected, actual);
    }

    /** 真实 Canonical → T06 → T07 → T09 路径必须通过。 */
    @Test
    void compilesRealCanonicalCommonPath() {
        RawDefinitionSet definitions =
                InformationTestFixture.canonicalCommonDefinitions();
        Object result = InformationTestFixture.compile(
                definitions,
                InformationTestFixture.symbols(definitions));

        assertEquals("COMPILED", InformationTestFixture.status(result));
        Object compilation = InformationTestFixture.compilation(result);
        assertNotNull(compilation);
        assertEquals(2, ((Number) InformationTestFixture.call(
                compilation, "size")).intValue());
    }

    /** common 未限定引用必须使用专用 Diagnostic 拒绝。 */
    @Test
    void rejectsUnqualifiedCommonReference() {
        assertFailedWith(
                InformationTestFixture.commonDefinitions(
                        "success and order.paySuccessStatus",
                        "payment.error"),
                "MIX-COMMON-UNQUALIFIED",
                "information.common.reference.unqualified");
    }

    /** common qualified 但未知引用必须报告 MIX-REF-UNKNOWN。 */
    @Test
    void rejectsUnknownCommonReference() {
        assertFailedWith(
                InformationTestFixture.commonDefinitions(
                        "payment.missing",
                        "payment.error"),
                "MIX-REF-UNKNOWN",
                "information.reference.unknown");
    }

    /** common Information 不得携带 view/rule/change-data 等成员。 */
    @Test
    void rejectsForbiddenCommonInformationMember() {
        assertFailedWith(
                InformationTestFixture.commonInformationWithMember(),
                "MIX-COMMON-MEMBER",
                "information.common.member.invalid");
    }

    /** common System 的 data/view/rule-file sections 必须保持空。 */
    @Test
    void rejectsCommonSystemDataMember() {
        RawDefinitionSet invalid =
                InformationTestFixture.commonSystemWithDataMember();
        RawDefinitionSet valid = InformationTestFixture.commonDefinitions(
                "payment.success",
                "payment.error");
        Object result = InformationTestFixture.compile(
                invalid,
                InformationTestFixture.symbols(valid));

        assertEquals("FAILED", InformationTestFixture.status(result));
        assertEquals(null, InformationTestFixture.compilation(result));
        assertTrue(InformationTestFixture.diagnostics(result).stream().anyMatch(
                diagnostic -> "MIX-COMMON-MEMBER".equals(
                        diagnostic.code().code())));
    }

    /** common 间接循环在 T09 只形成 Deferred，不得提前执行 P3 循环检查。 */
    @Test
    void allowsIndirectCommonCycleAsDeferredFacts() {
        RawDefinitionSet definitions = InformationTestFixture.commonDefinitions(
                "common.payError",
                "common.paySuccess");
        Object result = InformationTestFixture.compile(
                definitions,
                InformationTestFixture.symbols(definitions));

        assertEquals("COMPILED", InformationTestFixture.status(result));
        DeferredRegistry registry = (DeferredRegistry) InformationTestFixture.call(
                InformationTestFixture.compilation(result), "deferredRegistry");
        assertEquals(2, registry.size());
    }

    /** common 混合有效与无效表达式时不得发布有效项的部分结果。 */
    @Test
    void rejectsWholeBatchWithoutPartialCommonPublication() {
        RawDefinitionSet definitions = InformationTestFixture.commonDefinitions(
                "payment.success and order.paySuccessStatus",
                "missing");
        Object result = InformationTestFixture.compile(
                definitions,
                InformationTestFixture.symbols(definitions));

        assertEquals("FAILED", InformationTestFixture.status(result));
        assertEquals(null, InformationTestFixture.compilation(result));
        assertFalse(InformationTestFixture.diagnostics(result).isEmpty());
    }

    /** Deferred 与 dependency collections 必须不可修改。 */
    @Test
    void freezesCommonDeferredAndDependencies() {
        RawDefinitionSet definitions = InformationTestFixture.commonDefinitions(
                "payment.success and order.paySuccessStatus",
                "payment.error and order.payErrorStatus");
        Object result = InformationTestFixture.compile(
                definitions,
                InformationTestFixture.symbols(definitions));
        assertEquals("COMPILED", InformationTestFixture.status(result));
        DeferredRegistry registry = (DeferredRegistry) InformationTestFixture.call(
                InformationTestFixture.compilation(result), "deferredRegistry");
        DeferredDefinition deferred = registry.find(registry.keys().get(0)).get();

        assertThrows(UnsupportedOperationException.class,
                () -> registry.keys().add(registry.keys().get(0)));
        assertThrows(UnsupportedOperationException.class,
                () -> deferred.resolvedReferences().add(
                        new InformationKey(new SystemKey("payment"), "success")));
    }

    /** 执行失败断言并核对稳定 code/messageKey。 */
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
        assertTrue(diagnostics.stream().anyMatch(diagnostic ->
                expectedCode.equals(diagnostic.code().code())
                        && expectedMessageKey.equals(diagnostic.messageKey())),
                diagnostics.toString());
    }
}
