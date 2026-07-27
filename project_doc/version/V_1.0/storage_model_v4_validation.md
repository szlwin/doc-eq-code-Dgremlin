# common-develop 2.38 存储模型 v4 真实项目验证报告

## 1. 验证结论

- 验证对象：`project_doc/version/V_1.0/task/P1-COMPILER-F01`
- Skill 基线：`common-develop 2.38`
- 仓库分支：`dev_all`
- 仓库基线提交：`755d07fba9db2850b7b5de2aa2c12141fc666964`
- 总体结论：**PARTIAL**
- 存储模型迁移：**PASSED**
- 迁移后任务完整性：**PASSED**
- AI 上下文与热缓存性能：**PASSED**
- 阶段重开：**PASSED（隔离副本验证）**
- 恢复、延迟 Git checkpoint、归档：**发现阻断缺陷，未达到完整闭环**

本次已直接将真实 `project_doc` 中现有任务从旧存储模型迁移到存储模型 v4。迁移后的权威任务状态保持为：

- 当前阶段：`requirement_analysis`
- 当前任务状态：`READY`
- 当前 Agent：`ProjectManagerAgent`
- 开放问题：0
- `task_verify`：`PASSED`

未执行 Git commit 或 push。

## 2. 迁移内容

### 2.1 工作记录

- 删除旧的 `work_record.md`。
- 新增 `work_record.jsonl`。
- 共 17 条紧凑事件，文件大小 27,236 bytes。
- 原 Markdown 大小 67,160 bytes，减少 39,924 bytes，约 **59.45%**。
- 事件状态：14 条 `PASSED`、2 条 `PARTIAL`、1 条 `BLOCKED`。

### 2.2 Review 存储

- 删除旧 `evidence/reviews/index.jsonl` 及 26 个 Review Object 文件。
- 新增单一 `evidence/reviews.jsonl`。
- 共 26 条 schema v3 Review 记录，大小 73,240 bytes。
- Review 物理文件由 27 个索引/对象文件收敛为 1 个事件流文件。
- 隔离环境执行阶段重开后，`reviews.jsonl` 字节级保持不变，证明历史 Review 未被覆盖或重写。

### 2.3 Evidence 存储

迁移后 `evidence_index.json`：

- schema：4
- 总记录：113
- `ACTIVE`：104
- `SUPERSEDED`：9
- `SNAPSHOT`：55 条逻辑记录
- `GIT_REF`：38 条
- `REVIEW_REF`：20 条
- 实际 Snapshot 文件：37 个

38 条证据已改为 Git commit、path、blob OID 和 digest 引用，不再保存重复内容副本。

GC 验证结果：

- 引用 Snapshot：37
- 实际 Snapshot：37
- 孤儿 Snapshot：0
- Review Object：0
- 可回收孤儿数据：0 bytes

### 2.4 派生投影

已从权威存储中删除：

- 根 `manifest.json`
- `current/task_state.json`
- `current/phase_state.json`
- `current/open_issues.json`
- `current/handoff.json`
- `views/**`

投影改为可删除缓存：

- `cache/projection/manifest.json`
- `cache/projection/context.json`
- `cache/verification/task-health/{inputDigest}.json`

`cache/.gitignore` 仅跟踪自身，投影和验证结果不会作为权威事实提交。

实际删除投影文件后重新同步：

- 重建耗时：2.01 秒（热缓存）
- 投影可完整重建
- 连续热缓存重建结果字节级一致

## 3. 体积与重复率效果

### 3.1 任务目录

> 以下体积统计以迁移完成时的权威数据为准，不包含本验证报告及可删除缓存。

| 指标 | 迁移前实际备份 | 迁移后权威数据 | 变化 |
|---|---:|---:|---:|
| 文件数 | 154 | 69 | -85（**-55.19%**） |
| 文件内容大小 | 810,420 bytes | 618,645 bytes | -191,775 bytes（**-23.66%**） |
| Snapshot 文件数 | 68 | 37 | -31（**-45.59%**） |
| Snapshot 大小 | 435,653 bytes | 290,061 bytes | -145,592 bytes（**-33.42%**） |
| Review Object/Index 文件数 | 27 | 0 | -27（事件流另为 1 个文件） |
| 旧投影文件数 | 26 | 0 | -26 |
| 重复数据组 | 9 | 2 | -7 |
| 重复额外字节 | 27,159 bytes | 1,264 bytes | -25,895 bytes（**-95.35%**） |

