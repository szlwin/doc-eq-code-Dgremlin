# TP-P1-COMPILER-F01-R40 — TASK-P1-T12 I007 canonical collision 返工计划

- Revision：`TP-P1-COMPILER-F01-R40@P1-T12-REWORK-I007`
- Design：`DESIGN-R44@P1-T12-REWORK-I007`
- Status：`PASSED`
- Base：`PR27@a59a39fde202366742963658bf07797c9537de57`
- Invalidated Completion：`COMPLETION-P1-T12-R06@ce8c92523256`

## Sequential workflow

1. 验证 `common-develop-v2.44-rc8` 指向 `4787876e...`，保留 Skill 既有 worktree drift，不 reset/clean。
2. 将 PR #27 转为 Draft；R11/R06 标记为 `INVALIDATED / PRESERVED`，T13 保持阻断。
3. 冻结 R44/R40，并记录 first commit/blob；两者必须早于 I007 有效 RED。
4. 新建 `TASK-P1-T12-I007`、rework input 与 R12 invalidation，Open P0/P1/P2=`0/0/1`。
5. 新增 I007 RED Oracle：Map 双侧 collision、Map 单侧 collision、Set collision、正常 LinkedHashMap、普通 hash collision 精确区分。
6. 运行 P0，确认新增测试可编译，且旧生产代码至少出现预期 collision 失败；I001～I006 保持绿色。
7. Architecture Gate 冻结异常类型、Map/Set 完成阶段门禁、异常传播和无调用 private task 清理。
8. 修改仅限 `dec.core.compiler.pass`：
   - 新增 package-private `CanonicalCollisionException`；
   - Set/Map 在 canonical node intern 前检查重复 canonical ID；
   - 删除 `ConditionalCompareTask`。
9. 运行 First GREEN、定向 I007、T12、Compiler 与全 Reactor。
10. 独立 Review 增加 Frozen receiver、Map.Entry、嵌套 collision、预算边界和 no-partial-cache Oracle。
11. 执行 clean-code P0，下载 Artifact 并独立解析 SHA/Surefire XML。
12. 登记 Development、Testing、Code Review、Revision Lock、Completion 与 machine checkpoint。
13. clean-code Head 后只允许 `project_doc` 变化；final documented Head 再执行 P0/Artifact。
14. 更新 PR #27 标题/正文并恢复 Ready for Review；不合并 PR，不启动 T13。

## Stop conditions

- R44/R40 晚于 RED；
- collision 仍可形成合法 MAP/SET canonical node；
- 两个非法 collision 容器仍返回 true；
- 普通 hash collision 被误判为 canonical collision；
- collision 后写入 partial canonical cache；
- 新异常扩展 Compiler 公共 API；
- `@Override` 未独占一行或关键逻辑缺少中文注释；
- I001～I006、Java 8、12 modules、intentional failure gate 回归；
- Open P0/P1/P2 未清零；
- 用户未明确授权时合并 PR #27 或启动 T13。
