# COMPILER P2 架构增量

> Revision：`DESIGN-P2-R09`。状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。

## 1. Dependency direction

```text
dec-core-context
  <- neutral immutable facts
  <- exact Guard / generic capability contracts
  <- RuntimeBindingPlan / Requirement contracts
       ^
       |
dec-core-compiler
  <- resolved access IR
  <- production DynamicBindingClassifier
  <- exact rule / runtime-plan publication
       ^
       |
framework execution runtime
  <- ProtectedAccessResolutionContext ownership
  <- actual target resolution for static + runtime access
  <- generic one-shot ResolvedProtectedAccess issuance
  <- ProtectedAccessGateway -> ModelAccessGuard -> same-target execution
  <- RuntimeBindingVerifier only for selected runtime-required rules
```

禁止 context -> compiler、compiler -> concrete parser、split package、global mutable current Context。

## 2. Compile-time authority

Production classifier remains deterministic：

- `DIRECT_EXACT -> STATIC_BOUND -> STATIC_ALLOW`；
- current grammar `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND -> RUNTIME_GUARD_REQUIRED`；
- other unsupported dynamic selector -> `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED` compile ERROR。

`STATIC_ALLOW` compiled rule has no RuntimeBindingPlan/RuntimeAccessRequirement。Runtime-required rule must carry exactly one compiler-published plan + requirement。

## 3. Generic execution resolution authority

R09 replaces the runtime-only capability-creation architecture with a generic protected-access resolution layer。

`ProtectedAccessResolutionContext` is framework-owned and scoped to one current EngineContext, one resolved access-consumer IR, one execution frame/root owner and an optional collection cursor。DIRECT_EXACT normally has no collection cursor; EVERY element evaluation binds the current cursor/element frame。

`ProtectedAccessResolver` resolves the **actual operation target** in that frame and issues one generic one-shot `ResolvedProtectedAccess`。Capability creation does not query PolicyIndex and does not require RuntimeBindingPlan。

This separates two independent concerns：

1. actual target + operation binding — required for every protected access；
2. runtime membership proof — required only when the exact selected rule status is `RUNTIME_GUARD_REQUIRED`。

## 4. Generic operation-bound capability

Every protected access capability internally binds：

- actual target identity；
- current EngineContext；
- exact requested ModelAccessRuleKey；
- operation + payload/action identity；
- execution frame/root owner；
- optional collection cursor/provenance；
- one-shot lifecycle state。

Business code has no capability mint API, no raw target getter and no ability to inject selected policy status/plan/proof。

## 5. ProtectedAccessGateway is the only protected execution boundary

For **both** STATIC_ALLOW and runtime-required access：

```text
framework resolves actual target A
 -> generic ResolvedProtectedAccess A
 -> ProtectedAccessGateway.execute(A)
      -> ModelAccessGuard.authorize(A) exactly once
           -> exact PolicyIndex lookup exactly once
           -> selected rule
           -> static or runtime branch
      -> execute only hidden target+operation bound to A
      -> consume A
```

Gateway performs no second policy lookup。The Guard owns the single exact lookup。

### 5.1 STATIC_ALLOW architecture

```text
selected rule = STATIC_ALLOW
 -> selected rule plan/requirement MUST be empty
 -> RuntimeBindingVerifier calls = 0
 -> evaluator calls = 0
 -> Guard internal fast-path ALLOW
 -> gateway executes same capability-bound target once
```

Architecture forbids a caller-side branch such as `if STATIC_ALLOW then direct operation`。Such a path is `MODEL_ACCESS_GUARD_BYPASS` even when the static authorization itself is valid。

### 5.2 RUNTIME_GUARD_REQUIRED architecture

```text
selected rule = RUNTIME_GUARD_REQUIRED
 -> exact compiler-published plan + requirement MUST exist
 -> RuntimeBindingVerifier verifies capability hidden membership/provenance
    against current Context/rule/plan/frame/cursor
 -> Guard ALLOW only on match
 -> gateway executes same capability-bound target once
```

Thus RuntimeBindingPlan is conditional verification metadata, not a prerequisite to universal capability construction。

## 6. Substitution / TOCTOU / one-shot

Supported architecture has no `execute(capability,target)`, `execute(handle,rawObject)` or callback that receives ALLOW for A and chooses B。A low-level invariant seam observing executor target != capability target must DENY before side effects。

Capability reserve/consume is atomic。For runtime-required access, membership/frame/cursor/Context/plan/rule are revalidated immediately before operation inside the gateway execution boundary。For static access, Context/frame/capability target binding is revalidated and caller still cannot substitute another target。Concurrent replay yields at most one terminal successful consumer。

## 7. Publication closure

System、RuleView、exact ModelPath rules、classifier result、RuntimeBindingPlan/Requirement when applicable、Diagnostic、digest and PolicyIndex remain in one immutable `CompiledModelSet` closure。Execution capability state is context-local runtime state and never mutates compiled facts。

## 8. Guard coverage invariant

The architectural enforcement point for BR-P2-013 / FND-001 is：

```text
all protected READ/WRITE/EXECUTE
 -> ProtectedAccessResolver
 -> ProtectedAccessGateway
 -> ModelAccessGuard
```

`STATIC_ALLOW` is not a separate architecture path；it is only a decision branch inside Guard after exact lookup。

## 9. Compatibility

Existing final EngineContext/P1 APIs remain compatible and additive。Current AC-006 runtime binding verification stays framework runtime logic；future business predicate evaluator remains out of scope without new Requirement。Legacy Config/RuleView compatibility remains read-only until P7。
