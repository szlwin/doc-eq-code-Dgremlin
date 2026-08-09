# FEATURE-DESC-3361AD2E54FC Traceability

> Current candidate projection only. Historical lifecycle PASSED revisions remain immutable.

```text
REQAN-P2-R01@d08612768131 + Overlay R04
 -> BM-R20
 -> FLOW-R11
 -> DESIGN-P2-R26
 -> TESTDESIGN-P2-R27
parallel: P2-IMPACT-R25
```

| Trace | Requirement / decision | BM | Flow | Impact | Design | TestDesign |
|---|---|---|---|---|---|---|
| `TR-P2-001` | System/RuleView compilation | `BM-R20` | `FLOW-CONFIG-COMPILE@FLOW-R11` | `P2-IMPACT-R25` | `DESIGN-P2-R26` | `TESTDESIGN-P2-R27` |
| `TR-P2-004` | READ/WRITE protected authority | `BM-R20` | `FLOW-PROTECTED-ACCESS-EXECUTE@FLOW-R11` | `P2-IMPACT-R25` | `DESIGN-P2-R26` | `TESTDESIGN-P2-R27` |
| `TR-P2-005` | TargetKey/ModelPath/compiled binding | `BM-R20` | `FLOW-CONFIG-COMPILE@FLOW-R11` | `P2-IMPACT-R25` | `DESIGN-P2-R26` | `TESTDESIGN-P2-R27` |
| `TR-P2-006` | runtime target/materialization fail closed | `BM-R20` | `FLOW-PROTECTED-ACCESS-EXECUTE@FLOW-R11` | `P2-IMPACT-R25` | `DESIGN-P2-R26` | `TESTDESIGN-P2-R27` |
| `TR-P2-007` | Guard / MODEL effect / no bypass | `BM-R20` | `FLOW-PROTECTED-ACCESS-EXECUTE@FLOW-R11` | `P2-IMPACT-R25` | `DESIGN-P2-R26` | `TESTDESIGN-P2-R27` |
| `TR-P2-008` | atomic Context publication | `BM-R20` | `FLOW-CONFIG-COMPILE@FLOW-R11` | `P2-IMPACT-R25` | `DESIGN-P2-R26` | `TESTDESIGN-P2-R27` |
| `TR-P2-009` | mutation/concurrency/trusted materialization/handoff | `BM-R20` | `FLOW-PROTECTED-ACCESS-EXECUTE@FLOW-R11` | `P2-IMPACT-R25` | `DESIGN-P2-R26` | `TESTDESIGN-P2-R27` |
| `TR-P2-REV-DAG` | current revision authority | `BM-R20` | `FLOW-R11` | `P2-IMPACT-R25` parallel | `DESIGN-P2-R26` | `TESTDESIGN-P2-R27` |

Current materialization/handoff blockers are `CASE-P2-TD-TRUSTED-MATERIALIZATION-INPUT-001`, `CASE-P2-TD-TRUSTED-MATERIALIZATION-EXACT-VIEW-001`, `CASE-P2-TD-PRODUCTION-FRAME-HANDOFF-001`, and `CASE-P2-TD-PRODUCTION-SESSION-HANDOFF-001`. Wrong-target substitution remains `CASE-P2-TD-RUNTIME-TARGET-SUBSTITUTION-001`. Exact current linkage lives here and in the dependency graph, not in BM downstream refs.
