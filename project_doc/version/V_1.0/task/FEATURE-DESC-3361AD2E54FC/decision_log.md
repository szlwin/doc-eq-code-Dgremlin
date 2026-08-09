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
    "decision": "采用 direct bridge invocation。caller 可逐次提交 exact ModelAccessRuleKey 与 current P2 AccessOperation；当前 AccessOperation 范围由 DEC-P2-ACCESS-OPERATIONS-001 冻结为 READ/WRITE。AccessConsumerIrKey 仅作为 provenance/diagnostic，不是 authorization-key 维度。",
    "rationale": "用户明确撤销 token 方案并采用 direct bridge；PolicyIndex、Gateway/Guard、runtime proof、actual-target/operation capability binding 与 fail-closed policy miss 继续有效。",
    "decided_by": "user",
    "decided_at": "2026-08-09T09:41:00+08:00",
    "affects": ["requirement_analysis","business_model","design","test_design","implementation_plan","tdd","development"],
    "supersedes": "DESIGN-P2-R12 execution-token/recognizes/claim invocation authority model"
  },
  {
    "id": "DEC-P2-AC007-STAGE-BOUNDARY-001",
    "status": "ACTIVE",
    "category": "SCOPE",
    "question": "AC-P2-SYSTEM-RULEVIEW-007 最终由 P2 验收 seam/no-bypass并下沉 concrete integrations，还是 P2 本身提供代表性 production consumers 执行原 literal AC？",
    "options_considered": ["A: P2 seam/no-bypass；真实 integrations 下沉 P3/P4/P6", "B: P2 提供真实 production Rule/change/custom-action representative consumers"],
    "decision": "采用 Option B。P2 必须交付并通过真实 production main-source Rule、change、custom-action 三类 representative protected-access consumers；三类入口都真实执行授权/未授权场景并经过同一个 production composition -> ProtectedExecutionBridge -> Gateway -> Guard 权限链。P3/P4/P6 完整业务语义仍留后续阶段。",
    "rationale": "用户明确选择 Option B；保留原 AC-007 concrete-entry acceptance，不用 seam-only 替代。",
    "decided_by": "user",
    "decided_at": "2026-08-09T11:55:00+08:00",
    "affects": ["requirement_analysis","business_model","business_flow","design","test_design","impact_analysis","cross_module_integration","P3","P4","P6"],
    "supersedes": "Only the AC-007 future-only/contract-only consequence in p2-direct-bridge-authority-decision-r01.md; DEC-P2-DIRECT-BRIDGE-AUTHORITY-001 authority decision remains ACTIVE"
  },
  {
    "id": "DEC-P2-ACCESS-OPERATIONS-001",
    "status": "ACTIVE",
    "category": "BUSINESS_RULE",
    "question": "Current P2 model-access operation 集合是否包含 EXECUTE？",
    "options_considered": ["READ/WRITE/EXECUTE", "READ/WRITE only"],
    "decision": "Current P2 model-access 只有 READ 与 WRITE，没有 EXECUTE。AccessOperation current contract exactly = READ|WRITE；不新增 EXECUTE source syntax/raw IR/policy/runtime/test contract。",
    "rationale": "用户明确说明当前只有 READ、WRITE，没有 EXECUTE；真实 P1 AccessMode 也只有 READ/WRITE。",
    "decided_by": "user",
    "decided_at": "2026-08-09T12:36:00+08:00",
    "affects": ["requirement_analysis","business_model","business_flow","design","test_design","impact_analysis","implementation_plan","tdd","development"],
    "supersedes": "Current-candidate EXECUTE portions of historical REQAN-P2-R01 acceptance semantics; historical R01 text is preserved"
  }
]
```

## 当前记录结构

字段集合以 `assets/long-task/record-contract.json#records.decisionLogItem` 为准。只记录会影响后续 Agent 判断的持久决策；历史事实不删除。

## 当前 Gate

- Direct Bridge authority：ACTIVE / user-decided。
- AC-007：ACTIVE Option B / user-decided；旧 future-only consequence 已局部 supersede。
- Access operations：ACTIVE READ/WRITE-only / user-decided。
- Requirement exact Review、BM/Flow/Design/TestDesign exact Review、risk detection 与 machine lifecycle 仍未闭环。
- 本地 `$common-develop` lifecycle scripts 当前不可用，因此不伪称 machine reopen/publish/risk scan。
