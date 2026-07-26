# P1—P8 调整报告

## 结论

P7 必须调整，而且不能只改 P7。原计划把 `dec-expand-declaration` 作为可复用能力和 Adapter 迁移来源，导致 P1—P6 的 AST、System、Information、Action/Produce、Directory 和 Query 仍可能隐式继承第二套模型。现已统一改为整体退役策略。

## 各阶段调整

- P1：增加整体删除模块、POM 和依赖任务；旧核心 Config Adapter 不覆盖 declaration；Business 明确为逻辑作用域。
- P2：删除旧 SystemDesc/BusinessDesc 映射，改为 `mix` System + BusinessScope 边界。
- P3：Information 直接按 `mix` 实现；消费语义改为依赖/read-set，不建立 Consumer runtime。
- P4：ActionInput/ActionResult/Produce 重新定义，不复用 Producer/Consumer SPI。
- P5：DirectoryEngine 是唯一流程执行器，不调用 workflow callback 或 BusinessDeclare。
- P6：QueryCompiler 只依赖统一 Compiled AST。
- P7：删除能力抽取、LegacyDeclarationAdapter、旧入口冻结与迁移任务；改为核心 Session、事务、外部副作用、资源生命周期及残留门禁。
- P8：增加仓库/Reactor/依赖树/artifact/文档残留扫描和最终退役报告。

## 不变项

- `mix` 中的 System、BusinessScope、Information、Directory、Action、Produce 和 Back 仍是目标业务概念；
- 删除的是临时 Maven 模块和第二套运行时，不是删除 `mix` 业务语义；
- P0 历史 Evidence 保留原始事实，但后续构建以删除模块后的 Reactor 为准。
