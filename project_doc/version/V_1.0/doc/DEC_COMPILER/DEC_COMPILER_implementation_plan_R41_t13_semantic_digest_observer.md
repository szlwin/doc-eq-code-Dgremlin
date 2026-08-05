# TP-P1-COMPILER-F01-R41 — TASK-P1-T13 实施计划

- Revision：`TP-P1-COMPILER-F01-R41@P1-T13-I001`
- Design：`DESIGN-R45@P1-T13-I001`
- Status：`PASSED`
- Base：`dev_all@659fb74563bbe1fa1daaf4d3a0e868f702daaec6`
- Dependency：`COMPLETION-P1-T12-R07@74f402287bc4`
- Branch：`feature/p1-t13-semantic-digest-20260805-2005`

## Sequential workflow

1. 验证 `common-develop-v2.44-rc8@4787876e...`，保留既有 dirty worktree，不 reset/clean/覆盖。
2. 确认 PR #27 已合并，冻结最新 `dev_all@659fb745...` 为唯一 Base。
3. 创建 T13 独立分支，不复用 T12 分支或历史 Completion。
4. 冻结 R45/R41 与 `TASK-P1-T13-I001`；Design/Plan 必须早于有效 RED。
5. 新增 `SemanticDigestDeterminismTest`、`CompilationDeadlineTest`、`CompilationObserverTest`，先得到可编译、原因精确的有效 RED。
6. 冻结 Architecture Gate，明确新包、canonical JSON、Digest 输入闭包、Timing phase 和 observation diagnostic 边界。
7. 生产实现：
   - 新增 `dec.core.compiler.compiled.SemanticDigestInput`；
   - 新增 `CanonicalJsonWriter`；
   - 新增 `CompilerDigestService`；
   - `CompilationSession` 增加受控 observation Warning 写入口；
   - `PipelineDiagnostics` 增加 timing/state observer Warning；
   - `CompilerPipeline` 记录 DISCOVERY/PARSE/DIGEST supplemental timing，并把 Observer 异常转为 Warning。
8. 所有 `@Override` 独占一行；方法与关键排序、编码、摘要、异常边界使用中文注释。
9. 执行定向测试、T13 package、Compiler module、全 Reactor、Java release 8 和故意失败门禁。
10. 新增独立 Review Oracle：Unicode code point、escaping、版本域、空输入、快照不可变、Clock 读取次数、Observer 终态 warning、T14/T15 范围扫描。
11. 完成 clean-code Review，Open P0/P1/P2 必须为 `0/0/0`。
12. 独立下载 P0 Artifact，核对 ZIP SHA-256、Surefire XML、T13/Compiler/全量测试数量与 Errors/Skipped。
13. 登记 Design/Plan/TDD/Architecture/Development/Review/Testing/Revision Lock/Completion/Handoff/Resume。
14. clean-code Head 后只允许 `project_doc` 变化；最终 documented Head 重新执行 P0 与 Artifact 校验。
15. 创建或更新 T13 PR 为 Ready for Review，不执行合并；T14 在 PR 合并前保持阻断。

## Production files

预计新增：

- `dec-core-compiler/src/main/java/dec/core/compiler/compiled/SemanticDigestInput.java`
- `dec-core-compiler/src/main/java/dec/core/compiler/compiled/CanonicalJsonWriter.java`
- `dec-core-compiler/src/main/java/dec/core/compiler/compiled/CompilerDigestService.java`

预计修改：

- `dec-core-compiler/src/main/java/dec/core/compiler/pass/CompilationSession.java`
- `dec-core-compiler/src/main/java/dec/core/compiler/pass/PipelineDiagnostics.java`
- `dec-core-compiler/src/main/java/dec/core/compiler/pass/CompilerPipeline.java`

不得修改 `ContextPublisher`、`PublicationRequest`、`EngineContext` CAS 语义或 Starter 组装。

## Test matrix

### RED/主合同

- Source 枚举乱序；
- Definition/Deferred 插入乱序；
- SourceRef line/column 变化；
- 重复运行；
- Source 原始内容变化；
- compiler/schema/options/algorithm 版本域变化；
- Timing phase 数量与顺序；
- timing observer failure；
- transition observer failure；
- deadline/cancel 发布前阻断。

### Independent Review

- supplementary Unicode key 排序；
- quote/backslash/control character escaping；
- canonical object duplicate key 拒绝；
- semantic input 防御性快照；
- empty manifest/registry/deferred；
- Clock 不因 supplemental timing 增加读取；
- PUBLISHED 后 observer warning 可登记但不能回滚；
- FAILED 后 observer warning 不改变失败原因；
- T14/T15 类型与组装未出现。

## Validation commands

```text
./mvnw -pl dec-core-compiler -am -Dtest=SemanticDigestDeterminismTest,CompilationDeadlineTest,CompilationObserverTest test
./mvnw -pl dec-core-compiler -am test
./mvnw --batch-mode --no-transfer-progress clean verify
```

MySQL 未被 T13 改动，P0 中保持 `SKIPPED_NOT_APPLICABLE`；若仓库配置可执行额外 MySQL Profile，只作为补充证据，不替代 P0。
