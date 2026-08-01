# P1-COMPILER-F01 测试矩阵

> Revision：`TESTDESIGN-R01@ba7779cf089b`。本矩阵由 `TestDesignAgent` 基于 `DESIGN-R05@0b37a9b4dd48` 冻结；详细 Given/When/Then、禁止副作用、RED 合同和命令见 [test_case.md](test_case.md)。

| Case | TR | AC | 类型 | 主要 oracle | 失败 code |
|---|---|---|---|---|---|
| `CASE-P1-TD-SOURCE-MANIFEST-001` | TR-P1-COMPILER-001 | AC-P1-COMPILER-001 | contract | SourceManifest 恰好包含 10 个固定 sourceId；声明边恰好 7 条且 edgeType/from/target/SourceRef 精确匹配；inventory 为 5 Data、2 View、4 System、14 RuleView、16 Information、1 Scope、5 Directory、8 Action、4 Produce。 | — |
| `CASE-P1-TD-SOURCE-ORDER-001` | TR-P1-COMPILER-001, TR-P1-COMPILER-005 | AC-P1-COMPILER-001, AC-P1-COMPILER-005 | boundary | sourceId、边集合、Diagnostic 顺序和 semanticDigest 完全一致；sourceDigest 可因原文字节变化而独立变化。 | MIX-DIGEST-NONDETERMINISTIC |
| `CASE-P1-TD-SOURCE-POLICY-001` | TR-P1-COMPILER-001 | AC-P1-COMPILER-001 | negative | 统一返回 FAILED Diagnostic=MIX-SOURCE-POLICY，SourceRef 指向声明位置。 | MIX-SOURCE-POLICY |
| `CASE-P1-TD-SOURCE-NOT-FOUND-001` | TR-P1-COMPILER-001 | AC-P1-COMPILER-001 | negative | 产生 MIX-SOURCE-NOT-FOUND；CompilationSession FAILED；Publisher 调用 0 次。 | MIX-SOURCE-NOT-FOUND |
| `CASE-P1-TD-SOURCE-SECURITY-001` | TR-P1-COMPILER-001 | AC-P1-COMPILER-001 | security | 访问前拒绝并产生 MIX-SOURCE-PATH-ESCAPE；网络/根外读取计数为 0。 | MIX-SOURCE-PATH-ESCAPE |
| `CASE-P1-TD-SOURCE-DUPLICATE-001` | TR-P1-COMPILER-001 | AC-P1-COMPILER-001 | negative | 两组均以 MIX-SOURCE-DUPLICATE-ID 阻断，不覆盖已有 Source。 | MIX-SOURCE-DUPLICATE-ID |
| `CASE-P1-TD-FRONTEND-XML-001` | TR-P1-COMPILER-002 | AC-P1-COMPILER-002 | security | 解析前/解析中拒绝，产生 MIX-FRONTEND-XML-UNSAFE；外部访问计数 0。 | MIX-FRONTEND-XML-UNSAFE |
| `CASE-P1-TD-FRONTEND-YAML-001` | TR-P1-COMPILER-002 | AC-P1-COMPILER-002 | security | 产生 MIX-FRONTEND-YAML-UNSAFE；不实例化任意类型。 | MIX-FRONTEND-YAML-UNSAFE |
| `CASE-P1-TD-CANONICAL-PARITY-001` | TR-P1-COMPILER-002, TR-P1-COMPILER-005 | AC-P1-COMPILER-002, AC-P1-COMPILER-005 | contract | CanonicalDocumentNode 和 semanticDigest 等价；SourceRef 保留各自来源；Canonical 不持有 DOM/YAML Node。 | — |
| `CASE-P1-TD-STRUCTURE-UNKNOWN-001` | TR-P1-COMPILER-002 | AC-P1-COMPILER-002 | negative | 所有 P1 模式均产生 ERROR MIX-STRUCTURE-UNKNOWN，未知节点 SourceRef 精确。 | MIX-STRUCTURE-UNKNOWN |
| `CASE-P1-TD-RAW-INVENTORY-001` | TR-P1-COMPILER-002 | AC-P1-COMPILER-002 | contract | RawDefinitionSet 数量与类型精确匹配固定 inventory，全部对象有 SourceRef。 | — |
| `CASE-P1-TD-SYMBOL-DUPLICATE-001` | TR-P1-COMPILER-003 | AC-P1-COMPILER-003 | negative | 同 TypedKey 产生 MIX-SYMBOL-DUPLICATE；异类型同名可共存。 | MIX-SYMBOL-DUPLICATE |
| `CASE-P1-TD-REFERENCE-001` | TR-P1-COMPILER-003 | AC-P1-COMPILER-003 | contract | 全部注册后合法前向引用成功；未知 key 产生 MIX-REF-UNKNOWN。 | MIX-REF-UNKNOWN |
| `CASE-P1-TD-RULE-SYSTEM-001` | TR-P1-COMPILER-003, TR-P1-COMPILER-008 | AC-P1-COMPILER-003, AC-P1-COMPILER-008 | negative | 每个冲突均产生 MIX-REF-RULE-SYSTEM-MISMATCH。 | MIX-REF-RULE-SYSTEM-MISMATCH |
| `CASE-P1-TD-INFORMATION-OWNER-001` | TR-P1-COMPILER-003, TR-P1-COMPILER-008 | AC-P1-COMPILER-003, AC-P1-COMPILER-008 | negative | 产生 MIX-INFORMATION-OWNER；BusinessScope Information 数为 0。 | MIX-INFORMATION-OWNER |
| `CASE-P1-TD-COMMON-SUCCESS-001` | TR-P1-COMPILER-004, TR-P1-COMPILER-008 | AC-P1-COMPILER-004, AC-P1-COMPILER-008 | contract | 仅解析 system-qualified InformationKey，登记 P3 Deferred；common 无 Data/View/RuleView/ModelAccess。 | — |
| `CASE-P1-TD-INFORMATION-CROSS-SYSTEM-001` | TR-P1-COMPILER-008 | AC-P1-COMPILER-008 | negative | 产生 MIX-INFORMATION-CROSS-SYSTEM。 | MIX-INFORMATION-CROSS-SYSTEM |
| `CASE-P1-TD-COMMON-MEMBER-001` | TR-P1-COMPILER-008 | AC-P1-COMPILER-008 | negative | 逐类产生 MIX-COMMON-MEMBER。 | MIX-COMMON-MEMBER |
| `CASE-P1-TD-COMMON-QUALIFIED-001` | TR-P1-COMPILER-008 | AC-P1-COMPILER-008 | negative | 产生 MIX-COMMON-UNQUALIFIED，relatedRefs 指向问题 token。 | MIX-COMMON-UNQUALIFIED |
| `CASE-P1-TD-VIEW-BOUNDARY-001` | TR-P1-COMPILER-008, TR-P1-COMPILER-009 | AC-P1-COMPILER-008, AC-P1-COMPILER-009 | negative | 产生 MIX-REF-VIEW-NOT-DECLARED。 | MIX-REF-VIEW-NOT-DECLARED |
| `CASE-P1-TD-MODEL-ACCESS-TARGET-MAIN-001` | TR-P1-COMPILER-008, TR-P1-COMPILER-009 | AC-P1-COMPILER-008, AC-P1-COMPILER-009 | contract | selector 首先且仅命中 target-main 根目标，sourcePath 仍为 user。 | — |
| `CASE-P1-TD-MODEL-ACCESS-PATH-001` | TR-P1-COMPILER-009 | AC-P1-COMPILER-009 | contract | 区分大小写逐段命中唯一目标。 | — |
| `CASE-P1-TD-MODEL-ACCESS-NOT-FOUND-001` | TR-P1-COMPILER-009 | AC-P1-COMPILER-009 | negative | 产生 MIX-MODEL-ACCESS-NOT-FOUND。 | MIX-MODEL-ACCESS-NOT-FOUND |
| `CASE-P1-TD-MODEL-ACCESS-AMBIGUOUS-001` | TR-P1-COMPILER-009 | AC-P1-COMPILER-009 | negative | 逐种产生 MIX-MODEL-ACCESS-AMBIGUOUS，零绑定发布。 | MIX-MODEL-ACCESS-AMBIGUOUS |
| `CASE-P1-TD-MODEL-ACCESS-NON-COMPOSITE-001` | TR-P1-COMPILER-009 | AC-P1-COMPILER-009 | negative | 产生 MIX-MODEL-ACCESS-NON-COMPOSITE。 | MIX-MODEL-ACCESS-NON-COMPOSITE |
| `CASE-P1-TD-DEFERRED-COMPLETE-001` | TR-P1-COMPILER-004 | AC-P1-COMPILER-004 | contract | 完整项冻结成功；任一字段缺失产生 MIX-DEFERRED-INCOMPLETE。 | MIX-DEFERRED-INCOMPLETE |
| `CASE-P1-TD-DEFERRED-NO-RUNTIME-001` | TR-P1-COMPILER-004, TR-P1-COMPILER-008 | AC-P1-COMPILER-004, AC-P1-COMPILER-008 | boundary | Information evaluator、Action/Directory executor、Query planner、Transaction manager 调用均为 0。 | — |
| `CASE-P1-TD-PUBLISH-SUCCESS-001` | TR-P1-COMPILER-005 | AC-P1-COMPILER-005 | contract | 同一 compileAndPublish 调用内 Publisher 恰好 1 次，返回 PUBLISHED 与完整不可变 Context；返回后不再调用。 | — |
| `CASE-P1-TD-PUBLISH-BLOCKED-001` | TR-P1-COMPILER-003, TR-P1-COMPILER-005 | AC-P1-COMPILER-003, AC-P1-COMPILER-005 | negative | FAILED/MIX-PUBLICATION-BLOCKED；model/context/digest 不可取得；Publisher 0 次；旧 Context digest 不变。 | MIX-PUBLICATION-BLOCKED |
| `CASE-P1-TD-PUBLISH-TIMEOUT-001` | TR-P1-COMPILER-005 | AC-P1-COMPILER-005 | failure | FAILED/MIX-COMPILATION-TIMED-OUT；Publisher 0 次；状态机结束 FAILED。 | MIX-COMPILATION-TIMED-OUT |
| `CASE-P1-TD-PUBLISH-CANCEL-001` | TR-P1-COMPILER-005 | AC-P1-COMPILER-005 | failure | FAILED/MIX-COMPILATION-CANCELLED；Publisher 0 次；旧 Context 保持。 | MIX-COMPILATION-CANCELLED |
| `CASE-P1-TD-CONTEXT-CONSTRUCTION-001` | TR-P1-COMPILER-005 | AC-P1-COMPILER-005 | failure | FAILED/MIX-CONTEXT-CONSTRUCTION-FAILED；Publisher 0 次；无 model/context/digest。 | MIX-CONTEXT-CONSTRUCTION-FAILED |
| `CASE-P1-TD-PUBLISH-CONFLICT-001` | TR-P1-COMPILER-005 | AC-P1-COMPILER-005 | concurrency | FAILED/MIX-PUBLICATION-CONFLICT；Publisher 1 次；现有新 Context 不被覆盖。 | MIX-PUBLICATION-CONFLICT |
| `CASE-P1-TD-PUBLISH-FAILURE-001` | TR-P1-COMPILER-005 | AC-P1-COMPILER-005 | failure | FAILED/MIX-PUBLICATION-FAILURE；Publisher 1 次；旧 Context 保持。 | MIX-PUBLICATION-FAILURE |
| `CASE-P1-TD-DIGEST-001` | TR-P1-COMPILER-005 | AC-P1-COMPILER-005 | determinism | 同义输入 semanticDigest 相同，非同义不同；sourceDigest 反映原文；digest 输入不含 DigestPair 自身。 | MIX-DIGEST-NONDETERMINISTIC |
| `CASE-P1-TD-CONTEXT-ISOLATION-001` | TR-P1-COMPILER-005, TR-P1-COMPILER-006 | AC-P1-COMPILER-005, AC-P1-COMPILER-006 | concurrency | Registry/Diagnostic/digest 无交叉污染；修改拒绝并产生/映射 MIX-CONTEXT-MUTATION；无 static mutable current。 | MIX-CONTEXT-MUTATION |
| `CASE-P1-TD-PROJECTION-001` | TR-P1-COMPILER-006 | AC-P1-COMPILER-006 | compatibility | 读取与 CompiledModelSet 一致；写操作产生 MIX-PROJECTION-WRITE；不存在第二 Registry。 | MIX-PROJECTION-WRITE |
| `CASE-P1-TD-DIAGNOSTIC-CATALOG-001` | TR-P1-COMPILER-001, TR-P1-COMPILER-002, TR-P1-COMPILER-003, TR-P1-COMPILER-004, TR-P1-COMPILER-005, TR-P1-COMPILER-006, TR-P1-COMPILER-007, TR-P1-COMPILER-008, TR-P1-COMPILER-009 | AC-P1-COMPILER-001, AC-P1-COMPILER-002, AC-P1-COMPILER-003, AC-P1-COMPILER-004, AC-P1-COMPILER-005, AC-P1-COMPILER-006, AC-P1-COMPILER-007, AC-P1-COMPILER-008, AC-P1-COMPILER-009 | contract | 23 个稳定 code 均有 code/severity/SourceRef/definitionKey/relatedRefs/pass/recoveryHint；排序为 sourceId,line,column,code,entityKey,pass。 | — |
| `CASE-P1-TD-OBSERVER-TIMING-001` | TR-P1-COMPILER-005 | AC-P1-COMPILER-005 | observability | discovery/parse/每 pass/digest 各一次非负 elapsedNanos；状态转换完整；Observer 异常仅增加非 ERROR MIX-OBSERVER-FAILURE，不改变原结果/context/digest。 | MIX-OBSERVER-FAILURE |
| `CASE-P1-TD-RETIREMENT-001` | TR-P1-COMPILER-007 | AC-P1-COMPILER-007 | architecture | dec-expand-declaration、LegacyDeclarationAdapter、复制实现和第二运行时残留数为 0；否则 MIX-RETIREMENT-RESIDUE。 | MIX-RETIREMENT-RESIDUE |
| `CASE-P1-TD-JAVA8-MODULE-001` | TR-P1-COMPILER-005, TR-P1-COMPILER-007 | AC-P1-COMPILER-005, AC-P1-COMPILER-007 | architecture | Java 8 编译通过；无 record/List.of/Map.of/Optional.isEmpty/var；compiler core 无 DOM4J/SnakeYAML/SQL/MySQL/demo 生产依赖；context 不反向依赖 compiler。 | — |

