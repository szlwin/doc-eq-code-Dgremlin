# TASK-P1-T03 REWORK I004

- Task：`TASK-P1-T03`
- Iteration：`I004`
- 状态：`IN_PROGRESS`
- 分支：`feature/p1-t03-source-graph-20260802-1430`
- Rework base：`0a845817c90d201b834df6f581c5461b3ebac880`
- 原 Completion：`COMPLETION-P1-T03-R03@cedf22bb14ff`（被独立 Review 推翻，历史保留）
- Design：`DESIGN-R16@P1-T03-REWORK-I004`
- Plan：`TP-P1-COMPILER-F01-R12@P1-T03-REWORK-I004`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## Findings

- `FND-P1-T03-I004-001` / P1：删除前导字面量或编码当前目录段会把相对引用提升为绝对 URI，绕过 Provider 前安全门禁。
- `FND-P2-T03-I004-002` / P2：Resolver 根 SourceRef、策略验证和 Provider 判空仍位于统一受控失败边界外。

## 门禁

1. 新 Oracle 必须先形成有效 RED；
2. 既有 Context 26 项与 Compiler 74 项不得回归；
3. canonicalization 前后 `URI.isAbsolute()` 必须保持；
4. 四种前导点段相对引用必须稳定返回 `MIX_SOURCE_PATH_ESCAPE`；
5. graph 为空且 Provider access 0；
6. 根 SourceRef、策略验证、Provider 判空和 Discovery 全部进入受控边界；
7. `root == null`、`policy == null` 保持显式参数异常；
8. 绝对 URI 点段、编码父目录、编码分隔符、query/fragment、图和位置合同不得回归；
9. `SourceReference` 保持唯一实例字段；
10. `@Override` 独占一行，方法与重要逻辑使用中文注释；
11. Java 8、12 模块 Reactor 和失败阻断门禁通过；
12. 开放 P0/P1 阻断 Completion；
13. PR #18 未经授权不得合并；
14. TASK-P1-T04 保持未启动。
