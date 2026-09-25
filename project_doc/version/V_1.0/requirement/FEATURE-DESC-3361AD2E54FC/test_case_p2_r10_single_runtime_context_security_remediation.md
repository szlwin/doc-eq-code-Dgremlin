# P2 Security Authority / Single Runtime Context TestDesign R37

Revision: `TESTDESIGN-P2-R37`

Supersedes the active execution/harness authority in `TESTDESIGN-P2-R36`.

Inputs: `DESIGN-P2-R33`, `DEC-P2-SINGLE-RUNTIME-CONTEXT-001`, `REQAN-P2-R01@d08612768131 + Requirement Overlay R04`, `BM-R20`, `FLOW-R11`, and the R36 RED->GREEN harness-integrity rules.

Scope remains **READ / WRITE only**. `EXECUTE = N/A`.

## R37 delta

R36 assumed that one supported runtime lifecycle may encounter two distinct EngineContexts and therefore kept two exact-context provenance cases in the pre-production RED gate.

The user has now frozen the opposite runtime architecture:

> one production runtime lifecycle/generation binds exactly one EngineContext at bootstrap; configuration update requires restart/new runtime generation; hot reload/live Context replacement is unsupported.

R37 therefore:

1. keeps the four genuine current P0 raw/proofless authority defects as `MANDATORY_RED`;
2. retires the two cross-context P1 REDs as `RETIRED_SUPERSEDED`, rather than pretending they became GREEN;
3. retires the future `RuntimeContextBinding` lifetime mechanism case;
4. adds explicit single-runtime-context / no-hot-reload lifecycle verification;
5. preserves R36's implementation-neutral RED->GREEN evidence integrity for the remaining four genuine REDs.

## Active pre-production MANDATORY_RED gate — exactly four

### P0 raw/proofless authority bypass

- `CASE-P2-TD-R34-RAW-MODEL-PORT-PUBLIC-SEAM-001`
- `CASE-P2-TD-R34-READONLY-RAW-WRITE-BYPASS-001`
- `CASE-P2-TD-R34-PROOFLESS-READ-ACCESS-NOT-AUTHORITY-001`
- `CASE-P2-TD-R34-PROOFLESS-WRITE-ACCESS-NOT-AUTHORITY-001`

These four remain genuine current-defect reproductions. No production mutation for P2-CR-001 may begin until valid RED Evidence for all four is frozen.

## Retired/superseded provenance cases

The following historical Case IDs remain auditable but are no longer active RED/GREEN gates:

- `CASE-P2-TD-R34-SAME-PLAN-CROSS-CONTEXT-001` -> `RETIRED_SUPERSEDED`
- `CASE-P2-TD-R34-STRUCTURALLY-IDENTICAL-CONTEXT-IDENTITY-001` -> `RETIRED_SUPERSEDED`
- `CASE-P2-TD-R34-CONTEXT-BINDING-LIFETIME-001` -> `RETIRED_SUPERSEDED`

Reason: each depends on the R32 premise that multiple EngineContexts are supported inside one runtime lifecycle and therefore need exact per-Scope/Handle context identity. `DEC-P2-SINGLE-RUNTIME-CONTEXT-001` removes that premise.

Retirement is **not** evidence that the current implementation would pass those historical cases. They simply no longer represent a supported current-P2 runtime contract.

## Implementation-neutral final oracles for the four active REDs

### RAW-MODEL-PORT-PUBLIC-SEAM

Pre-fix RED proves an ordinary production-facing caller can reach a usable raw MODEL effect chain without Guard authority.

Final GREEN is satisfied when either:

1. the raw chain is no longer usable by ordinary external production callers because it was removed/hidden/internalized; or
2. a retained internal/test-visible primitive requires a valid Guard-minted opaque authorization and rejects absent/mismatched authorization before effect.

The harness must not require a removable pre-fix method signature to stay callable.

### READONLY-RAW-WRITE-BYPASS

Pre-fix RED proves READ-only policy can currently reach raw WRITE without Guard authorization.

Final GREEN requires either raw WRITE to be unreachable or unauthorized WRITE to deterministically deny before protected effect with:

- protected write/effect count unchanged;
- model value unchanged;
- mutation version unchanged;
- origin writeback unchanged;
- no alternate raw fallback.

READ authorization cannot satisfy WRITE.

### PROOFLESS-READ-ACCESS-NOT-AUTHORITY

Pre-fix RED proves proofless READ transport can currently participate in executable raw READ.

Final GREEN requires that the proofless path be removed/hidden/non-executable, or that executable READ require matching Guard-minted READ authorization and deny proofless execution before protected effect.

### PROOFLESS-WRITE-ACCESS-NOT-AUTHORITY

Pre-fix RED proves proofless WRITE transport can currently participate in executable raw WRITE.

Final GREEN requires removal/hiding/non-executable transport, or matching Guard-minted WRITE authorization before effect. Denial preserves zero write effects and unchanged value/version/origin.

## Single-runtime-context lifecycle verification

These cases verify the new architecture without inventing false pre-fix defects.

### `CASE-P2-TD-R37-SINGLE-RUNTIME-CONTEXT-BIND-ONCE-001` — GREEN_ONLY

Verify one production runtime composition/bootstrap captures one compiler-published `EngineContext` and all MODEL/STARTER protected-access components created for that runtime use that bootstrap Context for the lifecycle.

Observable contract:

