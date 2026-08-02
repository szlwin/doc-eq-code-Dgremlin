# TASK-P1-T03 REWORK I004

- Task：`TASK-P1-T03`
- Iteration：`I004`
- 状态：`COMPLETED`
- 分支：`feature/p1-t03-source-graph-20260802-1430`
- Rework base：`0a845817c90d201b834df6f581c5461b3ebac880`
- 原 Completion：`COMPLETION-P1-T03-R03@cedf22bb14ff`（被独立 Review 推翻，历史保留）
- Design：`DESIGN-R16@P1-T03-REWORK-I004`
- Plan：`TP-P1-COMPILER-F01-R12@P1-T03-REWORK-I004`
- TDD：`TDD-P1-T03-R04@282951053978`
- Architecture Skeleton：`DEVSKEL-P1-T03-R04@01e8b7aa0e61`
- Development：`DEV-P1-T03-R04@04bfb86c9bf1`
- Code Review：`CODEREVIEW-P1-T03-R04@04bfb86c9bf1`
- Testing：`TESTING-P1-T03-R04@04bfb86c9bf1`
- Completion：`COMPLETION-P1-T03-R04@04bfb86c9bf1`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## Findings

- `FND-P1-T03-I004-001` / P1：删除前导字面量或编码当前目录段会把相对引用提升为绝对 URI，绕过 Provider 前安全门禁。`CLOSED`
- `FND-P2-T03-I004-002` / P2：Resolver 根 SourceRef、策略验证和 Provider 判空仍位于统一受控失败边界外。`CLOSED`

## 验证结果

1. 有效 RED：`282951053978faf8d7036be02c162622993639f8`，P0 Run `30740438344`；
2. Architecture Skeleton：`01e8b7aa0e612244b8416302d570b9967bcb4a53`，P0 Run `30740576933`；
3. Clean-code Head：`04bfb86c9bf1accf879a729b3ceb04e1eee46f86`；
4. Development P0 Run：`30740667853`；
5. Artifact：`8831175314`；
6. Artifact SHA-256：`af2820e792436d885d24ca91fbae7318445d0d22d64a84fda317e7442fd2ce6f`；
7. Context：26/26；Compiler：78/78；I004：4/4；
8. 12 模块 Reactor、Java 8、故意失败阻断：PASSED；
9. MySQL：`SKIPPED_NOT_APPLICABLE`；
10. Review：`REV-000174`～`REV-000184`；Evidence：`EVD-000415`～`EVD-000427`；
11. 开放 P0/P1：0；
12. PR #18 未合并，TASK-P1-T04 未启动。

## 完成合同

- canonicalization 前后 `URI.isAbsolute()` 保持一致；
- 字面量和编码前导当前目录段统一形成稳定相对 canonical key；
- 四种可提升相对根均返回 `MIX_SOURCE_PATH_ESCAPE`、空 graph、Provider 0 次访问；
- 根 SourceRef、策略验证、Provider 判空和 Discovery 全部进入受控边界；
- `root == null`、`policy == null` 保持显式参数异常；
- 绝对 URI 点段、编码父目录、编码分隔符、query/fragment、图和位置合同未回归；
- `SourceReference` 保持唯一实例字段；
- `@Override` 独占一行，方法与重要逻辑使用中文注释；
- 未修改 Context 生产代码，未实现 TASK-P1-T04。
