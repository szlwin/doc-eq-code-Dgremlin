# 结构化文档增量合并预览

- 版本：V_1.0
- 模块：COMPILER
- 当前 Agent：IncrementalArchiveAgent
- 基线 revisions：{'business_model': 'BM-R05@4ecb1f8c09f4', 'business_flow__COMPILER': 'FLOW-R02@compiler-owned-discovery'}
- 结果 revisions：{'business_model': 'BM-R06', 'business_flow__COMPILER': 'FLOW-R08@p3-information-evaluation'}
- 工作模式：model_code=false page_design=false minimal=false auto=false architecture_review=false git_checkpoint=true git_push=false lightweight=false
- 归档门禁：请在同一 merge_plan.yaml 的 archiveGate 中补充证据和授权结论

## 变更

- update business_model.terms TERM-ENGINE-CONTEXT: Align EngineContext with the simplified ConfigInfo installation model.
- add business_model.terms TERM-CONFIG-INFO: Establish the simplified configuration owner.
- add business_model.terms TERM-SIMPLE-MODEL-EXECUTION: Freeze the easy-use business calling model.
- add business_model.scenarios SCN-P2-CONFIG-PUBLISH: Add isolated candidate compilation and atomic installation.
- add business_model.scenarios SCN-P2-SIMPLE-EXECUTE: Add the direct business execution scenario.
- add business_model.scenarios SCN-P2-DEFINITION-NOT-FOUND: Add the essential fail-fast definition scenario.
- add business_model.entities ENT-P2-CONFIG-INFO: Add the candidate and installed configuration entity.
- add business_model.valueObjects VO-P2-INSTALLED-CONFIG: Represent the whole configuration publication fact.
- add business_model.entities ENT-P2-MODEL-EXECUTION: Represent one direct ModelContainer execution.
- add business_model.aggregates AGG-P2-CONFIG-PUBLICATION: Add the simplified configuration publication aggregate.
- add business_model.aggregates AGG-P2-SIMPLE-EXECUTION: Add the direct rule execution aggregate.
- add business_model.invariants INV-COMPILER-016: Protect candidate isolation.
- add business_model.invariants INV-COMPILER-017: Protect atomic installation.
- add business_model.invariants INV-COMPILER-018: Prevent half-compiled configuration use.
- add business_model.invariants INV-COMPILER-019: Freeze the direct business call.
- add business_model.invariants INV-COMPILER-020: Keep clear definition failures.
- add business_model.invariants INV-COMPILER-021: Preserve transaction and resource cleanup.
- add business_model.stateMachines SM-P2-CONFIG-CANDIDATE: Add candidate publication state without runtime session state.
- add business_model.businessErrors ERR-P2-CONFIG-COMPILE-FAILED: Add candidate failure.
- add business_model.businessErrors ERR-P2-VIEW-NOT-FOUND: Add clear View lookup failure.
- add business_model.businessErrors ERR-P2-RULE-NOT-FOUND: Add clear Rule lookup failure.
- add business_model.businessErrors ERR-P2-CONNECTION-NOT-FOUND: Add clear Connection lookup failure.
- add business_model.businessErrors ERR-P2-MODEL-EXECUTION-FAILED: Add simple execution failure.
- add business_model.traceability TR-P2-001: Trace candidate isolation.
- add business_model.traceability TR-P2-002: Trace candidate parsing.
- add business_model.traceability TR-P2-003: Trace successful installation.
- add business_model.traceability TR-P2-004: Trace failed publication behavior.
- add business_model.traceability TR-P2-005: Trace direct business execution.
- add business_model.traceability TR-P2-006: Trace explicit definition failures.
- add business_model.traceability TR-P2-007: Trace transaction cleanup.
- add business_model.traceability TR-P2-008: Trace compatibility and retirement.
- add business_model.traceability TR-P2-009: Trace atomic installation.
- add business_model.traceability TR-P2-010: Trace the simplified requirement, design and code revision.
- update business_flow.COMPILER.FLOW-CONFIG-COMPILE: Compile an isolated ConfigInfo candidate and install it only after successful EngineContext publication.
- add business_flow.COMPILER.FLOW-SIMPLE-MODEL-EXECUTE: Add the user-facing DataUtil to ModelContainer execution flow without heavy runtime access objects.
- add business_flow.COMPILER.FLOW-P3-INFORMATION-EVALUATION: Provide canonical end-to-end evidence for P3 ordering, branches, transaction outcome and blocking failures.

## 业务流程事实合并

- COMPILER：FLOW-R02@compiler-owned-discovery -> FLOW-R08@p3-information-evaluation

## Markdown 事实合并

- design_topic：version/V_1.0/doc/COMPILER/COMPILER_design.md -> docs/COMPILER/design/p2-simple-runtime.md
- module_desc：/Users/shazhoulin/mac_new/pub_work/doc-eq-code-Dgremlin/project_doc/version/V_1.0/doc/COMPILER/COMPILER_desc.md -> /Users/shazhoulin/mac_new/pub_work/doc-eq-code-Dgremlin/project_doc/docs/COMPILER/COMPILER_desc.md
- module_desc：/Users/shazhoulin/mac_new/pub_work/doc-eq-code-Dgremlin/project_doc/version/V_1.0/doc/MODEL/MODEL_desc.md -> /Users/shazhoulin/mac_new/pub_work/doc-eq-code-Dgremlin/project_doc/docs/MODEL/MODEL_desc.md
- requirement_list：/Users/shazhoulin/mac_new/pub_work/doc-eq-code-Dgremlin/project_doc/version/V_1.0/requirement_list.md -> /Users/shazhoulin/mac_new/pub_work/doc-eq-code-Dgremlin/project_doc/docs/requirement_list.md

## 已应用并跳过

- FLOW-CHG-V_1.0-COMPILER-LAYOUT-001
