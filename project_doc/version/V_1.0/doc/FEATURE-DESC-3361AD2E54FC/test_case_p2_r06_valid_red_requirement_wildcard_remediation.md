# P2 Test Design R06 — Valid RED + Runtime Requirement/Wildcard Rebind

> Revision: `TESTDESIGN-P2-R06`.
> Base: `TESTDESIGN-P2-R05`.
> Inputs: `BM-R08`, `DESIGN-P2-R05`.
> Status: `BLOCKED_BY_DESIGN_REVIEW / NEEDS_REVIEW`.
> This revision carries forward all non-conflicting R05/R04 cases and supersedes their conflicting RED bootstrap and current Design binding. Historical Test Design/Evidence remains immutable.

## 1. Revision binding

R05 was bound to `DESIGN-P2-R04`. Independent Review found R04 incomplete for selected runtime requirement delivery, source `read path="*"` compilation semantics, and RuntimeFactValue implementation closure. R06 rebinds all P2 Test Design oracles to `DESIGN-P2-R05`.

Until DESIGN-P2-R05 reaches a valid exact-revision conclusion, R06 remains `BLOCKED_BY_DESIGN_REVIEW / NEEDS_REVIEW`.

## 2. Maven command contract and RED validity

### 2.1 Dependency/bootstrap build

When the formal target test step needs upstream artifacts installed, use:

```bash
./mvnw -pl <target-module> -am -Dmaven.test.skip=true install
```

`maven.test.skip=true` is intentional for this bootstrap step: it skips both test execution and test compilation, so unrelated/new tests in reactor modules cannot turn dependency preparation into a false TDD RED.

The formal target-test step remains target-module only:

```bash
./mvnw -pl <target-module> -Dtest=<TestClass> -Dsurefire.failIfNoSpecifiedTests=true test
```

Do **not** use `-am` in the target-test step. `failIfNoSpecifiedTests=true` stays explicit so a misspelled/missing target test is never false-green.

### 2.2 What counts as a valid RED

A valid TDD RED must satisfy all of the following:

1. Maven reaches the intended target test class.
2. test source compiles.
3. the test runner starts the intended test.
4. failure is an assertion/behavior/contract failure caused by the missing target behavior.
5. Evidence records the intended failing oracle and command.

The following are `INVALID_RED` and cannot satisfy the TDD gate:

- target module does not exist;
- target test class is not found;
- test source cannot compile because a production type/method does not exist;
- dependency/plugin/setup failure;
- Surefire fails in an upstream reactor module before the intended test;
- syntax/error unrelated to the intended behavior.

### 2.3 RED-safe API bootstrap

For API types that do not yet exist, the first RED MUST NOT statically import/reference the missing type.

Use a test that already compiles on the pre-change API and checks shape using strings/reflection or source/API-contract inspection, for example:

- `Class.forName("dec.core.context.RuntimeFactValue")` inside the test body with an assertion that turns absence/wrong modifiers into the intended RED;
- reflection on class/method/constructor names represented as strings;
- source/bytecode contract scan that can run without compiling against the new symbol.

After the legal workflow has produced the minimal API skeleton, direct typed unit tests may be added. From that point, a valid behavior RED must compile and fail a behavioral assertion; compile failure remains invalid.

No production skeleton may be created merely to make an otherwise invalid RED appear valid outside the formal TDD lifecycle.

## 3. Selected runtime requirement reaches evaluator

### CASE-P2-TD-RUNTIME-REQUIREMENT-SELECTION-001-R06

**Purpose:** prove evaluator uses the exact rule/requirement selected by Guard rather than hidden PolicyIndex state.

Required scenarios:

1. Build two `CompiledModelAccessRule` values with the same System/target/exact ModelPath/operation request surface but different `RuntimeAccessRequirement` identities/predicates in two isolated policy contexts.
2. Use identical `RuntimeFacts` for both requests.
3. One selected requirement evaluates ALLOW while the other evaluates DENY according to its predicate.
4. A spy evaluator observes the exact selected rule passed by Guard.
5. evaluator performs zero PolicyIndex lookup/reselection.
6. selected-rule key mismatch => DENY before evaluator.
7. `RUNTIME_GUARD_REQUIRED` with missing/invalid requirement is compile-time ERROR, never a runtime default-allow.
8. the current `systems.xml` grammar has no runtime-predicate declaration; compiler must not invent one from target names, ambient evaluator state or a second PolicyIndex lookup.
9. an exact current-fixture permission with no declared runtime requirement becomes STATIC_ALLOW when fully determined, or compile ERROR when runtime authorization would otherwise be required.
10. requirement identity/predicate change changes semantic digest input and `RuntimeRequirementKey`.

RED strategy:

- initial API-shape RED uses reflection/string contract checks and therefore compiles before the new types exist;
- direct typed behavior test becomes eligible only after the formal TDD skeleton revision creates the frozen R05 API.

## 4. Real `read path="*"` fixture expands to exact runtime facts

### CASE-P2-TD-REAL-SYSTEMS-WILDCARD-READ-001-R06

Fixture is the existing repository file:

`dec-demo/src/main/resources/mix/system/systems.xml`

It contains READ wildcard declarations for both `order/OrderInfo` and `payment/OrderInfo` and is a required P2 compatibility fixture.

