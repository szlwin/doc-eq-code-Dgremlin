<!-- template: common-develop/requirement-list-v2 -->
# V_1.0 需求列表

> 需求确认通过后登记；每行必须包含版本号和 requirement.md 链接；需求变更通过“对应变更需求编号”关联原需求。

## 需求列表

| 需求编码 | 需求名称 | 版本号 | 需求文档 | 需求说明 | 对应变更需求编号 | 需求完成情况 |
| --- | --- | --- | --- | --- | --- | --- |
| P1-COMPILER | 基于实际 mix 的统一 AST、Registry 与 Compiler 骨架 | V_1.0 | [requirement.md](doc/P1-COMPILER-F01/requirement.md) | P1 建立实际 mix 源图、RawDefinitionSet、CompiledModelSet 与不可变 EngineContext，并整体退役 `dec-expand-declaration` | - | R02 草案完成，待重新确认与 Review |
| P1-COMPILER-CR01 | 整体退役 dec-expand-declaration | V_1.0 | [requirement_change.md](doc/P1-COMPILER-CR01/requirement_change.md) | 删除临时模块及其依赖，不建立 Adapter，P1—P8 直接基于 mix | P1-COMPILER | R02 草案完成，待关闭变更问题 |
