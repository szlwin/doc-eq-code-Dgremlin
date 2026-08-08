# COMPILER P2 API 契约

> Revision：`DESIGN-P2-R08`。输入：`BM-R11` candidate。状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。
> 本文件是当前 canonical P2 API source；Java 示例是 signature contract，生产实现必须兼容 Java 8。

## 1. Compatibility

- Java release 8；禁止 record / `Map.of` / `Map.copyOf` 等 Java 9+ API。
- `EngineContext` 保持 `public final class`，现有单参构造器和 `compiledModelSet()/modelSet()/projection()` 保持兼容。
- P2 API additive only；禁止新增 bare-name RuleView lookup。

## 2. Exact access rule / classifier

`ModelAccessRuleKey = SystemKey + DefinitionKey target + CanonicalModelPath + AccessOperation`。PolicyIndex 只允许一次 exact lookup，无 wildcard/fallback。

```java
public enum AccessCompilationStatus { STATIC_ALLOW, RUNTIME_GUARD_REQUIRED }
public enum DynamicBindingClassification { STATIC_BOUND, RUNTIME_OBJECT_BOUND }

public interface DynamicBindingClassifier {
    DynamicBindingResult classify(ResolvedAccessConsumerIr accessIr);
}
```

Frozen rules：`DIRECT_EXACT -> STATIC_BOUND`；current grammar `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND`；其它动态形式 -> `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED` compile ERROR。Classifier stub 不得作为 production correctness/AC-006 Evidence。

## 3. RuntimeBindingPlan / requirement

```java
public final class CompiledModelAccessRule {
    public ModelAccessRuleKey key();
    public AccessCompilationStatus status();
    public Optional<RuntimeAccessRequirement> runtimeRequirement();
    public Optional<RuntimeBindingPlan> runtimeBindingPlan();
    public SourceRef sourceRef();
}

public final class RuntimeBindingPlan {
    public enum Kind { COLLECTION_ELEMENT_MEMBERSHIP }
    public RuntimeBindingPlanKey key();
    public ModelAccessRuleKey authorizedRuleKey();
    public CanonicalModelPath collectionPath();
    public CanonicalModelPath elementRelativePath();
}

public final class RuntimeAccessRequirement {
    public enum Kind { EXACT_RUNTIME_BINDING }
    public RuntimeRequirementKey key();
    public ModelAccessRuleKey authorizedRuleKey();
    public RuntimeBindingPlanKey planKey();
    public Kind kind();
}
```

Factories remain context-owned public validated factories callable by compiler。Runtime authority comes only from current compiler-published selected rule。

## 4. RuntimeResolutionContext ownership

```java
public interface RuntimeResolutionContext {
    String engineContextId();
    AccessConsumerIrKey accessConsumerIrKey();
    RuntimeExecutionFrameId frameId();
    RuntimeResolutionOwnerId ownerResolutionId();
    Optional<RuntimeCollectionCursorId> collectionCursorId();
}
```

Contract：

- framework execution pipeline creates it; no business caller public constructor/factory；
- bound to one current Context + one access-consumer IR + one execution frame/root owner + optional collection cursor；
- not reusable across Context/frame/rule evaluation/cursor；
- exposes no raw domain object getter；
- frame/cursor/owner invalidation makes derived runtime capability stale。

## 5. R08 operation-bound capability

R07 detached handle verification is insufficient as final execution authority because proof A could otherwise be followed by operation on B。The supported runtime-bound API now uses a one-shot framework capability：

```java
public final class ResolvedProtectedAccess {
    // no public/protected constructor; no public mint/factory
    public String capabilityId();
    public String engineContextId();
    public ModelAccessRuleKey selectedRuleKey();
    public RuntimeBindingPlanKey planKey();
    public AccessOperation operation();
}

public interface RuntimeBindingResolver {
    ResolvedProtectedAccess resolve(
        RuntimeBindingPlan plan,
        RuntimeResolutionContext executionContext,
        ProtectedOperationIntent operationIntent);
}

public interface ProtectedAccessGateway {
    ProtectedAccessResult execute(ResolvedProtectedAccess access);
}
```

`ProtectedOperationIntent` is framework-owned immutable intent derived from the current resolved access-consumer IR。It contains the already-determined operation/payload/action identity required by the framework, **not an arbitrary replacement target object**。

`ResolvedProtectedAccess` internally binds actual object identity, collection owner/membership provenance, current frame/cursor, exact selected rule, exact plan, operation intent and one-shot lifecycle state。Those hidden identities are not exposed as raw POJO accessors。

## 6. ModelAccessGuard integration

```java
public interface ModelAccessGuard {
    ModelAccessDecision authorize(ResolvedProtectedAccess access);
}
```

For `RUNTIME_GUARD_REQUIRED` the normal supported execution path is：

```text
ResolvedProtectedAccess
 -> ProtectedAccessGateway.execute
 -> exact PolicyIndex lookup once
 -> ModelAccessGuard.authorize(the same capability)
 -> ALLOW only if current proof/membership/frame/plan/rule all match
 -> framework executes the actual target+operation already bound inside that same capability
```

A detached `ALLOW` result is **not** a reusable execution authority。There is no supported API shaped as：

```text
authorize(handle/proof A) -> then execute(object B)
```

and no `execute(capability, target)` / `execute(handle, rawObject)` / caller callback that can select a second protected target。

Capability is consumed after successful execution or terminal DENY。Replay -> `RUNTIME_BINDING_CAPABILITY_CONSUMED`。If actual executor target identity differs from the capability-bound identity on an invariant-test seam -> `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH` before operation。

## 7. Membership change / TOCTOU

Immediately before operation, gateway revalidates current Context + frame/cursor + selected rule/plan + membership/provenance。If collection ownership/membership changed since resolve, capability is stale and DENY before protected operation。Implementation may use a context-local resolution registry/version/critical section, but may not authorize an old proof and then operate on a newly selected arbitrary object。

## 8. Static access

`STATIC_ALLOW` still enters Guard and remains Guard-internal fast path。FND-019 operation-bound capability is mandatory for `RUNTIME_GUARD_REQUIRED`; it does not permit caller-side Guard bypass for static access。

## 9. RuntimeFactValue / evaluator

`RuntimeFactValue` remains public final, private constructor, six typed immutable factories, deep immutable collection/object values, typed visitor, deterministic canonical form。Current AC-006 uses binding proof/capability, not business predicate evaluator。Future predicate semantics require a new Requirement revision。

## 10. EngineContext additive surfaces

May expose `contextId()`, owner-qualified lookup, policy status, fail-closed Guard, runtime resolver and `ProtectedAccessGateway` read surfaces。No new bare-name RuleView API。

## 11. Stable reasons

Compile：`MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED` plus existing P2 diagnostics。

Runtime at least：

- `RUNTIME_BINDING_REQUIRED`
- `RUNTIME_BINDING_PROOF_INVALID`
- `RUNTIME_BINDING_STALE`
- `RUNTIME_BINDING_PLAN_MISMATCH`
- `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH`
- `RUNTIME_BINDING_CAPABILITY_CONSUMED`
- `CONTEXT_IDENTITY_MISMATCH`
- `POLICY_NOT_FOUND`
- `GUARD_UNAVAILABLE`
- `STATIC_ALLOW`
- `RUNTIME_ALLOW`
- `RUNTIME_DENY`
