# FEATURE-DESC-3361AD2E54FC Test Design

> Revision：`TESTDESIGN-P2-R13`。
> Base：`TESTDESIGN-P2-R12`。
> Inputs：Requirement `REQAN-P2-R01`、Business Model candidate `BM-R12`、Design candidate `DESIGN-P2-R12`。
> Status：`NEEDS_CHANGES_CANDIDATE_FIXED / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED`。本 Revision 新增 production trusted-issuance reachability 与 policy-publication-constructor compatibility blocking cases，并把所有 runtime E2E 切到 composition-issued bridge；不创建 skeleton、不执行 TDD。

## 1. Principles

1. 所有 protected READ/WRITE/EXECUTE，包括 STATIC_ALLOW，都必须进入 starter-owned bridge -> internal issuance -> Gateway -> Guard path。
2. External consumer 不直接获得 `issueInvocation(...)`、issued context/intent mint API 或 public `execute(context,intent)` production entry。
3. Production reachability 由 composition-issued exact `ProtectedExecutionBridge` 提供；bridge 固定 consumer/rule/operation，per-call 只接受 bound trusted state port 可识别的 opaque token。
4. caller 自行实现 `ProtectedExecutionToken`、`ProtectedAccessResolutionContext` 或 `ProtectedOperationIntent` 都不产生 authority。
5. 唯一 runtime policy authority 是 compiler-published、CompiledModelSet-owned、EngineContext-retained immutable `ModelAccessPolicyIndex`。
6. `ModelAccessPolicyIndex` 必须通过 validated context factory 构造；P2 compiler 必须在 semantic digest binding 前完成 index 并走 policy-aware `CompiledModelSet.published(...)`。
7. Legacy 八参数 CompiledModelSet constructor 保持兼容且确定性 empty-policy fail closed，不重建 policy。
8. STATIC_ALLOW 只能是 Guard exact lookup 后内部 fast path；无 RuntimeBindingPlan/verifier/evaluator。
9. Runtime proof 只属于 selected `RUNTIME_GUARD_REQUIRED`；actual target 与 operation one-shot binding 不可替换。
10. valid RED：bootstrap 可以 `-am`，正式 target test 禁止 `-am`；missing module/test/symbol/setup/compile failure = INVALID_RED。
11. Implementation Plan/TDD/Development 在 exact Design Review 与 machine gate 前仍 BLOCKED。

## 2. Exact Maven / planned TestClass contract

| Purpose | Exact module | Planned TestClass |
|---|---|---|
| Neutral protected-access API/Java8 | `dec-core-context` | `dec.core.context.model.access.ProtectedAccessApiContractTest` |
| Validated policy-index factory | `dec-core-context` | `dec.core.context.model.access.ModelAccessPolicyIndexContractTest` |
| CompiledModelSet legacy/new publication compatibility | `dec-core-context` | `dec.core.context.model.ModelAccessPolicyPublicationCompatibilityTest` |
| Rule status/plan invariant | `dec-core-compiler` | `dec.core.compiler.access.ModelAccessRuleCompilationContractTest` |
| Policy index publication + digest closure | `dec-core-compiler` | `dec.core.compiler.access.ModelAccessPolicyIndexPublicationTest` |
| Starter ownership | `dec-core-starter` | `dec.core.starter.access.ProtectedAccessRuntimeOwnershipTest` |
| Production bridge API/token authority | `dec-core-starter` | `dec.core.starter.access.ProtectedExecutionBridgeContractTest` |
| Internal issued-pair defense | `dec-core-starter` | `dec.core.starter.access.ProtectedAccessInputAuthorityTest` |
| Single policy authority integration | `dec-core-starter` | `dec.core.starter.access.ModelAccessPolicyAuthorityIntegrationTest` |
| STATIC_ALLOW Guard path | `dec-core-starter` | `dec.core.starter.access.ProtectedAccessStaticAllowPathTest` |
| Runtime membership proof | `dec-core-starter` | `dec.core.starter.access.RuntimeBindingProofIntegrationTest` |
| A-proof/B-target substitution + TOCTOU | `dec-core-starter` | `dec.core.starter.access.ProtectedAccessOperationBindingTest` |
| Unified static/runtime branch counts | `dec-core-starter` | `dec.core.starter.access.UnifiedProtectedAccessBranchTest` |
| Real production classifier fixture | `dec-demo` | `dec.demo.p2.P2DynamicClassifierRealFixtureTest` |
| Trusted production issuance reachability | `dec-demo` | `dec.demo.p2.P2TrustedIssuanceReachabilityTest` |
| Full real source -> protected operation | `dec-demo` | `dec.demo.p2.P2DynamicSourceToOperationTest` |

