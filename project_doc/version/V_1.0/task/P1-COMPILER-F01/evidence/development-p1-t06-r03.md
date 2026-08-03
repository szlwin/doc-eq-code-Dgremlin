# TASK-P1-T06 I003 Development Evidence

- Revision：`DEV-P1-T06-R03@432ccdc1103f`
- Clean-code Head：`432ccdc1103f0119230858e7ae2343529af6c294`
- Architecture Skeleton：`DEVSKEL-P1-T06-R03@35357c213fdc`
- TDD RED：`TDD-P1-T06-R03@ea1701deb923`
- Finding：`FND-P1-T06-I003-001` — `CLOSED`

## 实现

`RawDefinitionBuilder.build(List)` 现在执行：

1. `snapshotDocuments(documents)` 单次迭代复制调用方输入；
2. 对复制过程逐项拒绝 null；
3. 对复制结果判断 empty；
4. 使用 `Collections.unmodifiableList(snapshot)` 冻结容器；
5. `validateDocuments(snapshot)`；
6. extraction 只遍历同一 `snapshot`；
7. `sourceOrdinal` 只由 snapshot 顺序产生；
8. RuntimeException catch 只调用 `firstSourceRef(snapshot)`，不再访问原始 `documents`。

快照读取期间若 RuntimeException 发生在方法返回前，局部 `snapshot` 仍为 null，稳定返回 `raw.build.failed` 与 UNKNOWN_SOURCE，不发布部分集合。

## 范围

- 修改生产代码：仅 `dec-core-compiler/.../RawDefinitionBuilder.java`；
- 新增测试：仅 `RawInputSnapshotReworkTest.java`；
- public API：无变化；
- Grammar、14 Kind、lexical、reference、depth/node limits、Raw 值对象：无变化；
- 未修改 Context、Source Graph、Canonical public API、XML/YAML Frontend 生产代码；
- TypedKey、SymbolTable、引用解析、Deferred、Pipeline、Digest、Publication、TASK-P1-T07：未启动。

## 编码规范

- 所有新增和修改的 `@Override` 均独占一行；
- `build` 输入冻结、异常边界、测试 side-effecting List 等方法和重要逻辑均使用中文注释；
- 未添加线程调度、时间阈值、测试专用生产分支或静态可变状态。

## Reviews

- `REV-000289` SpecComplianceReviewAgent — `PASSED` / `EVD-000531`；
- `REV-000290` EngineeringStandardsReviewAgent — `PASSED` / `EVD-000532`；
- `REV-000291` ArchitectureReviewAgent — `PASSED` / `EVD-000533`；
- `REV-000292` SecurityReviewAgent — `PASSED` / `EVD-000534`；
- `REV-000293` TDDReviewAgent GREEN — `PASSED` / `EVD-000535`。
