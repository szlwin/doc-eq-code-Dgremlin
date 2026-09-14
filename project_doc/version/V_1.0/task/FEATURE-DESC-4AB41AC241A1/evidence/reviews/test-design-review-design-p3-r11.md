# 人工 Review 确认单

<!-- manual-review-meta
{
  "schema_version": 1,
  "assertion_id": "ASRT-REVIEW-FE25B90ECEBC31E6055E04FA",
  "acceptance_id": "AC-P3-INFORMATION-ENGINE-001",
  "reviewer_agent": "TestDesignAgent",
  "review_phase": "design",
  "artifact_revision": "DESIGN-P3-R11",
  "assertion_phase": "design",
  "assertion_revision": "DESIGN-P3-R11",
  "profile_id": "design:TestDesignAgent",
  "mode": "MARKDOWN",
  "drafted_by_agent": "",
  "context_digest": "7f9d1a846e9bc32e9762ab6aab9ec0ccb244ae77a6d7100dc09defcdfcfd4feb"
}
-->

## 基本信息（系统生成，请勿修改）

- Review：专业 Review
- Assertion：`ASRT-REVIEW-FE25B90ECEBC31E6055E04FA`
- Acceptance：`AC-P3-INFORMATION-ENGINE-001`
- Reviewer：`TestDesignAgent`
- Review 产物：`design@DESIGN-P3-R11`
- 验收产物：`design@DESIGN-P3-R11`
- 输入模式：`MARKDOWN`

## 检查项

每题只勾选一项；发现未解决问题时选择“否”，证据不足时选择“无法判断”。

### [MRQ-SCOPE] 本次变更的职责、范围和边界是否清楚且一致？

关联 criterion：`RC-DES-010`、`RC-DES-002`、`RC-DES-003`、`RC-DES-004`、`RC-DES-005`、`RC-DES-006`、`RC-DES-007`、`RC-DES-008`、`RC-DES-009`、`RC-DES-011`、`RC-ENG-001`

- [ ] 是
- [x] 否
- [ ] 无法判断

说明（选择“否”或“无法判断”时必填）：

> **P1 / RC-DES-010、RC-DES-002、RC-DES-005、RC-DES-008、RC-DES-011：R11 的测试接缝和跨阶段事实仍存在不可验证的关键歧义。**
>
> 1. **绑定输入与 R11 终点冲突。** 当前设计（EVD-000140，尤其第 1、5、7.2、13、14 节）定义：P3 只输出 `MaterializationTargetFact`；Directory 只提供有序 `RuleViewInfo`；调用方对同一 `ModelContainer` 多次 `load` 后只 `execute` 一次；成功不产生 `MaterializationResult`，Directory/调用方不依赖 `DirectoryAction`、`Listener` 或 `getResult()`。但设计声明的输入 `REQAN-P3-R06`/`BM-P3-R05`/`FLOW-R08`（EVD-000141～EVD-000143）仍把 `Action + change-data` 作为共享事务主体，并把成功终点定义为 `grammer → update → commit → MaterializationResult`；需求验收还要求返回该结果。这样同一 `AC/TR` 下无法确定测试应断言“无 `MaterializationResult` 的容器正常返回”还是“有 `MaterializationResult` 的物化成功”，也无法确定 Action 是否在当前设计路径中。R11 必须先由授权的需求/模型/Flow revision 统一终点与依赖，再以同一 revision 重绑测试接缝；本 Review 不替其修改上游事实。
>
> 2. **`RuleViewInfo` 对象到真实执行链没有稳定的对象级 seam。** R11 要求 Directory 返回有序 `RuleViewInfo`，调用方为每项创建 `ModelLoader`；但真实 `ModelLoader` 只接收 `String ruleName + ModelData + connection`（EVD-000145），`RuleContainer` 再通过 `DataUtil.getRuleViewInfo(ruleName)` 从全局 `ConfigInfo` 查找对象（EVD-000146～EVD-000147），而 `DirectoryParser` 当前仅解析 `Action`/`change`，没有 R11 所称的有序 `RuleViewInfo` 输出方法（EVD-000148）。设计没有冻结“返回列表的具体接口、每个 Loader 如何绑定该列表元素、是否按对象身份还是全局名称绑定、候选 ConfigInfo 的生命周期/隔离与清理”的可观察契约。因此无法可靠断言每个输入对象恰好对应一个 Loader、Loader 顺序等于 Directory 顺序，或防止同名/全局注册污染；“Directory 不创建 Loader/不调用 execute”也没有具体可拦截调用点。需要在设计中给出真实文件/方法/字段级的绑定 seam 与生命周期，再定义对应集成断言。
>
> 3. **失败与多连接观察契约不够精确。** 真实 `ModelContainer.execute()` 在 `ResultInfo.isSuccess()==false` 时只 break，随后 `end(false)` rollback/close/clear，并在当前代码路径正常 return；只有异常路径才形成 `ExecuteRuleException`（EVD-000144，76-137、204-315）。R11 虽写“clear 前保存失败并在收尾后抛既有 `ExecuteRuleException`”，但未规定 unsuccessful 结果的异常类型/主错误字段、rollback/close 自身失败时的优先级与 suppressed 关系，也未把“首失败 Loader 的结果、后续 Loader 未调用、每个连接的 commit/rollback/close 次数及顺序”冻结为可观察事件。R11 仅说多连接沿用现有逐连接语义且不宣称分布式原子性，不能替代这些测试 oracle；尤其 `FastMap` 的连接遍历顺序也没有契约。应补充失败注入点、事件记录格式、异常主/从关系和多连接逐连接结果。
>
> **其余核对结论：** R11 对 `MaterializationTargetFact` 不应包含 `ChangeInfo`/`RuleViewInfo`、P3 编译全量原子发布、`TRUE/FALSE/ERROR`、只读 evaluate、编译期路径/权限/类型校验、实时读取、`every(emptyCollection)=TRUE` 与订单明细非空、以及不依赖 `getResult`/`Listener`/`DirectoryAction` 的意图描述是明确的（EVD-000140）。但上述 P1 缺口使这些声明尚不能组成无歧义、可端到端验证的当前设计测试契约。

