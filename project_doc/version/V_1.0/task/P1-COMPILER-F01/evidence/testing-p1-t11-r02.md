# TASK-P1-T11 I002 Testing Evidence

- Testing：`TESTING-P1-T11-R02@86b55b45d1cd`
- Reviews：`REV-000485`～`REV-000486`
- Evidence：`EVD-000778`～`EVD-000783`
- Head：`86b55b45d1cd658401ec541fa12bfd868ef5fadc`
- P0 Run：`30919883791` — SUCCESS
- Artifact：`8896619234`
- Artifact digest：`sha256:1e37ba710cf47c7f8ff22c1d2e8d7509cadbcc0172c7ed28a30924fcaf9f2294`
- Independent ZIP SHA-256：`1e37ba710cf47c7f8ff22c1d2e8d7509cadbcc0172c7ed28a30924fcaf9f2294`

## Independent Surefire parse

- Surefire XML：`83`
- 全部测试记录：`440`
- 正常测试：`439 / 439 PASSED`
- Intentional failure gate：`1`，按预期失败并被 P0 识别
- Errors：`0`
- Skipped：`0`
- I002：`8 / 8 PASSED`
- T11 Deferred：`34 / 34 PASSED`
- Compiler module：`319 / 319 PASSED`
- 12 模块 Reactor：`PASSED`
- Java release 8：`PASSED`
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Affected regression

以下既有路径在全量 Compiler 测试中保持绿色：

- T07 Symbol 注册与确定性；
- T08 强类型引用解析；
- T09 Information owner/common 绑定；
- T10 ModelAccess selector；
- T11 I001 分类、完整性、确定性与 4096 项资源边界。

Artifact ZIP 已独立下载、计算 SHA-256 并解析全部 Surefire XML，结果与 GitHub digest 和 P0 结论一致。
