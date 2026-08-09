# FEATURE-DESC-3361AD2E54FC Review 问题台账

> Review baseline: `4a8bfef3f96c37d9b130c01256c7e1cf7645d760`.
> Current candidate: `BM-R20 / FLOW-R11 / P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29`.
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
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29",
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
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29",
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
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29",
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
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29",
    "resolution_evidence": "R28 freezes aggregate placement/accessors and stable MODEL root/scope/session/composition API/failure surfaces so implementation no longer chooses hidden integration or null/exception behavior.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-005",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "Risk detection and specialist Review are not machine-closed",
    "decision": "BLOCKED_PENDING_REVIEW",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29",
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
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29",
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
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29",
    "resolution_evidence": "R29 contains 89 complete blocking current-revision oracles including aggregate publication, explicit MODEL root/scope producer and stable composition/session failures; the user-excluded legacy post-copy rollback is not treated as a blocker.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-008",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "Frozen P2 Java API violates Java8/existing compatibility",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29",
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
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29",
    "resolution_evidence": "R28 publishes the typed materialization index inside CompiledModelSet/EngineContext, so MODEL can consume exact captured facts without NormalizedBody/raw/default-context reconstruction.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-010",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "Real read path=* conflicts with exact runtime semantics",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29",
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
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29",
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
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29",
    "resolution_evidence": "R29 retains 23 exact owner-module TestClasses, exact paths/bootstrap/target RED, target -am forbidden and INVALID_RED for pre-assert failure.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-013",
    "severity": "P1",
    "status": "OPEN",
    "phase": "governance",
    "title": "Current revisions/decisions/lifecycle not consistently materialized",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29",
    "resolution_evidence": "Current projection is BM-R20 -> FLOW-R11 -> DESIGN-P2-R28 -> TESTDESIGN-P2-R29 with Impact R27 parallel; BM/FLOW and historical lifecycle remain unchanged.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-014",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "Legal dynamic access unreachable",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29",
    "resolution_evidence": "R28 freezes RuntimeModelExecutionRoot as the explicit MODEL production seam from captured Context + exact plan + real origin object to existing ModelLoader/Container and active scope.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-015",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "Policy construction/module boundary and authority consistency incomplete",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29",
    "resolution_evidence": "CompiledViewMaterializationIndex is owned by the same immutable CompiledModelSet aggregate and EngineContext only delegates to it; no side/global materialization registry exists.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-016",
    "severity": "P1",
    "status": "OPEN",
    "phase": "test_design",
    "title": "Test Design does not prove AC-006 source-to-runtime reachability",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29",
    "resolution_evidence": "R29 real fixture/reachability oracles require the explicit MODEL root load and scope handoff before STARTER session/Guard/MODEL effect.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-017",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "RuntimeAccessBinding cannot prove runtime object binding",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29",
    "resolution_evidence": "The root creates the actual ModelData through the captured aggregate descriptor and freezes that same reference in the trusted handle; STARTER cannot supply an existing ModelData or rebind the handle.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-018",
    "severity": "P1",
    "status": "OPEN",
    "phase": "design",
    "title": "DynamicBindingClassification production rule not frozen",
    "decision": "FIX_PROPOSED",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29",
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
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29",
    "resolution_evidence": "R28 binds plan, same created/loaded ModelData, handle, MODEL-minted scope, registered session and resolved target through one explicit root lifetime. Per user directive, legacy post-copy POJO/Map rollback after later commit failure is outside this remediation scope.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  },
  {
    "id": "FND-P2-REV-020",
    "severity": "P1",
    "status": "OPEN",
    "phase": "business_model",
    "title": "TargetKey source-model identity conflicts with frozen P1 model-access semantics and real systems.xml fixture",
    "decision": "SEMANTIC_FIX_VERIFIED_FORMAL_CLOSURE_PENDING",
    "resolution_revision": "BM-R20 / FLOW-R11 / P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29",
    "resolution_evidence": "P1 shared ViewKey source-identity semantic repair remains independently verified; R28 does not reopen BM-R20 core semantics.",
    "defer_reason": "same-revision specialist Review, risk scan and machine Evidence outstanding"
  }
]
```

## Gate

- Requirement + Overlay R04: semantic PASS / MACHINE_BLOCKED.
- BM-R20: semantic PASS / MACHINE_BLOCKED; artifact unchanged.
- FLOW-R11: semantic PASS / MACHINE_BLOCKED; structured Flow artifact unchanged.
- P2-IMPACT-R27 / DESIGN-P2-R28 / TESTDESIGN-P2-R29: current remediation candidates; same-revision specialist Review required.
- OPEN P1: exactly 20; `FND-P2-REV-021` absent.
- User-confirmed scope exclusion: no new requirement/test blocker for restoration of a POJO/Map already copied before a later legacy commit failure.
- `risk_detection.json`: NOT_SCANNED; current execution Evidence: none.
- Implementation Plan / TDD / Development: BLOCKED.
