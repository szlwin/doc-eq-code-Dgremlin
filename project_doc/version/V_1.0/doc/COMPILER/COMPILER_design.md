# COMPILER P2 详细设计

> Revision：`DESIGN-P2-R21`。Base：`DESIGN-P2-R20`。
> Inputs：`REQAN-P2-R01@d08612768131` + Overlay R04 + `BM-R19` + `FLOW-R09@p2-system-ruleview-protected-access`。
> Decisions：Direct Bridge ACTIVE；AC-007 Option B ACTIVE；READ/WRITE-only ACTIVE。
> Status：`NEEDS_REVIEW / BLOCKED_BY_BM_REVIEW / MACHINE_BLOCKED`。

R21 保留 R20 已独立 Review 确认正确的 P1 TargetKey、structured artifacts、READ/WRITE-only、policy truth table、neutral dependency direction、WRITE 0/1/N 和 RuntimeFactValue 方向；只补 current API 自包含性、WRITE authority/path、runtime locator、transaction failure 与 multi-capability concurrency。

<a id="p2-revision-dag"></a>
## 1. Revision DAG

`Overlay R04 -> BM-R19 -> FLOW-R09 -> DESIGN-P2-R21 -> TESTDESIGN-P2-R22`。

<a id="p2-target-key"></a>
## 2. P1-compatible target identity

```text
sourceModel -> existing shared ViewKey -> TargetKey(ViewKey)
sourcePath  -> ModelPathCompiler -> exact ModelPath
```

`ModelAccessRuleKey = authorizationOwnerSystemKey + TargetKey + ModelPath + AccessOperation(READ|WRITE)`。TargetKey 不 System-qualify shared source View。

<a id="p2-policy-classification"></a>
## 3. Policy truth table

仅允许：

```text
STATIC_ALLOW           + NONE                  + no plan
RUNTIME_GUARD_REQUIRED + EXACT_RUNTIME_BINDING + plan
```

Compiler 与 PolicyIndex construction 都必须 reject 其他组合；runtime 不 repair/reclassify。

<a id="current-api-contract"></a>
## 4. Current revision API is self-contained

R21 的完整 current API 签名以 `COMPILER_api_contract.md@DESIGN-P2-R21` 为准；实现者不需要读取 R19/R20 才能恢复接口。当前 revision 明确冻结 `ModelAccessRuleKey / ModelAccessPolicyIndex / CompiledModelAccessRule`、typed frame/owner/optional cursor、ProtectedAccess invocation/result/resolved access、RuntimeModelOperationPort、Bridge/Factory/Composition 与 read/write/denial results。

<a id="p2-write-authority"></a>
## 5. WRITE authority and typed context

Starter-owned `WriteIntentResolver` input is `ModelAccessRuleKey + RuntimeExecutionFrameId + RuntimeResolutionOwnerId + Optional<RuntimeCollectionCursorId>`。`RuleKey` is optional provenance only. 0/N candidates fail closed；exactly one becomes immutable `ResolvedWriteIntent` before Guard。

<a id="p2-single-write-path"></a>
## 6. Single path authority

`ResolvedWriteIntent` carries exactly one `ModelAccessRuleKey`; it has no separate TargetKey/ModelPath fields. `ResolvedProtectedWriteAccess` carries invocationId + runtimeObjectId + writeIntent only.

Production port is:

```java
RuntimeFactValue read(ResolvedProtectedReadAccess access);
ProtectedWriteReceipt write(ResolvedProtectedWriteAccess access);
```

There is no `write(intent, objectId, path)` overload, so target/path/operation substitution after Guard is structurally impossible.

<a id="runtime-object-locator"></a>
## 7. RuntimeObjectId locator

Existing runtime facts are explicit: `ModelLoader` holds `ModelData`; `ModelData` is mutable; `ModelContainer` owns connection commit/rollback/close. R21 adds no global object registry.

`dec-core-model` introduces a composition/frame-scoped `RuntimeModelSession`:

```text
build session -> register RuntimeObjectId -> ModelData/runtime handle -> seal locator table -> protected execution -> close session
```

Only starter production assembly creates/binds the session；registration is pre-seal only；lookup after seal is deterministic/thread-safe；missing=`RUNTIME_OBJECT_NOT_FOUND`；closed/different/stale=`RUNTIME_OBJECT_STALE`；RuntimeObjectId cannot cross composition/session boundaries。

<a id="p2-transactional-write"></a>
## 8. WRITE transaction / rollback

Guard ALLOW 后 production adapter executes one exact object/path transaction：acquire serialization boundary -> compare current RuntimeMutationVersion to intent.expectedVersion -> stale mismatch denies with zero mutation -> apply frozen mutation to isolated working state -> commit model/data-source transaction -> publish committed state -> increment version -> receipt。

Any mutation/transaction/commit exception triggers rollback or working-copy restore。Externally observable ModelData/origin equals pre-write state；receipt absent；capability remains CONSUMED；no automatic retry/reselection。

Because current `ModelContainer.execute()` can copy successful values to origin before `end(commit)`, P2 implementation must defer external publication until commit succeeds or preserve/restore a pre-write snapshot。

<a id="p2-concurrency"></a>
## 9. Different-capability concurrency

`RuntimeModelSession` maintains one monotonic `RuntimeMutationVersion` per `(RuntimeObjectId, ModelPath)` plus a serialization primitive。If two different capabilities freeze the same version then race the same object/path, at most one sees the expected current version and commits；winner increments version once；loser returns `WRITE_INTENT_STALE`, mutation=0, receipt absent。Winner identity may be scheduler-dependent, but the oracle is deterministic：exactly one commit and no partial/lost update。Different object/path keys may proceed independently。

<a id="p2-production-composition"></a>
## 10. Production composition and dependency direction

```text
P3/P4/P6 core -> dec-core-context : allowed
P3/P4/P6 core -> dec-core-starter : forbidden

dec-core-model -> dec-core-context : existing
starter -> compiler/frontends       : existing
starter -> dec-core-model           : planned production assembly
```

`ProtectedAccessRuntimeFactory` binds one EngineContext + one sealed RuntimeModelSession + one production model adapter to one composition。Rule/Change/CustomAction entries share the same bridge/context/session。

## 11. Gate

No production Java/TDD is claimed。Current risk scan and same-revision ApiContract/Architecture/Develop/Impact/CrossModule/Concurrency/TestDesign Reviews remain required。Implementation Plan/TDD/Development remain BLOCKED。
