# EVD-000993 — TDD-P1-T12-R07

- TDD：`TDD-P1-T12-R07@cb3f08f28807`
- Design：`DESIGN-R44@P1-T12-REWORK-I007`
- Plan：`TP-P1-COMPILER-F01-R40@P1-T12-REWORK-I007`
- Status：`PASSED`

## Valid RED

- Head：`cb3f08f28807ad40e2a4b40519baf4a2fc83ba61`
- P0 Run：`31000174741` — `FAILURE / EXPECTED_RED`
- Artifact：`8927903337`
- SHA-256：`7a828a1d8d73aaf502032470780ca4af5089beef860484a9a06852a33016d0b6`
- I007：`6 tests / 4 expected failures / 2 passing controls / 0 errors`

## Expected failures

1. 两个具有相同非法 duplicate-key 结构的 IdentityHashMap 未被拒绝；
2. 单侧非法 Map 未被拒绝；
3. identity-backed Set duplicate canonical element 未被拒绝；
4. 嵌套 collision 未被拒绝。

## Passing controls

- 正常 LinkedHashMap 保持相等；
- hashCode 相同但 equals 不同的 key 保持精确不等。

I001～I006 与既有 Compiler 测试在 RED Head 保持绿色。RED 测试使用既有 `IllegalArgumentException` 父类型，未依赖尚不存在的新异常，因此是可编译、可执行、有效的行为 RED。
