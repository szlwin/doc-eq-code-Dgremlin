# COMPILER P2 Test Seams

> Revision：`DESIGN-P2-R21`。Status：`NEEDS_REVIEW / MACHINE_BLOCKED`。

## Stable seams

- Direct Bridge authority: exact `ModelAccessRuleKey`, not required `RuleKey`.
- Typed invocation context: frame/owner/optional cursor wrappers end-to-end.
- WRITE path: exactly one path through `ResolvedWriteIntent.modelAccessRuleKey`; operation port has no second path.
- Write intent: deterministic 0/1/N; optional RuleKey provenance only.
- Runtime object lookup: one sealed composition/frame `RuntimeModelSession`; no global registry.
- Transactional write: isolated/rollback-safe failure, receipt only after commit.
- Concurrent same-version WRITE: at most one commit; stale loser zero mutation.
- RuntimeFactValue: closed deep-immutable deterministic domain.

## Production-vs-test rule

A fake model adapter can prove sequencing only. Production reachability must acquire normal starter composition and use the dec-core-model production RuntimeModelSession/operation implementation over real ModelData state.

## Failure injection

Tests must inject failures at mutation and commit boundaries and assert externally observable ModelData/origin state equals pre-write state, receipt absent and capability consumed.

## Concurrency

Use latch/barrier. Freeze two capabilities against the same object/path/version before release; assert exactly one committed mutation/receipt, exactly one `WRITE_INTENT_STALE`, version increments once and no partial update. No sleep-based oracle.
