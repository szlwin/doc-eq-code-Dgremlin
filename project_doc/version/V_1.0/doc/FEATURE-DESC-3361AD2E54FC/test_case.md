# FEATURE-DESC-3361AD2E54FC Test Design

> Revision：`TESTDESIGN-P2-R12`。
> Base：`TESTDESIGN-P2-R11`。
> Inputs：Requirement `REQAN-P2-R01`、Business Model candidate `BM-R12`、Design candidate `DESIGN-P2-R11`。
> Status：`NEEDS_CHANGES_CANDIDATE_FIXED / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED`。本 Revision 新增 FND-004 的 trusted-input authority 与 single-policy-authority blocking cases，并补齐 FND-007 fail-closed matrix；Test Design 不创建 skeleton、不执行 TDD。

## 1. Principles

1. 所有 protected READ/WRITE/EXECUTE，包括 STATIC_ALLOW，都必须进入同一 starter-owned runtime -> Gateway -> Guard path。
2. Public `ProtectedAccessResolutionContext` / `ProtectedOperationIntent` 只是 read contracts；caller 自行实现接口不产生 authority。
3. 生产只接受 starter context-local registry 已签发并登记的 exact context+intent pair；authenticity failure 必须早于 target resolution/capability issuance/policy lookup。
4. 唯一 runtime policy authority 是 compiler-published、CompiledModelSet-owned、EngineContext-retained 的 immutable `ModelAccessPolicyIndex`。
5. `STATIC_ALLOW` 只能是 Guard exact lookup 后内部 fast path；无 RuntimeBindingPlan/verifier/evaluator。
6. Runtime binding proof 只属于 selected `RUNTIME_GUARD_REQUIRED`。
7. actual target 与 operation one-shot binding 不可替换；proof/capability A 不得授权 B。
8. P2 concrete runtime owner 是真实 `dec-core-starter`；不使用抽象 `<target-module>` 或不存在的 `dec-core-runtime`。
9. valid RED：bootstrap 可以 `-am`，正式 target test **禁止 `-am`**；missing module/test/symbol/setup/compile failure = INVALID_RED。
10. Implementation Plan/TDD/Development 在 exact Design Review 与 machine gate 前仍 BLOCKED。

## 2. Exact Maven / planned TestClass contract

> 下表 TestClass 是下一阶段 TDD 的冻结目标名称，不是当前已存在/已执行 Evidence。

| Purpose | Exact module | Planned TestClass |
|---|---|---|
| Neutral access API/module shape | `dec-core-context` | `dec.core.context.model.access.ProtectedAccessApiContractTest` |
| Immutable policy index API | `dec-core-context` | `dec.core.context.model.access.ModelAccessPolicyIndexContractTest` |
| Rule status/plan invariant | `dec-core-compiler` | `dec.core.compiler.access.ModelAccessRuleCompilationContractTest` |
| Policy index publication/digest | `dec-core-compiler` | `dec.core.compiler.access.ModelAccessPolicyIndexPublicationTest` |
| Starter ownership | `dec-core-starter` | `dec.core.starter.access.ProtectedAccessRuntimeOwnershipTest` |
| Framework input authenticity | `dec-core-starter` | `dec.core.starter.access.ProtectedAccessInputAuthorityTest` |
| Single policy authority integration | `dec-core-starter` | `dec.core.starter.access.ModelAccessPolicyAuthorityIntegrationTest` |
| STATIC_ALLOW Guard path | `dec-core-starter` | `dec.core.starter.access.ProtectedAccessStaticAllowPathTest` |
| Runtime membership proof | `dec-core-starter` | `dec.core.starter.access.RuntimeBindingProofIntegrationTest` |
| A-proof/B-target substitution + TOCTOU | `dec-core-starter` | `dec.core.starter.access.ProtectedAccessOperationBindingTest` |
| Unified static/runtime branch counts | `dec-core-starter` | `dec.core.starter.access.UnifiedProtectedAccessBranchTest` |
| Real production classifier fixture | `dec-demo` | `dec.demo.p2.P2DynamicClassifierRealFixtureTest` |
| Full real source -> operation | `dec-demo` | `dec.demo.p2.P2DynamicSourceToOperationTest` |

Exact command pattern：

```bash
./mvnw -pl <EXACT-MODULE> -am -Dmaven.test.skip=true install
./mvnw -pl <EXACT-MODULE> -Dtest=<EXACT-TESTCLASS> -Dsurefire.failIfNoSpecifiedTests=true test
```

