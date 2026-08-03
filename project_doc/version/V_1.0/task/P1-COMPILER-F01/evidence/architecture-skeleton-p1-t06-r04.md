# TASK-P1-T06 I004 Architecture Skeleton Evidence

- Revision：`DEVSKEL-P1-T06-R04@2d78c2290498`
- Input Head：`2d78c2290498e4f72a1aca8b8444537ed0dbe610`
- Design：`DESIGN-R26@P1-T06-REWORK-I004`
- Plan：`TP-P1-COMPILER-F01-R22@P1-T06-REWORK-I004`
- Reviews：`REV-000300` ArchitectureReviewAgent — `PASSED`；`REV-000301` SpecComplianceReviewAgent — `PASSED`
- Evidence：`EVD-000542`、`EVD-000543`
- TDD State：I004 8 run / 5 expected failures / 0 errors

## 冻结代码骨架

```java
private List<CanonicalDocumentNode> snapshotDocuments(
        List<CanonicalDocumentNode> documents) {
    // null 输入与逐项 null 检查保持现有优先级
    List<CanonicalDocumentNode> snapshot =
            new ArrayList<CanonicalDocumentNode>();

    for (CanonicalDocumentNode document : documents) {
        if (document == null) {
            throw failure("raw.document.required", UNKNOWN_SOURCE);
        }

        checkSnapshotDocumentLimit(snapshot.size(), document);
        snapshot.add(document);
    }

    // empty 与不可变返回保持现有合同
}

private void checkSnapshotDocumentLimit(
        int documentCount,
        CanonicalDocumentNode document) {
    if (documentCount >= limits.maxCanonicalNodeCount()) {
        throw failure("raw.limit.node-count", document.sourceRef());
    }
}
```

## Review 结论

- `snapshotDocuments` 必须由 static 改为实例方法，仅为读取注入的 `RawBuilderLimits`；
- 检查发生在取得非 null 当前文档之后、`snapshot.add` 之前；
- 第 N+1 个文档的 SourceRef 是唯一失败位置；
- 抛出受控 `RawBuildFailure` 后 enhanced-for 立即退出，不请求第四项；
- 不调用原始 List 的随机访问、转换、Stream 或 Spliterator 入口；
- 前置文档数检查不能删除或弱化后续完整树 `ValidationBudget`；
- 不新增 public API、static 可变状态、I/O、线程、Error 捕获或 T07 类型；
- 生产范围仍仅为 `RawDefinitionBuilder.java`。

该骨架已在 Development 前完成独立 Architecture 与 Spec Compliance Review；实现者只能按此形状落盘，不得改变 R26/R22。
