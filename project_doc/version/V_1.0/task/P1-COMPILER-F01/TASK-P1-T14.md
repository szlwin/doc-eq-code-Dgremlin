# TASK-P1-T14 — Provenance-bound Candidate EngineContext

- Current Iteration：`I002`
- Status：`COMPLETED / PASSED`
- Base：`dev_all@3e4da420d2ef5ada8398aefbbeabb37964e384ce`
- Dependency：`COMPLETION-P1-T13-R03@5075793d06cc`
- Branch：`feature/p1-t14-candidate-context-20260805-2324`
- PR：`#29 / OPEN / READY_FOR_REVIEW / NOT_MERGED`
- Design：`DESIGN-R49@P1-T14-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R45@P1-T14-REWORK-I002`
- TDD：`TDD-P1-T14-R02@1df0a14f2a74`
- Architecture：`DEVSKEL-P1-T14-R02@2c7ddd4f4f96`
- Development：`DEV-P1-T14-R02@668d865b0189`
- Code Review：`CODEREVIEW-P1-T14-R03@668d865b0189`
- Testing：`TESTING-P1-T14-R02@668d865b0189`
- Completion：`COMPLETION-P1-T14-R02@668d865b0189`
- Reviews：`REV-000725`～`REV-000746`
- Evidence：`EVD-001068`～`EVD-001091`
- Open P0/P1/P2：`0 / 0 / 0`

## Completion history

- `COMPLETION-P1-T14-R01@252024603bfc` — `INVALIDATED / PRESERVED`；
- `COMPLETION-P1-T14-R02@668d865b0189` — `CURRENT / PASSED`。

## Delivered contract

- `CompilerDigestService.bind()` 原子冻结 raw/published Source、Definitions、Deferred 和版本域；
- 同一不可变快照构造 T13 `SemanticDigestInput` 并立即计算 `DigestPair`；
- raw 与 published SourceManifest 的 sourceId 集合必须完全一致；
- `DigestBoundCompiledInput` 私有构造，禁止调用方注入任意 DigestPair；
- `CompiledModelSetBuilder` 只接受 atomic input，freeze 后永久封闭；
- 正式摘要边界只接受 64 位小写 SHA-256；
- Publication Pass 在 prepare 前比较当前 request schema/options；
- mismatch 返回 `MIX_PUBLICATION_PROVENANCE_MISMATCH / ERROR`；
- missing input 返回 `MIX_PUBLICATION_BLOCKED / ERROR`；
- 失败路径固定 FAILED、publisher=0、artifacts empty；
- 正常路径完整 Manifest、Definitions、Deferred、Warning、Digest 和版本域精确传给 Publisher；
- Definition/Deferred 快照拒绝 negative size、keys mismatch、duplicate、missing、identity mismatch 和 size drift；
- bind 后原 Registry 不再读取；
- Pipeline 继续唯一持有 Publisher/CAS capability。

## Findings

- `FND-P1-T14-I002-001`：`CLOSED`；
- `FND-P1-T14-I002-002`：`CLOSED`；
- `FND-P1-T14-I002-003`：`CLOSED`；
- Open P0/P1/P2：`0/0/0`。

## Validation

### Valid RED

- Head：`1df0a14f2a746d6027485a99dcf9cbd3ceeb3899`
- Run：`31068551065` — `FAILURE / EXPECTED_RED`
- Artifact/SHA：`8954760225` / `7431ba21d9447de5cd60aa2db06cb849a3a045867553e276f7d22f61931d5d15`

### Clean-code GREEN

- Code/Test Revision：`668d865b0189e9107f25295a1726748968aa7462`
- Run：`31069685120` — SUCCESS
- Artifact/SHA：`8955166219` / `5553810bfb87146c97835dd5d1c2de10b4c2b8405a9ef533e994f110c7b71c6c`
- Surefire XML：109；T14：18/18；T13：34/34；T12：133/133；Compiler：504/504；
- Normal：624/624；All：625；intentional failure：1；Errors/Skipped：0/0；
- Java release 8、12 modules Reactor、intentional failure gate：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## Revision integrity and style

- R49 first commit：`eda473f06ea8b0dcc1666c0e41c9a179aaf5ad0d`；
- R49 final pre-production commit/blob：`2c7ddd4f4f96d6a5c108d8aeca4534d62ace380c` / `023cc974ad5b29e74b13249003c597e341acf738`；
- R45 first commit/blob：`331b3f6dc36596051cf2657e81b3d5059724e4e7` / `c80f520b34a409e5f5fa8eaa7166e95087ec9373`；
- R49 行为合同在 RED 前冻结，具体 bind 入口在 RED 后、首个 production commit 前受控修订；
- Code/Test Revision 后只允许 `project_doc` 更新；
- 所有新增 `@Override` 独占一行；
- 方法与重要逻辑使用中文注释。

PR #29 未执行合并；未经用户明确授权不得合并。`TASK-P1-T15` 保持 `BLOCKED_UNTIL_PR_29_MERGE`。
