# COMPILER P2 详细设计

> Revision：`DESIGN-P2-R19`。Base：`DESIGN-P2-R18`。  
> Inputs：`REQAN-P2-R01@d08612768131` + `REQAN-P2-R01+DEC-OVERLAY-20260809-R04` + `BM-R17` + `FLOW-R07@p2-system-ruleview-protected-access`。  
> Decisions：Direct Bridge ACTIVE；AC-007 Option B ACTIVE；READ/WRITE-only ACTIVE。  
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`。

R19 retains R18 System/RuleView/RuleKey/ModelPath/PolicyIndex/Option-B/atomic-capability contracts and closes the remaining implementation-uniqueness gaps: revision DAG, exact TargetKey mapping, policy classification truth table, real READ/WRITE operation semantics, and neutral downstream runtime seam.

<a id="p2-revision-dag"></a>
## 1. Authoritative revision DAG

```text
REQAN-P2-R01 + Overlay R04 + active Decisions
 -> BM-R17
 -> FLOW-R07
 -> DESIGN-P2-R19
 -> TESTDESIGN-P2-R20
```

Only upstream artifacts are authoritative inputs. Flow may carry `downstream*TraceRefs`, but Design/TestDesign are never Flow inputs and Flow is never BM input.

<a id="p2-system"></a>
## 2. System / RuleView / RuleKey

Existing `SystemKey(String)/name()` and `RuleViewKey(SystemKey,String)/owner()/name()` MUST remain source-compatible.

`SystemVersionIdentity = declaredVersion + sourceSemanticDigest + schemaVersion + compilerVersion`.

`RuleKey` is a public immutable `dec-core-context` value:

```java
public final class RuleKey {
    public RuleKey(RuleViewKey ownerRuleViewKey, String localRuleName);
    public static RuleKey of(RuleViewKey ownerRuleViewKey, String localRuleName);
    public RuleViewKey ownerRuleViewKey();
    public String localRuleName();
}
```

Identity/equality/hash = `(ownerRuleViewKey,localRuleName)`. Authoritative store is the owning `CompiledRuleView` immutable rule closure; no duplicate global Rule registry.

<a id="p2-target-key"></a>
## 3. TargetKey / sourceModel mapping

`TargetKey` is a neutral public immutable value in `dec-core-context`:

```java
public final class TargetKey {
    public TargetKey(SystemKey ownerSystemKey, String canonicalSourceModelName);
    public static TargetKey of(SystemKey ownerSystemKey, String canonicalSourceModelName);
    public SystemKey ownerSystemKey();
    public String canonicalSourceModelName();
}
```

Contract:

- fields non-null; source model name nonblank after existing lexical validation;
- equality/hash/order include both fields exactly; no case-fold/fuzzy/parent fallback;
- compiler resolves P1/model-access `sourceModel` **inside the owner System** before constructing TargetKey;
- unknown/ambiguous/cross-System sourceModel => stable source-aware ERROR, publication=0;
- `sourcePath` is not part of TargetKey and cannot influence target lookup;
- one source binding converts as `sourceModel -> TargetKey` and independently `sourcePath -> ModelPath`;
- after conversion, raw source strings are diagnostic/provenance only, never runtime authority.

Compiler-owned resolver contract:

```java
interface TargetKeyCompiler {
    TargetKey require(SystemKey ownerSystemKey, String sourceModel, SourceRef sourceRef);
}
```

`TargetKeyCompiler` is compiler implementation surface, not runtime permission authority.

<a id="p2-model-path"></a>
## 4. ModelPath / READ-WRITE migration

`ModelPath` is immutable exact canonical segments in `dec-core-context`; runtime wildcard is forbidden. RULE/CHANGE/QUERY_CONTRACT/MODEL_ACCESS share one `ModelPathCompiler` semantics.

```text
SharedModelPath exact -> ModelPathCompiler -> exact ModelPath
SharedModelPath("*") -> resolved TargetKey schema -> finite sorted exact ModelPaths
AccessMode.READ -> AccessOperation.READ
AccessMode.WRITE -> AccessOperation.WRITE
```

Current `AccessOperation` enum contains exactly READ and WRITE. No EXECUTE source/raw/enum/policy/runtime/test contract.

<a id="p2-policy-classification"></a>
## 5. PolicyStatus / RuntimeAccessRequirement truth table

Neutral enums:

```java
public enum PolicyStatus {
    STATIC_ALLOW,
    RUNTIME_GUARD_REQUIRED
}

