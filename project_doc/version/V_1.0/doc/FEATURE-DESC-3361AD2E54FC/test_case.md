# FEATURE-DESC-3361AD2E54FC Test Design

> Revision：`TESTDESIGN-P2-R09`。
> Base：`TESTDESIGN-P2-R08`。
> Inputs：Requirement `REQAN-P2-R01`、Business Model candidate `BM-R11`、Design candidate `DESIGN-P2-R08`。
> Status：`NEEDS_CHANGES_CANDIDATE_FIXED / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED`。本文件是当前 canonical Test Design candidate；在 exact-revision Review、RC9 reopen/publish、machine-valid risk Evidence 完成前不得 PASSED。

## 1. Principles

1. Acceptance 从真实 source/public framework behavior 出发，不以 helper/stub 自证。
2. 所有 protected READ/WRITE/EXECUTE 均证明进入 Guard；`STATIC_ALLOW` 仅为 Guard 内 fast path。
3. Runtime lookup exact-only；真实 `read path="*"` 只允许 compile-time finite expansion。
4. AC-006 必须证明 real source/IR -> production classifier -> compiler-published plan/rule -> Context -> framework resolution -> **operation-bound capability** -> Guard -> same-target protected execution。
5. classifier stub 只能隔离下游 unit；detached handle verification 不能替代 final operation-target binding Evidence。
6. caller 不得 mint capability、提交 replacement rule/requirement/plan/target 或通过 raw POJO/callback 选择第二 protected target。
7. valid TDD RED 必须 testCompile 成功、目标 test 启动、因目标 behavior/contract assertion 失败；missing symbol/module/test/setup/compile failure 均 `INVALID_RED`。
8. Test Design 不创建 production skeleton/实现。

## 2. Formal Maven / valid RED

Bootstrap only：

```bash
./mvnw -pl <target-module> -am -Dmaven.test.skip=true install
```

Formal target test：

```bash
./mvnw -pl <target-module> -Dtest=<TestClass> -Dsurefire.failIfNoSpecifiedTests=true test
```

目标 test step 禁止 `-am`。新 API 尚不存在时首个 RED 使用 reflection/string/source/bytecode contract inspection；合法 TDD skeleton 建立后才允许直接 typed test。

## 3. Production classifier — FND-018 blocking

### CASE-P2-DYNAMIC-CLASSIFIER-REAL-FIXTURE-001-R09 — BLOCKING

Real fixture：`dec-demo/src/main/resources/mix/system/systems.xml`，`system=order`，`information=ordered`，现有：

```text
status = 1
and
every(orderDetailList, status = 1)
```

Required：

1. production parser/compiler 产生 resolved access-consumer IR；
2. direct `status = 1` -> `DIRECT_EXACT -> STATIC_BOUND`；
3. `every(orderDetailList,status = 1)` element `status` READ -> `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND`；
4. collection/element relative path exact-resolve；真实 READ `*` expansion 含 exact readable element member；禁止 parent fallback；
5. deterministic classification/plan key/digest under equivalent source order；
6. unsupported index/key/filter/find/selector -> `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED` compile ERROR；
7. `DynamicBindingClassifierStub` 不得满足本 Case。

## 4. Runtime object proof — FND-017 regression

### CASE-P2-RUNTIME-BINDING-PROOF-001-R09 — BLOCKING

Precondition：production compiler 产生 element `status` exact `RUNTIME_GUARD_REQUIRED` rule + `RuntimeBindingPlan(COLLECTION_ELEMENT_MEMBERSHIP)` + `EXACT_RUNTIME_BINDING` requirement。

Required：

1. immutable EngineContext C1 publish；
2. framework execution frame 创建 production `RuntimeResolutionContext`；business caller 无 production factory；
3. resolver 实际选择 element A 并形成 framework proof/provenance；
4. foreign element B from different OrderInfo/collection under same static request tuple cannot satisfy A membership；
5. stale Context、wrong plan/rule、unknown/forged proof DENY；
6. no raw POJO/object reference exposed by Guard/capability public contract。

该 Case 证明 membership proof；最终 operation substitution 由下一 Case 单独阻断。

## 5. Operation substitution / TOCTOU — FND-019 blocking

### CASE-P2-RUNTIME-BINDING-OPERATION-SUBSTITUTION-001-R09 — BLOCKING

Purpose：证明 Guard 验证的实际 runtime object 与最终 protected operation target 是同一个不可替换 framework binding，而不是“证明 A 合法后去操作 B”。

Setup：

- same EngineContext C1；
- same System/target/exact path/READ operation；
- element A belongs to OrderInfo-A.orderDetailList；
- element B is a different element from OrderInfo-B or another collection while preserving the same static authorization tuple；
- resolver resolves A under current frame/cursor and produces one-shot `ResolvedProtectedAccess A`。

