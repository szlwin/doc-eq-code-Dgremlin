# COMPILER P2 架构增量

> Revision：`DESIGN-P2-R10`。Base：`DESIGN-P2-R09`。状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。
> 本 Revision 将 R09 抽象的 “framework execution runtime” 映射到真实 Maven reactor；不新增 runtime module，不新增 FND-020。

## 1. Repository-valid dependency direction

```text
dec-core-context
  dec.core.context.model.access.*
  <- neutral compiled/access contracts only
       ^
       | existing compiler dependency
dec-core-compiler
  dec.core.compiler.access.*
  <- classifier / exact rule / runtime-plan publication
       ^
       | existing starter composition dependency
dec-core-starter
  dec.core.starter.access.*
  <- concrete protected-access runtime
  dec.core.starter.access.spi.*
  <- trusted bootstrap-time execution adapters
       ^
       | application/composition dependency
dec-demo / future P3-P7 execution modules
```

Root reactor remains unchanged；there is no P2 `dec-core-runtime` module。Context never depends on compiler/starter；compiler never depends on starter；starter must not add a P2-only dependency on `dec-core-model` to understand business POJOs。

## 2. Why `dec-core-starter` owns runtime enforcement

`dec-core-starter` already owns `CompilerBootstrap` / `CompilerStarter`, depends on compiler + frontends, and is explicitly consumed by `dec-demo`。It is therefore the existing repository composition boundary where concrete runtime infrastructure can be assembled without contaminating context/compiler or inventing a new module。

This does **not** make starter the owner of P3-P7 business execution semantics。Starter owns only the access-control boundary and trusted adapter SPI。

## 3. Compile-time authority stays in compiler

- `DIRECT_EXACT -> STATIC_BOUND -> STATIC_ALLOW(no plan)`；
- `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND -> RUNTIME_GUARD_REQUIRED(plan+requirement)`；
- unsupported dynamic form -> compile ERROR。

Compiler publishes immutable facts into CompiledModelSet。No runtime registry/capability state lives in compiler or semantic digest。

## 4. Neutral contract authority stays in context

Context package owns stable facts/interfaces shared by compiler/starter：rules, plan, requirement, resolution context, operation intent, capability, decision/reasons, Guard/verifier contracts。

`EngineContext` remains immutable data/context; starter concrete runtime is composed around an EngineContext rather than injected as a concrete starter type into context。This preserves no context->starter dependency and existing EngineContext API compatibility。

## 5. Starter concrete runtime graph

```text
ProtectedAccessRuntimeFactory
  + immutable EngineContext
  + immutable ProtectedAccessAdapterRegistry
      |
      v
ProtectedAccessRuntime
  -> DefaultProtectedAccessResolver
       -> ContextLocalProtectedAccessRegistry
       -> trusted ProtectedTargetResolutionPort
  -> DefaultProtectedAccessGateway
       -> DefaultModelAccessGuard
            -> exact PolicyIndex lookup once
            -> DefaultRuntimeBindingVerifier only for runtime-required rule
       -> registry-bound ProtectedOperationExecutionPort
       -> same hidden target operation
```

All components are context-bound; no global mutable `currentContext` or global proof registry。

## 6. Adapter architecture / no reverse business dependency

`dec.core.starter.access.spi.*` defines framework extension ports registered only at runtime composition：

- `ProtectedTargetResolutionPort`
- `ProtectedOperationExecutionPort`
- `ProtectedAccessAdapterRegistry`

Adapter choice is frozen in the runtime composition and selected by framework-owned consumer identity。A business call may not pass a target resolver/executor callback into `execute(...)`。

Future Rule/change/action/query modules may provide adapters by depending on starter SPI (or an upper composition module may bind them), but starter does not depend on those future business modules。Missing adapter is fail-closed `PROTECTED_ACCESS_ADAPTER_UNAVAILABLE`。

## 7. Production consumer integration

All protected consumers use one flow：

```text
consumer execution frame
 -> framework-owned ProtectedAccessResolutionContext + ProtectedOperationIntent
 -> context-bound ProtectedAccessRuntime.execute(...)
 -> resolver binds target
 -> one-shot capability
 -> gateway
 -> Guard exact lookup once
 -> static fast path OR runtime proof verification
 -> same registry-bound execution adapter+target
```

Consumer cannot query PolicyIndex itself、inspect STATIC_ALLOW and bypass、mint capability、replace target after ALLOW or own a second permission registry。

If a future phase introduces a new execution consumer, its Architecture Review must show this integration edge before it may execute protected model access。

## 8. STATIC_ALLOW architecture

STATIC access still allocates a generic one-shot capability because target+operation binding is independent of runtime proof：

```text
DIRECT_EXACT -> STATIC_ALLOW(no plan)
 -> starter resolver binds actual target
 -> gateway -> Guard lookup=1
 -> verifier=0 / evaluator=0
 -> same target operation once
```

No plan is synthesized。No caller-side static path exists。

## 9. Runtime-required architecture

```text
EVERY_COLLECTION_ELEMENT -> runtime rule+plan
 -> starter resolver binds current actual element
 -> gateway -> Guard lookup=1
 -> DefaultRuntimeBindingVerifier validates membership/provenance
 -> same element operation once on match
```

Proof and operation remain atomically bound；A proof cannot authorize B。

## 10. TOCTOU / concurrency

`ContextLocalProtectedAccessRegistry` owns atomic reserve/consume state。Runtime path revalidates Context/frame/cursor/rule/plan/membership immediately before operation；static path revalidates Context/frame/target binding。Concurrent replay yields at most one terminal success；all stale/mismatch paths perform zero protected operation/effects。

## 11. P2 / P3-P7 architecture boundary

P2 implementation may add only starter runtime plumbing + adapter SPI needed to enforce authorization. It must not add Information/Rule/Change/Action/QueryPlan business evaluators, datasource transaction orchestration or source-authored per-object ACL semantics。

Future P3-P7 executors integrate into the starter boundary as trusted adapters/consumers；they do not move the access policy authority out of Guard。

## 12. Exact test-module ownership

- context API/immutability: `dec-core-context`
- classifier/rule compilation: `dec-core-compiler`
- concrete Guard/Gateway/registry/concurrency/no-bypass: `dec-core-starter`
- real existing `systems.xml` end-to-end: `dec-demo`

TESTDESIGN-P2-R11 supplies exact planned TestClass/commands；abstract `<target-module>` is no longer acceptable for P2 blocking cases。

## 13. Review gate

FND-004 remains `PARTIAL_FIX_PROPOSED / OPEN` until this module/package/dependency mapping receives exact Architecture + ApiContract + Develop + Impact + CrossModule + Concurrency Review and valid machine/risk Evidence。No production implementation is claimed by this document。