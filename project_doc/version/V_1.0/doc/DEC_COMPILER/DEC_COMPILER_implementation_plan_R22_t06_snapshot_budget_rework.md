# TP-P1-COMPILER-F01-R22 — TASK-P1-T06 I004 实施计划

- Revision：`TP-P1-COMPILER-F01-R22@P1-T06-REWORK-I004`
- Status：`PASSED`
- Design：`DESIGN-R26@P1-T06-REWORK-I004`
- Execution：`SEQUENTIAL / auto / architecture_review / git_checkpoint`
- Target PR：`#21`

## 顺序流程

1. 新增 `RawSnapshotBudgetReworkTest` 的 8 项确定性 Oracle，在 I003 生产代码上形成有效 RED；
2. Architecture Skeleton 将 `snapshotDocuments` 改为实例边界并冻结“取到当前文档后、add 前检查限制”的主线，限制判断先保持显式未实现；
3. ArchitectureReviewAgent 与 SpecComplianceReviewAgent 顺序复核同一 Skeleton revision；
4. 实现 snapshot 分配前文档数硬上限，并保留后续完整树 `ValidationBudget`；
5. 运行 I004、I003、全部 T06 Raw、Compiler、XML、YAML、Context、Demo、Legacy、完整 12 模块 Reactor 与故意失败门禁；
6. 依次执行 Specification、Engineering Standards、Architecture、Security、TDD、Test Evidence 和 Completion Review；
7. 保留 R01/R02/R03 Completion 历史，形成 R04 Completion；
8. 更新 PR #21，但不合并；TASK-P1-T07 不启动。

## 允许文件

- `dec-core-compiler/src/main/java/dec/core/compiler/raw/RawDefinitionBuilder.java`
- `dec-core-compiler/src/test/java/dec/core/compiler/raw/RawSnapshotBudgetReworkTest.java`
- `project_doc/version/V_1.0/**` 中 TASK-P1-T06 I004 的 Design、Plan、Review、Evidence、Completion 与恢复事实

禁止修改：

- `RawBuilderLimits` 的生产值；
- `dec-core-context` 生产代码；
- Source Graph、Canonical 公共 API、XML/YAML Frontend 生产代码；
- TypedKey、SymbolTable、ReferenceResolver、Pipeline、Publication；
- TASK-P1-T07 及后续任务。

## TDD RED

```bash
./mvnw -pl dec-core-compiler -am \
  -Dtest=RawSnapshotBudgetReworkTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

预期：Java release 8 编译成功；两个文档边界、完整树预算和无部分集合等既有行为通过；第三文档前置限制、精确 SourceRef、停止读取和禁用容器入口形成目标 RED。

## Architecture Skeleton

骨架必须：

- `snapshotDocuments` 为实例方法；
- 继续只使用 enhanced-for 单次 iterator；
- null 检查先于预算检查；
- 预算检查位于 `snapshot.add(document)` 之前；
- 触发节点上限时使用当前文档 SourceRef；
- 具体上限判断保持显式未实现，不能通过修改测试或捕获 Error 伪造通过；
- I003 七项 snapshot Oracle 和 I002 三十八项 Raw 合同不得回退。

## GREEN 实现

```java
if (snapshot.size() >= limits.maxCanonicalNodeCount()) {
    throw failure("raw.limit.node-count", document.sourceRef());
}
snapshot.add(document);
```

实现要求：

- 检查发生在 add 前；
- 第 N+1 个文档触发失败；
- 失败后 enhanced-for 立即退出，不再调用 iterator；
- snapshot 返回 `Collections.unmodifiableList`；
- 完整树 `ValidationBudget` 保持不变；
- 不读取原始 List 的其他入口。

## 验证命令

```bash
./mvnw -pl dec-core-compiler -am -Dtest=RawSnapshotBudgetReworkTest -Dsurefire.failIfNoSpecifiedTests=false test
./mvnw -pl dec-core-compiler -am test
./mvnw --batch-mode --no-transfer-progress clean verify
scripts/remediation/prove_test_failure_gate.sh
```

## Review

- `REV-000298`：PlanReviewAgent — `PASSED`；
- `EVD-000540`；
- R26/R22 必须在 RED 前冻结，后续仅引用，不得修改；
- 任一 RED 不真实、Skeleton Review 未通过、测试失败、开放 P0/P1、Revision 漂移、范围越界或 Error 捕获均阻断 Completion。