Oracles:

1. fixture parses without treating READ `*` as a runtime wildcard key.
2. compiler resolves the exact `OrderInfo` target and obtains one immutable target path catalog.
3. READ `*` expands deterministically to a finite sorted set of exact `ModelAccessRuleKey` values.
4. runtime PolicyIndex contains **no** `ModelPath("*")` and performs no wildcard/prefix fallback.
5. explicit exact READ declarations overlapping the expansion are deduplicated by key while provenance retains all contributing SourceRefs.
6. order of source declarations does not change the exact expanded set or semantic digest.
7. target model-shape change followed by recompile re-expands against the new catalog and changes digest if effective authorization changes; an already-published old EngineContext does not widen.
8. empty readable catalog => `MIX-MODEL-PATH-WILDCARD-EMPTY` and no publication.
9. wildcard WRITE/EXECUTE => `MIX-MODEL-PATH-WILDCARD-OPERATION-UNSUPPORTED` and no publication.
10. cross-target/System paths never enter the expansion.

Behavior RED should exercise the existing compiler/public entry surface and real fixture so the test itself compiles before P2 wildcard behavior exists.

## 5. RuntimeFactValue implementation set is closed

### CASE-P2-TD-RUNTIME-FACT-VALUE-CLOSED-001-R06

API-shape oracles:

- `RuntimeFactValue` is public **final**.
- it exposes no public/protected/package constructor usable outside its implementation; construction is via the six public static factories only.
- reflection/source contract proves the class cannot be subclassed.
- no generic `Object value()`/payload accessor is exposed.
- `Kind` is exactly STRING/BOOLEAN/DECIMAL/INSTANT/LIST/OBJECT for this revision.

Behavior oracles after skeleton exists:

- every factory produces the expected Kind and visitor callback exactly once.
- LIST/OBJECT inputs are recursively copied/frozen.
- custom mutable POJO/array/collection values cannot enter RuntimeFacts.
- visitor exhaustiveness cannot be bypassed through an external RuntimeFactValue subclass.
- equals/hashCode/canonicalForm are deterministic for semantically equal trees.

Initial RED is reflection/source-shape based; a Java compile error from attempting to import a nonexistent `RuntimeFactValue` is not valid RED.

## 6. Carried-forward timeout/no-bypass/fail-closed matrix

All non-conflicting R05 cases remain mandatory under DESIGN-P2-R05, including:

- every protected READ/WRITE/EXECUTE reaches Guard exactly once;
- STATIC_ALLOW reaches Guard and submits evaluator work zero times;
- RUNTIME_GUARD_REQUIRED uses the R04 bounded executor/Future contract;
- true non-returning evaluator timeout returns DENY within budget and attempts cancel(true);
- late ALLOW after timeout has no effect;
- executor rejection/degraded/unavailable is fail-closed and never CallerRuns;
- null/throw/unknown/evaluator unavailable/Guard unavailable/context mismatch/policy missing fail closed;
- all DENY outcomes prove zero protected target operation and zero external side effect;
- existing EngineContext Java-8 compatibility remains intact;
- RuleView missing System prevents publication;
- retired declaration module is never restored.

## 7. Formal future command examples

Dependency preparation for context tests:

```bash
./mvnw -pl dec-core-context -am -Dmaven.test.skip=true install
```

Then one target test at a time, without `-am`:

```bash
./mvnw -pl dec-core-context -Dtest=P2ApiShapeRedTest -Dsurefire.failIfNoSpecifiedTests=true test
./mvnw -pl dec-core-context -Dtest=P2RuntimeRequirementSelectionTest -Dsurefire.failIfNoSpecifiedTests=true test
./mvnw -pl dec-core-context -Dtest=P2RuntimeFactValueClosedTest -Dsurefire.failIfNoSpecifiedTests=true test
./mvnw -pl dec-core-context -Dtest=P2ModelAccessGuardTimeoutEnforcementTest -Dsurefire.failIfNoSpecifiedTests=true test
```

Compiler fixture preparation:

```bash
./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install
```

Then:

```bash
./mvnw -pl dec-core-compiler -Dtest=P2SystemsWildcardReadExpansionTest -Dsurefire.failIfNoSpecifiedTests=true test
```

These are future formal commands; no command is claimed PASSED until TestEvidenceReviewAgent executes it against the exact TDD/production revision.

## 8. Exact-revision review gate

Before `TESTDESIGN-P2-R06` can be PASSED:

1. `BM-R08` must have valid current-iteration exact-revision Business Model review and machine lifecycle binding.
2. `DESIGN-P2-R05` must pass all required exact-revision Design/specialist reviews.
3. Then R06 requires independent exact-revision review by:
   - `RequirementReviewAgent`
   - `DesignReviewAgent`
   - `TDDReviewAgent`
   - `TestEvidenceReviewAgent`
4. TestEvidenceReviewAgent must verify the actual final module/test names and valid-RED classification; compile/setup failure is not accepted as RED evidence.
5. FND-P2-REV-006/007/008/009/010/011/012 remain OPEN until their exact-revision oracles are independently verified.

`IMPLEMENTATION_PLAN`, `TDD`, and `DEVELOPMENT` remain blocked while any effective P1 is OPEN.
