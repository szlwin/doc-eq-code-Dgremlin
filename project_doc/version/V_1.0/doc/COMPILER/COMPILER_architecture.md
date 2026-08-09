# COMPILER P2 Architecture

> Revision：`DESIGN-P2-R19`。Base：`DESIGN-P2-R18`。  
> Inputs：Overlay R04 + BM-R17 + FLOW-R07。  
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`。

## 1. Authoritative artifact direction

```text
Requirement/Decisions -> BM-R17 -> FLOW-R07 -> DESIGN-P2-R19 -> TESTDESIGN-P2-R20
```

No downstream artifact is an authoritative input of an upstream artifact. Downstream links inside Flow are trace-only.

## 2. Module ownership

### `dec-core-context`
Neutral, immutable, non-assembly contracts only:

- SystemKey / RuleViewKey / RuleKey
- TargetKey / ModelPath / AccessOperation
- PolicyStatus / RuntimeAccessRequirement / RuntimeBindingPlan
- ModelAccessRuleKey / CompiledModelAccessRule / ModelAccessPolicyIndex
- ProtectedAccessInvocation / ProtectedAccessResult / ProtectedAccessPort
- opaque runtime IDs / non-authority value/result contracts

It MUST NOT depend on `dec-core-starter` and MUST NOT construct Gateway/Guard/capability/production composition.

### `dec-core-compiler`
Owns source parsing/semantic conversion:

```text
sourceModel -> exact owner-System TargetKey
sourcePath  -> shared exact ModelPath
AccessMode  -> READ/WRITE AccessOperation
source selector/classification -> legal PolicyStatus + RuntimeAccessRequirement + RuntimeBindingPlan
```

Compiler rejects unknown/ambiguous/cross-System target mappings and illegal policy truth-table combinations before publication.

### `dec-core-starter`
Owns runtime authority assembly and enforcement:

- `ProtectedExecutionBridge implements ProtectedAccessPort`
- production `ProtectedAccessRuntimeFactory/ProtectedAccessComposition`
- exact target/runtime-state resolver
- starter-internal `ResolvedProtectedAccess` / one-shot capability
- atomic capability consume
- Gateway / Guard
- protected operation execution adapter

No caller receives capability/Gateway/Guard/resolver/operation port/mutable PolicyIndex authority.

### Future P3/P4/P6 core modules
Depend only on neutral `dec-core-context ProtectedAccessPort` contract. They MUST NOT declare a dependency on `dec-core-starter`. Application/starter composition injects a starter implementation at the outer assembly boundary.

## 3. Target/path authority split

```text
sourceModel ----------------> TargetKey(SystemKey,canonicalSourceModelName)
sourcePath --ModelPathCompiler--> ModelPath
```

Target lookup and path compilation are independent and must converge into one exact `ModelAccessRuleKey`. Path cannot select/change target and target cannot broaden path.

For runtime-bound access:

```text
RuleView resolved View + selector + resolved TargetKey + ModelPath
 -> immutable RuntimeBindingPlan
```

The plan is proof metadata for the already selected rule; runtime does not use it to choose another rule/target/path/op.

## 4. Policy classification boundary

Exactly two valid representations enter PolicyIndex:

```text
STATIC_ALLOW             + NONE                  + no RuntimeBindingPlan
RUNTIME_GUARD_REQUIRED   + EXACT_RUNTIME_BINDING + RuntimeBindingPlan
```

Every other combination is compiler/publication error. PolicyIndex revalidates; Guard never repairs invalid compiler output.

## 5. Runtime flow / real operation boundary

```text
Rule/Change/CustomAction Entry or direct neutral port caller
 -> ProtectedAccessPort
 -> starter Bridge
 -> internal invocation + actual target/runtime operation resolution
 -> one-shot capability
 -> atomic consume
 -> Gateway
 -> Guard exact PolicyIndex lookup/proof
 -> ProtectedOperationExecutionPort
      READ  -> immutable ProtectedReadValue, no mutation
      WRITE -> exactly one resolved mutation + ProtectedWriteReceipt
```

The protected operation adapter is invoked only after ALLOW and cannot be caller-supplied. WRITE intent comes from current frame/owner execution state and is bound into starter-internal resolved access before Guard/operation dispatch.

All DENY paths terminate before operation port invocation and expose no read result/write receipt.

## 6. AC-007 production composition

`ProtectedAccessRuntimeFactory.bind(EngineContext)` creates one immutable composition with one Bridge and Rule/Change/CustomAction entries bound to it. Manual entry construction is unit-test-only and cannot prove production reachability.

The three P2 representative entries prove Option B categories; future full P3/P4/P6 engines reuse neutral port without moving Gateway/Guard into core modules.

## 7. Concurrency

Capability is intentionally concurrent-reachable. Atomic state transition:

```text
ISSUED --CAS/equivalent Java-8 atomic primitive--> CONSUMED
```

At most one winner can invoke Guard/operation for the capability; loser fail-closes. Different capabilities may execute concurrently without cross-wiring target/frame/owner/path/op.

## 8. Publication / Context isolation

PolicyIndex, RuleView closure, TargetKey/ModelPath facts, ownership/version and semantic digest are frozen as one immutable publication closure. Failed candidate keeps old Context. No global mutable current registry/context/composition.

## 9. Architecture review blockers

R19 remains blocked until exact Architecture/API/Develop/Impact/CrossModule/Concurrency Review and current-revision risk detection. P0 build success is not semantic closure Evidence.
