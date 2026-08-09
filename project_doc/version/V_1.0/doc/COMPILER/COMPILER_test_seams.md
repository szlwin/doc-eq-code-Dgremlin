# COMPILER P2 Test Seams

> Revision：`DESIGN-P2-R22`
> Inputs：`BM-R20 / FLOW-R10 / P2-IMPACT-R22`
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`

## Compile/publication seams

- construct a valid/invalid immutable Context candidate separately from publication;
- assert CONTEXT only constructs/holds representation;
- assert COMPILER coordinates the atomic swap;
- inject candidate-construction/publication failures and assert old Context remains;
- no global/default Context lookup seam is permitted.

## Runtime-target seams

- factory must accept explicit EngineContext and explicit `RuntimeExecutionFrameSnapshot`;
- invocation frame/owner mismatch is observable before resolver/capability/Guard;
- controlled sealed RuntimeModelSession fixtures provide 0/1/N candidate sets;
- resolver result freezes `RuntimeModelSessionId + RuntimeObjectId + RuntimeBindingProof`;
- tests can prove there is one production resolver path and no first/name/frame-only fallback.

## Runtime object ownership seams

- identity-preserving fixtures register the exact same ModelData instance twice in one session or in two active sessions;
- same-session duplicate -> `RUNTIME_OBJECT_ALREADY_REGISTERED`;
- cross-session active alias -> `RUNTIME_OBJECT_OWNERSHIP_CONFLICT`;
- close/reopen may transfer active lease but cannot reset per-path mutation version.

## WRITE stamp / rollback seams

- construct `RuntimeMutationStamp(sessionId, objectId, path, version)` only from one frozen target;
- mismatch session/object/path must fail construction or deny before Guard/effect;
- latch/barrier same-version race proves exactly one commit;
- mutation failure and commit failure both restore observable ModelData/origin state while capability stays CONSUMED.

## API self-containedness seam

`ProtectedAccessCurrentApiContractTest` compiles/reflects every P2-added type and factory using only `DESIGN-P2-R22` + current source. Superseded design text is not a fixture.

## Production reachability

A fake adapter/resolver may support unit tests but cannot satisfy production reachability. Production Evidence must acquire normal `ProtectedAccessRuntimeFactory.production(engineContext).create(frameSnapshot)` and observe real dec-core-model state.

## P2/P7 boundary

Tests must reject use of P2 RuntimeModelSession as a generic user/business session, cross-request transaction manager or P7 resource lifecycle abstraction.
