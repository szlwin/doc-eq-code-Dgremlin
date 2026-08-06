# CODEREVIEW-P1-T14-R03 — I002 Provenance-bound Candidate Review

- Code Review：`CODEREVIEW-P1-T14-R03@668d865b0189`
- Iteration：`TASK-P1-T14 / I002`
- Code/Test Revision：`668d865b0189e9107f25295a1726748968aa7462`
- Gate：`PASSED`
- Open P0/P1/P2：`0 / 0 / 0`

## Review input

重新 Review 在 `542a3e3900c41a91f354849ab056d1066db78656` 发现：

- `FND-P1-T14-I002-001` — P1：FrozenInput 未绑定同一个语义闭包或当前 request；
- `FND-P1-T14-I002-002` — P2：I001 的快照完整性与完整 candidate 证据超过真实 Oracle。

I001 的 `CODEREVIEW-P1-T14-R01@252024603bfc` 和 `COMPLETION-P1-T14-R01@252024603bfc` 已 `INVALIDATED / PRESERVED`。

## Architecture review

### Atomic provenance

- `DigestBoundCompiledInput` 私有构造，调用方不能注入任意 `DigestPair`；
- `CompilerDigestService.bind()` 先快照 Definitions/Deferred，再用同一不可变快照构造 `SemanticDigestInput`；
- source digest 与 semantic digest 在同一次 bind 中计算；
- raw SourceManifest 与 PublishedSourceManifest 的 sourceId 集合必须完全一致；
- compilerVersion、schemaVersion、optionsDigest 和模型事实共同进入 T13 canonical input；
- 正式边界只接受 64 位小写 SHA-256。

### Request binding

- `CompilationOptions` 当前只公开 schemaVersion/optionsDigest；
- Publication Pass 在 `prepare()` 前精确比较这两个 request 事实；
- compilerVersion 由 atomic bind 纳入 semantic digest，不能在 T14 再单独替换；
- mismatch 返回 `MIX_PUBLICATION_PROVENANCE_MISMATCH / ERROR`；
- FAILED、publisher=0、artifacts empty。

### Capability boundary

- `CandidateContextPublicationPass` 无 `ContextPublisher`、`PublicationRequest` 或 CAS 字段；
- Pipeline 继续唯一持有外部 publisher capability；
- missing input 继续稳定返回 `MIX_PUBLICATION_BLOCKED / ERROR`。

## Snapshot review

Definition 与 Deferred 均已直接验证：

- negative size；
- keys/size mismatch；
- duplicate key；
- missing value；
- external/internal identity mismatch；
- final size drift；
- bind 完成后原 Registry 零读取。

生产实现使用一次性 keys 复制和最终 size 复核，最终转换为不可变 Registry。

## Independent finding closure

### FND-P1-T14-I002-001 — CLOSED

模型事实、版本和 Digest 已由 atomic bind 形成不可拆分闭包；Publication Pass 同时绑定当前 request schema/options。任意 DigestPair、跨 request 版本和跨 raw/published Source 闭包均不能发布。

### FND-P1-T14-I002-002 — CLOSED

T14 Oracle 从 I001 的 12 项扩展为 I002 的 18 项，新增完整非空 candidate、真实 Digest、Warning、完整 Publisher 字段、精确 Diagnostic，以及 Definition/Deferred 全部负向边界。

### FND-P1-T14-I002-003 — CLOSED DURING REVIEW

独立审查发现 atomic bind 虽在同一次调用计算两个摘要，但仍需阻止 raw SourceManifest 与 published SourceManifest 的身份集合跨编译拼接。已增加 sourceId 闭包一致性门禁及直接负向 Oracle。

## Engineering standards

- 所有新增 `@Override` 独占一行；
- 类、方法、摘要绑定、快照、request 门禁与重要测试逻辑均使用中文注释；
- Java release 8；
- 无新依赖、反射生产逻辑、sleep、wall-clock 或共享可变测试状态；
- 测试中的反射仅用于证明正式私有摘要格式边界，未进入生产路径。

## Validation

### Valid RED

- Head：`1df0a14f2a746d6027485a99dcf9cbd3ceeb3899`
- P0 Run：`31068551065` — `FAILURE / EXPECTED_RED`
- Artifact：`8954760225`
- SHA-256：`7431ba21d9447de5cd60aa2db06cb849a3a045867553e276f7d22f61931d5d15`

### First complete GREEN

- Head：`4d748d5cc0a0a1676d30965bce0972ca093bcffd`
- P0 Run：`31069272744` — SUCCESS
- Artifact：`8955019200`
- SHA-256：`ed86746f34ce68f5418eb23bd72682c9434da0fb0d75548d83c99bd0831d3f9e`
- T14：17/17；Compiler：503/503；Normal：623/623。

### Clean-code / source-closure GREEN

- Code/Test Revision：`668d865b0189e9107f25295a1726748968aa7462`
- P0 Run：`31069685120` — SUCCESS
- Artifact：`8955166219`
- SHA-256：`5553810bfb87146c97835dd5d1c2de10b4c2b8405a9ef533e994f110c7b71c6c`
- Surefire XML：109；T14：18/18；T13：34/34；T12：133/133；Compiler：504/504；
- Normal：624/624；All records：625；intentional failure：1；Errors/Skipped：0/0；
- 12 modules / Java release 8 / intentional failure gate：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

Artifact 已独立下载解析，ZIP SHA-256 与 GitHub digest 完全一致。

## Result

`TASK-P1-T14 / I002` 代码审查通过，Open P0/P1/P2=`0/0/0`。允许进入 Testing/Completion；PR #29 仍不得在未明确授权时合并，T15 继续等待 PR #29 合并。
