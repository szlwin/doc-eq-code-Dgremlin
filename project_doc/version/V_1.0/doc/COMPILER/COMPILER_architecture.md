# COMPILER P2 架构增量

> Revision：`DESIGN-P2-R12`。Base：`DESIGN-P2-R11`。状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。
> 本 Revision 不改变 R10/R11 的真实 Maven ownership；只把 trusted execution reachability 与 policy publication/compatibility 做成可落码的跨模块架构闭环。不新增 FND-020。

## 1. Repository dependency direction

```text
dec-core-context
  dec.core.context.model.access.*
  <- neutral access rules / plan / policy index / read contracts
  <- CompiledModelSet / EngineContext additive read API
       ^
       | existing compiler dependency
dec-core-compiler
  <- model-access compilation
  <- ModelAccessPolicyIndex construction
  <- semantic digest + DigestBoundCompiledInput publication
       ^
       | existing starter composition dependency
dec-core-starter
  dec.core.starter.access.*
  <- ProtectedAccessRuntime / Factory
  <- ProtectedExecutionBridge
  <- resolver / gateway / guard / verifier / context-local registry
  dec.core.starter.access.spi.*
  <- trusted state/target/operation ports + bridge receiver
       ^
       | application/composition dependency
dec-demo / future P3-P7 execution modules
  <- register trusted adapter at composition
  <- receive exact bridge capability
```

Root reactor unchanged；no P2 `dec-core-runtime`。Context never depends on compiler/starter；compiler never depends on starter；starter does not acquire a P2-only business dependency on `dec-core-model`。

## 2. Single policy authority architecture

Runtime authorization has exactly one immutable authority chain：

```text
compiler model-access compilation
 -> exact CompiledModelAccessRule iterable
 -> context-owned ModelAccessPolicyIndex.of(...)
 -> immutable index
 -> updated SemanticDigestInput
 -> DigestBoundCompiledInput(index + digest)
 -> CompiledModelSet.published(...same index...)
 -> EngineContext.modelAccessPolicyIndex()
 -> DefaultModelAccessGuard.find(exact key) once
```

There is no second runtime permission source。

Forbidden architectural alternatives：

- Guard scanning `CompiledModelSet.definitions()`；
- Guard rebuilding from `TypedDefinitionRegistries`；
- starter `Map<ModelAccessRuleKey,...>` used as authorization authority；
- resolver/gateway/verifier/adapter querying/reselecting policy；
- policy index attached after semantic digest computation；
- different index snapshot used for digest and publication。

## 3. Policy construction/publication boundary

`ModelAccessPolicyIndex` belongs to context because compiler and runtime both need the immutable cross-module contract。Compiler remains the **production publisher**。

Validated factory：

```text
ModelAccessPolicyIndex.of(Iterable<CompiledModelAccessRule>)
```

performs duplicate/key/state/canonical-path validation and returns frozen deterministic index。

`CompiledModelSet` has two explicit architecture paths：

```text
LEGACY COMPATIBILITY
existing 8-arg constructor
 -> existing model snapshot
 -> ModelAccessPolicyIndex.empty()
 -> no policy reconstruction
 -> protected runtime fail closed on exact miss

P2 PRODUCTION PUBLICATION
compiler policy compilation
 -> validated immutable index
 -> digest-bound closure
 -> CompiledModelSet.published(...index...)
 -> EngineContext
```

The legacy path is retained for source/binary compatibility and P1 tests, not used by P2 production publication once protected access rules are compiled。

## 4. Existing compiler publication pipeline impact

Current repository already has：

```text
DigestBoundCompiledInput.bind(...)
 -> CompiledModelSetBuilder.FrozenInput
 -> FrozenInput.candidate(...)
 -> new CompiledModelSet(existing 8 args)
 -> new EngineContext(modelSet)
```

R12 freezes the P2 evolution：

```text
model-access compilation before digest bind
 -> ModelAccessPolicyIndex.of(...)
 -> DigestBoundCompiledInput.bind(..., same index, ...)
 -> SemanticDigestInput includes same index
 -> FrozenInput carries same index
 -> FrozenInput.candidate(...)
 -> CompiledModelSet.published(..., same index, ...)
 -> EngineContext
```

This preserves the existing T13/T14 provenance principle: the model facts used for the semantic digest and the facts eventually published into Context are one immutable closure。

## 5. Why public `execute(context,intent)` is no longer the external path

R11 correctly made context/intent non-authoritative, but left a reachability cycle：external consumer needed an issued pair before calling the public method, while pair issuance was package-private starter code。

R12 removes that cycle by making issued pair an **internal implementation artifact** and exporting a capability-style bridge instead。

```text
external trusted execution module
   DOES NOT receive issueInvocation access
   DOES NOT mint context/intent
   DOES NOT pass rule/op/frame/owner/cursor per call
       |
       v
composition-issued ProtectedExecutionBridge
       |
       v
opaque execution token recognized by bound trusted state port
       |
       v
starter internal issuance
       |
       v
internal issued pair -> capability -> Guard -> operation
```

`ProtectedAccessRuntime` remains the context-bound composition owner, while `ProtectedExecutionBridge` is the only production invocation capability handed upward。

## 6. Bridge composition graph

At `ProtectedAccessRuntimeFactory` composition time：

```text
EngineContext
 + frozen ProtectedAccessAdapterRegistry registrations
      each registration binds:
        AccessConsumerIrKey
        exact ModelAccessRuleKey
        AccessOperation
        ProtectedExecutionStatePort
        ProtectedTargetResolutionPort
        ProtectedOperationExecutionPort
        ProtectedExecutionBridgeReceiver
          |
          v
factory validates/fixes registration for this runtime
          |
          v
creates one immutable ProtectedExecutionBridge
          |
          v
receiver receives that exact bridge once
```

