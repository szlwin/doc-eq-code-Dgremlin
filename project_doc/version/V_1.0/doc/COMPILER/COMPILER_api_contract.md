# COMPILER P2 API Contract

> Revision `DESIGN-P2-R29`; base `DESIGN-P2-R28`.
> Inputs: `REQAN-P2-R01@d08612768131 + Overlay R04 + BM-R20 + FLOW-R11`; parallel projection `P2-IMPACT-R28`.
> Status: `NEEDS_REVIEW / MACHINE_BLOCKED`. This is the complete current P2 cross-module contract.

## 1. Ownership and preserved public construction surfaces

- CONTEXT `dec.core.context.runtime`: policy/binding/value/invocation/result contracts.
- CONTEXT `dec.core.context.model`: `CompiledModelSet` aggregate + typed View materialization contracts.
- CONTEXT `dec.core.context.data`: typed `ModelDataFactory` overload.
- MODEL `dec.core.model.runtime`: production invocation/root, trusted handle/frame/scope/session, effect provider and MODEL operation implementation.
- STARTER `dec.core.starter.access`: composition/result/failure, target resolution, capability/Guard and Rule/Change/CustomAction entries.

All R28/R27/R25 factories remain current: `RuleKey.of`, `TargetKey.of`, `ModelPath.of`, `ModelAccessRuleKey.of`, `CompiledTargetBinding.targetMain/propertyPath`, `RuntimeBindingPlan.exact`, `CompiledModelAccessRule.of`, `ModelAccessPolicyIndex.of`, opaque-ID `of(String)`, `RuntimeMutationVersion.of`, all `RuntimeFactValue` factories, `ProtectedAccessInvocation.of`, `RuntimeBindingProof.exact`, `ResolvedRuntimeTarget.of`, `RuntimeMutationStamp.of`, `ResolvedProtectedReadAccess.of`, `ResolvedWriteIntent.of`, `ResolvedProtectedWriteAccess.of`, `ProtectedReadValue.of`, `ProtectedWriteReceipt.of`, `ProtectedAccessDenial.of`, `ProtectedAccessResult.allowRead/allowWrite/deny`, and `RuntimeTargetResolution.resolved/denied`.

## 2. Preserved neutral runtime contracts

```java
package dec.core.context.runtime;

public final class RuleKey {
  public static RuleKey of(RuleViewKey owner, String localName);
  public RuleViewKey ownerRuleViewKey(); public String localRuleName();
}
public final class TargetKey { public static TargetKey of(ViewKey sourceViewKey); public ViewKey sourceViewKey(); }
public final class ModelPath { public static ModelPath of(List<String> canonicalSegments); public List<String> canonicalSegments(); }
public enum AccessOperation { READ, WRITE }
public final class ModelAccessRuleKey {
  public static ModelAccessRuleKey of(SystemKey owner, TargetKey target, ModelPath path, AccessOperation operation);
  public SystemKey authorizationOwnerSystemKey(); public TargetKey targetKey();
  public ModelPath modelPath(); public AccessOperation operation();
}
public enum PolicyStatus { STATIC_ALLOW, RUNTIME_GUARD_REQUIRED }
public enum RuntimeAccessRequirement { NONE, EXACT_RUNTIME_BINDING }
public enum ResolvedTargetKind { TARGET_MAIN, PROPERTY_PATH }
public final class CompiledTargetBinding {
  public static CompiledTargetBinding targetMain(ViewKey view, String exactValue);
  public static CompiledTargetBinding propertyPath(ViewKey view, String exactValue);
  public ViewKey targetViewKey(); public ResolvedTargetKind kind(); public String exactResolvedValue();
}
public final class RuntimeBindingPlan {
  public static RuntimeBindingPlan exact(TargetKey source, CompiledTargetBinding binding);
  public TargetKey sourceTargetKey(); public CompiledTargetBinding compiledTargetBinding();
}
public final class CompiledModelAccessRule {
  public static CompiledModelAccessRule of(ModelAccessRuleKey key, PolicyStatus status,
      RuntimeAccessRequirement requirement, Optional<RuntimeBindingPlan> plan);
  public ModelAccessRuleKey key(); public PolicyStatus policyStatus();
  public RuntimeAccessRequirement runtimeRequirement(); public Optional<RuntimeBindingPlan> runtimeBindingPlan();
}
public final class ModelAccessPolicyIndex {
  public static ModelAccessPolicyIndex of(Collection<CompiledModelAccessRule> rules);
  public Optional<CompiledModelAccessRule> find(ModelAccessRuleKey key);
}
```

