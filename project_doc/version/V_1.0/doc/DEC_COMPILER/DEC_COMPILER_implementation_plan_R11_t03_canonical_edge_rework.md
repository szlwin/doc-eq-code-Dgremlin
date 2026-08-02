# DEC Compiler Implementation Plan R11 — T03 Canonical Edge Rework

- Revision：`TP-P1-COMPILER-F01-R11@P1-T03-REWORK-I003`
- Design input：`DESIGN-R15@P1-T03-REWORK-I003`
- Review input：`REV-000163`

## 执行顺序

1. **TDD RED**
   - 新增独立 Oracle，覆盖仅点段根引用、opaque/hierarchical `%2e`、编码等价重复声明、编码环路和 CRLF/CR 七边位置。
   - RED 必须是行为失败；生产和测试源码保持 Java 8 可编译；既有 Context 26 项和 Compiler 68 项保持通过。
2. **Architecture Skeleton**
   - 冻结 raw segment 分类器与 canonical 非空恢复规则。
   - 可先使值对象与编码 key 转绿，Resolver 受控边界和换行 Oracle允许保持受控 RED。
3. **Development**
   - 对 raw segment 仅识别一次解码后为 `.` 或 `..` 的点段；只删除单点段。
   - 仅点段相对路径统一为 `.`。
   - 根 SourceRef、策略验证和 Discovery 纳入统一受控边界。
   - 增加 LF、CRLF、CR 的位置回归。
4. **Independent Review**
   - Specification、Architecture、Security、Code、TDD 五类独立 Review。
   - 重点确认 `%2F` 不改变路径结构、`%2e%2e` 不被消除、Provider 前稳定拒绝非法根。
5. **Testing / Completion**
   - 全量 P0、12 模块 Reactor、Java 8 和故意失败阻断。
   - 无数据库变更时 MySQL 记为 `SKIPPED_NOT_APPLICABLE`。
   - 开放 P0/P1 为 0 后生成 R03 Completion，不覆盖 R02 历史。

## 预期 Revision

- TDD：`TDD-P1-T03-R03@<red-head>`
- Architecture Skeleton：`DEVSKEL-P1-T03-R03@<skeleton-head>`
- Development：`DEV-P1-T03-R03@<green-head>`
- Code Review：`CODEREVIEW-P1-T03-R03@<green-head>`
- Testing：`TESTING-P1-T03-R03@<green-head>`
- Completion：`COMPLETION-P1-T03-R03@<green-head>`

## 禁止项

- 不把 canonical 空字符串作为合法引用；
- 不对完整 URI 使用 URLDecoder；
- 不把 `+` 解释为空格；
- 不解码 `%2F` 或 `%5C` 后重新分段；
- 不消除解码后为 `..` 的 segment；
- 不用异常替代 SourceGraphResolutionResult 的无效引用失败；
- 不合并 PR #18；
- 不启动 T04。
