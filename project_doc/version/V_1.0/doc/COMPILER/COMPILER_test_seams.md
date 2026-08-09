# COMPILER P2 Test Seams

> Revision：`DESIGN-P2-R16`  
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED / AC007_PENDING_USER_DECISION`

## 1. System version seam

Observe `SystemVersionIdentity` as immutable values:
- declaredVersion present/absent；
- sourceSemanticDigest deterministic；
- schemaVersion == enclosing CompiledModelSet schemaVersion；
- compilerVersion == enclosing CompiledModelSet compilerVersion；
- no fabricated time/order/random value。

## 2. Ownership truth-source seam

Tests must be able to compare one `CompiledSystem` snapshot against:
- final typed Data/View/RuleView/Information registries；
- final CompiledRuleView rule closure；
- final ModelAccessPolicyIndex keys。

Negative setup must support orphan/missing/foreign snapshot facts before publication. Snapshot itself must not be writable/rebuild authoritative sources.

## 3. RuleView compatibility/resolution seam

Observe existing `RuleViewKey(SystemKey,String)`, `owner()`, `name()` plus additive aliases, and exact `CompiledRuleView.resolvedViewKey()/resolvedRuleKeys()`.

## 4. P1→P2 conversion seam

Provide controlled source facts for:
- exact `SharedModelPath` -> exact `ModelPath`；
- wildcard `SharedModelPath("*")` -> stable finite exact paths；
- `AccessMode.READ/WRITE` -> exact AccessOperation；
- proof that EXECUTE is never inferred；
- proof that runtime PolicyIndex/Bridge/Guard no longer reads P1 types as authority。

## 5. Business Flow seams

`FLOW-CONFIG-COMPILE` counters/observations: source discovery, symbol registration, reference resolution, compatibility conversion, PolicyIndex construction, ownership derivation, digest, publication。

`FLOW-PROTECTED-ACCESS-EXECUTE` counters/observations: Bridge invocation, issued invocation, target resolution, capability mint, Gateway, Guard lookup/proof, operation/effect, denial provenance。

## 6. Operation independence seam

For same System/target/path, independently seed READ-only, WRITE-only, EXECUTE-only exact policy. No `hasAnyPermission(path)` shortcut is acceptable.

## 7. Runtime binding / one-shot seam

Controllable current frame/owner/cursor/membership and target resolver; ability to attempt target substitution and concurrent same-capability consume without `Thread.sleep`.

## 8. Denial determinism seam

Repeat identical immutable-context failure and compare code/System/optional RuleView/op/ModelPath/policy SourceRef; ensure actual sensitive value/object dump is absent.

## 9. AC-007 decision-aware seam

The common no-bypass seam can be tested now, but `CASE-P2-TD-PRODUCTION-SEAM-NO-LEGAL-BYPASS-001` cannot by itself close original AC-007 until user chooses Option A. If user chooses Option B, TestDesign must additionally identify concrete production consumer classes/adapters and executable integration oracles.

## 10. TDD validity

Formal bootstrap may use `-am`; target RED command must not use `-am` and uses `-Dsurefire.failIfNoSpecifiedTests=true`. Missing class/symbol/setup/compile failure before intended assertion is `INVALID_RED`, not valid failing TDD evidence.

## 11. Gate

No skeleton/tests are executed by this Design artifact. Exact Testability/ApiContract/Architecture Review and AC-007 user decision remain blocking.
