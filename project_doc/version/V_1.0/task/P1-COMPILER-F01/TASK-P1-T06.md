# TASK-P1-T06 / I001 — RawDefinitionSet

- 状态：`IN_PROGRESS`
- Branch：`feature/p1-t06-raw-definition-20260803-1334`
- Base：`dev_all@17ce0834b947a75ff3ccbd24c7b1332fb93e8941`
- Dependency：`COMPLETION-P1-T05-R03@30529276cd8f`
- Design：`DESIGN-R23@P1-T06-I001`
- Plan：`TP-P1-COMPILER-F01-R19@P1-T06-I001`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 目标

实现 Canonical → RawDefinitionSet 的严格、确定性、不可变、fail-closed 转换：

- 14 类 RawDefinition；
- sourceOrdinal；
- SourceRef、owner/name、attributes、references、normalized body；
- 完整结构白名单；
- 失败无部分集合。

## 范围边界

不实现 TypedKey、SymbolTable、引用解析、Deferred、Pipeline、Digest、CompiledModelSet、Publication 或 T07。

## Revision Lock

- Design first commit：`8a6cadfbb35f82820dd077a44033c6ba179ad77c`
- Design blob：`f7eb09ff6291c3f84bb93ace88a6d301d9ef73ad`
- Plan first commit：`5bab7c508e27762d306d672fd925f2c743fbd245`
- Plan blob：`e5e65b495ffe01c8265061d975f2264d31b761e7`
- Evidence：`evidence/revision-lock-p1-t06-r01.json`

## 初始 Gate

- PR #20：已合并；
- dev_all 精确 Head：`17ce0834b947a75ff3ccbd24c7b1332fb93e8941`；
- T05 当前 Completion：`COMPLETION-P1-T05-R03@30529276cd8f`；
- T06 分支与 dev_all 初始比较：identical；
- T07：未启动。

## 历史保护

T01～T05 的 Design、Plan、TDD、Development、Review、Testing、Completion、Evidence 和机器 checkpoint 均不得覆盖或删除。
