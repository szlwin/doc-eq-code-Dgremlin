# P2 Access Operations Decision

> Decision ID：`DEC-P2-ACCESS-OPERATIONS-001`  
> Status：`ACTIVE / USER_CONFIRMED`  
> Decided by：`user`  
> Decided at：`2026-08-09T12:36:00+08:00`

## Decision

P2 model-access 当前只存在两种授权 operation：

```text
READ
WRITE
```

**不存在 EXECUTE。**

因此 current P2 candidate 必须满足：

- `AccessOperation` 只包含 `READ`、`WRITE`；
- source/XML 不新增 `<execute>` 或其他 EXECUTE declaration；
- frontend/raw IR 不产生 EXECUTE access fact；
- `CompiledModelAccessRule` / `ModelAccessRuleKey` / `ModelAccessPolicyIndex` 不表示 EXECUTE；
- `ProtectedAccessInvocation` / `ProtectedExecutionBridge` / Gateway / Guard 只接受 READ/WRITE；
- P1 `AccessMode.READ/WRITE` 一对一转换到 P2 `AccessOperation.READ/WRITE`；
- 不存在“由 READ/WRITE 推导 EXECUTE”的兼容逻辑；
- Test Design 不再要求 EXECUTE allow/deny 或 source-to-runtime fixture。

## Requirement delta

历史 `REQAN-P2-R01@d08612768131` 中 READ/WRITE/EXECUTE 三类 operation 的文字保持历史不改写；对当前 P2 candidate，其中 EXECUTE 部分由本用户决策显式修改为 **N/A / not part of current P2 model-access contract**。

这不是把历史 Requirement 描述成“原来就没有 EXECUTE”，而是当前生效的 Requirement delta。Requirement overlay 必须显式引用本 Decision。

## Compatibility

当前真实 P1 `AccessMode` 只有 READ/WRITE，与本 Decision 一致。P2 不为了形式对称创建一个没有 source semantics 的 EXECUTE operation。

## Gate

本 Decision 只关闭 operation scope ambiguity；Requirement exact Review、BM/Flow/Design/TestDesign Review、risk detection 和 machine lifecycle 仍必须按 current exact revision 执行。