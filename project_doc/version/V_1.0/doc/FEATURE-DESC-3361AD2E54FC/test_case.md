# FEATURE-DESC-3361AD2E54FC Test Design

> Revision：`TESTDESIGN-P2-R11`。
> Base：`TESTDESIGN-P2-R10`。
> Inputs：Requirement `REQAN-P2-R01`、Business Model candidate `BM-R12`、Design candidate `DESIGN-P2-R10`。
> Status：`NEEDS_CHANGES_CANDIDATE_FIXED / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED`。本 Revision 专门把 blocking cases 映射到真实 Maven module / planned TestClass / exact RED command；Test Design 不创建 skeleton、不执行 TDD。

## 1. Principles

1. 所有 protected READ/WRITE/EXECUTE，包括 STATIC_ALLOW，都必须进入同一 starter-owned protected runtime -> Gateway -> Guard path。
2. `STATIC_ALLOW` 只能是 Guard exact lookup 后内部 fast path；无 RuntimeBindingPlan/verifier/evaluator。
3. Runtime binding proof 只属于 selected `RUNTIME_GUARD_REQUIRED`。
4. actual target 与 operation one-shot binding 不可替换；proof/capability A 不得授权 B。
5. P2 concrete runtime owner 是真实 `dec-core-starter`；不使用抽象 `<target-module>` 或不存在的 `dec-core-runtime`。
6. Real `systems.xml` integration owner 是 `dec-demo`，因为 resource 与 starter dependency 已存在于该 module。
7. valid RED：bootstrap 可以 `-am`，正式 target test **禁止 `-am`**；missing module/test/symbol/setup/compile failure 是 INVALID_RED。
8. Implementation Plan/TDD/Development 在 exact Design Review 与 machine gate 前仍 BLOCKED。

## 2. Exact Maven / planned TestClass contract

> 下表中的 TestClass 是下一阶段 TDD 的**冻结目标名称**，不是当前已存在/已执行 Evidence。

| Purpose | Exact module | Planned TestClass |
|---|---|---|
| Neutral access API/module shape | `dec-core-context` | `dec.core.context.model.access.ProtectedAccessApiContractTest` |
| Rule status/plan invariant | `dec-core-compiler` | `dec.core.compiler.access.ModelAccessRuleCompilationContractTest` |
| Starter module/package ownership | `dec-core-starter` | `dec.core.starter.access.ProtectedAccessRuntimeOwnershipTest` |
| STATIC_ALLOW Guard path | `dec-core-starter` | `dec.core.starter.access.ProtectedAccessStaticAllowPathTest` |
| Runtime membership proof | `dec-core-starter` | `dec.core.starter.access.RuntimeBindingProofIntegrationTest` |
| A-proof/B-target substitution + TOCTOU | `dec-core-starter` | `dec.core.starter.access.ProtectedAccessOperationBindingTest` |
| Unified static/runtime branch counts | `dec-core-starter` | `dec.core.starter.access.UnifiedProtectedAccessBranchTest` |
| Real production classifier fixture | `dec-demo` | `dec.demo.p2.P2DynamicClassifierRealFixtureTest` |
| Full real source -> operation | `dec-demo` | `dec.demo.p2.P2DynamicSourceToOperationTest` |

Exact command pattern for any row：

```bash
./mvnw -pl <EXACT-MODULE> -am -Dmaven.test.skip=true install
./mvnw -pl <EXACT-MODULE> -Dtest=<EXACT-TESTCLASS> -Dsurefire.failIfNoSpecifiedTests=true test
```

The second command must not use `-am`。

## 3. FND-004 repository ownership — BLOCKING

### CASE-P2-RUNTIME-OWNERSHIP-001-R11

**Module/TestClass**

```text
dec-core-starter
dec.core.starter.access.ProtectedAccessRuntimeOwnershipTest
```

**Bootstrap**

```bash
./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install
```

**Formal RED/target**

```bash
./mvnw -pl dec-core-starter -Dtest=dec.core.starter.access.ProtectedAccessRuntimeOwnershipTest -Dsurefire.failIfNoSpecifiedTests=true test
```

Required architecture/API oracles：

