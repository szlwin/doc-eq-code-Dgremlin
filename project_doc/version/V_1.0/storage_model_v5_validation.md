# Storage Model v5 / common-develop 2.39 验证报告

## 1. 验证范围

本次在真实目录 `project_doc/version/V_1.0` 和任务 `P1-COMPILER-F01` 上执行 common-develop 2.39 迁移与回归，目标包括：

- 恢复人类可读的版本级 `work_record.md`，AI 通过隐藏结构化元数据读取；
- 修复同时间戳 handoff 选择、迁移中断恢复、历史阶段 Git checkpoint 补提、投影确定性和冷校验性能；
- 统一 Business Rule ID；
- 压缩 Evidence Registry、Review Registry 和 Snapshot；
- 重新验证业务流程与归档闭环。

迁移前已在仓库外创建回滚包：

- `/mnt/data/doc-eq-code-Dgremlin-v2.39-rollback.tar.gz`
- `/mnt/data/doc-eq-code-Dgremlin-v2.39-rollback.tar.gz.sha256`

`.git` 未删除、未重建，未执行 commit 或 push。

## 2. 工作记录格式

版本级工作记录已从机器优先的 `work_record.jsonl` 转为：

`project_doc/version/V_1.0/work_record.md`

结果：

- 17 条历史事件全部保留；
- 正文使用标题、表格、变更摘要和验证/阻塞摘要，适合人工阅读；
- 每条事件包含隐藏 `work-record-meta`，并通过 `render_digest` 校验可读正文；
- AI 使用 `long_task.py work-events --json` 读取，不需要解析 Markdown 表格；
- 旧 `work_record.jsonl` 已删除；
- `validate-work-record`：PASSED。

当前 Markdown 文件为 64,956 B。体积增加是恢复完整人类可读正文的预期结果，不属于重复权威事实：隐藏元数据是事件事实，表格正文是同文件内受摘要约束的展示层。

## 3. 存储收敛效果

| 指标 | 2.38 迁移后基线 | 2.39 | 减少量 | 降幅 |
|---|---:|---:|---:|---:|
| `evidence_index.json` | 136,009 B | 93,621 B | 42,388 B | 31.17% |
| `reviews.jsonl` | 73,240 B | 47,341 B | 25,899 B | 35.36% |
| Snapshot 物理文件 | 37 | 14 | 23 | 62.16% |
| Snapshot 物理大小 | 290,061 B | 110,192 B | 179,869 B | 62.01% |
| 任务权威文件数 | 69 | 45 | 24 | 34.78% |
| 任务权威数据大小 | 618,645 B | 360,807 B | 257,838 B | 41.68% |

Evidence 共 113 条，迁移后捕获模式：

- `GIT_REF`：78 条；
- `SNAPSHOT`：15 条逻辑记录，对应 14 个去重物理文件；
- `REVIEW_REF`：20 条。

压缩方式：

- Evidence 使用 `scope_sets`、`agent_sets` 字典化和缺省字段省略；
- 继续将可由 Git commit/blob/digest 重建的历史 Snapshot 转为 `GIT_REF`；
- Review schema v4 对重复结论文本、criterion reason 和 Evidence 集合进行字典化；
- 迁移只删除可由 Git 或 Registry 验证恢复的副本，未丢弃审计引用。

## 4. 六项优先修复验证

### 4.1 handoff 选择

`resume` 不再依赖 mtime。候选 handoff 按当前 artifact revision、iteration/round、阶段顺序、创建时间和文件名稳定排序。

真实任务在 R03/R04 时间戳相同的情况下选择：

`handoff/2026-07-26-reqconf-r04-passed.md`

结果：PASSED。

### 4.2 迁移中断恢复

Evidence 迁移使用 staging/backup 原子交换，并在重新执行时：

- 当前目录有效：完成交换并清除 backup；
- 当前目录无效：自动回滚 backup；
- 清除无效或过期 staging；
- 迁移与恢复均可幂等重试。

本次真实迁移在交换后被执行窗口中断，2.39 自动验证当前目录并完成交换，未留下 `.migrate-v4.tmp` 或 `.migrate-v4.backup`。

结果：PASSED。

### 4.3 历史阶段 Git checkpoint 补提

`git_checkpoint.py commit` 新增受控 `--phase`。它仍要求指定阶段的当前 artifact 与 StageOutcome 均为 PASSED、revision/iteration 一致、无运行中 Attempt，并通过完整长任务门禁。

真实任务对已经完成的 `requirement_confirmation` 门禁检查：

- StageOutcome：`SO-P1-COMPILER-F01-REQUIREMENT-CONFIRMATION-I004`
- Revision：`REQCONF-R04@c186ce681e1e`
- Gate：PASSED

