# P1-COMPILER-F01 阶段交接

## 已完成的设计调整草案

- 基于实际 `dec-demo/src/main/resources/mix` 重写源图和定义模型；
- 输出改为 `CompiledModelSet`；
- 术语改为 `RawDefinition`；
- Business 改为 `BusinessScope` 逻辑作用域；
- P2～P7 语义进入显式 Deferred Registry；
- 删除 declaration Adapter/迁移设计；
- 新增模块整体退役和残留扫描门禁。

## 当前状态

`BLOCKED / requirement_confirmation / REQCONF-I002`

R02 文档是待评审草案，不等于阶段已经通过。必须依次完成 requirement_confirmation、requirement_analysis、business_model、design 和串行 Review，关闭 `ISSUE-P1-SCOPE-CHANGE-001` 后才能进入 test_design。

## 当前草案 Revision

- REQCONF-R02-DRAFT；
- REQAN-R03-DRAFT；
- BM-R02-DRAFT；
- DESIGN-R02-DRAFT。
