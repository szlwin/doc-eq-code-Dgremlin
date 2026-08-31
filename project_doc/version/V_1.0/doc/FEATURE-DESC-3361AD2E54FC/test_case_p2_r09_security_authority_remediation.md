# P2 Security Authority / Provenance Remediation TestDesign R36

Revision: `TESTDESIGN-P2-R36`

Supersedes the TestDesign execution/harness authority in `TESTDESIGN-P2-R35`; stable Case IDs and the R35 RED/GREEN classifications are retained.

Inputs: `DESIGN-P2-R32`, `REQAN-P2-R01@d08612768131 + Requirement Overlay R04`, `BM-R20`, `P2-TD-REV-001`, `P2-TD-REV-002`.

Scope remains **READ / WRITE only**. `EXECUTE = N/A` and is not a defect. R36 is a TestDesign-only correction. Requirement, Business Model and `DESIGN-P2-R32` remain unchanged and authoritative.

## R36 delta — implementation-neutral RED -> GREEN harness

R35 correctly reduced the pre-production RED gate to six genuine current-defect reproductions. R36 fixes one remaining TestDesign P1: several raw/proofless RED cases could otherwise force Development to preserve the exact public/raw APIs used by the pre-fix test source even though R32 explicitly allows those APIs to be removed, hidden, or converted to internal primitives.

R32 allows either of these compliant remediation families:

1. **surface closure** — remove or reduce visibility of `beginSession()`, `effectProvider()`, raw operation-port extraction, proofless access factories, or equivalent public bypass pieces so an ordinary production caller cannot reach a usable raw effect seam; or
2. **authorized internal primitive** — retain an internal/package-private primitive, but require a non-forgeable Guard-minted opaque authorization that is exact-bound to operation/target/path/session/context and is rejected before effect when absent or mismatched.

Therefore TestDesign must verify the security invariant, not preservation of a particular pre-fix method signature.

## Classification authority inherited from R35

The pre-production gate remains exactly six `MANDATORY_RED` cases.

### P0 — raw/proofless authority bypass

- `CASE-P2-TD-R34-RAW-MODEL-PORT-PUBLIC-SEAM-001`
- `CASE-P2-TD-R34-READONLY-RAW-WRITE-BYPASS-001`
- `CASE-P2-TD-R34-PROOFLESS-READ-ACCESS-NOT-AUTHORITY-001`
- `CASE-P2-TD-R34-PROOFLESS-WRITE-ACCESS-NOT-AUTHORITY-001`

### P1 — exact EngineContext provenance

- `CASE-P2-TD-R34-SAME-PLAN-CROSS-CONTEXT-001`
- `CASE-P2-TD-R34-STRUCTURALLY-IDENTICAL-CONTEXT-IDENTITY-001`

The following remain `GREEN_ONLY`:

- `CASE-P2-TD-R34-AUTHORITY-OPERATION-BINDING-001`
- `CASE-P2-TD-R34-AUTHORITY-TARGET-PATH-BINDING-001`
- `CASE-P2-TD-R34-CONTEXT-BINDING-LIFETIME-001`

`CASE-P2-TD-R34-AUTHORITY-ONE-SHOT-001` remains `REGRESSION_REQUIRED`.

No Case ID is renumbered by R36.

## Implementation-neutral final oracles

### 1. RAW-MODEL-PORT-PUBLIC-SEAM

Pre-fix RED must prove an ordinary production-facing caller can discover/reach the complete usable raw chain, equivalent to:

`RuntimeModelExecutionRoot -> RuntimeModelAccessScope -> beginSession -> effectProvider -> bind -> operationPort -> read/write`.

Final GREEN is satisfied only when **ordinary external production callers cannot obtain a usable raw operation port**.

Either implementation family is acceptable:

- the chain is broken by removal/visibility reduction/internalization; **or**
- an internal primitive remains but cannot be consumed without valid Guard-minted opaque authorization.

The public-surface/architecture harness must not require the removed method to remain compile-time callable. Reflection, API-surface inspection, bytecode/architecture inspection, or another stable external-package seam may be used so API removal itself can become GREEN rather than a test compilation failure.