本次只验证补提门禁，没有实际创建 Git commit。

### 4.4 投影确定性

`cacheHit` 等运行态字段已移出语义投影。对同一 `sourceInputDigest` 分别执行冷同步和热同步：

- `cache/projection/context.json`：逐字节一致；
- `cache/projection/manifest.json`：逐字节一致；
- 冷、热同步使用同一 `sourceInputDigest`。

结果：PASSED。

### 4.5 冷校验性能

2.38 的完整历史校验会在不同门禁中重复解析 Evidence/Review Registry，并重复回查 Git blob，真实任务曾达到分钟级。

2.39 的处理：

- `task_verify` 预验证结果传递给长任务校验，避免同一调用链重复验证；
- Review Registry 缓存键使用文件内容 SHA-256；
- Evidence Registry 缓存键包含索引内容以及所有可变本地依赖的内容 SHA-256；
- Git blob 缓存键包含 commit、path、blob OID、期望 digest 和 size；
- 等长篡改且恢复 mtime 的负向测试仍会被发现。

真实目录本次运行（并行回归占用 CPU 时测得，故为保守值）：

- `long_task validate`：6.22 s；
- `task_verify --no-cache`：6.47 s；
- 投影冷同步：7.09 s；
- 投影热同步：3.14 s；
- `storage_migrate validate`：4.76 s；
- `task_layout validate`：1.61 s；
- `ai_context resolve`：3.32 s。

此前同一实现在线程内复用预验证时约为 0.04–3.3 s。与 2.38 的 4–11 分钟级冷路径相比已显著收敛。

### 4.6 系统文件与输入指纹

权威输入指纹明确排除 `.DS_Store`、`Thumbs.db` 和 AppleDouble `._*`；真实 `project_doc` 中的对应文件已清理。负向测试确认这些文件不会改变 input digest。

结果：PASSED。

## 5. Business Rule ID

活动文档和 Flow 中旧命名空间：

`BR-P1-COMPILER-###`

已统一为 Requirement 使用的稳定 ID：

`BR-P1-###`

检查范围排除历史不可变 Snapshot 和可删除 cache 后，旧 ID 残留为 0。业务流程：

`project_doc/version/V_1.0/doc/_flows/COMPILER/changes/001-layout-migration.yaml`

执行 `business_flow.py validate --require-links`：PASSED。

历史 `storage_model_v4_validation.md` 已恢复其当时的旧/新 ID 描述，避免历史报告因批量替换失真。

## 6. 归档闭环复验

Business Rule ID 不一致已不再阻断归档。重新执行 `merge_docs validate` 和 `merge_docs preview` 后，当前前置阻断为：

`missing canonical YAML: project_doc/docs/COMPILER/COMPILER_business_model.yaml; run init first`

结论：

- ID 统一：PASSED；
- Business Flow 结构和链接：PASSED；
- 归档闭环：BLOCKED（项目缺少 project-level 结构化业务模型 YAML 基线）。

本次未自动执行 `merge_docs init`。该命令会创建空的结构化基线，而当前项目已有手写 `COMPILER_desc.md`；在没有明确的 Markdown→YAML 导入方案前直接初始化并继续归档，可能用空结构覆盖现有语义。该阻断属于项目归档准备项，不是 2.39 存储迁移失败。

后续应先设计一次性 `COMPILER_desc.md`/版本文档到 `COMPILER_business_model.yaml` 的受控导入和人工核对，再执行 preview、授权和 archive。

## 7. common-develop 2.39 回归

- 非归档单元/集成测试：312/312 PASSED；
- 归档合并重型回归：49/49 PASSED；
- 合计：361/361 PASSED；
- `check_skill.py`：12 项检查全部通过，0 warning，0 error；
- `semantic_audit.py`：PASSED。

归档负向测试包含 Evidence 内容被篡改后的 digest mismatch 门禁；新增测试还覆盖等长篡改并恢复 mtime，确认内容指纹缓存不会掩盖篡改。

## 8. 最终状态

- `work_record.md`：PASSED，17 条事件；
- Evidence：PASSED，113 条；
- Review：PASSED，26 条；
- `long_task validate`：PASSED，0 error，0 warning；
- `task_verify task-health`：PASSED；
- `storage_migrate validate`：PASSED；
- `task_layout validate`：PASSED；
- Git checkpoint 文档：PASSED；
- Business Flow：PASSED；
- 归档：BLOCKED，仅剩结构化业务模型 YAML 基线前置条件。

真实任务保持：

- Current phase：`requirement_analysis`；
- Task status：`READY`；
- Current Agent：`ProjectManagerAgent`；
- Open issue：0。

本次未执行 Git commit 或 push。
