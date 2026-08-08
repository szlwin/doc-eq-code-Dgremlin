# FEATURE-DESC-3361AD2E54FC Test Design

> Revision：`TESTDESIGN-P2-R14`。
> Base：`TESTDESIGN-P2-R13`。
> Inputs：Requirement `REQAN-P2-R01`、Business Model candidate `BM-R12`、Design candidate `DESIGN-P2-R13`。
> Status：`NEEDS_CHANGES_CANDIDATE_FIXED / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED`。本 Revision 按用户明确决策删除 execution-token/recognizes/claim/replay 测试模型，把所有 production E2E 改为 direct-argument `ProtectedExecutionBridge.execute(ruleKey, operation, frameId, ...)`；不创建 skeleton、不执行 TDD。

## 1. Principles

1. 所有 protected READ/WRITE/EXECUTE，包括 STATIC_ALLOW，都必须进入 starter-owned bridge -> internal issuance -> Gateway -> Guard。
2. External consumer 不调用 package-private `issueInvocation(...)`，但可直接调用 public bridge 并提交 rule/op/frame/owner/cursor 参数。
3. 不存在 `ProtectedExecutionToken`、`ProtectedExecutionStatePort.recognizes()`、token claim/replay gate。
4. 相同 bridge 参数被调用两次视为两个独立 invocation；Test Design 不要求 duplicate suppression。
5. 唯一 runtime policy authority 是 compiler-published、CompiledModelSet-owned、EngineContext-retained immutable `ModelAccessPolicyIndex`。
6. PolicyIndex 必须在 semantic digest 前建立并进入同一 digest-bound publication closure。
7. Legacy 八参数 CompiledModelSet constructor = deterministic empty-policy fail closed。
8. STATIC_ALLOW 只能是 Guard exact lookup 后 fast path；runtime proof 只属于 RUNTIME_GUARD_REQUIRED。
9. capability actual target + operation binding 不可替换；同一 capability concurrent execute 最多一次 terminal success。
10. valid RED：bootstrap 可 `-am`，target test 禁止 `-am`。
11. Implementation Plan/TDD/Development 在 exact Design Review/machine gate 前 BLOCKED。

## 2. Exact Maven / planned TestClass contract

| Purpose | Exact module | Planned TestClass |
|---|---|---|
| Neutral protected-access API/Java8 | `dec-core-context` | `dec.core.context.model.access.ProtectedAccessApiContractTest` |
| Validated policy-index factory | `dec-core-context` | `dec.core.context.model.access.ModelAccessPolicyIndexContractTest` |
| CompiledModelSet publication compatibility | `dec-core-context` | `dec.core.context.model.ModelAccessPolicyPublicationCompatibilityTest` |
| Rule status/plan invariant | `dec-core-compiler` | `dec.core.compiler.access.ModelAccessRuleCompilationContractTest` |
| Policy publication + digest closure | `dec-core-compiler` | `dec.core.compiler.access.ModelAccessPolicyIndexPublicationTest` |
| Starter ownership | `dec-core-starter` | `dec.core.starter.access.ProtectedAccessRuntimeOwnershipTest` |
| Direct bridge API | `dec-core-starter` | `dec.core.starter.access.ProtectedExecutionBridgeContractTest` |
| Direct bridge concurrency | `dec-core-starter` | `dec.core.starter.access.ProtectedExecutionBridgeConcurrencyTest` |
| Internal issued-pair invariant | `dec-core-starter` | `dec.core.starter.access.ProtectedAccessInputAuthorityTest` |
| Single policy authority | `dec-core-starter` | `dec.core.starter.access.ModelAccessPolicyAuthorityIntegrationTest` |
| STATIC_ALLOW path | `dec-core-starter` | `dec.core.starter.access.ProtectedAccessStaticAllowPathTest` |
| Runtime membership proof | `dec-core-starter` | `dec.core.starter.access.RuntimeBindingProofIntegrationTest` |
| A-proof/B-target substitution + capability replay | `dec-core-starter` | `dec.core.starter.access.ProtectedAccessOperationBindingTest` |
| Unified static/runtime counts | `dec-core-starter` | `dec.core.starter.access.UnifiedProtectedAccessBranchTest` |
| Real classifier fixture | `dec-demo` | `dec.demo.p2.P2DynamicClassifierRealFixtureTest` |
| Direct production reachability | `dec-demo` | `dec.demo.p2.P2DirectBridgeReachabilityTest` |
| Full real source -> operation | `dec-demo` | `dec.demo.p2.P2DynamicSourceToOperationTest` |

Command pattern：

```bash
./mvnw -pl <EXACT-MODULE> -am -Dmaven.test.skip=true install
./mvnw -pl <EXACT-MODULE> -Dtest=<EXACT-TESTCLASS> -Dsurefire.failIfNoSpecifiedTests=true test
```