1. root reactor contains `dec-core-starter` and does not require a P2 `dec-core-runtime` module；
2. starter owns package `dec.core.starter.access.*` planned concrete runtime classes：
   - `ProtectedAccessRuntime`
   - `ProtectedAccessRuntimeFactory`
   - `DefaultProtectedAccessResolver`
   - `DefaultProtectedAccessGateway`
   - `DefaultModelAccessGuard`
   - `DefaultRuntimeBindingVerifier`
   - `ContextLocalProtectedAccessRegistry`；
3. trusted SPI owner is `dec.core.starter.access.spi.*`；
4. context contracts remain under `dec.core.context.model.access.*`；compiler publication remains under `dec.core.compiler.access.*`；
5. no context->starter/compiler reverse dependency；no compiler->starter dependency；
6. P2 starter runtime must not add a direct `dec-core-model` dependency merely to understand business POJOs；
7. adapter registry is composition-time immutable; public per-call execute surface has no raw target/executor callback；
8. starter source in P2 contains no full Rule/change/action/query business execution implementation。

This Case is mandatory for FND-004 closure。

## 4. FND-001 STATIC_ALLOW Guard path — BLOCKING

### CASE-P2-STATIC-ALLOW-GUARD-PATH-001-R11

**Module/TestClass**

```text
dec-core-starter
dec.core.starter.access.ProtectedAccessStaticAllowPathTest
```

**Commands**

```bash
./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-starter -Dtest=dec.core.starter.access.ProtectedAccessStaticAllowPathTest -Dsurefire.failIfNoSpecifiedTests=true test
```

Compile precondition from compiler fixture：`DIRECT_EXACT -> STATIC_BOUND -> STATIC_ALLOW` and selected rule has no plan/requirement。

Runtime oracle：

```text
ProtectedAccessRuntime.execute(context,intent)
 -> DefaultProtectedAccessResolver binds target A
 -> generic one-shot capability A (no runtime plan input)
 -> DefaultProtectedAccessGateway = 1
 -> DefaultModelAccessGuard = 1
 -> Guard exact PolicyIndex lookup = 1
 -> selected STATIC_ALLOW
 -> DefaultRuntimeBindingVerifier = 0
 -> evaluator = 0
 -> same bound target A operation = 1
 -> capability consumed
```

Negative：caller-side/direct `STATIC_ALLOW` operation outside starter runtime is impossible through supported API or fails `MODEL_ACCESS_GUARD_BYPASS` before operation；operation/effects=0。

## 5. Production classifier real fixture — BLOCKING

### CASE-P2-DYNAMIC-CLASSIFIER-REAL-FIXTURE-001-R11

**Module/TestClass**

```text
dec-demo
dec.demo.p2.P2DynamicClassifierRealFixtureTest
```

**Commands**

```bash
./mvnw -pl dec-demo -am -Dmaven.test.skip=true install
./mvnw -pl dec-demo -Dtest=dec.demo.p2.P2DynamicClassifierRealFixtureTest -Dsurefire.failIfNoSpecifiedTests=true test
```

Real source：`dec-demo/src/main/resources/mix/system/systems.xml` / `system=order` / information `ordered`：

```text
status = 1
and
every(orderDetailList, status = 1)
```

Required：
- production parser/compiler generates real resolved IR；
- direct status -> `DIRECT_EXACT -> STATIC_BOUND`；
- every element status -> `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND`；
- real READ `*` expansion includes exact readable member；no parent fallback；
- unsupported dynamic selector -> `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED`；
- classifier stub cannot satisfy this Case。

## 6. Compiled rule/plan invariant

### CASE-P2-RULE-PLAN-INVARIANT-001-R11

**Module/TestClass**

```text
dec-core-compiler
dec.core.compiler.access.ModelAccessRuleCompilationContractTest
```

**Commands**

```bash
./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-compiler -Dtest=dec.core.compiler.access.ModelAccessRuleCompilationContractTest -Dsurefire.failIfNoSpecifiedTests=true test
```

Oracle：
- STATIC_ALLOW -> plan empty / requirement empty；
- RUNTIME_GUARD_REQUIRED -> exact plan present / EXACT_RUNTIME_BINDING present；
- illegal mixed state cannot publish；
- semantic digest includes runtime plan identity only when runtime-required。

## 7. Runtime membership proof — BLOCKING

### CASE-P2-RUNTIME-BINDING-PROOF-001-R11

