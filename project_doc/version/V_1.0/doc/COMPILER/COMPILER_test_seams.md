# COMPILER P2 设计测试接缝

> Revision：`DESIGN-P2-R07`。正式 Test Design candidate：`TESTDESIGN-P2-R08`。

## 1. Production classifier fixtures

AC-006/classifier acceptance MUST 使用真实当前 grammar，不能使用 classifier stub：

- source：`dec-demo/src/main/resources/mix/system/systems.xml` -> `order` -> information `ordered` -> `rule-data`；
- direct access `status = 1` -> production classifier MUST return `STATIC_BOUND`；
- `every(orderDetailList, status = 1)` element `status` READ -> production classifier MUST return `RUNTIME_OBJECT_BOUND`；
- 对应真实 `read path="*"` expansion 必须包含 exact readable element member path，否则 compile ERROR，禁止 parent-path fallback；
- unsupported dynamic selector fixture -> `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED`。

`DynamicBindingClassifierStub` 仅允许用于 production classifier 已单独证明后的下游 Guard/plan unit isolation。

## 2. Runtime proof fixtures

- actual element A 来自当前 `OrderInfo.orderDetailList`，由 framework resolver 解析并签发 opaque handle A -> verify MATCH；
- element B 来自不同 OrderInfo/collection，但 request 的 static System/target/path/operation 相同 -> handle B -> verify DENY；
- stale prior-Context handle、replay 到另一 plan/rule、unknown/forged resolution id -> DENY；
- business test code 无 handle mint API，Guard API 不暴露 raw POJO。

## 3. AC-006 end-to-end oracle

```text
real systems.xml + real rule-data IR
 -> exact read authorization
 -> production classifier
      direct status = STATIC_BOUND
      every(orderDetailList,status) = RUNTIME_OBJECT_BOUND
 -> RuntimeBindingPlan + EXACT_RUNTIME_BINDING requirement
 -> Context publishes
 -> framework resolves element A and issues handle A -> Guard ALLOW -> protected read once
 -> foreign element B handle with same static tuple -> Guard DENY -> protected read zero / side effects zero
```

手工构造 `CompiledModelAccessRule`、classifier stub 或四字段 static binding object 均不能满足 AC-006。

## 4. Other seams/oracles

继续保留 deterministic System source provider、composite RuleView fixtures、wildcard exact expansion、exact PolicyIndex lookup-count spy、unavailable Guard sentinel、RuntimeFactValue immutability、bounded evaluator/fake time、protected operation probes。Compile/setup/missing-symbol failure 是 INVALID_RED；timeout oracle 禁止 `Thread.sleep`。