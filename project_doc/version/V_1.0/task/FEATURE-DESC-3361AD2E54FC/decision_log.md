# FEATURE-DESC-3361AD2E54FC 持久决策日志

```json decision-log
[
  {
    "id": "DEC-P2-REQCONF-AUTO-001",
    "status": "ACTIVE",
    "category": "SCOPE",
    "question": "PR #33 合并后，是否按已冻结的 P2 正式计划在 auto 模式下确认当前需求边界并进入独立 Review？",
    "options_considered": [
      "按已冻结 P2 计划确认并继续",
      "暂停并重新定义 P2 范围"
    ],
    "decision": "按已冻结 P2 计划确认当前 requirement_confirmation revision；只有必需 Reviewer 与机器门禁均通过才允许推进。",
    "rationale": "P2 固定目标已由正式 P0-P8 计划和 Request Intake 明确，用户在 PR #33 合并后明确要求执行后续任务；当前没有需要新增业务选择的未决项。",
    "decided_by": "ProjectManagerAgent(auto; user-authorized continuation)",
    "decided_at": "2026-08-07T16:04:53+00:00",
    "affects": [
      "requirement_confirmation",
      "requirement_analysis",
      "business_model",
      "design",
      "test_design",
      "implementation_plan",
      "tdd",
      "development",
      "code_review",
      "testing",
      "completion_verification"
    ],
    "supersedes": ""
  },
  {
    "id": "DEC-P2-DIRECT-BRIDGE-AUTHORITY-001",
    "status": "ACTIVE",
    "category": "BUSINESS_RULE",
    "question": "P2 protected-access runtime 是否继续使用 execution-token/claim 权威模型，还是允许 caller 直接提交 exact compiler-published ModelAccessRuleKey + AccessOperation？",
    "options_considered": [
      "execution-token / recognizes / atomic claim",
      "direct bridge.execute(ruleKey, operation, frameId, ownerResolutionId, cursorId)"
    ],
    "decision": "采用 direct bridge invocation。当前 P2 caller 可以逐次选择 exact ModelAccessRuleKey 与 AccessOperation；AccessConsumerIrKey 仅作为 provenance/diagnostic，不作为 authorization key 维度。只有当前 compiler-published ModelAccessPolicyIndex 中 exact 存在且状态合法的规则才可能 ALLOW，其他情况 fail closed。",
    "rationale": "用户在 P2 Design Review 中明确要求撤销 token 方案并采用 direct bridge；该选择不取消 compiler-published PolicyIndex、统一 Gateway/Guard、runtime proof、actual-target/operation capability binding 或 fail-closed policy miss。",
    "decided_by": "user",
    "decided_at": "2026-08-09T09:41:00+08:00",
    "affects": [
      "requirement_analysis",
      "business_model",
      "design",
      "test_design",
      "implementation_plan",
      "tdd",
      "development"
    ],
    "supersedes": "DESIGN-P2-R12 execution-token/recognizes/claim invocation authority model"
  },
  {
    "id": "DEC-P2-AC007-STAGE-BOUNDARY-001",
    "status": "ACTIVE",
    "category": "SCOPE",
    "question": "AC-P2-SYSTEM-RULEVIEW-007 是否要求 P2 实现并执行 P3-P7 concrete Rule/change/custom-action/query executors，还是要求 P2 交付唯一 production protected-access seam 并把 concrete consumer integration 验收下沉到后续阶段？",
    "options_considered": [
      "P2 只验收唯一 production protected-access seam 与无合法旁路；concrete executor integration 下沉 P3/P4/P6",
      "P2 提前提供 Rule/change/custom-action concrete production consumers"
    ],
    "decision": "采用阶段边界方案：P2 的有效 AC-007 candidate 只要求唯一 production protected-access seam、公共可见性/依赖结构无合法旁路、所有 P2 可执行 protected access 均通过 Bridge -> Gateway -> Guard。真实 Rule/change/custom-action/query executor 集成测试成为对应 P3/P4/P6 downstream obligation，不在 P2 提前实现完整执行器。",
    "rationale": "REQAN-P2-R01 同时明确 P3 Information、P4 Action/Produce、P6 QueryPlan 与 P7 runtime 生命周期属于后续阶段。按 concrete executor 解释 AC-007 会造成 P2 无法在自身阶段完成验收；将 AC-007 收敛为 seam contract 保留 fail-closed 目标且不越权完成后续阶段。",
    "decided_by": "ProjectManagerAgent(review remediation; preserves frozen P2/P3-P7 stage boundary)",
    "decided_at": "2026-08-09T09:41:00+08:00",
    "affects": [
      "requirement_analysis",
      "business_model",
      "design",
      "test_design",
      "P3",
      "P4",
      "P6"
    ],
    "supersedes": "AC-P2-SYSTEM-RULEVIEW-007 literal concrete-executor acceptance interpretation"
  }
]
```

## 当前记录结构

字段集合以 `assets/long-task/record-contract.json#records.decisionLogItem` 为准。

只记录会影响后续 Agent 判断的持久决策。被替代的决策改为 `SUPERSEDED` 并填写新记录的关联关系，不删除历史证据。

> 注意：本文件的增量记录只 materialize 已明确/可由既有阶段边界消解的持久决策。当前本地 `$common-develop` 安装缺少声明的 lifecycle scripts，因此本次没有伪称执行 machine reopen/publish；这些 Decision 仍需在后续 exact current-revision Review/machine lifecycle 中被消费和验证。
