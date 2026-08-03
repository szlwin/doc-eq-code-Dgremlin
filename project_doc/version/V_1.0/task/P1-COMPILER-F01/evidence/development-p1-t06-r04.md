# TASK-P1-T06 I004 Development Evidence

- Revision：`DEV-P1-T06-R04@242db638c61d`
- Clean-code Head：`242db638c61d58eb70e452c1ac08668b6d738b0a`
- Architecture Skeleton：`DEVSKEL-P1-T06-R04@2d78c2290498`
- Production file：`dec-core-compiler/src/main/java/dec/core/compiler/raw/RawDefinitionBuilder.java`
- Test file：`dec-core-compiler/src/test/java/dec/core/compiler/raw/RawSnapshotBudgetReworkTest.java`
- Reviews：`REV-000302`～`REV-000306`
- Evidence：`EVD-000544`～`EVD-000548`

## 实现

- `snapshotDocuments` 改为实例方法，以读取当前 Builder 注入的 `RawBuilderLimits`；
- null 文档检查保持在预算检查之前；
- 每个文档执行 `checkSnapshotDocumentLimit(snapshot.size(), document)` 后才允许 `snapshot.add(document)`；
- 当现有快照文档数达到 `maxCanonicalNodeCount` 时，第 N+1 个文档以 `raw.limit.node-count` 和当前文档 SourceRef 受控失败；
- 抛出失败后立即退出 iterator，不请求后续文档；
- snapshot 仍返回不可变 List；
- 后续 `ValidationBudget` 未删除、未弱化，继续累计所有根与后代节点；
- 未捕获 `Error`，未依赖真实 OOM 或无限循环；
- public API、Grammar、14 Kind、lexical、reference、depth budget 和发布模型无变化。

## Review

- `REV-000302` SpecComplianceReviewAgent：PASSED；
- `REV-000303` EngineeringStandardsReviewAgent：PASSED；
- `REV-000304` ArchitectureReviewAgent：PASSED；
- `REV-000305` SecurityReviewAgent：PASSED；
- `REV-000306` TDDReviewAgent / GREEN：PASSED。

所有新增和修改的 `@Override` 均独占一行；方法及资源边界关键逻辑使用中文注释。未修改 Context、Source Graph、Canonical API、XML/YAML Frontend 生产代码；TypedKey、SymbolTable、Pipeline 与 T07 未启动。
