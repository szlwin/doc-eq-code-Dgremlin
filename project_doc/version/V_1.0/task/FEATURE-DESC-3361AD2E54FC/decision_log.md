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
    "rationale": "P2 固定目标由正式 P0-P8 计划和 Request Intake 明确，用户要求继续执行。",
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
    "decision": "采用 direct bridge invocation。caller 可逐次提交 exact ModelAccessRuleKey 与 current P2 AccessOperation；当前 AccessOperation 范围由 DEC-P2-ACCESS-OPERATIONS-001 冻结为 READ/WRITE。AccessConsumerIrKey 仅作为 provenance/diagnostic，不是 authorization-key 维度。",
    "rationale": "用户明确撤销 token 方案并采用 direct bridge；PolicyIndex、Gateway/Guard、runtime proof、actual-target/operation capability binding 与 fail-closed policy miss 继续有效。",
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
    "supersedes": ""
  },
  {
    "id": "DEC-P2-AC007-STAGE-BOUNDARY-001",
    "status": "ACTIVE",
    "category": "SCOPE",
    "question": "AC-P2-SYSTEM-RULEVIEW-007 最终由 P2 验收 seam/no-bypass并下沉 concrete integrations，还是 P2 本身提供代表性 production consumers 执行原 literal AC？",
    "options_considered": [
      "A: P2 seam/no-bypass；真实 integrations 下沉 P3/P4/P6",
      "B: P2 提供真实 production Rule/change/custom-action representative consumers"
    ],
    "decision": "采用 Option B。P2 必须交付并通过真实 production main-source Rule、change、custom-action 三类 representative protected-access consumers；三类入口都真实执行授权/未授权场景并经过同一个 production composition -> ProtectedExecutionBridge -> Gateway -> Guard 权限链。P3/P4/P6 完整业务语义仍留后续阶段。",
    "rationale": "用户明确选择 Option B；保留原 AC-007 concrete-entry acceptance，不用 seam-only 替代。",
    "decided_by": "user",
    "decided_at": "2026-08-09T11:55:00+08:00",
    "affects": [
      "requirement_analysis",
      "business_model",
      "business_flow",
      "design",
      "test_design",
      "impact_analysis",
      "cross_module_integration",
      "P3",
      "P4",
      "P6"
    ],
    "supersedes": ""
  },
  {
    "id": "DEC-P2-ACCESS-OPERATIONS-001",
    "status": "ACTIVE",
    "category": "BUSINESS_RULE",
    "question": "Current P2 model-access operation 集合是否包含 EXECUTE？",
    "options_considered": [
      "READ/WRITE/EXECUTE",
      "READ/WRITE only"
    ],
    "decision": "Current P2 model-access 只有 READ 与 WRITE，没有 EXECUTE。AccessOperation current contract exactly = READ|WRITE；不新增 EXECUTE source syntax/raw IR/policy/runtime/test contract。",
    "rationale": "用户明确说明当前只有 READ、WRITE，没有 EXECUTE；真实 P1 AccessMode 也只有 READ/WRITE。",
    "decided_by": "user",
    "decided_at": "2026-08-09T12:36:00+08:00",
    "affects": [
      "requirement_analysis",
      "business_model",
      "business_flow",
      "design",
      "test_design",
      "impact_analysis",
      "implementation_plan",
      "tdd",
      "development"
    ],
    "supersedes": ""
  },
  {
    "id": "DEC-P2-MODEL-LOADING-BOUNDARY-001",
    "status": "ACTIVE",
    "category": "SCOPE",
    "question": "P2 MODEL production loading 是否采用 opaque RuntimeModelProductionInvocation credential，还是采用 direct RuntimeModelLoadRequest 并把 MODEL production lifecycle 作为可信生产边界？",
    "options_considered": [
      "opaque RuntimeModelProductionInvocation credential with root/replay/one-shot semantics",
      "direct RuntimeModelLoadRequest transport inside trusted MODEL production lifecycle"
    ],
    "decision": "P2 不采用 opaque production invocation credential；RuntimeModelProductionInvocation 及其 root/replay/one-shot 语义仅保留为 historical/deferred。Current P2 采用 RuntimeModelLoadRequest(RuntimeBindingPlan, originObject, ruleName, connectionName) 作为 ACTIVE production loading DTO，并将 MODEL production lifecycle 作为可信生产边界。request possession 不产生 READ/WRITE authority；ModelAccessRuleKey/Guard 仍是唯一权限 authority。BM-R20 与 FLOW-R11 不因此修改。",
    "rationale": "用户明确选择简化 P2 loading；current Design/TestDesign 已保持 captured Context validation、MODEL-owned Container、same ModelData -> Handle -> Session -> resolve -> Guard -> same effect invariant，因此无需用额外 opaque credential 承担权限或 proof-to-effect 语义。",
    "decided_by": "user",
    "decided_at": "2026-08-10T12:36:00+08:00",
    "affects": [
      "design",
      "test_design",
      "impact_analysis",
      "cross_module_integration",
      "implementation_plan",
      "tdd",
      "development"
    ],
    "supersedes": ""
  },
  {
    "id": "DEC-P2-POST-COPY-ROLLBACK-EXCLUSION-001",
    "status": "ACTIVE",
    "category": "SCOPE",
    "question": "P2 是否要求在 legacy ModelContainer 已将值 BeanUtils.copy 到 POJO/Map、但后续 legacy commit 失败时恢复已经 copy 的对象状态？",
    "options_considered": [
      "P2 新增 post-copy snapshot/restore 或 commit-after-copy rollback 机制",
      "维持 legacy 行为并明确该 post-copy restoration 不属于 P2 blocking scope"
    ],
    "decision": "P2 不要求恢复已经 copy 到 POJO/Map 后又遇到后续 legacy commit failure 的对象状态。仍要求 Guard DENY/所有 pre-effect failure 不产生 protected WRITE/copy-back，且正常成功路径必须到达现有真实 originData write-back。该 exclusion 不得重新作为 Design/TestDesign blocker。",
    "rationale": "用户已明确确认该 legacy post-copy rollback 不需要修改；本决定只冻结 P2 scope，不改变 Guard-before-effect、same-target effect 或正常成功 write-back 语义。",
    "decided_by": "user",
    "decided_at": "2026-08-10T12:36:00+08:00",
    "affects": [
      "design",
      "test_design",
      "implementation_plan",
      "tdd",
      "development"
    ],
    "supersedes": ""
  },
  {
    "id": "DEC-P2-SINGLE-RUNTIME-CONTEXT-001",
    "status": "ACTIVE",
    "category": "ARCHITECTURE",
    "question": "P2 production runtime 是否支持同一 runtime lifecycle 内的 EngineContext 热替换/多 Context 共存，并因此要求逐 Scope/Handle 的 exact RuntimeContextBinding？",
    "options_considered": [
      "同一 runtime 支持多 EngineContext / hot reload，并保留 exact RuntimeContextBinding 与 cross-context provenance 拒绝",
      "一个 runtime lifecycle 只绑定一个 EngineContext；配置更新必须重启；不支持 hot reload"
    ],
    "decision": "采用 single-runtime-context。每个 production runtime lifecycle/generation 只在 bootstrap 时绑定一次 compiler-published EngineContext，并在该 lifecycle 内保持不可替换；配置变化必须先终止旧 runtime（关闭旧 Root/Scope/Frame/Handle），再启动新的 runtime generation 并绑定新 EngineContext；不支持 live context swap、runtime republish 或 hot reload。Compiler、测试和离线编译仍可在不同 compilation/session 或不同 runtime generation 中构造多个 candidate EngineContext；该能力不等于同一 active runtime 支持多 Context。P2 不再要求 RuntimeContextBinding，也不再把 same-plan cross-context mixing 作为受支持运行态的阻断性验收场景。",
    "rationale": "用户于 2026-08-18 明确确认真实运行模型为单 EngineContext/runtime lifecycle。该约束消除了 DESIGN-P2-R32 中 P2-CR-002 的运行前提，避免为不支持的 hot-reload/multi-context 场景传播额外 context identity；P2-CR-001 的 Guard 唯一权限 authority 与 raw MODEL bypass closure 完全保留。Requirement Overlay R04 的 atomic publication/old Context preservation/Context isolation 继续适用于 Compiler publication 与不同 runtime generation，不被解释为 live runtime replacement 要求。",
    "decided_by": "user",
    "decided_at": "2026-08-18T01:14:00+08:00",
    "affects": [
      "design",
      "test_design",
      "impact_analysis",
      "cross_module_integration",
      "implementation_plan",
      "tdd",
      "development"
    ],
    "supersedes": ""
  }
]
```

## 当前记录结构

字段集合以 `assets/long-task/record-contract.json#records.decisionLogItem` 为准。历史事实不删除。

