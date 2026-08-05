# TESTING-P1-T12-R06 — comparison operation 资源返工测试证据

- Testing：`TESTING-P1-T12-R06@ce8c92523256`
- Evidence：`EVD-000983`～`EVD-000990`
- Open P0/P1/P2：`0 / 0 / 0`

## First GREEN

- Head：`91fe23a388d6fc62376222f36a291e8d00544f6a`
- P0 Run：`30992157198` — `SUCCESS`
- Artifact：`8924592524`
- SHA-256：`4ddc6101fcbed0ca602b34247d843019e90e5a8ddf120871208c67859928e271`
- Surefire XML：97；
- I006 RED suite：8 / 8；
- 正常测试：546 / 546；
- 全部记录：547；
- 故意失败门禁：1 项按预期失败；
- Errors / Skipped：0 / 0。

## Independent Review / clean-code validation

- Head：`ce8c9252325642cf45e89f71aaa1f807d4916aca`
- P0 Run：`30992489987` — `SUCCESS`
- Artifact：`8924724966`
- GitHub digest：`f0d5b9ce6c44a922b9bdd534c82f0e235912588f97ced16c117d9b57774a54a4`
- Independent SHA-256：`f0d5b9ce6c44a922b9bdd534c82f0e235912588f97ced16c117d9b57774a54a4`
- Surefire XML：98；
- I006：18 / 18；
- T12 total：117 / 117；
- Compiler module：436 / 436；
- 正常测试：556 / 556；
- 全部测试记录：557；
- 故意失败门禁：1 项按预期失败并被识别；
- Errors / Skipped：0 / 0；
- 12 模块 Reactor：PASSED；
- Java release 8：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## Independent Review Oracle

- reverse List query operation cache；
- Map key get/containsKey child-pair reuse；
- Set element 与 EntrySet 的 EQUAL prefix reuse；
- Set 内嵌普通 List iterator-only canonicalization；
- Map entrySet.size 不读取；
- iterator 业务异常原样传播；
- 宽 Set 在第 maxEdges+1 次 next 前拒绝；
- Map 第二 entry 在 2-edge budget 后不读取；
- 循环普通 List 非递归稳定拒绝。
