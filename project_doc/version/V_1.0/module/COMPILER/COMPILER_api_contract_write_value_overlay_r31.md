# COMPILER P2 API Contract — WRITE Value Transport Overlay

> Revision `DESIGN-P2-R31`; base `DESIGN-P2-R30`.
> This overlay changes only the neutral WRITE-value transport needed to close `P1-P2-DEV07-WRITE-VALUE-CONTRACT-GAP-001`. R30 remains normative for every other contract.

## Additive CONTEXT surface

```java
package dec.core.context.runtime;

public final class ProtectedAccessInvocation {
  // R30/R29 compatibility factory remains unchanged.
  public static ProtectedAccessInvocation of(
      ProtectedInvocationId id,
      ModelAccessRuleKey key,
      RuntimeExecutionFrameId frameId,
      RuntimeResolutionOwnerId ownerId,
      Optional<RuntimeCollectionCursorId> cursorId);

  // New R31 WRITE factory. RuntimeFactValue is data only, never authority.
  public static ProtectedAccessInvocation write(
      ProtectedInvocationId id,
      ModelAccessRuleKey key,
      RuntimeExecutionFrameId frameId,
      RuntimeResolutionOwnerId ownerId,
      Optional<RuntimeCollectionCursorId> cursorId,
      RuntimeFactValue writeValue);

  public Optional<RuntimeFactValue> writeValue();
}

public final class ResolvedWriteIntent {
  // R30/R29 compatibility factory remains unchanged.
  public static ResolvedWriteIntent of(
      RuntimeWriteIntentId id,
      ModelAccessRuleKey key,
      Optional<RuleKey> provenance,
      ResolvedRuntimeTarget target,
      RuntimeMutationStamp stamp);

  // New R31 frozen WRITE intent.
  public static ResolvedWriteIntent of(
      RuntimeWriteIntentId id,
      ModelAccessRuleKey key,
      Optional<RuleKey> provenance,
      ResolvedRuntimeTarget target,
      RuntimeMutationStamp stamp,
      RuntimeFactValue writeValue);

  public Optional<RuntimeFactValue> writeValue();
}

public final class ResolvedProtectedWriteAccess {
  public static ResolvedProtectedWriteAccess of(
      ProtectedInvocationId invocationId,
      ResolvedWriteIntent intent);
  public RuntimeFactValue value(); // non-null only for executable R31 WRITE intent
}
```

## Guard/effect rules

- `RuntimeFactValue` never affects policy lookup or target selection.
- Exact authority remains `ModelAccessRuleKey(owner,target,path,operation)`.
- STARTER freezes the same immutable value into the same one-shot write intent as target/path/version.
- A missing WRITE value is rejected before the MODEL operation port.
- The operation port remains composition-private and retains its DEV-06 same-session/object/handle/stamp revalidation.
- The successful consumer `ProtectedWriteReceipt` remains the R30/R29 receipt shape; STARTER may translate the MODEL-internal target/path/version receipt to the consumer invocation/intent/version receipt after a successful effect only.

No public overload may accept `ModelData`, `Container`, `RuntimeModelEffectProvider`, `RuntimeModelOperationPort` or a Guard.
