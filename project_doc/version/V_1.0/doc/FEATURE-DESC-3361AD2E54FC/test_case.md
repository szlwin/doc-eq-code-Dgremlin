# FEATURE-DESC-3361AD2E54FC Test Design

> Revision：`TESTDESIGN-P2-R10`。
> Base：`TESTDESIGN-P2-R09`。
> Inputs：Requirement `REQAN-P2-R01`、Business Model candidate `BM-R12`、Design candidate `DESIGN-P2-R09`。
> Status：`NEEDS_CHANGES_CANDIDATE_FIXED / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED`。本文件是当前 canonical Test Design candidate；在 exact-revision Review、RC9 reopen/publish、machine-valid risk Evidence 完成前不得 PASSED。

## 1. Principles

1. Acceptance 从真实 source/public framework behavior 出发，不以 helper/stub 自证。
2. **所有 protected READ/WRITE/EXECUTE，包括 STATIC_ALLOW，都必须证明进入同一个 Gateway -> Guard 路径。**
3. `STATIC_ALLOW` 只能是 Guard exact rule lookup 后的内部 fast path；不得伪造 RuntimeBindingPlan，也不得 caller-side bypass。
4. Runtime lookup exact-only；真实 `read path="*"` 只允许 compile-time finite expansion。
5. AC-006 必须证明 real source/IR -> production classifier -> compiler-published rule/plan -> Context -> generic resolution capability -> Guard -> same-target operation。
6. classifier stub 只能隔离下游 unit；detached handle/proof verification不能替代 final same-target execution Evidence。
7. caller 不得 mint capability、提交 replacement rule/plan/target 或通过 raw POJO/callback 选择第二 protected target。
8. valid TDD RED 必须 testCompile 成功、目标 test 启动、因目标 behavior/contract assertion 失败；missing symbol/module/test/setup/compile failure 均 `INVALID_RED`。
9. Test Design 不创建 production skeleton/实现。

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

### CASE-P2-DYNAMIC-CLASSIFIER-REAL-FIXTURE-001-R10 — BLOCKING

Real fixture：`dec-demo/src/main/resources/mix/system/systems.xml`，`system=order`，`information=ordered`：

```text
status = 1
and
every(orderDetailList, status = 1)
```

Required：

1. production parser/compiler produces resolved access-consumer IR；
2. direct `status = 1` -> `DIRECT_EXACT -> STATIC_BOUND`；
3. `every(orderDetailList,status = 1)` element `status` READ -> `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND`；
4. collection/element relative path exact-resolve；真实 READ `*` expansion 含 exact readable member；禁止 parent fallback；
5. deterministic classification/plan key/digest under equivalent source order；
6. unsupported index/key/filter/find/selector -> `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED` compile ERROR；
7. `DynamicBindingClassifierStub` 不得满足本 Case。

## 4. STATIC_ALLOW unified Guard path — FND-001 blocking

### CASE-P2-STATIC-ALLOW-GUARD-PATH-001-R10 — BLOCKING

Purpose：证明 STATIC_ALLOW 不依赖 RuntimeBindingPlan，同时也不能成为 caller-side Guard bypass。

Fixture：同一真实 `systems.xml / order.ordered` 的 direct `status = 1` protected READ。

Compile oracles：

1. production classifier -> `STATIC_BOUND`；
2. exact compiled rule -> `status=STATIC_ALLOW`；
3. `runtimeBindingPlan().isPresent() == false`；
4. `runtimeRequirement().isPresent() == false`；
5. semantic digest 不包含伪造 runtime-plan identity。

Runtime positive oracle：

```text
framework ProtectedAccessResolutionContext for direct status target A
 -> ProtectedAccessResolver.resolve(context, READ intent)
 -> legal generic ResolvedProtectedAccess A created with no RuntimeBindingPlan input
 -> ProtectedAccessGateway.execute(A) = 1
 -> ModelAccessGuard.authorize(A) = 1
 -> Guard exact PolicyIndex lookup = 1
 -> selected rule = STATIC_ALLOW
 -> RuntimeBindingVerifier = 0
 -> runtime evaluator submit = 0
 -> same hidden target A protected READ = 1
 -> capability consumed
```

Runtime negative/bypass oracle：

- no supported caller-side API can perform the same protected READ directly after observing `STATIC_ALLOW`；
- no `executeStatic(target)` / `if STATIC_ALLOW then directOperation` privileged path outside Gateway/Guard；
- attempting a lower-level direct protected operation without a valid generic capability/gateway/Guard path must fail architecture/API inspection or return `MODEL_ACCESS_GUARD_BYPASS` before operation；
- bypass attempt protected operation count = 0, state unchanged, external effects = 0。

This Case is mandatory for FND-001 closure；a test that only asserts `STATIC_ALLOW` exists is insufficient。

## 5. Runtime object proof — FND-017 regression

### CASE-P2-RUNTIME-BINDING-PROOF-001-R10 — BLOCKING