**Module/TestClass**

```text
dec-core-starter
dec.core.starter.access.RuntimeBindingProofIntegrationTest
```

**Commands**

```bash
./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-starter -Dtest=dec.core.starter.access.RuntimeBindingProofIntegrationTest -Dsurefire.failIfNoSpecifiedTests=true test
```

Required：
- context-bound starter runtime uses trusted adapter registry；
- actual element A under current owner/cursor -> runtime rule -> verifier MATCH -> A op=1；
- B from another OrderInfo/collection under same static tuple -> DENY；
- stale Context/frame/cursor, wrong rule/plan, forged provenance -> DENY；
- no raw target/capability mint public API；
- absent trusted adapter -> `PROTECTED_ACCESS_ADAPTER_UNAVAILABLE`, op/effects=0。

## 8. Operation substitution / TOCTOU — BLOCKING

### CASE-P2-RUNTIME-BINDING-OPERATION-SUBSTITUTION-001-R11

**Module/TestClass**

```text
dec-core-starter
dec.core.starter.access.ProtectedAccessOperationBindingTest
```

**Commands**

```bash
./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-starter -Dtest=dec.core.starter.access.ProtectedAccessOperationBindingTest -Dsurefire.failIfNoSpecifiedTests=true test
```

Positive：capability A -> Guard verifies A -> exact registry-bound execution port operates A once。

Negative：valid A capability + forced target B -> supported API impossible OR invariant seam `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH`；A/B operation=0、state unchanged、effects=0。

TOCTOU/replay：membership change、frame/cursor/Context invalidation -> DENY；second execute -> consumed；two concurrent execute -> at most one terminal success。

## 9. Unified branch counts — BLOCKING

### CASE-P2-UNIFIED-PROTECTED-ACCESS-BRANCH-001-R11

**Module/TestClass**

```text
dec-core-starter
dec.core.starter.access.UnifiedProtectedAccessBranchTest
```

**Commands**

```bash
./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-starter -Dtest=dec.core.starter.access.UnifiedProtectedAccessBranchTest -Dsurefire.failIfNoSpecifiedTests=true test
```

| Branch | Resolver | Gateway | Guard | Policy lookup | Runtime verifier | Evaluator | Operation |
|---|---:|---:|---:|---:|---:|---:|---:|
| STATIC_ALLOW | 1 | 1 | 1 | 1 | 0 | 0 | 1 same target |
| RUNTIME + valid proof | 1 | 1 | 1 | 1 | 1 | 0 | 1 same target |
| RUNTIME + invalid proof | 1 | 1 | 1 | 1 | 1 | 0 | 0 |
| missing adapter | 1 attempt | 0 operation | 0/blocked before policy as designed | 0 | 0 | 0 | 0 |

Adapter failure must be fail-closed and must not create a direct fallback path。

## 10. Full real source -> protected operation — BLOCKING

### CASE-P2-DYNAMIC-SOURCE-TO-OPERATION-001-R11

**Module/TestClass**

```text
dec-demo
dec.demo.p2.P2DynamicSourceToOperationTest
```

**Commands**

```bash
./mvnw -pl dec-demo -am -Dmaven.test.skip=true install
./mvnw -pl dec-demo -Dtest=dec.demo.p2.P2DynamicSourceToOperationTest -Dsurefire.failIfNoSpecifiedTests=true test
```

```text
real systems.xml
 -> production compiler/classifier
 -> immutable EngineContext
 -> ProtectedAccessRuntimeFactory(context, trusted test adapters)
 -> static direct status branch: no plan, Guard=1, verifier=0, same target op=1
 -> runtime every element branch: exact plan, Guard=1, verifier=1, valid A op=1
 -> foreign/stale/substituted case: DENY, op=0, effects=0
```

Manual compiled rule、classifier stub、detached proof-only test、caller-side static fast path cannot satisfy this Case。

## 11. Neutral API / Java8 compatibility

### CASE-P2-PROTECTED-ACCESS-API-001-R11

**Module/TestClass**

```text
dec-core-context
dec.core.context.model.access.ProtectedAccessApiContractTest
```

**Commands**

```bash
./mvnw -pl dec-core-context -am -Dmaven.test.skip=true install
./mvnw -pl dec-core-context -Dtest=dec.core.context.model.access.ProtectedAccessApiContractTest -Dsurefire.failIfNoSpecifiedTests=true test
```

