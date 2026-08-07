<!-- managed-by: common-develop/wk-d -->
# 项目需求列表

> 本文件由各版本 `requirement_list.md` 通过 `wk -d` 按需求编码合并；需求文档链接已改写为对应版本目录中的稳定相对路径。

## 需求列表

| 需求编码 | 需求名称 | 版本号 | 需求文档 | 需求说明 | 对应变更需求编号 | 需求完成情况 |
| --- | --- | --- | --- | --- | --- | --- |
| P1-COMPILER | 统一 `mix` AST、Registry 与 Compiler 骨架 | V_1.0 | [requirement.md](../version/V_1.0/doc/P1-COMPILER-F01/requirement.md) | P1 基于实际 mix 建立统一编译骨架，Information 归属 System，BusinessScope 仅编排，并保留 P2～P7 Deferred 边界 | P1-COMPILER-CR01、P1-COMPILER-CR02、P1-COMPILER-CR03 | P1 Stage Completion 已完成：Code Review I008、Testing I009、Completion I009 均 PASSED；PR #31 已合并至 dev_all@7f001bb0d7e5；P1 wk -d 归档已完成 |
| P1-COMPILER-CR01 | 整体退役 `dec-expand-declaration` | V_1.0 | [requirement.md](../version/V_1.0/doc/P1-COMPILER-CR01/requirement.md) | 删除临时模块及其依赖，不建立 Adapter，P1—P8 直接基于 mix | P1-COMPILER | 已完成退役验收；T15 declaration runtime retirement gate 在最终 P0 Build Gate 中 PASSED |
