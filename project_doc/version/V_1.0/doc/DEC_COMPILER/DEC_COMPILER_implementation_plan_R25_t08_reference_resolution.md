# TP-P1-COMPILER-F01-R25 — TASK-P1-T08 I001 实施计划

- Revision：`TP-P1-COMPILER-F01-R25@P1-T08-I001`
- Status：`PASSED`
- Design：`DESIGN-R29@P1-T08-I001`
- Base：`dev_all@c6cd8ec156563480ec30989cdd358d4979a8599b`
- Dependency：`COMPLETION-P1-T07-R02@ffe544e3060d`
- Branch：`feature/p1-t08-reference-resolution-20260803-2254`
- Target PR：`#23`
- Execution：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 1. 顺序流程

1. 冻结 R29/R25 与 TASK-P1-T08/I001 记录；
2. 创建最小可编译结果模型与 `ReferenceResolver` not-implemented seam；
3. 新增 `ReferenceResolverContractTest`、`DiagnosticOrderTest`；
4. 运行 P0，RED 必须是行为不满足，errors=0，既有测试继续通过；
5. 完成 Architecture Skeleton：建立 sourceKey 索引、System View 声明索引、Data property 索引和 role policy 接缝，保留受控 RED；
6. Architecture Review 通过后完成精确解析 GREEN；
7. 运行定向测试、Compiler 全量、12 模块 `clean verify`、故意失败门禁；
8. 串行执行 Specification、Engineering、Architecture、Security、TDD、Test Evidence 与 Completion Review；
9. 生成 Completion、revision lock、机器恢复入口并更新 handoff；
10. clean-code Head 与 final documented Head 分别执行 P0、下载 Artifact、独立 SHA-256 和 Surefire XML 解析；
11. 删除临时 `.github/workflows/t08-source-snapshot.yml`，最终 PR 不保留工具工作流；
12. 更新 PR #23 并转为 Ready for Review，不合并、不启动 T09。

## 2. TDD seam

RED 前新增以下最小生产 API，使测试因行为缺失而失败而不是缺类编译：

- `ReferenceResolutionStatus`；
- `ResolvedReference`；
- `ResolvedReferenceSet`；
- `ReferenceResolutionResult`；
- `ReferenceResolver.resolve(RawDefinitionSet, SymbolTable)`。

seam 固定返回 `reference.not-implemented` FAILED，且满足 Java 8、不可变、防御复制和中文注释基本结构。RED 不得通过抛异常、缺依赖或测试未发现形成。

## 3. RED Oracle

`ReferenceResolverContractTest`：legal connection/view/system/ruleView/action/directory/produce、forward RuleView、owner boundary、unknown、type mismatch、rule-system mismatch、property exact、failure no partial、immutable result。

`DiagnosticOrderTest`：多 Source、多 code、多 key 错误、输入顺序置换、完整聚合、稳定排序、重复运行一致、相同 Diagnostic 去重。

## 4. Architecture Skeleton

建立单次 resolve 状态：`sourceKeys`、仅用于失败分类的 `keysByLexicalName`、`systemViews`、`dataProperties`、完整 references 和 LinkedHashSet diagnostics。成功解析始终构造期望 TypedKey并调用 `SymbolTable.find(expectedKey)`。

## 5. GREEN 实现

- 先建立全部索引，再解析，支持前向引用；
- 使用 kind + role suffix 白名单派发；
- qualified Information 精确切分；
- Directory rel 使用同一 BusinessScope；
- Action rule 使用 system-ref 构造 RuleViewKey；
- RuleView view 必须位于 owner System 声明集合；
- View property 必须在当前绑定 Data 的 Raw body 中精确存在；
- 完整扫描后统一构造成功或失败结果。

## 6. 验证

- 定向测试；Compiler 全量；全 Reactor clean verify；故意失败门禁；
- P0 Run、Artifact ID/size/digest；独立 ZIP SHA-256 与 Surefire XML；
- clean-code 到 final documented Head 只允许 project_doc 变化；
- R29/R25 在 RED 前创建且最终 blob 不变；
- 最终 PR 不含临时 snapshot workflow。

## 7. 停止条件

需要修改 T06/T07/Context 公共合同、RED 为环境失败、成功使用模糊搜索、失败暴露部分结果、Diagnostic 不完整/不稳定、侵入 T09/T10/P2～P7、编码规范或门禁失败时立即阻断 Completion。

## 8. Review

- `REV-000342` — PlanReviewAgent — `PASSED`；
- Evidence：`EVD-000589`；
- R29/R25 在 RED 前冻结，后续只引用、不覆盖。