迁移后的可删除缓存约 26.9 KB，不计入权威数据；含缓存时任务文件内容约 645.5 KB。

### 3.2 整个 V_1.0 版本目录

| 指标 | 迁移前 | 迁移后权威数据 | 变化 |
|---|---:|---:|---:|
| 文件数 | 186 | 101 | -85（**-45.70%**） |
| 文件内容大小 | 1,002,887 bytes | 771,188 bytes | -231,699 bytes（**-23.10%**） |
| 派生缓存 | 0 | 约 26,864 bytes | 可删除、可重建 |

### 3.3 仍占用空间的主要内容

迁移后的任务权威数据约 618.6 KB，尚未低于 500 KB。主要占用为：

- `evidence/evidence_index.json`：136,009 bytes
- `evidence/reviews.jsonl`：73,240 bytes
- 37 个 Snapshot：290,061 bytes

三项合计 499,310 bytes，占任务权威数据约 **80.71%**。因此本次已显著减少文件数和重复内容，但体积目标只完成了第一阶段收敛。

## 4. 完整性与性能验证

### 4.1 迁移后任务健康

`task_verify` 最终结果：

- overall：`PASSED`
- cacheHit：`true`
- Evidence：113
- Reviews：26
- Acceptance Assertions：9
- Traceability：9
- Open Issues：0
- Critical Issues：0

以下检查全部通过：

- Document Layout
- Task State
- Evidence Integrity
- Review Registry
- Acceptance
- Risk Report
- Traceability
- Open Issues
- Long Task
- Phase Gate

### 4.2 AI 上下文

`ai_context resolve`：

- 请求预算：12,000 tokens
- 估算使用：3,969 tokens
- 省略项：0
- 输出大小：19,815 bytes
- 耗时：2.53 秒
- Task verification：热缓存命中且 `PASSED`

当前阶段 Evidence 查询：

- 返回 19 条
- 输出大小：13,965 bytes
- 耗时：4.14 秒

说明 AI 不再需要读取全部历史目录和每个 Review Object，紧凑上下文路径有效。

### 4.3 热缓存性能

| 操作 | 耗时 |
|---|---:|
| 投影删除后重建 | 2.01 秒 |
| `task_verify` | 1.83 秒 |
| `ai_context resolve` | 2.53 秒 |
| 存储模型 validate | 2.55 秒 |
| 当前阶段 Evidence 查询 | 4.14 秒 |

热缓存路径满足 AI 快速启动需求。

### 4.4 冷路径性能

真实历史数据下，首次完整校验、投影同步、恢复和隔离阶段重开后的全量验证均需要约 4–5 分钟：

- `resume`：245.59 秒
- 阶段重开后完整 validate：261.51 秒
- 延迟 checkpoint 尝试：302.55 秒后被门禁阻断

冷路径虽然能够完成，但性能明显不适合高频 Agent 调度，需要在下一版优化增量校验和缓存复用。

## 5. 生命周期验证结果

### 5.1 阶段重开：PASSED

在隔离副本中执行 `requirement_analysis` 阶段重开：

- 新 iteration：`ITER-P1-COMPILER-F01-REQUIREMENT-ANALYSIS-005`
- 当前状态：`REWORK`
- 当前轮次：`REQUIREMENT_ANALYSIS-I005`
- `requirement_analysis` artifact：`IN_PROGRESS`
- 后续阶段全部失效
- Review 状态回到 `PENDING`
- 历史 Review 事件流未被改写
- 完整 validate：`PASSED`
- 开放问题：0

### 5.2 恢复：发现缺陷

真实目录执行 `resume` 时，生成的恢复上下文选择了：

`handoff/2026-07-26-reqconf-r03-passed.md`

但权威状态和根 `handoff.md` 已明确当前 Revision 为 R04。R03 与 R04 文件的 mtime 完全相同，当前实现仅按 mtime 取最大值，导致选择不确定。

为避免错误上下文留在真实项目中，已删除该派生 `resume_context.md`；错误结果副本保存在仓库外用于修复分析。

建议 2.39：按权威 iteration/revision 元数据选择 handoff；至少使用 `(mtime, 解析后的 revision, filename)` 确定性排序，并考虑根 `handoff.md`。

### 5.3 Git checkpoint：门禁有效，但延迟提交不可恢复

真实目录 `git_checkpoint validate`：`PASSED`，当前没有 checkpoint。