public enum RuntimeAccessRequirement {
    NONE,
    EXACT_RUNTIME_BINDING
}
```

Only legal rows:

| PolicyStatus | RuntimeAccessRequirement | RuntimeBindingPlan | result |
|---|---|---|---|
| STATIC_ALLOW | NONE | absent | VALID |
| STATIC_ALLOW | EXACT_RUNTIME_BINDING | any | INVALID |
| RUNTIME_GUARD_REQUIRED | EXACT_RUNTIME_BINDING | present | VALID |
| RUNTIME_GUARD_REQUIRED | NONE | any | INVALID |

Null/unknown values or any mixed row are INVALID before PolicyIndex publication. `ModelAccessPolicyIndex.of(...)` revalidates this invariant and never repairs/reclassifies malformed rules.

`RuntimeBindingPlan` is immutable exact compiler fact:

```java
public final class RuntimeBindingPlan {
    public TargetKey resolvedTargetKey();
    public ModelPath modelPath();
    public ViewKey targetViewKey();
    public RuntimeSelectorPlanId selectorPlanId();
    public SourceRef sourceRef();
}
```

Plan facts must match the selected `ModelAccessRuleKey`; runtime proof can validate membership/staleness only and cannot select another target/path/rule/operation.

<a id="p2-model-access"></a>
## 6. Authorization identity / PolicyIndex

```java
public enum AccessOperation { READ, WRITE }
```

`ModelAccessRuleKey` immutable identity = `SystemKey + TargetKey + ModelPath + AccessOperation`.

`CompiledModelAccessRule` immutable facts = key + SourceRef + PolicyStatus + RuntimeAccessRequirement + optional RuntimeBindingPlan.

`ModelAccessPolicyIndex` is the sole runtime authorization authority and exact-lookup index. Duplicate/null/wildcard/operation/truth-table mismatch rejects before collapse.

<a id="p2-neutral-runtime-port"></a>
## 7. Neutral protected-access contract / dependency direction

To prevent future core modules from depending upward on starter, `dec-core-context` owns neutral contract types:

```java
public interface ProtectedAccessPort {
    ProtectedAccessResult execute(ProtectedAccessInvocation invocation);
}

public final class ProtectedAccessInvocation {
    public ModelAccessRuleKey requestedRuleKey();
    public AccessOperation operation();
    public RuntimeExecutionFrameId frameId();
    public RuntimeResolutionOwnerId ownerResolutionId();
    public Optional<RuntimeCollectionCursorId> cursorId();
}
```

`ProtectedAccessInvocation` and `ProtectedAccessResult` contain no Gateway/Guard/capability/mutable PolicyIndex authority.

`dec-core-starter` owns:

```java
public final class ProtectedExecutionBridge implements ProtectedAccessPort { ... }
```

and exclusively owns target resolver, invocation ID mint, one-shot capability, Gateway, Guard, protected-operation adapter, and production assembly.

Dependency invariant:

```text
P3/P4/P6 core -> dec-core-context ProtectedAccessPort
P3/P4/P6 core -X-> dec-core-starter

dec-core-starter -> dec-core-context
application/demo -> dec-core-starter composition
```

The neutral port is transport/seam only. Authorization truth remains the current EngineContext `ModelAccessPolicyIndex` inside starter flow.

<a id="p2-direct-bridge"></a>
## 8. Direct Bridge

User-approved scalar API remains:

```java
public final class ProtectedExecutionBridge implements ProtectedAccessPort {
    public ProtectedAccessResult execute(
        ModelAccessRuleKey requestedRuleKey,
        AccessOperation operation,
        RuntimeExecutionFrameId frameId,
        RuntimeResolutionOwnerId ownerResolutionId,
        Optional<RuntimeCollectionCursorId> cursorId);

    public ProtectedAccessResult execute(ProtectedAccessInvocation invocation);
}
```

Only READ/WRITE; invocation operation must equal rule-key operation. No token/recognizes/claim and no caller-injected operation callback.

<a id="p2-production-composition"></a>
## 9. AC-007 Option B production composition

```java
public final class ProtectedAccessRuntimeFactory {
    public ProtectedAccessComposition bind(EngineContext engineContext);
}