Positive oracle：

```text
ResolvedProtectedAccess A
 -> ProtectedAccessGateway.execute(A)
 -> exact PolicyIndex lookup = 1
 -> Guard verifies same capability A
 -> gateway executes internally-bound actual target A
 -> A protected read count = 1
 -> B protected read count = 0
 -> capability A consumed
```

Substitution negative oracle：

```text
valid proof/capability A
 + attempt actual protected operation on B
 => supported API construction is impossible
 OR lower-level invariant seam detects executorTarget != capabilityTarget
 => DENY / RUNTIME_BINDING_OPERATION_TARGET_MISMATCH
 => B protected operation count = 0
 => A protected operation count = 0 for rejected attempt
 => state version unchanged
 => external side effect count = 0
```

Required API-shape assertions：

- no supported `execute(capability, target)`；
- no supported `execute(handle, rawObject)`；
- no caller callback/closure API that receives authorization for A but can select arbitrary protected target B；
- `ResolvedProtectedAccess` has no public/protected constructor/factory and no raw target getter；
- operation intent is bound when resolver creates capability and is not caller-replaceable after Guard verification。

TOCTOU/replay assertions：

1. move/remove A from planned collection after resolve but before execute -> DENY before operation；
2. invalidate frame/cursor or replace Context -> stale DENY；
3. second execute(A) -> `RUNTIME_BINDING_CAPABILITY_CONSUMED`；
4. two concurrent execute(A) attempts -> at most one terminal successful consumer；
5. membership/provenance revalidation occurs immediately before operation inside gateway execution boundary。

## 6. Full AC-006 source-to-operation chain

### CASE-P2-DYNAMIC-SOURCE-TO-OPERATION-001-R09 — BLOCKING

```text
real systems.xml / order.ordered
 -> exact static READ authorization
 -> production classifier
      direct status -> STATIC_BOUND
      every(orderDetailList,status) -> RUNTIME_OBJECT_BOUND
 -> RuntimeBindingPlan + EXACT_RUNTIME_BINDING
 -> exact RUNTIME_GUARD_REQUIRED CompiledModelAccessRule
 -> semantic digest includes plan/requirement identity
 -> immutable Context publishes
 -> framework RuntimeResolutionContext for actual element A
 -> resolver binds A + READ intent into one-shot ResolvedProtectedAccess A
 -> ProtectedAccessGateway.execute(A)
      -> Guard verifies A
      -> same internally-bound A is read exactly once
 -> proof/capability A cannot authorize B
 -> DENY paths perform zero protected operation / zero external effects
```

Manual compiled rule、classifier stub、detached handle-only test、four-field static binding object 均不能关闭 AC-006。

## 7. Existing acceptance matrix carried forward

### AC-001 — CASE-P2-SYSTEM-DETERMINISM-001-R09
Real systems.xml + reordered/multi-source equivalent -> same SystemKey set/order/digest；duplicate/conflict -> stable ERROR/no partial publication。

### AC-002 — CASE-P2-RULEVIEW-COMPOSITE-001-R09
Cross-System same name legal；same-System duplicate fails；missing System -> `MIX-RULEVIEW-SYSTEM-REQUIRED`；no bare-name registration fallback。

### AC-003 — CASE-P2-RULEVIEW-CALL-001-R09
Only exact `(SystemKey,name)` resolves；wrong/bare lookup fails without cross-System fallback。

### AC-004 — CASE-P2-ACCESS-MATRIX-001-R09
READ/WRITE/EXECUTE independent；undeclared denied；shared WRITE denied unless explicit；DENY zero operation/effects。

### AC-005 — CASE-P2-MODEL-PATH-001-R09
Same logical path -> same canonical identity；unknown/non-composite/target mismatch compile ERROR；no fuzzy/prefix/suffix/cross-target lookup。

### AC-007 — CASE-P2-GUARD-NO-BYPASS-001-R09
Rule/change/custom action/protected query-read all enter Guard。STATIC_ALLOW => Guard entry 1/evaluator 0。Runtime-bound path must enter gateway+Guard and cannot detach ALLOW from target execution。

### AC-008 — CASE-P2-CONTEXT-ATOMICITY-001-R09
Successful candidate publishes whole closure；failed candidate preserves old Context；contexts do not share mutable registry/policy/current/resolution registry。

### AC-009 — CASE-P2-DIAGNOSTIC-001-R09
Stable reasons include duplicate/missing-owner/invalid path/static denial/unsupported dynamic/proof invalid/stale/plan mismatch/operation-target mismatch/capability-consumed。

### AC-010 — CASE-P2-DECLARATION-BOUNDARY-001-R09
Retired module not restored；surviving compatibility read-only；no second registry/runtime；final removal P7。

