# COMPILER P2 Test Seams

> Revision：`DESIGN-P2-R24`
> Inputs：`BM-R20 / FLOW-R10`；CrossModule projection `P2-IMPACT-R23`
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`

## Compile-side seams

- real P1-style fixture resolves `targetView + SystemViewSelector` exactly once to `TargetPropertyPath(kind,value)`;
- compiler adapts that fact to neutral `CompiledTargetBinding(targetViewKey, TARGET_MAIN|PROPERTY_PATH, exactResolvedValue)`;
- runtime-visible plan contains no raw selector lexical authority;
- atomic publication remains compiler-coordinated and context-carried.

## Production registration provenance seam

Use at least two distinct actual `ModelData` instances and at least two distinct compiler-produced binding pairs.

The production fixture must construct:

```text
RuntimeModelRegistrationInput(TargetKey A, CompiledTargetBinding A, ModelData A)
RuntimeModelRegistrationInput(TargetKey B, CompiledTargetBinding B, ModelData B)
```

Then acquire the normal `ProtectedAccessRuntimeFactory.production(exact EngineContext).create(frameSnapshot)` path.

Instrument/spy all prohibited inference paths and assert invocation count = 0 for:

- `ModelData.getName()` as binding authority;
- `ViewData`/property-tree binding discovery;
- list-position pairing;
- raw XML/YAML/definition lookup;
- selector parsing/trim/normalization;
- first-match scan;
- any global mutable association registry.

A registration pair not present in the exact captured EngineContext must fail composition before RuntimeTargetResolver/capability/Guard/model effect. A valid registration pair does not grant READ/WRITE authority; exact PolicyIndex/Guard checks remain required.

## Runtime target / model seams

- sealed RuntimeModelSession stores the exact validated `TargetKey + CompiledTargetBinding + ModelData` association;
- resolver exact-matches both `sourceTargetKey` and `compiledTargetBinding`;
- resolver result freezes `RuntimeModelSessionId + RuntimeObjectId + RuntimeBindingProof`;
- `RuntimeMutationStamp` binds the same session/object/path/version;
- identity-preserving fixtures register the same actual ModelData in one/two active sessions to verify lease/coordination rules;
- transaction failure restores observable ModelData/origin state while capability remains CONSUMED.

## Current API compile seam

`ProtectedAccessCurrentApiContractTest` compiles/reflects every P2-added type and factory using only `DESIGN-P2-R24` + current source. It must specifically prove:

```java
public interface RuntimeModelSession extends AutoCloseable
```

and reject any generated/current contract using `interface ... implements AutoCloseable`. The current API test also compiles the typed `RuntimeModelRegistrationInput` / `RuntimeExecutionFrameSnapshot` construction surface.

## RED validity

Exact target commands remain those declared by TESTDESIGN-P2-R25. Missing TestClass/symbol/setup or a compile failure before the intended assertion is `INVALID_RED`, never valid RED.

## Concurrency

Use latch/barrier only, never sleep. Same actual ModelData/path/version may commit at most once; the stale loser mutates zero times.

## P2/P7 boundary

Tests may use RuntimeModelSession only as a composition/frame locator and protected-operation atomicity seam; they must not turn it into a user session, cross-request transaction manager or P7 lifecycle abstraction.

## Gate

No TDD or current execution Evidence is claimed. Risk scan and same-revision TestDesign/TDD/TestEvidence Review remain required.
