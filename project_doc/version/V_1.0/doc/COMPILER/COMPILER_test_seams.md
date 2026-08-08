# COMPILER P2 设计测试接缝

> Revision：`DESIGN-P2-R10`。正式 Test Design candidate：`TESTDESIGN-P2-R11`。
> 本 Revision 冻结 repository-valid module/package seams；不创建 production skeleton，也不执行 TDD。

## 1. Module seams

| Seam | Exact module | Production owner |
|---|---|---|
| Neutral access contracts/API shape | `dec-core-context` | `dec.core.context.model.access.*` |
| Dynamic classifier/rule/plan | `dec-core-compiler` | `dec.core.compiler.access.*` |
| Resolver/Gateway/Guard/verifier/registry | `dec-core-starter` | `dec.core.starter.access.*` |
| Bootstrap-time trusted adapters | `dec-core-starter` | `dec.core.starter.access.spi.*` |
| Real source integration | `dec-demo` | existing `systems.xml` + starter dependency |

No test may target a nonexistent `framework execution runtime`/`dec-core-runtime` module。

## 2. Starter runtime ownership seams — FND-004

Architecture/API inspection must require planned production classes in `dec-core-starter`：

```text
dec.core.starter.access.ProtectedAccessRuntime
dec.core.starter.access.ProtectedAccessRuntimeFactory
dec.core.starter.access.DefaultProtectedAccessResolver
dec.core.starter.access.DefaultProtectedAccessGateway
dec.core.starter.access.DefaultModelAccessGuard
dec.core.starter.access.DefaultRuntimeBindingVerifier
dec.core.starter.access.ContextLocalProtectedAccessRegistry
```

SPI owner：

```text
dec.core.starter.access.spi.ProtectedTargetResolutionPort
dec.core.starter.access.spi.ProtectedOperationExecutionPort
dec.core.starter.access.spi.ProtectedAccessAdapterRegistry
```

Assertions：
- no new Maven runtime module required；
- no context -> starter/compiler reverse dependency；
- no compiler -> starter dependency；
- no P2 starter -> dec-core-model dependency merely to access POJO；
- adapters registered at runtime composition, not passed per execution as target/callback。

## 3. Production classifier fixtures

Real source is `dec-demo/src/main/resources/mix/system/systems.xml`：
- direct `status = 1` -> `STATIC_BOUND`；
- `every(orderDetailList,status = 1)` element `status` READ -> `RUNTIME_OBJECT_BOUND`；
- unsupported dynamic selector -> compile ERROR；
- READ `*` expands compile-time to exact member rule；no parent fallback。

Classifier stub cannot prove production correctness。

## 4. STATIC_ALLOW Guard path — FND-001 regression

Starter harness must observe：
- generic resolver capability created without RuntimeBindingPlan；
- Gateway=1；Guard=1；Guard PolicyIndex lookup=1；Gateway lookup=0；
- selected STATIC_ALLOW plan/requirement empty；
- RuntimeBindingVerifier=0；evaluator=0；
- same hidden target operation=1；
- direct caller static executor outside starter runtime unavailable or `MODEL_ACCESS_GUARD_BYPASS` before operation。

## 5. Runtime binding / substitution — FND-017/FND-019

Starter harness provides C1/frame/owner/cursor fixtures and context-local registry probe：
- actual element A -> runtime-required rule -> verifier match -> A operation=1；
- foreign B under same static tuple -> DENY；
- capability A + forced executor target B -> `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH`, A/B operation=0；
- member removal/frame expiry/Context replacement -> stale DENY；
- replay -> consumed DENY；concurrent replay -> at most one success。

## 6. Adapter integration/no-bypass seam

Use an immutable starter test `ProtectedAccessAdapterRegistry` with deterministic fake framework ports。Tests must prove：
- registry is supplied when `ProtectedAccessRuntimeFactory` composes the runtime；
- per-call `execute(context,intent)` has no adapter/raw-target/callback argument；
- resolver records the selected adapter with capability；
- gateway invokes only that bound execution port；
- absent adapter -> `PROTECTED_ACCESS_ADAPTER_UNAVAILABLE`, operation/effects=0。

Future production adapter implementations are out of P2 Test Design；only their mandatory integration contract is tested here。

## 7. P2/P3 boundary seam

Repository inspection must fail the P2 architecture test if the P2 change introduces：
- full Rule/change/action/query business executors；
- QueryPlan semantics；
- datasource transaction orchestration；
- source-authored runtime permission predicate DSL；
- starter direct dependency on a future business-executor module。

Starter may implement only access-control runtime plumbing and SPI。

## 8. Real end-to-end seam (`dec-demo`)

`dec-demo` is the exact module for real `systems.xml` source-to-operation integration because it already contains the resource and depends on `dec-core-starter`。

Oracle：

```text
real systems.xml
 -> production compiler/classifier
 -> immutable EngineContext
 -> context-bound starter ProtectedAccessRuntime
 -> trusted test adapter registry
 -> direct status static branch: Guard=1/verifier=0/op=1
 -> every element runtime branch: Guard=1/verifier=1/op=1 on valid A
 -> invalid proof/substitution: op=0/effects=0
```

## 9. Exact planned test classes

- `dec-core-context`: `dec.core.context.model.access.ProtectedAccessApiContractTest`
- `dec-core-compiler`: `dec.core.compiler.access.ModelAccessRuleCompilationContractTest`
- `dec-core-starter`: `dec.core.starter.access.ProtectedAccessRuntimeOwnershipTest`
- `dec-core-starter`: `dec.core.starter.access.ProtectedAccessStaticAllowPathTest`
- `dec-core-starter`: `dec.core.starter.access.RuntimeBindingProofIntegrationTest`
- `dec-core-starter`: `dec.core.starter.access.ProtectedAccessOperationBindingTest`
- `dec-core-starter`: `dec.core.starter.access.UnifiedProtectedAccessBranchTest`
- `dec-demo`: `dec.demo.p2.P2DynamicClassifierRealFixtureTest`
- `dec-demo`: `dec.demo.p2.P2DynamicSourceToOperationTest`

These are **planned TDD targets**, not claims that the files currently exist。

## 10. RED validity

For every planned TestClass：bootstrap dependencies with `-am -Dmaven.test.skip=true install`, then run the exact target module **without `-am`** and `-Dsurefire.failIfNoSpecifiedTests=true`。Missing module/test/symbol/setup failure is `INVALID_RED`; pre-skeleton first RED must use a compilable reflection/source/API-shape test when needed。

## 11. Other carried seams

Continue deterministic System source provider、RuleView composite identity、wildcard exact expansion、PolicyIndex lookup spy、RuntimeFactValue immutability、unavailable Guard sentinel、controlled Future/fake monotonic time and protected-operation/effect probes。Timeout oracle must not use `Thread.sleep`。
