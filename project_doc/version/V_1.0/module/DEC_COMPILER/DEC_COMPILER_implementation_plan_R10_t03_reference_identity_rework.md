# DEC Compiler Implementation Plan R10 — T03 Reference Identity Rework

- Revision：`TP-P1-COMPILER-F01-R10@P1-T03-REWORK-I002`
- Design input：`DESIGN-R14@P1-T03-REWORK-I002`
- Review input：`REV-000152`

## 执行顺序

1. **TDD RED**
   - 新增独立 Oracle，覆盖 opaque `.` 规范化、canonical Provider key、等价重复声明、`sourceId != uri` 环路和声明 `<` 起始位置。
   - RED 必须是行为失败，生产与测试源码保持 Java 8 可编译，既有 62 项 Compiler 测试保持通过。
2. **Architecture Skeleton**
   - 冻结 `SourceReference` canonical key 规则和声明位置定位器边界。
   - 环路仍允许保持受控 RED，但不得引入缺类、缺方法或 fixture 失败。
3. **Development**
   - 将 ancestor stack 改为 canonical reference key。
   - Provider、edge、重复键、排序与图相等性统一使用 canonical reference。
   - Source ID 继续只用于 Manifest 与 Diagnostic 身份。
   - 从原始文本定位声明 start tag `<`。
4. **Independent Review**
   - Specification、Architecture、Security、Code、TDD 五类 Review 独立执行。
   - 重点检查 traversal 不会因 canonicalization 被隐藏，以及 cycle 在 Provider 前阻断。
5. **Testing / Completion**
   - 全量 P0、12 模块 Reactor、Java 8、故意失败测试阻断。
   - MySQL 无数据库变更时记为 `SKIPPED_NOT_APPLICABLE`。
   - 开放 P0/P1 为 0 后生成新 Completion，不覆盖 R01 历史。

## 预期 Revision

- TDD：`TDD-P1-T03-R02@<red-head>`
- Architecture Skeleton：`DEVSKEL-P1-T03-R02@<skeleton-head>`
- Development：`DEV-P1-T03-R02@<green-head>`
- Code Review：`CODEREVIEW-P1-T03-R02@<green-head>`
- Testing：`TESTING-P1-T03-R02@<green-head>`
- Completion：`COMPLETION-P1-T03-R02@<green-head>`

## 禁止项

- 不通过修改测试放宽 10 Source / 7 Edge 合同；
- 不把 sourceId 强制改成 URI；
- 不在 canonicalization 中消除 `..` 后再做安全校验；
- 不使用 StAX 标签末尾列号冒充起始列；
- 不合并 PR #18；
- 不启动 T04。