The second command must not use `-am`。

## 3. FND-004 framework input authenticity — BLOCKING

### CASE-P2-PROTECTED-INPUT-AUTHORITY-001-R12

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

Positive oracle：

```text
trusted framework execution state
 -> starter internal issueInvocation
 -> exact issued context A + exact issued intent A
 -> registry authoritative record A
 -> ProtectedAccessRuntime.execute(A,A)
 -> authenticity PASS
 -> target resolution proceeds
```

Required negative fixtures：

1. caller anonymous/fake `ProtectedAccessResolutionContext` with chosen `engineContextId`；
2. forged `accessConsumerIrKey`；
3. foreign/forged `frameId`；
4. foreign/forged `ownerResolutionId`；
5. forged collection cursor；
6. caller anonymous/fake `ProtectedOperationIntent`；
7. forged requestedRuleKey；
8. operation substitution READ -> WRITE；
9. operation substitution READ -> EXECUTE；
10. issued context A + issued intent B from another invocation；
11. expired/replayed issued pair；
12. public API inspection confirms no public/protected production issued-object constructor/factory and no per-call runtime mint/sign API for arbitrary caller facts。

Expected：

```text
unknown/fabricated input
 -> DENY / PROTECTED_ACCESS_INPUT_UNTRUSTED

issued A-context + B-intent
 -> DENY / PROTECTED_ACCESS_INPUT_PAIR_MISMATCH
```

For every authenticity failure：

```text
target resolver calls = 0
capability issuance = 0
Gateway = 0
Guard = 0
PolicyIndex lookup = 0
RuntimeBindingVerifier = 0
protected operation = 0
state change = 0
external effects = 0
```

A test that only checks capability non-forgeability is insufficient；the pre-capability inputs themselves must be proven non-authoritative unless issued by framework registry state。

## 4. FND-004 single policy authority — BLOCKING

### CASE-P2-POLICY-INDEX-AUTHORITY-001-R12

This blocking family spans three exact modules/classes。

#### 4.1 Context API

```text
dec-core-context
dec.core.context.model.access.ModelAccessPolicyIndexContractTest
```

```bash
./mvnw -pl dec-core-context -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-context -Dtest=dec.core.context.model.access.ModelAccessPolicyIndexContractTest -Dsurefire.failIfNoSpecifiedTests=true test
```

Required：

- `ModelAccessPolicyIndex` immutable；
- exact `find(ModelAccessRuleKey)` only；
- no wildcard/prefix/suffix/parent/bare-name lookup；
- duplicate key and key/rule identity mismatch invalid；
- `CompiledModelSet.modelAccessPolicyIndex()` exists as additive published surface；
- `EngineContext.modelAccessPolicyIndex()` returns the same current immutable authority as the CompiledModelSet。

#### 4.2 Compiler publication / digest

```text
dec-core-compiler
dec.core.compiler.access.ModelAccessPolicyIndexPublicationTest
```

```bash
./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-compiler -Dtest=dec.core.compiler.access.ModelAccessPolicyIndexPublicationTest -Dsurefire.failIfNoSpecifiedTests=true test
```

Required：

- compiler builds policy index from final exact `CompiledModelAccessRule`s；
- READ `*` already finite-expands before index publication；runtime wildcard keys = 0；
- index is in same CompiledModelSet publication closure；
- equivalent source ordering -> same canonical policy entries + same semantic digest；
- changing rule exact key/status/runtime requirement/runtime plan changes semantic digest；
- runtime capability/issued invocation state does not affect semantic digest。

#### 4.3 Starter Guard integration

```text
dec-core-starter
dec.core.starter.access.ModelAccessPolicyAuthorityIntegrationTest
```

```bash
./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-starter -Dtest=dec.core.starter.access.ModelAccessPolicyAuthorityIntegrationTest -Dsurefire.failIfNoSpecifiedTests=true test
```

Required positive chain：

```text
compiler-published index
 -> CompiledModelSet
 -> EngineContext same immutable authority
 -> valid issued access
 -> DefaultModelAccessGuard exact find = 1
```

Required negative/absence assertions：

```text
resolver policy lookup = 0
gateway policy lookup = 0
runtime verifier policy lookup = 0
adapter policy lookup = 0
starter secondary policy Map used as authority = absent
definitions() scan for authorization = absent
TypedDefinitionRegistries policy reconstruction = absent
```

