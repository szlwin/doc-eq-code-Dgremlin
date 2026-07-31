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


## 自动验证结果

- DESIGN-R05 测试接缝恢复提交：`189cd60133394d306a05529448f8eada3790c545`；
- 测试接缝 SHA-256：`27ec9cb82d7d4ea2ab893d9200c9ed1db82628a3ca6372df39c72e213c67e8ac`；
- 工作记录恢复提交：`b9bc52ba7dc0e4eb2e9b1b179ea7a553a51a7234`；
- 工作记录 SHA-256：`f1e39f9bfcf0bc0eb359d6aa2bda9f2a7405670795ac652b94c886b0c3d18a49`；
- 追踪矩阵恢复来源：`current HEAD side selection`；
- 追踪矩阵 SHA-256：`536968529395f53f854d0fa03269a2a88d5d93ffddb6f73f0f859c3449faa35b`；
- Task Plan JSON：PASSED；
- Task State JSON：PASSED；
- Review Issue JSON：PASSED；
- Traceability 9/9：PASSED；
- DESIGN-R05 命令 Evidence：PASSED；
- `project_doc` 冲突标记扫描：PASSED。
