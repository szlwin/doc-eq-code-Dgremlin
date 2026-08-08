# COMPILER P2 架构增量

> Revision：`DESIGN-P2-R06`。状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。

## 1. Dependency direction

```text
dec-core-context        <- neutral immutable P2 facts + Guard/API contracts
       ^
       |
dec-core-compiler       <- builders/passes, dynamic-binding classification, publication
       ^
       |
frontends / starter / execution consumers

legacy Config path      <- read-only compatibility boundary only
```

禁止 context -> compiler 反向依赖；禁止 compiler class 通过 split package 放入 `dec.core.context.*`；禁止 compiler -> concrete parser；禁止 global current Context。

## 2. Cross-module fact construction

Immutable compiled facts live in context。Compiler may call **public validated context-owned factories** to create them。Factory visibility is not an authorization boundary: only facts inside the compiler-published immutable `CompiledModelSet`/PolicyIndex are authoritative。Runtime requests cannot inject a replacement `RuntimeAccessRequirement`。

## 3. AC-006 dynamic binding

`RUNTIME_GUARD_REQUIRED` means the static authorization is valid but final object binding depends on runtime。Compiler derives `RuntimeAccessRequirement(EXACT_RUNTIME_BINDING)` from existing access/path IR；no new XML/YAML predicate DSL is introduced。

Runtime execution resolves an immutable `RuntimeAccessBinding`；Guard compares it with the exact selected rule and requirement before protected access。This makes Source -> Compiler -> published Context -> Guard behavior reachable。

## 4. Publication closure

System、RuleView、exact ModelPath rules、RuntimeAccessRequirement、remaining Deferred、Diagnostic、digest and policy index are one immutable `CompiledModelSet` closure。No second AccessRegistry lifecycle。

## 5. Timeout and concurrency

R04 bounded executor remains for any evaluator extension。Exact runtime-binding validation is synchronous/pure。Timeout/cancellation tasks never obtain authority to execute protected business operation。

## 6. Compatibility

Existing final EngineContext and P1 APIs remain compatible。Legacy bare-name Config/RuleView reads survive only as read-only migration boundary until P7 and never write P2 registries。
