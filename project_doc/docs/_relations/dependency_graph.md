# P2 Dependency Graph

- Project: `doc-eq-code`
- Current candidate: `BM-R20 / FLOW-R11 / P2-IMPACT-R24 / DESIGN-P2-R25 / TESTDESIGN-P2-R26`
- Status: `NEEDS_REVIEW / MACHINE_BLOCKED`
- Decisions: Direct Bridge ACTIVE; AC-007 Option B ACTIVE; AccessOperation READ/WRITE-only

```text
REQAN-P2-R01@d08612768131 + Overlay R04
        |
        v
BM-R20
        |
        v
FLOW-R11
        |------------------> P2-IMPACT-R24 (parallel projection)
        v
DESIGN-P2-R25
        |
        v
TESTDESIGN-P2-R26
```

No downstream artifact is an authoritative upstream input. BM uses stable trace/flow refs; exact current linkage is owned by this graph + task traceability projection.

## Trusted runtime association chain

```text
Compiler/P1 target resolution
 -> CONTEXT RuntimeBindingPlan(TargetKey + CompiledTargetBinding)
 -> MODEL trusted materialization creates ModelData + immutable RuntimeModelHandle provenance atomically
 -> MODEL RuntimeModelFrame freezes frame/owner/cursor + handles
 -> STARTER validates handle provenance against captured EngineContext
 -> MODEL RuntimeModelSession.register(handle) / seal
 -> STARTER RuntimeTargetResolver exact match
 -> one-shot capability + Guard
 -> MODEL actual READ / rollback-safe WRITE
```

Forbidden: public `binding + arbitrary ModelData` association, handle rebind, frame relabel, metadata/list-order/raw-selector inference, first-match fallback, Guard bypass.

Current CMI IDs: `CMI-P2-COMPILE-004`, `CMI-P2-PROTECTED-ACCESS-004`.
