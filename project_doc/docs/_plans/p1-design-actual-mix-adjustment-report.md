# P1 设计按实际 `mix` 调整报告

## 结论

P1 设计已从“抽象推测的 mix 目录”调整为用户提供实物：根配置直接发现 Data/View/System/Business，System 再间接发现 Rule。设计不再使用 `CompiledBusiness`、`RawDeclaration` 或 declaration Adapter。

## 调整文件

- P1 requirement、analysis、concept model、test matrix、testability；
- DEC_COMPILER business model、architecture、design、API、test seams；
- mix contract inventory；
- dependency graph/impact；
- task plan、traceability、acceptance assertions、handoff 和 current draft state；
- P1/P8 总计划中的核心术语。

## 状态

文档为 R02 草案，未伪造 Review 或 StageOutcome。P1 仍阻断在 requirement_confirmation。
