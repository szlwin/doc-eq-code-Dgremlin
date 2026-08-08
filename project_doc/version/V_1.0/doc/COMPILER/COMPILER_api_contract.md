# COMPILER P2 API 契约

> Revision：`DESIGN-P2-R09`。输入：`BM-R12` candidate。状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。
> 本文件是当前 canonical P2 API source；Java 示例是 signature contract，生产实现必须兼容 Java 8。

## 1. Compatibility

- Java release 8；禁止 record / `Map.of` / `Map.copyOf` 等 Java 9+ API。
- `EngineContext` 保持 `public final class`，现有单参构造器和 `compiledModelSet()/modelSet()/projection()` 保持兼容。
- P2 API additive only；禁止新增 bare-name RuleView lookup。

## 2. Exact access rule / classifier

`ModelAccessRuleKey = SystemKey + DefinitionKey target + CanonicalModelPath + AccessOperation`。PolicyIndex runtime lookup exact-only。

```java
public enum AccessCompilationStatus { STATIC_ALLOW, RUNTIME_GUARD_REQUIRED }
public enum DynamicBindingClassification { STATIC_BOUND, RUNTIME_OBJECT_BOUND }

public interface DynamicBindingClassifier {
    DynamicBindingResult classify(ResolvedAccessConsumerIr accessIr);
}
```

Frozen rules：

- `DIRECT_EXACT -> STATIC_BOUND -> STATIC_ALLOW`；
- `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND -> RUNTIME_GUARD_REQUIRED`；
- 其它未冻结 dynamic form -> `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED` compile ERROR。

Classifier stub 不得作为 production correctness/AC-006 Evidence。

## 3. Compiled rule / runtime plan invariant

```java
public final class CompiledModelAccessRule {
    public ModelAccessRuleKey key();
    public AccessCompilationStatus status();
    public Optional<RuntimeAccessRequirement> runtimeRequirement();
    public Optional<RuntimeBindingPlan> runtimeBindingPlan();
    public SourceRef sourceRef();
}
```

Invariant：

```text
STATIC_ALLOW
 -> runtimeRequirement.empty
 -> runtimeBindingPlan.empty

RUNTIME_GUARD_REQUIRED
 -> runtimeRequirement.present(EXACT_RUNTIME_BINDING)
 -> runtimeBindingPlan.present(exact compiler-published plan)
```

STATIC_ALLOW 不得为了进入 Guard 伪造 RuntimeBindingPlan。

## 4. Framework-owned `ProtectedAccessResolutionContext`

R09 supersedes the narrower R08 `RuntimeResolutionContext` candidate name with a generic framework execution context used by both static and runtime-bound protected access：

```java
public interface ProtectedAccessResolutionContext {
    String engineContextId();
    AccessConsumerIrKey accessConsumerIrKey();
    RuntimeExecutionFrameId frameId();
    RuntimeResolutionOwnerId ownerResolutionId();
    Optional<RuntimeCollectionCursorId> collectionCursorId();
}
```

Contract：

- framework execution pipeline creates it; no business caller production constructor/factory；
- scoped to one current Context + one resolved access-consumer IR + one execution frame/root owner + optional collection cursor；
- not reusable across Context/frame/rule evaluation/cursor；
- exposes no raw domain object getter；
- cursor is absent for DIRECT_EXACT and present when a runtime collection-element frame requires it。

## 5. Generic one-shot `ResolvedProtectedAccess`

`ResolvedProtectedAccess` is the execution capability for **every** protected READ/WRITE/EXECUTE. It is not a runtime-plan-only token：

```java
public final class ResolvedProtectedAccess {
    // no public/protected constructor; no public mint/factory
    public String capabilityId();
    public String engineContextId();
    public ModelAccessRuleKey requestedRuleKey();
    public AccessOperation operation();
    public RuntimeExecutionFrameId executionFrameId();
}
```

Hidden framework state binds actual target identity, owner/cursor/provenance, operation payload/action identity and one-shot lifecycle state。There is deliberately **no mandatory `RuntimeBindingPlanKey planKey()`** in the generic capability contract。

`requestedRuleKey` is derived from resolved access-consumer IR, not caller-selected policy status。Capability creation does not perform PolicyIndex lookup and does not require a runtime plan。

## 6. Generic resolver

```java
public interface ProtectedAccessResolver {
    ResolvedProtectedAccess resolve(
        ProtectedAccessResolutionContext executionContext,
        ProtectedOperationIntent operationIntent);
}
```

`ProtectedOperationIntent` is framework-owned immutable intent containing the exact requested rule key, operation and required payload/action identity. It exposes no caller-replaceable target object。

