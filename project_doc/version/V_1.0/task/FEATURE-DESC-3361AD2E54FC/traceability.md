# FEATURE-DESC-3361AD2E54FC Traceability

> Current candidate projection only. Historical lifecycle PASSED revisions remain immutable.

```text
REQAN-P2-R01@d08612768131 + Overlay R04
 -> BM-R20
 -> FLOW-R11
 -> DESIGN-P2-R25
 -> TESTDESIGN-P2-R26

parallel: P2-IMPACT-R24
```

| Trace | Requirement/decision | BM | Flow | Impact | Design | TestDesign |
|---|---|---|---|---|---|---|
| `TR-P2-001` | System/RuleView compilation | `BM-R20` | `FLOW-CONFIG-COMPILE@FLOW-R11` | `P2-IMPACT-R24` | `DESIGN-P2-R25` | `TESTDESIGN-P2-R26` |
| `TR-P2-004` | READ/WRITE protected authority | `BM-R20` | `FLOW-PROTECTED-ACCESS-EXECUTE@FLOW-R11` | `P2-IMPACT-R24` | `DESIGN-P2-R25` | `TESTDESIGN-P2-R26` |
| `TR-P2-005` | TargetKey/ModelPath/compiled binding | `BM-R20` | `FLOW-CONFIG-COMPILE@FLOW-R11` | `P2-IMPACT-R24` | `DESIGN-P2-R25` | `TESTDESIGN-P2-R26` |
| `TR-P2-006` | runtime target/provenance fail closed | `BM-R20` | `FLOW-PROTECTED-ACCESS-EXECUTE@FLOW-R11` | `P2-IMPACT-R24` | `DESIGN-P2-R25` | `TESTDESIGN-P2-R26` |
| `TR-P2-007` | Guard / MODEL effect / no bypass | `BM-R20` | `FLOW-PROTECTED-ACCESS-EXECUTE@FLOW-R11` | `P2-IMPACT-R24` | `DESIGN-P2-R25` | `TESTDESIGN-P2-R26` |
| `TR-P2-008` | atomic publication | `BM-R20` | `FLOW-CONFIG-COMPILE@FLOW-R11` | `P2-IMPACT-R24` | `DESIGN-P2-R25` | `TESTDESIGN-P2-R26` |
| `TR-P2-009` | mutation stamp/concurrency/trusted handle | `BM-R20` | `FLOW-PROTECTED-ACCESS-EXECUTE@FLOW-R11` | `P2-IMPACT-R24` | `DESIGN-P2-R25` | `TESTDESIGN-P2-R26` |
| `TR-P2-REV-DAG` | current revision authority | `BM-R20` | `FLOW-R11` | `P2-IMPACT-R24` parallel | `DESIGN-P2-R25` | `TESTDESIGN-P2-R26` |

Trusted provenance-specific verification is `CASE-P2-TD-RUNTIME-TARGET-SUBSTITUTION-001`; API self-containment is split across CONTEXT/MODEL/STARTER cases. Exact current linkage lives here and in dependency graph, not in BM downstream exact refs.
