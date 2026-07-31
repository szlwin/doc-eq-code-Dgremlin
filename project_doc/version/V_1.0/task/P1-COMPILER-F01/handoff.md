# P1-COMPILER-F01 阶段交接

## 已完成

- requirement_confirmation I004 已完成：`REQCONF-R04@c186ce681e1e`；
- requirement_analysis I005 已完成：`REQAN-R05@7de35e8dc15b`；
- business_model I005 已完成：`BM-R05@4ecb1f8c09f4`；
- design I007 已完成：`DESIGN-R05@0b37a9b4dd48`；
- Design StageOutcome：`SO-P1-COMPILER-F01-DESIGN-I007`，PASSED；
- Design Review：`REV-000050`～`REV-000056` 全部 PASSED；
- Design Evidence：`EVD-000284`；
- Design Git checkpoint：`CP-V_1.0-P1-COMPILER-F01-DESIGN-007`；
- 开放 P0/P1：0。

## 下一阶段

- 下一阶段：`test_design` I007；
- 下一 Agent：`TestDesignAgent`；
- 下一 Task：`TASK-P1-R2-005`；
- 输入 Revision：`DESIGN-R05@0b37a9b4dd48`，并同时绑定 `REQCONF-R04`、`REQAN-R05`、`BM-R05`；
- 执行模式：SEQUENTIAL；
- Reviewer：DesignReviewAgent、RequirementReviewAgent、TDDReviewAgent、TestEvidenceReviewAgent，按登记顺序串行执行。

## 测试设计目标

1. 将 9 条 TR 映射为可执行 Case 和非测试验证；
2. 覆盖 10 个 SourceManifest source、7 条 declaration edge 和固定 mix inventory；
3. 覆盖 Canonical/Raw、TypedKey、System-owned Information、common expression 和 ModelAccess selector；
4. 覆盖 23 个稳定错误、Diagnostic 排序、失败不发布、timeout、cancel 和 CAS conflict；
5. 明确 TDD RED 接缝、数据、预期结果、禁止副作用和 Evidence 采集方式；
6. 不编写生产实现，不提前实现 P2～P7 运行语义。

## 冻结边界

- `dec-expand-declaration` 整体退役，不抽取、不迁移、不建立 Adapter；
- Information 由 System 拥有，BusinessScope 只编排；
- 跨 System expression 只归 `common`；
- `common` 不拥有 Data、View、RuleView 或 ModelAccess；
- selector 先精确匹配 `target-main`，再精确回退 property path；
- 禁止模糊匹配、跨 View 搜索、root-property 和静默降级；
- P1 只建立 P2～P7 Deferred，不执行其运行时语义。

## 进入后续阶段的门禁

TESTDESIGN Revision、四类独立 Review、Evidence、StageOutcome 和 Git checkpoint 全部通过后，才允许进入 `implementation_plan` I007；在此之前不得执行 TDD 或开发。
