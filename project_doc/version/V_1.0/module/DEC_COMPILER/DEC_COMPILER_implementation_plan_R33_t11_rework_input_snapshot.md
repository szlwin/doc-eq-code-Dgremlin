# TP-P1-COMPILER-F01-R33 — TASK-P1-T11 I002 implementation plan

- Revision：`TP-P1-COMPILER-F01-R33@P1-T11-REWORK-I002`
- Design：`DESIGN-R37@P1-T11-REWORK-I002`
- Status：`PASSED`
- Base：`dev_all@f97b7e47ac0fb40209c4dc512aa15d67c19be44b`

## Sequential workflow

1. 将独立 Review 的两个 Finding 登记到 I002，推翻但保留 R01 Completion。
2. 冻结 R37/R33，并记录首次提交与 blob；不得覆盖 I001 Revision。
3. 新增 I002 阻断 Oracle，先验证旧实现出现业务 RED，errors 必须为 0。
4. Architecture Review 复用现有 Input/Builder/Result 原子发布 seam，不新增公共 API。
5. 修正 `resolvedReferences(null)` 的 provided 语义，显式空列表继续合法。
6. 在 null 批次门禁后形成局部批次快照，后续仅遍历快照；复制异常转换为稳定 Diagnostic。
7. 运行 I002、T11 全部测试和 T07～T10 受影响回归。
8. 运行 Compiler 模块全量测试、12 模块 `clean verify`、Java release 8 与故意失败门禁。
9. 下载最终 Artifact，独立计算 SHA-256 并解析全部 Surefire XML。
10. 独立执行 Specification、Architecture、Security、Code、TDD、Test Evidence 与 Completion Review。
11. 更新 R02 Completion、Review、Revision Lock、resume/handoff 与 PR #26 描述；不合并 PR。

## Acceptance gates

- `resolvedReferences(null)` 与未调用 setter 均产生 `deferred.incomplete.resolved-references`；
- 显式空列表成功，含 null 元素使用 `resolved-reference-null`；
- Builder 先设置合法列表再设置 null 时最终失败；
- 任一上述错误阻断整个批次，Registry 缺席；
- build 在遍历前复制输入批次，复制后不再读取原 List；
- 自定义 List 的复制异常不得越过结果边界；
- snapshot 中 null 元素继续产生稳定 Diagnostic；
- Open P0/P1/P2=`0/0/0`；
- `@Override` 独占一行，方法和重要逻辑使用中文注释；
- Java 8、全 Reactor、故意失败门禁与 Artifact Evidence 通过。

## Validation

```bash
./mvnw -pl dec-core-compiler -am \
  -Dtest=DeferredI002ReworkTest,DeferredClassificationTest,DeferredCompletenessTest,DeferredIndependentReviewTest test

./mvnw -pl dec-core-compiler -am \
  -Dtest=SymbolRegistrationTest,ReferenceResolverContractTest,InformationOwnershipTest,ModelAccessSelectorTest test

./mvnw -pl dec-core-compiler -am test
./mvnw clean verify
```