Precondition：production compiler creates element `status` exact `RUNTIME_GUARD_REQUIRED` rule + `RuntimeBindingPlan(COLLECTION_ELEMENT_MEMBERSHIP)` + `EXACT_RUNTIME_BINDING` requirement。

Required：

1. immutable EngineContext C1 publish；
2. framework generic `ProtectedAccessResolutionContext` uses current every element frame/cursor；
3. resolver binds actual element A into generic capability A without caller-provided plan/proof；
4. Guard exact lookup selects runtime rule and RuntimeBindingVerifier verifies A against exact compiler plan；
5. foreign element B from another OrderInfo/collection under same static tuple cannot satisfy A membership；
6. stale Context、wrong plan/rule、unknown/forged provenance DENY；
7. no raw POJO/object reference exposed by Guard/capability public contract。

## 6. Operation substitution / TOCTOU — FND-019 blocking

### CASE-P2-RUNTIME-BINDING-OPERATION-SUBSTITUTION-001-R10 — BLOCKING

Positive：

```text
ResolvedProtectedAccess A
 -> Gateway.execute(A)
 -> Guard verifies A
 -> same internally-bound A executes once
 -> B executes zero
```

Substitution negative：

```text
valid capability/proof A + attempted actual target B
 => supported API construction impossible
 OR lower-level invariant detects executorTarget != capabilityTarget
 => DENY / RUNTIME_BINDING_OPERATION_TARGET_MISMATCH
 => A/B operation count = 0 for rejected attempt
 => state unchanged
 => external effects = 0
```

API-shape assertions：no `execute(capability,target)`、no `execute(handle,rawObject)`、no caller callback selecting B、no public capability mint/factory/raw-target getter。

TOCTOU/replay：member removal/move after resolve -> DENY；frame/cursor/Context invalidation -> DENY；second execute -> `RUNTIME_BINDING_CAPABILITY_CONSUMED`；concurrent execute -> at most one terminal success。

## 7. Unified static/runtime branch contract

### CASE-P2-UNIFIED-PROTECTED-ACCESS-BRANCH-001-R10 — BLOCKING

Using one Gateway/Guard spy harness：

| Branch | Gateway | Guard | Policy lookup | Runtime verifier | Evaluator | Protected operation |
|---|---:|---:|---:|---:|---:|---:|
| STATIC_ALLOW | 1 | 1 | 1 | 0 | 0 | 1 on same bound target |
| RUNTIME_GUARD_REQUIRED + valid proof | 1 | 1 | 1 | 1 | 0 for current AC-006 | 1 on same bound target |
| RUNTIME_GUARD_REQUIRED + invalid proof | 1 | 1 | 1 | 1 | 0 | 0 |

This matrix prevents both static Guard bypass and unnecessary runtime-plan/evaluator coupling。

## 8. Full AC-006 source-to-operation chain

### CASE-P2-DYNAMIC-SOURCE-TO-OPERATION-001-R10 — BLOCKING

```text
real systems.xml / order.ordered
 -> exact static READ authorization
 -> production classifier
      direct status -> STATIC_BOUND -> STATIC_ALLOW(no plan)
      every(orderDetailList,status) -> RUNTIME_OBJECT_BOUND -> runtime rule+plan
 -> immutable Context publishes
 -> generic ProtectedAccessResolutionContext
 -> ProtectedAccessResolver binds actual target + exact READ intent
 -> ProtectedAccessGateway.execute
 -> ModelAccessGuard exact lookup once
      static branch: verifier/evaluator 0
      runtime branch: exact runtime plan/proof verification
 -> same internally-bound target is read once
 -> no static bypass
 -> proof/capability A cannot authorize B
 -> every DENY zero operation / zero external effects
```

Manual compiled rule、classifier stub、detached handle-only test、four-field static binding object or caller-side static fast path cannot close AC-006/FND-001。

## 9. Existing acceptance matrix carried forward

- **AC-001 / CASE-P2-SYSTEM-DETERMINISM-001-R10**：reordered/multi-source equivalent input -> same SystemKey/order/digest；duplicate/conflict -> stable ERROR/no partial publish。
- **AC-002 / CASE-P2-RULEVIEW-COMPOSITE-001-R10**：cross-System same name legal；same-System duplicate fails；missing System -> `MIX-RULEVIEW-SYSTEM-REQUIRED`；no bare-name fallback。
- **AC-003 / CASE-P2-RULEVIEW-CALL-001-R10**：only exact `(SystemKey,name)` resolves。
- **AC-004 / CASE-P2-ACCESS-MATRIX-001-R10**：READ/WRITE/EXECUTE independent；undeclared denied；shared WRITE denied unless explicit；all operations through Gateway/Guard。
- **AC-005 / CASE-P2-MODEL-PATH-001-R10**：same logical path -> same canonical identity；invalid path compile ERROR；no fuzzy/prefix/suffix/cross-target lookup。
- **AC-007 / CASE-P2-GUARD-NO-BYPASS-001-R10**：Rule/change/custom action/protected query-read all use generic capability/Gateway/Guard；STATIC fast path remains internal。
- **AC-008 / CASE-P2-CONTEXT-ATOMICITY-001-R10**：whole closure publish；failed candidate preserves old Context；no shared mutable policy/resolution global current。
- **AC-009 / CASE-P2-DIAGNOSTIC-001-R10**：stable diagnostic/runtime reason ordering including `MODEL_ACCESS_GUARD_BYPASS`、proof/stale/plan/substitution/consumed capability。
- **AC-010 / CASE-P2-DECLARATION-BOUNDARY-001-R10**：retired module not restored；legacy compatibility read-only；no second runtime authority。

