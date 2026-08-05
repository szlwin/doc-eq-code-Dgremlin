# TDD-P1-T12-R06 — I006 Valid RED Evidence

- TDD：`TDD-P1-T12-R06@788f475d60e4`
- Evidence：`EVD-000971`～`EVD-000974`
- Design：`DESIGN-R43@P1-T12-REWORK-I006`
- Plan：`TP-P1-COMPILER-F01-R39@P1-T12-REWORK-I006`
- Head：`788f475d60e4864fc6c11bfffee3ff925aa757ac`
- Run：`30991106416`
- Artifact：`8924134527`
- SHA-256：`4837ffc477af0bc7b892fe5d71fb2e0c6b824108346413f00f8c8dc371870221`
- Result：`VALID_RED`

## Test result

- I006：8 tests；
- Expected failures：7；
- Passing control：1；
- Errors / Skipped：0 / 0；
- Compiler tests：426；
- Existing I001～I005：GREEN。

## Expected failures

1. Set canonicalization 在 edge budget 前整体复制外部 Set；
2. Map canonicalization 在 edge budget 前整体复制 entrySet；
3. 无限 Set iterator 未在 edge 上限前停止；
4. List equality 调用外部 `size()`；
5. 非 RandomAccess List equality 调用 `get(index)`；
6. List 多候选 contains 重复展开同一 NOT_EQUAL pair；
7. Map 多 entry containsValue 重复展开同一 NOT_EQUAL pair。

所有失败均来自 R43/R39 尚未实现的行为，不是测试编译、环境、依赖或既有合同回归，因此满足有效 RED 门禁。