Exact command pattern：

```bash
./mvnw -pl <EXACT-MODULE> -am -Dmaven.test.skip=true install
./mvnw -pl <EXACT-MODULE> -Dtest=<EXACT-TESTCLASS> -Dsurefire.failIfNoSpecifiedTests=true test
```

Second command must not use `-am`。

## 3. FND-004 / FND-016 production trusted issuance reachability — BLOCKING

### CASE-P2-TRUSTED-ISSUANCE-REACHABILITY-001-R13

**Module/TestClass**

```text
dec-demo
dec.demo.p2.P2TrustedIssuanceReachabilityTest
```

**Commands**

```bash
./mvnw -pl dec-demo -am -Dmaven.test.skip=true install
./mvnw -pl dec-demo -Dtest=dec.demo.p2.P2TrustedIssuanceReachabilityTest -Dsurefire.failIfNoSpecifiedTests=true test
```

**Positive production oracle**

The test class lives in `dec-demo`, not starter package, and may only use public production SPI：

```text
immutable EngineContext
 -> ProtectedAccessAdapterRegistry composition
      exact consumer/rule/operation registration
      trusted ProtectedExecutionStatePort
      target-resolution port
      operation-execution port
      bridge receiver
 -> ProtectedAccessRuntimeFactory creates context-bound runtime
 -> receiver obtains one exact ProtectedExecutionBridge
 -> adapter creates/owns one current opaque ProtectedExecutionToken
 -> bridge.execute(token)
 -> bound state port recognizes token
 -> starter internally derives frame/owner/cursor
 -> starter uses bridge-bound consumer/rule/operation
 -> internal issueInvocation
 -> internal exact issued pair
 -> resolver/capability/Gateway/Guard
 -> same target protected operation
```

**Mandatory API/reachability assertions**

1. `dec-demo` never uses reflection to access starter internals；
2. no package-private starter helper is called from `dec-demo`；
3. no test-only public mint/backdoor；
4. no manual `IssuedProtectedAccessResolutionContext` / `IssuedProtectedOperationIntent` construction；
5. no moving the test into `dec.core.starter.access` to gain package access；
6. no direct Guard/Gateway call substituting for production bridge；
7. bridge has no public/protected constructor/rebind API；
8. public runtime surface does not require external caller to possess issued pair before invocation；
9. public invocation accepts token only, not caller-supplied consumerIrKey/ruleKey/operation/frame/owner/cursor。

**Negative token authority**

Fixtures：

- anonymous/fake `ProtectedExecutionToken`；
- valid token from bridge B passed to bridge A；
- stale/replayed token；
- token from runtime/context C2 passed to C1 bridge。

Expected：

```text
DENY / PROTECTED_EXECUTION_TOKEN_UNTRUSTED
internal issued pair = 0
target resolution = 0
capability issuance = 0
Gateway = 0
Guard = 0
PolicyIndex lookup = 0
RuntimeBindingVerifier = 0
protected operation = 0
state change = 0
external effects = 0
```

This Case is mandatory for FND-004/FND-016 candidate closure。A package-private starter-only test cannot substitute for it。

## 4. FND-004 internal input authenticity — BLOCKING DEFENSE-IN-DEPTH

### CASE-P2-PROTECTED-INPUT-AUTHORITY-001-R13

**Module/TestClass**