Opaque IDs `ProtectedInvocationId`, `RuntimeObjectId`, `RuntimeWriteIntentId`, `RuntimeExecutionFrameId`, `RuntimeResolutionOwnerId`, `RuntimeCollectionCursorId`, `RuntimeModelSessionId`, `RuntimeProductionInvocationId` are immutable exact/case-sensitive `public final` values, reject null/blank, and expose `public static of(String)`, `value()`, structural `equals/hashCode` except `RuntimeProductionInvocationId`, whose construction is MODEL-internal only and has no public/protected factory.

```java
public final class RuntimeMutationVersion { public static RuntimeMutationVersion of(long nonNegative); public long value(); }
public final class RuntimeFactValue {
  public enum Kind { NULL, BOOLEAN, INTEGER, DECIMAL, STRING, LIST, OBJECT }
  public static RuntimeFactValue nullValue(); public static RuntimeFactValue ofBoolean(boolean value);
  public static RuntimeFactValue ofInteger(BigInteger value); public static RuntimeFactValue ofDecimal(BigDecimal value);
  public static RuntimeFactValue ofString(String value); public static RuntimeFactValue ofList(List<RuntimeFactValue> value);
  public static RuntimeFactValue ofObject(Map<String,RuntimeFactValue> value);
  public Kind kind(); public String deterministicJson();
}
public final class ProtectedAccessInvocation {
  public static ProtectedAccessInvocation of(ProtectedInvocationId id, ModelAccessRuleKey key,
      RuntimeExecutionFrameId frameId, RuntimeResolutionOwnerId ownerId,
      Optional<RuntimeCollectionCursorId> cursorId);
  public ProtectedInvocationId invocationId(); public ModelAccessRuleKey modelAccessRuleKey();
  public RuntimeExecutionFrameId frameId(); public RuntimeResolutionOwnerId ownerResolutionId();
  public Optional<RuntimeCollectionCursorId> cursorId();
}
public interface ProtectedAccessPort { ProtectedAccessResult invoke(ProtectedAccessInvocation invocation); }
public final class RuntimeBindingProof { public static RuntimeBindingProof exact(String digest); public String value(); }
public final class ResolvedRuntimeTarget {
  public static ResolvedRuntimeTarget of(RuntimeModelSessionId sessionId, RuntimeObjectId objectId,
      TargetKey targetKey, CompiledTargetBinding binding, RuntimeExecutionFrameId frameId,
      RuntimeResolutionOwnerId ownerId, Optional<RuntimeCollectionCursorId> cursorId,
      RuntimeBindingProof proof);
  public RuntimeModelSessionId sessionId(); public RuntimeObjectId runtimeObjectId(); public TargetKey targetKey();
  public CompiledTargetBinding compiledTargetBinding(); public RuntimeExecutionFrameId frameId();
  public RuntimeResolutionOwnerId ownerResolutionId(); public Optional<RuntimeCollectionCursorId> cursorId();
  public RuntimeBindingProof bindingProof();
}
public final class RuntimeMutationStamp {
  public static RuntimeMutationStamp of(RuntimeModelSessionId sessionId, RuntimeObjectId objectId,
      ModelPath path, RuntimeMutationVersion version);
  public RuntimeModelSessionId sessionId(); public RuntimeObjectId runtimeObjectId();
  public ModelPath modelPath(); public RuntimeMutationVersion version();
}
public final class ResolvedProtectedReadAccess {
  public static ResolvedProtectedReadAccess of(ProtectedInvocationId id, ModelAccessRuleKey key,
      ResolvedRuntimeTarget target);
  public ProtectedInvocationId invocationId(); public ModelAccessRuleKey modelAccessRuleKey();
  public ResolvedRuntimeTarget resolvedRuntimeTarget();
}
public final class ResolvedWriteIntent {
  public static ResolvedWriteIntent of(RuntimeWriteIntentId id, ModelAccessRuleKey key,
      Optional<RuleKey> provenance, ResolvedRuntimeTarget target, RuntimeMutationStamp stamp);
  public RuntimeWriteIntentId id(); public ModelAccessRuleKey modelAccessRuleKey();
  public Optional<RuleKey> ruleKeyProvenance(); public ResolvedRuntimeTarget resolvedRuntimeTarget();
  public RuntimeMutationStamp mutationStamp();
}
public final class ResolvedProtectedWriteAccess {
  public static ResolvedProtectedWriteAccess of(ProtectedInvocationId id, ResolvedWriteIntent intent);
  public ProtectedInvocationId invocationId(); public ResolvedWriteIntent writeIntent();
}
public enum RuntimeModelOperationFailureCode {
  SESSION_SCOPE_MISMATCH, OBJECT_NOT_REGISTERED, OBJECT_STALE, WRITE_FAILED
}
public final class RuntimeModelOperationException extends Exception {
  public RuntimeModelOperationFailureCode code(); public String stableMessage();
}
public interface RuntimeModelOperationPort {
  RuntimeFactValue read(ResolvedProtectedReadAccess access) throws RuntimeModelOperationException;
  ProtectedWriteReceipt write(ResolvedProtectedWriteAccess access) throws RuntimeModelOperationException;
}
public final class ProtectedReadValue {
  public static ProtectedReadValue of(ProtectedInvocationId id, RuntimeFactValue value);
  public ProtectedInvocationId invocationId(); public RuntimeFactValue value();
}
public final class ProtectedWriteReceipt {
  public static ProtectedWriteReceipt of(ProtectedInvocationId id, RuntimeWriteIntentId intentId,
      RuntimeMutationVersion committedVersion);
  public ProtectedInvocationId invocationId(); public RuntimeWriteIntentId writeIntentId();
  public RuntimeMutationVersion committedVersion();
}
public enum DenialCode {
  POLICY_NOT_FOUND, POLICY_MISMATCH, RUNTIME_PLAN_MISMATCH, GUARD_UNAVAILABLE,
  CAPABILITY_ALREADY_CONSUMED, RUNTIME_CONTEXT_MISMATCH, RUNTIME_MODEL_PROVENANCE_MISMATCH,
  RUNTIME_TARGET_NOT_FOUND, RUNTIME_TARGET_AMBIGUOUS, WRITE_INTENT_NOT_FOUND,
  WRITE_INTENT_AMBIGUOUS, WRITE_INTENT_STALE, RUNTIME_SESSION_SCOPE_MISMATCH,
  RUNTIME_OBJECT_NOT_FOUND, RUNTIME_OBJECT_STALE, RUNTIME_OBJECT_ALREADY_REGISTERED,
  RUNTIME_OBJECT_OWNERSHIP_CONFLICT, RUNTIME_WRITE_FAILED
}
public final class ProtectedAccessDenial {
  public static ProtectedAccessDenial of(ProtectedInvocationId id, DenialCode code, String stableMessage);
  public ProtectedInvocationId invocationId(); public DenialCode code(); public String stableMessage();
}
public final class ProtectedAccessResult {
  public static ProtectedAccessResult allowRead(ProtectedReadValue value);
  public static ProtectedAccessResult allowWrite(ProtectedWriteReceipt receipt);
  public static ProtectedAccessResult deny(ProtectedAccessDenial denial);
  public boolean allowed(); public Optional<ProtectedReadValue> readValue();
  public Optional<ProtectedWriteReceipt> writeReceipt(); public Optional<ProtectedAccessDenial> denial();
}
```

