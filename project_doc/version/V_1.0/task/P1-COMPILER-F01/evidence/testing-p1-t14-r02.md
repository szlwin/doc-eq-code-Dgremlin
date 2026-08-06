# TESTING-P1-T14-R02 — I002 Testing Evidence

- Testing：`TESTING-P1-T14-R02@668d865b0189`
- Code/Test Revision：`668d865b0189e9107f25295a1726748968aa7462`
- Status：`PASSED`

## Clean-code validation

- P0 Run：`31069685120` — SUCCESS；
- Artifact：`8955166219`；
- SHA-256：`5553810bfb87146c97835dd5d1c2de10b4c2b8405a9ef533e994f110c7b71c6c`；
- Surefire XML：109；
- T14：18/18；
- T13：34/34；
- T12：133/133；
- Compiler：504/504；
- 正常测试：624/624；
- 全部记录：625；
- intentional failure：1；
- Errors/Skipped：0/0；
- 12 modules Reactor：PASSED；
- Java release 8：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## Independent parse

Artifact 已独立下载并解析；ZIP SHA-256 与 GitHub digest 完全一致。唯一 Failure 为 `P0IntentionalFailureTest`，用于证明测试失败会阻断构建，并由 workflow 正确识别。

## T14 suites

- `CandidateContextT14Test`：5/5；
- `CandidateContextT14I002RedTest`：2/2；
- `CandidateContextT14IndependentReviewTest`：11/11。
