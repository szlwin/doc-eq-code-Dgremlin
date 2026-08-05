# TASK-P1-T12 I005 TDD Evidence

- TDD：`TDD-P1-T12-R05@c3a78498e595`
- Design：`DESIGN-R42@P1-T12-REWORK-I005`
- Plan：`TP-P1-COMPILER-F01-R38@P1-T12-REWORK-I005`
- Evidence：`EVD-000923`～`EVD-000930`

## Invalid pre-RED attempt

- Head：`035f8801dd11b43cb65cfd323943eee8f4a3d226`
- Run：`30982889960`
- Result：测试源码在 Map cast 处缺少右括号，停在 testCompile；该运行不是有效 RED，不用于 Completion。
- Artifact：`8920836810`
- SHA-256：`2b31c8a8121bf791a3a1f860ec7e8100906717d6068be663605712041b522530`

## Valid RED

- Head：`c3a78498e595d0006334c8ec382c72c830142d19`
- Run：`30983520984`
- Artifact：`8921103609`
- SHA-256：`9348d599ace1898147697646e544452eacfe815d1ef0a23d21c681a034de8189`
- Surefire：I005 8 tests / 6 failures / 0 errors / 0 skipped。

六项失败精确命中：

1. `FrozenList.equals()` 对 24 层共享 DAG 指数展开；
2. `FrozenSet.contains()` 对共享 DAG 指数展开；
3. `FrozenMap.get/containsKey()` 对共享 DAG key 指数展开；
4. `FrozenEntrySet.contains()` 递归 Entry/List equality；
5. comparison depth limits API 缺失；
6. identity-pair budget API 缺失。

两项控制通过：hash 相同但结构不同未误判；小规模普通 Java Collection 对称性和 hash 合同保持。I001～I004 与既有 Compiler 测试全部保持绿色。

TDD Gate：`PASSED`。