The receiver/caller cannot later ask `bridgeFor(otherRuleKey)` or rebind bridge fields。Possession of the delivered bridge is the composition capability granted to that trusted execution adapter。

Composition is the framework trust boundary；per-business-call code is not。

## 7. Per-invocation bridge flow

```text
future Rule/change/action/query executor current execution occurrence
 -> adapter-private/adapter-recognized ProtectedExecutionToken
 -> its exact composition-issued bridge.execute(token)
 -> bridge checks runtime/context active
 -> bound ProtectedExecutionStatePort.recognizes(token)
 -> derives frame/owner/cursor from the bound trusted state port
 -> consumer/rule/operation come from immutable bridge binding, NOT token/caller
 -> starter internal issueInvocation(...)
 -> ContextLocalProtectedAccessRegistry stores issued pair
 -> internal requireIssuedPair
 -> resolver binds actual target + adapter + operation
 -> one-shot capability
 -> gateway
 -> Guard exact current-context policy lookup once
 -> STATIC fast path or runtime proof
 -> same bound target operation
```

This flow is production-reachable from `dec-demo`/future modules without package access, reflection or test-only minting。

## 8. Opaque execution token trust model

`ProtectedExecutionToken` is deliberately a marker with no authority getters。An arbitrary caller may implement the Java interface, but that token is useless unless the bridge's already-bound trusted `ProtectedExecutionStatePort` recognizes it as one of its current execution occurrences。

The state port may expose only execution-occurrence facts needed by starter：frame、owner、optional cursor。It does **not** choose consumerIrKey、ruleKey or operation per call；those are frozen in the bridge registration。

Foreign/fabricated/stale token：

```text
PROTECTED_EXECUTION_TOKEN_UNTRUSTED
 -> internal issuance 0
 -> target resolution 0
 -> capability 0
 -> Guard/policy lookup 0
 -> operation/effects 0
```

## 9. Internal issued pair remains defense-in-depth

R11 `IssuedProtectedAccessResolutionContext`、`IssuedProtectedOperationIntent`、`IssuedInvocationRecord` and exact pair identity validation remain starter-internal。

Bridge does not return these objects to external consumers。Internal forged/pair-mismatch checks remain useful for implementation invariants and tests, but they are no longer a prerequisite external API that blocks production reachability。

## 10. Guard/Gateway runtime graph

```text
ProtectedExecutionBridge
 -> internal issuance/authenticity
 -> DefaultProtectedAccessResolver
      -> ContextLocalProtectedAccessRegistry
      -> bound target-resolution port
 -> DefaultProtectedAccessGateway
      -> DefaultModelAccessGuard
           -> EngineContext.modelAccessPolicyIndex().find(key) exactly once
           -> DefaultRuntimeBindingVerifier only for runtime-required
      -> registry-bound operation port
      -> same hidden target
```

- STATIC_ALLOW：plan/verifier/evaluator 0；
- runtime-required：exact compiler plan + verifier；
- Gateway/resolver/verifier/adapter policy lookup 0；
- capability/target substitution forbidden；
- terminal ALLOW/DENY consumes capability。

## 11. Legacy compatibility architecture

Legacy direct construction is explicitly isolated：

```text
new CompiledModelSet(existing 8 args)
 -> immutable model facts as today
 -> policy index = empty
 -> EngineContext still constructible
 -> existing P1 reads/projection remain compatible
 -> P2 protected access: exact policy miss, fail closed
```

It must never silently reconstruct access policy from definitions。This avoids two incompatible authorities and makes the compatibility behavior deterministic/testable。

## 12. P2 / later-phase boundary

P2 bridge state port is not a Rule/change/action/query executor。Future modules own their business execution state and opaque token lifecycle, but must register the required trusted SPI and use the bridge for every protected model operation。

P2 does not implement：full business evaluator、query plan、datasource transaction、business side effects、per-object ACL DSL。

Future phase Architecture Review must demonstrate bridge integration before any protected operation path is allowed。

## 13. Concurrency / TOCTOU

- runtime/bridge registration immutable after factory freeze；
- bridge cannot switch rule/op/ports at runtime；
- token recognition and frame-state derivation occur before internal issuance；
- issued pair/capability registry is per EngineContext runtime；
- capability reserve/consume atomic，concurrent replay at most one terminal success；
- runtime branch revalidates Context/frame/cursor/rule/plan/membership immediately before operation；
- immutable policy index prevents policy-source swap between lookup and execution。

## 14. Exact test ownership

- context：`ModelAccessPolicyIndex` factory + legacy/new CompiledModelSet publication compatibility；
- compiler：index publication before digest bind、same-snapshot digest/publication；
- starter：bridge API/registration/token fail closed、internal issued-pair defense、single Guard authority；
- dec-demo：real production bridge reachability from existing `systems.xml` source to protected operation。

TESTDESIGN-P2-R13 freezes exact planned TestClass/commands。

## 15. Review gate

FND-004 / FND-015 / FND-016 remain `PARTIAL_FIX_PROPOSED / OPEN` until exact `DESIGN-P2-R12` Architecture + ApiContract + Develop + Impact + CrossModule + Concurrency Reviews independently confirm this architecture。FND-001/FND-007/FND-019 remain formally OPEN as recorded。No FND-020。Implementation Plan/TDD/Development remain BLOCKED。