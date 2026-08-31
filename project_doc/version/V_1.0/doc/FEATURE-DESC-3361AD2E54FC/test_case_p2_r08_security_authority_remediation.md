# P2 Security Authority / Provenance Remediation TestDesign R35

Revision: `TESTDESIGN-P2-R35`

Supersedes TestDesign classification authority in `TESTDESIGN-P2-R34`; stable Case IDs are retained so traceability identity does not change.

Inputs: `DESIGN-P2-R32`, `REQAN-P2-R01@d08612768131 + Requirement Overlay R04`, `BM-R20`, independent finding `P2-TD-REV-001`.

Scope remains **READ / WRITE only**. `EXECUTE = N/A` and is not a defect. R35 is a TestDesign-only correction: Requirement, Business Model and `DESIGN-P2-R32` remain unchanged and authoritative.

## R35 delta — genuine RED vs future-mechanism contract

R34 correctly required every `MANDATORY_RED` to compile and be discovered on the pre-fix production revision, and correctly rejected compile/setup/missing-class failures as `INVALID_RED`. The independent review found that three R34 cases described contracts of remediation mechanisms newly introduced by R32 rather than defects that are guaranteed to exist as semantic failures on the pre-fix code object:

- `CASE-P2-TD-R34-AUTHORITY-OPERATION-BINDING-001`
- `CASE-P2-TD-R34-AUTHORITY-TARGET-PATH-BINDING-001`
- `CASE-P2-TD-R34-CONTEXT-BINDING-LIFETIME-001`

Those three cases are therefore reclassified to `GREEN_ONLY`. They remain mandatory for completion, but they **must not** be used as pre-production RED evidence and may legitimately depend on the new opaque authorization / exact runtime-context binding mechanism after that mechanism exists.

The genuine pre-fix RED gate is limited to six cases that directly reproduce the two current defects: four raw/proofless MODEL authority bypass cases and two exact EngineContext provenance cases.

## RED-before-production rule

For every case classified `MANDATORY_RED`, Development must, before the first production-source mutation:

1. add the stated test without changing production code;
2. run its exact focused command against the pre-fix production revision;
3. prove the test source compiles and the named test is discovered;
4. freeze Evidence showing failure at the intended authorization/provenance assertion;
5. reject setup, compilation, missing-class, missing-test, dependency or unrelated failures as `INVALID_RED`;
6. for denial semantics, record zero protected side effects and unchanged model state/version/origin writeback.

Production mutation is forbidden until **all six** `MANDATORY_RED` cases have valid frozen RED Evidence.

`GREEN_ONLY` and `REGRESSION_REQUIRED` cases do not contribute to the RED gate. They become mandatory GREEN gates after the remediation mechanism needed by the case exists.

## Exact remediation registry

