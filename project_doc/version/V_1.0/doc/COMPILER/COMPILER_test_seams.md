# COMPILER P2 Test Seams

> Revision：`DESIGN-P2-R18`  
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`

## 1. Canonical BM pair seam

Review/test tooling can compare `COMPILER_business_model.yaml` machine facts with `.md` normative mirror for same revision/inputs/operation set/RuleKey/AC007/concurrency invariants。Mismatch invalidates candidate。

## 2. System ownership/version seam

Observe SystemVersionIdentity and compare CompiledSystem projections against typed Data/View/RuleView/Information registries、CompiledRuleView RuleKey closures and PolicyIndex keys。No snapshot mutation/rebuild authority。

## 3. RuleKey seam

Create two RuleViews with same local Rule name and prove distinct RuleKey identities by owner。Within one RuleView duplicate local RuleKey rejects。Every resolved RuleKey owner equals CompiledRuleView key。No global Rule registry required。

## 4. READ/WRITE-only operation seam

Observe `AccessOperation.values()` exactly `[READ, WRITE]` modulo enum declaration order contract；there is no current P2 EXECUTE source/raw/policy/runtime branch。Seed READ-only and WRITE-only policy independently and prove non-implication。

## 5. ModelPath/P1 migration seam

Controlled exact and wildcard SharedModelPath inputs convert once into exact P2 ModelPaths；AccessMode READ/WRITE converts one-to-one。After conversion PolicyIndex/Bridge/Guard observations never read P1 path/mode authority。

## 6. Production composition seam — AC-007

Normal starter production composition must expose/acquire:

```text
ProtectedAccessRuntimeFactory
 -> ProtectedAccessComposition
      -> bridge
      -> ruleEntry
      -> changeEntry
      -> customActionEntry
```

Test seam must verify all three entries are bound to the same Bridge and EngineContext。Real AC-007 E2E must acquire via this composition, not `new Entry(testBridge)` or reflection/package-private internals。

## 7. Authority dependency seam

For each representative entry, inspect constructor/field/dependency graph：Bridge is the only protected-access authority dependency。Gateway/Guard/resolver/raw operation/mutable PolicyIndex/issued-pair/capability mint absent from business-entry reachability。

## 8. Runtime target/proof seam

Controllable frame/owner/cursor/target/membership enables valid/stale/foreign/wrong target paths。A capability cannot substitute target or READ/WRITE operation。

## 9. Atomic capability concurrency seam

Expose a controlled starter-internal test seam capable of racing the **same issued capability** at the Gateway using `CountDownLatch`/barrier (no `Thread.sleep`)。

Oracle：

- atomic ISSUED->CONSUMED success count <=1；
- Guard/operation/effect for that capability <=1；
- losing consume = stable `CAPABILITY_ALREADY_CONSUMED` DENY；
- losing operation/effect=0；
- later sequential reuse same denial。

## 10. Denial determinism seam

Repeat equal immutable-context failure and compare code/System/optional RuleView/READ-or-WRITE/ModelPath/policy SourceRef；no sensitive actual value。

## 11. Bare-name compatibility seam

P2 new canonical public resolver must not expose bare-name lookup。If historical compatibility path exists, verify read-only/no Registry or Policy mutation/no protected WRITE and ambiguous same-name reject。

## 12. TDD validity

Bootstrap command may use `-am`; target RED command must not use `-am` and must set `-Dsurefire.failIfNoSpecifiedTests=true`。Missing class/symbol/setup/compile before intended assertion = `INVALID_RED`。

## 13. Gate

No skeleton/tests are executed by this Design artifact。Exact Testability/ApiContract/Architecture/Concurrency Review and machine risk scan remain blocking。
