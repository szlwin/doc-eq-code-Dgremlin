# FEATURE-DESC-3361AD2E54FC Test Design

> Revision：`TESTDESIGN-P2-R07`。
> Base：`TESTDESIGN-P2-R06`。
> Inputs：Requirement `REQAN-P2-R01`、Business Model candidate `BM-R09`、Design candidate `DESIGN-P2-R06`。
> Status：`NEEDS_CHANGES_CANDIDATE_FIXED / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED`，即本文件是返修后的 canonical Test Design candidate，但在 Design exact-revision Review、RC9 reopen/publish 与 machine-valid Evidence 完成前不得 PASSED。

## 1. Test Design principles

1. Acceptance must be tested from public/source-observable behavior, not only internal helper behavior.
2. Every protected READ/WRITE/EXECUTE must prove Guard entry; STATIC_ALLOW is Guard-internal only.
3. Runtime wildcard lookup is forbidden; real source `read path="*"` is compile-time expansion only.
4. AC-006 must prove **Source -> Compiler -> published Context -> Runtime Guard -> ALLOW/DENY**, not just direct construction of an internal compiled rule.
5. A valid TDD RED must compile the target test, start the intended test and fail the intended behavioral/contract assertion.
6. Missing module/test/production symbol, testCompile error, dependency/plugin/setup failure or upstream reactor failure is `INVALID_RED`.
7. No production code, API skeleton or implementation is created in Test Design.

## 2. Formal Maven / valid-RED contract

Dependency/bootstrap preparation, when required:

```bash
./mvnw -pl <target-module> -am -Dmaven.test.skip=true install
```

Formal target test:

```bash
./mvnw -pl <target-module> -Dtest=<TestClass> -Dsurefire.failIfNoSpecifiedTests=true test
```

The target-test step MUST NOT use `-am`.

Before a new API symbol exists, the initial API-shape RED uses reflection/string/source/bytecode contract inspection that itself compiles. Direct typed tests become eligible only after the legal TDD lifecycle has produced the minimal frozen API skeleton.

## 3. Requirement acceptance matrix

### AC-001 — deterministic System compilation

**CASE-P2-SYSTEM-DETERMINISM-001-R07**

- compile the real `systems.xml` and semantically identical reordered/multi-source forms;
- assert identical SystemKey set, canonical ordering and semantic digest;
- duplicate/conflicting System emits stable ERROR;
- failed candidate does not publish partial Context.

### AC-002 — RuleView composite identity

**CASE-P2-RULEVIEW-COMPOSITE-001-R07**

- same RuleView name in two Systems publishes as two `(SystemKey,name)` keys;
- duplicate within one System fails;
- missing System -> `MIX-RULEVIEW-SYSTEM-REQUIRED`;
- no bare-name registration fallback.

### AC-003 — RuleView composite call

**CASE-P2-RULEVIEW-CALL-001-R07**

- correct System+name resolves exact RuleView;
- wrong System/name and bare-name attempts fail deterministically;
- no cross-System search/fallback.

### AC-004 — model-access permission matrix

**CASE-P2-ACCESS-MATRIX-001-R07**

For READ/WRITE/EXECUTE independently:

- declared operation -> authorized according to compiled rule;
- undeclared operation -> fail closed;
- shared WRITE is denied unless explicitly declared;
- denial executes zero protected operation and zero external side effects.

### AC-005 — unified ModelPath / static blocking

**CASE-P2-MODEL-PATH-001-R07**

- same logical path used by rule/change/query/access yields same canonical identity;
- unknown segment/non-composite intermediate/target mismatch fails at compile time;
- no fuzzy/prefix/suffix/cross-target lookup.

### AC-006 — dynamic access from Source to runtime Guard

**CASE-P2-DYNAMIC-SOURCE-TO-GUARD-001-R07** — **blocking**

Purpose: prove the production Compiler can actually generate a legal `RUNTIME_GUARD_REQUIRED` fact from current source semantics.

Fixture contract:

- use existing source grammar only; do not invent a runtime-predicate XML/YAML DSL;
- declare an exact legal model-access surface;
- use a rule/change/custom-action/read fixture whose **final object instance or collection element is chosen at runtime** under that exact authorized target/path. A representative fixture may use an authorized container path such as `orderDetailList` with runtime element traversal; the exact fixture chosen by TDD must already be expressible by existing source syntax and remain within P2 scope.

Required chain/oracles:

