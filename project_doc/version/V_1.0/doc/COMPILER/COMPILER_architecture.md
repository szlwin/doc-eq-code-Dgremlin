# COMPILER P2 Architecture

> Revision：`DESIGN-P2-R20`。Base：`DESIGN-P2-R19`。Status：`NEEDS_REVIEW / MACHINE_BLOCKED`。

## Dependency direction

```text
P3/P4/P6 core -> dec-core-context : allowed
P3/P4/P6 core -> dec-core-starter : forbidden

dec-core-compiler -> dec-core-context
dec-core-model    -> dec-core-context (+ datasource as existing runtime dependency)
dec-core-starter  -> dec-core-context / dec-core-compiler / frontends / dec-core-model
application/demo  -> dec-core-starter
```

The new starter->model edge is the planned production assembly dependency that closes real runtime READ/WRITE; it must be implemented/reviewed later and is not claimed as present code in this documentation-only remediation.

## Authorities

- shared source View identity: existing `ViewKey` namespace;
- authorization identity: `authorizationOwnerSystemKey + TargetKey(ViewKey) + ModelPath + READ/WRITE`;
- runtime permission truth: immutable `ModelAccessPolicyIndex`;
- runtime operation target/intent: immutable `ResolvedProtectedAccess` frozen before Guard;
- actual model read/write: production `dec-core-model RuntimeModelOperationPort` implementation;
- production enforcement/assembly: starter Bridge/Gateway/Guard/capability/adapter.

No component may introduce a second mutable PolicyIndex, caller operation callback, target/path substitution, or post-Guard intent re-resolution.

## Publication

Compilation publishes System/RuleView/RuleKey/source View TargetKey/ModelPath/policy/version/digest as one immutable Context closure. Failure retains old Context.

## Gate

Architecture/Impact/CrossModule/Concurrency independent Review and risk scan remain required.
