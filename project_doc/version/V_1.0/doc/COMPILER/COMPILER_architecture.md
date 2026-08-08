# COMPILER P2 架构增量

> Revision：`DESIGN-P2-R11`。Base：`DESIGN-P2-R10`。状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。
> 本 Revision 保持 R10 的真实 Maven ownership，只补两个架构 authority：trusted issued invocation input 与 single immutable ModelAccessPolicyIndex。无新 FND-020。

## 1. Repository-valid dependency direction

```text
dec-core-context
  dec.core.context.model.access.*
  <- neutral access contracts
  <- immutable ModelAccessPolicyIndex
       ^
       | existing compiler dependency
dec-core-compiler
  dec.core.compiler.access.*
  <- classifier / exact rule / runtime-plan publication
  <- ModelAccessPolicyIndex assembly + digest contribution
       ^
       | existing starter composition dependency
dec-core-starter
  dec.core.starter.access.*
  <- concrete protected runtime
  <- issued input implementations + context-local issuance registry
  dec.core.starter.access.spi.*
  <- trusted composition-time execution adapters
       ^
       | application/composition dependency
dec-demo / future P3-P7 execution modules
```

Root reactor unchanged；no P2 `dec-core-runtime`。Context never depends on compiler/starter；compiler never depends on starter；starter does not gain P2-only `dec-core-model` business coupling。

## 2. Compile-time authority

Compiler remains sole producer of exact runtime access facts：

- `DIRECT_EXACT -> STATIC_BOUND -> STATIC_ALLOW(no plan)`；
- `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND -> RUNTIME_GUARD_REQUIRED(plan+requirement)`；
- unsupported dynamic form -> compile ERROR。

Compiler deterministically assembles one `ModelAccessPolicyIndex` from final exact `CompiledModelAccessRule`s and publishes it in the same `CompiledModelSet` closure。

## 3. Single policy authority architecture

```text
compiler exact rules
 -> ModelAccessPolicyIndex (immutable, exact-only)
 -> CompiledModelSet.modelAccessPolicyIndex()
 -> EngineContext.modelAccessPolicyIndex()
 -> DefaultModelAccessGuard.find(exact key) exactly once
```

This is the only runtime authorization authority。

Forbidden alternative authorities：

- scan `CompiledModelSet.definitions()` for access rules；
- rebuild rules from `TypedDefinitionRegistries`；
- starter-side independent `Map<ModelAccessRuleKey,...>`；
- resolver/gateway/verifier/adapter policy lookup；
- caller-supplied rule/status/plan。

The semantic digest covers canonical exact policy entries；runtime capability/issuance state does not。

## 4. EngineContext preserves one immutable authority

`EngineContext` remains immutable and context-owned。Its additive `modelAccessPolicyIndex()` exposes the same immutable policy object/authority already inside `CompiledModelSet`; it is not a derived cache。

Therefore context replacement is also policy-authority replacement：a capability/input issued for C0 cannot be authorized against C1 even if individual rule keys happen to match。

## 5. Public input interfaces are only read views

`ProtectedAccessResolutionContext` and `ProtectedOperationIntent` stay in context to avoid starter reverse coupling, but interface implementation is not authority。

Production objects are package-private starter implementations：

```text
IssuedProtectedAccessResolutionContext
IssuedProtectedOperationIntent
```

Their exact object identities are registered in a `ContextLocalProtectedAccessRegistry.IssuedInvocationRecord`。

## 6. Trusted issuance architecture

```text
application/framework composition
 -> immutable trusted ProtectedAccessAdapterRegistry
 -> current trusted framework execution adapter/state
 -> starter internal issueInvocation(...)
 -> exact issued context A + exact issued intent A
 -> context-local authoritative IssuedInvocationRecord A
```

There is no public per-business-call mint/sign API。A caller implementing the public interfaces itself receives no authority。

Composition-time adapter registration is a trusted framework boundary；Rule source/business operation callers are outside that boundary。

## 7. Authenticity enforcement point

Every call begins：

```text
ProtectedAccessRuntime.execute(context,intent)
 -> ContextLocalProtectedAccessRegistry.requireIssuedPair(context,intent)
```

The registry validates exact reference identity and pair relationship before any target resolution。

