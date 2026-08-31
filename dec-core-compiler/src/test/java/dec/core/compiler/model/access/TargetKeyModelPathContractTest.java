package dec.core.compiler.model.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dec.core.context.model.ModelPath;
import dec.core.context.model.TargetKey;
import dec.core.context.model.ViewKey;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** TESTDESIGN-P2-R32 P2 target/path executable contract with explicit task ownership. */
class TargetKeyModelPathContractTest {

    @Test
    @DisplayName("CASE-P2-TD-TARGETKEY-SOURCE-MAPPING-001")
    void targetKeyWrapsExactlyTheSharedSourceViewKey() {
        ViewKey source = new ViewKey("OrderView");
        TargetKey target = TargetKey.of(source);
        assertEquals(source, target.sourceViewKey());
        assertNotEquals(TargetKey.of(new ViewKey("OrderView")), TargetKey.of(new ViewKey("orderView")));
    }

    @Test
    @DisplayName("CASE-P2-TD-TARGET-PATH-ORTHOGONALITY-001")
    void targetIdentityAndCanonicalModelPathRemainOrthogonal() {
        TargetKey target = TargetKey.of(new ViewKey("OrderView"));
        ModelPath path = ModelPath.of("user.authInfo");
        assertEquals(new ViewKey("OrderView"), target.sourceViewKey());
        assertEquals(Arrays.asList("user", "authInfo"), path.segments());
        assertEquals("user.authInfo", path.canonical());
    }

    @Test
    @DisplayName("DEV03-DEFERRED-MODEL-PATH-UNKNOWN-001")
    void unknownSegmentRequiresCompilerShapeValidation() {
        // DEV-03 将基于已编译 View shape 精确验证 unknown segment；DEV-02 只冻结值对象拒绝非法 lexical。
        assertThrows(IllegalArgumentException.class, () -> ModelPath.of("user.*"));
    }

    @Test
    @DisplayName("DEV03-DEFERRED-WILDCARD-FINITE-EXPANSION-001")
    void wildcardExpansionBelongsToModelAccessCompiler() {
        assertThrows(IllegalArgumentException.class, () -> ModelPath.of("user.*"));
    }

    @Test
    @DisplayName("DEV03-DEFERRED-MODEL-PATH-CROSS-CONSUMER-EQUIVALENCE-001")
    void crossConsumerNormalizationBelongsToModelAccessCompiler() {
        assertEquals(ModelPath.of("user.authInfo"), ModelPath.ofSegments(Arrays.asList("user", "authInfo")));
    }

    @Test
    @DisplayName("DEV09-DEFERRED-P1-PATH-OPERATION-MIGRATION-001")
    void compatibilityMigrationRemainsDeferred() {
        assertEquals("user.authInfo", ModelPath.of("user.authInfo").canonical());
    }

    @Test
    @DisplayName("DEV03-DEFERRED-NESTED-OBJECT-PATH-001")
    void nestedObjectShapeValidationRemainsDeferred() {
        assertEquals(Arrays.asList("user", "authInfo"), ModelPath.of("user.authInfo").segments());
    }

    @Test
    @DisplayName("DEV03-DEFERRED-DEEP-NESTED-OBJECT-PATH-001")
    void deepNestedShapeValidationRemainsDeferred() {
        assertEquals(Arrays.asList("user", "authInfo", "role"), ModelPath.of("user.authInfo.role").segments());
    }

    @Test
    @DisplayName("DEV03-DEFERRED-NON-COMPOSITE-INTERMEDIATE-001")
    void nonCompositeIntermediateValidationRemainsDeferred() {
        assertEquals(Arrays.asList("user", "id", "value"), ModelPath.of("user.id.value").segments());
    }

    @Test
    @DisplayName("DEV03-DEFERRED-NESTED-COLLECTION-PATH-001")
    void nestedCollectionShapeValidationRemainsDeferred() {
        assertEquals(
                Arrays.asList("payInfo", "payDetailList", "productId"),
                ModelPath.of("payInfo.payDetailList.productId").segments());
    }

    @Test
    @DisplayName("DEV03-DEFERRED-TARGET-MAIN-PATH-ISOLATION-001")
    void targetMainPathIsolationRemainsDeferred() {
        TargetKey target = TargetKey.of(new ViewKey("user"));
        ModelPath path = ModelPath.of("user.authInfo");
        assertEquals(new ViewKey("user"), target.sourceViewKey());
        assertEquals("user.authInfo", path.canonical());
    }
}