- one runtime bootstrap input Context;
- no mutation/setter/rebind of the active runtime Context;
- Rule/Change/CustomAction protected consumers obtain the same runtime composition;
- normal guarded READ/WRITE remain functional.

This case may use API/architecture inspection plus a real composition behavior probe. It must not require a JVM-global singleton.

### `CASE-P2-TD-R37-NO-HOT-RELOAD-001` — GREEN_ONLY

Verify the supported active-runtime surface exposes no `setContext`, `replaceContext`, `reloadContext`, live `publishIntoRuntime`, or semantic equivalent that mutates the Context of an already-running runtime generation.

`CompilerBootstrap.compileAndPublish(...)` by itself does not fail this case because Compiler publication is not an active-runtime hot-reload API.

### `CASE-P2-TD-R37-RESTART-NEW-GENERATION-001` — REGRESSION_REQUIRED

Verify two independent runtime generations may use different EngineContexts only across lifecycle replacement:

```text
generation A uses Context A
-> A is shut down / artifacts closed
-> generation B starts with Context B
```

No test requires A/B to coexist in one supported runtime or requires cross-generation Scope/Handle interoperability.

## Existing GREEN_ONLY / regression contracts

Remain `GREEN_ONLY`:

- `CASE-P2-TD-R34-AUTHORITY-OPERATION-BINDING-001`
- `CASE-P2-TD-R34-AUTHORITY-TARGET-PATH-BINDING-001`

Remain `REGRESSION_REQUIRED`:

- `CASE-P2-TD-R34-AUTHORITY-ONE-SHOT-001`

The old `AUTHORITY-CONTEXT-BINDING-LIFETIME` case is superseded by the R37 lifecycle cases above.

## RED -> GREEN evidence integrity

For each of the four genuine REDs, freeze:

- stable `caseId`;
- exact pre-fix production Git revision;
- exact RED test-source digest;
- exact command;
- compile/discovery success;
- semantic failing assertion and relevant output;
- zero-side-effect tuple where applicable.

GREEN should reuse the same test-source digest whenever the R33-compliant fix leaves the harness compilable.

Changing an assertion merely to make the test pass is forbidden.

### Controlled harness adaptation

If an R33-authorized raw/proofless API removal or visibility reduction makes a frozen RED source stop compiling, adaptation is allowed only with:

1. original RED caseId/digest/command/pre-fix revision/semantic evidence;
2. production revision causing compile-time reachability loss;
3. exact test-source delta and new GREEN digest;
4. mapping to R33-authorized surface closure/internalization;
5. proof the semantic oracle is not weakened;
6. GREEN command/log;
7. independent `TestEvidenceReviewAgent` approval before GREEN counts.

Invalid adaptations include deleting the blocking assertion without equivalent observable coverage, turning DENY into success, dropping zero-side-effect checks, replacing real authority/effect behavior with a mock, or changing Case identity to evade comparison.

## Stable harness guidance

For the four raw/proofless cases, prefer:

1. external production-facing surface/API/architecture probe that does not compile-time depend on a removable method;
2. real behavior probe only where a primitive remains reachable.

A final case is GREEN only when the ordinary caller cannot obtain executable raw authority, or retained primitive execution without the exact Guard authorization is denied before effect.

The R37 lifecycle cases should target the supported runtime composition/bootstrap seam, not private per-Scope implementation fields.

## Preserved regression matrix

Development GREEN and later Testing must preserve:

- guarded READ success;
- guarded WRITE success;
- missing rule/default deny before effect;
- READ does not imply WRITE;
- WRITE does not imply READ;
- mutation stamp/version stale-write protection;
- Container/model/origin failure semantics already in current P2 scope;
- RuleView/compiler behavior unchanged;
- atomic Compiler publication and old Context preservation;
- deterministic non-sensitive failures;
- dependency direction `compiler -> context`, `model -> context`, `starter -> context + model`;
- no active-runtime hot reload/rebind.

## Development ordering frozen by R37

```text
pre-fix production revision
  -> create/run FOUR genuine MANDATORY_RED cases
  -> freeze RED Evidence + source digests
  -> only then mutate production for P2-CR-001
  -> choose R33-compliant surface closure / authorized internal primitive topology
  -> RED -> GREEN, using controlled harness adaptation only when required
  -> run authority GREEN_ONLY cases
  -> run single-runtime-context/no-hot-reload GREEN_ONLY cases
  -> run one-shot/restart/regression matrix
  -> independent Reviews
  -> Development closure
```

The two historical cross-context cases never count toward the four-RED pre-production gate.

## Completion rule

`TESTDESIGN-P2-R37` is acceptable when:

1. active MANDATORY_RED count is exactly four and all four are genuine current P0 authority defects;
2. the two cross-context P1 cases are explicitly `RETIRED_SUPERSEDED`, not relabeled GREEN;
3. no `RuntimeContextBinding` future-mechanism test remains active;
4. runtime bind-once/no-hot-reload/restart semantics are directly verifiable;
5. R36 test-source-digest and controlled harness-adaptation rules remain intact for the four active REDs;
6. RequirementReview confirms alignment to R33/R04/BM-R20/FLOW-R11;
7. TestEvidenceReview confirms stable seams, reproducible evidence and no fake RED;
8. no production/test-source/runtime-config mutation is performed during this Design/TestDesign correction;
9. downstream Implementation Plan remains blocked until common-develop lifecycle state is safely reconciled.