1. source parses and compiles successfully;
2. static System/target/path/operation authorization is valid;
3. `DynamicBindingClassification = RUNTIME_OBJECT_BOUND` (or exact frozen equivalent);
4. compiler emits one exact `CompiledModelAccessRule` with `status=RUNTIME_GUARD_REQUIRED`;
5. that rule owns a deterministic compiler-derived `RuntimeAccessRequirement(EXACT_RUNTIME_BINDING)` traceable to the rule/SourceRef;
6. semantic digest includes the derived requirement identity;
7. candidate publishes into an immutable EngineContext;
8. runtime binding A resolves to the same Context/target/exact authorized path/operation -> Guard ALLOW -> protected operation exactly once;
9. runtime binding B mismatches Context/target/path/operation or escapes the authorized binding -> Guard DENY;
10. DENY path executes protected operation zero times, state version unchanged and external-effect count zero;
11. Guard unavailable also DENY/fail-closed;
12. compiler must not require or invent `FACT_EQUALS/ALL_OF/ANY_OF/NOT` source predicates for this case.

A unit test that manually creates `CompiledModelAccessRule`/`RuntimeAccessRequirement` does **not** satisfy this case.

### AC-007 — no bypass from all protected entry types

**CASE-P2-GUARD-NO-BYPASS-001-R07**

- Rule, change, custom action and protected query/read all call Guard;
- STATIC_ALLOW still records one Guard entry and zero evaluator submissions;
- DENY blocks before read/write/execute and side effects.

### AC-008 — atomic publication / Context isolation

**CASE-P2-CONTEXT-ATOMICITY-001-R07**

- valid new compilation publishes whole closure;
- failed P2 candidate leaves old Context unchanged;
- two contexts have independent registries/policies/guards and no mutable global current.

### AC-009 — stable diagnostic/denial

**CASE-P2-DIAGNOSTIC-001-R07**

Repeated runs produce stable codes/order/source location for duplicate System, missing RuleView owner, unknown composite key, invalid path and access denial. Runtime reasons distinguish policy/context/binding/Guard/evaluator failures and do not leak complete runtime data.

### AC-010 — declaration compatibility boundary

**CASE-P2-DECLARATION-BOUNDARY-001-R07**

- retired `DEC-EXPAND-DECLARATION` is not restored to repository/reactor/dependencies;
- surviving legacy RuleView/Config read surface is read-only;
- new P2 compiler/runtime does not write legacy registry or create second authority;
- final removal remains P7.

## 4. Selected-rule delivery / FND-009 regression

**CASE-P2-SELECTED-RULE-001-R07**

- exact PolicyIndex lookup count = 1;
- Guard passes the exact selected `CompiledModelAccessRule` to any evaluator/validator seam;
- no evaluator PolicyIndex re-selection;
- request cannot supply a replacement rule/requirement;
- selected rule/request key mismatch -> DENY before protected operation.

For current P2 `EXACT_RUNTIME_BINDING`, authorization is decided from selected rule + runtime binding facts. Future business predicates require a future Requirement revision.

## 5. Real source wildcard / FND-010 regression

**CASE-P2-SYSTEMS-WILDCARD-READ-001-R07**

Fixture: `dec-demo/src/main/resources/mix/system/systems.xml`.

Required assertions:

- `order/OrderInfo` and `payment/OrderInfo` source READ `path="*"` are accepted;
- each expands only against its exact target `CompiledTargetPathCatalog`;
- result is finite, canonical-sorted and deduplicated exact READ rules;
- explicit exact overlap preserves provenance without duplicate rule key;
- runtime policy index contains zero wildcard keys;
- wildcard WRITE/EXECUTE rejected;
- empty expansion rejected;
- target model-shape change changes digest/forces recompile; old Context does not silently expand permission.

## 6. Closed RuntimeFactValue / FND-011 regression

**CASE-P2-RUNTIME-FACT-VALUE-001-R07**

- class modifier is public+final;
- constructor is not externally accessible;
- only frozen typed factories are public construction seams;
- LIST/OBJECT deep-copy recursively and expose unmodifiable values;
- external subclassing impossible;
- visitor exhaustively handles six kinds;
- no generic mutable payload getter;
- canonical form deterministic.

## 7. Cross-module construction / FND-015 regression

**CASE-P2-RUNTIME-REQUIREMENT-MODULE-BOUNDARY-001-R07**

- `RuntimeAccessRequirement` lives in `dec-core-context` neutral fact package;
- production `dec-core-compiler` can construct it through the frozen public validated factory without split package or reflection;
- context has no dependency on compiler;
- public factory does not grant authorization: a caller-created requirement not present in current selected rule/CompiledModelSet cannot affect Guard decision;
- `RuntimeRequirementKey` cannot be caller-chosen/overridden.

This case includes a Java 8 compile/API architecture check, not merely documentation inspection.

## 8. Guard unavailable / timeout / cancellation / fail-closed matrix

Carry forward R04/R05/R06 coverage:

