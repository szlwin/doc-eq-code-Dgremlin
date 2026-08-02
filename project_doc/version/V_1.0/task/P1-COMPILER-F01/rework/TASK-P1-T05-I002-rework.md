# TASK-P1-T05 / I002 — YAML 来源事实 Rework

- 状态：`IN_PROGRESS`
- PR：`#20`（Draft）
- Branch：`feature/p1-t05-yaml-canonical-20260802-2106`
- Rework Base：`52fe48d46dd2c4ac9c822d5be141d47c03ae955f`
- Dependency：`COMPLETION-P1-T04-R02@0699c6bc2ed4`
- Superseded Completion：`COMPLETION-P1-T05-R01@040f09b80463`
- Design：`DESIGN-R21@P1-T05-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R17@P1-T05-REWORK-I002`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 独立 Review

- Review：`REV-000231`
- Evidence：`EVD-000475`
- 结论：`NEEDS_CHANGES / REWORK`
- I001 状态：历史记录不可变保留，当前有效性被本 Review 推翻

## Findings

- `FND-P1-T05-I002-001` `[P1][BLOCKER]`：非法 UTF-8 被替换后继续解析；
- `FND-P1-T05-I002-002` `[P1][BLOCKER]`：显式标准 typed tag 缺少来源词法约束；
- `FND-P1-T05-I002-003` `[P2]`：节点名称可包含路径分隔符和换行，nodePath 不可逆。

## 冻结修复

1. 原始 byte[] 使用严格 UTF-8 decoder，malformed/unmappable 均 REPORT；
2. 允许隐式标准 scalar tag 和显式 `!!str`，拒绝显式 bool/int/float/null/timestamp；
3. Canonical 节点名限制为 `[A-Za-z_][A-Za-z0-9._-]*`；
4. 所有失败保持 `FAILED / MIX_FRONTEND_YAML_UNSAFE / empty root`；
5. 保持 R20 资源、安全、Canonical 映射和模块边界；
6. 不修改 Context 生产代码、compiler canonical API或 XML 生产语义；
7. 不启动 T06。

## 历史保护

I001 的 Design R20、Plan R16、TDD R01、Skeleton R01、Development R01、Testing R01、Completion R01、Review `REV-000220`～`REV-000230`、Evidence `EVD-000464`～`EVD-000474` 均不得覆盖或删除。