<a id="context-publication-aggregate"></a>
## 3. CONTEXT publication aggregate

```java
package dec.core.context.model;
public enum MaterializationNodeKind { SCALAR, OBJECT, LIST }
public final class CompiledMaterializationNode {
  public static CompiledMaterializationNode scalar(String fieldName);
  public static CompiledMaterializationNode object(String fieldName, List<CompiledMaterializationNode> children);
  public static CompiledMaterializationNode list(String fieldName, List<CompiledMaterializationNode> elementShape);
  public String fieldName(); public MaterializationNodeKind kind(); public List<CompiledMaterializationNode> children();
}
public final class CompiledViewMaterializationPlan {
  public static CompiledViewMaterializationPlan of(ViewKey viewKey, List<CompiledMaterializationNode> rootFields);
  public ViewKey viewKey(); public List<CompiledMaterializationNode> rootFields();
}
public final class CompiledViewMaterializationIndex {
  public static CompiledViewMaterializationIndex of(Collection<CompiledViewMaterializationPlan> plans);
  public Optional<CompiledViewMaterializationPlan> find(ViewKey viewKey); public Set<ViewKey> viewKeys();
}
public final class CompiledModelSet {
  public CompiledModelSet(PublishedSourceManifest sourceManifest,
      Registry<DefinitionKey, CompiledDefinition> definitions,
      CompiledViewMaterializationIndex viewMaterializationIndex,
      DeferredRegistry deferred, List<Diagnostic> diagnostics, DigestPair digestPair,
      String compilerVersion, String schemaVersion, String optionsVersion);
  public CompiledViewMaterializationIndex viewMaterializationIndex();
}
public final class EngineContext {
  public EngineContext(CompiledModelSet compiledModelSet);
  public CompiledModelSet compiledModelSet(); public CompiledModelSet modelSet();
  public CompiledViewMaterializationIndex viewMaterializationIndex();
  public CoreConfigProjection projection();
}
```

