# COMPILER P2 Business Model

> Revision：`BM-R20`。Base：`BM-R19`。
> Status：`NEEDS_EXACT_REVIEW / MACHINE_BLOCKED`。
> Canonical：`COMPILER_business_model.yaml@BM-R20`。

## Current full snapshot rule

BM-R20 is the complete current P2 business-model snapshot. `baseRevision: BM-R19` records lineage only and does not imply inheritance. It restores every still-valid BM-R18 compile/System/RuleView/READ/policy/publication/error fact and merges the BM-R19 runtime authority/transaction/concurrency refinements.

## Preserved business semantics

- shared `sourceModel -> ViewKey -> TargetKey`; owner System is a separate authority axis;
- `ModelPath` is exact and orthogonal to TargetKey;
- `AccessOperation` is exactly READ/WRITE;
- `ModelAccessRuleKey` is the sole policy/Direct-Bridge/WRITE authority key;
- PolicyIndex accepts only the frozen two-row classification;
- READ returns a deep immutable RuntimeFactValue and never mutates;
- WRITE intent 0/1/N remains fail closed and frozen before Guard;
- source-model-not-found and invalid policy classification are current business errors;
- P3/P4/P6 core depends on neutral context contracts, never starter internals.

## New current runtime facts

A legal dynamic invocation first proves composition frame/owner equality, then one `RuntimeTargetResolver` resolves `RuntimeBindingPlan + frame/owner/cursor + sealed RuntimeModelSession` to one immutable `ResolvedRuntimeTarget`.

WRITE freezes one `RuntimeMutationStamp(sessionId, objectId, path, version)` into the intent. The stamp must refer to the exact same target and exact `ModelAccessRuleKey.modelPath`.

One actual ModelData/runtime handle has one model-internal coordination cell and at most one active session registration lease. This prevents duplicate/cross-session aliasing from creating independent lock/version domains.

## Publication responsibility

```text
CONTEXT  -> construct immutable candidate representation
COMPILER -> coordinate atomic publication
```

Any failure retains the prior Context.

## Error state semantics

For `WRITE_INTENT_STALE` and `RUNTIME_WRITE_FAILED`:

```text
modelStateChanged      = false
capabilityStateChanged = true
```

The schema has one `stateChanged` field, so these errors set `stateChanged: true` because the capability state has already transitioned `ISSUED -> CONSUMED`; the `meaning` explicitly records that the observable model state did not change.

## Scope boundary

P2 RuntimeModelSession/transaction/version are internal protected-operation seams and do not define P7 user-session lifecycle, cross-request transaction semantics or general resource ownership.

## Gate

All current candidate findings remain formally OPEN until same-revision Review/risk/machine Evidence. Implementation Plan/TDD/Development remain BLOCKED.
