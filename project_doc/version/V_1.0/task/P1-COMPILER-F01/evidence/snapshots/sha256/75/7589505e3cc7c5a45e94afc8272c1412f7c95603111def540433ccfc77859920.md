# P1-COMPILER-F01 治理修复交接

```json handoff
{
  "schema_version": 2,
  "target_id": "P1-COMPILER-F01",
  "version": "V_1.0",
  "task_id": "TASK-P1-R2-001",
  "phase": "requirement_confirmation",
  "round": "REQCONF-I002",
  "from_agent": "ProjectManagerAgent",
  "to_agent": "RequirementConfirmationAgent",
  "input_revisions": {
    "change_requirement": "P1-COMPILER-CR01",
    "requirement": "REQCONF-R02-DRAFT",
    "analysis": "REQAN-R03-DRAFT",
    "business_model": "BM-R02-DRAFT",
    "design": "DESIGN-R02-DRAFT"
  },
  "output_revision": "GOV-REPAIR-R01@2422fc8521da",
  "read_files": [
    "project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/evidence_index.json",
    "project_doc/version/V_1.0/task/P1-COMPILER-F01/task_attempts.md",
    "project_doc/version/V_1.0/task/P1-COMPILER-F01/stage_outcomes.md",
    "project_doc/version/V_1.0/doc/P1-COMPILER-CR01/requirement_change.md"
  ],
  "modified_files": [
    "project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/evidence_index.json",
    "project_doc/version/V_1.0/task/P1-COMPILER-F01/task_plan.md",
    "project_doc/version/V_1.0/task/P1-COMPILER-F01/task_state.md",
    "project_doc/version/V_1.0/task/P1-COMPILER-F01/review_issues.md",
    "project_doc/version/V_1.0/task/P1-COMPILER-F01/decision_log.md"
  ],
  "new_files": [
    "project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/snapshots/sha256/",
    "project_doc/version/V_1.0/task/P1-COMPILER-F01/handoff/2026-07-26-p1-governance-repair.md"
  ],
  "decision_ids": [
    "DEC-P1-COMPILER-005"
  ],
  "review_conclusion_refs": [],
  "discussion_issue_ids": [
    "ISSUE-P1-SCOPE-CHANGE-001"
  ],
  "traceability_updates": [
    "TR-P1-COMPILER-007"
  ],
  "validation": [
    "common-develop 2.35 check_skill: PASSED",
    "historical Evidence Git snapshot recovery: PASSED",
    "evidence.py migrate-v3: PASSED",
    "long_task.py validate: 0 errors"
  ],
  "open_issues": [],
  "blockers": [],
  "next_action": "RequirementConfirmationAgent 启动 TASK-P1-R2-001，对 REQCONF-R02-DRAFT 执行正式需求确认和串行 Review。",
  "stop_conditions": [
    "不得恢复 dec-expand-declaration",
    "不得建立 LegacyDeclarationAdapter",
    "不得在 Review 前推进 requirement_analysis"
  ],
  "created_at": "2026-07-26T02:49:04+00:00"
}
```

## 修复前

- `long_task.py validate`：`FAILED`
- errors：`78`
- warnings：`1`
- 根因：25 个 R01 Evidence 使用 DIRECT 引用指向后来被 R02 改写的文件；同一错误在历史 attempt 与 StageOutcome 回查时重复展开为 78 条。

## 修复动作

1. 从 Git 历史按已登记 SHA-256 恢复原始 R01 Artifact 字节。
2. 将 25 个旧 Evidence 改为 `SNAPSHOT`，保存在 `evidence/snapshots/sha256/`，Evidence ID、revision、phase 与 digest 均保持不变。
3. 执行 2.35 官方 `evidence.py migrate-v3`，完成 schema v3 一致性校验。
4. 修正历史业务建模与设计任务的 `allowed_files`，使历史 attempt 与真实修改范围一致。
5. 为当前 P1-COMPILER-CR01、REQCONF-R02-DRAFT、REQAN-R03-DRAFT、BM-R02-DRAFT、DESIGN-R02-DRAFT 注册新的不可变快照 Evidence。

## 修复结果

- Git 历史快照恢复：`PASSED`，repaired_count=`25`
- 官方 migrate-v3：`PASSED`，before=`62`，after=`62`
- 核心 long-task 校验：`PASSED`，errors=`0`，warnings=`1`
- 当前草案 Evidence：EVD-000201, EVD-000202, EVD-000203, EVD-000204, EVD-000205, EVD-000206, EVD-000207, EVD-000208, EVD-000209, EVD-000210, EVD-000211, EVD-000212, EVD-000213, EVD-000214

## 状态边界

- R01 StageOutcome、Review、attempt 和 Evidence 保留为历史，不删除、不刷新旧 digest。
- 范围漂移问题已由用户决策和新设计草案解决，可关闭该 issue。
- P1 不因此视为完成；仍停留在 `requirement_confirmation`，后续由 `RequirementConfirmationAgent` 对 R02 草案执行正式确认和串行 Review。
- 未推进到 requirement_analysis、test_design 或 development。

## 下一步

1. RequirementConfirmationAgent 启动 `TASK-P1-R2-001`。
2. 对 `REQCONF-R02-DRAFT` 完成确认，产出正式 revision。
3. RequirementAnalysisAgent、TestDesignAgent 串行独立 Review。
4. 当前阶段门禁通过后，使用 `advance-phase` 进入 requirement_analysis。