The materialization index is a mandatory `CompiledModelSet` member, participates in equality/hash and canonical semantic-digest input, and is atomically published with the same Context. Missing or duplicate descriptor for a P2 dynamic target View blocks compile/publication. MODEL may consume only this captured aggregate; runtime `NormalizedBody`, XML/YAML, `ViewData`, `ModelData.name`, thread-local/global/default Context reconstruction is forbidden.

```java
package dec.core.context.data;
public final class ModelDataFactory {
  public ModelData createData(CompiledViewMaterializationPlan plan, Object originObject)
      throws DataNotDefineException;
}
```

<a id="trusted-production-invocation"></a>
## 4. Trusted production invocation and Container boundary

`DESIGN-P2-R28` public `RuntimeModelLoadRequest.of(plan, originObject, ...)` and `production(context, Container)` are superseded because they allow callers to compose trust inputs. Current R29 freezes an opaque single-invocation token and MODEL-created production container.

```java
package dec.core.model.runtime;

public enum ProductionContainerKind { COMMIT, SYNCHRONIZED }
public final class RuntimeModelProductionInvocation {
  // NO public/protected constructor/factory/rebind; minted only inside MODEL production adapter.
  public RuntimeProductionInvocationId invocationId();
  public RuntimeBindingPlan runtimeBindingPlan();
}
public enum RuntimeModelLoadFailureCode {
  EXECUTION_CLOSED,
  INVOCATION_ROOT_MISMATCH,
  INVOCATION_ALREADY_CONSUMED,
  PLAN_NOT_IN_CAPTURED_CONTEXT,
  MATERIALIZATION_DESCRIPTOR_NOT_FOUND,
  ORIGIN_NOT_MATERIALIZABLE,
  CONTAINER_LOAD_REJECTED
}
public final class RuntimeModelLoadFailure { public RuntimeModelLoadFailureCode code(); public String stableMessage(); }
public final class RuntimeModelLoadResult {
  public boolean loaded(); public Optional<RuntimeModelHandle> handle();
  public Optional<RuntimeModelLoadFailure> failure();
}
public enum RuntimeModelScopeFailureCode { NO_TRUSTED_MODEL, EXECUTION_CLOSED, SCOPE_INACTIVE, SCOPE_STALE }
public final class RuntimeModelScopeFailure { public RuntimeModelScopeFailureCode code(); public String stableMessage(); }
public final class RuntimeModelScopeResult {
  public boolean available(); public Optional<RuntimeModelAccessScope> scope();
  public Optional<RuntimeModelScopeFailure> failure();
}
public interface RuntimeModelExecutionRoot extends AutoCloseable {
  RuntimeModelLoadResult load(RuntimeModelProductionInvocation trustedInvocation);
  RuntimeModelScopeResult accessScope();
  @Override void close();
}
public final class RuntimeModelExecutionRoots {
  public static RuntimeModelExecutionRoot production(
      EngineContext capturedEngineContext, ProductionContainerKind containerKind);
}
```

