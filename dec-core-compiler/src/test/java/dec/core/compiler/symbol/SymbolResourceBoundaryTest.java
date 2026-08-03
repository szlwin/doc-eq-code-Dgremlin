package dec.core.compiler.symbol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dec.core.compiler.canonical.DocumentFormat;
import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionKind;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.raw.RawNodeBody;
import dec.core.context.model.SourceRef;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * TASK-P1-T07 Symbol Map 分配前定义数量预算 Oracle。
 */
class SymbolResourceBoundaryTest {

    /**
     * 恰好位于预算边界的定义批次必须允许进入完整两遍注册。
     */
    @Test
    void acceptsDefinitionCountAtInjectedLimit() {
        SymbolBuildResult result = new SymbolTableBuilder(
                new SymbolBuilderLimits(2)).build(new RawDefinitionSet(Arrays.asList(
                        definition(RawDefinitionKind.DATA, 0, "data"),
                        definition(RawDefinitionKind.VIEW, 1, "view"))));

        assertEquals(SymbolBuildStatus.BUILT, result.status());
        assertEquals(2, result.symbolTable().get().size());
    }

    /**
     * 第 N+1 个定义必须在创建 Symbol Map 前受控失败并绑定其 SourceRef。
     */
    @Test
    void rejectsDefinitionCountAboveInjectedLimit() {
        RawDefinition third = definition(RawDefinitionKind.SYSTEM, 2, "system");
        SymbolBuildResult result = new SymbolTableBuilder(
                new SymbolBuilderLimits(2)).build(new RawDefinitionSet(Arrays.asList(
                        definition(RawDefinitionKind.DATA, 0, "data"),
                        definition(RawDefinitionKind.VIEW, 1, "view"),
                        third)));

        assertEquals(SymbolBuildStatus.FAILED, result.status());
        assertFalse(result.symbolTable().isPresent());
        assertEquals("symbol.limit.definition-count",
                result.diagnostics().get(0).messageKey());
        assertEquals(third.sourceRef(), result.diagnostics().get(0).sourceRef());
    }

    /**
     * 非正预算必须在 Builder 执行前拒绝。
     */
    @Test
    void rejectsNonPositiveLimit() {
        assertThrows(IllegalArgumentException.class,
                () -> new SymbolBuilderLimits(0));
        assertThrows(IllegalArgumentException.class,
                () -> new SymbolBuilderLimits(-1));
    }

    private static RawDefinition definition(
            RawDefinitionKind kind,
            long ordinal,
            String name) {
        SourceRef sourceRef = new SourceRef(
                "limit-" + ordinal + ".xml",
                (int) ordinal + 1,
                1,
                "/definition[" + ordinal + "]");
        return new RawDefinition(
                kind,
                ordinal,
                sourceRef,
                Optional.<String>empty(),
                Optional.of(name),
                Collections.<String, String>emptyMap(),
                Collections.emptyList(),
                new RawNodeBody(
                        kind.name().toLowerCase(),
                        Collections.<String, String>emptyMap(),
                        Optional.<String>empty(),
                        Collections.<RawNodeBody>emptyList(),
                        sourceRef),
                DocumentFormat.XML,
                "1.0");
    }
}
