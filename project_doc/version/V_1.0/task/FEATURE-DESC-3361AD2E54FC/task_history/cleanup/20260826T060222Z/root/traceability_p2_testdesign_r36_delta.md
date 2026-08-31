# P2 TestDesign R36 traceability delta

Target: `FEATURE-DESC-3361AD2E54FC`

Current semantic authority chain after this candidate correction:

`REQAN-P2-R01@d08612768131 + Requirement Overlay R04 -> BM-R20 -> FLOW-R11 -> DESIGN-P2-R32 -> TESTDESIGN-P2-R36`

Canonical machine ledger remains at historical `TESTDESIGN-P2-R34 / TEST_DESIGN-I007` until the safe I008 append/reducer transition is executed.

## Review delta

- `P2-TD-REV-001`: fixed by R35; future opaque-authority/context-binding mechanism contracts are `GREEN_ONLY`, not fabricated pre-fix RED.
- `P2-TD-REV-002`: fixed by R36; RED->GREEN harness is implementation-neutral and does not force preservation of raw APIs that R32 allows to remove or internalize.

## Stable Case identity

R36 retains all R34/R35 Case IDs. No trace identity is renumbered.

### MANDATORY_RED — unchanged six cases

- `CASE-P2-TD-R34-RAW-MODEL-PORT-PUBLIC-SEAM-001`
- `CASE-P2-TD-R34-READONLY-RAW-WRITE-BYPASS-001`
- `CASE-P2-TD-R34-PROOFLESS-READ-ACCESS-NOT-AUTHORITY-001`
- `CASE-P2-TD-R34-PROOFLESS-WRITE-ACCESS-NOT-AUTHORITY-001`
- `CASE-P2-TD-R34-SAME-PLAN-CROSS-CONTEXT-001`
- `CASE-P2-TD-R34-STRUCTURALLY-IDENTICAL-CONTEXT-IDENTITY-001`

### GREEN_ONLY — unchanged from R35

- `CASE-P2-TD-R34-AUTHORITY-OPERATION-BINDING-001`
- `CASE-P2-TD-R34-AUTHORITY-TARGET-PATH-BINDING-001`
- `CASE-P2-TD-R34-CONTEXT-BINDING-LIFETIME-001`

### REGRESSION_REQUIRED

- `CASE-P2-TD-R34-AUTHORITY-ONE-SHOT-001`

## R36 oracle delta for raw/proofless cases

Final GREEN must prove the invariant, not the survival of a particular raw API signature:

`ordinary caller cannot reach a usable raw/proofless effect seam`

**OR**, where an internal primitive remains:

`missing/wrong Guard-minted opaque authorization -> deterministic deny before effect + zero side effects`.

This delta maps directly to `DESIGN-P2-R32#R32-3 Raw MODEL effect closure`.

## RED evidence / GREEN integrity trace

For every genuine RED, Development must freeze:

- Case ID;
- exact pre-fix production revision;
- RED test-source digest;
- command;
- compile/discovery success;
- semantic failing assertion/output.

GREEN normally reuses the same source digest. If R32-authorized API removal/visibility reduction breaks that harness, the Case may retain identity only through a controlled harness delta containing old/new digests, exact test diff, reason mapped to R32, semantic non-weakening proof, GREEN command/log, and independent TestEvidenceReview approval.

This rule is part of the blocking TestDesign contract and must be reflected in the refreshed Implementation Plan before Development.

## Review results

- `REV-000118` RequirementReviewAgent — `TESTDESIGN-P2-R36` — **PASSED**.
- `REV-000119` TestEvidenceReviewAgent — `TESTDESIGN-P2-R36` — **PASSED**.

## Lifecycle

Required canonical sequence:

`historical R34/I007 PASSED -> reopen test_design -> I008 -> R35 candidate NEEDS_CHANGES(P2-TD-REV-002) -> R36 candidate -> REV-000118/REV-000119 PASSED -> finalize I008 as TESTDESIGN-P2-R36 -> implementation_plan`.

Until the append-only `task_events.jsonl` is safely updated by the common-develop lifecycle writer/reducer, downstream phases remain canonical-blocked.
