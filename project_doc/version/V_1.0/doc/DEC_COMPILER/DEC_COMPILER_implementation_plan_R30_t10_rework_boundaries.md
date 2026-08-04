# TP-P1-COMPILER-F01-R30 — TASK-P1-T10 I002 implementation plan

- Revision：`TP-P1-COMPILER-F01-R30@P1-T10-REWORK-I002`
- Design：`DESIGN-R34@P1-T10-REWORK-I002`
- Status：`PASSED`
- Parent history：R29 / I001 保留，不覆盖

## Sequential workflow

1. 记录独立 Review，失效 R01 Completion，PR #25 转 Draft。
2. 在任何新测试前冻结 R34/R30。
3. 添加嵌入式 wildcard、多 property-info、malformed root/attributes、resolver call=0 与资源计数 Oracle，取得有效 RED。
4. Architecture Review：建立 `WritePathOverlapIndex` 与 ModelAccess structural gate seam，保持受控 RED。
5. 实现完整 wildcard grammar、全 property-info 聚合、严格 root/access/ref 验证和 segment trie overlap。
6. Review property selector 资源边界、原子发布、无全局状态、Java 8 与注释格式。
7. 运行 T10 I002、T10 全量、T09/T08/T07、Compiler、XML/YAML 与 12 模块 clean verify。
8. 删除临时 snapshot/seed workflow，形成 clean-code Head；下载 Artifact 并独立验证 SHA/Surefire。
9. 写 I002 Development、Review、Testing、Revision Lock、Completion、TASK、resume_context、handoff 与 machine checkpoint。
10. 证明 clean-code 后仅 project_doc 变化，运行 final documented P0，更新 PR #25 并转 Ready for Review；不合并，T11 保持阻断。

## Oracle matrix

### Wildcard

- `a.*`、`*.a`、`a.*.b`、`*.*` 全部失败；
- `a.* + a.b`、`*.a + x.a` 不发布 Compilation；
- 完整 `*` 仍合法，并与任一第二 WRITE 重叠。

### Multi property-info

真实 `CanonicalDocumentNode → RawDefinitionBuilder → SymbolTableBuilder → ModelAccessCompiler` 覆盖：第二 section 命中、跨 section 歧义、空首 section、嵌套路径、target-main 优先。

### Structural gate

绕过 T06 的 Raw 输入覆盖：错误 root、缺失/blank model-ref、name/model-ref 不一致、body/definition attributes 不一致、root/access/ref scalar、非法 child、额外 attributes。根失败 resolver 调用数为 0。

### Resource

N 条互不重叠 WRITE 通过 package-private trie 计数证明结构查询随总 segment 数线性增长；不得使用耗时断言。

## Stop conditions

任一 P1 未转为 RED、根失败仍调用 resolver、embedded wildcard 被接受、property-info 任取首个、overlap 仍有嵌套 pair loop、失败发布部分结果、临时 workflow 残留、开放 P0/P1/P2 或最终 Head 未过 P0 时禁止 Completion。
