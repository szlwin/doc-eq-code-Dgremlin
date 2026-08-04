# TASK-P1-T09 R01 Testing Evidence

## TDD Attempts

### Rejected RED

- Head：`cf8c8b6f6b4bc8b6016eb81478eb12a29448842f`
- P0 Run：`30873531942`
- Artifact：`8878689025`
- Result：`17 failures / 1 error`
- Rejection：结构测试未接管 `ClassNotFoundException`，不满足 errors=0。

### Valid RED

- Revision：`TDD-P1-T09-R01@404105e89485`
- Head：`404105e894853b36b0788ed40ac65d23d6ee8899`
- P0 Run：`30873857907`
- Artifact：`8878801137`
- Artifact SHA-256：`dd49874403e63c11082f0c037ff6d8705cc8f096bb43062261e06f1e33fc20b4`
- Result：`18 failures / 0 errors`
- Java release 8：生产与测试编译成功
- Existing Compiler regression：`195/195 PASSED`

### Architecture Skeleton

- Head：`8ae3f86316fad55349e7d76fed71336dc32c5292`
- P0 Run：`30874099740`
- Artifact：`8878893760`
- SHA-256：`04a755e8abb63120117d7421920cf773c21bff1033c0e147c1b48272a004a400`
- Result：`17 controlled failures / 0 errors`

## GREEN Attempts

- `b37c07065d2f...` / Run `30874625185`：业务合同 17/18；JaCoCo `$jacocoData` 被测试误判为 Compiler static mutable field；
- `83b616d233e1...` / Run `30874740506`：Compiler instrumentation 已过滤，AST instrumentation 仍被误判；
- `fdfdb6ac17e9...` / Run `30874829811`：T09 18/18 与全量 P0 PASSED；
- `f2cecff642c0...` / Run `30874917539`：独立 Review 6/6 与全量 P0 PASSED。

两个 JaCoCo attempt 均为测试工具 synthetic field 误报，生产源码经 GitHub 文件与 Java 8 编译确认不存在 static mutable field。

## Clean-code Validation

- Revision：`TESTING-P1-T09-R01@ecfe3f53bde7`
- Head：`ecfe3f53bde72e055c97886aef20712f6a42fea3`
- P0 Run：`30874981158` — SUCCESS
- Artifact：`8879210068`
- Artifact size：`1938368`
- SHA-256：`faeb4b46c1325fe50edbe90dc2d89098ded105fd683d994160da025bda244fb3`
- Independent ZIP SHA-256 match：`true`
- Surefire XML：`69`

## Independent Surefire Counts

- T09：`24/24`
- Symbol：`66/66`
- Compiler：`219/219`
- XML：`30/30`
- YAML：`59/59`
- Context normal：`26/26`
- Demo：`4/4`
- Legacy declaration：`1/1`
- Normal tests：`339/339`
- Intentional failure gate：`1 expected failure / recognized`
- Reactor：`12 modules / PASSED`
- Java release 8：`PASSED`
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Independent Boundary Review

`InformationIndependentReviewTest` 6/6 覆盖：重复依赖去重、多段 common 引用、128 层深度、1024 token、operator 大小写和 parser 失败时 resolver 不执行。

- `REV-000384` TddFinalReview — PASSED；
- `REV-000385` TestEvidenceReview — PASSED；
- `REV-000386` IndependentBoundaryReview — PASSED。
