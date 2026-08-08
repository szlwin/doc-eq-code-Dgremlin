# COMPILER P2 设计测试接缝

> Revision：`DESIGN-P2-R06`。正式 Test Design：`TESTDESIGN-P2-R07`。

## 1. Compile seams

- deterministic System source provider；
- duplicate System and composite RuleView fixtures；
- exact model-shape/path catalog；
- real `systems.xml` READ `path="*"` expansion fixture；
- declared/undeclared READ/WRITE/EXECUTE matrix；
- `DynamicBindingClassifierStub(STATIC_BOUND|RUNTIME_OBJECT_BOUND)`，用于证明 production compiler 能产生 `RUNTIME_GUARD_REQUIRED`；
- source fixture for dynamic container-element/object selection under an exact authorized path。

## 2. Runtime seams

- immutable Context A/B；
- exact PolicyIndex spy (lookup count must be one)；
- `RuntimeAccessBinding` allow/mismatch fixtures；
- Guard spy + unavailable sentinel；
- optional evaluator ALLOW/DENY/THROW/NULL/UNKNOWN/non-returning stubs；
- bounded executor/fake monotonic time source；
- Mutation/Read/Execute probes recording protected operation count, state version and external-effect count。

## 3. AC-006 end-to-end seam

Required oracle：

```text
source with legal dynamic object binding
 -> compiler succeeds
 -> selected rule = RUNTIME_GUARD_REQUIRED
 -> compiler-derived RuntimeAccessRequirement is traceable
 -> Context publishes
 -> runtime binding A matches -> ALLOW -> protected operation once
 -> runtime binding B escapes/mismatches -> DENY -> protected operation zero, side effects zero
```

A test starting by directly constructing a `CompiledModelAccessRule` is useful unit coverage but cannot satisfy this end-to-end acceptance by itself。

## 4. Oracle rules

- expected identity/path/operation comes from Requirement/BM/Design, never implementation output；
- compile/setup/missing-symbol failure is not a valid TDD RED；
- initial API-shape RED uses reflection/source/bytecode contract checks that compile before new symbols exist；
- runtime DENY asserts zero protected operation and zero external side effects；
- no `Thread.sleep` timeout oracle；
- wildcard never appears in runtime key lookup。
