# FEATURE-DESC-3361AD2E54FC 持久决策日志

```json decision-log
[]
```

## 当前记录结构

```json
{
  "id": "DEC-001",
  "status": "ACTIVE",
  "category": "BUSINESS_RULE",
  "question": "已取消订单是否允许再次支付？",
  "options_considered": ["允许恢复后支付", "直接拒绝支付"],
  "decision": "直接拒绝支付并返回 ORDER_NOT_PAYABLE",
  "rationale": "取消状态为当前业务终态",
  "decided_by": "user",
  "decided_at": "2026-07-19T10:00:00-05:00",
  "affects": ["requirement", "design", "test_design"],
  "supersedes": ""
}
```

字段集合以 `assets/long-task/record-contract.json#records.decisionLogItem` 为准。

只记录会影响后续 Agent 判断的持久决策。被替代的决策改为 `SUPERSEDED` 并填写新记录的关联关系，不删除历史证据。
