# FEATURE-DESC-3361AD2E54FC Test Design

> Revision：`TESTDESIGN-P2-R08`。
> Base：`TESTDESIGN-P2-R07`。
> Inputs：Requirement `REQAN-P2-R01`、Business Model candidate `BM-R10`、Design candidate `DESIGN-P2-R07`。
> Status：`NEEDS_CHANGES_CANDIDATE_FIXED / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED`。本文件是当前 canonical Test Design candidate；在 exact-revision Review、RC9 reopen/publish、machine-valid risk Evidence 完成前不得 PASSED。

## 1. Test Design principles

1. Acceptance 从真实 source/public behavior 出发，不以 helper/stub 自证。
2. 所有 protected READ/WRITE/EXECUTE 均证明进入 Guard；`STATIC_ALLOW` 仅为 Guard 内 fast path。
3. Runtime lookup exact-only；真实 `read path="*"` 只允许 compile-time finite expansion。
4. AC-006 必须证明真实 source/IR -> production classifier -> compiler-published plan/rule -> Context -> framework resolver -> opaque binding handle -> Guard ALLOW/DENY。
5. classifier stub 只能隔离下游 unit，不得作为 production classifier 或 AC-006 Evidence。
6. caller 不得 mint binding handle、提交 replacement rule/requirement/plan 或通过 raw POJO/boolean 声明“在边界内”。
7. valid TDD RED 必须 testCompile 成功、目标 test 启动、因目标行为/contract assertion 失败；missing symbol/module/test/setup/compile failure 均为 `INVALID_RED`。
8. Test Design 不创建生产 skeleton/实现。

## 2. Formal Maven / valid-RED contract

Bootstrap only:

```bash
./mvnw -pl <target-module> -am -Dmaven.test.skip=true install
```

Formal target test:

```bash
./mvnw -pl <target-module> -Dtest=<TestClass> -Dsurefire.failIfNoSpecifiedTests=true test
```

目标 test step 禁止 `-am`。新 API 尚不存在时，首个 RED 用 reflection/string/source/bytecode contract inspection；合法 TDD skeleton 建立后才允许直接 typed test。

## 3. AC-006 production classifier — FND-018 blocking case

### CASE-P2-DYNAMIC-CLASSIFIER-REAL-FIXTURE-001-R08 — BLOCKING

Real fixture 固定为：

`dec-demo/src/main/resources/mix/system/systems.xml`

Use `system=order`, `information=ordered`, existing `rule-data`:

```text
status = 1
and
every(orderDetailList, status = 1)
```

Required production-classifier assertions:

1. compiler 解析真实 source 到 resolved access-consumer IR；
2. direct `status = 1` access node 是 `DIRECT_EXACT`，MUST classify `STATIC_BOUND`；
3. `every(orderDetailList, status = 1)` 是当前冻结的 `EVERY_COLLECTION_ELEMENT` IR；
4. 其 element `status` READ MUST classify `RUNTIME_OBJECT_BOUND`；
5. target/model-shape 必须证明 `orderDetailList` 是 collection，并 exact resolve element-relative `status`；
6. 真实 `read path="*"` expansion 必须包含该 exact readable element-member rule；parent-path permission fallback 禁止；
7. output 含稳定 reason 与 deterministic `RuntimeBindingPlan(COLLECTION_ELEMENT_MEMBERSHIP)`；
8. source reorder/canonical-equivalent input 得到同 classification/plan key/digest；
9. unsupported runtime index/key/filter/find selector fixture -> `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED` compile ERROR。

`DynamicBindingClassifierStub(STATIC_BOUND|RUNTIME_OBJECT_BOUND)` 明确不能满足本 Case。

## 4. AC-006 runtime object proof — FND-017 blocking case

### CASE-P2-RUNTIME-BINDING-PROOF-001-R08 — BLOCKING

Precondition：上一 Case 已产生 element `status` 的 exact selected READ rule，status=`RUNTIME_GUARD_REQUIRED`，并包含 `RuntimeAccessRequirement(EXACT_RUNTIME_BINDING)` + one `RuntimeBindingPlan`。

Required runtime setup/oracles:

