# COMPILER P2 架构增量

> Revision：`DESIGN-P2-R08`。状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。

## 1. Dependency direction

```text
dec-core-context
  <- neutral immutable facts
  <- exact Guard/capability contracts
  <- RuntimeBindingPlan/Requirement contracts
       ^
       |
dec-core-compiler
  <- resolved access IR
  <- production DynamicBindingClassifier
  <- plan/rule publication
       ^
       |
framework execution runtime
  <- RuntimeResolutionContext ownership
  <- actual object resolution
  <- one-shot ResolvedProtectedAccess issuance
  <- ProtectedAccessGateway verify+execute
```

禁止 context -> compiler、compiler -> concrete parser、split package、global mutable current Context。

## 2. Compile-time authority

Production classifier remains deterministic：

- `DIRECT_EXACT -> STATIC_BOUND`；
- current grammar `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND`；
- any other unresolved/unsupported dynamic selector -> `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED` compile ERROR。

真实 `systems.xml / order.ordered` fixture 是 production classifier acceptance source；stub 不能作为 classifier correctness Evidence。

## 3. Runtime resolution ownership

`RuntimeResolutionContext` belongs to the framework execution pipeline, not business code。It is scoped to one current EngineContext、resolved access-consumer IR、execution frame/root owner and optional collection cursor。It exposes no raw domain object API and cannot be reused across Context/frame/cursor boundaries。

Compiler-published `RuntimeBindingPlan(COLLECTION_ELEMENT_MEMBERSHIP)` describes the exact runtime membership boundary。Framework runtime resolves the actual element under that plan。

## 4. Operation-bound capability

The runtime resolver does not hand a detached authorization proof to a caller that can later choose another target。It creates one one-shot `ResolvedProtectedAccess` capability whose hidden framework state binds：

- actual target identity；
- collection owner/membership provenance；
- current frame/cursor；
- current EngineContext；
- exact selected rule and plan；
- exact protected operation intent。

Business code has no capability mint API and no raw target getter。

## 5. ProtectedAccessGateway is the supported execution boundary

For `RUNTIME_GUARD_REQUIRED`：

```text
framework resolves actual target A
 -> issues ResolvedProtectedAccess A
 -> ProtectedAccessGateway.execute(A)
      -> exact policy lookup once
      -> Guard verifies the same capability/proof
      -> revalidates frame/cursor/membership
      -> executes the target+operation internally bound to A
      -> consumes A
```

Architecture forbids：

```text
Guard ALLOW for proof A
 -> caller chooses arbitrary object B
 -> protected operation on B
```

There is no supported `execute(capability, target)` / `execute(handle, rawObject)` / callback API that can select a second protected object。If a low-level invariant seam observes executor target identity != capability target identity, it DENY before side effects。

## 6. TOCTOU / concurrency

Resolve-to-execute membership/frame changes invalidate the capability。Gateway revalidates immediately before operation under a context-local resolution registry/version/critical section equivalent。Capability consumption is atomic；concurrent replay yields at most one terminal consumer。No global mutable proof registry。

## 7. Publication closure

System、RuleView、exact rules、classifier result、RuntimeBindingPlan、RuntimeAccessRequirement、Diagnostic、digest、PolicyIndex remain in one immutable `CompiledModelSet` closure。Runtime capability state is execution-time context-local state and never mutates compiled facts。

## 8. Compatibility

Existing final EngineContext/P1 APIs remain compatible and additive。Current AC-006 binding verification is framework runtime logic；future business predicate evaluator remains out of scope without a new Requirement。Legacy Config/RuleView compatibility remains read-only until P7。
