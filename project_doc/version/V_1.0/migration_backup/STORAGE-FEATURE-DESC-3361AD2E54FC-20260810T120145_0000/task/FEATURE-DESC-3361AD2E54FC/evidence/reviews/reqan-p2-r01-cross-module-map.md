# P2 REQAN-R01 Cross-Module Analysis Map

Revision: `REQAN-P2-R01@d08612768131`
Flow reused: `FLOW-CONFIG-COMPILE`
Baseline dependency fact: `project_doc/docs/_relations/dependency_impact.yaml#crossModuleImplementation/CMI-P1-COMPILER-001`

This is review-support evidence for the frozen requirement-analysis revision. It describes required responsibilities and failure/recovery boundaries only; concrete class/API design remains deferred to the Design phase.

```text
XML/YAML Frontends
    -> Raw System / RuleView / model-access facts + SourceRef
    -> Compiler registration/resolution/path/access validation
    -> immutable Registry / EngineContext publication
    -> Starter/runtime RuleView resolution by (system,name)
    -> runtime ModelAccess guard before mutation when access is genuinely dynamic
```

| Participant | P2 responsibility | Input / output contract | Failure / recovery |
|---|---|---|---|
| XML/YAML frontend | preserve explicit System ownership and source facts | canonical raw facts with SourceRef | malformed/unknown source fact becomes Diagnostic; no partial runtime state |
| Compiler | register System identity; resolve `(system,name)`; compile paths and statically decidable permissions | Raw facts -> compiled identities/bindings/diagnostics | duplicate/unknown/invalid/denied static access blocks publish; corrected input can be recompiled |
| Registry / EngineContext | publish immutable owner-qualified compiled objects | one atomic compiled context | any compile failure retains previous valid context; contexts do not contaminate each other |
| Starter/runtime lookup | resolve RuleView with full composite identity | `(system-ref, rule-ref)` -> compiled RuleView | no bare-name fallback; missing composite key fails explicitly |
| Mutation services / rule/change/custom action paths | use one runtime access decision when static decision is insufficient | compiled access rule + runtime subject/path/op -> allow/deny | deny occurs before mutation or external side effect; retry requires corrected authority/input |
| declaration compatibility boundary | remain compatibility input only in P2 | legacy declaration facts remain readable where already supported | no P2 deletion and no second runtime authority; final retirement remains P7 |

Consistency boundary: publication is atomic. A partially compiled System/RuleView/access set is never observable. Runtime authorization failure is fail-closed and occurs before mutation.

Ordering/idempotency boundary: source file order must not change semantic identity or diagnostics. Recompiling the same semantic input must produce an equivalent compiled result/digest.

Partial-success/compensation boundary: compilation has no partial-success publication. Runtime access denial has no business mutation to compensate. External business compensation semantics beyond this authorization boundary remain later-phase/domain behavior, not P2.

Observability/recovery: diagnostics identify System, RuleView, operation, path and SourceRef without secret values. Recovery is correcting input/authority and recompiling or retrying the denied operation; no hidden fallback is allowed.

Verification mapping: AC-001/002/003/008/009 cover compile and publication boundaries; AC-004/005/006/007 cover permission/path/runtime guard; AC-010 protects the declaration migration boundary.
