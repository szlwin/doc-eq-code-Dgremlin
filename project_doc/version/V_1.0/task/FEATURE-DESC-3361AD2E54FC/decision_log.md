# FEATURE-DESC-3361AD2E54FC 持久决策日志

```json decision-log
[
  {
    "id": "DEC-P2-REQCONF-AUTO-001",
    "status": "ACTIVE",
    "category": "SCOPE",
    "question": "PR #33 合并后，是否按已冻结的 P2 正式计划在 auto 模式下确认当前需求边界并进入独立 Review？",
    "options_considered": ["按已冻结 P2 计划确认并继续", "暂停并重新定义 P2 范围"],
    "decision": "按已冻结 P2 计划确认当前 requirement_confirmation revision；只有必需 Reviewer 与机器门禁均通过才允许推进。",
    "rationale": "P2 固定目标由正式 P0-P8 计划和 Request Intake 明确，用户要求继续执行。",
    "decided_by": "ProjectManagerAgent(auto; user-authorized continuation)",
    "decided_at": "2026-08-07T16:04:53+00:00",
    "affects": ["requirement_confirmation","requirement_analysis","business_model","design","test_design","implementation_plan","tdd","development","code_review","testing","completion_verification"],
    "supersedes": ""
  },
  {
    "id": "DEC-P2-DIRECT-BRIDGE-AUTHORITY-001",
    "status": "ACTIVE",
    "category": "BUSINESS_RULE",
    "question": "P2 protected-access runtime 是否继续使用 execution-token/claim 权威模型，还是允许 caller 直接提交 exact compiler-published ModelAccessRuleKey + AccessOperation？",
    "options_considered": ["execution-token / recognizes / atomic claim", "direct bridge.execute(ruleKey, operation, frameId, ownerResolutionId, cursorId)"],
    "decision": "采用 direct bridge invocation。当前 P2 caller 可以逐次选择 exact ModelAccessRuleKey 与 AccessOperation；AccessConsumerIrKey 仅作为 provenance/diagnostic，不作为 authorization key 维度。只有当前 compiler-published ModelAccessPolicyIndex 中 exact 存在且状态合法的规则才可能 ALLOW，其他情况 fail closed。",
    "rationale": "用户在 P2 Design Review 中明确要求撤销 token 方案并采用 direct bridge；该选择不取消 PolicyIndex、Gateway/Guard、runtime proof、actual-target/operation capability binding 或 fail-closed policy miss。",
    "decided_by": "user",
    "decided_at": "2026-08-09T09:41:00+08:00",
    "affects": ["requirement_analysis","business_model","design","test_design","implementation_plan","tdd","development"],
    "supersedes": "DESIGN-P2-R12 execution-token/recognizes/claim invocation authority model"
  },
  {
    "id": "DEC-P2-AC007-STAGE-BOUNDARY-001",
    "status": "ACTIVE",
    "category": "SCOPE",
    "question": "AC-P2-SYSTEM-RULEVIEW-007 最终由 P2 验收 seam/no-bypass 并下沉 concrete integrations，还是 P2 本身提供代表性 production consumers 执行原 literal AC？",
    "options_considered": [
      "A: P2 验收唯一 production protected-access seam、visibility/dependency 无合法旁路；真实 Rule/change/custom-action/query integration 下沉 P3/P4/P6",
      "B: P2 提供足以真实执行原 AC-007 的 representative production Rule/change/custom-action consumers"
    ],
    "decision": "采用 Option B。P2 必须交付并通过真实 production main-source 的 Rule、change、custom-action 三类 representative protected-access consumers；三类入口都必须实际执行授权/未授权场景，并证明权限结果不因 consumer kind 改变、所有允许/拒绝都经过同一个 ProtectedExecutionBridge -> Gateway -> Guard 路径、拒绝发生在副作用之前。P3/P4 的完整业务执行语义与 P6 QueryPlan 完整执行仍留在后续阶段。",
    "rationale": "用户于 2026-08-09 明确选择 AC-007 Option B。该选择保留原 AC-007 的 concrete-entry acceptance semantics，而不是以 seam-only 解释 supersede 原验收；同时把 P2 新增范围限制为代表性 production protected-access entry adapters，不提前实现 P3/P4 完整 Rule/Action/Produce 状态机或 P6 QueryPlan。",
    "decided_by": "user",
    "decided_at": "2026-08-09T11:55:00+08:00",
    "affects": ["requirement_analysis","business_model","business_flow","design","test_design","impact_analysis","cross_module_integration","P3","P4","P6"],
    "supersedes": ""
  }
]
```

## 当前记录结构

字段集合以 `assets/long-task/record-contract.json#records.decisionLogItem` 为准。

只记录会影响后续 Agent 判断的持久决策。被替代的决策改为 `SUPERSEDED` 并填写新记录关联关系，不删除历史证据。

## 当前 Gate

- `DEC-P2-DIRECT-BRIDGE-AUTHORITY-001`：真实用户授权，ACTIVE。
- `DEC-P2-AC007-STAGE-BOUNDARY-001`：真实用户选择 **Option B**，ACTIVE；原 AC-007 concrete-entry acceptance 保持有效。
- 当前决策已消除 AC-007 的用户选择阻断，但 Requirement exact Review、BM/Flow/Design/TestDesign exact Review、risk detection 与 machine lifecycle 仍未闭环。
- 本地 `$common-develop` lifecycle scripts 当前不可用，因此本文件只 materialize 持久决策事实，不伪称 machine reopen/publish。
