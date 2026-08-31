# P2 Review remediation R26

Review baseline: `cc275e9211eda344489cd086233dcdc7966ac870`.

Current candidate:

```text
REQAN-P2-R01@d08612768131 + Overlay R04
 -> BM-R20
 -> FLOW-R11
 -> DESIGN-P2-R26
 -> TESTDESIGN-P2-R27
parallel: P2-IMPACT-R25
```

This remediation preserves the independent semantic PASS for Requirement/Overlay, BM-R20 and FLOW-R11. It does not change production Java, lifecycle PASSED records, `risk_detection.json`, TDD execution or Development.

Closed author-side residuals proposed for independent Review:

1. Trusted MODEL materialization now has a typed input: exact current `RuntimeBindingPlan` + deep-immutable source snapshot. Existing `ModelData` is not accepted.
2. MODEL derives identity only from exact captured-plan `targetViewKey`, resolves that view in the same captured Context, creates a new internal ModelData, then freezes provenance+handle atomically.
3. Legacy name/default-context ModelData creation is explicitly outside the P2 trusted path.
4. MODEL production API is concrete: `RuntimeModelRuntimes.production(context) -> RuntimeModelRuntime.open(request) -> RuntimeModelExecutionResult(frame+sealed session)`.
5. STARTER production factory internally obtains that exact MODEL runtime, retains the returned execution, and exposes no caller-injection seam for runtime/session/frame/Guard/operation port/ModelData.
6. `P2-IMPACT-R25` aligns to `CMI-P2-PROTECTED-ACCESS-005` and the concrete current API sequence.
7. `TESTDESIGN-P2-R27` has 76 blockers / 22 exact TestClasses, adds trusted-materialization and frame/session-handoff cases, and removes every literal truncated Expected.
8. Findings remain mapped to the existing 20 OPEN P1; no `FND-P2-REV-021` is created.

Formal closure remains pending same-revision specialist Review, current risk scan and machine Evidence. Plan/TDD/Development remain BLOCKED.
