# FEATURE-DESC-3361AD2E54FC 阶段结果

```json stage-outcomes
[]
```

## 使用说明

字段集合以 `assets/long-task/record-contract.json#records.stageOutcome` 为准。

```json
{
  "id": "SO-MOD0001-DESIGN-R01",
  "phase": "design",
  "round": "DESIGN-R01",
  "iteration_id": "ITER-MOD0001-DESIGN-001",
  "iteration_no": 1,
  "is_current": true,
  "superseded_by": "",
  "status": "PASSED",
  "produced_by_agent": "DesignAgent",
  "input_revisions": {},
  "output_revision": "DESIGN-R01@hash",
  "evidence_ids": [],
  "open_risks": [],
  "started_at": "",
  "completed_at": ""
}
```

- `evidence_ids` 数组保存 Evidence Registry ID；`PASSED` 必须有 output revision、当前 revision 的 ACTIVE evidence 和完成时间。
- 每阶段只能有一个 `is_current=true` 的结果；旧结果必须保留并标记 `superseded_by`。
- `SKIPPED` 必须说明原因，不等于通过。
