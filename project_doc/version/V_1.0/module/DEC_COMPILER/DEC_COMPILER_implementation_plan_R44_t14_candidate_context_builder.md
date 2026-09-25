# DEC_COMPILER IMPLEMENTATION PLAN R44 — TASK-P1-T14

- Plan ID：`TP-P1-COMPILER-F01-R44@P1-T14-I001`
- Design：`DESIGN-R48@P1-T14-I001`
- Base：`dev_all@3e4da420d2ef5ada8398aefbbeabb37964e384ce`
- Status：`FROZEN`

## Step 1 — RED

新增 T14 测试，先验证当前缺少：

- `CompiledModelSetBuilder`；
- `CandidateContextPublicationPass`；
- Publication Context 的稳定 Diagnostic 读取入口；
- frozen input artifact 与 candidate 构建接线。

RED 必须为可编译的合同失败；若类型尚不存在，先提交最小测试架构桩再形成行为 RED，不接受无关 testCompile 失败。

## Step 2 — Builder

实现一次性 Builder：

- 固定四阶段输入；
- 每阶段立即快照；
- Definition/Deferred identity 校验；
- build 前完整性门禁；
- build 后封闭；
- 生成实现 `ImmutablePipelineArtifact` 的 frozen input；
- 由 frozen input 根据 Diagnostic 快照构造 candidate。

## Step 3 — Publication Pass

实现 `CandidateContextPublicationPass`：

- 固定 PublicationPass 名称；
- 读取唯一 T14 artifact key；
- 缺失输入返回 publication-blocked ERROR；
- 构造 candidate 后只执行 `prepare()`；
- 不接触 Publisher/CAS。

为 `PublicationPassContext` 增加只读、稳定排序的 Diagnostic 快照入口，不增加发布能力。

## Step 4 — Independent Review

独立验证：

- mutable Registry/Deferred 在阶段入口后不再读取；
- candidate 与 frozen facts 精确一致；
- missing/error/duplicate/order/reuse 全部 fail-closed；
- final pass 不具备 publisher capability；
- publisher 调用次数与最终状态准确；
- T12/T13 Deadline、Observer、Digest 和 publication 回归保持。

## Step 5 — Validation and Completion

执行：

1. T14 定向测试；
2. T13、T12 回归；
3. Compiler module；
4. 全 Reactor；
5. intentional failure gate；
6. final documented P0；
7. Artifact 独立 SHA-256 与 Surefire XML 解析；
8. Revision integrity 比较；
9. Review、Evidence、Completion、Handoff；
10. 创建并更新 T14 PR，保持未合并。

## Style

- `@Override` 必须独占一行；
- 类、方法、阶段状态、快照时机、异常边界和重要测试逻辑使用中文注释；
- Java release 8；
- 不引入新依赖、反射、默认 Charset、线程等待或 wall-clock 测试。