## 当前 Gate

- Direct Bridge / AC-007 Option B / READ-WRITE only：ACTIVE。
- MODEL loading：direct `RuntimeModelLoadRequest` ACTIVE；opaque production invocation credential = NOT_ADOPTED / DEFERRED；MODEL production lifecycle = trusted P2 boundary；request != permission；Guard/ModelAccessRuleKey = sole READ/WRITE authority。
- Legacy post-copy rollback exclusion：ACTIVE；不要求恢复已 copy 后再遇到 later legacy commit failure 的 POJO/Map；pre-effect fail-closed 与正常成功 originData write-back 继续要求。
- Single runtime Context：`DEC-P2-SINGLE-RUNTIME-CONTEXT-001` ACTIVE；每个 runtime generation 只绑定一次 EngineContext，配置更新必须 restart；live swap/hot reload = NOT_SUPPORTED；Compiler 可跨 session/generation 构造多个 candidate。
- DESIGN-P2-R32 / TESTDESIGN-P2-R36 中依赖 multi-context runtime 前提的 context-binding/cross-context 条款被当前决策 supersede，必须通过新的 Design/TestDesign candidate 回修；历史内容保持可审计。
- Canonical `task_events.jsonl` 仍停在 `TE-000094 -> DESIGN-P2-R32 PASSED / TESTDESIGN-P2-R34 PASSED`；新的 reopen/finalize 必须由 common-develop append-only writer/reducer 安全写入，禁止手改 ledger。
- Implementation Plan / TDD / Development remain BLOCKED until corrected Design/TestDesign are canonically finalized。
