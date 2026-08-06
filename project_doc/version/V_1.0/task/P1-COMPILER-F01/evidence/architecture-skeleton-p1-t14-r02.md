# DEVSKEL-P1-T14-R02 — I002 Architecture Evidence

- Architecture：`DEVSKEL-P1-T14-R02@2c7ddd4f4f96`
- Design：`DESIGN-R49@P1-T14-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R45@P1-T14-REWORK-I002`
- Status：`PASSED`

## Frozen boundaries

- 新增 `DigestBoundCompiledInput` 作为 atomic provenance artifact；
- 创建权限定在 `CompilerDigestService.bind()`；
- bind 内先快照模型事实，再构造 T13 `SemanticDigestInput` 并计算摘要；
- `CompiledModelSetBuilder` 只接受 atomic input；
- Publication Pass 只比较 request schema/options、构造 candidate、调用 `prepare()`；
- Pipeline 继续唯一持有 Publisher/CAS capability。

## Fail-closed boundaries

- raw/published source closure mismatch；
- Registry 完整性失败；
- 非法 SHA-256；
- request schema/options mismatch；
- missing input；
- ERROR Diagnostic。

以上边界均在 production 前完成最终设计修订，未扩展到 T15、Starter 或 P2～P7 runtime。
