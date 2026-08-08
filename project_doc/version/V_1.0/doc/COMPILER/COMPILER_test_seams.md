# COMPILER P2 设计测试接缝

> Revision：`DESIGN-P2-R08`。正式 Test Design candidate：`TESTDESIGN-P2-R09`。

## 1. Production classifier fixtures

AC-006/classifier acceptance MUST 使用真实当前 grammar：

- source：`dec-demo/src/main/resources/mix/system/systems.xml` -> `order` -> information `ordered` -> `rule-data`；
- direct `status = 1` -> production classifier MUST `STATIC_BOUND`；
- `every(orderDetailList, status = 1)` element `status` READ -> MUST `RUNTIME_OBJECT_BOUND`；
- source READ `*` expansion 必须包含 exact readable element member；禁止 parent-path fallback；
- unsupported dynamic selector -> `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED`。

`DynamicBindingClassifierStub` 仅可隔离下游 unit，不得证明 classifier correctness/AC-006。

## 2. RuntimeResolutionContext fixtures

提供 framework-owned execution frame fixtures：

- C1 / frame F1 / owner OrderInfo-A / cursor element-A；
- 同 Context 但 owner OrderInfo-B / different collection cursor；
- prior Context C0；
- expired frame/cursor；
- context replacement/member removal before gateway execution。

Business test code 不得通过 public constructor/factory 创建 production `RuntimeResolutionContext`，也不获得 raw domain object getter。

## 3. Operation-bound capability fixtures（FND-019）

- resolver resolves actual element A and protected READ intent -> one-shot capability A；
- gateway executes A -> Guard ALLOW -> **A protected read count = 1**；
- lower-level invariant seam attempts to pair A proof/capability metadata with actual target B -> `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH` -> **A/B protected operation count = 0 for the rejected attempt**；
- supported public/runtime API inspection must find no `execute(capability,target)`、`execute(handle,rawObject)` or callback seam that allows caller-selected second protected target；
- capability A replay -> `RUNTIME_BINDING_CAPABILITY_CONSUMED`；
- remove/move A from planned collection after resolve but before execute -> stale/changed-membership DENY before operation；
- concurrent execute(A) attempts -> at most one terminal consumer；
- terminal DENY and successful execute both consume capability according to R08 contract。

## 4. AC-006 end-to-end oracle

```text
real systems.xml + real rule-data IR
 -> exact read authorization
 -> production classifier
      direct status = STATIC_BOUND
      every(orderDetailList,status) = RUNTIME_OBJECT_BOUND
 -> RuntimeBindingPlan + EXACT_RUNTIME_BINDING requirement
 -> Context publishes
 -> framework RuntimeResolutionContext for current every element A
 -> resolver binds actual A + READ intent into ResolvedProtectedAccess A
 -> ProtectedAccessGateway.execute(A)
      -> exact lookup once
      -> Guard verifies same capability
      -> framework executes same hidden A target
 -> A read once
 -> proof/capability A + target B substitution
      -> impossible by supported API or lower-level invariant DENY
      -> B operation 0 / state unchanged / effects 0
```

手工构造 compiled rule、classifier stub、四字段 binding object、detached valid handle 均不能满足 AC-006/FND-019。

## 5. Other seams/oracles

继续保留 deterministic System source provider、composite RuleView fixtures、wildcard exact expansion、exact PolicyIndex lookup-count spy、unavailable Guard sentinel、RuntimeFactValue immutability、bounded evaluator/fake time、protected operation probes。Compile/setup/missing-symbol failure 是 INVALID_RED；timeout oracle 禁止 `Thread.sleep`。
