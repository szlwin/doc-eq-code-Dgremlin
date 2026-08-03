# TASK-P1-T05 / I003 — TDD RED Evidence

- TDD：`TDD-P1-T05-R03@3deacf0aa036`
- Review：`REV-000247`
- Evidence：`EVD-000489`
- Head：`3deacf0aa036f16e039236f2f49e25b2a3adc0e9`
- P0 Run：`30755854204`
- Artifact：`8835892685`
- Artifact SHA-256：`bc3b3047389a6b45c9a8ff9cef8e5c9078d6466381d34c5aca207714b7c4e916`
- Result：`PASSED RED`

## RED 结果

- Java release 8 生产与测试源码编译：PASSED；
- Context 正常测试：26/26 PASSED；
- Compiler：83/83 PASSED；
- XML：30/30 PASSED；
- I001/I002 YAML：45/45 PASSED；
- I003：12 run / 12 expected failures / 0 errors；
- YAML 总计：57 run / 12 expected failures / 0 errors；
- 下游 Reactor 因受控 YAML RED 停止，不声明通过；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## 十二个精确失败

- 四个 scalar 位置：预期 `yaml.frontend.limit.scalar-per-node`，实际为 `yaml.frontend.scalar.invalid-lexeme`；
- 四个 scalar 位置：`1e3` / `1.2e3` / 显式 `!!float 1e3` 预期 PARSED，实际 FAILED；
- 四个 scalar 位置：显式 `!!int 0b_` / `0x_` / `0_` 预期 FAILED，实际 PARSED。

## RED 可信性

- 使用 `maxScalarCharsPerNode = 8` 的小型注入预算；
- 不使用真实 OOM、接近 1 MiB 输入或耗时阈值；
- 超限合法与超限非法 typed scalar 同时断言诊断优先级；
- 每个负向组先验证同一生产 Frontend 能解析安全控制样本；
- 失败精确对应三个 Review Finding，不是编译错误或无关回归；
- R22/R18 已在此 Head 之前创建并锁定。
