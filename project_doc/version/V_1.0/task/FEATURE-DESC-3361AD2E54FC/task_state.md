# FEATURE-DESC-3361AD2E54FC 长任务状态

```json task-state
{
  "schema_version": 2,
  "target_id": "FEATURE-DESC-3361AD2E54FC",
  "version": "V_1.0",
  "task_status": "READY",
  "current_phase": "requirement_confirmation",
  "current_round": "REQUIREMENT_CONFIRMATION-I001",
  "current_agent": "ProjectManagerAgent",
  "project_manager_agent": "ProjectManagerAgent",
  "execution_mode": "SEQUENTIAL",
  "active_task_ids": [],
  "current_attempt_id": "",
  "work_mode": {
    "ref": "version/V_1.0/work.md",
    "digest": "f4c8c42059d03dafb0516e27fe92b6c46e10b2292d6d41bf9cabab68a63d2ace",
    "model_code": false,
    "page_design": false,
    "minimal": false,
    "auto": true,
    "architecture_review": true,
    "git_checkpoint": true,
    "synced_at": "2026-08-07T15:15:47+00:00"
  },
  "request_intake": {
    "ref": "request_intake.json",
    "digest": "0833678801677205d95fd7c190d3c8493827ce3102c87449a775eb1ed868bab9",
    "classification": "NEW_REQUIREMENT",
    "workflow_profile": "STANDARD_FEATURE_FLOW",
    "entry_phase": "requirement_confirmation",
    "classification_revision": 1,
    "synced_at": "2026-08-07T15:15:47+00:00"
  },
  "architecture_review": {
    "enabled": true,
    "step": "SKELETON",
    "skeleton_iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-DEVELOPMENT-001",
    "skeleton_revision": "",
    "skeleton_review_result_refs": [],
    "implementation_iteration_id": "",
    "implementation_revision": "",
    "final_review_revision": "",
    "final_review_result_refs": [],
    "updated_at": "2026-08-07T15:15:47+00:00"
  },
  "artifact_revisions": {
    "requirement_confirmation": {
      "revision": "",
      "status": "PENDING",
      "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-REQUIREMENT-CONFIRMATION-001",
      "iteration_no": 1
    },
    "requirement_analysis": {
      "revision": "",
      "status": "PENDING",
      "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-REQUIREMENT-ANALYSIS-001",
      "iteration_no": 1
    },
    "business_model": {
      "revision": "",
      "status": "PENDING",
      "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-BUSINESS-MODEL-001",
      "iteration_no": 1
    },
    "design": {
      "revision": "",
      "status": "PENDING",
      "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-DESIGN-001",
      "iteration_no": 1
    },
    "test_design": {
      "revision": "",
      "status": "PENDING",
      "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-TEST-DESIGN-001",
      "iteration_no": 1
    },
    "implementation_plan": {
      "revision": "",
      "status": "PENDING",
      "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-IMPLEMENTATION-PLAN-001",
      "iteration_no": 1
    },
    "tdd": {
      "revision": "",
      "status": "PENDING",
      "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-TDD-001",
      "iteration_no": 1
    },
    "development": {
      "revision": "",
      "status": "PENDING",
      "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-DEVELOPMENT-001",
      "iteration_no": 1
    },
    "code_review": {
      "revision": "",
      "status": "PENDING",
      "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-CODE-REVIEW-001",
      "iteration_no": 1
    },
    "testing": {
      "revision": "",
      "status": "PENDING",
      "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-TESTING-001",
      "iteration_no": 1
    },
    "completion_verification": {
      "revision": "",
      "status": "PENDING",
      "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-COMPLETION-VERIFICATION-001",
      "iteration_no": 1
    }
  },
  "collaboration_reviews": {
    "requirement_confirmation": {
      "artifact_revision": "",
      "required_reviewers": [
        "RequirementAnalysisAgent",
        "TestDesignAgent"
      ],
      "additional_reviewers": {},
      "independent_conclusions": {},
      "status": "PENDING",
      "current_iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-REQUIREMENT-CONFIRMATION-001",
      "review_history": []
    },
    "requirement_analysis": {
      "artifact_revision": "",
      "required_reviewers": [
        "BusinessModelAgent",
        "DesignAgent",
        "TestDesignAgent"
      ],
      "conditional_reviewers": {
        "ImpactAnalysisReviewAgent": "impact_required",
        "CrossModuleIntegrationReviewAgent": "cross_module_implementation_required"
      },
      "additional_reviewers": {},
      "independent_conclusions": {},
      "status": "PENDING",
      "current_iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-REQUIREMENT-ANALYSIS-001",
      "review_history": []
    },
    "business_model": {
      "artifact_revision": "",
      "required_reviewers": [
        "BusinessModelReviewAgent",
        "DesignReviewAgent",
        "RequirementReviewAgent",
        "TestDesignAgent"
      ],
      "conditional_reviewers": {
        "ImpactAnalysisReviewAgent": "impact_required",
        "CrossModuleIntegrationReviewAgent": "cross_module_implementation_required"
      },
      "additional_reviewers": {},
      "independent_conclusions": {},
      "status": "PENDING",
      "current_iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-BUSINESS-MODEL-001",
      "review_history": []
    },
    "design": {
      "artifact_revision": "",
      "required_reviewers": [
        "ArchitectureReviewAgent",
        "BusinessModelReviewAgent",
        "DevelopAgent",
        "RequirementReviewAgent",
        "TestDesignAgent"
      ],
      "conditional_reviewers": {
        "ImpactAnalysisReviewAgent": "impact_required",
        "CrossModuleIntegrationReviewAgent": "cross_module_implementation_required"
      },
      "additional_reviewers": {},
      "independent_conclusions": {},
      "status": "PENDING",
      "current_iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-DESIGN-001",
      "review_history": []
    },
    "test_design": {
      "artifact_revision": "",
      "required_reviewers": [
        "DesignReviewAgent",
        "RequirementReviewAgent",
        "TDDReviewAgent",
        "TestEvidenceReviewAgent"
      ],
      "additional_reviewers": {},
      "independent_conclusions": {},
      "status": "PENDING",
      "current_iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-TEST-DESIGN-001",
      "review_history": []
    },
    "implementation_plan": {
      "artifact_revision": "",
      "required_reviewers": [
        "ArchitectureReviewAgent",
        "DevelopAgent",
        "PlanReviewAgent",
        "TestDesignAgent"
      ],
      "additional_reviewers": {},
      "independent_conclusions": {},
      "status": "PENDING",
      "current_iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-IMPLEMENTATION-PLAN-001",
      "review_history": []
    },
    "tdd": {
      "artifact_revision": "",
      "required_reviewers": [
        "TDDReviewAgent"
      ],
      "additional_reviewers": {},
      "independent_conclusions": {},
      "status": "PENDING",
      "current_iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-TDD-001",
      "review_history": []
    },
    "code_review": {
      "artifact_revision": "",
      "required_reviewers": [
        "ArchitectureReviewAgent",
        "EngineeringStandardsReviewAgent",
        "SpecComplianceReviewAgent"
      ],
      "additional_reviewers": {},
      "independent_conclusions": {},
      "status": "PENDING",
      "current_iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-CODE-REVIEW-001",
      "review_history": []
    },
    "testing": {
      "artifact_revision": "",
      "required_reviewers": [
        "TestEvidenceReviewAgent"
      ],
      "additional_reviewers": {},
      "independent_conclusions": {},
      "status": "PENDING",
      "current_iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-TESTING-001",
      "review_history": []
    },
    "development": {
      "artifact_revision": "",
      "required_reviewers": [
        "ArchitectureReviewAgent",
        "SpecComplianceReviewAgent"
      ],
      "additional_reviewers": {},
      "independent_conclusions": {},
      "status": "PENDING",
      "current_iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-DEVELOPMENT-001",
      "review_history": []
    }
  },
  "review_rounds": {},
  "open_issue_ids": [],
  "last_gate": "REQUEST_INTAKE_CLASSIFIED",
  "failed_attempts": {},
  "next_action": "按 STANDARD_FEATURE_FLOW 开始 requirement_confirmation",
  "next_agent": "RequirementConfirmationAgent",
  "resume_from": "执行 long_task.py task-context 获取当前任务、最新 attempt、开放问题和恢复引用；需要细节时再按引用读取",
  "max_auto_review_rounds": 3,
  "stale_events": [],
  "checkpoint_at": "2026-08-07T15:15:47+00:00"
}
```

## 使用规则

- `task_state.md` 只保存当前快照；`work_mode` 必须与版本 `work.md` 的模式和 SHA-256 一致，由 ProjectManagerAgent 单写；`current_agent` 记录最近一次修改该快照的 Agent，`project_manager_agent` 表示该长任务的项目管理责任 Agent。
- `additional_reviewers` 只用于运行契约 `riskReviewerCatalog` 中的风险 Reviewer，必须写 trigger、reason 和 `evidence_ids`。
- `PASSED` 只能在当前 revision 的全部必需、触发和额外 Reviewer 独立验证完成后写入。
- `independent_conclusions` 只保存 `review_result_ref`、结论和 finding 摘要；完整 criterion 结果压缩写入单一 `evidence/reviews.jsonl`，读取时按 `REV-*` 确定性恢复。

顶层字段集合以 `assets/long-task/record-contract.json#records.taskState` 为准。

- 阶段 iteration 由 `reopen-phase` 创建；禁止通过直接覆盖当前 revision 删除历史。