## 覆盖门禁

- Case：41 个，ID 唯一；
- TR：`TR-P1-COMPILER-001`～`009` 全覆盖；
- AC：`AC-P1-COMPILER-001`～`009` 全覆盖；
- BM-R05：23 个稳定业务 Diagnostic code 全覆盖；
- DESIGN-R05：超时、取消、Context 构造失败、CAS conflict、Publisher failure、Observer failure 和未知结构均有专用 Case；
- 测试层级覆盖 unit、contract、boundary、negative、security、concurrency、compatibility、architecture；
- 每个 Case 都定义禁止副作用和有效 RED，未实现模块/依赖失败不计 RED。

## 执行批次

| 批次 | Case 范围 | 主要命令 |
|---|---|---|
| TD-B01 | SourceGraph/Frontend/Raw | `./mvnw -pl dec-core-compiler -am -Dtest=MixSourceResolverContractTest,CanonicalParityContractTest,RawDefinitionInventoryTest test` |
| TD-B02 | Symbol/Information/ModelAccess/Deferred | `./mvnw -pl dec-core-compiler -am -Dtest=TypedSymbolTableTest,InformationOwnershipPolicyTest,ModelAccessSelectorPolicyTest,DeferredDefinitionCompletenessTest test` |
| TD-B03 | Publication/Digest/Context | `./mvnw -pl dec-core-compiler,dec-core-context -am -Dtest=AtomicPublicationContractTest,SemanticDigestContractTest,EngineContextIsolationTest test` |
| TD-B04 | Diagnostic/Observer | `./mvnw -pl dec-core-compiler -am -Dtest=DiagnosticCatalogContractTest,CompilationObserverContractTest test` |
| TD-B05 | Architecture/Retirement | Java 8 compile、`dependency:tree`、repository/artifact/static scans |

批次必须串行执行；单批失败后先定位最早责任阶段，不继续以其它批次通过掩盖失败。