### 2. READONLY-RAW-WRITE-BYPASS

Pre-fix RED must prove that under a READ-only policy/context the current raw MODEL path can still perform WRITE without `ExactModelAccessGuard` authorization.

Final GREEN is the disjunction below; either branch is valid and neither requires preservation of the pre-fix raw method signature:

**A. raw path unreachable**

An ordinary caller cannot acquire/invoke a usable WRITE primitive through the raw chain.

**OR**

**B. raw/internal primitive retained but authorization enforced**

Without an exact Guard-minted WRITE authorization, invocation deterministically denies before effect and records:

- `writeCount == 0` / equivalent protected-effect count unchanged;
- model value unchanged;
- mutation version unchanged;
- origin writeback unchanged;
- no fallback or alternate raw path executed.

A READ authorization must never satisfy the WRITE branch.

### 3. PROOFLESS-READ-ACCESS-NOT-AUTHORITY

Pre-fix RED must prove a proofless request such as `ResolvedProtectedReadAccess.of(target,path)` can currently participate in executable raw READ without Guard authority.

Final GREEN is satisfied when either:

- the proofless public factory/request path is removed, hidden, or converted into non-executable transport so an ordinary caller cannot use it to execute READ; **or**
- the transport object remains, but MODEL requires a matching Guard-minted READ authorization and rejects proofless execution before any protected read effect.

The test must verify lack of executable authority, not the continued existence of `ResolvedProtectedReadAccess.of(target,path)`.

### 4. PROOFLESS-WRITE-ACCESS-NOT-AUTHORITY

Pre-fix RED must prove a proofless write request plus mutation stamp can currently participate in executable raw WRITE without Guard authority.

Final GREEN is satisfied when either:

- the proofless public factory/request path is removed, hidden, or converted into non-executable transport; **or**
- it remains as transport/internal data but cannot cause WRITE without an exact Guard-minted WRITE authorization.

For the deny branch the final oracle includes zero protected write effects, unchanged model value/version/origin writeback, and no alternate fallback effect.

### 5. SAME-PLAN-CROSS-CONTEXT

Unchanged from R35.

- `Context A + Scope A` is eligible for Guard evaluation and succeeds when policy allows.
- `Context A + Scope B` must return `PROVENANCE_MISMATCH` before Guard/effect.
- Structural `RuntimeBindingPlan.equals()` is never sufficient authority identity.

This remains a genuine RED on the current implementation.

### 6. STRUCTURALLY-IDENTICAL-CONTEXT-IDENTITY

Unchanged from R35.

Two distinct EngineContexts with structurally identical plan/policy/digest must remain identity-isolated:

- exact A + Scope A may proceed;
- A + Scope B must return `PROVENANCE_MISMATCH` before Guard/effect.

## RED -> GREEN test integrity rule

Each genuine RED Evidence record must freeze at least:

- stable `caseId`;
- exact pre-fix production Git revision;
- exact RED test-source digest;
- exact command;
- test discovery/compile success;
- semantic failing assertion and relevant output;
- for denial-oriented cases, the expected zero-side-effect tuple that will be asserted at GREEN.

### Default rule: same test source

GREEN should reuse the same test source digest whenever the Design-compliant production fix leaves that harness compilable.

Changing assertions merely to make a failing test pass is forbidden.

### Controlled harness adaptation for Design-authorized API closure

If a compliant R32 implementation removes/reduces visibility of a raw/proofless API such that the frozen RED source no longer compiles, the case does **not** fail completion merely because the old harness was statically bound to that removable API. A controlled harness adaptation is allowed, but only when all of the following are recorded:

1. original RED `caseId`, test-source digest, command, pre-fix production revision and semantic RED Evidence;
2. production revision at which the old harness lost compile-time reachability;
3. exact test-source delta and new GREEN test-source digest;
4. explicit mapping from the delta to an R32-authorized API closure/internalization decision;
5. proof that the semantic oracle is not weakened and remains implementation-neutral;
6. a GREEN command/log proving the final invariant;
7. independent `TestEvidenceReviewAgent` approval of the harness delta before the case may count as GREEN.

