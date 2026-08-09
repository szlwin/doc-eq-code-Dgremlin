# FEATURE-DESC-3361AD2E54FC Test Design

> Revision：`TESTDESIGN-P2-R21`
> Base：`TESTDESIGN-P2-R20`
> Inputs：`REQAN-P2-R01@d08612768131` + Overlay R04 + `BM-R18` + `FLOW-R08@p2-system-ruleview-protected-access` + `DESIGN-P2-R20`
> Decisions：AC-007 Option B ACTIVE；AccessOperation READ/WRITE-only ACTIVE
> Status：`NEEDS_REVIEW / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED`

R21 retains the R20 matrix and corrects source identity, WRITE-intent uniqueness, real production model reachability, API value contracts and exact future RED commands. It defines Test Design only; no tests were created/executed and no TDD Evidence is claimed.

## 1. Exact Formal RED contract

Bootstrap may use `-am`; target RED must not.

| Contract Test | Maven module | Planned test source path | Exact bootstrap | Exact target RED |
|---|---|---|---|---|
| `P2RevisionDependencyDagContractTest` | `dec-core-compiler` | `dec-core-compiler/src/test/java/dec/core/compiler/contract/P2RevisionDependencyDagContractTest.java` | `./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install` | `./mvnw -pl dec-core-compiler -Dtest=P2RevisionDependencyDagContractTest -Dsurefire.failIfNoSpecifiedTests=true test` |
| `TargetKeySourceMappingContractTest` | `dec-core-compiler` | `dec-core-compiler/src/test/java/dec/core/compiler/model/access/TargetKeySourceMappingContractTest.java` | `./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install` | `./mvnw -pl dec-core-compiler -Dtest=TargetKeySourceMappingContractTest -Dsurefire.failIfNoSpecifiedTests=true test` |
| `RuntimeFactValueContractTest` | `dec-core-context` | `dec-core-context/src/test/java/dec/core/context/runtime/RuntimeFactValueContractTest.java` | `./mvnw -pl dec-core-context -am -Dmaven.test.skip=true install` | `./mvnw -pl dec-core-context -Dtest=RuntimeFactValueContractTest -Dsurefire.failIfNoSpecifiedTests=true test` |
| `OpaqueRuntimeIdContractTest` | `dec-core-context` | `dec-core-context/src/test/java/dec/core/context/runtime/OpaqueRuntimeIdContractTest.java` | `./mvnw -pl dec-core-context -am -Dmaven.test.skip=true install` | `./mvnw -pl dec-core-context -Dtest=OpaqueRuntimeIdContractTest -Dsurefire.failIfNoSpecifiedTests=true test` |
| `ProtectedWriteIntentResolutionTest` | `dec-core-starter` | `dec-core-starter/src/test/java/dec/core/starter/access/ProtectedWriteIntentResolutionTest.java` | `./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install` | `./mvnw -pl dec-core-starter -Dtest=ProtectedWriteIntentResolutionTest -Dsurefire.failIfNoSpecifiedTests=true test` |
| `ProtectedRuntimeModelAdapterIntegrationTest` | `dec-core-starter` | `dec-core-starter/src/test/java/dec/core/starter/access/ProtectedRuntimeModelAdapterIntegrationTest.java` | `./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install` | `./mvnw -pl dec-core-starter -Dtest=ProtectedRuntimeModelAdapterIntegrationTest -Dsurefire.failIfNoSpecifiedTests=true test` |
| `ProtectedAccessDependencyDirectionTest` | `dec-core-starter` | `dec-core-starter/src/test/java/dec/core/starter/architecture/ProtectedAccessDependencyDirectionTest.java` | `./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install` | `./mvnw -pl dec-core-starter -Dtest=ProtectedAccessDependencyDirectionTest -Dsurefire.failIfNoSpecifiedTests=true test` |

If target TestClass/symbol/setup is missing or compile fails before intended assertion, result is `INVALID_RED`, not a valid failing behavioral test.

## 2. Revision DAG

### CASE-P2-TD-REVISION-DAG-001 — BLOCKING
Assert exactly `Overlay R04 -> BM-R18 -> FLOW-R08 -> DESIGN-P2-R20 -> TESTDESIGN-P2-R21`; no downstream authoritative input; dependency graph/impact/traceability must carry current revisions.