## 推荐证据

脚本仅列出当前 Review phase/revision 且类型适用的 ACTIVE evidence。

- [x] EVD-000135 | design_ref | `evidence/objects/e1a5746a01be0bce505dbd47b6d07bff9d1cd7926200f732dbba0bf49ef192d6.md`
- [x] EVD-000140 | design_ref | `../../doc/FEATURE-DESC-4AB41AC241A1/FEATURE-DESC-4AB41AC241A1_design.md`
- [x] EVD-000136 | code_ref | `evidence/objects/861be5f3d20cb37ee923b9f947d15062ad6f5b0231e112b17189c607efbbaa68.tar.xz`
- [x] EVD-000144 | code_ref | `git:f29f7a6052672283c989528988ab9a8e2cdb5b41`
- [x] EVD-000145 | code_ref | `git:f29f7a6052672283c989528988ab9a8e2cdb5b41`
- [x] EVD-000146 | code_ref | `git:f29f7a6052672283c989528988ab9a8e2cdb5b41`
- [x] EVD-000147 | code_ref | `git:f29f7a6052672283c989528988ab9a8e2cdb5b41`
- [x] EVD-000148 | code_ref | `git:f29f7a6052672283c989528988ab9a8e2cdb5b41`

## 主要结论

> **NEEDS_CHANGES。** 本次是只读独立 Test Design Review，绑定 `design:TestDesignAgent` Profile、`DESIGN-P3-R11`、`AC-P3-INFORMATION-ENGINE-001` 与 `TR-P3-INFORMATION-ENGINE-001`。设计对 P3 编译/发布闭包、`MaterializationTargetFact` 的边界、只读实时 `TRUE/FALSE/ERROR`、以及多 Loader/单次 execute 的目标行为已有较完整叙述；但跨 revision 的 `MaterializationResult`/Action 终点冲突，以及缺少从 Directory 返回的 `RuleViewInfo` 到真实全局名称查找执行链的稳定绑定 seam，均为 P1，阻断测试设计可验证性。真实当前代码还证明 unsuccessful 结果目前会 rollback/clear 后正常返回，R11 未把修订后的异常和多连接事件 oracle 冻结到足以独立断言的契约。应先修订并重新发布 design/需求模型 Flow 的一致 revision，补充具体绑定与失败/多连接观察契约，再重跑本 Profile。

## 非阻断补充说明

此处只能记录不影响当前结论的范围说明；存在未解决问题时必须在对应检查项选择“否”或“无法判断”。

- 本 Review 未创建、未修改或执行任何测试 Case、测试代码、XML 或生产代码；未执行 RequirementReview、ArchitectureReview、实现、测试设计阶段任务或后续任务。
- 本 Review 未修改 canonical design；Review 记录及 Evidence/StageOutcome 生命周期事实由 `manual_review.py submit` 统一登记。
- 发现项为 P1：跨 revision 终点冲突、对象级执行接缝缺失；并记录失败传播和多连接 oracle 的补充缺口。不要在本记录中关闭或复制既有 Issue。