A copied starter Map that happens to contain the same rules fails this Case；the Guard must read the context-owned published authority itself。

## 5. FND-004 repository ownership — BLOCKING regression

### CASE-P2-RUNTIME-OWNERSHIP-001-R12

**Module/TestClass**

```text
dec-core-starter
dec.core.starter.access.ProtectedAccessRuntimeOwnershipTest
```

```bash
./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-starter -Dtest=dec.core.starter.access.ProtectedAccessRuntimeOwnershipTest -Dsurefire.failIfNoSpecifiedTests=true test
```

Require real `dec-core-starter` ownership for runtime/factory/resolver/gateway/guard/verifier/registry；context contracts remain context；compiler publication remains compiler；no new runtime module/reverse dependency/P2 starter->dec-core-model business coupling；issued implementations are package-private starter-owned classes。

## 6. FND-001 STATIC_ALLOW Guard path — BLOCKING

### CASE-P2-STATIC-ALLOW-GUARD-PATH-001-R12

**Module/TestClass**

```text
dec-core-starter
dec.core.starter.access.ProtectedAccessStaticAllowPathTest
```

```bash
./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-starter -Dtest=dec.core.starter.access.ProtectedAccessStaticAllowPathTest -Dsurefire.failIfNoSpecifiedTests=true test
```

Precondition：`DIRECT_EXACT -> STATIC_BOUND -> STATIC_ALLOW`；selected rule has no plan/requirement。

```text
valid issued pair
 -> authenticity PASS
 -> resolver binds target A
 -> generic capability A
 -> Gateway=1
 -> Guard=1
 -> ModelAccessPolicyIndex exact lookup=1
 -> STATIC_ALLOW
 -> RuntimeBindingVerifier=0
 -> evaluator=0
 -> same A operation=1
 -> consumed
```

Direct caller STATIC operation outside runtime impossible or `MODEL_ACCESS_GUARD_BYPASS` before operation。

## 7. Production classifier real fixture — BLOCKING

### CASE-P2-DYNAMIC-CLASSIFIER-REAL-FIXTURE-001-R12

**Module/TestClass**

```text
dec-demo
dec.demo.p2.P2DynamicClassifierRealFixtureTest
```

```bash
./mvnw -pl dec-demo -am -Dmaven.test.skip=true install
./mvnw -pl dec-demo -Dtest=dec.demo.p2.P2DynamicClassifierRealFixtureTest -Dsurefire.failIfNoSpecifiedTests=true test
```

Real source：`dec-demo/src/main/resources/mix/system/systems.xml` / `system=order` / information `ordered`。

Required：direct `status = 1 -> DIRECT_EXACT -> STATIC_BOUND`；`every(orderDetailList,status=1)` element READ -> `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND`；READ `*` exact expansion；unsupported dynamic selector compile ERROR；classifier stub cannot satisfy。

## 8. Compiled rule/plan invariant

### CASE-P2-RULE-PLAN-INVARIANT-001-R12

**Module/TestClass**

```text
dec-core-compiler
dec.core.compiler.access.ModelAccessRuleCompilationContractTest
```

```bash
./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-compiler -Dtest=dec.core.compiler.access.ModelAccessRuleCompilationContractTest -Dsurefire.failIfNoSpecifiedTests=true test
```

STATIC_ALLOW -> plan/requirement empty；RUNTIME_GUARD_REQUIRED -> exact plan + EXACT_RUNTIME_BINDING；illegal mixed state cannot publish。

## 9. Runtime membership proof — BLOCKING

### CASE-P2-RUNTIME-BINDING-PROOF-001-R12

**Module/TestClass**

```text
dec-core-starter
dec.core.starter.access.RuntimeBindingProofIntegrationTest
```

```bash
./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-starter -Dtest=dec.core.starter.access.RuntimeBindingProofIntegrationTest -Dsurefire.failIfNoSpecifiedTests=true test
```

Valid issued pair + actual element A -> runtime rule -> verifier MATCH -> A op=1；B from another owner/collection -> DENY；stale Context/frame/cursor、wrong rule/plan、forged provenance -> DENY；no raw target/capability mint；missing adapter -> `PROTECTED_ACCESS_ADAPTER_UNAVAILABLE`。

## 10. Operation substitution / TOCTOU — BLOCKING

