<!-- template: common-develop/requirement-list-v2 -->
# V_1.0 需求列表

> 需求确认通过后登记；每行必须包含版本号和 requirement.md 链接；需求变更通过“对应变更需求编号”关联原需求。

## 需求列表

| 需求编码 | 需求名称 | 版本号 | 需求文档 | 需求说明 | 对应变更需求编号 | 需求完成情况 |
| --- | --- | --- | --- | --- | --- | --- |
| P1-COMPILER | 统一 `mix` AST、Registry 与 Compiler 骨架 | V_1.0 | [requirement.md](doc/P1-COMPILER-F01/requirement.md) | P1 基于实际 mix 建立统一编译骨架，Information 归属 System，BusinessScope 仅编排，并保留 P2～P7 Deferred 边界 | P1-COMPILER-CR01、P1-COMPILER-CR02、P1-COMPILER-CR03 | REQCONF-R04、REQAN-R05、BM-R05、DESIGN-R05 已通过；当前 test_design I007 |
| P1-COMPILER-CR01 | 整体退役 `dec-expand-declaration` | V_1.0 | [requirement.md](doc/P1-COMPILER-CR01/requirement.md) | 删除临时模块及其依赖，不建立 Adapter，P1—P8 直接基于 mix | P1-COMPILER | 已确认；在 P1 实施阶段按计划执行退役验收 |