在隔离副本启用 `-gc` 后尝试提交 checkpoint，门禁正确阻止提交：

`current phase requirement_analysis has no current StageOutcome`

原因是最近通过的 StageOutcome 属于 `requirement_confirmation`，但 CLI 固定使用当前阶段，无法指定尚未提交 checkpoint 的前一通过阶段。

结论：

- 无效 checkpoint 不会被提交，门禁有效。
- 如果阶段已推进但 checkpoint 尚未提交，现有 CLI 无法补做。

建议 2.39：增加受控 `--phase`，仅允许选择当前或最近尚未 checkpoint 的已通过 StageOutcome；或者在 `advance-phase` 前强制完成 checkpoint。

### 5.4 归档：门禁有效，项目文档尚未满足契约

真实项目归档状态：无已应用 changeset。

真实目录直接 validate 被阻断，因为缺少：

`project_doc/docs/COMPILER/COMPILER_business_model.yaml`

在隔离副本执行 init 后，validate 继续被以下错误阻断：

`FLOW-CONFIG-COMPILE references rule not found in requirement document: BR-P1-COMPILER-004`

当前 Requirement 使用类似 `BR-P1-004` 的 ID，而 Flow 使用 `BR-P1-COMPILER-004`，存在稳定 ID 命名空间不一致。

结论：归档门禁能够发现真实跨文档不一致；当前 `project_doc` 尚不能归档，需要先统一 Business Rule ID。

## 6. 新发现的问题

除生命周期问题外，本次真实数据验证还发现：

1. **迁移中断恢复不足**：迁移完成 swap 后若在耗时的完整校验阶段被中断，会留下 `.migrate-v4.backup`，需要人工判断并清理；命令不能自动 resume。
2. **恢复 handoff 选择不确定**：mtime 相同会选到旧 Revision。
3. **延迟 Git checkpoint CLI 缺口**：阶段推进后无法为上一已通过 StageOutcome 补 checkpoint。
4. **归档稳定 ID 不一致**：Flow 与 Requirement 的 Business Rule ID 命名空间不同。
5. **投影确定性不完整**：同一 `sourceInputDigest` 下，冷缓存与热缓存输出可能因 `taskState.verification.cacheHit` 不同而产生内容差异；缓存命中状态不应成为语义投影的一部分。
6. **隐藏系统文件影响输入指纹**：`project_doc` 下存在被 Git 忽略的 `.DS_Store`；当前权威输入扫描可能将其计入 digest。应使用权威文件白名单或遵循 VCS ignore。
7. **冷校验过慢**：完整历史回查为分钟级，需增量化。
8. **体积仍可继续收敛**：Evidence Index、Review 流和 Snapshot 占权威数据约 80.71%。

## 7. 后续建议

下一项任务建议进入 **common-develop 2.39**，优先级如下：

1. 修复 handoff 确定性选择和错误恢复上下文问题。
2. 完善迁移中断后的自动恢复、回滚和幂等重试。
3. 为 Git checkpoint 增加受控补提机制，或将 checkpoint 变为阶段推进的前置门禁。
4. 将 `cacheHit` 等运行时信息移出语义投影，保证相同 digest 产生完全一致输出。
5. 权威输入指纹排除 `.DS_Store`、缓存和其他 VCS 忽略文件。
6. 优化冷路径为增量校验，避免每次重复回查全部历史 Evidence/Review。
7. 在项目文档中统一 `BR-P1-004` 与 `BR-P1-COMPILER-004` 命名空间后重新验证归档。
8. 继续收敛 Evidence：保留紧凑活跃索引，将历史记录分段；对已在 Git 中稳定存在的 Snapshot 继续转换为 Git 引用。

修复 2.39 并统一项目稳定 ID 后，应在同一真实目录重新执行：迁移幂等性、resume、checkpoint、archive 和删除缓存后的恢复测试。

## 8. 工作区状态

本次迁移结果保留在真实工作目录中，尚未提交：

`/mnt/data/doc-eq-code-Dgremlin`

主要 Git 变化为：

- 删除旧 Review Object、冗余 Snapshot、`current/**`、`views/**`、根 manifest 和旧 `work_record.md`；
- 修改 `evidence/evidence_index.json`；
- 新增 `evidence/reviews.jsonl`、`work_record.jsonl` 和 `cache/.gitignore`；
- 新增本验证报告。

在确认 2.39 修复范围之前，不建议提交当前变化。
