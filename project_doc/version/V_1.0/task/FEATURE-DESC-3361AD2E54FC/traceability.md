# FEATURE-DESC-3361AD2E54FC 需求—模型—设计—计划—实现—测试追踪矩阵

```json traceability
[]
```

## 使用说明

字段集合以 `assets/long-task/record-contract.json#records.traceabilityItem` 为准。

每个业务规则、验收项或重要非功能约束建立稳定 ID：

```json
{
  "id": "TR-MOD0001-001",
  "description": "已取消订单不可支付",
  "status": "PENDING",
  "acceptance_criteria": ["AC-ORDER-007"],
  "requirement_refs": [],
  "impact_required": false,
  "dependency_impact_refs": [],
  "business_flow_required": false,
  "business_flow_refs": [],
  "cross_module_implementation_required": false,
  "cross_module_implementation_refs": [],
  "business_model_refs": [],
  "design_refs": [],
  "test_case_ids": [],
  "plan_task_ids": [],
  "contract_refs": [],
  "implementation_refs": [],
  "verification_evidence_ids": [],
  "verified_by_agents": [],
  "notes": ""
}
```

状态只使用 `PENDING`、`COVERED`、`GAP`、`STALE`、`NOT_APPLICABLE`。`verification_evidence_ids` 保存 evidence ID；每个适用 `acceptance_criteria` 必须在 `acceptance_assertions.json` 中有结构化 assertion。

## 关联影响规则

- 删除、取消、失效、迁移、替代、跨状态联动或需求/功能依赖存在时，`impact_required=true`，并填写 `dependency_impact_refs`。
- 涉及三步以上、状态流转、变体、回退/补偿或端到端场景时，`business_flow_required=true`，并填写稳定 `FLOW-*` 到 `business_flow_refs`。
- 涉及两个及以上业务模块的技术协作时，`cross_module_implementation_required=true`，并填写 `cross_module_implementation_refs`，引用 `dependency_impact.yaml` 中的跨模块实现映射。
- 无关联影响时保持 `false` 和空数组，不得为了过门禁生成虚假关系。