### CASE-P2-RUNTIME-BINDING-OPERATION-SUBSTITUTION-001-R12

**Module/TestClass**

```text
dec-core-starter
dec.core.starter.access.ProtectedAccessOperationBindingTest
```

```bash
./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-starter -Dtest=dec.core.starter.access.ProtectedAccessOperationBindingTest -Dsurefire.failIfNoSpecifiedTests=true test
```

Capability A -> Guard verifies A -> exact registry-bound execution port operates A once。

Valid A capability + forced target B -> supported API impossible OR `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH`；A/B op=0、state unchanged、effects=0。

Membership/frame/cursor/Context invalidation -> DENY；second execute -> consumed；two concurrent execute -> at most one terminal success。

## 11. Unified branch counts — BLOCKING

### CASE-P2-UNIFIED-PROTECTED-ACCESS-BRANCH-001-R12

**Module/TestClass**

```text
dec-core-starter
dec.core.starter.access.UnifiedProtectedAccessBranchTest
```

```bash
./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-starter -Dtest=dec.core.starter.access.UnifiedProtectedAccessBranchTest -Dsurefire.failIfNoSpecifiedTests=true test
```

| Branch | Input auth | Resolver | Gateway | Guard | Policy lookup | Runtime verifier | Evaluator | Operation |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| forged input | fail | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| STATIC_ALLOW | pass | 1 | 1 | 1 | 1 | 0 | 0 | 1 same target |
| RUNTIME + valid proof | pass | 1 | 1 | 1 | 1 | 1 | 0 | 1 same target |
| RUNTIME + invalid proof | pass | 1 | 1 | 1 | 1 | 1 | 0 | 0 |
| missing adapter | pass | 1 attempt | 0 operation | 0/blocked before policy as designed | 0 | 0 | 0 | 0 |

## 12. Full real source -> protected operation — BLOCKING

### CASE-P2-DYNAMIC-SOURCE-TO-OPERATION-001-R12

**Module/TestClass**

```text
dec-demo
dec.demo.p2.P2DynamicSourceToOperationTest
```

```bash
./mvnw -pl dec-demo -am -Dmaven.test.skip=true install
./mvnw -pl dec-demo -Dtest=dec.demo.p2.P2DynamicSourceToOperationTest -Dsurefire.failIfNoSpecifiedTests=true test
```

```text
real systems.xml
 -> production compiler/classifier
 -> immutable exact ModelAccessPolicyIndex published in CompiledModelSet
 -> EngineContext retains same policy authority
 -> starter runtime + trusted adapters
 -> starter-issued context+intent
 -> authenticity PASS
 -> static direct status: no plan, Guard=1, policy lookup=1, verifier=0, op=1
 -> runtime every element: exact plan, Guard=1, policy lookup=1, verifier=1, valid A op=1
 -> caller-forged input: resolver=0/policy lookup=0/op=0
 -> foreign/stale/substituted proof/target: DENY/op=0/effects=0
```

Manual compiled rule、classifier stub、detached proof-only test、caller-side static fast path or starter secondary policy Map cannot satisfy this Case。

## 13. Neutral API / Java8 compatibility

### CASE-P2-PROTECTED-ACCESS-API-001-R12

**Module/TestClass**

```text
dec-core-context
dec.core.context.model.access.ProtectedAccessApiContractTest
```

```bash
./mvnw -pl dec-core-context -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-context -Dtest=dec.core.context.model.access.ProtectedAccessApiContractTest -Dsurefire.failIfNoSpecifiedTests=true test
```

Oracle：Java 8；neutral contracts contain no starter dependency；EngineContext existing constructor/accessors remain；additive policy index accessor；public input interfaces are read-only contracts；capability has no public mint/raw target/selected-policy setter；RuntimeFactValue remains closed immutable typed value。

## 14. Existing acceptance matrix carried forward

- AC-001 System determinism：same canonical SystemKey/order/digest；duplicate/conflict stable ERROR/no partial publish。
- AC-002 RuleView identity：`(SystemKey,name)`；missing owner stable ERROR；no bare-name fallback。
- AC-003 exact RuleView call only。
- AC-004 READ/WRITE/EXECUTE independent；undeclared/shared-write default denied；all protected operations use starter runtime。
- AC-005 exact canonical ModelPath only。
- AC-007 no bypass：current/future consumer must use trusted-issued input + starter runtime；caller-created input is not authority。
- AC-008 immutable whole-context publication；policy index belongs compiled facts，runtime issuance/capability registry is context-local and outside digest facts。
- AC-009 stable diagnostics/reasons including forged input/pair mismatch/adapter unavailable/Guard bypass/proof/stale/plan/substitution/consumed。
- AC-010 retired declaration module remains read-only compatibility only。

