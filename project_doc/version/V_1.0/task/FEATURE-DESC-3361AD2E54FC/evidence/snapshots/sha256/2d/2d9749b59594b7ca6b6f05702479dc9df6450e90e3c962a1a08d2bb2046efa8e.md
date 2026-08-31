# P2 Security Authority / Provenance Remediation TestDesign R34

Revision: `TESTDESIGN-P2-R34`

Inputs: `DESIGN-P2-R32`, `REQAN-P2-R01@d08612768131 + Requirement Overlay R04`, `BM-R20`.

Scope remains **READ / WRITE only**. `EXECUTE = N/A` and is not a defect. This TestDesign freezes executable RED/GREEN oracles for `P2-CR-001` and `P2-CR-002`; it does not modify production or test source in this phase. The real failing tests must be created and frozen before the first production mutation in Development.

## RED-before-production rule

For every remediation case marked `MANDATORY_RED`, Development must first add the stated test, run its exact focused command against the pre-fix production revision, and freeze command/log Evidence showing the failure is the intended semantic failure rather than compilation/setup/missing-test failure. Production mutation is forbidden until both P0 and P1 mandatory RED groups are frozen.

A valid RED must satisfy all of the following:

1. the test source compiles and the named test is discovered;
2. the failure occurs at the intended authorization/provenance assertion;
3. no unrelated setup or dependency failure is present;
4. Evidence binds the exact pre-fix Git revision;
5. for denial cases, the test records `effectCount=0` and unchanged model state/version when the expected GREEN behavior is denial-before-effect.

## Exact remediation registry

| Case | Severity | Planned module / TestClass | RED oracle on current code | GREEN oracle | Classification |
|---|---|---|---|---|---|
| `CASE-P2-TD-R34-RAW-MODEL-PORT-PUBLIC-SEAM-001` | P0 | `dec-core-starter` / new `ProtectedAccessModelAuthorityBoundaryTest` | external production-facing package can reach MODEL scope/effect binding and obtain `RuntimeModelOperationPort` | ordinary external caller cannot obtain a raw operation port; architecture/API scan reports no public bypass seam | `MANDATORY_RED` |
| `CASE-P2-TD-R34-READONLY-RAW-WRITE-BYPASS-001` | P0 | `dec-core-starter` / `ProtectedAccessModelAuthorityBoundaryTest` | policy/context authorizes READ only, yet low-level MODEL path performs WRITE without Guard authorization | WRITE denied before effect; `effectCount=0`; value and mutation version unchanged | `MANDATORY_RED` |
| `CASE-P2-TD-R34-PROOFLESS-READ-ACCESS-NOT-AUTHORITY-001` | P0 | `dec-core-model` / `RuntimeModelSessionEffectBehaviorTest` | proofless `ResolvedProtectedReadAccess.of(target,path)` is sufficient for raw read | proofless transport object alone cannot authorize an effect; Guard-minted authorization is required | `MANDATORY_RED` |
| `CASE-P2-TD-R34-PROOFLESS-WRITE-ACCESS-NOT-AUTHORITY-001` | P0 | `dec-core-model` / `RuntimeModelSessionEffectBehaviorTest` | proofless write access plus mutation stamp can reach raw write | proofless transport object cannot authorize write; rejected before side effect | `MANDATORY_RED` |
| `CASE-P2-TD-R34-AUTHORITY-OPERATION-BINDING-001` | P0 | `dec-core-starter` / `ProtectedAccessProductionCompositionTest` | authorization transport can be confused/reused if operation is not cryptographically/opaquely bound | READ authorization cannot be consumed as WRITE; deterministic reject; zero write effect | `MANDATORY_RED` |
| `CASE-P2-TD-R34-AUTHORITY-TARGET-PATH-BINDING-001` | P0 | `dec-core-starter` / `ProtectedAccessProductionCompositionTest` | authority mismatch path is attempted | target/path mismatch deterministically rejects before effect | `MANDATORY_RED` |
| `CASE-P2-TD-R34-SAME-PLAN-CROSS-CONTEXT-001` | P1 | `dec-core-starter` / `ProtectedAccessProductionCompositionTest` | Context A and B use equal `RuntimeBindingPlan` but different policy; `Context A + Scope B` is accepted by structural plan equality | `Context A + Scope B -> PROVENANCE_MISMATCH`, no Guard/effect | `MANDATORY_RED` |
| `CASE-P2-TD-R34-STRUCTURALLY-IDENTICAL-CONTEXT-IDENTITY-001` | P1 | `dec-core-starter` / `ProtectedAccessProductionCompositionTest` | two distinct EngineContexts with equal plan/policy/digest are indistinguishable by structural equality | cross-context Scope rejects even when all structural facts equal; same exact Context + Scope succeeds | `MANDATORY_RED` |
| `CASE-P2-TD-R34-CONTEXT-BINDING-LIFETIME-001` | P1 | `dec-core-model` + `dec-core-starter` | stale/foreign binding attempts are exercised | binding is MODEL/context minted, exact-identity checked and stale after owning context/scope lifecycle ends | `MANDATORY_RED` |
| `CASE-P2-TD-R34-AUTHORITY-ONE-SHOT-001` | P0 | `dec-core-starter` / `ProtectedAccessConcurrencyTest` | same authority is concurrently/repeatedly consumed | at most one valid protected effect; replay deterministically rejects | `REGRESSION_REQUIRED` |

