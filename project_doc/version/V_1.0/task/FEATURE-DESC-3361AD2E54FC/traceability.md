# FEATURE-DESC-3361AD2E54FC Traceability

> Authoritative chain：`REQAN-P2-R01@d08612768131` + Overlay `R04` -> `BM-R20` -> `FLOW-R10` -> `DESIGN-P2-R24` -> `TESTDESIGN-P2-R25`.
> CrossModule projection：`P2-IMPACT-R23`（parallel/non-authoritative）。
> Status：`PENDING / MACHINE_BLOCKED`. Exact downstream revisions are maintained here; BM/Flow canonical artifacts use stable trace/artifact projections and do not treat downstream exact revisions as authoritative inputs.

| Trace | Requirement / AC | Current candidate refs | Full stable blocking TestDesign IDs | Status |
|---|---|---|---|---|
| TR-P2-001 | AC-001 System deterministic/first-class | BM-R20; FLOW-R10; P2-IMPACT-R23; DESIGN-P2-R24; TESTDESIGN-P2-R25 | `CASE-P2-TD-SYSTEM-DETERMINISM-001`; `CASE-P2-TD-SYSTEM-DUPLICATE-001`; `CASE-P2-TD-SYSTEM-FORWARD-REF-001`; `CASE-P2-TD-SYSTEM-OWNERSHIP-SNAPSHOT-001`; `CASE-P2-TD-SYSTEM-VERSION-IDENTITY-001`; `CASE-P2-TD-BM-CANONICAL-PAIR-001` | PENDING |
| TR-P2-002 | AC-002 RuleView scope / duplicates | BM-R20; FLOW-R10; P2-IMPACT-R23; DESIGN-P2-R24; TESTDESIGN-P2-R25 | `CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001`; `CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001`; `CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001`; `CASE-P2-TD-RULEKEY-CONTRACT-001` | PENDING |
| TR-P2-003 | AC-003 composite lookup / compatibility | BM-R20; FLOW-R10; P2-IMPACT-R23; DESIGN-P2-R24; TESTDESIGN-P2-R25 | `CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001`; `CASE-P2-TD-KEY-SOURCE-COMPAT-001`; `CASE-P2-TD-BARE-NAME-COMPATIBILITY-BOUNDARY-001` | PENDING |
| TR-P2-004 | AC-004 READ/WRITE independent authorization | BM-R20; FLOW-R10; P2-IMPACT-R23; DESIGN-P2-R24; TESTDESIGN-P2-R25 | `CASE-P2-TD-ACCESS-READ-WRITE-MATRIX-001`; `CASE-P2-TD-WRITE-AUTHORITY-MODEL-ACCESS-RULEKEY-001`; `CASE-P2-TD-WRITE-SINGLE-PATH-AUTHORITY-001`; `CASE-P2-TD-REAL-READ-OPERATION-001`; `CASE-P2-TD-REAL-WRITE-OPERATION-001`; `CASE-P2-TD-RUNTIME-WRITE-ROLLBACK-001` | PENDING |
| TR-P2-005 | AC-005 canonical source/path semantics | BM-R20; FLOW-R10; P2-IMPACT-R23; DESIGN-P2-R24; TESTDESIGN-P2-R25 | `CASE-P2-TD-TARGETKEY-SOURCE-MAPPING-001`; `CASE-P2-TD-TARGET-PATH-ORTHOGONALITY-001`; `CASE-P2-TD-MODEL-PATH-UNKNOWN-001`; `CASE-P2-TD-WILDCARD-FINITE-EXPANSION-001`; `CASE-P2-TD-P1-PATH-OPERATION-MIGRATION-001` | PENDING |
| TR-P2-006 | AC-006 dynamic access narrows static authority | BM-R20; FLOW-R10; P2-IMPACT-R23; DESIGN-P2-R24; TESTDESIGN-P2-R25 | `CASE-P2-TD-POLICY-CLASSIFICATION-TRUTH-TABLE-001`; `CASE-P2-TD-RUNTIME-PLAN-EXACT-BINDING-001`; `CASE-P2-TD-RUNTIME-BINDING-PROOF-001`; `CASE-P2-TD-COMPOSITION-RUNTIME-CONTEXT-MATCH-001`; `CASE-P2-TD-PRODUCTION-RUNTIME-REGISTRATION-BINDING-001`; `CASE-P2-TD-RUNTIME-TARGET-SELECTION-001`; `CASE-P2-TD-RUNTIME-OBJECT-NOT-FOUND-STALE-001` | PENDING |
| TR-P2-007 | AC-007 Option B no bypass / production composition | BM-R20; FLOW-R10; P2-IMPACT-R23; DESIGN-P2-R24; TESTDESIGN-P2-R25 | `CASE-P2-TD-PRODUCTION-SEAM-NO-LEGAL-BYPASS-001`; `CASE-P2-TD-AC007-PRODUCTION-COMPOSITION-001`; `CASE-P2-TD-AC007-RULE-CONSUMER-INTEGRATION-001`; `CASE-P2-TD-AC007-CHANGE-CONSUMER-INTEGRATION-001`; `CASE-P2-TD-AC007-CUSTOM-ACTION-CONSUMER-INTEGRATION-001`; `CASE-P2-TD-AC007-REAL-PRODUCTION-REACHABILITY-001`; `CASE-P2-TD-PRODUCTION-RUNTIME-REGISTRATION-BINDING-001`; `CASE-P2-TD-MUTATION-STAMP-OBJECT-BINDING-001`; `CASE-P2-TD-CROSS-SESSION-MODELDATA-OWNERSHIP-001` | PENDING |
| TR-P2-008 | AC-008 atomic publication / isolation | BM-R20; FLOW-R10; P2-IMPACT-R23; DESIGN-P2-R24; TESTDESIGN-P2-R25 | `CASE-P2-TD-ATOMIC-PUBLICATION-001`; `CASE-P2-TD-CONTEXT-ISOLATION-001`; `CASE-P2-TD-POLICY-INDEX-PUBLICATION-001` | PENDING |
| TR-P2-009 | AC-009 deterministic diagnostics/runtime failure | BM-R20; FLOW-R10; P2-IMPACT-R23; DESIGN-P2-R24; TESTDESIGN-P2-R25 | `CASE-P2-TD-DIAGNOSTIC-DETERMINISM-001`; `CASE-P2-TD-RUNTIME-DENIAL-DIAGNOSTIC-DETERMINISM-001`; `CASE-P2-TD-PRODUCTION-RUNTIME-REGISTRATION-BINDING-001`; `CASE-P2-TD-RUNTIME-TARGET-SELECTION-001`; `CASE-P2-TD-MUTATION-STAMP-OBJECT-BINDING-001`; `CASE-P2-TD-RUNTIME-WRITE-ROLLBACK-001` | PENDING |
| TR-P2-010 | AC-010 declaration/migration boundary | BM-R20; FLOW-R10; P2-IMPACT-R23; DESIGN-P2-R24; TESTDESIGN-P2-R25 | `CASE-P2-TD-DECLARATION-BOUNDARY-001`; `CASE-P2-TD-P1-PATH-OPERATION-MIGRATION-001` | PENDING |
| TR-P2-REV-DAG | revision dependency integrity | authoritative chain + parallel P2-IMPACT-R23 projection | `CASE-P2-TD-REVISION-DAG-001` | PENDING |

## Current implementation-readiness facts

- BM-R20 and FLOW-R10 business semantics remain unchanged; their canonical downstream references are stable projections, not exact Design/TestDesign authority.
- Compiler resolves P1 selector semantics once into `CompiledTargetBinding`; runtime selector re-resolution remains forbidden.
- Production composition accepts explicit typed `RuntimeModelRegistrationInput(TargetKey, CompiledTargetBinding, ModelData)` values and validates each binding pair against the exact captured EngineContext before session registration/seal.
- Registration provenance never grants READ/WRITE permission; `ModelAccessRuleKey + ModelAccessPolicyIndex + Guard` remains the authority.
- `RuntimeModelSession` is a Java interface that extends `AutoCloseable`.
- Runtime target selection, mutation stamp, rollback, actual-ModelData coordination and P2/P7 boundary remain preserved.

## Evidence

Current verification Evidence IDs：none. `risk_detection.json` remains NOT_SCANNED. P0 Build Gate #1546 succeeded for the reviewed head `d0c86e41...`; it is a build signal only and is not reused as same-revision R24/R25 Review/TDD Evidence.