## 15. Fail-closed matrix — FND-007

| Condition | Expected | Policy lookup | Operation/effects |
|---|---|---:|---:|
| caller-fabricated context | DENY / PROTECTED_ACCESS_INPUT_UNTRUSTED | 0 | 0 |
| caller-fabricated intent | DENY / PROTECTED_ACCESS_INPUT_UNTRUSTED | 0 | 0 |
| forged consumerIrKey/frame/owner/cursor | DENY / PROTECTED_ACCESS_INPUT_UNTRUSTED | 0 | 0 |
| READ -> WRITE/EXECUTE forged intent | DENY / PROTECTED_ACCESS_INPUT_UNTRUSTED | 0 | 0 |
| issued context A + intent B | DENY / PROTECTED_ACCESS_INPUT_PAIR_MISMATCH | 0 | 0 |
| policy missing after valid input | DENY / POLICY_NOT_FOUND | 1 | 0 |
| Context mismatch | DENY / CONTEXT_IDENTITY_MISMATCH | <=1 after input PASS | 0 |
| direct operation outside runtime | DENY / MODEL_ACCESS_GUARD_BYPASS | 0 | 0 |
| trusted adapter missing | DENY / PROTECTED_ACCESS_ADAPTER_UNAVAILABLE | 0 | 0 |
| STATIC rule contains runtime plan | invalid compiled state | n/a | 0 |
| runtime plan/requirement missing | DENY / invalid compiled state | 1 | 0 |
| proof invalid | DENY / RUNTIME_BINDING_PROOF_INVALID | 1 | 0 |
| stale frame/membership | DENY / RUNTIME_BINDING_STALE | 1 | 0 |
| wrong rule/plan | DENY / RUNTIME_BINDING_PLAN_MISMATCH | 1 | 0 |
| target substitution | DENY / RUNTIME_BINDING_OPERATION_TARGET_MISMATCH | 1 | 0 |
| capability replay | DENY / RUNTIME_BINDING_CAPABILITY_CONSUMED | 0/1 per reserved-order contract, never operation | 0 |
| Guard unavailable | DENY / GUARD_UNAVAILABLE | 0 | 0 |

Every DENY -> protected operation=0 + external effects=0。Forged-input cases must additionally prove resolver/capability issuance=0。

## 16. Traceability

| Finding/Acceptance | Blocking case |
|---|---|
| FND-004 trusted issuance | CASE-P2-PROTECTED-INPUT-AUTHORITY-001-R12 |
| FND-004 single policy authority | CASE-P2-POLICY-INDEX-AUTHORITY-001-R12 |
| FND-004 module ownership | CASE-P2-RUNTIME-OWNERSHIP-001-R12 |
| FND-001 / AC-007 | CASE-P2-STATIC-ALLOW-GUARD-PATH-001-R12 + CASE-P2-UNIFIED-PROTECTED-ACCESS-BRANCH-001-R12 |
| FND-006 / FND-012 | §2 exact Maven/valid RED contract |
| FND-007 | §15 fail-closed matrix + forged-input case |
| FND-018 / AC-006 | CASE-P2-DYNAMIC-CLASSIFIER-REAL-FIXTURE-001-R12 |
| FND-014/FND-016 / AC-006 | CASE-P2-DYNAMIC-SOURCE-TO-OPERATION-001-R12 |
| FND-017 | CASE-P2-RUNTIME-BINDING-PROOF-001-R12 |
| FND-019 | CASE-P2-RUNTIME-BINDING-OPERATION-SUBSTITUTION-001-R12 |
| FND-008/FND-015 | CASE-P2-PROTECTED-ACCESS-API-001-R12 + policy-index context/compiler contracts |

## 17. Review / phase gate

`TESTDESIGN-P2-R12` remains BLOCKED until exact `DESIGN-P2-R11` passes required Architecture/ApiContract/Develop/Impact/CrossModule/Concurrency and other specialist Reviews and RC9 machine lifecycle/risk Evidence binds current revisions。Planned TestClass names/commands are contract only；no TDD execution is legal while effective P1 remains open。