public final class ProtectedAccessComposition {
    public EngineContext engineContext();
    public ProtectedExecutionBridge bridge();
    public RuleProtectedAccessEntry ruleEntry();
    public ChangeProtectedAccessEntry changeEntry();
    public CustomActionProtectedAccessEntry customActionEntry();
}
```

All entries share exact same Bridge/EngineContext. Production E2E obtains them through factory/composition; manual wrapper construction is not closure Evidence. Business callers cannot obtain Gateway/Guard/resolver/operation port/capability mint/mutable permission map.

Representative entries delegate to the same `ProtectedAccessPort` implementation and may add provenance only; consumer category cannot change authorization.

<a id="p2-operation-binding"></a>
## 10. Real protected READ / WRITE execution contract

Starter-internal resolved capability binds:

```text
ProtectedInvocationId
EngineContext identity
ModelAccessRuleKey
TargetKey + resolved RuntimeObjectId
ModelPath
AccessOperation READ|WRITE
RuntimeBindingPlan/proof identity when required
RuntimeWriteIntentId when operation=WRITE
```

`RuntimeWriteIntentId` is resolved from current frame/owner execution state by starter; caller cannot provide it as an executable callback.

Starter-internal operation port:

```java
interface ProtectedOperationExecutionPort {
    ProtectedReadValue read(ResolvedProtectedAccess access);
    ProtectedWriteReceipt write(ResolvedProtectedAccess access);
}
```

`ResolvedProtectedAccess` is starter-internal capability-bound state and cannot be constructed/retargeted by business callers.

READ success:

```java
public final class ProtectedReadValue {
    public RuntimeObjectId runtimeObjectId();
    public ModelPath modelPath();
    public RuntimeFactValue value();
}
```

- port reads exact capability-bound object/path;
- returned value is immutable snapshot/value fact;
- READ produces no mutation/write receipt.

WRITE success:

```java
public final class ProtectedWriteReceipt {
    public RuntimeObjectId runtimeObjectId();
    public ModelPath modelPath();
    public ProtectedInvocationId invocationId();
    public RuntimeWriteIntentId writeIntentId();
}
```

- port applies the exact internally resolved write intent to capability-bound object/path once;
- receipt presence means that mutation completed once for this capability;
- no READ value is returned for WRITE.

`ProtectedAccessResult` terminal shape:

```java
public final class ProtectedAccessResult {
    public boolean allowed();
    public AccessOperation operation();
    public Optional<ProtectedReadValue> readValue();
    public Optional<ProtectedWriteReceipt> writeReceipt();
    public Optional<ProtectedAccessDenial> denial();
}
```

Validity:
- ALLOW+READ => readValue present, writeReceipt/denial absent;
- ALLOW+WRITE => writeReceipt present, readValue/denial absent;
- DENY => denial present, readValue/writeReceipt absent;
- any other combination is invalid internal state.

Gateway/Guard ALLOW is required before operation port invocation. Every DENY occurs before operation port/effect. Operation port is never public composition output and never caller-injected.

<a id="p2-concurrency"></a>
## 11. One-shot concurrency

Capability is concurrent-reachable and uses atomic `ISSUED -> CONSUMED` transition before operation. Same capability concurrent consume: successful transition <=1; actual read/write operation <=1; WRITE mutation <=1. Loser stable `CAPABILITY_ALREADY_CONSUMED`, no operation/result/effect. Non-atomic check-then-set forbidden.

<a id="p2-context"></a>
## 12. Publication / digest

```text
registries + RuleView closure
 -> sourceModel->TargetKey
 -> sourcePath->ModelPath
 -> READ/WRITE conversion
 -> classification truth-table validation
 -> CompiledModelAccessRules / PolicyIndex
 -> ownership/version
 -> SemanticDigestInput
 -> digest
 -> CompiledModelSet.published
 -> EngineContext
```

Any ERROR => candidate publication=0, old Context unchanged. Legacy public constructors remain source-compatible and must not reconstruct permissions.

<a id="p2-runtime-denial"></a>
## 13. Denial / diagnostics

Stable families include POLICY_NOT_FOUND, RUNTIME_BINDING_STALE, RUNTIME_PLAN_MISMATCH, TARGET_SUBSTITUTION, GUARD_UNAVAILABLE, CAPABILITY_ALREADY_CONSUMED. DENY has no readValue/writeReceipt and no mutation. Diagnostic fields are stable and non-sensitive.

## 14. Gate

DESIGN-P2-R19 = `NEEDS_REVIEW / MACHINE_BLOCKED`. ApiContract/Architecture/Develop/Impact/CrossModule/Concurrency exact Reviews and risk scan remain required. Implementation Plan/TDD/Development stay BLOCKED.
