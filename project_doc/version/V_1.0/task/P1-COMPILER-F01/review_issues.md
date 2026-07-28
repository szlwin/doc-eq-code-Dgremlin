# P1-COMPILER-F01 Review 问题台账

```json review-issues
[
  {
    "id": "ISSUE-P1-SCOPE-CHANGE-001",
    "issue_type": "SCOPE_DRIFT",
    "axis": "ARCHITECTURE",
    "severity": "P1",
    "confidence": 10,
    "status": "CLOSED",
    "phase": "requirement_confirmation",
    "round": "REQCONF-I002",
    "artifact_revision": "P1-COMPILER-CR01",
    "raised_by_agent": "ProjectManagerAgent",
    "owner_agent": "RequirementConfirmationAgent",
    "title": "dec-expand-declaration 整体退役导致 P1 需求和设计 Revision 失效",
    "description": "原 Revision 假设保留/迁移 declaration 边界；新决策要求模块整体删除且无 Adapter。",
    "impact": "不得进入 test_design；需重新执行需求确认、分析、业务模型、设计及串行 Review。",
    "motivating_evidence": [
      "version/V_1.0/doc/P1-COMPILER-CR01/requirement_change.md"
    ],
    "question": "新的 P1 设计是否完整覆盖模块删除、mix 场景重写和依赖残留门禁？",
    "question_to": [
      "RequirementConfirmationAgent",
      "RequirementAnalysisAgent",
      "DesignAgent"
    ],
    "responses": [],
    "recommendation": "从 requirement_confirmation 开启新 iteration。",
    "affected_artifacts": [
      "requirement",
      "requirement_analysis",
      "business_model",
      "design",
      "test_design",
      "implementation_plan"
    ],
    "affected_trace_ids": [
      "TR-P1-COMPILER-007"
    ],
    "decision": "用户确认 dec-expand-declaration 整体退役，实际 mix 是唯一配置契约；R02/R03/BM-R02/DESIGN-R02 草案已覆盖模块删除、mix 场景重写和残留扫描门禁。",
    "resolution_revision": "GOV-REPAIR-R01@2422fc8521da",
    "resolution_evidence": "EVD-000215",
    "verified_by_agent": "ProjectManagerAgent",
    "verified_at": "2026-07-26T02:47:18+00:00",
    "defer_reason": ""
  },
  {
    "id": "ISSUE-MR-0001",
    "issue_type": "INCONSISTENCY",
    "axis": "ARCHITECTUREREVIEW",
    "severity": "P1",
    "confidence": 9,
    "status": "OPEN",
    "phase": "design",
    "round": "DESIGN-I004",
    "artifact_revision": "DESIGN-R04@1c14c8e89779",
    "raised_by_agent": "ArchitectureReviewAgent",
    "owner_agent": "DesignAgent",
    "title": "组件、模块和依赖边界是否合理？",
    "description": "P1：ModelCompiler.compile 只接收 CompilationRequest，无法访问 ContextPublisher 或 expectedCurrent；但详细设计要求同一 CompilationSession 原子暴露后进入 PUBLISHED，架构又把 CAS 发布放在 compile 返回后的 Starter，组件职责和终态所有权冲突。",
    "impact": "MANUAL_REVIEW assertion ASRT-P1-R4-DES-ARCH-001 cannot pass",
    "motivating_evidence": [
      "EVD-000276",
      "EVD-000277",
      "EVD-000278"
    ],
    "question": "请补充或修正后重新执行人工 Review。",
    "question_to": [
      "DesignAgent"
    ],
    "responses": [],
    "recommendation": "修正相关产物并补充当前 revision 的证据，然后重新 Review。",
    "affected_artifacts": [
      "design"
    ],
    "affected_trace_ids": [],
    "decision": "",
    "resolution_revision": "",
    "resolution_evidence": "",
    "verified_by_agent": "",
    "verified_at": "",
    "defer_reason": ""
  },
  {
    "id": "ISSUE-MR-0002",
    "issue_type": "INCONSISTENCY",
    "axis": "ARCHITECTUREREVIEW",
    "severity": "P1",
    "confidence": 9,
    "status": "OPEN",
    "phase": "design",
    "round": "DESIGN-I004",
    "artifact_revision": "DESIGN-R04@1c14c8e89779",
    "raised_by_agent": "ArchitectureReviewAgent",
    "owner_agent": "DesignAgent",
    "title": "数据流、事务和失败恢复路径是否完整？",
    "description": "P1：失败结果被要求强制携带 DigestPair，但 Source discovery、parse 等早期失败无法产生完整 semanticDigest；CompilationResult 又引入 CANCELLED/TIMED_OUT，而业务状态仅允许 PUBLISHED/FAILED，缺少确定映射。另有 CompiledModelSet 包含 DigestPair、semanticDigest 又基于 CompiledModelSet 计算的循环定义。",
    "impact": "MANUAL_REVIEW assertion ASRT-P1-R4-DES-ARCH-001 cannot pass",
    "motivating_evidence": [
      "EVD-000276",
      "EVD-000277",
      "EVD-000278"
    ],
    "question": "请补充或修正后重新执行人工 Review。",
    "question_to": [
      "DesignAgent"
    ],
    "responses": [],
    "recommendation": "修正相关产物并补充当前 revision 的证据，然后重新 Review。",
    "affected_artifacts": [
      "design"
    ],
    "affected_trace_ids": [],
    "decision": "",
    "resolution_revision": "",
    "resolution_evidence": "",
    "verified_by_agent": "",
    "verified_at": "",
    "defer_reason": ""
  },
  {
    "id": "ISSUE-MR-0003",
    "issue_type": "INCONSISTENCY",
    "axis": "ARCHITECTUREREVIEW",
    "severity": "P1",
    "confidence": 9,
    "status": "OPEN",
    "phase": "design",
    "round": "DESIGN-I004",
    "artifact_revision": "DESIGN-R04@1c14c8e89779",
    "raised_by_agent": "ArchitectureReviewAgent",
    "owner_agent": "DesignAgent",
    "title": "性能、安全、可用性等质量属性是否有落实？",
    "description": "P1：需求要求 discovery、parse、pass、digest 计时接缝，但 CompilationMetrics 未定义组成、时钟和观察接口；需求还要求每条源图边保留 SourceRef，当前 MixSourceGraph edge 没有字段契约与验证接缝。",
    "impact": "MANUAL_REVIEW assertion ASRT-P1-R4-DES-ARCH-001 cannot pass",
    "motivating_evidence": [
      "EVD-000276",
      "EVD-000277",
      "EVD-000278"
    ],
    "question": "请补充或修正后重新执行人工 Review。",
    "question_to": [
      "DesignAgent"
    ],
    "responses": [],
    "recommendation": "修正相关产物并补充当前 revision 的证据，然后重新 Review。",
    "affected_artifacts": [
      "design"
    ],
    "affected_trace_ids": [],
    "decision": "",
    "resolution_revision": "",
    "resolution_evidence": "",
    "verified_by_agent": "",
    "verified_at": "",
    "defer_reason": ""
  },
  {
    "id": "ISSUE-MR-0004",
    "issue_type": "INCONSISTENCY",
    "axis": "ARCHITECTUREREVIEW",
    "severity": "P1",
    "confidence": 9,
    "status": "OPEN",
    "phase": "design",
    "round": "DESIGN-I004",
    "artifact_revision": "DESIGN-R04@1c14c8e89779",
    "raised_by_agent": "ArchitectureReviewAgent",
    "owner_agent": "DesignAgent",
    "title": "其余检查项（路径完整、模型与设计映射）是否均满足？",
    "description": "P1：AC-P1-COMPILER-001 要求从根入口恰好发现 10 个 XML 和固定类型边，测试接缝只说明主/测试共 20 个 XML 可解析，不能证明 Resolver 的源图边界；Diagnostic 排序中的 entityKey 与 definitionKey/passId 也没有显式映射。",
    "impact": "MANUAL_REVIEW assertion ASRT-P1-R4-DES-ARCH-001 cannot pass",
    "motivating_evidence": [
      "EVD-000276",
      "EVD-000277",
      "EVD-000278"
    ],
    "question": "请补充或修正后重新执行人工 Review。",
    "question_to": [
      "DesignAgent"
    ],
    "responses": [],
    "recommendation": "修正相关产物并补充当前 revision 的证据，然后重新 Review。",
    "affected_artifacts": [
      "design"
    ],
    "affected_trace_ids": [],
    "decision": "",
    "resolution_revision": "",
    "resolution_evidence": "",
    "verified_by_agent": "",
    "verified_at": "",
    "defer_reason": ""
  }
]
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
