# TASK-P1-T05 / I003 — Scalar Budget / Resolver Rework

- 状态：`IN_PROGRESS`
- PR：`#20`（Draft）
- Branch：`feature/p1-t05-yaml-canonical-20260802-2106`
- Rework Base：`499b977a773da3e25b776d4debf7abb1391b5192`
- Dependency：`COMPLETION-P1-T04-R02@0699c6bc2ed4`
- Superseded Completion：`COMPLETION-P1-T05-R02@27d566714f5c`
- Design：`DESIGN-R22@P1-T05-REWORK-I003`
- Plan：`TP-P1-COMPILER-F01-R18@P1-T05-REWORK-I003`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 独立 Review 输入

- Review：`REV-000244`
- Evidence：`EVD-000487`
- 结论：`NEEDS_CHANGES / REWORK`
- R02 状态：当前有效性被推翻，全部历史不可变保留

## Findings

- `FND-P1-T05-I003-001` `[P1][BLOCKER]`：单 scalar 上限在昂贵 typed 词法处理之后执行；
- `FND-P1-T05-I003-002` `[P1][BLOCKER]`：自定义 typed scalar 词法与 SnakeYAML 2.2 Resolver 不一致；
- `FND-P1-T05-I003-003` `[P1][PROCESS BLOCKER]`：R21/R17 在 clean-code 后实质修改但 Revision ID 未变化。

## 独立确认

1. `readScalarEntered` 与 `readAttributeValue` 当前均先调用 `requireAllowedScalarTag`，后调用 `reserveScalar`；
2. I002 policy 使用 `BigDecimal(value.replace("_", ""))`，可在单值门禁前执行昂贵数值构造；
3. SnakeYAML 2.2 官方 Resolver commit `a34989252e6f59e36a3aaf788a903b7a37a73d33` 的 FLOAT 指数符号为可选 `[-+]?`；
4. 官方 INT 要求二/八/十六进制前缀后至少存在一个真实数位；
5. clean-code Head `27d566714f5c...` 的 R21/R17 冻结显式非字符串 typed tag拒绝与 `isResolved()`；最终 Head 同名 Revision 改为全量自定义词法和属性名 policy，属于核心语义变化。

## 冻结修复

1. 原始 scalar 长度门禁先于任何 regex、日期或数值处理；
2. 单值上限与累计 Canonical scalar 预算拆分为两阶段；
3. 四个 scalar 位置共享同一前置验证入口；
4. 直接使用 SnakeYAML 2.2 `Resolver` 公开 Pattern；
5. 删除 `BigDecimal` 和项目复制的数字接受正则；
6. Resolver timestamp 匹配后保留真实日期/时间语义校验；
7. R22/R18 从首次提交起保持 blob 不变；
8. 不修改 R21/R17/R02 历史；
9. 不修改 Context 生产代码、compiler canonical 公共 API或 XML Frontend；
10. 不启动 T06。

## Revision Lock

- Design first commit：`ab9ca21cf668aba03f030129022458bbd46304fc`
- Design blob：`b8ffb41226866b0854def9d4ce12a6c68c150b3b`
- Plan first commit：`a2283a8661210e0ebda26a67fad05a60d770a89b`
- Plan blob：`26adb13c7192e5f7419c59acf445bf8b56b6ceb7`
- Evidence：`../evidence/revision-lock-p1-t05-r03.json`

## 历史保护

I001 和 I002 的 Design、Plan、TDD、Skeleton、Development、Review、Testing、Completion、Evidence 与机器 checkpoint 均不得覆盖或删除。R21/R17 不再修改。
