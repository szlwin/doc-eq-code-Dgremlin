# TASK-P1-T06 I004 TDD RED Evidence

- Revision：`TDD-P1-T06-R04@e2e41dac48fe`
- Head：`e2e41dac48fe3ccef948efb333443de81d3466ca`
- P0 Build Gate：Run `30809689151` — `FAILURE`（预期 RED）
- Artifact：`8854234669`
- Artifact SHA-256：`9f66cc5e3dc91ce29650cf580a8c01430d31622776b0fabf1132e8c394c5b60c`
- Review：`REV-000299` — TDDReviewAgent — `PASSED`
- Evidence：`EVD-000541`

## 编译与既有回归

- Java release 8 生产源码：编译成功；
- Java release 8 测试源码：编译成功；
- Context：26/26 PASSED；
- I002 + I003 既有 Raw：38/38 PASSED；
- I004：8 run / 5 failures / 0 errors / 0 skipped / 3 passed；
- Compiler 总计：129 run / 5 failures / 0 errors；
- Reactor 在 `dec-core-compiler` 按预期停止，后续模块未被错误声明为通过；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## 精确 RED

通过的小预算 Oracle：

1. 两个单根文档等于预算时允许构建；
2. 单文档后代仍由完整树 ValidationBudget 拒绝；
3. 两文档正常路径不调用 List 的 size/isEmpty/get/toArray/stream/parallelStream/spliterator。

目标失败 Oracle：

1. 第三个文档未在 snapshot.add 前返回 `raw.limit.node-count`；
2. Diagnostic 未绑定第三个文档；
3. Builder 继续请求第三项之后的 iterator 状态，触发测试主动异常；
4. 资源失败虽未发布集合，但错误类型为 `raw.build.failed`；
5. 单次 iterator 事实成立，但没有在预算边界及时停止。

测试不执行真实 OOM、无限循环或线程调度。失败全部来自 `FND-P1-T06-I004-001` 目标行为缺失，构成有效 RED。
