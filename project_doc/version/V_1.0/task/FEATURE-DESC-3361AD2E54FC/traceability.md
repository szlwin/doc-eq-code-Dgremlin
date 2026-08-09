# FEATURE-DESC-3361AD2E54FC Traceability

> Current candidate chain：`REQAN-P2-R01@d08612768131` + Overlay `R04` -> `BM-R18` -> `FLOW-R08` -> `DESIGN-P2-R20` -> `TESTDESIGN-P2-R21`。
> Status：`PENDING / MACHINE_BLOCKED`。
> Rule：refs below are candidate trace only; no row is COVERED/PASSED until same-revision Review + execution Evidence exists.

| Trace | Requirement / AC | BM / Flow | Design refs | Current TestDesign case IDs | Status |
|---|---|---|---|---|---|
| TR-P2-001 | AC-001 System deterministic/first-class | BM-R18; FLOW-CONFIG-COMPILE | `#p2-context` | `CASE-P2-TD-SYSTEM-DETERMINISM-001`; `CASE-P2-TD-SYSTEM-DUPLICATE-001`; `CASE-P2-TD-SYSTEM-FORWARD-REF-001`; `CASE-P2-TD-SYSTEM-OWNERSHIP-SNAPSHOT-001`; `CASE-P2-TD-SYSTEM-VERSION-IDENTITY-001`; `CASE-P2-TD-BM-CANONICAL-PAIR-001` | PENDING |
| TR-P2-002 | AC-002 RuleView System scope / duplicates | BM-R18; FLOW-CONFIG-COMPILE | RuleView sections | `CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001`; `CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001`; `CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001`; `CASE-P2-TD-RULEVIEW-VIEW-RESOLUTION-001`; `CASE-P2-TD-RULEKEY-CONTRACT-001` | PENDING |
| TR-P2-003 | AC-003 composite lookup / compatibility | BM-R18; FLOW-CONFIG-COMPILE | compatibility sections | `CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001`; `CASE-P2-TD-KEY-SOURCE-COMPAT-001`; `CASE-P2-TD-BARE-NAME-COMPATIBILITY-BOUNDARY-001` | PENDING |
| TR-P2-004 | AC-004 READ/WRITE independent authorization | BM-R18; both flows | `#p2-operation-binding`, `#production-runtime-model-operation` | `CASE-P2-TD-ACCESS-READ-WRITE-MATRIX-001`; `CASE-P2-TD-NO-EXECUTE-CONTRACT-001`; `CASE-P2-TD-STATIC-DENY-001`; `CASE-P2-TD-REAL-READ-OPERATION-001`; `CASE-P2-TD-REAL-WRITE-OPERATION-001`; `CASE-P2-TD-PRODUCTION-MODEL-ADAPTER-REACHABILITY-001` | PENDING |
| TR-P2-005 | AC-005 canonical source/path semantics | BM-R18; FLOW-CONFIG-COMPILE | `#p2-target-key`, `#p2-model-path` | `CASE-P2-TD-TARGETKEY-SOURCE-MAPPING-001`; `CASE-P2-TD-TARGET-PATH-ORTHOGONALITY-001`; `CASE-P2-TD-MODEL-PATH-UNKNOWN-001`; `CASE-P2-TD-WILDCARD-FINITE-EXPANSION-001`; `CASE-P2-TD-MODEL-PATH-CROSS-CONSUMER-EQUIVALENCE-001`; `CASE-P2-TD-P1-PATH-OPERATION-MIGRATION-001` | PENDING |
| TR-P2-006 | AC-006 dynamic access narrows static authority | BM-R18; both flows | `#p2-policy-classification`, `#p2-write-intent`, `#p2-runtime-denial` | `CASE-P2-TD-POLICY-CLASSIFICATION-TRUTH-TABLE-001`; `CASE-P2-TD-RUNTIME-PLAN-EXACT-BINDING-001`; `CASE-P2-TD-DYNAMIC-CLASSIFIER-REAL-001`; `CASE-P2-TD-RUNTIME-BINDING-PROOF-001`; `CASE-P2-TD-RUNTIME-PLAN-MISMATCH-001`; `CASE-P2-TD-SOURCE-TO-READ-WRITE-OPERATION-001`; `CASE-P2-TD-WRITE-INTENT-NOT-FOUND-001`; `CASE-P2-TD-WRITE-INTENT-AMBIGUOUS-001`; `CASE-P2-TD-WRITE-INTENT-FREEZE-STABILITY-001` | PENDING |
| TR-P2-007 | AC-007 Option B production entries no bypass | BM-R18; FLOW-PROTECTED-ACCESS-EXECUTE | `#p2-neutral-protected-port`, `#p2-production-composition` | `CASE-P2-TD-PRODUCTION-SEAM-NO-LEGAL-BYPASS-001`; `CASE-P2-TD-AC007-PRODUCTION-COMPOSITION-001`; `CASE-P2-TD-AC007-RULE-CONSUMER-INTEGRATION-001`; `CASE-P2-TD-AC007-CHANGE-CONSUMER-INTEGRATION-001`; `CASE-P2-TD-AC007-CUSTOM-ACTION-CONSUMER-INTEGRATION-001`; `CASE-P2-TD-AC007-CONSUMER-PARITY-001`; `CASE-P2-TD-AC007-REPRESENTATIVE-CONSUMER-STRUCTURE-001`; `CASE-P2-TD-AC007-REAL-PRODUCTION-REACHABILITY-001`; `CASE-P2-TD-DOWNSTREAM-DEPENDENCY-DIRECTION-001` | PENDING |
| TR-P2-008 | AC-008 atomic publication / isolation | BM-R18; FLOW-CONFIG-COMPILE | `#p2-context` | `CASE-P2-TD-ATOMIC-PUBLICATION-001`; `CASE-P2-TD-CONTEXT-ISOLATION-001`; `CASE-P2-TD-POLICY-INDEX-PUBLICATION-001` | PENDING |
| TR-P2-009 | AC-009 deterministic diagnostics / values | BM-R18; both flows | `#p2-runtime-value-contract`, `#p2-runtime-denial` | `CASE-P2-TD-DIAGNOSTIC-DETERMINISM-001`; `CASE-P2-TD-RUNTIME-DENIAL-DIAGNOSTIC-DETERMINISM-001`; `CASE-P2-TD-RUNTIME-FACT-VALUE-DOMAIN-001`; `CASE-P2-TD-RUNTIME-FACT-VALUE-DEEP-IMMUTABILITY-001`; `CASE-P2-TD-OPAQUE-RUNTIME-ID-VALUE-CONTRACT-001` | PENDING |
| TR-P2-010 | AC-010 declaration/migration boundary | BM-R18; FLOW-CONFIG-COMPILE | compatibility/publication sections | `CASE-P2-TD-DECLARATION-BOUNDARY-001`; `CASE-P2-TD-P1-PATH-OPERATION-MIGRATION-001` | PENDING |
| TR-P2-REV-DAG | exact revision dependency integrity | BM-R18; FLOW-R08 metadata | `#p2-revision-dag` | `CASE-P2-TD-REVISION-DAG-001` | PENDING |

## Current additional implementation-readiness traces

- Source identity: authorization owner System is separate from `TargetKey(shared ViewKey)`; sourcePath independently maps to ModelPath; local targetView/selector remains a separate owner-System binding.
- WRITE intent: exact 0/1/N selection before Guard; one immutable intent only; post-freeze state cannot trigger re-selection.
- Production operation: starter production assembly delegates actual object/path read/write to dec-core-model, not a test callback.
- RuntimeFactValue: closed deep-immutable deterministic domain; runtime IDs are opaque exact value wrappers.

## Evidence

Current verification Evidence IDs：none. `risk_detection.json` remains NOT_SCANNED and machine task revisions remain historical; therefore all rows stay PENDING.
