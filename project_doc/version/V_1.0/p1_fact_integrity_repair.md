# P1 项目事实完整性修复

## 修复范围

- 恢复 `DESIGN-R05@0b37a9b4dd48` 对应的测试接缝文件，清除误留的 Git 冲突标记；
- 将 `task_plan.md` 对齐到 `TEST_DESIGN-I007` 与 `IMPLEMENTATION_PLAN-I007`；
- 保留有效的 DESIGN-R04 返修历史、DESIGN-I006 失败记录以及后续 REQAN-R05、BM-R05、DESIGN-R05 记录；
- 删除冲突另一侧已被替代的旧 DESIGN-R04 记录；
- 将项目摘要、需求状态、恢复上下文、阶段交接和整改状态同步到当前机器事实；
- 保持 `common-develop 2.44 RC8` 全局持久目录不变。

## 当前生命周期

```text
P1-COMPILER-F01
  REQCONF-R04   PASSED
  REQAN-R05     PASSED
  BM-R05        PASSED
  DESIGN-R05    PASSED
  TEST_DESIGN   I007 / TODO
```

下一任务由 `TestDesignAgent` 基于 `DESIGN-R05@0b37a9b4dd48` 形成可执行测试设计。test_design 与独立 Review 通过前，不进入 implementation_plan、TDD 或开发。

## 验证要求

最终提交必须满足：

- `project_doc` 无 Git 冲突标记；
- Task Plan、Task State 和 Review Issue 结构化区块可解析；
- 四项设计问题均已关闭；
- `DEC_COMPILER_test_seams.md` 的 SHA-256 与 `EVD-000284` 一致；
- 正常 `verify-and-open-pr.yml` 已恢复，临时修复 Workflow 不进入 PR 最终文件树。
