# DEC Compiler Implementation Plan R12 — TASK-P1-T03 I004

- Revision：`TP-P1-COMPILER-F01-R12@P1-T03-REWORK-I004`
- Design：`DESIGN-R16@P1-T03-REWORK-I004`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 阶段

1. **TDD RED**
   - 新增 `SourceReferenceRelativeCategoryReworkTest`；
   - 覆盖四种前导点段相对引用；
   - 断言 canonicalization 不改变 `URI.isAbsolute()`；
   - 断言 Resolver 返回 `MIX_SOURCE_PATH_ESCAPE`、空图、Provider 0 次访问；
   - 增加 Resolver 根入口受控边界 Oracle；
   - 既有 Context 26 项和 Compiler 74 项不得回归。

2. **Architecture Skeleton**
   - 在 `SourceReference` 中增加无状态类别保持辅助逻辑；
   - 保持唯一实例字段合同；
   - 将 Resolver 根入口动作整体纳入统一受控边界；
   - 新 Oracle 全绿且既有反射合同通过。

3. **Development**
   - 收敛相对类别保持逻辑，避免整 URI 解码；
   - 保持绝对 opaque/hierarchical、编码父目录、编码分隔符、query/fragment 的现有行为；
   - 补齐中文注释并审计 `@Override` 格式。

4. **独立 Review**
   - Specification、Architecture、Security、Code、TDD 五类 Review；
   - Reviewer 绑定同一 clean-code Head；
   - 开放 P0/P1 阻断 Testing/Completion。

5. **Testing / Completion**
   - Java 8、12 模块 Reactor、失败阻断、Surefire Artifact；
   - MySQL 仅按实际结果记录，不把 skipped 表述为 passed；
   - 更新 Completion R04、handoff、resume_context 和机器 checkpoint；
   - 最终文档化 Head 再运行独立 P0；
   - PR #18 恢复 Ready for review，但未经授权不合并；T04 保持阻断。

## 停止条件

- canonicalization 仍可把相对引用提升为绝对 URI；
- 任一相对根进入 Provider；
- Resolver 根入口 RuntimeException 泄漏；
- 既有 T03 图、安全或位置合同回归；
- Java 8、Reactor、失败阻断失败；
- 存在开放 P0/P1。