| Condition | Expected |
|---|---|
| policy missing | DENY / POLICY_NOT_FOUND |
| Context mismatch | DENY / CONTEXT_IDENTITY_MISMATCH |
| RUNTIME_GUARD_REQUIRED but runtime binding missing | DENY / RUNTIME_BINDING_REQUIRED |
| binding target/path/operation mismatch | DENY / RUNTIME_BINDING_MISMATCH |
| Guard unavailable sentinel | DENY / GUARD_UNAVAILABLE |
| evaluator required but unavailable | DENY / RUNTIME_EVALUATOR_UNAVAILABLE |
| evaluator exception | DENY / RUNTIME_EVALUATOR_EXCEPTION |
| evaluator null | DENY / RUNTIME_EVALUATOR_NULL |
| evaluator timeout/non-return | DENY / RUNTIME_EVALUATOR_TIMEOUT + cancellation |
| evaluator unknown | DENY / RUNTIME_EVALUATOR_UNKNOWN |
| STATIC_ALLOW | Guard entry 1, evaluator submit 0, ALLOW |
| runtime binding matches | ALLOW only after exact selected-rule validation |

Every DENY asserts protected read/write/execute count = 0 and external-effect count = 0.

Timeout tests use injected fake monotonic time / controlled Future; `Thread.sleep` is not the oracle.

## 9. Java 8 / EngineContext compatibility

**CASE-P2-JAVA8-ENGINE-CONTEXT-001-R07**

- production P2 API compiles with Java release 8;
- no record / Java 9 collection factory/copy API in production source;
- `EngineContext` remains `public final class`;
- existing single-arg constructor and `compiledModelSet()/modelSet()/projection()` remain callable;
- P2 APIs are additive;
- no new `findRuleView(String)` bare-name API;
- existing equals/hashCode/toString behavior is not silently changed by P2 metadata.

## 10. Runtime requirement API security

**CASE-P2-RUNTIME-REQUIREMENT-AUTHORITY-001-R07**

- public validated factory accepts only a complete authorized rule key, frozen kind and SourceRef;
- deterministic key derived inside factory;
- caller cannot inject the constructed value into `ModelAccessRequest` as authority;
- Guard uses only requirement embedded in current selected rule from current Context;
- requirement can only validate/narrow runtime binding against the statically authorized surface.

## 11. Formal TDD RED examples

Initial API shape, before symbols exist:

- reflection/string lookup for class/modifier/method signature;
- source/bytecode contract scan that compiles against existing code.

After legal minimal API skeleton exists:

- direct typed behavior tests;
- expected RED is failed assertion/behavior, not compilation failure.

Evidence for each RED must record command, target test, intended failing oracle and actual failure category.

## 12. Traceability

| Acceptance / Finding | Blocking case |
|---|---|
| AC-001 | CASE-P2-SYSTEM-DETERMINISM-001-R07 |
| AC-002 | CASE-P2-RULEVIEW-COMPOSITE-001-R07 |
| AC-003 | CASE-P2-RULEVIEW-CALL-001-R07 |
| AC-004 | CASE-P2-ACCESS-MATRIX-001-R07 |
| AC-005 | CASE-P2-MODEL-PATH-001-R07 |
| AC-006 / FND-014 / FND-016 | CASE-P2-DYNAMIC-SOURCE-TO-GUARD-001-R07 |
| AC-007 / FND-001 | CASE-P2-GUARD-NO-BYPASS-001-R07 |
| AC-008 | CASE-P2-CONTEXT-ATOMICITY-001-R07 |
| AC-009 | CASE-P2-DIAGNOSTIC-001-R07 |
| AC-010 / FND-003 | CASE-P2-DECLARATION-BOUNDARY-001-R07 |
| FND-009 | CASE-P2-SELECTED-RULE-001-R07 |
| FND-010 | CASE-P2-SYSTEMS-WILDCARD-READ-001-R07 |
| FND-011 | CASE-P2-RUNTIME-FACT-VALUE-001-R07 |
| FND-012 | §2 valid-RED command/oracle contract |
| FND-015 | CASE-P2-RUNTIME-REQUIREMENT-MODULE-BOUNDARY-001-R07 |
| FND-008 | CASE-P2-JAVA8-ENGINE-CONTEXT-001-R07 |

## 13. Review and phase gate

`TESTDESIGN-P2-R07` cannot pass before exact `DESIGN-P2-R06` passes and RC9 machine lifecycle binds the current revisions.

After Design closure, exact R07 requires independent Test Design reviewers including RequirementReviewAgent, DesignReviewAgent, TDDReviewAgent and TestEvidenceReviewAgent under the current RC9 contract.

Implementation Plan / TDD / Development remain BLOCKED while any effective P0/P1 is open.
