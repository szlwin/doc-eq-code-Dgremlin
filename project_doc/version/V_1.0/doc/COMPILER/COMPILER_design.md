# COMPILER P2 详细设计

> Revision：`DESIGN-P2-R20`。Base：`DESIGN-P2-R19`。
> Inputs：`REQAN-P2-R01@d08612768131` + Overlay R04 + `BM-R18` + `FLOW-R08@p2-system-ruleview-protected-access`。
> Decisions：Direct Bridge ACTIVE；AC-007 Option B ACTIVE；READ/WRITE-only ACTIVE。
> Status：`NEEDS_REVIEW / BLOCKED_BY_BM_REVIEW / MACHINE_BLOCKED`。

R20 保留 R19 的 PolicyIndex、Option B、one-shot capability、neutral port、real READ/WRITE 方向，修复独立 Review 指出的 P1 source identity、WRITE intent 0/1/N、production operation adapter 和 value/error contract。它不是 PASSED lifecycle revision。

<a id="p2-revision-dag"></a>
## 1. Revision DAG

`Overlay R04 -> BM-R18 -> FLOW-R08 -> DESIGN-P2-R20 -> TESTDESIGN-P2-R21`。只允许 upstream authoritative input。

<a id="p2-target-key"></a>
## 2. TargetKey preserves P1 shared source View identity

```java
public final class TargetKey {
    public TargetKey(ViewKey sourceViewKey);
    public static TargetKey of(ViewKey sourceViewKey);
    public ViewKey sourceViewKey();
}
```

Compiler mapping：

```text
sourceModel -> ViewKey(sourceModel) -> existing SymbolTable.find(ViewKey)
            -> TargetKey(shared source ViewKey)
sourcePath  -> ModelPathCompiler -> exact ModelPath
```

The authorization owner System is **not** part of TargetKey. Policy identity is:

```java
ModelAccessRuleKey(
    SystemKey authorizationOwnerSystemKey,
    TargetKey sourceTargetKey,
    ModelPath modelPath,
    AccessOperation operation)
```

`targetView + selector + resolvedTarget` remain separate owner-System runtime-binding facts. Missing shared source View => stable compile ERROR. R20 does not introduce a new System-qualified source View namespace; such a migration would require a Requirement/Decision first.

<a id="p2-model-path"></a>
## 3. ModelPath / READ-WRITE

`TargetKey` and `ModelPath` are orthogonal. Runtime wildcard is forbidden. `AccessOperation` enum is exactly `READ, WRITE`; no EXECUTE source/raw/enum/policy/runtime/TestDesign contract.

<a id="p2-policy-classification"></a>
## 4. Policy classification

Only:

```text
STATIC_ALLOW           + NONE                  + plan absent
RUNTIME_GUARD_REQUIRED + EXACT_RUNTIME_BINDING + plan present
```

may enter immutable `ModelAccessPolicyIndex`. Compiler construction and `ModelAccessPolicyIndex.of(...)` both reject malformed combinations; runtime never repairs/reclassifies.

<a id="p2-neutral-protected-port"></a>
## 5. Neutral contracts

`dec-core-context` owns public immutable neutral contracts only: `ProtectedAccessPort`, invocation/result, `TargetKey`, `ModelPath`, `RuntimeFactValue`, `RuntimeObjectId`, `ProtectedInvocationId`, `RuntimeWriteIntentId`, and `RuntimeModelOperationPort`.

P3/P4/P6 core may depend on context contracts, never starter. Gateway/Guard/capability/production assembly stay starter-internal.

<a id="p2-write-intent"></a>
## 6. WRITE intent exact 0/1/N selection

Starter-owned `WriteIntentResolver` receives:

```text
ruleKey + TargetKey + ModelPath + frameId + ownerResolutionId + cursorId
```

and returns a finite candidate set.

- `0` -> `WRITE_INTENT_NOT_FOUND` DENY before capability/Guard;
- `1` -> create immutable `ResolvedWriteIntent` and embed its ID/facts in `ResolvedProtectedAccess` before capability/Guard;
- `N>1` -> `WRITE_INTENT_AMBIGUOUS` DENY before capability/Guard.

After the one candidate is frozen, Guard and operation execution consume that exact snapshot. Frame/owner/cursor changes cannot cause re-resolution. If a staleness proof detects a changed binding, result is DENY; never select a replacement intent.