```text
dec-core-starter
dec.core.starter.access.ProtectedAccessInputAuthorityTest
```

**Commands**

```bash
./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-starter -Dtest=dec.core.starter.access.ProtectedAccessInputAuthorityTest -Dsurefire.failIfNoSpecifiedTests=true test
```

This is a low-level same-package invariant test, not the external reachability proof。

Required negative fixtures：caller/fake read-interface context、forged consumer/frame/owner/cursor、fake intent、requestedRuleKey substitution、READ->WRITE/EXECUTE、issued context A + issued intent B、expired pair。

Expected：

```text
PROTECTED_ACCESS_INPUT_UNTRUSTED
or PROTECTED_ACCESS_INPUT_PAIR_MISMATCH
resolver = 0
capability = 0
Guard = 0
policy lookup = 0
operation/effects = 0
```

## 5. FND-015 validated policy-index construction — BLOCKING

### CASE-P2-POLICY-INDEX-CONSTRUCTION-001-R13

**Module/TestClass**

```text
dec-core-context
dec.core.context.model.access.ModelAccessPolicyIndexContractTest
```

**Commands**

```bash
./mvnw -pl dec-core-context -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-context -Dtest=dec.core.context.model.access.ModelAccessPolicyIndexContractTest -Dsurefire.failIfNoSpecifiedTests=true test
```

Required API：

```text
ModelAccessPolicyIndex.empty()
ModelAccessPolicyIndex.of(Iterable<CompiledModelAccessRule>)
find(exact key)
keys()
```

Positive：valid mixed static/runtime rules -> deterministic immutable exact index。

Negative：

- duplicate exact key -> construction failure；
- null rule/key -> failure；
- static rule with runtime plan/requirement -> failure；
- runtime-required missing plan/requirement -> failure；
- non-EXACT runtime requirement -> failure；
- wildcard/fuzzy runtime key -> failure；
- post-construction source collection mutation does not mutate index；
- `keys()` cannot mutate index；
- no public raw-map mutable constructor/builder continuation。

## 6. FND-015 CompiledModelSet publication/legacy compatibility — BLOCKING

### CASE-P2-POLICY-PUBLICATION-COMPATIBILITY-001-R13

**Module/TestClass**

```text
dec-core-context
dec.core.context.model.ModelAccessPolicyPublicationCompatibilityTest
```

**Commands**

```bash
./mvnw -pl dec-core-context -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-context -Dtest=dec.core.context.model.ModelAccessPolicyPublicationCompatibilityTest -Dsurefire.failIfNoSpecifiedTests=true test
```

Required：

1. existing public eight-argument `CompiledModelSet` constructor still exists with exact existing parameter signature；
2. old constructor keeps existing model/diagnostic/version behavior；
3. old constructor `modelAccessPolicyIndex()` is deterministic immutable empty；
4. old constructor does not reconstruct policy from definitions/typedRegistries；
5. protected exact lookup on legacy context -> `POLICY_NOT_FOUND` / fail closed；
6. public `CompiledModelSet.published(..., ModelAccessPolicyIndex, ...)` exists as the explicit P2 path；
7. policy-aware set returns exact same/equivalent immutable policy authority；
8. `EngineContext.modelAccessPolicyIndex()` returns the CompiledModelSet authority, not a copy/rebuild；
9. two CompiledModelSet values that differ only in policy index are not equal and have compatible hashCode distinction；
10. legacy constructor supplied digest is retained and is not mislabeled as P2 policy-aware compiler publication。

## 7. FND-015 compiler policy publication + digest closure — BLOCKING

### CASE-P2-POLICY-INDEX-PUBLICATION-001-R13

**Module/TestClass**

```text
dec-core-compiler
dec.core.compiler.access.ModelAccessPolicyIndexPublicationTest
```

**Commands**

```bash
./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-compiler -Dtest=dec.core.compiler.access.ModelAccessPolicyIndexPublicationTest -Dsurefire.failIfNoSpecifiedTests=true test
```

Production oracle：