Mandatory production rules:
1. `RuntimeModelExecutionRoots.production` creates the supported existing MODEL container internally through `ContainerFactory`; there is no public production overload accepting `Container`, provider, ModelData or operation port.
2. A package-private MODEL production adapter captures **one active production invocation** into one immutable `RuntimeModelProductionInvocation`: exact current `RuntimeBindingPlan`, the real origin object, explicit rule name, explicit connection name and root identity are captured atomically before ModelData creation. Ordinary business/application/STARTER code has no constructor/factory for this token.
3. `root.load(token)` accepts only a token minted for that root and consumes it once. Cross-root token use returns `INVOCATION_ROOT_MISMATCH`; reuse returns `INVOCATION_ALREADY_CONSUMED`.
4. The exact plan is verified in the root's captured Context; the exact materialization descriptor is selected by target ViewKey; typed `ModelDataFactory` creates ModelData from the token's captured real origin object; MODEL uses the existing three-argument `ModelLoader.load(ruleName, modelData, connectionName)` and root-owned production Container.
5. The same created/loaded ModelData reference is frozen in the trusted handle. No public API accepts `RuntimeBindingPlan + Object` as independently composable trusted inputs.
6. `COMMIT`/`SYNCHRONIZED` map only to production container implementations selected by existing `ContainerFactory`; fake/test Container may exist in unit tests but is not AC-007 production evidence.

<a id="trusted-runtime-scope"></a>
## 5. MODEL scope/session and effect provider

```java
package dec.core.model.runtime;

public final class RuntimeModelProvenance {
  // no public/protected constructor/factory
  public RuntimeBindingPlan runtimeBindingPlan();
}
public final class RuntimeModelHandle {
  // no public/protected constructor/factory/wrap/rebind; no public ModelData accessor
  public RuntimeModelProvenance provenance();
}
public final class RuntimeModelFrame {
  // no public/protected constructor/factory/rebind
  public RuntimeExecutionFrameId frameId(); public RuntimeResolutionOwnerId ownerResolutionId();
  public Optional<RuntimeCollectionCursorId> cursorId(); public List<RuntimeModelHandle> handles();
}
public enum RuntimeModelSessionFailureCode {
  SCOPE_INACTIVE, SESSION_CLOSED, SESSION_ALREADY_SEALED, DUPLICATE_REGISTRATION, OWNERSHIP_CONFLICT
}
public final class RuntimeModelSessionException extends Exception {
  public RuntimeModelSessionFailureCode code(); public String stableMessage();
}
public interface RuntimeModelSession extends AutoCloseable {
  RuntimeModelSessionId sessionId();
  RuntimeObjectId register(RuntimeModelHandle trustedHandle) throws RuntimeModelSessionException;
  void seal() throws RuntimeModelSessionException;
  LocatedRuntimeObject locate(ResolvedRuntimeTarget target);
  RuntimeMutationVersion currentVersion(ResolvedRuntimeTarget target, ModelPath path);
  @Override void close();
}
public enum RuntimeModelEffectBindingFailureCode {
  SCOPE_INACTIVE, SESSION_NOT_SEALED, SESSION_CLOSED, SESSION_SCOPE_MISMATCH
}
public final class RuntimeModelEffectBindingFailure {
  public RuntimeModelEffectBindingFailureCode code(); public String stableMessage();
}
public final class RuntimeModelEffectBindingResult {
  public boolean bound(); public Optional<RuntimeModelOperationPort> operationPort();
  public Optional<RuntimeModelEffectBindingFailure> failure();
}
public interface RuntimeModelEffectProvider {
  RuntimeModelEffectBindingResult bind(RuntimeModelSession sealedSession);
}
public final class RuntimeModelAccessScope {
  // no public/protected constructor/factory; MODEL-minted only
  public RuntimeModelFrame frame();
  public RuntimeModelSession beginSession() throws RuntimeModelSessionException;
  public RuntimeModelEffectProvider effectProvider();
}
public final class LocatedRuntimeObject {
  public RuntimeModelSessionId sessionId(); public RuntimeObjectId runtimeObjectId();
  public RuntimeModelProvenance provenance();
}
```

The provider belongs to the same scope/root/handle set. `bind(session)` succeeds only after that exact session is sealed and proves the same scope/root. The returned `RuntimeModelOperationPort` is private to STARTER composition; it is never exposed by `ProtectedAccessComposition`, Rule/Change/CustomAction entry interfaces, or business APIs. A bound operation port validates every resolved access session/object against the same sealed session and registered handle before touching ModelData; operation mismatch maps to existing fail-closed denial codes and performs zero effect.

<a id="composition-failure-algebra"></a>
## 6. STARTER composition and actual effect binding

