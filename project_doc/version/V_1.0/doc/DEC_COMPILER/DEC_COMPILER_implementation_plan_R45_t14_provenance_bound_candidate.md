# DEC_COMPILER IMPLEMENTATION PLAN R45 — TASK-P1-T14 / I002

- Plan ID：`TP-P1-COMPILER-F01-R45@P1-T14-REWORK-I002`
- Design：`DESIGN-R49@P1-T14-REWORK-I002`
- Status：`FROZEN`

## Step 1 — Invalidate and RED

- 保留并失效 `CODEREVIEW-P1-T14-R01@252024603bfc` 与 `COMPLETION-P1-T14-R01@252024603bfc`；
- 新增可编译的 I002 测试；
- 证明 request 版本 mismatch 当前仍可 PUBLISHED；
- 证明任意 DigestPair/非法摘要当前可进入 candidate；
- 为缺失快照 Oracle 建立直接反例；
- RED 只接受行为失败，不接受无关 testCompile 失败。

## Step 2 — Atomic provenance

- 新增 `DigestBoundCompiledInput`；
- 由 `CompilerDigestService.bind()` 使用同一 `SourceManifest + SemanticDigestInput` 原子计算并封装摘要；
- 正式边界校验 64 位小写 SHA-256；
- 删除 Builder 分别接收版本、模型事实和 Digest 的生产入口；
- Builder 仅冻结 provenance-bound 输入。

## Step 3 — Publication binding

- Publication Pass 在 prepare 前校验 compiler/schema/options 与当前 request；
- mismatch 形成 `MIX_PUBLICATION_PROVENANCE_MISMATCH / ERROR`；
- FAILED、publisher=0、artifacts empty；
- 正常路径完整 candidate 精确传递，Pipeline 继续唯一持有 publisher capability。

## Step 4 — Oracle completion

为 Definition 与 Deferred 分别覆盖：negative size、keys/size mismatch、duplicate、missing value、identity mismatch、final-size drift、snapshot 后零读取。增加非空完整模型、Warning、真实 Digest、精确 Diagnostic 和完整 publisher candidate 断言。

## Step 5 — Review and validation

- T14 定向；
- T13/T12 回归；
- Compiler module；
- 全 Reactor；
- intentional failure gate；
- 最终冻结 Head P0；
- Artifact 独立 SHA-256 与 Surefire XML 解析；
- Revision integrity；
- 新 Code Review、Testing、Completion、Handoff；
- 更新 PR #29，保持未合并。

## Style

- Java release 8；
- `@Override` 必须独占一行；
- 类、方法、provenance、摘要绑定、快照和失败边界使用中文注释；
- 不引入新依赖、反射、sleep、wall-clock 或共享可变测试状态。