```text
compiled model-access rules
 -> ModelAccessPolicyIndex.of
 -> SemanticDigestInput receives same immutable index
 -> CompilerDigestService
 -> DigestBoundCompiledInput stores same immutable index + digest
 -> CompiledModelSetBuilder.FrozenInput
 -> CompiledModelSet.published(same index + digest)
 -> EngineContext
```

Assertions：

- index exists before digest compute；
- semantic digest canonicalization includes exact rule key/status/requirement/plan semantic fields；
- equivalent rule insertion order -> same index canonical order + same semantic digest；
- rule add/remove/status/plan/requirement semantic change -> semantic digest changes；
- final published index equals the snapshot used for digest；
- `DigestBoundCompiledInput` has policy-index read surface for candidate publication；
- P2 production candidate path does not invoke legacy eight-arg constructor；
- capability/bridge/token/registry runtime state is absent from semantic digest。

## 8. FND-004 single policy authority — BLOCKING

### CASE-P2-POLICY-INDEX-AUTHORITY-001-R13

**Module/TestClass**

```text
dec-core-starter
dec.core.starter.access.ModelAccessPolicyAuthorityIntegrationTest
```

**Commands**

```bash
./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-starter -Dtest=dec.core.starter.access.ModelAccessPolicyAuthorityIntegrationTest -Dsurefire.failIfNoSpecifiedTests=true test
```

Through a valid bridge invocation：

```text
Guard EngineContext ModelAccessPolicyIndex exact lookup = 1
Resolver policy lookup = 0
Gateway policy lookup = 0
RuntimeBindingVerifier policy lookup = 0
ExecutionStatePort policy lookup = 0
TargetResolutionPort policy lookup = 0
OperationExecutionPort policy lookup = 0
```

Repository/source/API inspection：

- no starter authorization `Map<ModelAccessRuleKey,...>` secondary authority；
- Guard does not scan `definitions()`；
- Guard does not rebuild from `TypedDefinitionRegistries`；
- policy missing derives only from exact current Context index miss。

## 9. FND-001 STATIC_ALLOW Guard path — BLOCKING

### CASE-P2-STATIC-ALLOW-GUARD-PATH-001-R13

**Module/TestClass**

```text
dec-core-starter
dec.core.starter.access.ProtectedAccessStaticAllowPathTest
```

**Commands** follow exact-module pattern。

Precondition：`DIRECT_EXACT -> STATIC_BOUND -> STATIC_ALLOW` and selected rule has no plan/requirement。

Runtime must use bridge path：

```text
recognized token
 -> bridge
 -> internal issuance
 -> resolver target A
 -> capability A
 -> Gateway=1
 -> Guard=1
 -> exact policy lookup=1
 -> STATIC_ALLOW
 -> RuntimeBindingVerifier=0
 -> evaluator=0
 -> A operation=1
```

Direct caller-side STATIC operation outside bridge/runtime is impossible or `MODEL_ACCESS_GUARD_BYPASS` before operation/effects。

## 10. Production classifier real fixture — BLOCKING

### CASE-P2-DYNAMIC-CLASSIFIER-REAL-FIXTURE-001-R13

**Module/TestClass**

```text
dec-demo
dec.demo.p2.P2DynamicClassifierRealFixtureTest
```

Real source：`dec-demo/src/main/resources/mix/system/systems.xml` / system `order` / information `ordered`：

```text
status = 1
every(orderDetailList, status = 1)
```

Required：production parser/compiler real IR；direct status -> STATIC_BOUND；every element -> RUNTIME_OBJECT_BOUND；READ `*` finite exact expansion includes exact member；unsupported dynamic selector compile ERROR；classifier stub cannot satisfy。

## 11. Compiled rule/plan invariant

### CASE-P2-RULE-PLAN-INVARIANT-001-R13

**Module/TestClass**

```text
dec-core-compiler
dec.core.compiler.access.ModelAccessRuleCompilationContractTest
```

