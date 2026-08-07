# FEATURE-DESC-3361AD2E54FC Review 问题台账

```json review-issues
[]
```

## 当前记录结构

```json
{
  "id": "ISSUE-001",
  "issue_type": "RISK",
  "axis": "ARCHITECTURE",
  "severity": "P1",
  "confidence": 9,
  "status": "OPEN",
  "phase": "design",
  "round": "DESIGN-R01",
  "artifact_revision": "DESIGN-R01@hash",
  "raised_by_agent": "ArchitectureReviewAgent",
  "owner_agent": "DesignAgent",
  "title": "事务边界不明确",
  "description": "写入顺序可能形成部分成功状态",
  "impact": "订单与支付状态可能不一致",
  "motivating_evidence": ["design.md#transaction-boundary"],
  "question": "原子性或补偿规则是什么？",
  "question_to": ["DesignAgent"],
  "responses": [],
  "recommendation": "定义本地事务或明确补偿流程",
  "affected_artifacts": ["design", "test_design"],
  "affected_trace_ids": [],
  "decision": "",
  "resolution_revision": "",
  "resolution_evidence": "",
  "verified_by_agent": "",
  "verified_at": "",
  "defer_reason": ""
}
```

字段集合以 `assets/long-task/record-contract.json#records.reviewIssue` 为准。

- 提出者新增问题；`owner_agent` 修复；原提出者或指定 validator 关闭。
- `owner_agent` 不得等于 `verified_by_agent`。
- 阻塞 finding 必须有精确 `motivating_evidence`。
- 无法引用证据的观察标记低置信度，不得作为高置信阻塞项。
- 状态只使用运行契约的 issue 状态域。
