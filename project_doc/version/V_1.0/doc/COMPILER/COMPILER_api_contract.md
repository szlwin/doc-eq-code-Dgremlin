# COMPILER P2 API 契约

> Revision：`DESIGN-P2-R10`。输入：`BM-R12` candidate。状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。
> 本 Revision 在 R09 统一 Guard semantics 上冻结 repository-valid module/package ownership；生产实现必须 Java 8 compatible。

## 1. Maven / package ownership

| Concern | Maven module | Package / owner |
|---|---|---|
| Neutral access contracts/facts | `dec-core-context` | `dec.core.context.model.access.*` |
| Classifier/rule/plan publication | `dec-core-compiler` | `dec.core.compiler.access.*` |
| Concrete protected runtime | `dec-core-starter` | `dec.core.starter.access.*` |
| Trusted framework adapter SPI | `dec-core-starter` | `dec.core.starter.access.spi.*` |
| Real source integration fixture | `dec-demo` | `dec-demo` tests/resources |

No new Maven runtime module is introduced by P2. `dec-core-starter` remains above compiler/frontends and does not add a P2 dependency on `dec-core-model` merely to implement access control.

## 2. Compatibility

- Java release 8；禁止 record / `Map.of` / `Map.copyOf` 等 Java 9+ API。
- `EngineContext` 保持 `public final class`、现有单参 constructor 和 `compiledModelSet()/modelSet()/projection()`。
- P2 API additive only；no bare-name RuleView lookup。
- Concrete starter runtime is composed **around** an immutable EngineContext; context never depends on starter concrete classes。

## 3. Exact access rule / classifier

```java
public enum AccessCompilationStatus { STATIC_ALLOW, RUNTIME_GUARD_REQUIRED }
public enum DynamicBindingClassification { STATIC_BOUND, RUNTIME_OBJECT_BOUND }

public interface DynamicBindingClassifier {
    DynamicBindingResult classify(ResolvedAccessConsumerIr accessIr);
}
```

Frozen production rules：
- `DIRECT_EXACT -> STATIC_BOUND -> STATIC_ALLOW`；
- `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND -> RUNTIME_GUARD_REQUIRED`；
- 其它 dynamic form -> `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED` compile ERROR。

```java
public final class CompiledModelAccessRule {
    public ModelAccessRuleKey key();
    public AccessCompilationStatus status();
    public Optional<RuntimeAccessRequirement> runtimeRequirement();
    public Optional<RuntimeBindingPlan> runtimeBindingPlan();
    public SourceRef sourceRef();
}
```

Invariant：STATIC_ALLOW has no plan/requirement；RUNTIME_GUARD_REQUIRED has exactly one compiler-published exact plan + requirement。

## 4. Context neutral contracts (`dec-core-context`)

```java
public interface ProtectedAccessResolutionContext {
    String engineContextId();
    AccessConsumerIrKey accessConsumerIrKey();
    RuntimeExecutionFrameId frameId();
    RuntimeResolutionOwnerId ownerResolutionId();
    Optional<RuntimeCollectionCursorId> collectionCursorId();
}

public final class ResolvedProtectedAccess {
    public String capabilityId();
    public String engineContextId();
    public ModelAccessRuleKey requestedRuleKey();
    public AccessOperation operation();
    public RuntimeExecutionFrameId executionFrameId();
    // no public/protected constructor, factory, raw target getter or selected-policy setter
}

public interface ModelAccessGuard {
    ModelAccessDecision authorize(ResolvedProtectedAccess access);
}

public interface RuntimeBindingVerifier {
    RuntimeBindingVerification verify(
        ResolvedProtectedAccess access,
        CompiledModelAccessRule selectedRule,
        RuntimeBindingPlan plan,
        String engineContextId);
}
```

`requestedRuleKey` comes from resolved access-consumer IR, not caller-selected policy status。

## 5. Starter facade / concrete implementation ownership

### 5.1 Public composition facade

```java
package dec.core.starter.access;

public final class ProtectedAccessRuntime {
    public ProtectedAccessResult execute(
        ProtectedAccessResolutionContext context,
        ProtectedOperationIntent intent);
}

public final class ProtectedAccessRuntimeFactory {
    public static ProtectedAccessRuntime create(
        EngineContext context,
        ProtectedAccessAdapterRegistry trustedAdapters);
}
```

The factory creates one context-bound composition of resolver + registry + gateway + guard + runtime verifier. It does not use global current state。

### 5.2 Concrete implementation classes

Frozen owner package `dec.core.starter.access`：

```text
DefaultProtectedAccessResolver
DefaultProtectedAccessGateway
DefaultModelAccessGuard
DefaultRuntimeBindingVerifier
ContextLocalProtectedAccessRegistry
```