<a id="p2-operation-binding"></a>
## 7. Capability / Guard ordering

```text
invocation
 -> resolve TargetKey/RuntimeObjectId/ModelPath/op
 -> resolve and freeze WRITE intent when WRITE
 -> mint one-shot capability over immutable ResolvedProtectedAccess
 -> atomic consume
 -> exact PolicyIndex / runtime proof Guard
 -> production operation adapter
```

Caller cannot inject raw operation callback/port, replace target/path/intent, or mutate authority after Guard.

<a id="production-runtime-model-operation"></a>
## 8. Production runtime model operation

Production closure is explicit:

```text
dec-core-context
  RuntimeModelOperationPort
       ^
       | implements
dec-core-model
  DefaultRuntimeModelOperationAdapter
       ^
       | wired by
dec-core-starter
  ProtectedOperationExecutionAdapter
  ProtectedAccessRuntimeFactory / Composition
```

`DefaultRuntimeModelOperationAdapter` resolves `RuntimeObjectId` against the actual runtime model store, traverses canonical `ModelPath`, and performs:

- READ: snapshot current value into `RuntimeFactValue`;
- WRITE: apply only the frozen `ResolvedWriteIntent` mutation and return receipt after success.

A fake/controlled adapter is valid for unit tests only. Production reachability Evidence must obtain `ProtectedAccessComposition` through normal starter assembly and observe actual `dec-core-model` state/value. This is a design/dependency contract for the subsequent implementation phase; no production Java is claimed in this PR.

<a id="p2-runtime-value-contract"></a>
## 9. RuntimeFactValue and opaque IDs

Closed `RuntimeFactValue.Kind`:

```text
NULL BOOLEAN INTEGER DECIMAL STRING LIST OBJECT
```

Canonical rules: recursive deep snapshot; no live mutable references; INTEGER=`BigInteger`; DECIMAL=normalized `BigDecimal`; LIST ordered immutable; OBJECT immutable unique string keys with deterministic serialization order; structural equals/hash after canonicalization; deterministic JSON representation; arbitrary Java object forbidden.

`RuntimeObjectId`, `ProtectedInvocationId`, `RuntimeWriteIntentId` are immutable wrappers over one nonblank opaque String. `of(String)`/`value()` only; exact case-sensitive equals/hash; no semantic parsing or permission inference.

<a id="p2-runtime-denial"></a>
## 10. Result / denial contract

```text
ALLOW READ  -> ProtectedReadValue only
ALLOW WRITE -> ProtectedWriteReceipt only
DENY        -> ProtectedAccessDenial only
```

Stable denial codes include policy/proof mismatch, guard unavailable, capability consumed, `WRITE_INTENT_NOT_FOUND`, `WRITE_INTENT_AMBIGUOUS`, runtime target unavailable and runtime operation failure. Denial text/provenance is non-sensitive. DENY exposes no read value/write receipt and occurs before any protected effect.

<a id="p2-production-composition"></a>
## 11. AC-007 Option B production composition

`ProtectedAccessRuntimeFactory` constructs one `ProtectedAccessComposition` bound to one EngineContext and one production RuntimeModelOperationPort. Rule/Change/CustomAction representative entries share the same Bridge/context; manual test construction cannot prove production reachability.

<a id="p2-context"></a>
## 12. Atomic publication / compatibility

System/RuleView/RuleKey ownership, shared source View TargetKey, exact ModelPath, READ/WRITE policy classification, PolicyIndex, version/digest publish as one immutable closure or old Context remains. Existing source-compatible key APIs remain; P2 introduces no bare-name fallback and no EXECUTE.

<a id="p2-concurrency"></a>
## 13. Concurrency

One capability uses atomic `ISSUED -> CONSUMED`; at most one protected operation and one WRITE mutation. Tests use latch/barrier, never sleep.

## 14. Gate

R20 remains `NEEDS_REVIEW / BLOCKED_BY_BM_REVIEW / MACHINE_BLOCKED`. Risk scan, independent ApiContract/Architecture/Develop/Impact/CrossModule/Concurrency Review and implementation Evidence are still required. Implementation Plan/TDD/Development remain BLOCKED.
