# FEATURE-DESC-3361AD2E54FC Review 问题台账

> Review baseline: `654012a55e2ba22662e0cd1ba573c152840f829e`.
> Current candidate: `BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28`.
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
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28",
    "resolution_evidence": "Prior semantic remediation is preserved; current candidate formal closure still requires same-revision specialist Review, risk scan and machine Evidence.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-002",
    "severity": "P1",
    "status": "OPEN",
    "phase": "business_model",
    "title": "Business Model misses RuleView System-required error",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28",
    "resolution_evidence": "Prior semantic remediation is preserved; current candidate formal closure still requires same-revision specialist Review, risk scan and machine Evidence.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-003",
    "severity": "P1",
    "status": "OPEN",
    "phase": "business_model",
    "title": "P2 declaration boundary points at retired P1 module",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28",
    "resolution_evidence": "Prior semantic remediation is preserved; current candidate formal closure still requires same-revision specialist Review, risk scan and machine Evidence.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-004",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "P2 API contract not implementation-ready",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28",
    "resolution_evidence": "R27 restores all R25-required cross-module factories and adds explicit constructible typed materialization descriptor plus MODEL trusted scope contracts.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-005",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "Risk detection and specialist Review are not machine-closed",
    "decision": "BLOCKED_PENDING_REVIEW",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28",
    "resolution_evidence": "Prior semantic remediation is preserved; current candidate formal closure still requires same-revision specialist Review, risk scan and machine Evidence.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-006",
    "severity": "P1",
    "status": "OPEN",
    "phase": "test_design",
    "title": "Formal future Maven command unreliable in reactor",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28",
    "resolution_evidence": "Prior semantic remediation is preserved; current candidate formal closure still requires same-revision specialist Review, risk scan and machine Evidence.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-007",
    "severity": "P1",
    "status": "OPEN",
    "phase": "test_design",
    "title": "Fail-closed / requirement test matrix incomplete",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28",
    "resolution_evidence": "R28 expands the complete fail-closed matrix to real write-back, scope provenance, typed descriptor/no-reparse, API constructibility and precondition failure cases; every blocking row remains current and complete.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-008",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "Frozen P2 Java API violates Java8/existing compatibility",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28",
    "resolution_evidence": "Prior semantic remediation is preserved; current candidate formal closure still requires same-revision specialist Review, risk scan and machine Evidence.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-009",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "Selected dynamic requirement not delivered to evaluator",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28",
    "resolution_evidence": "Compiler-produced RuntimeBindingPlan and CompiledViewMaterializationPlan are both published into the captured Context; MODEL consumes them without selector/NormalizedBody re-interpretation.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-010",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "Real read path=* conflicts with exact runtime semantics",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28",
    "resolution_evidence": "Prior semantic remediation is preserved; current candidate formal closure still requires same-revision specialist Review, risk scan and machine Evidence.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-011",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "RuntimeFactValue not truly framework-closed",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28",
    "resolution_evidence": "Prior semantic remediation is preserved; current candidate formal closure still requires same-revision specialist Review, risk scan and machine Evidence.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-012",
    "severity": "P1",
    "status": "OPEN",
    "phase": "test_design",
    "title": "Test Design did not guarantee valid TDD RED",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28",
    "resolution_evidence": "R28 uses 23 exact owner-module TestClasses with exact paths/bootstrap/target RED, no target -am and INVALID_RED for pre-assert failure.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-013",
    "severity": "P1",
    "status": "OPEN",
    "phase": "governance",
    "title": "Current revisions/decisions/lifecycle not consistently materialized",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28",
    "resolution_evidence": "Current projection is BM-R20 -> FLOW-R11 -> DESIGN-P2-R27 -> TESTDESIGN-P2-R28 with Impact R26 parallel; BM/FLOW artifacts and historical lifecycle remain unchanged.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-014",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "Legal dynamic access unreachable",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28",
    "resolution_evidence": "R27 removes detached sourceSnapshot runtime: the existing production origin object -> actual ModelData -> trusted frame -> Guard -> MODEL operation -> existing ModelContainer write-back path is frozen and reachable.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-015",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "Policy construction/module boundary and authority consistency incomplete",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28",
    "resolution_evidence": "Prior semantic remediation is preserved; current candidate formal closure still requires same-revision specialist Review, risk scan and machine Evidence.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-016",
    "severity": "P1",
    "status": "OPEN",
    "phase": "test_design",
    "title": "Test Design does not prove AC-006 source-to-runtime reachability",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28",
    "resolution_evidence": "R28 source-to-operation oracle now requires a real origin object to traverse compiler plan/typed View descriptor, existing MODEL lifecycle, trusted scope, STARTER Guard and real originData write-back.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-017",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "RuntimeAccessBinding cannot prove runtime object binding",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28",
    "resolution_evidence": "R27 binds exact RuntimeBindingPlan to the same actual ModelData created for the existing production loader using the compiler-published materialization descriptor; no public existing-ModelData wrap/rebind exists.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-018",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "DynamicBindingClassification production rule not frozen",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28",
    "resolution_evidence": "Prior semantic remediation is preserved; current candidate formal closure still requires same-revision specialist Review, risk scan and machine Evidence.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-019",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "Runtime proof not atomically bound to actual operation target",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28",
    "resolution_evidence": "Trusted handle, registered session, resolved target and mutation stamp all point to the same production ModelData; successful WRITE updates its real origin object and failure restores it.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-020",
    "severity": "P1",
    "status": "OPEN",
    "phase": "business_model",
    "title": "TargetKey source-model identity conflicts with frozen P1 model-access semantics and real systems.xml fixture",
    "decision": "SEMANTIC_FIX_VERIFIED_FORMAL_CLOSURE_PENDING",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28",
    "resolution_evidence": "P1 shared ViewKey source-identity semantic repair remains independently verified; current R26/R27 work does not reopen BM-R20 core semantics; formal closure still requires same-revision specialist Review, risk scan and machine Evidence.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  }
]
```

## Gate

- Requirement + Overlay R04: semantic PASS / MACHINE_BLOCKED.
- BM-R20 core semantics: PASS; same-revision BusinessModel mapping Review required because R26 residual was removed in R27.
- FLOW-R11: semantic PASS / MACHINE_BLOCKED; structured Flow artifact unchanged.
- P2-IMPACT-R26 / DESIGN-P2-R27 / TESTDESIGN-P2-R28: current remediation candidates; same-revision specialist Review required.
- OPEN P1: exactly 20; `FND-P2-REV-021` absent.
- `risk_detection.json`: NOT_SCANNED; current execution Evidence: none.
- Implementation Plan / TDD / Development: BLOCKED.
