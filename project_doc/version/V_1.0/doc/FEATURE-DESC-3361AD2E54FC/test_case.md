# FEATURE-DESC-3361AD2E54FC Test Design

> Revision：`TESTDESIGN-P2-R22`  
> Base：`TESTDESIGN-P2-R21`  
> Inputs：`REQAN-P2-R01@d08612768131` + Overlay R04 + `BM-R19` + `FLOW-R09@p2-system-ruleview-protected-access` + `DESIGN-P2-R21`  
> Decisions：AC-007 Option B ACTIVE；AccessOperation READ/WRITE-only ACTIVE  
> Status：`NEEDS_REVIEW / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED`

R22 preserves the broad R21 coverage and adds executable oracles for the remaining runtime authority/locator/atomicity/concurrency gaps. Every blocking Case ID in this document is mapped to one exact planned TestClass, Maven module, source path and target RED command. No TDD execution is claimed.

## 1. Formal RED rules

- Bootstrap MAY use `-am` and `-Dmaven.test.skip=true install`.
- Target RED MUST NOT use `-am`.
- Target command MUST use `-Dsurefire.failIfNoSpecifiedTests=true`.
- Missing class/symbol/setup or compilation failure before the intended assertion is `INVALID_RED`.

## 2. Exact TestClass registry

