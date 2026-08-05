# TASK-P1-T13 / R01 Development Evidence

- Evidence：`EVD-001007`～`EVD-001010`
- Development：`DEV-P1-T13-R01@74672ee1367b`
- Production Revision：`65f96c71ae0560f375d402b586125ad4879dde4b`
- Code/Test Revision：`74672ee1367bab9de75b4028cd4578b6118f96f0`
- Architecture：`DEVSKEL-P1-T13-R01@4f3d444f779f`

## Delivered production

新增：

- `dec.core.compiler.compiled.CanonicalJsonWriter`
- `dec.core.compiler.compiled.SemanticDigestInput`
- `dec.core.compiler.compiled.CompilerDigestService`

修改：

- `CompilationSession`：只允许未 seal Session 写入非 ERROR `MIX_OBSERVER_FAILURE`；
- `PipelineDiagnostics`：稳定 timing/transition Observer Warning；
- `CompilerPipeline`：同一 Pass elapsed 记录 DISCOVERY/PARSE/PASS/DIGEST，Observer RuntimeException fail-open；
- `PipelineExecutionResult`：明确返回 PASS 与补充阶段 timing；
- 两项历史 timing 数量 Oracle 迁移到 T13 的 13 条合同。

## Contract

- sourceDigest：Source ID/原始内容长度前缀、code point 排序、SHA-256 小写 hex；
- semanticDigest：不可变语义快照 canonical JSON 的 UTF-8 SHA-256；
- line/column、Source format/content digest、Timing、Observer 与 DigestPair 不进入 semantic digest；
- Object key 使用 Unicode code point 顺序，string/number/boolean/null 稳定编码；
- NaN、Infinity、未知值、循环和重复 object key fail-closed；
- Observer 失败不改变 PUBLISHED/FAILED、artifact、Context 或发布次数；
- supplemental timing 不额外读取 Clock；
- T14/T15 与 P2～P7 runtime 未实现。

## Operational integrity

- 首次大型内嵌 payload Workflow 因 Base64 被连接器截断而失败，未产生生产提交；
- 后续 5 段 payload 合并前校验 SHA-256 `0b14f12ba476842373efbe0409deeee79d63494a5840efe706d41d53b1e065f4`；
- payload 与应用 Workflow 在 Production Revision 中全部删除；
- Independent Review Workflow 同样在 Test Revision 中自删除；
- 最终文件树无传输辅助文件。

## Style

- 所有新增或修改的 `@Override` 独占一行；
- public/package-private 方法、排序、escaping、SHA-256、Clock 与 fail-open 边界均有中文注释；
- 无 tab、静态可变缓存、ThreadLocal、默认 Charset 或未使用 private task。

结论：`PASSED`。
