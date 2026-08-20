# FEATURE-DESC-3361AD2E54FC Traceability

Current candidate:

```text
REQAN-P2-R01@d08612768131 + Overlay R04
 -> BM-R20
 -> FLOW-R11
 -> DESIGN-P2-R30
 -> TESTDESIGN-P2-R31
parallel/non-authoritative: P2-IMPACT-R29
```

| Trace | Requirement/business authority | Current design projection | Blocking TestDesign |
|---|---|---|---|
| TR-P2-001 | System/RuleView identity | preserved R30 neutral key contracts | SYSTEM/RULEVIEW cases |
| TR-P2-002 | TargetKey + exact ModelPath | preserved exact target/path contracts | TARGET cases |
| TR-P2-003 | READ/WRITE only; ModelAccessRuleKey sole authority | request explicitly non-authoritative; Guard unchanged | POLICY/INTENT cases |
| TR-P2-004 | 0/1/N runtime fail closed | sealed session resolver and stable denials | LOCATOR/COMPOSE cases |
| TR-P2-005 | immutable/atomic Context | CompiledViewMaterializationIndex remains aggregate-owned | PUB/API_CTX cases |
| TR-P2-006 | real runtime binding/reachability | MODEL production `RuntimeModelLoadRequest` -> typed ModelDataFactory -> 3-arg ModelLoader -> MODEL-owned Container -> same Handle/Scope | MATERIALIZE/FIXTURE cases |
| TR-P2-007 | Guard-before-effect | same scope session effect-provider; private operation port after ALLOW | ADAPTER/COMPOSE cases |
| TR-P2-008 | source/config determinism | no runtime config/default Context repair | COMPILER/DIAG cases |
| TR-P2-009 | proof-to-effect same target | same ModelData A -> Handle A -> object A -> Guard A -> effect A | MODEL-EFFECT-SAME-HANDLE / TARGET-SUBSTITUTION / MODELDATA-IDENTITY |

R29 opaque production invocation token is historical/deferred only; it is not current authority. BM-R20/FLOW-R11 are not modified. `risk_detection.json` remains NOT_SCANNED; current execution Evidence none; Implementation Plan/TDD/Development remain BLOCKED.
