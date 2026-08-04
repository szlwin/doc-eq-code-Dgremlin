# TASK-P1-T09 R01 Development Evidence

- Revision：`DEV-P1-T09-R01@ecfe3f53bde7`
- Clean-code Head：`ecfe3f53bde72e055c97886aef20712f6a42fea3`
- Owner package：`dec.core.compiler.information`

## Production Components

- `InformationExpressionAst`：不可变 REFERENCE/AND/OR AST，稳定 canonical 与前序引用；
- `DefaultInformationExpressionParser`：单遍 tokenizer、递归下降 parser、长度/token/深度预算；
- `DefaultInformationReferenceResolver`：严格两段 qualified target、精确 `InformationKey` 与 `SymbolTable.find`；
- `InformationCommonValidator`：common System section、Information attributes/body 与 ModelAccess 边界；
- `InformationCompiler`：完整批次协调、Diagnostic 聚合、原子发布；
- `InformationCompilation` / `ResolvedInformationExpression`：不可变成功事实；
- parser/resolver/result seam：支持独立测试注入且无运行时全局状态。

## Published Contract

每个成功 expression 创建：

- `DeferredKey(ownerInformationKey, INFORMATION, 0)`；
- `RequiredStage.P3`；
- `reasonCode=information-expression-evaluation`；
- `NormalizedBody(information-expression-ast/v1, canonical AST)`；
- 稳定排序且去重的精确 `InformationKey` dependencies。

普通 System 跨 owner、普通未限定、common 未限定、unknown target、common 非法成员、语法错误和资源越界均返回专用 Diagnostic。任何 ERROR 都不发布部分 AST、依赖或 Deferred Registry。

## Scope and Security

- 未修改 Context、T06 Raw、T07 Symbol、T08 或 Compiler API；
- 未修改 `systems.xml`；
- 无求值、DAG、循环检测、缓存、I/O、网络、反射执行、模糊查询或 cross-type fallback；
- 所有 parser/resolver 状态为单次调用局部状态；
- `InformationCompiler` 仅含两个 final 实例 seam；
- 所有新增 `@Override` 独占一行；
- 方法、构造器与重要 parser、owner、common、Diagnostic、资源和失败逻辑使用中文注释。

## Reviews

- `REV-000380` DevelopmentSpecificationReview — PASSED；
- `REV-000381` EngineeringStandardsReview — PASSED；
- `REV-000382` ArchitectureFinalReview — PASSED；
- `REV-000383` SecurityReview — PASSED。
