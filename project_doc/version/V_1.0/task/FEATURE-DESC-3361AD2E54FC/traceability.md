# P2 Traceability

Current candidate authority:

```text
REQAN-P2-R01@d08612768131 + Overlay R04
        -> BM-R20
        -> FLOW-R11
        -> DESIGN-P2-R28
        -> TESTDESIGN-P2-R29
parallel/non-authoritative: P2-IMPACT-R27
```

| Trace | Requirement / Flow | Current implementation projection | Current TestDesign |
|---|---|---|---|
| TR-P2-005 | compile exact binding/publication | `CMI-P2-COMPILE-005`; `CompiledModelSet.viewMaterializationIndex` aggregate + digest/publication closure | materialization aggregate/publication cases |
| TR-P2-006 | trusted runtime binding / fail closed | `RuntimeModelExecutionRoot.load` -> typed ModelDataFactory -> existing ModelLoader/Container -> trusted handle/scope | root load, scope producer, composition/session failure cases |
| TR-P2-007 | Guard before actual MODEL effect | FLOW-R11 STEP-03..06 unchanged; STARTER composition only after stable STEP-01/02 setup | production composition, target, capability/Guard cases |
| TR-P2-008 | atomic Context publication | missing/duplicate materialization descriptor blocks the complete candidate; old Context retained | atomic publication + materialization closure cases |
| TR-P2-009 | real production reachability | same ModelData is loaded into existing Container and frozen in trusted handle; successful existing originData write-back preserved | registration-binding + real fixture/write-back success cases |

Explicit current user directive: do not change or require a blocking test for restoration of a POJO/Map already copied before a later legacy commit failure. This does not reopen BM-R20/FLOW-R11.

Formal lifecycle remains blocked by current same-revision Reviews, risk scan and machine Evidence. Historical PASSED revisions are not rewritten.