```text
unknown caller context/intent
 -> PROTECTED_ACCESS_INPUT_UNTRUSTED

issued context A + issued intent B
 -> PROTECTED_ACCESS_INPUT_PAIR_MISMATCH
```

For these failures：

```text
target resolver = 0
capability issuance = 0
PolicyIndex lookup = 0
protected operation = 0
external effect = 0
```

Getter values are diagnostic projections only；downstream resolver uses authoritative registry record values。

## 8. Starter protected runtime graph

```text
ProtectedAccessRuntime
  -> input-authenticity gate (registry)
  -> DefaultProtectedAccessResolver
       -> trusted target-resolution port
       -> one-shot capability registry binding
  -> DefaultProtectedAccessGateway
       -> DefaultModelAccessGuard
            -> EngineContext.ModelAccessPolicyIndex exact lookup once
            -> DefaultRuntimeBindingVerifier only for runtime-required rule
       -> registry-bound ProtectedOperationExecutionPort
       -> same hidden target operation
```

No global mutable current Context, no global issuance registry, no secondary policy registry。

## 9. STATIC_ALLOW architecture

```text
issued pair PASS
 -> resolver binds actual target
 -> capability
 -> gateway -> Guard policy index lookup=1
 -> selected STATIC_ALLOW(no plan)
 -> verifier=0 / evaluator=0
 -> same target operation once
```

No caller-side static direct path and no runtime plan synthesis。

## 10. Runtime-required architecture

```text
issued pair PASS
 -> resolver binds current actual element
 -> capability
 -> gateway -> Guard policy index lookup=1
 -> selected runtime rule + exact compiler plan
 -> verifier validates current membership/provenance
 -> same target operation once on match
```

A/B substitution and stale membership remain fail closed。

## 11. Input substitution and policy substitution are separate protections

Input authenticity prevents caller authority escalation **before capability creation**：

- forged consumerIrKey/frame/owner/cursor；
- forged requestedRuleKey；
- READ -> WRITE/EXECUTE intent substitution；
- context A + intent B mixing。

Policy authority prevents runtime implementation divergence **after capability creation**：

- no copied policy Map；
- no definitions scan；
- no resolver/gateway/verifier/adapter rule selection；
- Guard exact current-context index is final authority。

Both must hold；one does not replace the other。

## 12. TOCTOU / concurrency

IssuedInvocationRecord and capability state are context-local and lifecycle-bound。Expired frame/cursor/context or replay fails closed。Capability reserve/consume atomic；concurrent execute yields at most one terminal success。

Because policy index is immutable and retained by EngineContext, Guard cannot observe two policy registries during one operation。Context replacement invalidates issued state rather than hot-swapping policy under an existing capability。

## 13. Adapter/no-bypass architecture

Adapters remain composition-time frozen SPI：

- `ProtectedTargetResolutionPort`
- `ProtectedOperationExecutionPort`
- `ProtectedAccessAdapterRegistry`

No per-call raw target/callback/adapter selection。Missing adapter -> `PROTECTED_ACCESS_ADAPTER_UNAVAILABLE` before operation。Future P3-P7 modules integrate above starter and cannot create their own access-policy authority。

## 14. P2 / P3-P7 boundary

P2 includes only access-control publication, issued-input authenticity, runtime Guard/Gateway/registry/verifier/factory and adapter SPI plumbing。

P2 excludes full Rule/change/action/query evaluators, QueryPlan, datasource transaction semantics, source-authored object ACL predicates and business side effects。

## 15. Test ownership

- `dec-core-context`: `ModelAccessPolicyIndex` immutable/exact API + neutral input read contracts。
- `dec-core-compiler`: policy index publication + semantic digest determinism。
- `dec-core-starter`: forged-input rejection, issued-pair matching, Guard single-index lookup, no secondary map, static/runtime/proof/concurrency behavior。
- `dec-demo`: real `systems.xml` source -> published index -> issued runtime -> operation integration。

TESTDESIGN-P2-R12 freezes exact TestClass/commands。

## 16. Review gate

FND-004 remains `PARTIAL_FIX_PROPOSED / OPEN` until exact Architecture + ApiContract + Develop + Impact + CrossModule + Concurrency Review validates both authorities and machine/risk Evidence binds the revision。No production implementation is claimed。