## 3. System / RuleView / compatibility

Retain:
- `CASE-P2-TD-SYSTEM-DETERMINISM-001`
- `CASE-P2-TD-SYSTEM-DUPLICATE-001`
- `CASE-P2-TD-SYSTEM-FORWARD-REF-001`
- `CASE-P2-TD-SYSTEM-OWNERSHIP-SNAPSHOT-001`
- `CASE-P2-TD-SYSTEM-VERSION-IDENTITY-001`
- `CASE-P2-TD-BM-CANONICAL-PAIR-001`
- `CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001`
- `CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001`
- `CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001`
- `CASE-P2-TD-RULEVIEW-VIEW-RESOLUTION-001`
- `CASE-P2-TD-RULEKEY-CONTRACT-001`
- `CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001`
- `CASE-P2-TD-KEY-SOURCE-COMPAT-001`
- `CASE-P2-TD-BARE-NAME-COMPATIBILITY-BOUNDARY-001`

## 4. P1-compatible TargetKey / ModelPath

### CASE-P2-TD-TARGETKEY-SOURCE-MAPPING-001 — BLOCKING
Using the real P1-style shape where authorization owner System may refer to a shared source View different from its local targetView:

- `sourceModel="OrderInfo"` resolves through existing shared `ViewKey("OrderInfo")` regardless of authorization owner System;
- SystemA and SystemB authorizing the same shared source View produce value-equal `TargetKey(ViewKey("OrderInfo"))`;
- their `ModelAccessRuleKey` differs by `authorizationOwnerSystemKey`;
- local `targetView/selector/resolvedTarget` is validated separately inside owner System;
- changing sourcePath does not change TargetKey;
- missing shared source View => stable source-aware compile ERROR and publication=0;
- no System-qualified source namespace is assumed without a Requirement/Decision.

### CASE-P2-TD-TARGET-PATH-ORTHOGONALITY-001 — BLOCKING
Verify TargetKey and ModelPath independent axes.

Retain `CASE-P2-TD-MODEL-PATH-UNKNOWN-001`, `CASE-P2-TD-WILDCARD-FINITE-EXPANSION-001`, `CASE-P2-TD-MODEL-PATH-CROSS-CONSUMER-EQUIVALENCE-001`, `CASE-P2-TD-P1-PATH-OPERATION-MIGRATION-001`.

## 5. READ/WRITE + policy classification

Retain:
- `CASE-P2-TD-ACCESS-READ-WRITE-MATRIX-001`
- `CASE-P2-TD-NO-EXECUTE-CONTRACT-001`
- `CASE-P2-TD-STATIC-DENY-001`
- `CASE-P2-TD-POLICY-CLASSIFICATION-TRUTH-TABLE-001`
- `CASE-P2-TD-RUNTIME-PLAN-EXACT-BINDING-001`
- `CASE-P2-TD-DYNAMIC-CLASSIFIER-REAL-001`
- `CASE-P2-TD-RUNTIME-BINDING-PROOF-001`
- `CASE-P2-TD-RUNTIME-PLAN-MISMATCH-001`
- `CASE-P2-TD-SOURCE-TO-READ-WRITE-OPERATION-001`

## 6. WRITE intent exact selection — new blockers

### CASE-P2-TD-WRITE-INTENT-NOT-FOUND-001 — BLOCKING
Zero candidates for exact `(ruleKey,target,path,frame,owner,cursor)` => `WRITE_INTENT_NOT_FOUND`; capability/Guard/operation=0; mutation/receipt/value absent.

### CASE-P2-TD-WRITE-INTENT-AMBIGUOUS-001 — BLOCKING
Two or more candidates => `WRITE_INTENT_AMBIGUOUS`; deterministic candidate ordering may be diagnostic only; no arbitrary first/last choice; Guard/operation=0.

### CASE-P2-TD-WRITE-INTENT-FREEZE-STABILITY-001 — BLOCKING
Exactly one candidate is frozen before Guard. Mutating/replacing frame/cursor state after freeze cannot change the selected intent. If staleness invalidates proof, DENY before operation; never re-resolve another intent.

## 7. Real production READ/WRITE — strengthened blockers

