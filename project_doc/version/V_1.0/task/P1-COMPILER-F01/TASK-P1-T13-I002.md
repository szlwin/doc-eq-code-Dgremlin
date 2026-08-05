# TASK-P1-T13 / I002 — 严格 Unicode Source 身份与 FAILED Observer 返工

- Status：`IN_PROGRESS / TDD_RED_PENDING`
- Base：`PR28@9d180f2d34728cd453c377a6310b01fe1a7659cf`
- Branch：`feature/p1-t13-semantic-digest-20260805-2005`
- PR：`#28 / REWORK / DO_NOT_MERGE`
- Previous Completion：`COMPLETION-P1-T13-R01@74672ee1367b` — `INVALIDATED / PRESERVED`
- Design：`DESIGN-R46@P1-T13-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R42@P1-T13-REWORK-I002`
- Open P0/P1/P2：`0 / 1 / 1`

## Findings

- `FND-P1-T13-I002-001`：P1，malformed UTF-16 sourceId 在宽松 UTF-8 编码中发生替代字节碰撞；
- `FND-P1-T13-I002-002`：P2，缺少 FAILED transition Observer failure 的冻结 Oracle。

## Goal

- 对 `DEC-SOURCE-DIGEST-V1` 的 sourceId identity 执行严格 UTF-8 编码；
- malformed high/low surrogate 稳定 fail-closed；
- 合法 ASCII、BMP、supplementary digest vector 保持；
- FAILED 后 Observer exception 只追加 Warning，不改变原 ERROR、状态、publisher 或 artifacts。

## Allowed production scope

- `dec-core-compiler/src/main/java/dec/core/compiler/compiled/CompilerDigestService.java`

## Test scope

- `SemanticDigestIndependentReviewTest.java`
- `CompilationObserverIndependentReviewTest.java`

## Excluded

- Source 模型整体重构；
- ContextPublisher、PublicationRequest、EngineContext CAS；
- T14 candidate Context；
- T15 Starter/旧模块退役；
- P2～P7 runtime。

## Required evidence

- I002 valid RED；
- Architecture skeleton；
- First GREEN；
- Independent Review；
- Compiler/T12/全 Reactor/Java 8；
- intentional failure gate；
- clean-code 与 final documented Artifact 独立 SHA/XML 解析；
- Revision Integrity；
- 新 Completion 与 PR #28 Review。

## Stop conditions

- malformed sourceId 被静默替换；
- 合法已知 digest vector 漂移；
- FAILED Observer Warning 覆盖原 ERROR；
- publisher>0 或失败 artifacts 非空；
- Open P0/P1/P2 未清零；
- PR #28 被合并；
- T14 被提前启动。