Oracle：STATIC_ALLOW plan/requirement empty；RUNTIME_GUARD_REQUIRED exact plan + EXACT_RUNTIME_BINDING；illegal mixed state cannot enter `ModelAccessPolicyIndex.of`/publish；policy canonical entry matches rule state。

## 12. Runtime membership proof — BLOCKING

### CASE-P2-RUNTIME-BINDING-PROOF-001-R13

**Module/TestClass**

```text
dec-core-starter
dec.core.starter.access.RuntimeBindingProofIntegrationTest
```

Use bridge-bound trusted ports。Actual element A -> runtime rule -> verifier match -> A op=1；B from another collection under same static tuple -> DENY；stale Context/frame/cursor、wrong rule/plan、forged provenance -> DENY；no raw target/capability public mint；missing adapter -> fail closed。

## 13. Operation substitution / TOCTOU — BLOCKING

### CASE-P2-RUNTIME-BINDING-OPERATION-SUBSTITUTION-001-R13

**Module/TestClass**

```text
dec-core-starter
dec.core.starter.access.ProtectedAccessOperationBindingTest
```

Positive：bridge/capability A -> Guard verifies A -> exact registry-bound port operates A once。

Negative：valid A capability + forced B -> API impossible OR invariant `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH`；A/B op=0、state unchanged、effects=0。

TOCTOU/replay：membership change、frame/cursor/Context invalidation -> DENY；second capability execute -> consumed；two concurrent execute -> at most one terminal success。Bridge cannot rebind operation/target after composition。

## 14. Unified branch counts — BLOCKING

### CASE-P2-UNIFIED-PROTECTED-ACCESS-BRANCH-001-R13

| Branch | Bridge | Internal issuance | Resolver | Gateway | Guard | Policy lookup | Runtime verifier | Operation |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| STATIC_ALLOW valid token | 1 | 1 | 1 | 1 | 1 | 1 | 0 | 1 same target |
| RUNTIME valid proof | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 1 same target |
| RUNTIME invalid proof | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 0 |
| untrusted token | 1 attempt | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| missing adapter/composition invalid | 0 usable bridge | 0 | 0 | 0 | 0 | 0 | 0 | 0 |

Every failure has effects=0。

## 15. Full real source -> protected operation — BLOCKING

### CASE-P2-DYNAMIC-SOURCE-TO-OPERATION-001-R13

**Module/TestClass**

```text
dec-demo
dec.demo.p2.P2DynamicSourceToOperationTest
```

**Commands**

```bash
./mvnw -pl dec-demo -am -Dmaven.test.skip=true install
./mvnw -pl dec-demo -Dtest=dec.demo.p2.P2DynamicSourceToOperationTest -Dsurefire.failIfNoSpecifiedTests=true test
```

```text
real systems.xml
 -> production compiler/classifier
 -> validated ModelAccessPolicyIndex
 -> semantic digest bound with same index
 -> CompiledModelSet.published
 -> immutable EngineContext
 -> starter runtime factory + trusted test adapter registration
 -> composition-issued bridge delivered to dec-demo adapter
 -> recognized execution token
 -> static direct status: no plan, Guard=1, verifier=0, same target op=1
 -> runtime every element: exact plan, Guard=1, verifier=1, valid A op=1
 -> foreign/stale token/proof/substitution: DENY, op=0, effects=0
```

Manual policy Map、manual CompiledModelSet legacy constructor、manual issued pair、reflection、classifier stub、direct Guard/Gateway cannot satisfy this Case。

## 16. Neutral API / Java8 compatibility

### CASE-P2-PROTECTED-ACCESS-API-001-R13

**Module/TestClass** `dec-core-context / dec.core.context.model.access.ProtectedAccessApiContractTest`。

Oracle：Java8 source/API；context contains no starter dependency；EngineContext existing constructor/accessors remain；CompiledModelSet existing 8-arg constructor remains；new additive policy factory/accessors compile in Java8；capability/read contracts have no raw target or selected-policy mutator；RuntimeFactValue remains closed immutable typed value。

## 17. Fail-closed matrix — FND-007 regression

