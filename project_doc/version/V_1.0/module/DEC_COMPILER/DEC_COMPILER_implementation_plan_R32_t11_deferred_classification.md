# TP-P1-COMPILER-F01-R32 — TASK-P1-T11 implementation plan

- Revision：`TP-P1-COMPILER-F01-R32@P1-T11-I001`
- Design：`DESIGN-R36@P1-T11-I001`
- Status：`PASSED`
- Base：`dev_all@f97b7e47ac0fb40209c4dc512aa15d67c19be44b`

## Sequential workflow

1. 验证 PR #25 已合并，`dev_all` 精确包含 T10 R03 Completion。
2. 从该 Head 创建 T11 独立分支和 Draft PR。
3. 冻结 R36/R32，确认提交早于有效 RED。
4. 建立仅定义 API、无运行语义的 Architecture seam，并提交 `DeferredClassificationTest`、`DeferredCompletenessTest` 阻断 Oracle。
5. 运行 P0：测试源码必须按 Java release 8 编译；RED 只能来自 T11 合同，errors=0，既有 T01-T10 保持绿色。
6. 实现 Policy、输入快照、批量 Builder、Result、Diagnostic；任一 ERROR 不发布部分 Registry。
7. 首轮 GREEN 后执行独立 Review，补充确定性、不可变、无 runtime、无静态状态和资源边界测试。
8. 运行全量 `clean verify`、故意失败门禁、12 模块 Reactor；MySQL 无配置时只记录 `SKIPPED_NOT_APPLICABLE`。
9. 下载 Artifact，独立校验 SHA-256 与全部 Surefire XML。
10. 形成 clean-code Head；其后只写 `project_doc` Evidence、Review、Revision Lock、Completion、resume/handoff。
11. 对 final documented Head 再运行 P0 与 Artifact 校验。
12. 更新新 PR 为 Ready for Review；不合并；PR 合并前 T12 保持阻断。

## Acceptance gates

- Open P0/P1/P2=`0/0/0`；
- 八种 DeferredKind 全覆盖，P2-P7 映射和稳定 reason 准确；
- 缺任一必填字段、未类型化引用、null typed ref、重复 key 或 reason 不匹配均产生 `MIX-DEFERRED-INCOMPLETE`；
- 任一错误不发布部分 Registry；
- 输入乱序不影响输出；
- 不执行权限、Information、Action/Produce、Directory、Query、SQL 或 Transaction runtime；
- 所有 `@Override` 独占一行，方法和重要逻辑使用中文注释；
- Java 8、全 Reactor、既有回归和 Artifact Evidence 通过。