### `DAG` — `P2RevisionDependencyDagContractTest`
- Module: `dec-core-compiler`
- Planned source: `dec-core-compiler/src/test/java/dec/core/compiler/contract/P2RevisionDependencyDagContractTest.java`
- Bootstrap: `./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-compiler -Dtest=P2RevisionDependencyDagContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `SYSTEM` — `SystemCompilationContractTest`
- Module: `dec-core-compiler`
- Planned source: `dec-core-compiler/src/test/java/dec/core/compiler/system/SystemCompilationContractTest.java`
- Bootstrap: `./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-compiler -Dtest=SystemCompilationContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `RULEVIEW` — `RuleViewCompilationContractTest`
- Module: `dec-core-compiler`
- Planned source: `dec-core-compiler/src/test/java/dec/core/compiler/ruleview/RuleViewCompilationContractTest.java`
- Bootstrap: `./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-compiler -Dtest=RuleViewCompilationContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `TARGET` — `TargetKeyModelPathContractTest`
- Module: `dec-core-compiler`
- Planned source: `dec-core-compiler/src/test/java/dec/core/compiler/model/access/TargetKeyModelPathContractTest.java`
- Bootstrap: `./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-compiler -Dtest=TargetKeyModelPathContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `POLICY` — `ModelAccessPolicyContractTest`
- Module: `dec-core-compiler`
- Planned source: `dec-core-compiler/src/test/java/dec/core/compiler/model/access/ModelAccessPolicyContractTest.java`
- Bootstrap: `./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-compiler -Dtest=ModelAccessPolicyContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `API` — `ProtectedAccessCurrentApiContractTest`
- Module: `dec-core-context`
- Planned source: `dec-core-context/src/test/java/dec/core/context/runtime/ProtectedAccessCurrentApiContractTest.java`
- Bootstrap: `./mvnw -pl dec-core-context -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-context -Dtest=ProtectedAccessCurrentApiContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `VALUE` — `RuntimeFactValueContractTest`
- Module: `dec-core-context`
- Planned source: `dec-core-context/src/test/java/dec/core/context/runtime/RuntimeFactValueContractTest.java`
- Bootstrap: `./mvnw -pl dec-core-context -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-context -Dtest=RuntimeFactValueContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `ID` — `OpaqueRuntimeIdContractTest`
- Module: `dec-core-context`
- Planned source: `dec-core-context/src/test/java/dec/core/context/runtime/OpaqueRuntimeIdContractTest.java`
- Bootstrap: `./mvnw -pl dec-core-context -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-context -Dtest=OpaqueRuntimeIdContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `INTENT` — `ProtectedWriteIntentResolutionTest`
- Module: `dec-core-starter`
- Planned source: `dec-core-starter/src/test/java/dec/core/starter/access/ProtectedWriteIntentResolutionTest.java`
- Bootstrap: `./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-starter -Dtest=ProtectedWriteIntentResolutionTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `ADAPTER` — `ProtectedRuntimeModelAdapterIntegrationTest`
- Module: `dec-core-starter`
- Planned source: `dec-core-starter/src/test/java/dec/core/starter/access/ProtectedRuntimeModelAdapterIntegrationTest.java`
- Bootstrap: `./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-starter -Dtest=ProtectedRuntimeModelAdapterIntegrationTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `LOCATOR` — `RuntimeObjectLocatorIntegrationTest`
- Module: `dec-core-model`
- Planned source: `dec-core-model/src/test/java/dec/core/model/runtime/RuntimeObjectLocatorIntegrationTest.java`
- Bootstrap: `./mvnw -pl dec-core-model -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-model -Dtest=RuntimeObjectLocatorIntegrationTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `TXN` — `ProtectedWriteTransactionIntegrationTest`
- Module: `dec-core-model`
- Planned source: `dec-core-model/src/test/java/dec/core/model/runtime/ProtectedWriteTransactionIntegrationTest.java`
- Bootstrap: `./mvnw -pl dec-core-model -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-model -Dtest=ProtectedWriteTransactionIntegrationTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `COMPOSE` — `ProtectedAccessProductionCompositionTest`
- Module: `dec-core-starter`
- Planned source: `dec-core-starter/src/test/java/dec/core/starter/access/ProtectedAccessProductionCompositionTest.java`
- Bootstrap: `./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-starter -Dtest=ProtectedAccessProductionCompositionTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `CONC` — `ProtectedAccessConcurrencyTest`
- Module: `dec-core-starter`
- Planned source: `dec-core-starter/src/test/java/dec/core/starter/access/ProtectedAccessConcurrencyTest.java`
- Bootstrap: `./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-starter -Dtest=ProtectedAccessConcurrencyTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `DEP` — `ProtectedAccessDependencyDirectionTest`
- Module: `dec-core-starter`
- Planned source: `dec-core-starter/src/test/java/dec/core/starter/architecture/ProtectedAccessDependencyDirectionTest.java`
- Bootstrap: `./mvnw -pl dec-core-starter -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-starter -Dtest=ProtectedAccessDependencyDirectionTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `PUB` — `AtomicPublicationContractTest`
- Module: `dec-core-compiler`
- Planned source: `dec-core-compiler/src/test/java/dec/core/compiler/publication/AtomicPublicationContractTest.java`
- Bootstrap: `./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-compiler -Dtest=AtomicPublicationContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `DIAG` — `P2DiagnosticDeterminismTest`
- Module: `dec-core-compiler`
- Planned source: `dec-core-compiler/src/test/java/dec/core/compiler/diagnostic/P2DiagnosticDeterminismTest.java`
- Bootstrap: `./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-compiler -Dtest=P2DiagnosticDeterminismTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `FIXTURE` — `P2RealFixtureIntegrationTest`
- Module: `dec-demo`
- Planned source: `dec-demo/src/test/java/dec/demo/p2/P2RealFixtureIntegrationTest.java`
- Bootstrap: `./mvnw -pl dec-demo -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-demo -Dtest=P2RealFixtureIntegrationTest -Dsurefire.failIfNoSpecifiedTests=true test`

### `COMPAT` — `P2DeclarationCompatibilityContractTest`
- Module: `dec-core-compiler`
- Planned source: `dec-core-compiler/src/test/java/dec/core/compiler/compat/P2DeclarationCompatibilityContractTest.java`
- Bootstrap: `./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install`
- Exact target RED: `./mvnw -pl dec-core-compiler -Dtest=P2DeclarationCompatibilityContractTest -Dsurefire.failIfNoSpecifiedTests=true test`

## 3. Complete blocking Case map

Every blocking case maps to the registry entry named after `=>`; that registry entry freezes module, source path, bootstrap, and exact target RED command.

### `DAG` => `P2RevisionDependencyDagContractTest` (1 case)
- `CASE-P2-TD-REVISION-DAG-001`

### `SYSTEM` => `SystemCompilationContractTest` (6 cases)
- `CASE-P2-TD-SYSTEM-DETERMINISM-001`
- `CASE-P2-TD-SYSTEM-DUPLICATE-001`
- `CASE-P2-TD-SYSTEM-FORWARD-REF-001`
- `CASE-P2-TD-SYSTEM-OWNERSHIP-SNAPSHOT-001`
- `CASE-P2-TD-SYSTEM-VERSION-IDENTITY-001`
- `CASE-P2-TD-BM-CANONICAL-PAIR-001`

### `RULEVIEW` => `RuleViewCompilationContractTest` (8 cases)
- `CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001`
- `CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001`
- `CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001`
- `CASE-P2-TD-RULEVIEW-VIEW-RESOLUTION-001`
- `CASE-P2-TD-RULEKEY-CONTRACT-001`
- `CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001`
- `CASE-P2-TD-KEY-SOURCE-COMPAT-001`
- `CASE-P2-TD-BARE-NAME-COMPATIBILITY-BOUNDARY-001`

### `TARGET` => `TargetKeyModelPathContractTest` (6 cases)
- `CASE-P2-TD-TARGETKEY-SOURCE-MAPPING-001`
- `CASE-P2-TD-TARGET-PATH-ORTHOGONALITY-001`
- `CASE-P2-TD-MODEL-PATH-UNKNOWN-001`
- `CASE-P2-TD-WILDCARD-FINITE-EXPANSION-001`
- `CASE-P2-TD-MODEL-PATH-CROSS-CONSUMER-EQUIVALENCE-001`
- `CASE-P2-TD-P1-PATH-OPERATION-MIGRATION-001`

### `POLICY` => `ModelAccessPolicyContractTest` (7 cases)
- `CASE-P2-TD-ACCESS-READ-WRITE-MATRIX-001`
- `CASE-P2-TD-NO-EXECUTE-CONTRACT-001`
- `CASE-P2-TD-STATIC-DENY-001`
- `CASE-P2-TD-POLICY-CLASSIFICATION-TRUTH-TABLE-001`
- `CASE-P2-TD-RUNTIME-PLAN-EXACT-BINDING-001`
- `CASE-P2-TD-RUNTIME-BINDING-PROOF-001`
- `CASE-P2-TD-RUNTIME-PLAN-MISMATCH-001`

### `API` => `ProtectedAccessCurrentApiContractTest` (1 case)
- `CASE-P2-TD-CURRENT-API-SELF-CONTAINED-001`

### `VALUE` => `RuntimeFactValueContractTest` (2 cases)
- `CASE-P2-TD-RUNTIME-FACT-VALUE-DOMAIN-001`
- `CASE-P2-TD-RUNTIME-FACT-VALUE-DEEP-IMMUTABILITY-001`

### `ID` => `OpaqueRuntimeIdContractTest` (1 case)
- `CASE-P2-TD-OPAQUE-RUNTIME-ID-VALUE-CONTRACT-001`

### `INTENT` => `ProtectedWriteIntentResolutionTest` (6 cases)
- `CASE-P2-TD-WRITE-INTENT-NOT-FOUND-001`
- `CASE-P2-TD-WRITE-INTENT-AMBIGUOUS-001`
- `CASE-P2-TD-WRITE-INTENT-FREEZE-STABILITY-001`
- `CASE-P2-TD-WRITE-AUTHORITY-MODEL-ACCESS-RULEKEY-001`
- `CASE-P2-TD-WRITE-SINGLE-PATH-AUTHORITY-001`
- `CASE-P2-TD-TYPED-RUNTIME-CONTEXT-001`

### `ADAPTER` => `ProtectedRuntimeModelAdapterIntegrationTest` (4 cases)
- `CASE-P2-TD-REAL-READ-OPERATION-001`
- `CASE-P2-TD-REAL-WRITE-OPERATION-001`
- `CASE-P2-TD-PRODUCTION-MODEL-ADAPTER-REACHABILITY-001`
- `CASE-P2-TD-OPERATION-PORT-NOT-CALLER-INJECTABLE-001`

### `LOCATOR` => `RuntimeObjectLocatorIntegrationTest` (2 cases)
- `CASE-P2-TD-RUNTIME-OBJECT-LOCATOR-SCOPE-001`
- `CASE-P2-TD-RUNTIME-OBJECT-NOT-FOUND-STALE-001`

### `TXN` => `ProtectedWriteTransactionIntegrationTest` (1 case)
- `CASE-P2-TD-RUNTIME-WRITE-ROLLBACK-001`

### `COMPOSE` => `ProtectedAccessProductionCompositionTest` (8 cases)
- `CASE-P2-TD-PRODUCTION-SEAM-NO-LEGAL-BYPASS-001`
- `CASE-P2-TD-AC007-PRODUCTION-COMPOSITION-001`
- `CASE-P2-TD-AC007-RULE-CONSUMER-INTEGRATION-001`
- `CASE-P2-TD-AC007-CHANGE-CONSUMER-INTEGRATION-001`
- `CASE-P2-TD-AC007-CUSTOM-ACTION-CONSUMER-INTEGRATION-001`
- `CASE-P2-TD-AC007-CONSUMER-PARITY-001`
- `CASE-P2-TD-AC007-REPRESENTATIVE-CONSUMER-STRUCTURE-001`
- `CASE-P2-TD-AC007-REAL-PRODUCTION-REACHABILITY-001`

### `CONC` => `ProtectedAccessConcurrencyTest` (2 cases)
- `CASE-P2-TD-CAPABILITY-CONCURRENT-CONSUME-001`
- `CASE-P2-TD-DIFFERENT-CAPABILITY-CONCURRENCY-001`

### `DEP` => `ProtectedAccessDependencyDirectionTest` (1 case)
- `CASE-P2-TD-DOWNSTREAM-DEPENDENCY-DIRECTION-001`

### `PUB` => `AtomicPublicationContractTest` (3 cases)
- `CASE-P2-TD-ATOMIC-PUBLICATION-001`
- `CASE-P2-TD-CONTEXT-ISOLATION-001`
- `CASE-P2-TD-POLICY-INDEX-PUBLICATION-001`

### `DIAG` => `P2DiagnosticDeterminismTest` (2 cases)
- `CASE-P2-TD-DIAGNOSTIC-DETERMINISM-001`
- `CASE-P2-TD-RUNTIME-DENIAL-DIAGNOSTIC-DETERMINISM-001`

### `FIXTURE` => `P2RealFixtureIntegrationTest` (2 cases)
- `CASE-P2-TD-DYNAMIC-CLASSIFIER-REAL-001`
- `CASE-P2-TD-SOURCE-TO-READ-WRITE-OPERATION-001`

### `COMPAT` => `P2DeclarationCompatibilityContractTest` (1 case)
- `CASE-P2-TD-DECLARATION-BOUNDARY-001`

## 4. New/changed behavioral oracles

### CASE-P2-TD-WRITE-AUTHORITY-MODEL-ACCESS-RULEKEY-001 — BLOCKING
Direct Bridge invocation and `ResolvedWriteIntent` must carry the same exact `ModelAccessRuleKey`. `RuleKey` may be absent for Change/CustomAction and, when present, is provenance only. No authorization decision may depend on `RuleKey`.

### CASE-P2-TD-WRITE-SINGLE-PATH-AUTHORITY-001 — BLOCKING
Compile/reflection oracle proves `ResolvedWriteIntent` has no separate `targetKey/modelPath` fields and `RuntimeModelOperationPort.write` accepts only `ResolvedProtectedWriteAccess`. Runtime mutation path is exactly `writeIntent.modelAccessRuleKey.modelPath`; no second path argument exists.

### CASE-P2-TD-TYPED-RUNTIME-CONTEXT-001 — BLOCKING
Invocation -> resolved access -> write intent preserves `RuntimeExecutionFrameId`, `RuntimeResolutionOwnerId`, `Optional<RuntimeCollectionCursorId>`. No raw String/null/empty/`N/A` cursor representation is accepted.

### CASE-P2-TD-CURRENT-API-SELF-CONTAINED-001 — BLOCKING
Using only current `DESIGN-P2-R21` API contract and current source, compile/reflect all frozen P2 public signatures. Test may not read superseded R19/R20 to discover required methods/types.

### CASE-P2-TD-RUNTIME-OBJECT-LOCATOR-SCOPE-001 — BLOCKING
Create two production model sessions/compositions. Register object in session A before seal. After seal: A resolves it exactly; registration/replacement is rejected; session B cannot resolve A id; no static/global object map is observed. Closing A makes its id stale.

### CASE-P2-TD-RUNTIME-OBJECT-NOT-FOUND-STALE-001 — BLOCKING
Missing id -> `RUNTIME_OBJECT_NOT_FOUND`; closed/cross-session/stale id -> `RUNTIME_OBJECT_STALE`; Guard/protected operation/mutation=0 and result has no value/receipt.

### CASE-P2-TD-RUNTIME-WRITE-ROLLBACK-001 — BLOCKING
Acquire normal production model session and freeze pre-write ModelData/origin snapshot. Inject mutation failure and commit failure separately after Guard ALLOW. In both branches: externally observable state equals pre-write snapshot, receipt absent, capability remains CONSUMED, denial/error is `RUNTIME_WRITE_FAILED`, and no automatic retry occurs. Successful branch commits one mutation then publishes state and receipt.

### CASE-P2-TD-DIFFERENT-CAPABILITY-CONCURRENCY-001 — BLOCKING
Freeze two different capabilities against the same RuntimeObjectId + ModelPath + RuntimeMutationVersion, then release with latch/barrier. Oracle: exactly one committed mutation/receipt, mutation version increments exactly once, exactly one stale loser with `WRITE_INTENT_STALE`, loser mutation=0, no partial/lost update. Winner identity need not be predetermined.

## 5. Preserved key oracles

- P1 sourceModel remains shared `ViewKey -> TargetKey`; authorization owner System is separate.
- READ/WRITE-only and two-row policy classification remain exhaustive.
- WRITE 0/1/N selection and Guard-before-effect remain.
- RuntimeFactValue remains closed/deep immutable/deterministic.
- AC-007 production Rule/Change/CustomAction entries share one Bridge/Context/session.
- Same capability atomic consume still permits at most one operation.
- P3/P4/P6 core -> starter remains forbidden; planned starter -> model production assembly is allowed.

## 6. Review / Evidence gate

`risk_detection.json` remains NOT_SCANNED and current execution Evidence IDs remain none. Exact RED commands above are planned TDD commands, not executed Evidence. `TESTDESIGN-P2-R22` remains blocked by same-revision Design/TestDesign Reviews and current-revision risk scan; Implementation Plan/TDD/Development remain BLOCKED.