These classes should be package-private where public exposure is unnecessary；their ownership is normative even if exact visibility is narrowed during implementation。

### 5.3 Resolver contract

Conceptually implemented by `DefaultProtectedAccessResolver`：

```java
ResolvedProtectedAccess resolve(
    ProtectedAccessResolutionContext context,
    ProtectedOperationIntent intent);
```

It resolves/binds the actual target through the trusted adapter registry, records target+operation+frame/owner/cursor/provenance in the context-local registry, returns a one-shot capability, and performs **zero PolicyIndex lookup**。

## 6. Trusted adapter SPI (`dec-core-starter`)

```text
dec.core.starter.access.spi.ProtectedTargetResolutionPort
dec.core.starter.access.spi.ProtectedOperationExecutionPort
dec.core.starter.access.spi.ProtectedAccessAdapterRegistry
```

Normative SPI rules：
- registered/frozen only when `ProtectedAccessRuntimeFactory` composes the runtime；
- not accepted as per-call callbacks by `execute(...)`；
- selected from framework-owned consumer identity, never from a caller-supplied raw target；
- resolver records the selected target-resolution/execution adapter binding inside the capability registry；
- Gateway may invoke only the exact execution port bound at capability issuance；
- missing supported adapter -> `PROTECTED_ACCESS_ADAPTER_UNAVAILABLE` before operation；
- future P3-P7 adapter modules depend on this starter SPI; starter does not depend upward on those business executor modules。

## 7. Guard / Gateway exact responsibilities

`DefaultProtectedAccessGateway` is the sole supported execution boundary. `DefaultModelAccessGuard` owns the single policy lookup。

```text
ProtectedAccessRuntime.execute(context,intent)
 -> DefaultProtectedAccessResolver -> capability
 -> DefaultProtectedAccessGateway.execute(capability)
      -> DefaultModelAccessGuard.authorize(capability) exactly once
           -> PolicyIndex exact lookup exactly once
           -> selected rule
      -> same capability-bound execution port + hidden target
      -> consume capability
```

Gateway PolicyIndex lookup count = 0。

### 7.1 STATIC_ALLOW

```text
selected STATIC_ALLOW
 -> selected plan/requirement absent
 -> RuntimeBindingVerifier calls = 0
 -> evaluator calls = 0
 -> Guard-internal ALLOW
 -> same hidden target executes once
```

STATIC_ALLOW never creates/fakes RuntimeBindingPlan and never becomes a caller-side bypass。

### 7.2 RUNTIME_GUARD_REQUIRED

```text
selected runtime-required rule
 -> exact plan+requirement required
 -> DefaultRuntimeBindingVerifier verifies hidden membership/provenance
    against Context/rule/plan/frame/cursor
 -> ALLOW only on match
 -> same hidden target executes once
```

Verifier does not re-query PolicyIndex or select a different rule。

## 8. Capability / target substitution API shape

Forbidden supported API shapes：
- `execute(capability,target)`；
- `execute(handle,rawObject)`；
- per-call caller callback selecting target/adapter after authorization；
- public capability mint/factory/raw target getter；
- `if STATIC_ALLOW then directOperation(target)` outside runtime/gateway/Guard。

Low-level target mismatch -> `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH` before operation。Replay -> `RUNTIME_BINDING_CAPABILITY_CONSUMED`。

## 9. Production consumer integration contract

Current/future Rule/change/custom action/protected-query execution consumers MUST receive a context-bound `ProtectedAccessRuntime` from application composition and invoke its `execute(context,intent)` surface. They may implement trusted adapter SPI in their own phase/module, but may not own a second policy lookup, second permission registry or direct protected operation path。

P2 intentionally does not define the business semantics of those future executors；it defines only their mandatory access-control integration boundary。

## 10. RuntimeFactValue / future evaluator

`RuntimeFactValue` remains public final/private constructor/six typed immutable factories/deep immutable LIST+OBJECT/typed visitor/deterministic canonical form。Current AC-006 uses runtime binding verification, not business predicate evaluator；future predicate semantics require a new Requirement revision。

## 11. Stable reasons

Runtime at least：
- `POLICY_NOT_FOUND`
- `CONTEXT_IDENTITY_MISMATCH`
- `MODEL_ACCESS_GUARD_BYPASS`
- `PROTECTED_ACCESS_ADAPTER_UNAVAILABLE`
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

## 12. Review gate

This API is a candidate contract, not implementation Evidence。FND-004 remains OPEN until exact-revision ApiContract/Architecture/Develop/Impact/CrossModule/Concurrency Reviews and RC9 machine/risk Evidence close it。Implementation Plan/TDD/Development remain BLOCKED。