Resolver resolves and internally binds the actual target in the current execution frame. It does not decide STATIC_ALLOW vs RUNTIME_GUARD_REQUIRED and does not accept RuntimeBindingPlan as a universal input。

## 7. Runtime-only verification seam

```java
public interface RuntimeBindingVerifier {
    RuntimeBindingVerification verify(
        ResolvedProtectedAccess access,
        CompiledModelAccessRule selectedRule,
        RuntimeBindingPlan plan,
        String engineContextId);
}
```

This seam is invoked **only** after Guard exact lookup selects `RUNTIME_GUARD_REQUIRED`。It verifies hidden framework membership/provenance against the selected rule's exact plan/current Context/frame/cursor。STATIC_ALLOW invocation count = 0。

## 8. Guard / Gateway

```java
public interface ModelAccessGuard {
    ModelAccessDecision authorize(ResolvedProtectedAccess access);
}

public interface ProtectedAccessGateway {
    ProtectedAccessResult execute(ResolvedProtectedAccess access);
}
```

Normative ownership：

- Gateway is the only supported protected execution boundary for both static and runtime paths；
- Gateway calls Guard exactly once and performs **zero** separate PolicyIndex lookup；
- Guard performs exactly one exact PolicyIndex lookup using `access.requestedRuleKey()`；
- Guard-selected `STATIC_ALLOW` -> validates base capability/context/key/op, requires selected rule plan/requirement empty, calls RuntimeBindingVerifier 0 and evaluator 0, then returns internal fast-path ALLOW；
- Guard-selected `RUNTIME_GUARD_REQUIRED` -> requires exact plan/requirement and calls RuntimeBindingVerifier before ALLOW；
- Gateway executes only the actual target+operation already hidden-bound inside the same capability；
- no detached ALLOW is exposed as reusable execution authority；
- capability is consumed after successful execution or terminal DENY。

### 8.1 STATIC_ALLOW supported path

```text
DIRECT_EXACT
 -> STATIC_BOUND
 -> STATIC_ALLOW rule (no plan)
 -> ProtectedAccessResolver -> generic capability
 -> ProtectedAccessGateway.execute
 -> ModelAccessGuard.authorize
      -> exact PolicyIndex lookup = 1
      -> STATIC_ALLOW
      -> RuntimeBindingVerifier = 0
      -> evaluator = 0
 -> same capability-bound target executes once
```

No supported API may directly execute a STATIC_ALLOW protected operation outside Gateway/Guard。

### 8.2 Runtime-required supported path

```text
EVERY_COLLECTION_ELEMENT
 -> runtime rule + plan
 -> ProtectedAccessResolver -> generic capability for current actual element
 -> Gateway -> Guard exact lookup = 1
 -> RuntimeBindingVerifier(selected rule exact plan)
 -> ALLOW
 -> same capability-bound element executes once
```

## 9. Substitution / TOCTOU API shape

Forbidden supported APIs include：

- `execute(capability, target)`；
- `execute(handle, rawObject)`；
- `authorize(A) -> caller callback chooses B`；
- public capability mint/factory；
- public raw target getter；
- caller-side static fast-path executor。

If a low-level invariant seam observes executor target identity != capability-bound target identity, DENY `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH` before operation。Replay -> `RUNTIME_BINDING_CAPABILITY_CONSUMED`。

## 10. RuntimeFactValue / evaluator

`RuntimeFactValue` remains public final, private constructor, six typed immutable factories, deep immutable LIST/OBJECT, typed visitor and deterministic canonical form。Current AC-006 uses runtime binding verification only; future business predicate semantics require a new Requirement revision。

## 11. EngineContext additive surfaces

May expose `contextId()`, owner-qualified lookup, policy status, fail-closed Guard, `ProtectedAccessResolver` and `ProtectedAccessGateway` read surfaces。No new bare-name RuleView API。

## 12. Stable reasons

Compile：`MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED` plus existing P2 diagnostics。

Runtime at least：

- `POLICY_NOT_FOUND`
- `CONTEXT_IDENTITY_MISMATCH`
- `MODEL_ACCESS_GUARD_BYPASS`
- `RUNTIME_BINDING_REQUIRED`
- `RUNTIME_BINDING_PROOF_INVALID`
- `RUNTIME_BINDING_STALE`
- `RUNTIME_BINDING_PLAN_MISMATCH`
- `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH`
- `RUNTIME_BINDING_CAPABILITY_CONSUMED`
- `GUARD_UNAVAILABLE`
- `STATIC_ALLOW`
- `RUNTIME_ALLOW`
- `RUNTIME_DENY`
