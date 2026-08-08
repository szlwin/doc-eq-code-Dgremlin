# COMPILER P2 架构增量

> Revision：`DESIGN-P2-R07`。状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。

## 1. Dependency direction

```text
dec-core-context        <- neutral immutable facts, Guard, RuntimeBindingPlan/Handle contracts
       ^
       |
dec-core-compiler       <- access IR, production DynamicBindingClassifier, plan publication
       ^
       |
frontends / starter / execution consumers
```

禁止 context -> compiler、compiler -> concrete parser、split package、global current Context。

## 2. Compile-time classification authority

`DynamicBindingClassifier` 是 production compiler logic。它在 exact target/path/static authorization 完成后消费 resolved access-consumer IR。R07 当前只冻结：

- `DIRECT_EXACT -> STATIC_BOUND`；
- current grammar `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND`；
- 其它未冻结 dynamic IR -> compile ERROR `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED`。

Test stub 只可用于隔离下游 unit，不能证明 production classifier correctness。

## 3. Runtime binding authority

Compiler 发布 immutable `RuntimeBindingPlan(COLLECTION_ELEMENT_MEMBERSHIP)` 并把 plan key 绑定到 exact selected rule / `RuntimeAccessRequirement(EXACT_RUNTIME_BINDING)`。

Framework-owned resolver 在真实 collection element 被解析时签发 opaque `RuntimeBindingHandle`。Handle 无 public mint API；resolver/verifier 内部可保存 object/collection-owner identity 与 provenance，但 Guard API 不暴露 raw POJO。

Handle 仅在 current EngineContext + exact selected rule + exact plan + actual membership 全部匹配时有效。来自另一 collection/OrderInfo/context/plan/rule 的 handle 即使静态 request tuple 完全相同也 DENY。

## 4. Publication closure

System、RuleView、exact ModelPath rule、`DynamicBindingClassification`、`RuntimeBindingPlan`、`RuntimeAccessRequirement`、Diagnostic、digest、PolicyIndex 属于同一 immutable `CompiledModelSet` closure。Plan key/model-shape digest 必须进入 semantic digest。

## 5. Protected operation integration

固定顺序：framework resolver 解析实际对象并签发 handle -> Guard exact lookup/verification -> 只有 ALLOW 执行 protected operation。Business caller 不得提交 `withinBoundary=true`、raw object、replacement rule/requirement/plan 或 caller-minted proof。

## 6. Compatibility / concurrency

现有 final EngineContext/P1 API 保持兼容。R04 bounded evaluator executor 仅保留给未来 Requirement-authorized predicate extension；当前 binding verification 同步、纯验证、fail-closed。Legacy Config/RuleView compatibility 到 P7 仍只读。