A harness adaptation is invalid if it:

- deletes the blocking assertion without an equivalent observable assertion;
- changes expected DENY into success;
- stops checking zero side effects;
- bypasses the ordinary-caller/public-surface boundary being tested;
- depends on a new mock that replaces the real authority/effect behavior;
- changes Case identity solely to avoid comparison with the frozen RED.

The adapted harness must still fail if a later regression re-exposes a usable raw public seam or restores proofless executable authority.

## Preferred stable harness strategy

Development should prefer writing the six RED tests so they survive both remediation families without source changes where practical.

For raw/proofless P0 cases, use a two-level harness:

1. an **external production-facing surface probe** that detects whether an ordinary caller can obtain a usable raw seam without compile-time dependence on a removable method; and
2. a **behavior probe** only when the primitive remains reachable in the chosen implementation, proving absent/wrong authorization denies before effect.

The final case passes only if the surface is unreachable **or**, where an internal/test-visible primitive remains, the behavior probe proves authorization enforcement and zero side effects.

This strategy is preferred but not mandatory when the controlled harness-adaptation rule above is satisfied.

## GREEN_ONLY future-mechanism contracts

R35 classification remains authoritative:

- operation binding;
- target/path binding;
- context-binding lifetime.

These tests may directly reference the new R32 remediation types after they exist. They are mandatory GREEN gates and never substitute for the six genuine RED Evidence records.

## One-shot and regression matrix

`AUTHORITY-ONE-SHOT` remains `REGRESSION_REQUIRED`.

Development GREEN and later Testing must also preserve:

- exact Context + Scope guarded READ success;
- exact Context + Scope guarded WRITE success;
- missing rule/default deny before protected effect;
- READ does not imply WRITE;
- mutation stamp/version stale-write protection;
- Container/model/origin rollback on failed write;
- RuleView/compiler behavior unchanged;
- dependency direction `compiler -> context`, `model -> context`, `starter -> context + model`;
- deterministic non-sensitive authorization/provenance failures.

## Development ordering frozen by R36

```text
pre-fix production revision
  -> create/run six genuine MANDATORY_RED cases
  -> freeze RED Evidence including test-source digest
  -> only then mutate production
  -> choose any R32-compliant surface-closure / authorized-internal-primitive topology
  -> if old RED harness still compiles: reuse same digest and turn RED -> GREEN
  -> if R32-compliant API closure breaks harness compilation:
       freeze harness delta + unchanged semantic oracle
       obtain independent TestEvidenceReview for that delta
       run adapted GREEN harness
  -> run GREEN_ONLY mechanism contracts
  -> run REGRESSION_REQUIRED + preserved regression matrix
  -> only then allow Development closure
```

Production mutation before all six genuine RED Evidence records are frozen remains forbidden.

## Focused command families

Planned command families remain those frozen in R35. Exact class names may be created in Development after TestDesign is finalized. Every RED command must compile and discover the intended test on the pre-fix production revision. Setup/compile/missing-class/missing-test failure is `INVALID_RED`.

A later compile failure caused specifically by an R32-authorized API closure is not retroactively valid RED and is not itself GREEN; it triggers the controlled harness-adaptation rule above.

## Completion rule for R36

`TESTDESIGN-P2-R36` is acceptable when:

1. the R35 six-case genuine RED classification is unchanged;
2. raw/proofless final oracles accept both R32-compliant remediation families without forcing preservation of a specific public/raw API;
3. RED Evidence binds test-source digest and pre-fix production revision;
4. any post-fix harness adaptation is traceable, semantics-preserving, independently TestEvidence-reviewed, and regression-sensitive;
5. the two provenance RED cases remain unchanged and exact-identity based;
6. RequirementReview confirms no Requirement/Design semantic weakening;
7. TestEvidenceReview confirms stable seams, genuine RED, evidence freshness, and RED->GREEN integrity;
8. TestDesign is finalized before Implementation Plan and no production/test-source mutation occurs in this TestDesign phase.
