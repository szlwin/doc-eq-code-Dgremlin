# FEATURE-DESC-3361AD2E54FC Test Design

> Revision：`TESTDESIGN-P2-R20`  
> Base：`TESTDESIGN-P2-R19`  
> Inputs：`REQAN-P2-R01@d08612768131` + Overlay R04 + `BM-R17` + `FLOW-R07@p2-system-ruleview-protected-access` + `DESIGN-P2-R19`  
> Decisions：AC-007 Option B ACTIVE；AccessOperation READ/WRITE-only ACTIVE  
> Status：`NEEDS_REVIEW / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED`

R20 retains R19 coverage and adds blocking cases for the remaining Review gaps: acyclic revision authority, exact sourceModel→TargetKey conversion, complete policy-classification truth table, real READ/WRITE result/effect semantics, and downstream core→starter dependency prohibition. No TDD skeleton or execution is claimed.

## 1. Formal RED

```bash
./mvnw -pl <EXACT-MODULE> -am -Dmaven.test.skip=true install
./mvnw -pl <EXACT-MODULE> -Dtest=<EXACT-TESTCLASS> -Dsurefire.failIfNoSpecifiedTests=true test
```

Second command MUST NOT use `-am`. Missing TestClass/symbol/setup/compile failure before intended assertion = `INVALID_RED`.

## 2. Planned TestClass map

### document / contract
- `P2RevisionDependencyDagContractTest`

### dec-core-context
- `SystemOwnershipSnapshotContractTest`
- `RuleViewCompiledRelationContractTest`
- `RuleKeyContractTest`
- `TargetKeyContractTest`
- `ModelAccessPolicyIndexContractTest`
- `ReadWriteAccessOperationContractTest`
- `ProtectedAccessNeutralApiContractTest`

### dec-core-compiler
- `SystemCompilationContractTest`
- `RuleViewCompilationContractTest`
- `TargetKeySourceMappingContractTest`
- `ModelPathCrossConsumerContractTest`
- `P1ToP2ModelAccessMigrationContractTest`
- `ModelAccessPolicyClassificationTruthTableTest`
- `ModelAccessPolicyIndexPublicationTest`
- `P2DiagnosticDeterminismTest`

### dec-core-starter
- `ProtectedExecutionBridgeContractTest`
- `ProtectedAccessProductionCompositionTest`
- `RuleProtectedAccessConsumerIntegrationTest`
- `ChangeProtectedAccessConsumerIntegrationTest`
- `CustomActionProtectedAccessConsumerIntegrationTest`
- `ProtectedAccessConsumerParityTest`
- `ProtectedReadOperationIntegrationTest`
- `ProtectedWriteOperationIntegrationTest`
- `ProtectedAccessCapabilityConcurrencyTest`
- `RuntimeBindingProofIntegrationTest`
- `RuntimeDenialDiagnosticDeterminismTest`

### dependency / integration
- `ProtectedAccessDependencyDirectionTest`

### dec-demo
- `P2SystemOwnershipRealFixtureTest`
- `P2RuleViewCompositeRealFixtureTest`
- `P2DynamicClassifierRealFixtureTest`
- `P2Ac007RepresentativeConsumersRealFixtureTest`
- `P2SourceToReadWriteOperationTest`

## 3. Revision authority — new blocker

### CASE-P2-TD-REVISION-DAG-001 — BLOCKING
Parse current candidate headers/metadata and assert authoritative chain exactly:

```text
Overlay R04 -> BM-R17 -> FLOW-R07 -> DESIGN-P2-R19 -> TESTDESIGN-P2-R20
```

Oracle:
- BM authoritative inputs contain no Flow/Design/TestDesign;
- FLOW authoritative inputs contain BM-R17 but no Design/TestDesign;
- Design inputs contain BM-R17/FLOW-R07;
- TestDesign inputs contain Design-R19;
- downstream trace refs do not count as inputs;
- any cycle or future-revision input => contract failure.

## 4. AC-001 System

