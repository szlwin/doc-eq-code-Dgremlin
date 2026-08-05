# TESTING-P1-T13-R02 — I002 Testing Evidence

- Testing：`TESTING-P1-T13-R02@7d39c3bc0ab4`
- Evidence：`EVD-001026`～`EVD-001033`
- Code/Test Revision：`7d39c3bc0ab45b6cd3c8ab637c10ae40a15e07b8`
- P0 Run：`31011874941` — SUCCESS
- Artifact：`8932801028`
- Artifact SHA-256：`679600735885f589a6370b0ad54845c909a24b2749b7b5edc4ac231822a8bf05`

## Independent artifact parse

- Surefire XML：106；
- 全部测试记录：607；
- 正常通过：606；
- intentional failure gate：1 项按预期失败；
- Errors：0；
- Skipped：0。

## T13 matrix

- `SemanticDigestDeterminismTest`：6/6；
- `SemanticDigestIndependentReviewTest`：13/13；
- `SemanticDigestStrictUnicodeI002ReviewTest`：3/3；
- `CompilationDeadlineTest`：3/3；
- `CompilationObserverTest`：4/4；
- `CompilationObserverIndependentReviewTest`：5/5；
- T13 合计：34/34。

## Regression

- T12：133/133；
- Compiler module：486/486；
- 正常全量：606/606；
- 12 模块 Reactor：PASSED；
- Java release 8：PASSED；
- 故意失败阻断证明：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## Required behavior

- malformed high/low surrogate：稳定 fail-closed；
- malformed 位于首/中/尾：稳定 fail-closed；
- exception/message/cause：稳定；
- 合法 supplementary：通过；
- ASCII/BMP/supplementary known vectors：保持；
- 并发无状态：通过；
- FAILED Observer：原 ERROR + Warning，state FAILED，publisher=0，artifacts empty。

Artifact 已独立下载，计算的 ZIP SHA-256 与 GitHub digest 完全一致。
