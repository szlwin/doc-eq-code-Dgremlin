# TASK-P1-T15 R02 Revision Lock

- Status：`LOCKED`
- Iteration：`TASK-P1-T15 / I002`
- Base：`665dd364975505bb01263885a25b3bb1be767d2b`
- Code/Test Revision：`7c901332b8e5c559a73c127e1a1bd86411f8adc1`

## Frozen inputs

- Design：`DESIGN-R51@P1-T15-I001`
  - first commit：`4e47d3a4b57f40ee2da6c9fcd4ba30e572bbd9b2`
  - frozen blob：`3a11a6f8f8110ab0c187d07a3a88bf4c442c0516`
- Plan：`TP-P1-COMPILER-F01-R47@P1-T15-I001`
  - first commit：`c5d0537f95f0d0b7c95be2d6e9bbff0151a643b4`
  - frozen blob：`051e41e77d3f5c40a8248e3de1bb94c65e71ed8d`
- TDD：`TDD-P1-T15-R01@bff67b86fb55` — VALID；
- Architecture：`DEVSKEL-P1-T15-R01@bff67b86fb55` — VALID；
- Rework Finding：`FND-P1-T15-I001-002`。

## Superseded history

- `CODEREVIEW-P1-T15-R01@f36b03e6243`；
- `TESTING-P1-T15-R01@f36b03e6243`；
- `COMPLETION-P1-T15-R01@f36b03e6243`。

以上记录保留，不删除；其生产代码结论仍有效，但 retirement gate 和 Completion 结论由 R02 取代。

## R02 revisions

- Initial hardening：`d0bbec6b3dd54774ffb4c840619064214b334f94`；
- Final Code/Test Revision：`7c901332b8e5c559a73c127e1a1bd86411f8adc1`；
- Development：`DEV-P1-T15-R02@7c901332b8e5`；
- Code Review：`CODEREVIEW-P1-T15-R02@7c901332b8e5`；
- Testing：`TESTING-P1-T15-R02@7c901332b8e5`；
- Completion：`COMPLETION-P1-T15-R02@7c901332b8e5`。

## Validation lock

- Run / Artifact：`31092216605 / 8963981122`；
- SHA-256：`b012e85a83b93fba76341fdeee5c719d147e57673e97d036f44abde259f7a016`；
- Surefire XML：110；All：633；Normal passed：632；intentional failure：1；Errors/Skipped：0/0；
- baseline POM / Reactor：`11 / 11-of-11`；
- baseline class / compiled resource：`947 / 205`；
- baseline Artifact / entry / unreadable：`10 / 958 / 0`；
- mutation Reactor：12/12；七类回流全部检测；
- baseline / expected mutation failure / restored baseline：`PASSED / OBSERVED / PASSED`；
- Finding `FND-P1-T15-I001-002`：`CLOSED`；Open P0/P1/P2：`0/0/0`。

本 Lock 后只允许更新 `project_doc` 和 PR 元数据；若再次修改脚本、Workflow、生产或测试代码，必须重新打开迭代并使本 Lock 失效。
