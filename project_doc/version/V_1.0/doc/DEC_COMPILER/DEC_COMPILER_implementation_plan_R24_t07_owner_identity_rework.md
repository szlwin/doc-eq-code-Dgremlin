# TP-P1-COMPILER-F01-R24 — TASK-P1-T07 I002 实施计划

- Revision：`TP-P1-COMPILER-F01-R24@P1-T07-REWORK-I002`
- Status：`PASSED`
- Design：`DESIGN-R28@P1-T07-REWORK-I002`
- Base：`dev_all@3e0492b0319173c87abff6952d4dad0f5507c31c`
- Rework Base：`43846e2d2e2c8b174fb87cdeb15e16c37392f505`
- Branch：`feature/p1-t07-symbol-table-20260803-1958`
- PR：`#22`
- Superseded Completion：`COMPLETION-P1-T07-R01@7f4ee8a0ee5a`
- Execution：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 1. 顺序流程

1. 保留 R01/R27/R23 全部历史，创建 I002 Rework 记录；
2. 新增可编译的 `DiagnosticAccumulator` TDD seam，初始仍使用线性扫描；
3. 新增 lexical owner、RuleView 顺序独立和 Diagnostic 聚合 Oracle；
4. 运行标准 P0，RED 必须仅来自三个 Review Finding，Java 8 与既有回归继续通过；
5. 提交 Architecture Skeleton：分离 lexical/key 上下文，建立 RuleView 延迟登记接缝，DiagnosticAccumulator 接入 Builder，但保留受控 RED；
6. Skeleton 独立 Review 通过后完成 GREEN；
7. 执行 T07 定向、Compiler 全量和 12 模块 Reactor；
8. 串行完成 Specification、Engineering、Architecture、Security、TDD、Test Evidence 与 Completion Review；
9. 生成 `COMPLETION-P1-T07-R02`、revision lock、机器恢复入口；
10. clean-code Head 和最终文档化 Head 分别运行 P0并独立校验 Artifact；
11. 更新 PR #22 标题、正文和顶层完成说明，转为 Ready for Review；
12. 不合并 PR，不启动 T08。

## 2. TDD RED

新增测试：

- `SymbolOwnerIdentityReworkTest`
  - padded System/Information；
  - padded BusinessScope/Directory/Action/Produce；
  - RuleView 位于 System 前后；
  - RuleView 指向非最近 System；
  - 多 RuleView、多 System、同名隔离；
  - missing System 失败语义；
  - 文档顺序置换结果相同；
  - Raw lexical 保留、TypedKey canonical 化。
- `DiagnosticAccumulatorReworkTest`
  - 小预算 N 次报告等于 N 次去重插入步骤；
  - 重复 Diagnostic 只保留一次；
  - Builder 能完整收集第一、第二阶段不同 Finding。

为避免缺类编译错误，RED 前先提供包内 `DiagnosticAccumulator` seam。seam 使用显式线性扫描并暴露测试只读步骤计数，使 P2 形成真实行为 RED；不得提前使用 Set 完成实现。

有效 RED 要求：

- 新测试 failure，errors=0；
- 失败原因精确绑定 lexical/canonical、RuleView recent-system 和 quadratic accumulator；
- R01 Symbol 23/23 继续通过；
- Context、Raw、XML、YAML 与 Compiler 既有测试无回退；
- Java release 8 编译成功。

## 3. Architecture Skeleton

Skeleton 必须建立但不伪造完成：

- `OwnerContext` 同时保存 TypedKey 与 raw lexical name；
- System、Scope、Directory、Action 的 ordinal 恢复同时恢复两类事实；
- RuleView 从正常扫描路径移出并进入 deferred list；
- 第一遍结束后存在独立 `registerRuleViews` 接缝；
- `RegistrationState` 使用 `DiagnosticAccumulator`；
- 所有状态局限于单次 build；
- 允许保留一个或多个明确 `not-implemented` Diagnostic 作为受控 RED。

Skeleton Review 必须确认：

- 没有修改 Context/T06 合同；
- RuleView 不再读取最近 System；
- lexical equals 与 TypedKey canonical lookup 分工清楚；
- Diagnostic 去重最终可替换为 O(1) hash add；
- 不实现 T08；
- `@Override` 独占一行，重要方法与逻辑有中文注释。

## 4. GREEN 实现

### lexical owner

- 进入 System/Scope/Directory/Action 时同时记录 raw name 与 Key；
- Information、Directory、Action 使用对应 raw lexical parent 比较；
- Produce 使用 raw directory/action composite 比较；
- TypedKey 使用 raw name 构造并由 Context canonicalize；
- RawDefinition 对象不复制、不改写。

### RuleView

- 第一次扫描登记全部 System；
- RuleView 原始定义按 sourceOrdinal 暂存；
- 扫描完成后按每个 RuleView 自身 ownerToken 创建目标 SystemKey；
- 目标 SystemKey 必须已存在且对应 Raw kind 为 SYSTEM；
- 不存在时产生 `symbol.owner.system.missing`；
- 存在时登记 `RuleViewKey(targetSystemKey, rawRuleViewName)`；
- RuleView 顺序不得改变最终 SymbolTable。

### Diagnostic

- `DiagnosticAccumulator` 最终使用 `LinkedHashSet<Diagnostic>`；
- 每次 add 只增加一次 add-attempt 计数并调用一次 Set.add；
- 快照 defensive copy；
- `SymbolBuildResult` 继续负责 compareTo 排序；
- 禁止时间阈值和真实 65,536 压测。

## 5. 验证

至少运行并记录：

- I002 新增测试；
- R01 Symbol 全量；
- `dec-core-compiler` 全量；
- Maven `clean verify`；
- 故意失败测试阻断证明；
- Artifact ZIP SHA-256 独立比对；
- Surefire XML 独立解析；
- clean-code Head 到 final documented Head 仅允许 project_doc 变化；
- R28/R24 blob 在 RED 前创建且最终未变化。

## 6. Scope 与停止条件

允许：

- `dec-core-compiler/src/main/java/dec/core/compiler/symbol/**`
- `dec-core-compiler/src/test/java/dec/core/compiler/symbol/**`
- T07 I002 对应 project_doc 文件

立即停止并阻断：

- 需要修改 Context 或 Raw 公共合同；
- RED 来自编译/缺类/依赖错误；
- RuleView 仍依赖扫描上下文；
- raw lexical 被 trim 或重写；
- Diagnostic 仍存在 List.contains 二次路径；
- 失败发布部分表；
- 开放 P0/P1 不为 0；
- Java 8、P0、Artifact 或 Revision Integrity 失败；
- 发现 T08 或范围越界实现。

## 7. Review

- `REV-000327` — PlanReviewAgent — `PASSED`；
- Evidence：`EVD-000570`；
- R28/R24 在 RED 前冻结，后续只引用，不覆盖。
