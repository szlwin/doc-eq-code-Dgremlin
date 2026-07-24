# P1-COMPILER-F01 阶段结果

```json stage-outcomes
[
  {
    "id": "SO-P1-COMPILER-F01-REQUIREMENT-CONFIRMATION-I001",
    "phase": "requirement_confirmation",
    "round": "REQCONF-R01",
    "status": "PASSED",
    "produced_by_agent": "RequirementConfirmationAgent",
    "input_revisions": {},
    "output_revision": "REQCONF-R01@ac6d126dafb3",
    "evidence_ids": [
      "EVD-000001",
      "EVD-000002",
      "EVD-000003",
      "EVD-000007",
      "EVD-000010"
    ],
    "open_risks": [],
    "started_at": "2026-07-24T12:08:41+00:00",
    "completed_at": "2026-07-24T12:10:32+00:00",
    "iteration_id": "ITER-P1-COMPILER-F01-REQUIREMENT-CONFIRMATION-001",
    "iteration_no": 1,
    "is_current": true,
    "superseded_by": ""
  },
  {
    "id": "SO-P1-COMPILER-F01-REQUIREMENT-ANALYSIS-I001",
    "phase": "requirement_analysis",
    "round": "REQAN-R02",
    "status": "PASSED",
    "produced_by_agent": "RequirementAnalysisAgent",
    "input_revisions": {
      "requirement_confirmation": "REQCONF-R01@ac6d126dafb3"
    },
    "output_revision": "REQAN-R02@d38b7f83f222",
    "evidence_ids": [
      "EVD-000011",
      "EVD-000012",
      "EVD-000013",
      "EVD-000014",
      "EVD-000015",
      "EVD-000016",
      "EVD-000017",
      "EVD-000018",
      "EVD-000019",
      "EVD-000020",
      "EVD-000032",
      "EVD-000043",
      "EVD-000051",
      "EVD-000057",
      "EVD-000072"
    ],
    "open_risks": [],
    "started_at": "2026-07-24T12:23:35+00:00",
    "completed_at": "2026-07-24T12:31:30+00:00",
    "iteration_id": "ITER-P1-COMPILER-F01-REQUIREMENT-ANALYSIS-001",
    "iteration_no": 1,
    "is_current": true,
    "superseded_by": ""
  },
  {
    "id": "SO-P1-COMPILER-F01-BUSINESS-MODEL-I001",
    "phase": "business_model",
    "round": "BM-R01",
    "status": "PASSED",
    "produced_by_agent": "BusinessModelAgent",
    "input_revisions": {
      "requirement_analysis": "REQAN-R02@d38b7f83f222"
    },
    "output_revision": "BM-R01@52a58f20cb32",
    "evidence_ids": [
      "EVD-000074",
      "EVD-000075",
      "EVD-000076",
      "EVD-000077",
      "EVD-000078",
      "EVD-000079",
      "EVD-000080",
      "EVD-000081",
      "EVD-000089",
      "EVD-000098",
      "EVD-000108",
      "EVD-000113",
      "EVD-000120",
      "EVD-000125"
    ],
    "open_risks": [],
    "started_at": "2026-07-24T12:34:00+00:00",
    "completed_at": "2026-07-24T12:39:00+00:00",
    "iteration_id": "ITER-P1-COMPILER-F01-BUSINESS-MODEL-001",
    "iteration_no": 1,
    "is_current": true,
    "superseded_by": ""
  },
  {
    "id": "SO-P1-COMPILER-F01-DESIGN-I001",
    "phase": "design",
    "round": "DESIGN-R01",
    "status": "PASSED",
    "produced_by_agent": "DesignAgent",
    "input_revisions": {
      "requirement_analysis": "REQAN-R02@d38b7f83f222",
      "business_model": "BM-R01@52a58f20cb32"
    },
    "output_revision": "DESIGN-R01@a7a6820a381e",
    "evidence_ids": [
      "EVD-000126",
      "EVD-000127",
      "EVD-000128",
      "EVD-000129",
      "EVD-000130",
      "EVD-000131",
      "EVD-000132",
      "EVD-000133",
      "EVD-000142",
      "EVD-000150",
      "EVD-000160",
      "EVD-000166",
      "EVD-000175",
      "EVD-000182",
      "EVD-000191"
    ],
    "open_risks": [],
    "started_at": "2026-07-24T12:41:35+00:00",
    "completed_at": "2026-07-24T12:49:21+00:00",
    "iteration_id": "ITER-P1-COMPILER-F01-DESIGN-001",
    "iteration_no": 1,
    "is_current": true,
    "superseded_by": ""
  }
]
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