## Preserved positive and regression matrix

The remediation must not regress the existing P2 contract. Development GREEN and later Testing must retain these cases:

| Case | Expected |
|---|---|
| `CASE-P2-TD-R34-GUARDED-READ-SUCCESS-001` | exact Context + Scope + READ policy + Guard-minted READ authority reads the same target/path |
| `CASE-P2-TD-R34-GUARDED-WRITE-SUCCESS-001` | exact Context + Scope + WRITE policy + Guard-minted WRITE authority updates the same ModelData and origin writeback path |
| `CASE-P2-TD-R34-MISSING-RULE-DENY-001` | missing authorization deterministically denies before protected effect |
| `CASE-P2-TD-R34-READ-DOES-NOT-IMPLY-WRITE-001` | READ-only policy never authorizes WRITE |
| `CASE-P2-TD-R34-MUTATION-STAMP-VERSION-001` | existing mutation stamp/version stale-write checks remain effective |
| `CASE-P2-TD-R34-CONTAINER-ROLLBACK-001` | failed protected write leaves Container/model/origin state unchanged according to existing transaction contract |
| `CASE-P2-TD-R34-RULEVIEW-COMPILER-UNAFFECTED-001` | Compiler/RuleView materialization and access-policy compilation remain behaviorally unchanged |
| `CASE-P2-TD-R34-DEPENDENCY-DIRECTION-001` | compiler -> context; model -> context; starter -> context+model; consumer does not gain MODEL bypass imports |
| `CASE-P2-TD-R34-ERROR-DETERMINISM-001` | authorization/provenance mismatch returns stable non-sensitive failure code; no identity/hash/JVM detail leakage |

## Public API architecture scan

The architecture/API test must inspect the MODEL public surface itself, not only STARTER signatures. It must fail if any ordinary external production caller can assemble a chain equivalent to:

`RuntimeModelExecutionRoot -> RuntimeModelAccessScope -> beginSession -> effectProvider -> bind -> operationPort -> read/write`

It must also check that no publicly constructible boolean/marker object can impersonate Guard authorization. An opaque authorization object is valid only when ordinary callers cannot mint it and MODEL validates its exact operation/target/path/session/context binding before effect.

## Same-plan cross-context fixture

The P1 fixture must construct two distinct EngineContexts:

- Context A and Context B contain structurally equal `RuntimeBindingPlan P`;
- case 1 uses different policy facts to show practical permission reuse risk;
- case 2 uses structurally identical plan/policy/digest to prove identity isolation is stronger than structural equality;
- Scope A is bound to A and Scope B to B;
- `A + Scope A` succeeds when policy allows;
- `A + Scope B` always returns `PROVENANCE_MISMATCH` before Guard/effect, even when plan/policy/digest are structurally identical.

## Zero-side-effect assertions

Every negative authority/provenance case must assert the observable tuple:

`createdEffectCount == 0`, `protectedReadCount == 0` or `protectedWriteCount == 0` as applicable, model value unchanged, mutation version unchanged, origin writeback unchanged, and no alternate fallback path executed.

## Planned focused commands for Development RED/GREEN

The exact class names may be added only in Development after this TestDesign is PASSED. Focused commands are:

```text
./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-starter -Dtest=ProtectedAccessModelAuthorityBoundaryTest -Dsurefire.failIfNoSpecifiedTests=true test
./mvnw -pl dec-core-starter -Dtest=ProtectedAccessProductionCompositionTest -Dsurefire.failIfNoSpecifiedTests=true test
./mvnw -pl dec-core-model -Dtest=RuntimeModelSessionEffectBehaviorTest -Dsurefire.failIfNoSpecifiedTests=true test
./mvnw -pl dec-core-starter -Dtest=ProtectedAccessConcurrencyTest -Dsurefire.failIfNoSpecifiedTests=true test
./mvnw -pl dec-core-starter -Dtest=ProtectedAccessDependencyDirectionTest -Dsurefire.failIfNoSpecifiedTests=true test
```

A pre-assert compile/setup/missing-class failure is `INVALID_RED`, never a valid remediation RED.

## Current-code reproducibility anchors

Before any remediation production mutation, current source already exposes the exact seams that the mandatory RED tests will lock down:

- `RuntimeModelAccessScope.effectProvider()` is public;
- `RuntimeModelEffectBindingResult.operationPort()` is public;
- existing MODEL tests call `operationPort.read(ResolvedProtectedReadAccess.of(...))` and `operationPort.write(ResolvedProtectedWriteAccess.of(...))` directly;
- `ProductionCompositionCoordinator.belongsToCapturedContext(...)` uses structural `RuntimeBindingPlan.equals()` to accept a Handle as belonging to captured Context.

These anchors establish that both findings are reproducible on the current code object. They are not a substitute for the mandatory failing test executions that must be frozen in Development before production changes.

## Completion rule for this TestDesign phase

`TESTDESIGN-P2-R34` is complete when this registry, traceability links, current-revision Evidence and independent `RequirementReviewAgent` + `TestEvidenceReviewAgent` reviews all pass. This phase intentionally stops before test-source or production-source mutation.