Oracle：Java 8 source/API；neutral contracts contain no starter implementation dependency；EngineContext existing constructor/accessors remain；capability has no public mint/raw-target/selected-policy setter；RuntimeFactValue remains closed immutable typed value。

## 12. Existing acceptance matrix carried forward

- AC-001 System determinism: same canonical SystemKey/order/digest; duplicate/conflict stable ERROR/no partial publish。
- AC-002 RuleView identity: `(SystemKey,name)`；missing owner stable ERROR；no bare-name fallback。
- AC-003 exact RuleView call only。
- AC-004 READ/WRITE/EXECUTE independent；undeclared/shared-write defaults denied；all protected operations use starter runtime boundary。
- AC-005 exact canonical ModelPath only。
- AC-007 no bypass: current/future consumer integration must use context-bound `ProtectedAccessRuntime`。
- AC-008 immutable whole-context publication；runtime registry is context-local and outside compiled facts。
- AC-009 stable diagnostics/reasons including adapter unavailable, Guard bypass, proof/stale/plan/substitution/consumed。
- AC-010 retired declaration module remains read-only compatibility only。

## 13. Wildcard / selected rule / value regressions

- Guard owns exact PolicyIndex lookup=1；Gateway/resolver/verifier/adapter lookup=0。
- READ `*` compile-time finite exact expansion；runtime wildcard key=0；wildcard WRITE/EXECUTE/empty expansion fail。
- `RuntimeFactValue` public final/private constructor/six typed factories/deep immutable/typed visitor/deterministic canonical form。

## 14. Fail-closed matrix

| Condition | Expected |
|---|---|
| policy missing | DENY / POLICY_NOT_FOUND |
| Context mismatch | DENY / CONTEXT_IDENTITY_MISMATCH |
| direct operation outside starter runtime | DENY / MODEL_ACCESS_GUARD_BYPASS |
| trusted adapter missing | DENY / PROTECTED_ACCESS_ADAPTER_UNAVAILABLE |
| STATIC rule contains runtime plan | invalid compiled state |
| runtime plan/requirement missing | DENY / invalid compiled state |
| proof invalid | DENY / RUNTIME_BINDING_PROOF_INVALID |
| stale frame/membership | DENY / RUNTIME_BINDING_STALE |
| wrong rule/plan | DENY / RUNTIME_BINDING_PLAN_MISMATCH |
| target substitution | DENY / RUNTIME_BINDING_OPERATION_TARGET_MISMATCH |
| capability replay | DENY / RUNTIME_BINDING_CAPABILITY_CONSUMED |
| Guard unavailable | DENY / GUARD_UNAVAILABLE |

Every DENY -> protected operation=0 + external effects=0。

## 15. Traceability

| Finding/Acceptance | Blocking case |
|---|---|
| FND-004 | CASE-P2-RUNTIME-OWNERSHIP-001-R11 + CASE-P2-PROTECTED-ACCESS-API-001-R11 |
| FND-001 / AC-007 | CASE-P2-STATIC-ALLOW-GUARD-PATH-001-R11 + CASE-P2-UNIFIED-PROTECTED-ACCESS-BRANCH-001-R11 |
| FND-018 / AC-006 | CASE-P2-DYNAMIC-CLASSIFIER-REAL-FIXTURE-001-R11 |
| FND-014/FND-016 / AC-006 | CASE-P2-DYNAMIC-SOURCE-TO-OPERATION-001-R11 |
| FND-017 | CASE-P2-RUNTIME-BINDING-PROOF-001-R11 |
| FND-019 | CASE-P2-RUNTIME-BINDING-OPERATION-SUBSTITUTION-001-R11 |
| FND-008/FND-015 | CASE-P2-PROTECTED-ACCESS-API-001-R11 + compiler module-boundary test |

## 16. Review / phase gate

`TESTDESIGN-P2-R11` remains BLOCKED until exact `DESIGN-P2-R10` passes Architecture/ApiContract/Develop/Impact/CrossModule/Concurrency and other required Reviews and RC9 machine lifecycle/risk Evidence binds current revisions。Planned TestClass names/commands are contract only；no TDD execution is legal while effective P1 remains open。