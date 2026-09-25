# TP-P1-COMPILER-F01-R31 — TASK-P1-T10 I003 implementation plan

- Revision：`TP-P1-COMPILER-F01-R31@P1-T10-REWORK-I003`
- Design：`DESIGN-R35@P1-T10-REWORK-I003`
- Status：`PASSED`
- Parent history：R30/R29 保留，不覆盖

## Sequential workflow

1. 记录独立 Review，失效 R02 Completion，PR #25 转 Draft。
2. 冻结 R35/R31，确认其提交早于 I003 RED。
3. 新增真实 Canonical 阻断性 Oracle：padded TypedKey reference 成功、Raw lexical 保留、Binding canonicalization，以及 padded path/property 与 blank reference 继续失败。
4. 运行 P0，要求测试源码 Java release 8 编译成功；RED 只能由新增 I003 合同失败产生，errors=0，既有 I001/I002 与其他模块不回退。
5. Architecture Review：确认无需新增公共 seam；结构验证器内部拆分 `hasTypedKeyReferenceLexical` 与 `hasExactPathLexical`，保持职责局部、无状态、无跨阶段改写。
6. Development：只修改 `ModelAccessStructureValidator`；`model-ref/ref@view` 使用 nonblank reference 策略，`path/ref@property` 保持 strict trimmed 策略。
7. 独立 Review：检查 Raw lexical 原值、canonical ViewKey、resolver 调用边界、原子失败、中文注释、`@Override` 独占一行及范围未扩张。
8. 全量 `clean verify`，下载 Artifact，独立校验 SHA-256 与 Surefire XML。
9. 删除任何临时 workflow/trigger；形成 clean-code Head。
10. 写入 I003 Design/TDD/Development/Review/Testing/Revision Lock/Completion Evidence，仅文档提交后再次验证最终 Head。
11. PR #25 更新为 Ready for Review；不执行 merge；PR 合并前 TASK-P1-T11 继续阻断。

## Acceptance gates

- Open P0/P1/P2=`0/0/0`。
- padded `model-ref/ref@view` 通过真实 T06/T07/T10 pipeline。
- Raw lexical 原值保持；Binding 发布 canonical ViewKey。
- padded `path/ref@property` 与 blank reference 继续 fail-closed。
- I001/I002 全部测试绿色；12 模块 Reactor 与 Java release 8 通过。
- MySQL 只允许记录 `SKIPPED_NOT_APPLICABLE`。
