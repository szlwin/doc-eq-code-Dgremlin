# COMPILER P2 Test Seams

> Design Revision：`DESIGN-P2-R19`。  
> Test Design Revision：`TESTDESIGN-P2-R20`。  
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`。

## 1. Revision DAG seam

A document-contract test must assert the authoritative chain is acyclic:

```text
R04 -> BM-R17 -> FLOW-R07 -> DESIGN-P2-R19 -> TESTDESIGN-P2-R20
```

`FLOW-R07.authoritativeInputs` must not contain Design/TestDesign. `BM-R17` must not declare Flow/Design/TestDesign as authoritative input. Downstream trace refs are allowed but non-authoritative.

## 2. TargetKey seam

Compiler fake/fixture supplies exact owner System target namespace. Verify:

- same `(System,sourceModel)` -> value-equal TargetKey;
- same local model under different Systems -> distinct TargetKeys;
- unknown/ambiguous/cross-System sourceModel -> stable ERROR;
- changing sourcePath does not change TargetKey;
- changing sourceModel does not silently reuse prior TargetKey;
- runtime PolicyIndex key uses converted TargetKey, never raw sourceModel string.

## 3. Policy classification seam

Table-driven construction/publication test for all `PolicyStatus x RuntimeAccessRequirement x runtimePlanPresent` combinations. Only:

```text
STATIC_ALLOW + NONE + absent plan
RUNTIME_GUARD_REQUIRED + EXACT_RUNTIME_BINDING + present plan
```

are valid. Compiler and `ModelAccessPolicyIndex.of` must reject every other row before runtime.

## 4. Real READ seam

Use a controlled production operation adapter with one concrete runtime object/path/value. Obtain invocation through real production composition/Bridge. For allowed READ assert:

- exact target/path passed to operation adapter;
- adapter read called exactly once;
- returned `ProtectedReadValue` contains exact object/path/value snapshot;
- write adapter/mutation count = 0;
- `ProtectedAccessResult` is ALLOW+READ with readValue only.

For denied READ assert operation adapter is never called and result contains denial only.

## 5. Real WRITE seam

Use a controlled runtime execution state that resolves one concrete `RuntimeWriteIntentId` and one target/path mutation. Caller does not provide a callback/operation port.

Allowed WRITE:
- exact capability-bound target/path/writeIntent reaches operation adapter once;
- mutation occurs exactly once;
- result is ALLOW+WRITE with one `ProtectedWriteReceipt` and no readValue.

Denied/consumed/stale WRITE:
- operation adapter call=0;
- mutation=0;
- receipt absent.

## 6. Neutral dependency direction seam

Static/build dependency test asserts:

```text
P3/P4/P6 core -> dec-core-context ProtectedAccessPort : allowed
P3/P4/P6 core -> dec-core-starter                  : forbidden
```

Starter may depend on context and implement `ProtectedAccessPort`. Demo/application may depend on starter composition.

## 7. AC-007 Option B seam

Production E2E must obtain Rule/Change/CustomAction entries via `ProtectedAccessRuntimeFactory -> ProtectedAccessComposition`, verify same Bridge/Context, then run allow/deny through each. Manual `new Entry(testBridge)` is not production reachability Evidence.

## 8. Capability concurrency seam

Use latch/barrier, not sleep. Two threads race the same real capability. Atomic consume winner <=1; actual protected operation <=1; WRITE mutation <=1; loser stable `CAPABILITY_ALREADY_CONSUMED` and no result/effect.

## 9. Runtime proof seam

For runtime-required rules, exact RuntimeBindingPlan is passed to proof verifier. Proof may validate membership/staleness only; cannot switch rule/TargetKey/ModelPath/READ-WRITE operation.

## 10. Formal RED

```bash
./mvnw -pl <EXACT-MODULE> -am -Dmaven.test.skip=true install
./mvnw -pl <EXACT-MODULE> -Dtest=<EXACT-TESTCLASS> -Dsurefire.failIfNoSpecifiedTests=true test
```

Second command MUST NOT use `-am`; missing class/symbol/setup/compile failure before intended assertion = `INVALID_RED`.