Second command must not use `-am`。

## 3. FND-004 / FND-016 direct production reachability — BLOCKING

### CASE-P2-DIRECT-BRIDGE-REACHABILITY-001-R14

**Module/TestClass**

```text
dec-demo
dec.demo.p2.P2DirectBridgeReachabilityTest
```

**Positive production oracle**

```text
immutable EngineContext
 -> starter runtime/factory
 -> public ProtectedExecutionBridge
 -> bridge.execute(
      exactRuleKey,
      READ,
      frameId,
      ownerResolutionId,
      optionalCursor)
 -> internal issueInvocation
 -> internal exact pair
 -> resolver/capability/Gateway/Guard
 -> same target operation
```

Mandatory：

1. test stays in `dec-demo`；
2. no reflection/package-private starter access；
3. no test-only mint helper；
4. no manual issued pair；
5. no direct Guard/Gateway shortcut；
6. public bridge has direct argument method；
7. public API does not contain `ProtectedExecutionToken` or `bridge.execute(token)`。

## 4. Direct bridge argument validation — BLOCKING

### CASE-P2-DIRECT-BRIDGE-ARGUMENT-VALIDATION-001-R14

**Module/TestClass**

```text
dec-core-starter
dec.core.starter.access.ProtectedExecutionBridgeContractTest
```

Negative fixtures：

- null ruleKey；
- null operation；
- null frameId；
- null ownerId；
- null Optional cursor wrapper；
- runtime/context closed/unavailable；
- runtime-required rule with incompatible/missing cursor/membership at verifier stage。

Expected for API-shape invalid inputs：

```text
PROTECTED_ACCESS_ARGUMENT_INVALID
internal issued pair = 0
target resolution = 0
capability = 0
Gateway = 0
Guard = 0
PolicyIndex lookup = 0
operation/effects = 0
```

Current Revision intentionally does **not** treat choosing a different valid `ruleKey` or `AccessOperation` as forged authority。

## 5. Direct bridge concurrency — BLOCKING

### CASE-P2-DIRECT-BRIDGE-CONCURRENCY-001-R14

**Module/TestClass**

```text
dec-core-starter
dec.core.starter.access.ProtectedExecutionBridgeConcurrencyTest
```

Use barriers/latches, never `Thread.sleep`。

Oracle A — different arguments：

```text
Thread A execute(argsA)
Thread B execute(argsB)
 -> independent invocation A/B
 -> no frame/owner/cursor/target/capability cross-talk
```

Oracle B — identical arguments：

```text
Thread A execute(ruleX, READ, frameF, ownerO, cursorC)
Thread B execute(ruleX, READ, frameF, ownerO, cursorC)
```

R14 explicitly **does not require success <= 1**。If both satisfy Guard/proof, they may each create their own capability/operation。The test only proves state isolation and no shared mutable invocation record corruption。

Oracle C — same capability：

```text
one ResolvedProtectedAccess capability
 -> two concurrent Gateway executions
 -> terminal success <= 1
 -> loser = RUNTIME_BINDING_CAPABILITY_CONSUMED or equivalent stable consumed denial
```

## 6. Internal issued-pair invariant — BLOCKING DEFENSE-IN-DEPTH

### CASE-P2-INTERNAL-ISSUED-PAIR-001-R14

`dec-core-starter / dec.core.starter.access.ProtectedAccessInputAuthorityTest`

Low-level negative：unknown internal read-interface implementation、A-context+B-intent、record/capability identity substitution。

这些只验证 starter internal consistency；不把 caller-selected rule/op 当成 external forgery。

## 7. FND-015 PolicyIndex construction — BLOCKING

### CASE-P2-POLICY-INDEX-CONSTRUCTION-001-R14

`dec-core-context / dec.core.context.model.access.ModelAccessPolicyIndexContractTest`

Required：`empty()`、`of(Iterable)`、exact `find`、read-only `keys`；duplicate/null/illegal state/non-canonical key rejected；immutable/deterministic。

## 8. FND-015 publication compatibility — BLOCKING

### CASE-P2-POLICY-PUBLICATION-COMPATIBILITY-001-R14

`dec-core-context / dec.core.context.model.ModelAccessPolicyPublicationCompatibilityTest`

Required：legacy 8-arg constructor retained、empty policy/no reconstruction/fail closed；new `CompiledModelSet.published(...policyIndex...)`；EngineContext same authority；equals/hashCode includes policy。

## 9. FND-015 compiler digest closure — BLOCKING

### CASE-P2-POLICY-INDEX-PUBLICATION-001-R14

`dec-core-compiler / dec.core.compiler.access.ModelAccessPolicyIndexPublicationTest`

