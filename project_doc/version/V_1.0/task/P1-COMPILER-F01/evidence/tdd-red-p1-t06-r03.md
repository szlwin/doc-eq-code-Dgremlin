# TASK-P1-T06 I003 TDD RED Evidence

- Revision：`TDD-P1-T06-R03@ea1701deb923`
- Head：`ea1701deb923cfef6eea819465659c08e2b606fc`
- P0 Build Gate：Run `30798045747` — `FAILURE`（预期 RED）
- Artifact：`8849672024`
- Artifact SHA-256：`cf8416ab49faa58d09a5cd6a9b24704b960be14b7ec05d708349c07deced5da6`
- Review：`REV-000286` — TDDReviewAgent — `PASSED`
- Evidence：`EVD-000528`

## 编译与既有回归

- Java release 8 生产源码：编译成功；
- Java release 8 测试源码：编译成功；
- Context：26/26 PASSED；
- I002 既有 Raw：31/31 PASSED；
- I003：7 run / 4 failures / 1 error / 0 skipped / 2 passed；
- Compiler 总计：121 run / 4 failures / 1 error；
- Reactor 在 `dec-core-compiler` 按预期停止，后续模块未被错误声明为通过；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## 精确 RED

1. `ignoresUnsupportedRootFromSecondIterator`：第二次迭代发布成功空集合，期望第一次快照中的合法定义；
2. `ignoresUnknownChildFromSecondIterator`：第二次迭代的 `bad/mystery` 进入成功结果；
3. `mutationAfterSnapshotDoesNotAffectResult`：验证后原始 List 修改改变提取结果；
4. `snapshotOrderDeterminesOrdinals`：第二次迭代顺序决定 ordinal；
5. `snapshotReadFailureDoesNotReaccessOriginalList`：RuntimeException catch 调用 `firstSourceRef(documents)`，再次通过 `size/isEmpty` 访问原始 List并产生未受控 error；
6. snapshot 内真实 unsupported root 和 unknown child 的既有 fail-closed 合同保持通过。

测试使用确定性 side-effecting List，不依赖并发、线程调度、执行时间、OOM 或 StackOverflowError。失败全部由 `FND-P1-T06-I003-001` 目标行为缺失导致，构成有效 RED。