| Stable Case ID | Severity | Planned module / TestClass | Pre-fix oracle | Required final oracle | Classification |
|---|---|---|---|---|---|
| `CASE-P2-TD-R34-RAW-MODEL-PORT-PUBLIC-SEAM-001` | P0 | `dec-core-starter` / new `ProtectedAccessModelAuthorityBoundaryTest` | external production-facing package can reach MODEL scope/effect binding and obtain `RuntimeModelOperationPort` | ordinary external caller cannot obtain a raw operation port; architecture/API scan reports no public bypass seam | `MANDATORY_RED` |
| `CASE-P2-TD-R34-READONLY-RAW-WRITE-BYPASS-001` | P0 | `dec-core-starter` / `ProtectedAccessModelAuthorityBoundaryTest` | policy/context authorizes READ only, yet low-level MODEL path performs WRITE without Guard authorization | WRITE denied before effect; `effectCount=0`; value and mutation version unchanged | `MANDATORY_RED` |
| `CASE-P2-TD-R34-PROOFLESS-READ-ACCESS-NOT-AUTHORITY-001` | P0 | `dec-core-model` / `RuntimeModelSessionEffectBehaviorTest` | proofless `ResolvedProtectedReadAccess.of(target,path)` is sufficient for raw read | proofless transport object alone cannot authorize an effect; Guard-minted authorization is required | `MANDATORY_RED` |
| `CASE-P2-TD-R34-PROOFLESS-WRITE-ACCESS-NOT-AUTHORITY-001` | P0 | `dec-core-model` / `RuntimeModelSessionEffectBehaviorTest` | proofless write access plus mutation stamp can reach raw write | proofless transport object cannot authorize write; rejected before side effect | `MANDATORY_RED` |
| `CASE-P2-TD-R34-AUTHORITY-OPERATION-BINDING-001` | P0 | `dec-core-starter` / `ProtectedAccessProductionCompositionTest` | no RED obligation; pre-fix guarded paths may already preserve operation separation and the R32 opaque authorization type does not yet exist | Guard-minted authorization is operation-bound; READ authorization cannot be consumed as WRITE; deterministic reject; zero write effect | `GREEN_ONLY` |
| `CASE-P2-TD-R34-AUTHORITY-TARGET-PATH-BINDING-001` | P0 | `dec-core-starter` / `ProtectedAccessProductionCompositionTest` | no RED obligation; do not manufacture a failure by referencing future remediation types | Guard-minted authorization is exact target/path-bound; mismatch deterministically rejects before effect | `GREEN_ONLY` |
| `CASE-P2-TD-R34-SAME-PLAN-CROSS-CONTEXT-001` | P1 | `dec-core-starter` / `ProtectedAccessProductionCompositionTest` | Context A and B use equal `RuntimeBindingPlan` but different policy; `Context A + Scope B` is accepted by structural plan equality | `Context A + Scope B -> PROVENANCE_MISMATCH`, no Guard/effect | `MANDATORY_RED` |
| `CASE-P2-TD-R34-STRUCTURALLY-IDENTICAL-CONTEXT-IDENTITY-001` | P1 | `dec-core-starter` / `ProtectedAccessProductionCompositionTest` | two distinct EngineContexts with equal plan/policy/digest are indistinguishable by structural equality | cross-context Scope rejects even when all structural facts equal; same exact Context + Scope succeeds | `MANDATORY_RED` |
| `CASE-P2-TD-R34-CONTEXT-BINDING-LIFETIME-001` | P1 | `dec-core-model` + `dec-core-starter` | no RED obligation; existing scope/session lifecycle protections may already be GREEN and the new exact `RuntimeContextBinding` does not yet exist | exact binding is minted/owned by the designated MODEL/context topology, exact-identity checked, and becomes unusable after owning context/scope lifecycle ends | `GREEN_ONLY` |
| `CASE-P2-TD-R34-AUTHORITY-ONE-SHOT-001` | P0 | `dec-core-starter` / `ProtectedAccessConcurrencyTest` | no RED obligation | at most one valid protected effect; replay/concurrent second consumption deterministically rejects | `REGRESSION_REQUIRED` |

## Genuine RED groups

The pre-production RED gate consists of exactly these six stable Case IDs:

### P0 — raw/proofless authority bypass

- `CASE-P2-TD-R34-RAW-MODEL-PORT-PUBLIC-SEAM-001`
- `CASE-P2-TD-R34-READONLY-RAW-WRITE-BYPASS-001`
- `CASE-P2-TD-R34-PROOFLESS-READ-ACCESS-NOT-AUTHORITY-001`
- `CASE-P2-TD-R34-PROOFLESS-WRITE-ACCESS-NOT-AUTHORITY-001`

### P1 — exact EngineContext provenance

- `CASE-P2-TD-R34-SAME-PLAN-CROSS-CONTEXT-001`
- `CASE-P2-TD-R34-STRUCTURALLY-IDENTICAL-CONTEXT-IDENTITY-001`

These are anchored in current observable seams: the raw MODEL effect path exists without Guard-minted authority, and captured-context ownership currently accepts structural `RuntimeBindingPlan.equals()` rather than exact EngineContext identity.

## GREEN-only mechanism contracts

The following cases are mandatory after the remediation mechanism exists, but are explicitly forbidden from being counted as RED Evidence:

- `CASE-P2-TD-R34-AUTHORITY-OPERATION-BINDING-001`
- `CASE-P2-TD-R34-AUTHORITY-TARGET-PATH-BINDING-001`
- `CASE-P2-TD-R34-CONTEXT-BINDING-LIFETIME-001`

A GREEN-only test may directly exercise R32 remediation types/topology such as opaque authorization or exact runtime-context binding. If such a test cannot compile on the pre-fix revision because the future mechanism does not exist, that fact is expected and **is not RED Evidence**.

