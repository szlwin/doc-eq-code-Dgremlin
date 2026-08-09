# FEATURE-DESC-3361AD2E54FC Review 问题台账

> Review baseline: `8f8e3b9e5525d065f0ce4288062f872c56b67f3f`.
> Current candidate: `BM-R20 / FLOW-R11 / P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30`.
> Historical PASSED lifecycle is unchanged. No `FND-P2-REV-021` is created.

```json review-issues
[
  {
    "id": "FND-P2-REV-001",
    "severity": "P1",
    "status": "OPEN",
    "phase": "business_model",
    "title": "Guard coverage narrower than requirement",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30",
    "resolution_evidence": "Prior semantic remediation is preserved; formal closure still requires same-revision specialist Review, risk scan and machine Evidence.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-002",
    "severity": "P1",
    "status": "OPEN",
    "phase": "business_model",
    "title": "Business Model misses RuleView System-required error",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30",
    "resolution_evidence": "Prior semantic remediation is preserved; formal closure still requires same-revision specialist Review, risk scan and machine Evidence.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-003",
    "severity": "P1",
    "status": "OPEN",
    "phase": "business_model",
    "title": "P2 declaration boundary points at retired P1 module",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30",
    "resolution_evidence": "Prior semantic remediation is preserved; formal closure still requires same-revision specialist Review, risk scan and machine Evidence.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-004",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "P2 API contract not implementation-ready",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30",
    "resolution_evidence": "R29 adds a concrete same-scope MODEL effect provider/operation binding, opaque root-bound production invocation token, trusted Container selection and stable binding failures; same-revision independent API/Design Review remains required.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-005",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "Risk detection and specialist Review are not machine-closed",
    "decision": "BLOCKED_PENDING_REVIEW",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30",
    "resolution_evidence": "Current risk scan and same-revision specialist Review/Evidence are still outstanding.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-006",
    "severity": "P1",
    "status": "OPEN",
    "phase": "test_design",
    "title": "Formal future Maven command unreliable in reactor",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30",
    "resolution_evidence": "R30 retains exact bootstrap and target RED commands; target RED contains no -am and pre-assert setup failure is INVALID_RED.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-007",
    "severity": "P1",
    "status": "OPEN",
    "phase": "test_design",
    "title": "Fail-closed / requirement test matrix incomplete",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30",
    "resolution_evidence": "R30 restores scenario-specific observable oracles for all prior cases and adds effect-provider, same-invocation provenance, trusted Container and same-handle effect blockers.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-008",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "Frozen P2 Java API violates Java8/existing compatibility",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30",
    "resolution_evidence": "Prior compatibility remediation remains; new R29 signatures use Java-8-compatible interfaces/classes/enums/Optional without language-level upgrades.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-009",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "Selected dynamic requirement not delivered to evaluator",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30",
    "resolution_evidence": "Typed materialization remains aggregate-published and R29 routes trusted production invocation plus effect through the captured Context/session without runtime config reinterpretation.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-010",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "Real read path=* conflicts with exact runtime semantics",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30",
    "resolution_evidence": "Prior exact ModelPath remediation is preserved; R29 effect port consumes only resolved exact access for the same registered object.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-011",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "RuntimeFactValue not truly framework-closed",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30",
    "resolution_evidence": "Prior closed/deep-immutable RuntimeFactValue contract remains unchanged and R30 keeps dedicated domain/immutability cases.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-012",
    "severity": "P1",
    "status": "OPEN",
    "phase": "test_design",
    "title": "Test Design did not guarantee valid TDD RED",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30",
    "resolution_evidence": "R30 has 93 explicit blocking Cases mapped to 23 exact owner-module TestClasses with target -am forbidden and INVALID_RED before assertion.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-013",
    "severity": "P1",
    "status": "OPEN",
    "phase": "governance",
    "title": "Current revisions/decisions/lifecycle not consistently materialized",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30",
    "resolution_evidence": "Current projection is BM-R20 -> FLOW-R11 -> DESIGN-P2-R29 -> TESTDESIGN-P2-R30 with Impact R28 parallel; BM/FLOW and historical lifecycle remain unchanged.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-014",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "Legal dynamic access unreachable",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30",
    "resolution_evidence": "R29 closes the missing STEP-06 seam: STARTER binds a MODEL-owned effect provider to the same sealed session and privately invokes the resulting operation port only after Guard ALLOW.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-015",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "Policy construction/module boundary and authority consistency incomplete",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30",
    "resolution_evidence": "Aggregate materialization ownership remains in CONTEXT; R29 adds no new permission authority and limits MODEL effect consumption to STARTER composition.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-016",
    "severity": "P1",
    "status": "OPEN",
    "phase": "test_design",
    "title": "Test Design does not prove AC-006 source-to-runtime reachability",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30",
    "resolution_evidence": "R30 real fixture/reachability cases require compiler aggregate -> root-bound MODEL invocation -> real Container -> scope/session/effect binding -> Guard -> MODEL operation.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-017",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "RuntimeAccessBinding cannot prove runtime object binding",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30",
    "resolution_evidence": "R29 removes public plan+origin trusted request construction; a MODEL-minted one-shot root-bound invocation token captures plan+real origin atomically and freezes the same loaded ModelData in the handle.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-018",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "DynamicBindingClassification production rule not frozen",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30",
    "resolution_evidence": "Prior classification truth-table contract remains current and R30 retains explicit classification and exact binding cases.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-019",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "Runtime proof not atomically bound to actual operation target",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30",
    "resolution_evidence": "R29 binds production invocation -> same ModelData handle -> same scope/session -> same resolved runtimeObjectId -> same private MODEL operation port; operation port revalidates session/object before effect. User-excluded post-copy rollback remains out of scope.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-020",
    "severity": "P1",
    "status": "OPEN",
    "phase": "business_model",
    "title": "TargetKey source-model identity conflicts with frozen P1 model-access semantics and real systems.xml fixture",
    "decision": "SEMANTIC_FIX_VERIFIED_FORMAL_CLOSURE_PENDING",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30",
    "resolution_evidence": "P1 shared ViewKey source-identity repair remains independently verified; R29 does not reopen BM-R20 core semantics.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  }
]
```

## Gate

- Requirement + Overlay R04: semantic PASS / MACHINE_BLOCKED.
- BM-R20: semantic PASS / MACHINE_BLOCKED; artifact unchanged.
- FLOW-R11: semantic PASS / MACHINE_BLOCKED; structured Flow artifact unchanged.
- P2-IMPACT-R28 / DESIGN-P2-R29 / TESTDESIGN-P2-R30: current remediation candidates; same-revision specialist Review required.
- OPEN P1: exactly 20; `FND-P2-REV-021` absent.
- User-confirmed scope exclusion: no requirement/test blocker for restoration of a POJO/Map already copied before a later legacy commit failure.
- `risk_detection.json`: NOT_SCANNED; current execution Evidence: none.
- Implementation Plan / TDD / Development: BLOCKED.
