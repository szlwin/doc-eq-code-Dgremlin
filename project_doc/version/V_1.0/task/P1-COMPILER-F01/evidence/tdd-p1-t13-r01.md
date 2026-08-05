# TASK-P1-T13 / R01 TDD Evidence

- Evidence：`EVD-001004`～`EVD-001006`
- TDD：`TDD-P1-T13-R01@4f3d444f779f`
- Design：`DESIGN-R45@P1-T13-I001`
- Plan：`TP-P1-COMPILER-F01-R41@P1-T13-I001`

## Valid RED

- Head：`4f3d444f779f5c1f69a5b61751cbd00b4a9a528b`
- P0 Run：`31005889102`
- Artifact：`8930284340`
- ZIP SHA-256：`fe03a8fea61ff6ecbcd2a45f8ddba3f91ac37629cf8c9ff1a583777dc5fa5946`
- Result：`13 tests / 11 expected failures / 2 passing controls / 0 errors`

## RED failure closure

- 6 项 Digest Oracle：正式 `SemanticDigestInput`、`CompilerDigestService` 与 canonical JSON 尚不存在；
- 4 项 Observer/Timing Oracle：Observer 异常被静默吞掉，DISCOVERY/PARSE/DIGEST 尚未登记；
- 1 项 supplemental Clock Oracle：成功路径只有 10 个 PASS Timing；
- 2 项 Deadline/Cancel 控制 Oracle 已通过；
- T01～T12 与其他 Compiler 测试保持绿色。

## Invalid attempts preserved

- Run `31005605957`：`action_required / no jobs`，不计 RED；
- Run `31005696771`：测试夹具调用不存在的 `candidate()`，testCompile 失败，不计 RED；
- 修正夹具后才取得上述有效 RED，生产代码尚未修改。

结论：`PASSED / VALID_RED`。