## Preserved positive and regression matrix

Development GREEN and later Testing must retain the R34 matrix:

- exact Context + Scope guarded READ succeeds;
- exact Context + Scope guarded WRITE succeeds;
- missing rule denies before protected effect;
- READ never implies WRITE;
- mutation stamp/version stale-write checks remain effective;
- rollback leaves Container/model/origin state unchanged;
- RuleView/compiler behavior remains unchanged;
- dependency direction remains `compiler -> context`, `model -> context`, `starter -> context + model`;
- authorization/provenance errors remain deterministic and non-sensitive;
- one-shot/replay/concurrency authority contract remains `REGRESSION_REQUIRED`.

## Public API architecture scan

The architecture/API test must inspect the MODEL public surface itself, not only STARTER signatures. It must fail on the pre-fix revision if an ordinary external production caller can assemble a chain equivalent to:

`RuntimeModelExecutionRoot -> RuntimeModelAccessScope -> beginSession -> effectProvider -> bind -> operationPort -> read/write`

It must also verify after remediation that no publicly constructible boolean/marker/transport object can impersonate Guard authorization.

## Same-plan cross-context fixture

The P1 fixture must use two distinct EngineContexts:

- A and B contain structurally equal `RuntimeBindingPlan P`;
- one fixture may use different policy facts to expose practical permission reuse;
- another fixture must use structurally identical plan/policy/digest to prove identity isolation is stronger than equality;
- Scope A is bound to A and Scope B to B;
- `A + Scope A` succeeds when policy allows;
- `A + Scope B` is genuine RED on the pre-fix implementation and must become `PROVENANCE_MISMATCH` before Guard/effect after remediation.

## Zero-side-effect assertions

Every negative authority/provenance final oracle must assert, as applicable:

`createdEffectCount == 0`, protected read/write count unchanged, model value unchanged, mutation version unchanged, origin writeback unchanged, and no alternate fallback path executed.

For genuine RED tests, the pre-fix failure must be the intended missing denial/isolation behavior, not a fabricated test harness failure.

## Development sequencing frozen by R35

```text
pre-fix production revision
  -> add/run six genuine MANDATORY_RED tests
  -> freeze valid RED Evidence for all six
  -> only then mutate production implementation
  -> implement raw authority closure + Guard-minted opaque authorization
  -> implement exact EngineContext/runtime-context provenance
  -> six RED tests become GREEN
  -> add/complete GREEN_ONLY future-mechanism contract tests
  -> run REGRESSION_REQUIRED + preserved P2 regression matrix
  -> only then allow Development closure
```

No production, test-source or runtime-config mutation is authorized by this TestDesign revision itself.

## Focused command families for Development

The exact class names may be added in Development after this TestDesign is PASSED. Planned focused commands remain:

```text
./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-starter -Dtest=ProtectedAccessModelAuthorityBoundaryTest -Dsurefire.failIfNoSpecifiedTests=true test
./mvnw -pl dec-core-starter -Dtest=ProtectedAccessProductionCompositionTest -Dsurefire.failIfNoSpecifiedTests=true test
./mvnw -pl dec-core-model -Dtest=RuntimeModelSessionEffectBehaviorTest -Dsurefire.failIfNoSpecifiedTests=true test
./mvnw -pl dec-core-starter -Dtest=ProtectedAccessConcurrencyTest -Dsurefire.failIfNoSpecifiedTests=true test
./mvnw -pl dec-core-starter -Dtest=ProtectedAccessDependencyDirectionTest -Dsurefire.failIfNoSpecifiedTests=true test
```

A pre-assert compile/setup/missing-class/missing-test failure is `INVALID_RED`, never a valid remediation RED.

## Completion rule for R35

`TESTDESIGN-P2-R35` is acceptable when:

1. the R34 Case IDs remain traceable while their R35 classifications are authoritative;
2. the pre-production gate contains exactly the six genuine RED cases above;
3. future remediation mechanism contracts are mandatory GREEN-only checks, not fabricated RED obligations;
4. RequirementReview confirms no requirement/design semantics were weakened;
5. TestEvidenceReview confirms every RED obligation is executable against the pre-fix code object and cannot be satisfied by compile/setup/missing-class failure;
6. TestDesign is finalized before Implementation Plan, and Development remains blocked until the six RED Evidences are frozen.
