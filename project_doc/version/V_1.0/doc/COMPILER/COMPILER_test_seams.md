# COMPILER P2 Test Seams

> Revision：`DESIGN-P2-R17`  
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`

## 1. System version seam

Observe `SystemVersionIdentity` as immutable values：declaredVersion present/absent、sourceSemanticDigest deterministic、schemaVersion == enclosing CompiledModelSet schemaVersion、compilerVersion == enclosing CompiledModelSet compilerVersion、no fabricated time/order/random value。

## 2. Ownership truth-source seam

Tests compare one `CompiledSystem` snapshot against：
- final typed Data/View/RuleView/Information registries；
- final CompiledRuleView rule closure；
- final ModelAccessPolicyIndex keys。

Negative setup supports orphan/missing/foreign snapshot facts before publication。Snapshot cannot be writable/rebuild authoritative sources。

## 3. RuleView compatibility/resolution seam

Observe existing `RuleViewKey(SystemKey,String)`、`owner()`、`name()` plus additive aliases and exact `CompiledRuleView.resolvedViewKey()/resolvedRuleKeys()`。

## 4. P1→P2 conversion seam

Controlled source facts for exact SharedModelPath -> exact ModelPath、wildcard -> stable finite paths、AccessMode.READ/WRITE -> exact AccessOperation、EXECUTE never inferred、runtime PolicyIndex/Bridge/Guard no longer read P1 types as authority。

## 5. Business Flow seams

`FLOW-CONFIG-COMPILE` counters：source discovery、symbol registration、reference resolution、compatibility conversion、PolicyIndex construction、ownership derivation、digest、publication。

`FLOW-PROTECTED-ACCESS-EXECUTE` counters：representative entry、Bridge invocation、issued invocation、target resolution、capability mint、Gateway、Guard lookup/proof、operation/effect、denial provenance。

## 6. AC-007 Option B representative consumer seams

The test environment must be able to instantiate and execute the **real main-source** production types frozen by Design R17：

- `RuleProtectedAccessEntry`；
- `ChangeProtectedAccessEntry`；
- `CustomActionProtectedAccessEntry`；
- immutable `ProtectedAccessInvocation`。

### 6.1 Per-consumer allow/deny observation

For each of the three entries, instrumentation must observe：

```text
entry calls
bridge calls
issued invocation count
target resolution count
capability mint count
gateway calls
guard calls
operation calls
effect count
denial code/provenance
```

Authorized case must reach exactly one capability-bound operation/effect per invocation；unauthorized case must have operation/effects=0 and stable DENY。

### 6.2 Consumer parity seam

Run the same immutable Context + same exact `ProtectedAccessInvocation` through all three production entries。Compare authorization classification and stable denial code/facts。Consumer kind may appear in provenance but must not alter `ModelAccessRuleKey`、AccessOperation、Guard lookup or target binding。

### 6.3 Structural no-bypass seam

Test/Review must be able to verify each representative entry's production constructor/dependency surface：
- allowed protected authority dependency: `ProtectedExecutionBridge` only；
- forbidden: Gateway、Guard、resolver、raw operation port、mutable/secondary PolicyIndex、issued-pair/capability mint。

API-shape reflection/source inspection may be used only to assert structure；the executable AC-007 allow/deny path itself must call the public production entries normally, not invoke internals through reflection。

### 6.4 Real fixture seam

`dec-demo`/cross-module fixture must compile real P2 source to a real Context and execute all three public representative entries without hand-built PolicyIndex/manual issued pair/capability/test-only consumer。

## 7. Operation independence seam

For same System/target/path, independently seed READ-only、WRITE-only、EXECUTE-only exact policy。No `hasAnyPermission(path)` shortcut。

## 8. Runtime binding / one-shot seam

Controllable current frame/owner/cursor/membership and target resolver；attempt target substitution and concurrent same-capability consume without `Thread.sleep`。

## 9. Denial determinism seam

Repeat identical immutable-context failure and compare code/System/optional RuleView/op/ModelPath/policy SourceRef；same authorization facts across three representative consumers must have same authorization denial classification；no sensitive actual value/object dump。

## 10. TDD validity

Formal bootstrap may use `-am`；target RED command must not use `-am` and uses `-Dsurefire.failIfNoSpecifiedTests=true`。Missing class/symbol/setup/compile failure before intended assertion is `INVALID_RED`。

## 11. Gate

No skeleton/tests are executed by this Design artifact。AC-007 user decision is satisfied by Option B；exact Testability/ApiContract/Architecture/Impact/CrossModule/Concurrency Reviews and machine risk scan remain blocking。
