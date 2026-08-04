# TASK-P1-T12 R01 Development Evidence

- Development：`DEV-P1-T12-R01@8b60fa1ea89f`
- Clean-code Head：`c6a5158209726dd9c803487993079121262a434a`
- Evidence：`EVD-000797`～`EVD-000800`

## Delivered behavior

- 十个 Pass 按 R38 固定名称和顺序执行；
- 成功路径精确产生九次状态转换并进入 `PUBLISHED`；
- StructuralValidationPass 精确推进 `PARSED → RAW_BUILT → STRUCTURALLY_VALIDATED`；
- InformationOwnership、ModelAccessBinding 和 Digest 不制造额外平行状态；
- Pass 前后检查 cancel 与 Deadline；
- PassResult ERROR、PassContext ERROR、null result、RuntimeException 均 fail-closed；
- 任一失败停止后续 Pass，PublicationPass 不执行；
- PublicationPass 异常使用 `MIX-PUBLICATION-FAILURE`；
- 每次执行创建独立 Session，集合防御性复制并冻结；
- 失败结果不暴露 artifact；
- Observer 异常不能改变编译事实，完整非 ERROR 策略留给 T13；
- compile-only execute 与内部 Session 不进入公共 API。

## Scope

生产代码只新增 `dec.core.compiler.pass`；未修改 Context、T01～T11 公共合同、P2～P7 runtime 或 Compiler API。所有 `@Override` 独占一行，公开方法、构造器和重要状态/失败逻辑均有中文注释。
