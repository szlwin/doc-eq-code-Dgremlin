# DESIGN-R38 — TASK-P1-T12 十阶段 Compiler Pipeline 与 Session 状态机

- Revision：`DESIGN-R38@P1-T12-I001`
- Status：`PASSED`
- Base：`dev_all@3f53ea4a31b0b1366aad383f665736b0487d4d00`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- Parent Design：`DESIGN-R05@0b37a9b4dd48`

## 1. 范围

本 Revision 只实现 `dec.core.compiler.pass` 中的 Pipeline 编排和 Session 状态机，不实现 T13 Digest 算法、Observer 失败策略、T14 候选 Context 构造/CAS 发布或 T15 Starter 接入。

允许新增：

- `CompilerPass`
- `PassContext`
- `PassResult`
- `CompilationSession`
- `CompilerPipeline`
- `PipelineExecutionResult`
- 仅供本包使用的 Diagnostic、固定顺序和不可变快照辅助类型

不得修改 T01～T11 已发布公共合同，不得执行 P2～P7 runtime、SQL、事务、I/O、网络、DAG 或缓存。

## 2. 固定 Pass 顺序

顺序严格冻结为：

1. `SourceGraphValidationPass`
2. `StructuralValidationPass`
3. `SymbolRegistrationPass`
4. `ReferenceResolutionPass`
5. `InformationOwnershipPass`
6. `ModelAccessBindingPass`
7. `DeferredClassificationPass`
8. `P1SemanticValidationPass`
9. `DigestPass`
10. `PublicationPass`

Pipeline 构造时必须验证数量、名称、顺序和 null；调用方传入 List 必须防御性复制。任何偏差在执行前失败，不得运行任一 Pass。

## 3. Session 状态机

唯一合法成功路径：

```text
CREATED
→ SOURCES_DISCOVERED
→ PARSED
→ RAW_BUILT
→ STRUCTURALLY_VALIDATED
→ SYMBOLS_REGISTERED
→ REFERENCES_RESOLVED
→ GRAPH_PREPARED
→ SEMANTICALLY_VALIDATED
→ PUBLISHED
```

任一非终态可以进入 `FAILED`；`PUBLISHED` 与 `FAILED` 均为终态，禁止继续转换。

固定 Pass 与状态转换：

- SourceGraphValidationPass：`SOURCES_DISCOVERED`
- StructuralValidationPass：`PARSED → RAW_BUILT → STRUCTURALLY_VALIDATED`
- SymbolRegistrationPass：`SYMBOLS_REGISTERED`
- ReferenceResolutionPass：`REFERENCES_RESOLVED`
- InformationOwnershipPass：不单独推进状态
- ModelAccessBindingPass：不单独推进状态
- DeferredClassificationPass：`GRAPH_PREPARED`
- P1SemanticValidationPass：`SEMANTICALLY_VALIDATED`
- DigestPass：不单独推进状态
- PublicationPass：`PUBLISHED`

## 4. 执行与失败边界

每次 `CompilerPipeline.execute(...)` 必须创建新的 `CompilationSession`。Session 持有 request、publication request、局部 artifact、Diagnostic、状态转换、Pass 执行记录和 timing；禁止 static/thread-local 可变状态。

每个 Pass 前后都检查：

- `CancellationToken`
- `Deadline` 与调用方注入的 `MonotonicClock`
- 当前 Diagnostic 是否包含 ERROR

任一条件失败：

- 进入 `FAILED`
- 稳定排序并冻结 Diagnostic
- 停止后续 Pass
- `PublicationPass` 不得执行

取消使用 `MIX-COMPILATION-CANCELLED`，超时使用 `MIX-COMPILATION-TIMED-OUT`，Pass 返回 ERROR 或抛 RuntimeException 使用 `MIX-PUBLICATION-BLOCKED`；PublicationPass 抛异常使用 `MIX-PUBLICATION-FAILURE`。

## 5. PassContext 与原子性

`PassContext` 只暴露当前 Session 的局部状态：

- request / publicationRequest
- 不可变读取的当前 state
- session-local artifact put/get
- Diagnostic 登记

artifact key 必须非空白，value 非 null。失败结果不暴露成功 artifact 快照；成功结果可以只读方式暴露，用于 T13/T14 后续扩展。

## 6. Timing 与观察

T12 使用 request 中注入的 `MonotonicClock` 记录每个 Pass 的非负耗时，并形成 `CompilationTiming(TimingPhase.PASS, passName, elapsedNanos)`。状态转换形成 `SessionStateTransition`。T13 将继续实现完整 Observer 失败隔离；T12 不允许 Observer 改变 Pipeline 状态。

## 7. 测试门禁

至少验证：

- 10 Pass 精确顺序和唯一成功状态路径；
- 任一位置 ERROR 均进入 FAILED；
- ERROR、cancel、timeout 后 PublicationPass 调用数为 0；
- PUBLISHED/FAILED 终态拒绝；
- reordered/missing/duplicate/null Pass 构造失败且 0 执行；
- 两个 Session 的 artifact、Diagnostic、timing、transition 完全隔离；
- Pass 列表、结果集合和 artifact 快照不可变；
- RuntimeException 不越过 Pipeline 结果边界；
- 生产类型无 static/thread-local 可变状态。

## 8. 编码约束

- Java release 8；
- 所有 `@Override` 注解独占一行；
- 所有公开方法、构造器与重要状态/失败逻辑使用中文注释；
- 不新增公共 compile-only 成功入口；
- 未经用户明确授权不得合并后续 PR；PR 合并前 T13 阻断。
