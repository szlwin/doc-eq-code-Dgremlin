# P2 Review remediation R23

> Base PR36 head: `f75a470f083fc44898065f5f49dc483222ab7aa7`
> Candidate chain: `BM-R20 -> FLOW-R10 -> P2-IMPACT-R22 -> DESIGN-P2-R23 -> TESTDESIGN-P2-R24`
> Lifecycle: candidate-only; no PASSED publication claim.

This is a deliberately narrow remediation. The independent Review semantically passed BM-R20, FLOW-R10 and P2-IMPACT-R22 and preserved the prior TargetKey/WRITE/runtime binding/concurrency fixes. Only the remaining RuntimeBindingPlan information-loss residual is changed.

1. `RuntimeBindingPlan` no longer exposes raw `String targetView` / `String selectorExpression`.
2. Compiler adapts existing P1 `ViewKey targetView + TargetPropertyPath(kind,value)` into neutral `CompiledTargetBinding(targetViewKey, TARGET_MAIN|PROPERTY_PATH, exactResolvedValue)`.
3. `SystemViewSelector` remains compiler-only lexical input; context does not depend on compiler classes.
4. Session registration and `ResolvedRuntimeTarget` carry the same neutral compiled binding; RuntimeTargetResolver exact-matches it only.
5. Runtime may not read raw XML/YAML/View definitions, scan property trees, parse/trim/normalize selector text, or reconstruct target semantics.
6. TESTDESIGN-P2-R24 keeps the existing 68 blockers / 19 exact TestClasses and strengthens `RUNTIME-PLAN-EXACT-BINDING` plus `RUNTIME-TARGET-SELECTION` rather than adding a redundant case.
7. No BM/Flow/DependencyImpact structured artifact is modified in this revision.

Residual mapping remains `FND-P2-REV-004 / 009 / 017 / 019`; no `FND-P2-REV-021` is created.

Not claimed: lifecycle PASSED, current risk scan, same-revision specialist Review closure, TDD execution, production Java or Development.