### CASE-P2-TD-REAL-READ-OPERATION-001 — BLOCKING
Acquire normal `ProtectedAccessRuntimeFactory -> ProtectedAccessComposition`; use production dec-core-model RuntimeModelOperationPort. ALLOW READ returns exact deep immutable snapshot from actual runtime object/path, write count=0. DENY invokes model port zero times.

### CASE-P2-TD-REAL-WRITE-OPERATION-001 — BLOCKING
Acquire normal production composition; exactly one frozen intent; Guard precedes model adapter; adapter mutates actual dec-core-model object/path exactly once and returns receipt bound to invocation/object/path/intent. DENY/stale/consumed => mutation=0, receipt absent.

### CASE-P2-TD-PRODUCTION-MODEL-ADAPTER-REACHABILITY-001 — BLOCKING
Prove normal starter assembly wires the dec-core-model production implementation. A fake adapter or effect counter does not satisfy this case.

### CASE-P2-TD-OPERATION-PORT-NOT-CALLER-INJECTABLE-001 — BLOCKING
Public consumer APIs expose no raw RuntimeModelOperationPort/operation callback and cannot replace operation after Guard.

## 8. Runtime value and ID contracts — new blockers

### CASE-P2-TD-RUNTIME-FACT-VALUE-DOMAIN-001 — BLOCKING
Exercise NULL/BOOLEAN/INTEGER/DECIMAL/STRING/LIST/OBJECT; reject arbitrary object; normalize integer/decimal; deterministic structural equality/serialization.

### CASE-P2-TD-RUNTIME-FACT-VALUE-DEEP-IMMUTABILITY-001 — BLOCKING
Mutating original nested input after snapshot cannot change read value; returned LIST/OBJECT cannot be mutated; no live runtime reference leaks.

### CASE-P2-TD-OPAQUE-RUNTIME-ID-VALUE-CONTRACT-001 — BLOCKING
RuntimeObjectId/ProtectedInvocationId/RuntimeWriteIntentId reject null/blank; preserve exact case-sensitive value; equality/hash are exact; IDs do not encode permission/target inference.

## 9. AC-007 / dependency direction / publication / concurrency

Retain:
- `CASE-P2-TD-PRODUCTION-SEAM-NO-LEGAL-BYPASS-001`
- `CASE-P2-TD-AC007-PRODUCTION-COMPOSITION-001`
- `CASE-P2-TD-AC007-RULE-CONSUMER-INTEGRATION-001`
- `CASE-P2-TD-AC007-CHANGE-CONSUMER-INTEGRATION-001`
- `CASE-P2-TD-AC007-CUSTOM-ACTION-CONSUMER-INTEGRATION-001`
- `CASE-P2-TD-AC007-CONSUMER-PARITY-001`
- `CASE-P2-TD-AC007-REPRESENTATIVE-CONSUMER-STRUCTURE-001`
- `CASE-P2-TD-AC007-REAL-PRODUCTION-REACHABILITY-001`
- `CASE-P2-TD-DOWNSTREAM-DEPENDENCY-DIRECTION-001`
- `CASE-P2-TD-ATOMIC-PUBLICATION-001`
- `CASE-P2-TD-CONTEXT-ISOLATION-001`
- `CASE-P2-TD-POLICY-INDEX-PUBLICATION-001`
- `CASE-P2-TD-DIAGNOSTIC-DETERMINISM-001`
- `CASE-P2-TD-RUNTIME-DENIAL-DIAGNOSTIC-DETERMINISM-001`
- `CASE-P2-TD-CAPABILITY-CONCURRENT-CONSUME-001`
- `CASE-P2-TD-DIFFERENT-CAPABILITY-CONCURRENCY-001`
- `CASE-P2-TD-DECLARATION-BOUNDARY-001`

Dependency oracle additionally allows planned `dec-core-starter -> dec-core-model` production assembly and continues forbidding P3/P4/P6 core -> starter.

## 10. Review / Evidence gate

`risk_detection.json` remains NOT_SCANNED and current verification Evidence IDs remain none. R21 therefore cannot enter TDD yet. Requirement/BM/Flow/Impact/XMod/API/Architecture/Develop/Concurrency/TestDesign independent Reviews and current-revision risk scan are still required.