| Condition | Expected | Pre-operation counters |
|---|---|---|
| untrusted/foreign/stale bridge token | `PROTECTED_EXECUTION_TOKEN_UNTRUSTED` | issuance/policy/op/effects = 0 |
| internal fake context/intent | `PROTECTED_ACCESS_INPUT_UNTRUSTED` | resolver/policy/op/effects = 0 |
| internal A-context + B-intent | `PROTECTED_ACCESS_INPUT_PAIR_MISMATCH` | resolver/policy/op/effects = 0 |
| policy missing | `POLICY_NOT_FOUND` | op/effects = 0 |
| Context mismatch | `CONTEXT_IDENTITY_MISMATCH` | op/effects = 0 |
| direct operation outside runtime | `MODEL_ACCESS_GUARD_BYPASS` | op/effects = 0 |
| trusted adapter missing | `PROTECTED_ACCESS_ADAPTER_UNAVAILABLE` | op/effects = 0 |
| STATIC rule contains runtime plan | invalid compiled/index construction state | publish blocked |
| runtime plan/requirement missing | invalid compiled/index construction state | publish blocked |
| proof invalid | `RUNTIME_BINDING_PROOF_INVALID` | op/effects = 0 |
| stale frame/membership | `RUNTIME_BINDING_STALE` | op/effects = 0 |
| wrong rule/plan | `RUNTIME_BINDING_PLAN_MISMATCH` | op/effects = 0 |
| target substitution | `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH` | op/effects = 0 |
| capability replay | `RUNTIME_BINDING_CAPABILITY_CONSUMED` | op/effects = 0 |
| Guard unavailable | `GUARD_UNAVAILABLE` | op/effects = 0 |

## 18. Existing acceptance matrix carried forward

- AC-001 System determinism；duplicate/conflict stable ERROR/no partial publish。
- AC-002 RuleView `(SystemKey,name)`；missing system stable ERROR；no bare-name fallback。
- AC-003 exact RuleView call only。
- AC-004 READ/WRITE/EXECUTE independent；all protected operations use bridge/runtime boundary。
- AC-005 exact canonical ModelPath only。
- AC-006 legal dynamic `every` source reaches real runtime proof/operation through production bridge。
- AC-007 Rule/change/custom action/future consumer cannot bypass bridge/Gateway/Guard。
- AC-008 immutable whole-context publication；runtime issued/capability registry is context-local and outside compiled facts。
- AC-009 stable diagnostics/reasons including token/input/policy/proof/stale/substitution/replay。
- AC-010 retired declaration module remains compatibility-only。

## 19. Traceability

| Finding/Acceptance | Blocking case |
|---|---|
| FND-004 | TRUSTED-ISSUANCE-REACHABILITY + PROTECTED-INPUT-AUTHORITY + POLICY-INDEX-AUTHORITY |
| FND-015 | POLICY-INDEX-CONSTRUCTION + POLICY-PUBLICATION-COMPATIBILITY + POLICY-INDEX-PUBLICATION |
| FND-016 / AC-006 | TRUSTED-ISSUANCE-REACHABILITY + DYNAMIC-SOURCE-TO-OPERATION |
| FND-001 / AC-007 | STATIC-ALLOW-GUARD-PATH + UNIFIED-BRANCH |
| FND-007 | fail-closed matrix including untrusted token/internal forged pair |
| FND-017 | RUNTIME-BINDING-PROOF |
| FND-018 | DYNAMIC-CLASSIFIER-REAL-FIXTURE |
| FND-019 | RUNTIME-BINDING-OPERATION-SUBSTITUTION |

## 20. Review / phase gate

`TESTDESIGN-P2-R13` remains **NEEDS_CHANGES_CANDIDATE_FIXED / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED** until exact `DESIGN-P2-R12` passes Architecture/ApiContract/Develop/Impact/CrossModule/Concurrency and required Reviews。Planned TestClass/commands are contract only；no TDD skeleton/execution is legal while effective P1 remains open or machine lifecycle/risk Evidence is absent。