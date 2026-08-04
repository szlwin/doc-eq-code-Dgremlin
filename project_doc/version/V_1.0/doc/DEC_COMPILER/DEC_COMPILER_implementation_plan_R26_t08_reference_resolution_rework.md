# TP-P1-COMPILER-F01-R26 — TASK-P1-T08 I002 返工实施计划

- Revision：`TP-P1-COMPILER-F01-R26@P1-T08-REWORK-I002`
- Status：`PASSED`
- Design：`DESIGN-R30@P1-T08-REWORK-I002`
- Base：`PR-23@9ece664412ee947f536e2de73f20b5c7b9790bf1`
- Invalidated Completion：`COMPLETION-P1-T08-R01@ab432a3189f4`
- Branch：`feature/p1-t08-reference-resolution-20260803-2254`
- Target PR：`#23`
- Execution：`SEQUENTIAL / auto / architecture_review / git_checkpoint`
- Findings：`FND-P1-T08-I002-001`～`004`

## 1. 顺序流程

1. 保留 I001/R01 全部历史，登记独立 Review 与四个 Finding；
2. 冻结 R30/R26，更新 TASK-P1-T08 当前 iteration 为 I002/REWORK；
3. 仅新增 I002 负向与集成 Oracle，生产代码保持 R01，执行有效 RED；
4. RED 必须 Java 8 编译成功、errors=0，失败只来自新合同未满足；
5. Architecture Skeleton 落地完整快照身份、lexical parser、safe TypedKey、lexical summary、lookup observer 与受控未实现边界；
6. 顺序执行 ArchitectureReviewAgent 与 SpecComplianceReviewAgent；任一未通过不得进入具体实现；
7. 完成 Role Policy 接入与全部 Finding 修复；
8. 执行 T08 定向、T07 Symbol 回归、Compiler 全量、12 模块 `clean verify`、故意失败门禁；
9. 独立执行 Specification、Engineering、Architecture、Security、TDD、Test Evidence 与 Completion Review；
10. 形成 clean-code Head，下载 Artifact、独立 SHA-256 与 Surefire XML 解析；
11. 仅追加 R02 Completion、Review、Evidence、TASK/resume/handoff 后形成 final documented Head；
12. final documented Head 再执行 P0、Artifact 独立校验和 Revision Integrity；
13. 更新 PR #23 正文并转为 Ready for Review，不合并、不启动 T09。

## 2. TDD RED Oracle

### 2.1 Lexical Boundary

新增测试必须在 R01 上行为失败而非编译失败：

- `user.active.extra`；
- ` .active`；
- `user. `；
- blank target-main；
- blank data-ref@name；
- blank view-ref@name；
- data-ref/view-ref 缺失 ref/name；
- 所有输入相关异常必须由 `assertDoesNotThrow` 包装并断言返回 `FAILED + reference.owner.invalid`。

### 2.2 Snapshot Binding

使用同 ordinal 但 name/kind/sourceRef/body 不同、上一 revision、增加、删除与重新编号的 RawDefinitionSet 调用旧 SymbolTable，断言：

```text
FAILED
reference.input.snapshot-mismatch
no partial references
```

### 2.3 Complexity

使用多个不同 BusinessScope 下同名 DirectoryKey，加多个当前 Scope 缺失同名 rel 引用；通过反射寻找 package-private lookup observer seam，计数 lexical summary 查询。R01 因 seam 不存在而断言失败；修复后 M 个失败引用只允许 O(M) 次摘要查询，不按 N 个候选放大。

### 2.4 Canonical Integration

以真实 Canonical 文档构造合法主路径和全部 malformed lexical，经过 RawDefinitionBuilder、SymbolTableBuilder、ReferenceResolver；禁止只测手工 Raw fixture。

## 3. Architecture Skeleton

Skeleton 必须编译并显式保留受控 RED：

- `SymbolTable` 保存 package-private 完整 RawDefinitionSet 快照；
- `SymbolTableBuilder` 在成功构建时绑定快照；
- `ReferenceResolver` 在任何索引前验证快照；
- `LexicalCandidateSummary` 预聚合类型代表；
- `LookupObserver` 为 package-private、默认无副作用；
- simple/qualified parser 与 safe key 构造边界存在；
- Role Policy 尚未全部切换时返回稳定 `reference.rework.not-implemented`，不得伪造成功。

## 4. GREEN 实现

- 所有直接 TypedKey 构造迁移到安全 helper；
- System declaration index 与实际 System resolve 复用同一 lexical helper；
- 缺失 ref/name 节点产生 owner invalid；
- qualified Information 恰好一个点、两段非空；
- lexical 失败分类与 RuleView name 查询只访问摘要；
- 快照不匹配入口立即失败；
- 任一普通引用错误继续完整聚合，失败不发布部分结果；
- 保持成功查询精确 TypedKey，不引入模糊或 first-match。

## 5. 验证矩阵

- I002 新测试全部通过；
- T08 I001 12 项继续通过；
- T07 Symbol 44 项及后续新增回归全部通过；
- Compiler 模块全量通过；
- XML/YAML/Context/Demo/Legacy 回归通过；
- 12 模块 Reactor 与 Java release 8 通过；
- 故意失败门禁按预期失败并被识别；
- MySQL 仅按真实状态报告，不把 `SKIPPED_NOT_APPLICABLE` 表述为通过。

## 6. Revision Integrity

- R30/R26 必须在 I002 RED 前创建；
- 首次 commit、blob SHA 与 final clean-code Head 复核一致；
- I001 R29/R25、R01 Completion、Review、Evidence 与失败 attempt 不覆盖、不删除；
- clean-code Head 到 final documented Head 只允许 `project_doc` 变化。

## 7. 停止条件

以下任一情况立即阻断 Completion：

- 需要改变 Context、T06 Raw 或 T07 Symbol 公共合同；
- RED 出现编译错误或测试 error；
- 输入相关异常仍可逃逸；
- 资源分类仍扫描候选 List；
- 快照只比较 ordinal 或部分字段；
- Canonical 集成路径未覆盖 Review 指定节点；
- 侵入 T09/T10/P2～P7；
- `@Override` 不独占一行或关键逻辑缺少中文注释；
- 任一 P0/P1 Finding 未关闭。

## 8. Review

- `REV-000357` — PlanReviewAgent — `PASSED`；
- Evidence：`EVD-000604`；
- 下一阶段：I002 TDD RED。