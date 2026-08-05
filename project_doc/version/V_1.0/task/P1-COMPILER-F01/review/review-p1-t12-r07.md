# TASK-P1-T12 / I004 Independent Review

- Code Review：`CODEREVIEW-P1-T12-R07@923129b1f20d`
- Reviewed Clean-code Head：`923129b1f20d6bebe589231b770b5c7675b52737`
- Result：`PASSED`
- Reviews：`REV-000570`～`REV-000590`
- Open P0/P1/P2：`0 / 0 / 0`

## Findings

### FND-P1-T12-I004-001 `[P1][BLOCKER][RESOURCE]` — CLOSED

- JVM 递归 freeze 已替换为显式 traversal stack；
- VISITING/FROZEN identity 状态区分循环和共享 DAG；
- 已完成共享子图复用同一 immutable snapshot；
- depth、unique containers、edges、map entries 四类预算在物化前阻断；
- 资源超限稳定返回 FAILED、`pipeline.artifact.resource-exceeded`、publisher=0；
- 不捕获 `StackOverflowError` 或 `OutOfMemoryError` 作为实现策略。

### FND-P1-T12-I004-002 `[P2][ORACLE]` — CLOSED

新增资源 Oracle 覆盖：

- 深度恰好等于预算与超过预算；
- 24 层共享 DAG 和输出 identity 复用；
- counting List 线性操作数；
- unique-container、List/Set edge、Map entry/edge 预算；
- 循环图；
- 普通/final Pass 资源失败及 publisher=0；
- Optional/List、Map key/value 共享 identity；
- 深路径不能借 FROZEN 节点绕过 depth；
- 非资源 iterator failure 分类；
- 组合容器深度不可变性。

### FND-P1-T12-I004-003 `[P1][RESOURCE][INDEPENDENT REVIEW]` — CLOSED

首轮 GREEN 后发现：即使 traversal 已 memoize，目标 `LinkedHashSet/LinkedHashMap` 仍会对共享冻结 DAG 递归调用 `hashCode()`，产生指数 CPU 放大。

修复：

- 容器底向上生成 canonical structural ID；
- Set element 和 Map key collision 使用 canonical ID，不调用递归 hash；
- Frozen List/Set/Map 缓存与 Java 容器合同一致的结构 hash；
- canonical key 仅包含 immediate child ID，构建复杂度受 unique graph/edge 预算约束；
- 新 Oracle 使用计数 immutable leaf，确认共享 DAG 作为 Set 元素时 hash 调用不放大。

## Architecture / security review

- 默认 limits 固定为 256 / 4096 / 65536 / 16384；
- limits 仅 package-private，不扩展公共 Compiler API；
- explicit stack、memoization 和 canonical metadata 总量均受现有预算约束；
- scalar/ImmutablePipelineArtifact 继续视为受信任不可变叶子；
- null、未知可变对象、循环、Map/Set collision 原合同不变；
- prepare/commit、final Diagnostic、Clock、Deadline、Context/Result、commit-wins 均未回归；
- 未实现 T13/T14/T15 或 P2～P7 runtime。

## Validation

- Valid RED：Run `30974123330`，6 expected failures / 0 errors；
- First GREEN：Run `30974452808` — SUCCESS；
- Independent Review GREEN：Run `30974629383` — SUCCESS；
- Hash-amplification Review RED：Run `30974844132`，1 expected failure / 0 errors；
- Clean-code P0：Run `30975103715` — SUCCESS；
- Artifact：`8917961744`；SHA-256：`df328a44496836e018c4725714adece969f46e0f71a0228c337ff9cadb71a640`；
- I004 17/17；T12 83/83；Compiler 402/402；正常测试 522/522；
- Surefire XML 94；Errors/Skipped 0/0；故意失败门禁按预期通过；
- Java release 8、12 模块 Reactor：PASSED；MySQL：`SKIPPED_NOT_APPLICABLE`。

## Style review

- 所有 `@Override` 注解独占一行；
- 公开方法、构造器及 traversal、memoization、budget、canonical ID/hash、collision 和 failure mapping 逻辑均有中文注释；
- 生产修改仅位于 `dec.core.compiler.pass`。

Independent Review：`PASSED / NO_OPEN_FINDINGS`。
