# FEATURE-DESC-3361AD2E54FC 阶段结果

```json stage-outcomes
[
  {
    "id": "SO-FEATURE-DESC-3361AD2E54FC-REQUIREMENT_CONFIRMATION-I001",
    "phase": "requirement_confirmation",
    "round": "REQUIREMENT_CONFIRMATION-I001",
    "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-REQUIREMENT-CONFIRMATION-001",
    "iteration_no": 1,
    "is_current": false,
    "superseded_by": "ITER-FEATURE-DESC-3361AD2E54FC-REQUIREMENT-CONFIRMATION-002",
    "status": "PASSED",
    "produced_by_agent": "RequirementConfirmationAgent",
    "input_revisions": {},
    "output_revision": "REQCONF-P2-R01@001604ced8af",
    "evidence_ids": [
      "EVD-000001",
      "EVD-000002",
      "EVD-000003",
      "EVD-000004"
    ],
    "open_risks": [],
    "started_at": "2026-08-07T16:05:39+00:00",
    "completed_at": "2026-08-07T16:08:56+00:00"
  },
  {
    "id": "SO-FEATURE-DESC-3361AD2E54FC-REQUIREMENT_CONFIRMATION-I002",
    "phase": "requirement_confirmation",
    "round": "REQUIREMENT_CONFIRMATION-I002",
    "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-REQUIREMENT-CONFIRMATION-002",
    "iteration_no": 2,
    "is_current": true,
    "superseded_by": "",
    "status": "PASSED",
    "produced_by_agent": "RequirementConfirmationAgent",
    "input_revisions": {},
    "output_revision": "REQCONF-P2-R02@ef30059b327d",
    "evidence_ids": [
      "EVD-000009",
      "EVD-000010",
      "EVD-000011",
      "EVD-000012"
    ],
    "open_risks": [],
    "started_at": "2026-08-07T16:12:37+00:00",
    "completed_at": "2026-08-07T16:13:22+00:00"
  },
  {
    "id": "SO-FEATURE-DESC-3361AD2E54FC-REQUIREMENT_ANALYSIS-I002",
    "phase": "requirement_analysis",
    "round": "REQUIREMENT_ANALYSIS-I002",
    "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-REQUIREMENT-ANALYSIS-002",
    "iteration_no": 2,
    "is_current": true,
    "superseded_by": "",
    "status": "PASSED",
    "produced_by_agent": "RequirementAnalysisAgent",
    "input_revisions": {
      "requirement_confirmation": "REQCONF-P2-R02@ef30059b327d"
    },
    "output_revision": "REQAN-P2-R01@d08612768131",
    "evidence_ids": [
      "EVD-000015",
      "EVD-000016",
      "EVD-000021",
      "EVD-000022",
      "EVD-000023"
    ],
    "open_risks": [],
    "started_at": "2026-08-07T16:26:34+00:00",
    "completed_at": "2026-08-07T16:33:21+00:00"
  },
  {
    "id": "SO-FEATURE-DESC-3361AD2E54FC-BUSINESS_MODEL-I002",
    "phase": "business_model",
    "round": "BUSINESS_MODEL-I002",
    "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-BUSINESS-MODEL-002",
    "iteration_no": 2,
    "is_current": false,
    "superseded_by": "ITER-FEATURE-DESC-3361AD2E54FC-BUSINESS-MODEL-003",
    "status": "PASSED",
    "produced_by_agent": "BusinessModelAgent",
    "input_revisions": {
      "requirement_analysis": "REQAN-P2-R01@d08612768131"
    },
    "output_revision": "BM-R06@6a0bce4fa0ae",
    "evidence_ids": [
      "EVD-000028",
      "EVD-000029",
      "EVD-000030",
      "EVD-000031",
      "EVD-000032",
      "EVD-000033",
      "EVD-000034",
      "EVD-000035",
      "EVD-000041",
      "EVD-000038",
      "EVD-000042"
    ],
    "open_risks": [],
    "started_at": "2026-08-07T16:48:19+00:00",
    "completed_at": "2026-08-07T16:51:08+00:00"
  },
  {
    "id": "SO-FEATURE-DESC-3361AD2E54FC-BUSINESS_MODEL-I003",
    "phase": "business_model",
    "round": "BUSINESS_MODEL-I003",
    "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-BUSINESS-MODEL-003",
    "iteration_no": 3,
    "is_current": true,
    "superseded_by": "",
    "status": "PASSED",
    "produced_by_agent": "BusinessModelAgent",
    "input_revisions": {
      "requirement_analysis": "REQAN-P2-R01@d08612768131"
    },
    "output_revision": "BM-R07@7d7bf504ca9d",
    "evidence_ids": [
      "EVD-000049",
      "EVD-000050",
      "EVD-000051",
      "EVD-000052",
      "EVD-000053",
      "EVD-000054",
      "EVD-000055",
      "EVD-000056",
      "EVD-000057",
      "EVD-000058",
      "EVD-000043",
      "EVD-000044",
      "EVD-000045",
      "EVD-000046",
      "EVD-000047",
      "EVD-000048"
    ],
    "open_risks": [],
    "started_at": "2026-08-08T04:12:48+00:00",
    "completed_at": "2026-08-08T04:18:20+00:00"
  },
  {
    "id": "SO-FEATURE-DESC-3361AD2E54FC-DESIGN-I003",
    "phase": "design",
    "round": "DESIGN-I003",
    "iteration_id": "ITER-FEATURE-DESC-3361AD2E54FC-DESIGN-003",
    "iteration_no": 3,
    "is_current": true,
    "superseded_by": "",
    "status": "PASSED",
    "produced_by_agent": "DesignAgent",
    "input_revisions": {
      "business_model": "BM-R07@7d7bf504ca9d"
    },
    "output_revision": "DESIGN-P2-R01@8875f042898c",
    "evidence_ids": [
      "EVD-000070",
      "EVD-000071",
      "EVD-000072",
      "EVD-000073",
      "EVD-000074",
      "EVD-000075",
      "EVD-000076",
      "EVD-000077",
      "EVD-000078",
      "EVD-000079",
      "EVD-000080",
      "EVD-000081",
      "EVD-000082",
      "EVD-000083",
      "EVD-000084",
      "EVD-000085",
      "EVD-000086",
      "EVD-000087",
      "EVD-000088"
    ],
    "open_risks": [],
    "started_at": "2026-08-08T05:33:50+00:00",
    "completed_at": "2026-08-08T05:37:43+00:00"
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
