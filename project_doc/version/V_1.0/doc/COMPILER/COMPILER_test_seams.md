# COMPILER P2 Test Seams

> Revision：`DESIGN-P2-R15`  
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`

## 1. System ownership seam

Observable without private-field inspection:

- compile source order A/B and B/A -> same SystemKey set/version digest/ownership sets;
- `EngineContext.system(key)` exposes immutable snapshot;
- every owned key resolves through current compiled facts;
- no orphan/missing owned fact;
- mutation of returned sets impossible;
- two Contexts remain isolated.

Required deterministic fixture dimensions:
- declared version present vs absent;
- source content change changes source semantic digest;
- ownership change changes semantic digest;
- same semantic input/order variation does not.

## 2. RuleView relation seam

Observe through public resolver/context:

- RuleView missing System -> stable ERROR;
- same-System duplicate -> stable ERROR;
- cross-System same name -> isolated success;
- `view-ref` resolves to exact `ViewKey` and is returned by `CompiledRuleView.resolvedViewKey()`;
- unknown/wrong-owner View -> stable ERROR;
- rule refs resolve deterministically and preserve canonical order;
- bare-name new lookup absent/rejected.

## 3. Shared ModelPath seam

Feed semantically equivalent raw path facts through RULE, CHANGE, QUERY_CONTRACT and MODEL_ACCESS compilation entry seams.

Oracle:
- value-equal canonical `ModelPath`;
- same canonical segments/case semantics;
- same invalid-segment classification;
- no consumer-specific parent/fuzzy fallback;
- query test stops at compile/IR contract and does not implement P6 execution.

Real fixture should include `status` and `every(orderDetailList,status=1)` / change counterpart.

## 4. Operation independence seam

For same System/target/path, publish only one operation rule at a time and assert the other two operations exact-miss/deny. Do not test merely declared-vs-undeclared for the same operation.

## 5. Policy publication seam

Observe:

```text
compiled rules
 -> ModelAccessPolicyIndex.of
 -> same index enters SemanticDigestInput
 -> same immutable index retained by DigestBoundCompiledInput
 -> CompiledModelSetBuilder candidate uses CompiledModelSet.published
 -> EngineContext returns same authority
```

Legacy eight-arg constructor -> empty policy and protected access fail-closed.

## 6. P2 production seam / AC-007

Test public/protected API shape and production composition rather than future P3/P4/P6 business state machines.

Blocking oracle:
- external `dec-demo` production-style consumer obtains/uses only public `ProtectedExecutionBridge`;
- no reflection/package-private helper/manual issued pair;
- no public/protected issued-pair/capability mint method;
- no public API accepts an already-Guarded allow plus caller-selected target operation;
- compatibility adapter cannot write/mint/execute protected mutation;
- policy authority accessible to runtime is exactly current EngineContext index;
- allow and deny both pass the same Bridge→Gateway→Guard seam.

Downstream concrete Rule/change/action/query integration remains P3/P4/P6 acceptance work.

## 7. Runtime binding / target seam

Controllable fixtures expose frame/owner/cursor/membership changes without `Thread.sleep`.

Oracle:
- stale/foreign/wrong plan -> DENY before operation;
- capability binds exact target+operation;
- A capability cannot operate B target;
- same capability concurrent terminal success <= 1;
- identical direct bridge arguments are independent invocations and are not token replay.

## 8. Runtime denial diagnostic seam

For each denial class, repeat same invocation against same immutable Context and assert stable:

- code;
- SystemKey;
- optional RuleView provenance;
- AccessOperation;
- canonical ModelPath;
- policy SourceRef.

Minimum denial classes:
- `POLICY_NOT_FOUND`;
- `RUNTIME_BINDING_STALE`;
- `RUNTIME_PLAN_MISMATCH`;
- `TARGET_SUBSTITUTION`;
- `GUARD_UNAVAILABLE`.

Assert denial text/value object does not include sensitive runtime value/object dump/credential/config payload.

## 9. Atomic publication / Diagnostic seam

Any duplicate System, ownership mismatch, missing RuleView System, unknown View/Rule, invalid path or static permission error -> candidate publication count 0 and old Context unchanged.

Repeat compile with same invalid source -> same ordered Diagnostic codes/definition identities/SourceRefs.

## 10. Formal TDD RED rule

```bash
./mvnw -pl <EXACT-MODULE> -am -Dmaven.test.skip=true install
./mvnw -pl <EXACT-MODULE> -Dtest=<EXACT-TESTCLASS> -Dsurefire.failIfNoSpecifiedTests=true test
```

Second target command must not use `-am`. Missing test/class/symbol/setup/compile failure that prevents the intended assertion from running is `INVALID_RED`.

No TDD skeleton is created while Design/TestDesign review gates are blocked.