```text
compiled rules
 -> ModelAccessPolicyIndex.of
 -> SemanticDigestInput(same index)
 -> DigestBoundCompiledInput(same index + digest)
 -> FrozenInput
 -> CompiledModelSet.published(same index + digest)
```

Index before digest；same immutable snapshot；policy semantic change changes semantic digest；P2 production path不走 legacy constructor。

## 10. Single policy authority — BLOCKING

### CASE-P2-POLICY-INDEX-AUTHORITY-001-R14

`dec-core-starter / dec.core.starter.access.ModelAccessPolicyAuthorityIntegrationTest`

Through direct bridge invocation：

```text
Guard PolicyIndex exact lookup = 1
Resolver = 0
Gateway = 0
RuntimeBindingVerifier = 0
TargetResolutionPort = 0
OperationExecutionPort = 0
```

No starter secondary policy Map；no definitions/typed-registry reconstruction。

## 11. STATIC_ALLOW Guard path — BLOCKING

### CASE-P2-STATIC-ALLOW-GUARD-PATH-001-R14

```text
bridge.execute(staticRuleKey, READ, frame, owner, cursor)
 -> resolver
 -> capability
 -> Gateway=1
 -> Guard=1
 -> PolicyIndex exact lookup=1
 -> STATIC_ALLOW
 -> verifier=0
 -> operation=1
```

Direct protected operation outside Guard path = bypass rejection。

## 12. Production classifier real fixture — BLOCKING

### CASE-P2-DYNAMIC-CLASSIFIER-REAL-FIXTURE-001-R14

`dec-demo / dec.demo.p2.P2DynamicClassifierRealFixtureTest`

Real `systems.xml`：direct status -> STATIC_BOUND；`every(orderDetailList,status=1)` -> RUNTIME_OBJECT_BOUND；unsupported dynamic selector compile ERROR；classifier stub cannot satisfy。

## 13. Rule/plan invariant

### CASE-P2-RULE-PLAN-INVARIANT-001-R14

STATIC_ALLOW plan/requirement empty；RUNTIME_GUARD_REQUIRED exact plan + EXACT_RUNTIME_BINDING；illegal mixed state cannot enter PolicyIndex/publication。

## 14. Runtime binding proof — BLOCKING

### CASE-P2-RUNTIME-BINDING-PROOF-001-R14

Direct bridge call provides rule/op/frame/owner/cursor。Resolver binds actual element A；verifier validates exact compiler plan/membership。Foreign B/stale frame/cursor/wrong plan/provenance -> DENY；no raw target public mint。

## 15. A/B target substitution + TOCTOU — BLOCKING

### CASE-P2-RUNTIME-BINDING-OPERATION-SUBSTITUTION-001-R14

- capability produced for target A cannot execute target B；
- ALLOW decision cannot be followed by caller callback selecting B；
- operation uses exact registry-bound target/port；
- frame/cursor/membership stale immediately before operation -> DENY；
- same capability replay/concurrent execute -> at most one success。

This is the remaining FND-019 concurrency/TOCTOU scope。No token claim/replay assertions。

## 16. Unified STATIC/RUNTIME branch counts

### CASE-P2-UNIFIED-PROTECTED-ACCESS-BRANCH-001-R14

STATIC：Guard=1, policy=1, verifier=0, op=1。
Runtime：Guard=1, policy=1, verifier=1, valid op=1。
DENY：op/effects=0。

## 17. Real source -> protected operation — BLOCKING

### CASE-P2-DYNAMIC-SOURCE-TO-OPERATION-001-R14

```text
systems.xml
 -> production parser/compiler
 -> PolicyIndex + digest-bound CompiledModelSet
 -> EngineContext
 -> starter public bridge
 -> bridge.execute(exactRuleKey, operation, frame, owner, cursor)
 -> Guard
 -> static/runtime branch
 -> same target operation
```

No token、manual compiled rule、manual issued pair、classifier stub、direct Gateway shortcut。

## 18. Valid RED rules

每个 target 两步命令保持；第二步禁止 `-am`。Missing module/test/symbol/setup/compile failure = INVALID_RED；pre-skeleton API shape tests可使用 compilable reflection/source assertions。

## 19. Finding / Gate

- FND-004：direct bridge reachability candidate-fixed，formal OPEN。
- FND-015：construction/publication candidate-fixed，formal OPEN。
- FND-016：source->runtime direct bridge candidate-fixed，formal OPEN。
- FND-007：`FIX_PROPOSED / OPEN`；不再要求 token concurrent replay matrix。
- FND-019：`FIX_PROPOSED / OPEN`；只保留 actual target/operation capability binding + capability replay/TOCTOU。
- no FND-020。

`TESTDESIGN-P2-R14` 仍 `BLOCKED_BY_DESIGN_REVIEW / NEEDS_REVIEW / MACHINE_BLOCKED`；TDD/Development 不得启动。