```java
package dec.core.starter.access;

public enum ProtectedAccessCompositionFailureCode {
  SCOPE_INACTIVE, SCOPE_STALE, PROVENANCE_MISMATCH,
  SESSION_DUPLICATE_REGISTRATION, SESSION_OWNERSHIP_CONFLICT,
  SESSION_ALREADY_SEALED, SESSION_CLOSED,
  EFFECT_SESSION_NOT_SEALED, EFFECT_SESSION_CLOSED, EFFECT_SESSION_SCOPE_MISMATCH
}
public final class ProtectedAccessCompositionFailure {
  public ProtectedAccessCompositionFailureCode code(); public String stableMessage();
}
public final class ProtectedAccessCompositionResult {
  public boolean created(); public Optional<ProtectedAccessComposition> composition();
  public Optional<ProtectedAccessCompositionFailure> failure();
}
public final class ProtectedAccessRuntimeFactory {
  public static ProtectedAccessRuntimeFactory production(EngineContext capturedEngineContext);
  public ProtectedAccessCompositionResult create(RuntimeModelAccessScope trustedScope);
}
public enum RuntimeTargetResolutionStatus {
  RESOLVED, NOT_FOUND, AMBIGUOUS, CONTEXT_MISMATCH, PROVENANCE_MISMATCH
}
public final class RuntimeTargetResolution {
  public static RuntimeTargetResolution resolved(ResolvedRuntimeTarget target);
  public static RuntimeTargetResolution denied(RuntimeTargetResolutionStatus status, DenialCode code);
  public RuntimeTargetResolutionStatus status(); public Optional<ResolvedRuntimeTarget> target();
  public Optional<DenialCode> denialCode();
}
public interface RuntimeTargetResolver {
  RuntimeTargetResolution resolve(RuntimeBindingPlan plan, ProtectedAccessInvocation invocation,
      RuntimeModelSession session);
}
public final class RuntimeExecutionFrameSnapshot {
  public static RuntimeExecutionFrameSnapshot from(RuntimeModelFrame trustedFrame);
  public RuntimeExecutionFrameId frameId(); public RuntimeResolutionOwnerId ownerResolutionId();
  public Optional<RuntimeCollectionCursorId> cursorId(); public RuntimeModelFrame runtimeModelFrame();
}
public interface RuleProtectedAccessEntry { ProtectedAccessResult invoke(ProtectedAccessInvocation invocation); }
public interface ChangeProtectedAccessEntry { ProtectedAccessResult invoke(ProtectedAccessInvocation invocation); }
public interface CustomActionProtectedAccessEntry { ProtectedAccessResult invoke(ProtectedAccessInvocation invocation); }
public final class ProtectedAccessComposition implements AutoCloseable {
  public ProtectedAccessPort protectedAccessPort();
  public RuntimeExecutionFrameId frameId(); public RuntimeResolutionOwnerId ownerResolutionId();
  public RuntimeModelSessionId runtimeModelSessionId();
  public RuleProtectedAccessEntry ruleEntry(); public ChangeProtectedAccessEntry changeEntry();
  public CustomActionProtectedAccessEntry customActionEntry();
  @Override public void close();
}
```

`create(scope)` performs FLOW-R11 STEP-01/02 exactly: validate frame/provenance, begin one session, register all handles, seal once, then call `scope.effectProvider().bind(theSameSealedSession)`. A composition is returned only after effect binding succeeds; it privately retains that operation port. STEP-03/04 resolves/freezes the same session/object/path authority; STEP-05 invokes Guard; only ALLOW reaches the private bound operation port at STEP-06. DENY never invokes the port. `RuntimeModelOperationException` is mapped to existing `DenialCode` and no success receipt/value is fabricated.

Production dependency rule: only `dec-core-starter` may consume MODEL scope/effect-provider/operation-port contracts. Rule/Change/CustomAction business consumers depend on STARTER entry interfaces and CONTEXT values, not on `dec.core.model.runtime`. Architecture tests must reject production imports of `RuntimeModelAccessScope`, `RuntimeModelEffectProvider`, or `RuntimeModelOperationPort` outside MODEL/STARTER.

## 7. Explicit scope exclusion

Per user directive, restoration of a POJO/Map already copied by legacy `ModelContainer` before a later commit failure is outside this remediation scope. Current P2 still requires normal successful production write-back reachability and pre-effect fail-closed behavior, but no new post-copy restoration design/test blocker is introduced.

No production Java, TDD execution, risk Evidence or lifecycle promotion is claimed.
