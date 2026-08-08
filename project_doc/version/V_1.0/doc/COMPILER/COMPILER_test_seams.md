# COMPILER P2 设计测试接缝

> Revision：`DESIGN-P2-R09`。正式 Test Design candidate：`TESTDESIGN-P2-R10`。

## 1. Production classifier fixtures

AC-006/classifier acceptance MUST 使用真实当前 grammar：

- source：`dec-demo/src/main/resources/mix/system/systems.xml` -> `order` -> information `ordered` -> `rule-data`；
- direct `status = 1` -> production classifier MUST `STATIC_BOUND`；
- `every(orderDetailList, status = 1)` element `status` READ -> MUST `RUNTIME_OBJECT_BOUND`；
- source READ `*` expansion 必须包含 exact readable element member；禁止 parent-path fallback；
- unsupported dynamic selector -> `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED`。

`DynamicBindingClassifierStub` 仅可隔离下游 unit，不得证明 classifier correctness/AC-006。

## 2. Generic `ProtectedAccessResolutionContext` fixtures

提供 framework-owned execution frame fixtures：

- STATIC：C1 / frame FS / owner OrderInfo-A / no collection cursor / direct `status` target；
- RUNTIME：C1 / frame FR / owner OrderInfo-A / cursor element-A；
- same Context but owner OrderInfo-B / different collection cursor；
- prior Context C0；
- expired frame/cursor；
- context replacement/member removal before gateway execution。

Business test code 不得通过 public constructor/factory 创建 production context，也不获得 raw domain object getter。

## 3. STATIC_ALLOW Guard-path fixtures（FND-001）

Real source direct `status = 1` MUST produce `STATIC_BOUND -> STATIC_ALLOW` with：

- selected compiled rule `runtimeBindingPlan().isPresent() == false`；
- selected compiled rule `runtimeRequirement().isPresent() == false`；
- generic resolver creates one `ResolvedProtectedAccess` without RuntimeBindingPlan input；
- `ProtectedAccessGateway.execute(access)` invocation count = 1；
- `ModelAccessGuard.authorize(access)` invocation count = 1；
- Guard-owned exact PolicyIndex lookup count = 1；
- RuntimeBindingVerifier invocation count = 0；
- runtime evaluator submit count = 0；
- same hidden target protected operation count = 1；
- any public/supported caller-side direct STATIC_ALLOW protected executor is an architecture/API test failure。

Required bypass seam：attempt to invoke a protected read/write/execute without the generic resolver/gateway/Guard path must be impossible by supported API or trip `MODEL_ACCESS_GUARD_BYPASS` before operation。

## 4. Runtime operation-bound capability fixtures（FND-017/FND-019）

- generic resolver resolves actual element A and protected READ intent -> one-shot capability A；
- Guard exact lookup selects runtime-required rule and RuntimeBindingVerifier verifies A against exact plan；
- gateway executes A -> **A protected read count = 1**；
- lower-level invariant seam pairs A capability metadata with actual target B -> `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH` -> A/B operation count = 0 for rejected attempt；
- public/runtime API inspection must find no `execute(capability,target)`、`execute(handle,rawObject)` or caller-selected second-target callback；
- replay -> `RUNTIME_BINDING_CAPABILITY_CONSUMED`；
- remove/move A after resolve but before execute -> DENY before operation；
- concurrent execute(A) -> at most one terminal successful consumer。

## 5. Unified Guard branch oracle

```text
same generic capability/gateway/Guard entry
 -> Guard exact lookup once
 -> selected STATIC_ALLOW
      -> runtime verifier 0
      -> evaluator 0
      -> same bound target execute once
 OR selected RUNTIME_GUARD_REQUIRED
      -> exact plan/requirement present
      -> runtime verifier 1
      -> same bound target execute once only on proof match
```

This oracle prevents implementation from treating STATIC_ALLOW as a caller-side bypass and prevents runtime proof from becoming a prerequisite for static capability creation。

## 6. AC-006 end-to-end oracle

```text
real systems.xml + real rule-data IR
 -> exact read authorization
 -> production classifier
      direct status = STATIC_BOUND -> STATIC_ALLOW(no plan)
      every(orderDetailList,status) = RUNTIME_OBJECT_BOUND -> runtime rule+plan
 -> immutable Context
 -> framework generic ProtectedAccessResolutionContext
 -> ProtectedAccessResolver binds actual target + operation into ResolvedProtectedAccess
 -> ProtectedAccessGateway.execute
 -> ModelAccessGuard exact lookup once
      static: verifier/evaluator 0
      runtime: exact runtime proof verification
 -> framework executes same hidden target
 -> no static bypass and no proof-A/target-B substitution
```

## 7. Other seams/oracles

继续保留 deterministic System source provider、composite RuleView fixtures、wildcard exact expansion、exact PolicyIndex lookup-count spy、unavailable Guard sentinel、RuntimeFactValue immutability、bounded evaluator/fake time、protected operation probes。Compile/setup/missing-symbol failure 是 INVALID_RED；timeout oracle 禁止 `Thread.sleep`。
