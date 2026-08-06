# TDD-P1-T14-R02 — I002 TDD Evidence

- TDD：`TDD-P1-T14-R02@1df0a14f2a74`
- Status：`PASSED`

## Valid RED

- Head：`1df0a14f2a746d6027485a99dcf9cbd3ceeb3899`
- P0 Run：`31068551065` — `FAILURE / EXPECTED_RED`
- Artifact：`8954760225`
- SHA-256：`7431ba21d9447de5cd60aa2db06cb849a3a045867553e276f7d22f61931d5d15`

RED 在生产代码和测试均可编译后证明：

- `CompilerDigestService` 缺少 atomic bind；
- Builder 仍暴露分离式版本/模型/Digest API；
- request schema/options mismatch 仍可进入 PUBLISHED。

## GREEN Oracle

I002 最终 18 项覆盖：

- atomic bind 和 Builder API；
- 完整非空 candidate；
- 完整 Publisher 字段与调用次数；
- missing/mismatch 精确 Diagnostic；
- request schema/options；
- 真实 SHA-256；
- raw/published source closure；
- Definition/Deferred negative size、枚举、duplicate、missing、identity、漂移和零重读；
- capability isolation 与 context close。

未使用无关 testCompile 失败作为 RED。