1. candidate 发布到 immutable EngineContext C1；
2. framework resolver 从 C1 当前 `OrderInfo.orderDetailList` 解析实际 element A 并签发 opaque handle A；
3. handle A 绑定 C1 + exact selected rule + exact plan + resolver-owned membership/provenance；business code 无 mint API；
4. Guard exact PolicyIndex lookup count = 1；
5. Guard verify A -> ALLOW；protected read exactly once；
6. 从**不同 OrderInfo 或不同 collection**解析 element B，同时 request static System/target/path/operation 保持与 A 相同；
7. handle B 因实际 membership/provenance 不同 MUST DENY；
8. B DENY -> protected read 0、state version unchanged、external-effect count 0；
9. old Context C0 handle used in C1 -> `RUNTIME_BINDING_STALE` 或等价稳定 DENY；
10. replay A against another plan/rule -> `RUNTIME_BINDING_PLAN_MISMATCH`；
11. unknown/forged resolution id -> `RUNTIME_BINDING_PROOF_INVALID`；
12. Guard API/test observation 暴露 raw POJO/object reference = 0；
13. caller-created `{context,target,path,operation}` 四字段对象不能满足 requirement，且不属于 R07 API。

该 Case 证明同一静态 tuple 下两个 runtime object 可以产生不同 decision，而无需 source-authored business predicate DSL。

## 5. Source -> Compiler -> Context -> Guard AC-006 chain

### CASE-P2-DYNAMIC-SOURCE-TO-GUARD-001-R08 — BLOCKING

```text
real systems.xml / order.ordered rule-data
 -> compiler exact static READ authorization
 -> production classifier
      direct status -> STATIC_BOUND
      every(orderDetailList,status) -> RUNTIME_OBJECT_BOUND
 -> RuntimeBindingPlan + EXACT_RUNTIME_BINDING requirement
 -> exact CompiledModelAccessRule = RUNTIME_GUARD_REQUIRED
 -> semantic digest includes plan/requirement identity
 -> immutable EngineContext publishes
 -> framework resolves actual element A -> opaque handle A -> Guard ALLOW -> protected read once
 -> foreign element B under same static tuple -> opaque handle B -> Guard DENY -> protected read zero / effects zero
```

手工 new compiled rule、classifier stub、四字段 binding object 均不能关闭 AC-006。

## 6. Existing acceptance matrix carried forward

### AC-001 System determinism — CASE-P2-SYSTEM-DETERMINISM-001-R08

Real systems.xml + reordered/multi-source equivalent input -> identical SystemKey set/order/digest；duplicate/conflicting System -> stable ERROR and no partial publication。

### AC-002 RuleView identity — CASE-P2-RULEVIEW-COMPOSITE-001-R08

Cross-System same name legal；same-System duplicate fails；missing System -> `MIX-RULEVIEW-SYSTEM-REQUIRED`；no bare-name registration fallback。

### AC-003 RuleView call — CASE-P2-RULEVIEW-CALL-001-R08

Only exact `(SystemKey,name)` resolves；wrong/bare name fails without cross-System fallback。

### AC-004 permission matrix — CASE-P2-ACCESS-MATRIX-001-R08

READ/WRITE/EXECUTE independent；undeclared denied；shared WRITE denied unless explicit；every DENY executes zero protected operation/effects。

### AC-005 ModelPath — CASE-P2-MODEL-PATH-001-R08

Same logical path across consumers -> same canonical identity；unknown/non-composite/target mismatch compile ERROR；no fuzzy/prefix/suffix/cross-target lookup。

### AC-007 Guard no-bypass — CASE-P2-GUARD-NO-BYPASS-001-R08

Rule/change/custom action/protected query-read all enter Guard。STATIC_ALLOW => Guard entry 1, evaluator 0。DENY precedes protected read/write/execute。

### AC-008 Context atomicity — CASE-P2-CONTEXT-ATOMICITY-001-R08

Successful candidate publishes whole closure；failed candidate preserves old Context；contexts do not share mutable registry/policy/current。

### AC-009 diagnostics — CASE-P2-DIAGNOSTIC-001-R08

Stable code/order/source for duplicate System、missing owner、invalid path、static denial、unsupported dynamic binding、proof invalid/stale/plan mismatch。

### AC-010 declaration boundary — CASE-P2-DECLARATION-BOUNDARY-001-R08

Retired module not restored；surviving compatibility read-only；no second registry/runtime；final removal remains P7。

