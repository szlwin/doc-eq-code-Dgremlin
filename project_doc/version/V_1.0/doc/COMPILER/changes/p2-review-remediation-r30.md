# P2 Review Remediation R30 — Direct MODEL Load Request

- Base: `DESIGN-P2-R29 / TESTDESIGN-P2-R30 / P2-IMPACT-R28`.
- New: `DESIGN-P2-R30 / TESTDESIGN-P2-R31 / P2-IMPACT-R29`.
- BM-R20 and FLOW-R11 unchanged.

## Decision

P2 does not adopt the R29 opaque invocation-token credential. `RuntimeModelProductionInvocation`, root binding/replay semantics and token-specific failures are `NOT_ADOPTED_IN_P2 / DEFERRED`.

Current MODEL loading is restored to `RuntimeModelLoadRequest(plan, originObject, ruleName, connectionName)` used inside the trusted MODEL production lifecycle. Request possession is not authorization. The production root still validates captured Context/materialization, creates/loads the real ModelData through existing ModelLoader and MODEL-owned Container, then mints Handle/Scope.

R29 effect-provider/session/same-handle design is preserved unchanged: `resolve A -> Guard A -> effect A` remains mandatory.

## TestDesign delta

R31 replaces token-specific provenance oracle with `PRODUCTION-LOAD-REQUEST`, adds `PRODUCTION-LOAD-PLAN-MISMATCH` and `PRODUCTION-MODELDATA-IDENTITY`, and rewrites all residual token wording in reachability/registration/substitution/materialization cases. Production Container and MODEL effect binding blockers remain.

## Explicit exclusion

No new POJO/Map restoration requirement is introduced for a later legacy commit failure after copy-back.