Retain blocking cases:
- `CASE-P2-TD-SYSTEM-DETERMINISM-001`
- `CASE-P2-TD-SYSTEM-DUPLICATE-001`
- `CASE-P2-TD-SYSTEM-FORWARD-REF-001`
- `CASE-P2-TD-SYSTEM-OWNERSHIP-SNAPSHOT-001`
- `CASE-P2-TD-SYSTEM-VERSION-IDENTITY-001`
- `CASE-P2-TD-BM-CANONICAL-PAIR-001` — now compares BM-R17 YAML/MD semantics.

## 5. AC-002 / AC-003 RuleView / RuleKey / compatibility

Retain:
- `CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001`
- `CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001`
- `CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001`
- `CASE-P2-TD-RULEVIEW-VIEW-RESOLUTION-001`
- `CASE-P2-TD-RULEKEY-CONTRACT-001`
- `CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001`
- `CASE-P2-TD-KEY-SOURCE-COMPAT-001`
- `CASE-P2-TD-BARE-NAME-COMPATIBILITY-BOUNDARY-001`

## 6. TargetKey / ModelPath exact mapping

### CASE-P2-TD-TARGETKEY-SOURCE-MAPPING-001 — BLOCKING
Given two Systems with same local sourceModel plus multiple sourcePaths:

- `(SystemA,"order")` repeatedly resolves to one value-equal TargetKey;
- `(SystemB,"order")` resolves to a different TargetKey;
- changing only sourcePath does not change TargetKey;
- unknown/case-mismatch/ambiguous/cross-System sourceModel => stable source-aware ERROR and publication=0;
- exact TargetKey enters `ModelAccessRuleKey`; raw sourceModel is not consulted by PolicyIndex/Bridge/Guard.

### CASE-P2-TD-TARGET-PATH-ORTHOGONALITY-001 — BLOCKING
Verify `sourceModel -> TargetKey` and `sourcePath -> ModelPath` are independent conversion axes; path cannot select target and target lookup cannot broaden path.

Retain:
- `CASE-P2-TD-MODEL-PATH-UNKNOWN-001`
- `CASE-P2-TD-WILDCARD-FINITE-EXPANSION-001`
- `CASE-P2-TD-MODEL-PATH-CROSS-CONSUMER-EQUIVALENCE-001`
- `CASE-P2-TD-P1-PATH-OPERATION-MIGRATION-001`

## 7. AC-004 READ / WRITE only

### CASE-P2-TD-ACCESS-READ-WRITE-MATRIX-001 — BLOCKING

```text
READ-only : READ eligible, WRITE DENY
WRITE-only: WRITE eligible, READ DENY
no policy : READ DENY, WRITE DENY
```

Retain `CASE-P2-TD-NO-EXECUTE-CONTRACT-001` and `CASE-P2-TD-STATIC-DENY-001`.

## 8. Policy classification truth table — new blocker

### CASE-P2-TD-POLICY-CLASSIFICATION-TRUTH-TABLE-001 — BLOCKING
Table-drive all combinations of:
- `PolicyStatus={STATIC_ALLOW,RUNTIME_GUARD_REQUIRED}`;
- `RuntimeAccessRequirement={NONE,EXACT_RUNTIME_BINDING}`;
- plan absent/present.

Only valid rows:

```text
STATIC_ALLOW           + NONE                  + plan absent
RUNTIME_GUARD_REQUIRED + EXACT_RUNTIME_BINDING + plan present
```

Every other row must fail compiler construction/publication deterministically. `ModelAccessPolicyIndex.of` must independently reject a malformed row rather than repair/reclassify it.

### CASE-P2-TD-RUNTIME-PLAN-EXACT-BINDING-001 — BLOCKING
For runtime-required rule, plan targetKey/modelPath/targetView/selector must equal selected rule context. A plan that points to another target/path/view/selector is rejected before operation; proof cannot reselect rule/op.

## 9. AC-006 dynamic runtime

Retain:
- `CASE-P2-TD-DYNAMIC-CLASSIFIER-REAL-001`
- `CASE-P2-TD-RUNTIME-BINDING-PROOF-001`
- `CASE-P2-TD-RUNTIME-PLAN-MISMATCH-001`
- `CASE-P2-SOURCE-TO-READ-WRITE-OPERATION-001-R20` — real source -> parser/raw -> TargetKey/ModelPath -> legal policy classification -> PolicyIndex -> Context -> production Bridge -> Guard -> actual READ/WRITE operation; no hand-built policy/capability.

