# P2 Review remediation R29

- Review baseline: `8f8e3b9e5525d065f0ce4288062f872c56b67f3f`.
- Candidate: `BM-R20 / FLOW-R11 / P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30`.
- BM-R20 and FLOW-R11 artifacts are unchanged.

## Closed residual design seams

1. Actual MODEL effect is now obtained through a scope-owned effect provider bound to the same sealed session; STARTER retains the operation port privately and invokes it only after Guard ALLOW.
2. Public `RuntimeModelLoadRequest.of(plan, originObject)` is superseded by a MODEL-minted opaque, root-bound, one-shot production invocation token that captures plan+real origin+routing atomically from one production invocation.
3. Public production Container injection is removed; production root selects supported existing Containers internally from `ProductionContainerKind` through MODEL `ContainerFactory`.
4. R30 restores case-specific observable Expected/Forbidden oracles and strengthens A/A vs A/B substitution, effect provider reachability and no-bypass evidence.

## Explicit exclusion

Per user directive, restoration of a POJO/Map already copied before a later legacy commit failure is not changed and is not a current blocking oracle.

Formal state remains blocked: 20 OPEN P1, no FND-021, risk NOT_SCANNED, no current execution Evidence, Implementation Plan/TDD/Development BLOCKED.
