# TASK-P1-T13 / I001 — 确定性 Digest、Deadline 与 Observer

- Status：`IN_PROGRESS / DEVELOPMENT_IMPLEMENTED_PENDING_GREEN`
- Base：`dev_all@659fb74563bbe1fa1daaf4d3a0e868f702daaec6`
- Dependency：`COMPLETION-P1-T12-R07@74f402287bc4`
- Branch：`feature/p1-t13-semantic-digest-20260805-2005`
- PR：`#28 / WORKING`
- Design：`DESIGN-R45@P1-T13-I001`
- Plan：`TP-P1-COMPILER-F01-R41@P1-T13-I001`
- TDD：`TDD-P1-T13-R01@4f3d444f779f`
- Architecture：`DEVSKEL-P1-T13-R01@4f3d444f779f`
- Production Revision：`65f96c71ae0560f375d402b586125ad4879dde4b`
- Open P0/P1/P2：`0 / 0 / 0`

## Goal

实现 `DEC-SEMANTIC-DIGEST-V1`、Source Digest、canonical JSON、同一 MonotonicClock 域的 Timing/Deadline，以及 fail-open 的只读 Observer Warning。相同语义输入在 Source 枚举、Map 插入、重复运行和 SourceRef 物理行列变化下必须生成一致 semantic digest。

## Scope

- `dec-core-compiler/src/main/java/dec/core/compiler/compiled`
- `CompilationSession` observation diagnostic 边界
- `PipelineDiagnostics` Observer Warning
- `CompilerPipeline` DISCOVERY/PARSE/PASS/DIGEST Timing 与 Observer failure handling

## Excluded

- T14：CompiledModelSet/EngineContext 候选构造、CAS 发布扩展
- T15：Starter 接入、CoreConfigProjection、旧 Declaration 模块退役
- P2～P7 runtime

## Acceptance

- `AC-P1-T13-001`：同语义输入在乱序、重复运行和 SourceRef line/column 变化下产生完全一致 semanticDigest；原始 Source 内容变化改变 sourceDigest。
- `AC-P1-T13-002`：deadline、cancel 与 Observer 异常产生稳定结果；Observer 失败不得改变原 status、context、artifact 或 digest。
- canonical JSON 符合 Unicode code point key order、标准 escaping、canonical decimal 和版本域闭包。
- 完整成功 Pipeline Timing 为 DISCOVERY=1、PARSE=1、PASS=10、DIGEST=1，额外 phase 不额外读取 Clock。
- Observer failure 产生非 ERROR `MIX-OBSERVER-FAILURE`，不静默吞掉。
- 所有 `@Override` 独占一行；方法与重要逻辑使用中文注释。

## TDD RED

- Head：`4f3d444f779f5c1f69a5b61751cbd00b4a9a528b`
- P0 Run：`31005889102` — `FAILURE / EXPECTED_RED`
- Artifact：`8930284340`
- SHA-256：`fe03a8fea61ff6ecbcd2a45f8ddba3f91ac37629cf8c9ff1a583777dc5fa5946`
- Result：`13 tests / 11 expected failures / 2 passing controls / 0 errors`

## Production delivery

- `CanonicalJsonWriter`：Unicode code point object-key 顺序、标准 escaping、canonical decimal、cycle/unknown/duplicate-key fail-closed；
- `SemanticDigestInput`：在构造时形成不可变 canonical semantic snapshot，排除 line/column、format、source content digest、Timing 与 DigestPair；
- `CompilerDigestService`：Source ID/内容长度前缀 SHA-256 与 canonical JSON SHA-256；
- Pipeline：复用原 Pass 时钟读数记录 DISCOVERY/PARSE/PASS/DIGEST；
- Observer：RuntimeException 转换为 `MIX-OBSERVER-FAILURE / WARNING`，不能改变 Session 终态或发布结果；
- T14/T15 范围未实现；
- 传输辅助 payload 与 Workflow 已从 Production Revision 文件树删除。

## Stop conditions

- semantic digest 含 DigestPair、Timing、SourceRef line/column 或 Source content digest；
- Map/filesystem/线程顺序影响摘要；
- Observer 失败改变 Session 终态或发布结果；
- supplemental timing 增加 Clock 读取；
- T12 的 Deadline/Cancel/Publication 原子性回归；
- 出现未关闭 P0/P1/P2；
- 实现 T14/T15 范围；
- final P0、Artifact 独立解析、Revision Integrity 未完成；
- 未经用户授权合并 PR。
