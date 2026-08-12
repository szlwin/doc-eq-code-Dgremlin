package dec.core.context.runtime;

import dec.core.context.EngineContext;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.CompiledMaterializationNode;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.CompiledViewMaterializationIndex;
import dec.core.context.model.CompiledViewMaterializationPlan;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DigestPair;
import dec.core.context.model.ImmutableDeferredRegistry;
import dec.core.context.model.ImmutableRegistry;
import dec.core.context.model.ModelPath;
import dec.core.context.model.PublishedSourceManifest;
import dec.core.context.model.ViewKey;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** TESTDESIGN-P2-R32 DEV-04 materialization aggregate exact oracle. */
class P2MaterializationAggregateExactTest {

    @Test
    void materializationIndexIsMandatoryAndParticipatesInAggregateValueAndDigest() {
        CompiledViewMaterializationIndex first = index("Order", "id");
        CompiledViewMaterializationIndex second = index("Order", "amount");
        CompiledModelSet left = model(first);
        CompiledModelSet same = model(first);
        CompiledModelSet different = model(second);

        assertEquals(left, same);
        assertEquals(left.hashCode(), same.hashCode());
        assertNotEquals(left, different);
        assertNotEquals(
                left.digestPair().semanticDigest(),
                different.digestPair().semanticDigest());
        assertThrows(NullPointerException.class, () -> model(null));
    }

    @Test
    void engineContextDelegatesExactlyTheCapturedIndex() {
        CompiledViewMaterializationIndex index = index("Order", "id");
        EngineContext context = new EngineContext(model(index));
        assertSame(index, context.viewMaterializationIndex());
    }

    /** 构造只包含一个精确 View/path 的测试索引。 */
    private static CompiledViewMaterializationIndex index(String view, String path) {
        return CompiledViewMaterializationIndex.of(Collections.singletonList(
                CompiledViewMaterializationPlan.of(
                        new ViewKey(view),
                        Collections.singletonList(
                                CompiledMaterializationNode.of(ModelPath.of(path))))));
    }

    /** 构造最小 CompiledModelSet，专门验证 materialization aggregate 值语义。 */
    private static CompiledModelSet model(CompiledViewMaterializationIndex index) {
        return new CompiledModelSet(
                PublishedSourceManifest.empty(),
                index,
                new ImmutableRegistry<DefinitionKey, CompiledDefinition>(
                        Collections.<DefinitionKey, CompiledDefinition>emptyMap()),
                new ImmutableDeferredRegistry(
                        Collections.<DeferredKey, DeferredDefinition>emptyMap()),
                Collections.<Diagnostic>emptyList(),
                new DigestPair("source", "semantic"),
                "compiler",
                "schema",
                "options");
    }
}