## 10. Selected rule / wildcard / immutable value regressions

- `CASE-P2-SELECTED-RULE-001-R10`：Guard owns exact PolicyIndex lookup count=1；Gateway lookup count=0；request/capability cannot replace rule/plan/status。
- `CASE-P2-SYSTEMS-WILDCARD-READ-001-R10`：real order/payment READ `*` compile-time finite exact expansion only；runtime wildcard keys=0；wildcard WRITE/EXECUTE/empty expansion fail。
- `CASE-P2-RUNTIME-FACT-VALUE-001-R10`：public final/private constructor/six typed factories/deep immutable/typed visitor/deterministic canonical form。

## 11. Cross-module / Java8 compatibility

- `CASE-P2-RUNTIME-REQUIREMENT-MODULE-BOUNDARY-001-R10`：context-owned immutable plan/requirement factories callable by compiler without split package/reverse dependency；factory visibility grants no runtime authority。
- `CASE-P2-JAVA8-ENGINE-CONTEXT-001-R10`：release 8；no Java9+ API；EngineContext existing constructor/accessors compatible；P2 additive only；no bare-name RuleView API。

## 12. Fail-closed matrix

| Condition | Expected |
|---|---|
| policy missing | DENY / POLICY_NOT_FOUND |
| Context mismatch | DENY / CONTEXT_IDENTITY_MISMATCH |
| direct protected operation outside Gateway/Guard | DENY / MODEL_ACCESS_GUARD_BYPASS |
| STATIC_ALLOW rule contains runtime plan/requirement | invalid compiled state |
| runtime plan/requirement missing | DENY / invalid compiled state |
| runtime proof invalid | DENY / RUNTIME_BINDING_PROOF_INVALID |
| stale frame/membership | DENY / RUNTIME_BINDING_STALE |
| wrong rule/plan | DENY / RUNTIME_BINDING_PLAN_MISMATCH |
| executor target substitution | DENY / RUNTIME_BINDING_OPERATION_TARGET_MISMATCH |
| capability replay | DENY / RUNTIME_BINDING_CAPABILITY_CONSUMED |
| Guard unavailable | DENY / GUARD_UNAVAILABLE |
| STATIC_ALLOW | Gateway 1 / Guard 1 / lookup 1 / verifier 0 / evaluator 0 / operation 1 |

Every DENY -> protected operation count 0 + external effects 0。Timeout tests use controlled Future/fake monotonic time；禁止 `Thread.sleep` oracle。

## 13. Traceability

| Acceptance/Finding | Blocking evidence |
|---|---|
| FND-001 / AC-007 | CASE-P2-STATIC-ALLOW-GUARD-PATH-001-R10 + CASE-P2-UNIFIED-PROTECTED-ACCESS-BRANCH-001-R10 + CASE-P2-GUARD-NO-BYPASS-001-R10 |
| AC-006 / FND-014 / FND-016 / FND-018 | CASE-P2-DYNAMIC-CLASSIFIER-REAL-FIXTURE-001-R10 + CASE-P2-DYNAMIC-SOURCE-TO-OPERATION-001-R10 |
| AC-006 / FND-017 | CASE-P2-RUNTIME-BINDING-PROOF-001-R10 |
| FND-019 | CASE-P2-RUNTIME-BINDING-OPERATION-SUBSTITUTION-001-R10 |
| FND-009 | CASE-P2-SELECTED-RULE-001-R10 |
| FND-010 | CASE-P2-SYSTEMS-WILDCARD-READ-001-R10 |
| FND-011 | CASE-P2-RUNTIME-FACT-VALUE-001-R10 |
| FND-012 | §2 valid RED contract |
| FND-015 | CASE-P2-RUNTIME-REQUIREMENT-MODULE-BOUNDARY-001-R10 |
| FND-008 | CASE-P2-JAVA8-ENGINE-CONTEXT-001-R10 |

## 14. Review and phase gate

`TESTDESIGN-P2-R10` cannot pass before exact `DESIGN-P2-R09` passes and RC9 machine lifecycle binds current revisions。FND-001 is `PARTIAL_FIX_PROPOSED` until the new static Guard path receives exact independent Review/Evidence。Implementation Plan / TDD / Development remain BLOCKED while any effective P0/P1 is open。
