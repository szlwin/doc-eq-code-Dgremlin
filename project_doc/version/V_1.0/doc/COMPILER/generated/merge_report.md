# 结构化文档增量合并预览

- 版本：V_1.0
- 模块：COMPILER
- 当前 Agent：IncrementalArchiveAgent
- 基线 revisions：{'dependency_impact': 'P2-IMPACT-R29', 'business_flow__COMPILER': 'FLOW-R02@compiler-owned-discovery'}
- 结果 revisions：{'dependency_impact': 'P2-IMPACT-R30', 'business_flow__COMPILER': 'FLOW-R04@p2-simple-runtime-model'}
- 工作模式：model_code=false page_design=false minimal=false auto=false architecture_review=false git_checkpoint=false git_push=false lightweight=true
- 归档门禁：请在同一 merge_plan.yaml 的 archiveGate 中补充证据和授权结论

## 变更

- update dependency_impact.nodes FLOW-CONFIG-COMPILE: Align configuration publication with the simplified flow.
- add dependency_impact.nodes FLOW-SIMPLE-MODEL-EXECUTE: Add the direct business execution process.
- deprecate dependency_impact.nodes FLOW-PROTECTED-ACCESS-EXECUTE: Scope, Session, Handle, Guard and Capability execution is no longer a P2 requirement.
- update dependency_impact.nodes CTX: Context owns ConfigInfo and its associated EngineContext.
- update dependency_impact.nodes COMP: Compiler validates and publishes the candidate EngineContext.
- update dependency_impact.nodes MODEL: MODEL owns direct ModelData and ModelContainer execution.
- update dependency_impact.nodes STARTER: Starter coordinates candidate compilation and installation without an access authority layer.
- deprecate dependency_impact.nodes LOAD_REQ: RuntimeModelLoadRequest belonged only to the retired protected-access model.
- deprecate dependency_impact.nodes EFFECT: RuntimeModelEffectProvider belonged only to the retired protected-access model.
- deprecate dependency_impact.nodes TEST: The old TestDesign node validates the retired model and is not reused as simplified-model evidence.
- deprecate dependency_impact.relationships REL-P2-R29-1: Superseded by the ConfigInfo publication relationship.
- deprecate dependency_impact.relationships REL-P2-R29-2: Superseded by direct MODEL configuration consumption.
- deprecate dependency_impact.relationships REL-P2-R29-3: RuntimeModelLoadRequest is retired.
- deprecate dependency_impact.relationships REL-P2-R29-4: RuntimeModelEffectProvider is retired.
- deprecate dependency_impact.relationships REL-P2-R29-5: STARTER no longer composes protected model access.
- deprecate dependency_impact.relationships REL-P2-R29-6: Old protected-access TestDesign is not current verification evidence.
- add dependency_impact.relationships REL-P2-SIMPLE-001: Compiler publishes EngineContext to the same ConfigInfo candidate owned by CONTEXT.
- add dependency_impact.relationships REL-P2-SIMPLE-002: MODEL consumes current configuration definitions without explicit EngineContext plumbing.
- add dependency_impact.relationships REL-P2-SIMPLE-003: Starter coordinates compiler publication without owning runtime authorization.
- deprecate dependency_impact.impactPolicies IMP-P2-CONTEXT-PUBLICATION-R29: Replaced by whole ConfigInfo and EngineContext installation semantics.
- deprecate dependency_impact.impactPolicies IMP-P2-DIRECT-LOAD-R29: RuntimeModelLoadRequest validation is retired.
- deprecate dependency_impact.impactPolicies IMP-P2-EFFECT-BINDING-R29: Bound effect and Guard semantics are retired.
- add dependency_impact.impactPolicies IMP-P2-SIMPLE-CONFIG-R30: Define failure and consistency behavior for whole configuration installation.
- add dependency_impact.impactPolicies IMP-P2-SIMPLE-EXECUTE-R30: Define fail-fast and resource cleanup behavior for direct model execution.
- deprecate dependency_impact.crossModuleImplementations CMI-P2-COMPILE-005: Typed materialization publication details are superseded by ConfigInfo candidate installation.
- deprecate dependency_impact.crossModuleImplementations CMI-P2-PROTECTED-ACCESS-009: Protected access composition is retired.
- add dependency_impact.crossModuleImplementations CMI-P2-SIMPLE-CONFIG-R30: Record the parser, compiler and context handoff for the simplified candidate.
- add dependency_impact.crossModuleImplementations CMI-P2-SIMPLE-EXECUTE-R30: Record direct model definition lookup, rule execution and connection cleanup.
- update business_flow.COMPILER.FLOW-CONFIG-COMPILE: Compile an isolated ConfigInfo candidate and install it only after successful EngineContext publication.
- add business_flow.COMPILER.FLOW-SIMPLE-MODEL-EXECUTE: Add the user-facing DataUtil to ModelContainer execution flow without heavy runtime access objects.

## 业务流程事实合并

- COMPILER：FLOW-R02@compiler-owned-discovery -> FLOW-R04@p2-simple-runtime-model

## Markdown 事实合并

- design_topic：version/V_1.0/doc/COMPILER/COMPILER_design.md -> docs/COMPILER/design/p2-simple-runtime.md
- module_desc：/Users/shazhoulin/mac_new/pub_work/doc-eq-code-Dgremlin/project_doc/version/V_1.0/doc/COMPILER/COMPILER_desc.md -> /Users/shazhoulin/mac_new/pub_work/doc-eq-code-Dgremlin/project_doc/docs/COMPILER/COMPILER_desc.md
- module_desc：/Users/shazhoulin/mac_new/pub_work/doc-eq-code-Dgremlin/project_doc/version/V_1.0/doc/MODEL/MODEL_desc.md -> /Users/shazhoulin/mac_new/pub_work/doc-eq-code-Dgremlin/project_doc/docs/MODEL/MODEL_desc.md
- requirement_list：/Users/shazhoulin/mac_new/pub_work/doc-eq-code-Dgremlin/project_doc/version/V_1.0/requirement_list.md -> /Users/shazhoulin/mac_new/pub_work/doc-eq-code-Dgremlin/project_doc/docs/requirement_list.md

## 已应用并跳过

- FLOW-CHG-V_1.0-COMPILER-LAYOUT-001
