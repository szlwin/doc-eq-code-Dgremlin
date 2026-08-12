package dec.core.compiler.modelaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dec.core.compiler.raw.RawDefinition;
import dec.core.compiler.raw.RawDefinitionKind;
import dec.core.compiler.raw.RawDefinitionSet;
import dec.core.compiler.symbol.SymbolTable;
import dec.core.context.model.AccessCompilationStatus;
import dec.core.context.model.AccessOperation;
import dec.core.context.model.CompiledModelAccessRule;
import dec.core.context.model.CompiledTargetBinding;
import dec.core.context.model.ModelAccessPolicyIndex;
import dec.core.context.model.ModelAccessRuleKey;
import dec.core.context.model.ModelPath;
import dec.core.context.model.RuntimeBindingPlan;
import dec.core.context.model.SourceRef;
import dec.core.context.model.SystemKey;
import dec.core.context.model.TargetKey;
import dec.core.context.model.ViewKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 DEV-03 exact static authorization oracle。 */
class P2ModelAccessStaticAuthorizationTest {

    @Test
    @DisplayName("CASE-P2-TD-ACCESS-READ-WRITE-MATRIX-001")
    void exactPolicyIndexSeparatesReadWriteAndHasNoParentFallback() {
        TargetKey target = TargetKey.of(new ViewKey("OrderInfo"));
        ModelAccessRuleKey readUser = ModelAccessRuleKey.of(
                new SystemKey("order"), target, ModelPath.of("user"), AccessOperation.READ);
        ModelAccessRuleKey writeUser = ModelAccessRuleKey.of(
                new SystemKey("order"), target, ModelPath.of("user"), AccessOperation.WRITE);
        CompiledModelAccessRule rule = CompiledModelAccessRule.of(
                readUser,
                AccessCompilationStatus.STATIC_ALLOW,
                RuntimeBindingPlan.exact(
                        target,
                        CompiledTargetBinding.propertyPath(new ViewKey("OrderInfo"), "user")),
                new SourceRef("systems.xml", 1, 1, "/read"));
        ModelAccessPolicyIndex index = ModelAccessPolicyIndex.of(Collections.singletonList(rule));

        assertEquals(AccessCompilationStatus.STATIC_ALLOW, index.classify(readUser));
        assertEquals(AccessCompilationStatus.STATIC_DENY, index.classify(writeUser));
        assertEquals(
                AccessCompilationStatus.STATIC_DENY,
                index.classify(ModelAccessRuleKey.of(
                        new SystemKey("order"),
                        target,
                        ModelPath.of("user.authInfo"),
                        AccessOperation.READ)));
        assertEquals(Arrays.asList(AccessOperation.READ, AccessOperation.WRITE),
                Arrays.asList(AccessOperation.values()));
    }

    @Test
    @DisplayName("CASE-P2-TD-RUNTIME-PLAN-EXACT-BINDING-001")
    void compiledPlanCarriesExactSourceTargetAndResolvedBinding() {
        TargetKey source = TargetKey.of(new ViewKey("OrderInfo"));
        RuntimeBindingPlan first = RuntimeBindingPlan.exact(
                source,
                CompiledTargetBinding.propertyPath(new ViewKey("UserInfo"), "user.authInfo"));
        RuntimeBindingPlan second = RuntimeBindingPlan.exact(
                source,
                CompiledTargetBinding.propertyPath(new ViewKey("UserInfo"), "user.authInfo.role"));
        assertEquals(source, first.sourceTargetKey());
        assertEquals("user.authInfo", first.compiledTargetBinding().exactResolvedValue());
        assertNotEquals(first, second);
    }

    @Test
    @DisplayName("CASE-P2-TD-NESTED-OBJECT-PATH-001")
    void concreteSourcePathCompilesAgainstFrozenViewShape() {
        RawDefinitionSet definitions = ModelAccessTestFixture.nestedPropertyFallback();
        RawDefinition sourceView = requireView(definitions, "OrderInfo");
        ModelPathCompilationResult result = new ModelPathCompiler().compile(
                new SharedModelPath("payInfo.payDetailList"), AccessMode.READ, sourceView);
        assertTrue(result.compiled(), result.diagnostics().toString());
        assertEquals(
                Collections.singletonList(ModelPath.of("payInfo.payDetailList")),
                result.paths());
    }

    @Test
    @DisplayName("CASE-P2-TD-WILDCARD-FINITE-EXPANSION-001")
    void readWildcardBecomesFiniteDeterministicExactPaths() {
        RawDefinitionSet definitions = ModelAccessTestFixture.nestedPropertyFallback();
        RawDefinition sourceView = requireView(definitions, "OrderInfo");
        ModelPathCompilationResult result = new ModelPathCompiler().compile(
                new SharedModelPath("*"), AccessMode.READ, sourceView);
        assertTrue(result.compiled(), result.diagnostics().toString());
        assertEquals(
                Arrays.asList(
                        ModelPath.of("payInfo"),
                        ModelPath.of("payInfo.payDetailList"),
                        ModelPath.of("payInfo.payDetailList.id")),
                result.paths());
        assertFalse(result.paths().stream().anyMatch(path -> "*".equals(path.canonical())));
    }

    @Test
    @DisplayName("CASE-P2-TD-POLICY-CLASSIFICATION-TRUTH-TABLE-001")
    void p1BindingsCompileAtomicallyIntoExactP2Policy() {
        RawDefinitionSet definitions = ModelAccessTestFixture.nestedPropertyFallback();
        SymbolTable symbols = ModelAccessTestFixture.symbols(definitions);
        ModelAccessCompilationResult p1 = new ModelAccessCompiler().compile(definitions, symbols);
        assertTrue(p1.compilation().isPresent());

        ModelAccessPolicyCompilationResult p2 = new ModelAccessPolicyCompiler().compile(
                p1.compilation().get(), symbols);
        assertTrue(p2.compiled(), p2.diagnostics().toString());
        ModelAccessPolicyIndex index = p2.policyIndex().get();
        ModelAccessRuleKey expected = ModelAccessRuleKey.of(
                new SystemKey("payment"),
                TargetKey.of(new ViewKey("OrderInfo")),
                ModelPath.of("payInfo.payDetailList"),
                AccessOperation.READ);
        // P1 binding 指向运行时目标对象，静态授权成立但最终对象绑定必须由 Guard 复核。
        assertEquals(AccessCompilationStatus.RUNTIME_GUARD_REQUIRED, index.classify(expected));
        assertEquals(
                CompiledTargetBinding.Kind.PROPERTY_PATH,
                index.find(expected).get().runtimeBindingPlan().compiledTargetBinding().kind());
    }

    /** 按完整 ViewKey 精确取得测试 source View，不做 bare-name fallback。 */
    private static RawDefinition requireView(RawDefinitionSet definitions, String name) {
        for (RawDefinition definition : definitions.definitions(RawDefinitionKind.VIEW)) {
            if (name.equals(definition.name().orElse(null))) {
                return definition;
            }
        }
        throw new AssertionError("missing view fixture: " + name);
    }
}