## 7. Selected-rule / wildcard / immutable-value regressions

### CASE-P2-SELECTED-RULE-001-R08

PolicyIndex exact lookup count=1；request cannot replace rule/requirement/plan；verifier uses exact selected rule；mismatch fails before operation。

### CASE-P2-SYSTEMS-WILDCARD-READ-001-R08

`order/OrderInfo` and `payment/OrderInfo` READ `*` expand only against exact target catalogs into finite sorted/deduplicated exact READ rules；runtime has zero wildcard keys；wildcard WRITE/EXECUTE/empty expansion fail；shape change changes digest/requires recompile。

### CASE-P2-RUNTIME-FACT-VALUE-001-R08

public final；constructor externally inaccessible；six typed factories；deep immutable LIST/OBJECT；exhaustive visitor；no generic mutable payload；deterministic canonical form。

## 8. Cross-module construction — FND-015

### CASE-P2-RUNTIME-REQUIREMENT-MODULE-BOUNDARY-001-R08

Context owns immutable `RuntimeBindingPlan`/`RuntimeAccessRequirement` factories callable by compiler without split package/reverse dependency。Factory visibility grants no runtime authority。`RuntimeBindingHandle` has no public mint factory。

## 9. Fail-closed matrix

| Condition | Expected |
|---|---|
| policy missing | DENY / POLICY_NOT_FOUND |
| Context mismatch | DENY / CONTEXT_IDENTITY_MISMATCH |
| runtime plan/requirement missing in runtime rule | DENY / invalid compiled state |
| runtime handle missing | DENY / RUNTIME_BINDING_REQUIRED |
| foreign/forged handle | DENY / RUNTIME_BINDING_PROOF_INVALID |
| stale Context handle | DENY / RUNTIME_BINDING_STALE |
| rule/plan replay mismatch | DENY / RUNTIME_BINDING_PLAN_MISMATCH |
| Guard unavailable | DENY / GUARD_UNAVAILABLE |
| future evaluator unavailable/exception/null/timeout/unknown | DENY / stable evaluator reason |
| STATIC_ALLOW | Guard entry 1, evaluator submit 0, ALLOW |
| valid runtime member handle | ALLOW only after exact rule+plan verification |

Every DENY -> protected operation count 0 + external effects 0。Timeout test 使用 controlled Future/fake monotonic time，禁止 `Thread.sleep` 作为 oracle。

## 10. Java 8 / EngineContext compatibility

### CASE-P2-JAVA8-ENGINE-CONTEXT-001-R08

Production API compiles with release 8；no record/Java9 collection factories；EngineContext remains final；existing constructor/core accessors callable；P2 additive only；no new bare-name RuleView API。

## 11. TDD RED examples

Before symbols exist：reflection/string/source/bytecode shape assertions that compile。After legal skeleton：direct typed behavior tests。Evidence 记录 command、target test、intended failing oracle、actual failure category。

## 12. Traceability

| Acceptance/Finding | Blocking evidence |
|---|---|
| AC-006 / FND-014 / FND-016 / FND-018 | CASE-P2-DYNAMIC-CLASSIFIER-REAL-FIXTURE-001-R08 + CASE-P2-DYNAMIC-SOURCE-TO-GUARD-001-R08 |
| AC-006 / FND-017 | CASE-P2-RUNTIME-BINDING-PROOF-001-R08 |
| FND-009 | CASE-P2-SELECTED-RULE-001-R08 |
| FND-010 | CASE-P2-SYSTEMS-WILDCARD-READ-001-R08 |
| FND-011 | CASE-P2-RUNTIME-FACT-VALUE-001-R08 |
| FND-012 | §2 / §11 valid RED contract |
| FND-015 | CASE-P2-RUNTIME-REQUIREMENT-MODULE-BOUNDARY-001-R08 |
| FND-008 | CASE-P2-JAVA8-ENGINE-CONTEXT-001-R08 |

## 13. Review and phase gate

`TESTDESIGN-P2-R08` cannot pass before exact `DESIGN-P2-R07` passes and RC9 machine lifecycle binds current revisions。Exact Test Design Review still requires current-contract independent Requirement/Design/TDD/TestEvidence reviewers。Implementation Plan / TDD / Development remain BLOCKED while any effective P0/P1 is open。