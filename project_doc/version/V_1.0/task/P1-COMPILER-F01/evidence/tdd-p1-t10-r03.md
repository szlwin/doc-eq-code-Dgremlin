# TASK-P1-T10 TDD Evidence R03

- Revision：`TDD-P1-T10-R03@b16d5ee9f9f1`
- Valid RED Head：`b16d5ee9f9f1f1a95446c6d96803dd35beae0a9b`
- P0 Run：`30905938187`
- Artifact：`8891035004`
- Artifact SHA-256：`81c61310417ae9244f40a176aef093627cc180d7f97a6d4b10140bdd4c34b703`
- Java release 8：生产 99 个源码、测试 55 个源码编译成功
- Result：`3 failures / 0 errors`

## 阻断性 RED

新增 `ModelAccessTypedKeyLexicalReworkTest` 共 9 项。有效 RED 中只有以下 3 项失败：

1. padded `model-ref` 应成功并保留 Raw lexical；
2. padded `ref@view` 应成功并保留 Raw lexical；
3. padded System declaration 与 padded ModelAccess reference 应通过 canonical ViewKey 对齐。

其余 6 项在 RED 中已通过：padded System declaration + plain reference、padded property/path 失败、blank reference 失败、Raw name/model-ref mismatch 失败。旧 I001/I002、Context、Symbol 与 Compiler 既有测试保持绿色；MySQL 为 `SKIPPED_NOT_APPLICABLE`。

## Rejected Development Attempt

- Head：`bc056b7ed1da2cf2d47c8a3e66c24947f5cc695c`
- P0 Run：`30906241652`
- Artifact：`8891150265`
- Result：`2 failures / 0 errors`

生产 lexical 修复已使结构错误消失，但两个正向 Canonical fixture 错误地声明 `OrderInfo`、实际引用 `UserInfo`，正确触发 `MIX-REF-VIEW-NOT-DECLARED`。该 attempt 作为测试夹具缺陷历史保留；只修正声明输入，没有扩大生产范围。
