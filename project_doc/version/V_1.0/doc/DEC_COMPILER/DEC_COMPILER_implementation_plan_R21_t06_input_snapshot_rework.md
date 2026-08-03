# TP-P1-COMPILER-F01-R21 — TASK-P1-T06 I003 实施计划

- Revision：`TP-P1-COMPILER-F01-R21@P1-T06-REWORK-I003`
- Status：`PASSED`
- Design：`DESIGN-R25@P1-T06-REWORK-I003`
- Execution：`SEQUENTIAL / auto / architecture_review / git_checkpoint`
- Target PR：`#21`

## 顺序流程

1. 建立 6 项确定性 `RawInputSnapshotReworkTest`，在当前生产代码上形成有效 RED；
2. 架构骨架冻结 `snapshotDocuments`、共享快照和失败边界，具体复制算法保持显式未实现；
3. ArchitectureReviewAgent 与 SpecComplianceReviewAgent 顺序复核同一 Skeleton revision；
4. 实现单次迭代复制、复制结果 empty/null 校验、不可变快照和 RuntimeException 受控边界；
5. 运行 I003、T06 Raw、Compiler、XML、YAML、Context、Demo、Legacy、完整 12 模块 Reactor 与故意失败门禁；
6. 依次执行 Specification、Engineering Standards、Architecture、Security、TDD、Test Evidence 和 Completion Review；
7. 保留 R01/R02 Completion 历史，形成 R03 Completion；
8. 更新 PR #21，但不合并；TASK-P1-T07 不启动。

## 允许文件

- `dec-core-compiler/src/main/java/dec/core/compiler/raw/RawDefinitionBuilder.java`
- `dec-core-compiler/src/test/java/dec/core/compiler/raw/RawInputSnapshotReworkTest.java`
- `project_doc/version/V_1.0/**` 中 TASK-P1-T06 I003 的 Design、Plan、Review、Evidence、Completion 与恢复事实

禁止修改：

- `dec-core-context` 生产代码；
- Source Graph、Canonical 公共 API、XML/YAML Frontend 生产代码；
- TypedKey、SymbolTable、ReferenceResolver、Pipeline、Publication；
- TASK-P1-T07 及后续任务。

## TDD RED

```bash
./mvnw -pl dec-core-compiler -am \
  -Dtest=RawInputSnapshotReworkTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

预期：测试源码与生产源码以 Java release 8 编译成功；6 项中仅依赖输入快照的行为失败，现有合同不回退。

## Architecture Skeleton

骨架必须：

- public build 首先调用 `snapshotDocuments(documents)`；
- `validateDocuments(snapshot)`；
- extraction 遍历 `snapshot`；
- RuntimeException 失败定位只读取 `snapshot`；
- `snapshotDocuments` 保持显式未实现并形成受控 RED；
- 不通过兼容分支、测试探针或再次遍历原始 List 伪造通过。

## GREEN 实现

`snapshotDocuments` 必须：

- null 输入返回 `raw.input.required`；
- 使用 enhanced-for 单次遍历复制；
- 迭代过程中逐项拒绝 null；
- 复制后判断 empty；
- 返回不可变 List；
- 不调用原始容器 `size/isEmpty/get/toArray/stream/spliterator`；
- snapshot 完成后生产路径不再访问原始容器。

## 验证命令

```bash
./mvnw -pl dec-core-compiler -am -Dtest=RawInputSnapshotReworkTest -Dsurefire.failIfNoSpecifiedTests=false test
./mvnw -pl dec-core-compiler -am test
./mvnw --batch-mode --no-transfer-progress clean verify
scripts/remediation/prove_test_failure_gate.sh
```

## Review

- `REV-000285`：PlanReviewAgent — `PASSED`；
- `EVD-000527`；
- R25/R21 必须在 RED 前冻结，后续仅引用，不得修改；
- 任一 RED 不真实、Skeleton Review 未通过、最终测试失败、开放 P0/P1、Revision 漂移或范围越界均阻断 Completion。
