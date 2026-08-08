# COMPILER P2 设计测试接缝

> Revision：`DESIGN-P2-R11`。正式 Test Design candidate：`TESTDESIGN-P2-R12`。
> 本 Revision 在 R10 repository-valid seams 上增加 trusted-input authenticity 与 single-policy-authority seams；不创建 production skeleton，也不执行 TDD。

## 1. Module seams

| Seam | Exact module | Production owner |
|---|---|---|
| Neutral access contracts/API shape | `dec-core-context` | `dec.core.context.model.access.*` |
| Immutable ModelAccessPolicyIndex | `dec-core-context` | `dec.core.context.model.access.ModelAccessPolicyIndex` |
| Classifier/rule/plan/policy publication | `dec-core-compiler` | `dec.core.compiler.access.*` |
| Resolver/Gateway/Guard/verifier/registry | `dec-core-starter` | `dec.core.starter.access.*` |
| Issued input implementations | `dec-core-starter` | package-private `IssuedProtectedAccessResolutionContext/Intent` |
| Trusted adapters | `dec-core-starter` | `dec.core.starter.access.spi.*` |
| Real source integration | `dec-demo` | existing `systems.xml` + starter dependency |

No test may target nonexistent `framework execution runtime`/`dec-core-runtime`。

## 2. FND-004 trusted input authenticity seam

Planned starter test must prove production runtime does not trust public interface getters。

Positive fixture：

```text
trusted composition adapter/current execution state
 -> starter internal issueInvocation
 -> exact issued context A + exact issued intent A
 -> registry authoritative record A
 -> runtime authenticity PASS
```

Negative fixtures：

- caller anonymous/fake implementation of `ProtectedAccessResolutionContext` with chosen `engineContextId/accessConsumerIrKey/frameId/ownerResolutionId/cursor`；
- caller fake `ProtectedOperationIntent` with chosen `requestedRuleKey/operation`；
- issued context A + issued intent B；
- foreign frame/owner/cursor/context；
- READ issued context paired with caller WRITE/EXECUTE intent；
- expired/replayed issued pair。

For unknown/forged input：`PROTECTED_ACCESS_INPUT_UNTRUSTED`。
For mismatched issued pair：`PROTECTED_ACCESS_INPUT_PAIR_MISMATCH`。

Mandatory counters on authenticity failure：

```text
target resolution = 0
capability issuance = 0
PolicyIndex lookup = 0
Guard = 0
protected operation = 0
external effects = 0
```

API inspection must find no public/protected constructors/factories for production issued implementations and no public runtime mint/sign method accepting arbitrary caller facts。

## 3. FND-004 single policy authority seam

Context/Compiler/Starter tests together must prove：

```text
compiler exact rules
 -> immutable ModelAccessPolicyIndex
 -> same CompiledModelSet publication closure
 -> semantic digest covers canonical index entries
 -> EngineContext exposes same immutable authority
 -> DefaultModelAccessGuard exact find = 1
```

Negative architecture/API assertions：

- resolver PolicyIndex lookup = 0；
- gateway PolicyIndex lookup = 0；
- runtime verifier PolicyIndex lookup = 0；
- adapter PolicyIndex lookup = 0；
- no starter-owned secondary `Map<ModelAccessRuleKey,...>` used as authority；
- no definitions scan / TypedDefinitionRegistries reconstruction for authorization；
- no runtime wildcard/fuzzy lookup；
- changed policy status/plan/requirement changes semantic digest；equivalent source order does not。

## 4. Starter runtime ownership seams

Continue requiring：

```text
dec.core.starter.access.ProtectedAccessRuntime
dec.core.starter.access.ProtectedAccessRuntimeFactory
dec.core.starter.access.DefaultProtectedAccessResolver
dec.core.starter.access.DefaultProtectedAccessGateway
dec.core.starter.access.DefaultModelAccessGuard
dec.core.starter.access.DefaultRuntimeBindingVerifier
dec.core.starter.access.ContextLocalProtectedAccessRegistry
```

Plus package-private production issued input implementations。No new Maven runtime module, reverse dependency, split package or P2 starter -> dec-core-model coupling。

## 5. Production classifier fixture

Real source `dec-demo/src/main/resources/mix/system/systems.xml`：

- direct `status = 1` -> `STATIC_BOUND`；
- `every(orderDetailList,status = 1)` element `status` READ -> `RUNTIME_OBJECT_BOUND`；
- unsupported dynamic selector -> compile ERROR；
- READ `*` compile-time finite exact expansion；no parent fallback。