## 8. Regressions for earlier Findings

### CASE-P2-SELECTED-RULE-001-R09
PolicyIndex exact lookup count=1；no replacement rule/requirement/plan；gateway and Guard use same selected rule。

### CASE-P2-SYSTEMS-WILDCARD-READ-001-R09
`order/OrderInfo` and `payment/OrderInfo` READ `*` -> finite sorted/deduplicated exact READ rules；runtime zero wildcard keys；wildcard WRITE/EXECUTE/empty fail；shape change changes digest/recompile。

### CASE-P2-RUNTIME-FACT-VALUE-001-R09
public final/private construction/six typed factories/deep immutable/exhaustive visitor/no generic mutable payload/deterministic canonical form。

### CASE-P2-RUNTIME-REQUIREMENT-MODULE-BOUNDARY-001-R09
Context owns immutable compiled factories callable by compiler without split/reverse dependency；factory visibility grants no runtime authority。

### CASE-P2-RUNTIME-RESOLUTION-CONTEXT-001-R09
Production `RuntimeResolutionContext` is framework-created and frame-scoped；no business factory/raw object getter；cross-context/frame/cursor reuse invalid。

## 9. Fail-closed matrix

| Condition | Expected |
|---|---|
| policy missing | DENY / POLICY_NOT_FOUND |
| Context mismatch | DENY / CONTEXT_IDENTITY_MISMATCH |
| runtime plan/requirement missing | DENY / invalid compiled state |
| runtime capability/proof missing | DENY / RUNTIME_BINDING_REQUIRED |
| foreign/forged proof | DENY / RUNTIME_BINDING_PROOF_INVALID |
| stale Context/frame/cursor/membership | DENY / RUNTIME_BINDING_STALE |
| rule/plan replay mismatch | DENY / RUNTIME_BINDING_PLAN_MISMATCH |
| capability target != executor target | DENY / RUNTIME_BINDING_OPERATION_TARGET_MISMATCH |
| consumed/replayed capability | DENY / RUNTIME_BINDING_CAPABILITY_CONSUMED |
| Guard unavailable | DENY / GUARD_UNAVAILABLE |
| future evaluator unavailable/exception/null/timeout/unknown | DENY / stable evaluator reason |
| STATIC_ALLOW | Guard entry 1, evaluator submit 0, ALLOW |
| valid runtime capability A | ALLOW only inside gateway; same bound A executes |

Every DENY -> protected operation count 0 + external effect count 0。Timeout test uses controlled Future/fake monotonic time；no `Thread.sleep` oracle。

## 10. Java 8 / EngineContext compatibility

### CASE-P2-JAVA8-ENGINE-CONTEXT-001-R09
Production API compiles release 8；no record/Java9 collection factories；EngineContext remains final/existing accessors callable；P2 additive only；no bare-name RuleView API。

## 11. TDD RED examples

Before symbols：reflection/string/source/bytecode shape assertion that itself compiles。After legal skeleton：direct typed behavior tests。Evidence records command、target test、intended failing oracle、actual failure category。

## 12. Traceability

| Acceptance/Finding | Blocking evidence |
|---|---|
| AC-006 / FND-014 / FND-016 / FND-018 | CASE-P2-DYNAMIC-CLASSIFIER-REAL-FIXTURE-001-R09 + CASE-P2-DYNAMIC-SOURCE-TO-OPERATION-001-R09 |
| AC-006 / FND-017 | CASE-P2-RUNTIME-BINDING-PROOF-001-R09 |
| AC-006 / FND-019 | CASE-P2-RUNTIME-BINDING-OPERATION-SUBSTITUTION-001-R09 |
| FND-004 | CASE-P2-RUNTIME-RESOLUTION-CONTEXT-001-R09 + operation-bound gateway API-shape assertions |
| FND-009 | CASE-P2-SELECTED-RULE-001-R09 |
| FND-010 | CASE-P2-SYSTEMS-WILDCARD-READ-001-R09 |
| FND-011 | CASE-P2-RUNTIME-FACT-VALUE-001-R09 |
| FND-012 | §2 / §11 valid RED contract |
| FND-015 | CASE-P2-RUNTIME-REQUIREMENT-MODULE-BOUNDARY-001-R09 |
| FND-008 | CASE-P2-JAVA8-ENGINE-CONTEXT-001-R09 |

## 13. Review / phase gate

`TESTDESIGN-P2-R09` cannot pass before exact `DESIGN-P2-R08` passes and RC9 machine lifecycle binds current revisions。Exact Test Design Review still requires current-contract Requirement/Design/TDD/TestEvidence independent reviewers。Implementation Plan / TDD / Development remain BLOCKED while any effective P0/P1 is open。
