# TP-P1-COMPILER-F01-R29 — TASK-P1-T10 implementation plan

- Revision: `TP-P1-COMPILER-F01-R29@P1-T10-I001`
- Status: `PASSED`
- Design: `DESIGN-R33@P1-T10-I001`
- Base: `dev_all@4fe0f6def8581e5c7234d86dfa0aafae794db15f`
- Dependency: `COMPLETION-P1-T09-R02@95b08223083f`
- Branch/PR: `feature/p1-t10-rule-dag-20260804-1428` / `#25`

## Sequential workflow

1. 冻结 R33、R29 和 T10 I001 入口。
2. 添加 `ModelAccessSelectorTest`、`ModelAccessFailureTest` 与 focused fixture，取得 Java 8、errors=0 的有效 RED。
3. 建立 value objects、resolver/compiler/result API Architecture Skeleton，保持受控 RED。
4. Review Skeleton 的不可变性、输入门禁、范围和无运行时执行边界。
5. 实现 exact declared-View、target-main-first、property path traversal、duplicate/overlap、P2 Deferred 与全批原子发布。
6. 删除 fixture 中冗余 `payInfo.payDetailList` WRITE。
7. 运行 directed T10、T09、T08/T07、Compiler full、XML/YAML、12 模块 P0 与故意失败门禁。
8. 删除临时 source workflow，形成 clean-code Head；下载并独立校验 Artifact。
9. 写 Development、Review、Testing、Revision Lock、Completion、TASK、resume_context、handoff 和 machine checkpoint。
10. 确认 clean-code 后仅 project_doc 变化，运行 final documented P0，更新 PR #25 并转 Ready for Review；不合并。

## Oracle matrix

### Success

- target-main 与同名 property 同时存在时 target-main 优先；
- target-main 未命中后解析 `payInfo.payDetailList`；
- 多 ref 生成多 Binding，source path 与 selector 不混淆；
- 无 ref 的直接 read/write 不制造目标 Binding但进入 P2 Deferred body；
- matching Raw/Symbol snapshot、source View 与 declared target View 成功；
- Binding、Compilation、Deferred collections 不可修改且稳定排序。

### Failure

- 未声明 target View、未知 source View；
- 大小写差异、缺段、root-property/模糊候选不得回退；
- 中间段非复合；
- 同层重复 property 多候选；
- 完全重复 ref；
- 相同、祖先/后代 WRITE 重叠；
- Raw/Symbol snapshot deletion/addition/body/order/ordinal mismatch；
- malformed owner/path/ref/property 只返回 Diagnostic，不泄漏 IllegalArgumentException；
- 任一失败不发布部分 Binding/Deferred，resolver 在快照失配时调用 0 次。

## Validation

```bash
./mvnw -pl dec-core-compiler -am \
  -Dtest=ModelAccessSelectorTest,ModelAccessFailureTest test
./mvnw --batch-mode --no-transfer-progress clean verify
```

MySQL 无配置时为 `SKIPPED_NOT_APPLICABLE`，不得报告 PASSED。

## Stop conditions

输入快照未门禁、跨 View/System 搜索、大小写折叠、root-property/模糊回退、部分发布、运行时权限/SQL/执行语义、Context/前置公共合同修改、无效 RED、开放 P0/P1 或最终 Head 未经过 P0 时立即停止 Completion。