Classifier stub cannot prove production correctness。

## 6. STATIC_ALLOW Guard path — FND-001 regression

With a valid issued pair：

```text
authenticity gate PASS
resolver capability no RuntimeBindingPlan
Gateway=1
Guard=1
ModelAccessPolicyIndex exact lookup=1
selected STATIC_ALLOW
RuntimeBindingVerifier=0
evaluator=0
same hidden target operation=1
```

Caller-side direct STATIC path outside starter runtime unavailable or `MODEL_ACCESS_GUARD_BYPASS` before operation。

## 7. Runtime binding / substitution — FND-017/FND-019

Starter harness：

- valid issued pair + actual element A -> runtime rule -> verifier MATCH -> A operation=1；
- foreign B under same static tuple -> DENY；
- capability A + forced executor B -> `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH`, A/B operation=0；
- member removal/frame expiry/Context replacement -> stale DENY；
- replay -> consumed DENY；concurrent replay -> at most one success。

## 8. Adapter integration/no-bypass seam

Use immutable composition-time test adapter registry。Prove：

- adapters frozen at runtime composition；
- per-call execute has no raw target/executor callback/adapter registration；
- starter internal issuance is driven by trusted framework execution state, not caller facts；
- resolver records selected adapter with capability；
- gateway invokes only bound execution port；
- missing adapter -> `PROTECTED_ACCESS_ADAPTER_UNAVAILABLE`, operation/effects=0。

## 9. Real end-to-end seam (`dec-demo`)

```text
real systems.xml
 -> production compiler/classifier
 -> compiler-published ModelAccessPolicyIndex in CompiledModelSet
 -> EngineContext same immutable policy authority
 -> context-bound starter runtime + trusted test adapter
 -> starter-issued pair
 -> authenticity PASS
 -> direct status static: Guard=1/index lookup=1/verifier=0/op=1
 -> every element runtime: Guard=1/index lookup=1/verifier=1/op=1
 -> forged input/pair mismatch: resolver=0/index lookup=0/op=0
 -> invalid proof/substitution: op=0/effects=0
```

## 10. Exact planned test classes

- `dec-core-context`: `dec.core.context.model.access.ProtectedAccessApiContractTest`
- `dec-core-context`: `dec.core.context.model.access.ModelAccessPolicyIndexContractTest`
- `dec-core-compiler`: `dec.core.compiler.access.ModelAccessRuleCompilationContractTest`
- `dec-core-compiler`: `dec.core.compiler.access.ModelAccessPolicyIndexPublicationTest`
- `dec-core-starter`: `dec.core.starter.access.ProtectedAccessRuntimeOwnershipTest`
- `dec-core-starter`: `dec.core.starter.access.ProtectedAccessInputAuthorityTest`
- `dec-core-starter`: `dec.core.starter.access.ModelAccessPolicyAuthorityIntegrationTest`
- `dec-core-starter`: `dec.core.starter.access.ProtectedAccessStaticAllowPathTest`
- `dec-core-starter`: `dec.core.starter.access.RuntimeBindingProofIntegrationTest`
- `dec-core-starter`: `dec.core.starter.access.ProtectedAccessOperationBindingTest`
- `dec-core-starter`: `dec.core.starter.access.UnifiedProtectedAccessBranchTest`
- `dec-demo`: `dec.demo.p2.P2DynamicClassifierRealFixtureTest`
- `dec-demo`: `dec.demo.p2.P2DynamicSourceToOperationTest`

These are planned TDD targets only。

## 11. RED validity

For every planned TestClass：

```bash
./mvnw -pl <EXACT-MODULE> -am -Dmaven.test.skip=true install
./mvnw -pl <EXACT-MODULE> -Dtest=<EXACT-TESTCLASS> -Dsurefire.failIfNoSpecifiedTests=true test
```

Second command must not use `-am`。Missing module/test/symbol/setup/compile failure = `INVALID_RED`；pre-skeleton first RED must use compilable reflection/source/API-shape assertions when necessary。

## 12. Fail-closed instrumentation

Test probes must separately count：input-authenticity checks、target resolution、capability issuance、Guard invocation、PolicyIndex lookup、runtime verifier、protected operation and external effects。This prevents a forged-input test from accidentally reaching Guard and being misreported as a policy DENY。

## 13. Other carried seams

Continue deterministic System source provider、RuleView composite identity、wildcard exact expansion、RuntimeFactValue immutability、unavailable Guard sentinel、controlled Future/fake monotonic time and protected-operation/effect probes。Timeout oracle must not use `Thread.sleep`。