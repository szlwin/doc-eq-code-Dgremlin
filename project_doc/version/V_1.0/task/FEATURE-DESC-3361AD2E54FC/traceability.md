# FEATURE-DESC-3361AD2E54FC Traceability

> Current candidate chain：`REQAN-P2-R01@d08612768131` + Overlay `R04` -> `BM-R17` -> `FLOW-R07` -> `DESIGN-P2-R19` -> `TESTDESIGN-P2-R20`。  
> Status：`PENDING / MACHINE_BLOCKED`。  
> Rule：refs below are candidate trace only; no row is COVERED/PASSED until same-revision Review + execution Evidence exists.

| Trace | Requirement / AC | BM / Flow | Design refs | Current TestDesign case IDs | Status |
|---|---|---|---|---|---|
| TR-P2-001 | AC-001 System deterministic/first-class | BM-R17 System; FLOW-CONFIG-COMPILE | `#p2-system`, `#p2-context` | CASE-P2-TD-SYSTEM-DETERMINISM-001; SYSTEM-DUPLICATE; SYSTEM-FORWARD-REF; SYSTEM-OWNERSHIP-SNAPSHOT; SYSTEM-VERSION-IDENTITY; BM-CANONICAL-PAIR | PENDING |
| TR-P2-002 | AC-002 RuleView System scope / duplicates | BM-R17 RuleView/Rule; FLOW-CONFIG-COMPILE | `#p2-system` | RULEVIEW-SYSTEM-REQUIRED; RULEVIEW-SAME-SYSTEM-DUPLICATE; RULEVIEW-CROSS-SYSTEM-ISOLATION; RULEVIEW-VIEW-RESOLUTION; RULEKEY-CONTRACT | PENDING |
| TR-P2-003 | AC-003 composite lookup / compatibility | BM-R17 RuleView; FLOW-CONFIG-COMPILE | API RuleView/compatibility sections | RULEVIEW-COMPOSITE-LOOKUP; KEY-SOURCE-COMPAT; BARE-NAME-COMPATIBILITY-BOUNDARY | PENDING |
| TR-P2-004 | AC-004 READ/WRITE independent authorization | BM-R17 ModelAccess; both flows | `#p2-model-access`, `#p2-operation-binding` | ACCESS-READ-WRITE-MATRIX; NO-EXECUTE-CONTRACT; STATIC-DENY; REAL-READ-OPERATION; REAL-WRITE-OPERATION | PENDING |
| TR-P2-005 | AC-005 one canonical path/target semantics | BM-R17 TargetKey/ModelPath; FLOW-CONFIG-COMPILE | `#p2-target-key`, `#p2-model-path` | TARGETKEY-SOURCE-MAPPING; TARGET-PATH-ORTHOGONALITY; MODEL-PATH-UNKNOWN; WILDCARD-FINITE-EXPANSION; MODEL-PATH-CROSS-CONSUMER-EQUIVALENCE; P1-PATH-OPERATION-MIGRATION | PENDING |
| TR-P2-006 | AC-006 dynamic access narrows static authority | BM-R17 policy classification/runtime plan; both flows | `#p2-policy-classification`, `#p2-runtime-denial` | POLICY-CLASSIFICATION-TRUTH-TABLE; RUNTIME-PLAN-EXACT-BINDING; DYNAMIC-CLASSIFIER-REAL; RUNTIME-BINDING-PROOF; RUNTIME-PLAN-MISMATCH; SOURCE-TO-READ-WRITE-OPERATION-R20 | PENDING / CANDIDATE_COVERED_NOT_VERIFIED |
| TR-P2-007 | AC-007 Option B real Rule/change/custom-action entries no bypass | BM-R17 neutral seam + Option B; FLOW-PROTECTED-ACCESS-EXECUTE | `#p2-neutral-runtime-port`, `#p2-production-composition`, `#p2-operation-binding` | PRODUCTION-SEAM-NO-LEGAL-BYPASS; AC007-PRODUCTION-COMPOSITION; AC007-RULE-CONSUMER-INTEGRATION; AC007-CHANGE-CONSUMER-INTEGRATION; AC007-CUSTOM-ACTION-CONSUMER-INTEGRATION; AC007-CONSUMER-PARITY; AC007-REPRESENTATIVE-CONSUMER-STRUCTURE; AC007-REAL-PRODUCTION-REACHABILITY; DOWNSTREAM-DEPENDENCY-DIRECTION | PENDING |
| TR-P2-008 | AC-008 atomic publication / context isolation | BM-R17 publication; FLOW-CONFIG-COMPILE | `#p2-context` | ATOMIC-PUBLICATION; CONTEXT-ISOLATION; POLICY-INDEX-PUBLICATION | PENDING |
| TR-P2-009 | AC-009 deterministic compile/runtime diagnostics | BM-R17 diagnostics; both flows | `#p2-runtime-denial` | DIAGNOSTIC-DETERMINISM; RUNTIME-DENIAL-DIAGNOSTIC-DETERMINISM; REAL-READ-OPERATION deny branch; REAL-WRITE-OPERATION deny branch | PENDING |
| TR-P2-010 | AC-010 declaration/migration boundary | BM-R17 migration; FLOW-CONFIG-COMPILE | compatibility/publication sections | DECLARATION-BOUNDARY; P1-PATH-OPERATION-MIGRATION | PENDING |
| TR-P2-REV-DAG | common-develop exact revision dependency integrity | BM-R17 revision direction; FLOW-R07 metadata | `#p2-revision-dag` | CASE-P2-TD-REVISION-DAG-001 | PENDING |

## Current additional implementation-readiness traces

- Target identity：`sourceModel -> TargetKey(SystemKey,canonicalSourceModelName)` and independently `sourcePath -> ModelPath`.
- Policy truth table：only STATIC_ALLOW/NONE/no-plan and RUNTIME_GUARD_REQUIRED/EXACT_RUNTIME_BINDING/plan are legal.
- Operation closure：READ returns `ProtectedReadValue`; WRITE returns `ProtectedWriteReceipt`; every DENY occurs before operation port/effect.
- Dependency direction：future P3/P4/P6 core depend on neutral `dec-core-context ProtectedAccessPort`, never `dec-core-starter`.
- One-shot：same capability uses atomic consume; at most one protected operation/mutation.

## Evidence

Current verification Evidence IDs：none. `risk_detection.json` remains NOT_SCANNED and machine task revisions remain historical; therefore all rows stay PENDING.
