package dec.core.context.runtime;

import dec.core.context.EngineContext;
import dec.core.context.model.AccessCompilationStatus;
import dec.core.context.model.AccessOperation;
import dec.core.context.model.CompiledDefinition;
import dec.core.context.model.CompiledModelAccessRule;
import dec.core.context.model.CompiledModelSet;
import dec.core.context.model.CompiledTargetBinding;
import dec.core.context.model.CompiledViewMaterializationIndex;
import dec.core.context.model.DefinitionKey;
import dec.core.context.model.DeferredDefinition;
import dec.core.context.model.DeferredKey;
import dec.core.context.model.Diagnostic;
import dec.core.context.model.DigestPair;
import dec.core.context.model.ImmutableDeferredRegistry;
import dec.core.context.model.ImmutableRegistry;
import dec.core.context.model.ModelAccessPolicyIndex;
import dec.core.context.model.ModelAccessRuleKey;
import dec.core.context.model.ModelPath;
import dec.core.context.model.PublishedSourceManifest;
import dec.core.context.model.RuntimeBindingPlan;
import dec.core.context.model.SourceRef;
import dec.core.context.model.SystemKey;
import dec.core.context.model.TargetKey;
import dec.core.context.model.ViewKey;
import java.util.Collections;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/** TESTDESIGN-P2-R32 DEV-03 PolicyIndex 原子发布 exact oracle。 */
class P2PolicyPublicationAggregateExactTest {

    @Test
    void policyIndexParticipatesInModelValueDigestAndCapturedContext() {
        ModelAccessPolicyIndex first = policy("order", "OrderInfo", "status");
        ModelAccessPolicyIndex second = policy("order", "OrderInfo", "amount");
        CompiledModelSet left = model(first);
        CompiledModelSet same = model(first);
        CompiledModelSet different = model(second);

        assertEquals(left, same);
        assertEquals(left.hashCode(), same.hashCode());
        assertNotEquals(left, different);
        assertNotEquals(
                left.digestPair().semanticDigest(),
                different.digestPair().semanticDigest());

        EngineContext context = new EngineContext(left);
        assertSame(first, left.modelAccessPolicyIndex());
        assertSame(first, context.modelAccessPolicyIndex());
    }

    /** 创建一条 exact STATIC_ALLOW 规则，用于证明 policy 是发布聚合成员。 */
    private static ModelAccessPolicyIndex policy(
            String system,
            String view,
            String path) {
        TargetKey target = TargetKey.of(new ViewKey(view));
        ModelAccessRuleKey key = ModelAccessRuleKey.of(
                new SystemKey(system),
                target,
                ModelPath.of(path),
                AccessOperation.READ);
        CompiledModelAccessRule rule = CompiledModelAccessRule.of(
                key,
                AccessCompilationStatus.STATIC_ALLOW,
                RuntimeBindingPlan.exact(
                        target,
                        CompiledTargetBinding.propertyPath(new ViewKey(view), path)),
                new SourceRef("systems.xml", 1, 1, "/model-access/read"));
        return ModelAccessPolicyIndex.of(Collections.singletonList(rule));
    }

    /** 构造包含 mandatory materialization + policy 两个 P2 aggregate 的最小模型。 */
    private static CompiledModelSet model(ModelAccessPolicyIndex policyIndex) {
        return new CompiledModelSet(
                PublishedSourceManifest.empty(),
                CompiledViewMaterializationIndex.empty(),
                policyIndex,
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
