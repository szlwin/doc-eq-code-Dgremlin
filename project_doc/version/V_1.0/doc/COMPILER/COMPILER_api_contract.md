# COMPILER P2 API Contract

> Revision：`DESIGN-P2-R20`。Base：`DESIGN-P2-R19`。
> Inputs：Overlay R04 + `BM-R18` + `FLOW-R08`。
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`。

## 1. Existing compatibility

Existing public SystemKey/RuleViewKey/EngineContext/CompiledModelSet source surfaces remain source-compatible. P2 adds no EXECUTE and no bare-name runtime permission fallback.

## 2. TargetKey

```java
public final class TargetKey {
    public TargetKey(ViewKey sourceViewKey);
    public static TargetKey of(ViewKey sourceViewKey);
    public ViewKey sourceViewKey();
}
```

Value identity is the existing canonical shared `ViewKey` resolved from P1 `sourceModel`. Authorization owner System is separate in `ModelAccessRuleKey`; local `targetView/selector/resolvedTarget` are separate binding facts.

## 3. ModelAccessRuleKey / policy classification

```java
public final class ModelAccessRuleKey {
    public SystemKey authorizationOwnerSystemKey();
    public TargetKey targetKey();
    public ModelPath modelPath();
    public AccessOperation operation(); // READ | WRITE only
}
```

Only `STATIC_ALLOW+NONE+no-plan` and `RUNTIME_GUARD_REQUIRED+EXACT_RUNTIME_BINDING+plan` are legal.

## 4. RuntimeFactValue

```java
public final class RuntimeFactValue {
    public enum Kind { NULL, BOOLEAN, INTEGER, DECIMAL, STRING, LIST, OBJECT }
    public Kind kind();
    // kind-specific immutable accessors; no raw arbitrary Object accessor
}
```

Contract: recursive deep snapshot; canonical BigInteger/normalized BigDecimal; ordered immutable LIST; immutable OBJECT with unique string keys and deterministic serialization order; structural equality/hash; deterministic JSON form; no live mutable runtime reference.

## 5. Opaque IDs

`RuntimeObjectId`, `ProtectedInvocationId`, `RuntimeWriteIntentId` are final immutable nonblank String wrappers with `of(String)` and `value()`, exact case-sensitive equals/hash, and no authority semantics.

## 6. Resolved write intent

```java
public final class ResolvedWriteIntent {
    public RuntimeWriteIntentId id();
    public RuleKey ruleKey();
    public TargetKey targetKey();
    public ModelPath modelPath();
    public String frameId();
    public String ownerResolutionId();
    public String cursorId();
}
```

`WriteIntentResolver` returns zero/one/multiple candidates. Zero => `WRITE_INTENT_NOT_FOUND`; multiple => `WRITE_INTENT_AMBIGUOUS`; exactly one is frozen before Guard and never re-resolved after Guard.

## 7. Production runtime operation port

Neutral context contract:

```java
public interface RuntimeModelOperationPort {
    RuntimeFactValue read(RuntimeObjectId objectId, ModelPath path);
    ProtectedWriteReceipt write(ResolvedWriteIntent intent, RuntimeObjectId objectId, ModelPath path);
}
```

Production implementation belongs to `dec-core-model`; starter production assembly wires it into the starter-internal `ProtectedOperationExecutionAdapter`. Business callers never receive/inject this port.

## 8. Protected result algebra

`ProtectedAccessResult` enforces mutually exclusive states:

- ALLOW+READ => `ProtectedReadValue` present only;
- ALLOW+WRITE => `ProtectedWriteReceipt` present only;
- DENY => `ProtectedAccessDenial` present only.

Denial codes are stable and non-sensitive; no result leaks on DENY.

## 9. Review gate

API remains candidate-only. No production implementation, same-revision independent Review, risk Evidence or TDD execution is claimed.