## 10. Real protected operation semantics — new blockers

### CASE-P2-TD-REAL-READ-OPERATION-001 — BLOCKING
Acquire production composition and use a real/controlled runtime object with a known canonical value.

Authorized READ oracle:
- operation adapter receives exact capability-bound RuntimeObjectId + ModelPath once;
- returned `ProtectedReadValue` contains exact object/path/value snapshot;
- `ProtectedAccessResult = ALLOW + READ + readValue`;
- writeReceipt absent;
- mutation/write invocation count = 0.

Denied READ oracle:
- operation adapter invocation=0;
- readValue/writeReceipt absent;
- denial present and stable.

### CASE-P2-TD-REAL-WRITE-OPERATION-001 — BLOCKING
Create one current frame/owner execution state containing one internally resolvable write intent. Caller supplies no callback/raw operation port.

Authorized WRITE oracle:
- resolver binds exact RuntimeWriteIntentId + target/path;
- Guard ALLOW occurs before operation adapter;
- adapter applies exact mutation once;
- returned receipt contains target/path/invocation/writeIntent IDs;
- result = ALLOW+WRITE+receipt; readValue absent.

Denied/stale/target-substituted/consumed WRITE:
- adapter=0; mutation=0; receipt/readValue absent; denial stable.

### CASE-P2-TD-OPERATION-PORT-NOT-CALLER-INJECTABLE-001 — BLOCKING
Public Bridge/neutral port/composition API exposes no raw operation callback/operation execution port. Caller cannot replace operation after Guard or bind WRITE to a different target/path/intent.

## 11. AC-007 Option B

Retain blocking cases:
- `CASE-P2-TD-PRODUCTION-SEAM-NO-LEGAL-BYPASS-001`
- `CASE-P2-TD-AC007-PRODUCTION-COMPOSITION-001`
- `CASE-P2-TD-AC007-RULE-CONSUMER-INTEGRATION-001`
- `CASE-P2-TD-AC007-CHANGE-CONSUMER-INTEGRATION-001`
- `CASE-P2-TD-AC007-CUSTOM-ACTION-CONSUMER-INTEGRATION-001`
- `CASE-P2-TD-AC007-CONSUMER-PARITY-001`
- `CASE-P2-TD-AC007-REPRESENTATIVE-CONSUMER-STRUCTURE-001`
- `CASE-P2-TD-AC007-REAL-PRODUCTION-REACHABILITY-001`

## 12. Downstream module direction — new blocker

### CASE-P2-TD-DOWNSTREAM-DEPENDENCY-DIRECTION-001 — BLOCKING
Static/module dependency oracle:

```text
P3/P4/P6 core may depend on dec-core-context ProtectedAccessPort
P3/P4/P6 core must not depend on dec-core-starter
starter may depend on context and implement the port
application/demo may depend on starter composition
```

Also assert neutral context contract imports/exposes no Gateway/Guard/capability/production composition/mutable PolicyIndex implementation type.

## 13. Publication / diagnostics / concurrency / compatibility

Retain:
- `CASE-P2-TD-ATOMIC-PUBLICATION-001`
- `CASE-P2-TD-CONTEXT-ISOLATION-001`
- `CASE-P2-TD-POLICY-INDEX-PUBLICATION-001`
- `CASE-P2-TD-DIAGNOSTIC-DETERMINISM-001`
- `CASE-P2-TD-RUNTIME-DENIAL-DIAGNOSTIC-DETERMINISM-001`
- `CASE-P2-TD-CAPABILITY-CONCURRENT-CONSUME-001`
- `CASE-P2-TD-DIFFERENT-CAPABILITY-CONCURRENCY-001`
- `CASE-P2-TD-DECLARATION-BOUNDARY-001`

Capability concurrency uses latch/barrier, never sleep; same capability authorized race yields exactly one protected operation total and at most one WRITE mutation.

## 14. Review Gate

- Overlay R04, BM-R17, FLOW-R07, DESIGN-P2-R19 require exact Reviews;
- risk detection/lifecycle remains required;
- all cases above are design cases only, not execution Evidence;
- Implementation Plan/TDD/Development remain BLOCKED.
