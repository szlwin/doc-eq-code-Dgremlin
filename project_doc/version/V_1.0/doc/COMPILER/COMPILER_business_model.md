# COMPILER 业务模型

> Revision：`BM-R13`。Base：`BM-R12`。  
> Inputs：`REQAN-P2-R01@d08612768131` + `REQAN-P2-R01+DEC-OVERLAY-20260809` + persistent decisions `DEC-P2-DIRECT-BRIDGE-AUTHORITY-001` / `DEC-P2-AC007-STAGE-BOUNDARY-001`。  
> Status：`NEEDS_EXACT_REVIEW / MACHINE_BLOCKED`。

BM-R13 保留 BM-R12 已 materialize 的 System / RuleView / ModelAccess / Guard / PolicyIndex 主语义，并补齐本轮 Review 指出的 first-class ownership/version 与 RuleView→View 关系。P1 的 Source/Canonical/Raw/TypedKey/Reference/Deferred/Diagnostic/digest/atomic publication 事实继续从 `BM-R07@7d7bf504ca9d` 继承，不在本 Revision 重定义。

## 1. P2 业务目标

P2 在同一 immutable `CompiledModelSet` / `EngineContext` 中形成：

1. 可查询、可验证、可摘要的 first-class System ownership snapshot；
2. `RuleViewKey=(SystemKey,name)`、resolved View、resolved Rule refs 的完整 RuleView compiled fact；
3. rule/change/query/model-access 共用的 canonical `ModelPath`；
4. compiler-published immutable `ModelAccessPolicyIndex`；
5. 所有 P2 protected READ/WRITE/EXECUTE 统一进入 production Bridge→Gateway→Guard seam；
6. static/runtime fail-closed、actual-target/operation one-shot capability binding；
7. System/RuleView/ownership/policy/digest 同一 atomic publication closure。

## 2. 统一语言

| ID | 术语 | 定义 |
|---|---|---|
| TERM-SYSTEM-COMPILED-IDENTITY | System compiled identity | 显式 `SystemKey` + `SystemVersionIdentity` + `SourceRef` + immutable ownership snapshot。不得由文件名、路径或加载顺序推断。 |
| TERM-SYSTEM-VERSION-IDENTITY | SystemVersionIdentity | declared version（配置存在时）+ mandatory source semantic digest + schema/compiler compatibility identity。配置未声明 version 时不得伪造业务版本，使用 empty declaredVersion + digest identity。 |
| TERM-SYSTEM-OWNERSHIP-SNAPSHOT | System ownership snapshot | 一个 System 在当前 compiled revision 中拥有的 Data/View/RuleView/Rule/Information/ModelAccessRule exact key 集合；集合不可变、确定性排序并进入 semantic digest。 |
| TERM-RULEVIEW-COMPOSITE-IDENTITY | RuleView composite identity | 唯一身份 `(SystemKey,name)`；同 System 同名非法，跨 System 同名合法。 |
| TERM-COMPILED-RULEVIEW | CompiledRuleView | `RuleViewKey + resolvedViewKey + resolvedRuleKeys + SourceRef`；所有引用编译时 exact resolve。 |
| TERM-MODEL-PATH | ModelPath | rule/change/query-contract/model-access 共用的 exact canonical path value；不同 consumer 不得各自解释。 |
| TERM-MODEL-ACCESS-POLICY-INDEX | ModelAccessPolicyIndex | compiler 从 exact rules 构造并随 Context 发布的唯一 immutable runtime policy authority。 |
| TERM-PROTECTED-ACCESS-SEAM | Protected access seam | P2 唯一受支持 production protected-operation 路径：Bridge→internal issuance→resolver→one-shot capability→Gateway→Guard→operation。 |

## 3. System first-class ownership model

<a id="ENT-COMPILED-SYSTEM"></a>
### ENT-COMPILED-SYSTEM

Identity：`SystemKey`。

Required facts：

- `systemKey`；
- `sourceRef`；
- `versionIdentity`；
- `ownedDataKeys`；
- `ownedViewKeys`；
- `ownedRuleViewKeys`；
- `ownedRuleKeys`；
- `ownedInformationKeys`；
- `ownedModelAccessRuleKeys`。

`versionIdentity` 必须至少含：

- optional declared version；
- normalized source semantic digest；
- schema/compiler compatibility identity。

如果真实配置没有 declared version，`declaredVersion` 必须为空；禁止用时间戳、文件顺序或随机值伪造版本。

<a id="AGG-SYSTEM-COMPILED-CONFIG"></a>
### AGG-SYSTEM-COMPILED-CONFIG

聚合根是当前 compiled System snapshot。所有 owned key set 在 candidate build 完成后冻结并确定性排序。

不变量：

- `INV-COMPILER-016`：同一 current candidate 中 exact SystemKey 只允许一个 System definition；source order 不影响结果。
- `INV-COMPILER-016A`：每个 owner-qualified compiled fact 必须映射到 exactly one System ownership snapshot；不存在 orphan RuleView/View/ModelAccess rule。
- `INV-COMPILER-016B`：ownership snapshot 与最终 typed registries/definitions 必须一致；不得出现 ownership index 指向不存在 key 或 registry 中存在 System-owned fact 但 snapshot 漏项。
- `INV-COMPILER-016C`：ownership key sets、version identity 与 source semantic digest 都进入 semantic digest；ownership/version 改变必须改变 semantic digest。
- `INV-COMPILER-016D`：两个 EngineContext 的 ownership snapshot 互不共享可变 collection。

<a id="SVC-SYSTEM-COMPILATION"></a>
### SVC-SYSTEM-COMPILATION

Compiler 顺序：发现所有 System source → 注册 SystemKey → 收集 owner-qualified facts → resolve refs → 构造 ownership snapshot → validate completeness → 纳入 digest-bound closure → atomic publication。

失败：duplicate System、unknown owner、ownership mismatch、orphan owned fact、invalid version identity 均产生稳定 ERROR 且 candidate publication=0。

## 4. RuleView compiled relation

<a id="VO-RULEVIEW-KEY"></a>
### VO-RULEVIEW-KEY

`RuleViewKey = (SystemKey systemKey, String localName)`。

<a id="ENT-COMPILED-RULEVIEW"></a>
### ENT-COMPILED-RULEVIEW

Required facts：

- `RuleViewKey key`；
- `ViewKey resolvedViewKey`；
- deterministic immutable `List<RuleKey> resolvedRuleKeys`；
- `SourceRef sourceRef`。

不变量：

- `INV-COMPILER-017`：RuleView System 必填；同 System 同名 duplicate ERROR；跨 System 同名隔离。
- `INV-COMPILER-017A`：`resolvedViewKey` 必须 exact resolve，并且 View ownership 必须与 RuleView owner System 一致，除非 existing explicit cross-System contract 明确允许；P2 默认不推断跨 owner View。
- `INV-COMPILER-017B`：每个 `resolvedRuleKey` 必须 exact resolve；unknown Rule/View reference stable ERROR。
- `INV-COMPILER-017C`：RuleView key、resolved View、ordered Rule refs 与 SourceRef identity 进入 semantic digest。

<a id="SVC-RULEVIEW-RESOLUTION"></a>
### SVC-RULEVIEW-RESOLUTION

新调用只接受 `system-ref + rule-ref`/完整 `RuleViewKey`。bare name 只允许 existing read-only compatibility Adapter，不能注册新 fact、不能跨 System fallback。

## 5. ModelPath shared contract

<a id="VO-MODEL-PATH"></a>
### VO-MODEL-PATH

Canonical `ModelPath` 是 value identity，不是 consumer-specific string。

`INV-COMPILER-018`：rule-data、change-data、query-contract 与 model-access 对同一 System/target/raw segments 必须产出 value-equal canonical ModelPath；case/segment/root semantics 完全一致。

禁止：

- rule parser 有 parent fallback 而 change parser exact；
- query consumer 重新按裸字符串搜索；
- model-access wildcard 在 runtime 继续模糊匹配。

`read path="*"` 必须 compile-time finite expansion 为 exact child paths 后再进入 policy rule/index。

## 6. ModelAccess authorization

<a id="VO-MODEL-ACCESS-RULE"></a>
### VO-MODEL-ACCESS-RULE

Rule identity 至少包含：System、target、canonical ModelPath、AccessOperation。READ/WRITE/EXECUTE 独立。

`INV-COMPILER-018A`：一种 operation permission 不隐含另一种；同 path 有 READ 不代表 WRITE/EXECUTE。

Classifier：

- `DIRECT_EXACT -> STATIC_BOUND -> STATIC_ALLOW`；
- `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND -> RUNTIME_GUARD_REQUIRED`；
- unsupported dynamic selector -> compile ERROR。

<a id="POL-MODEL-ACCESS-AUTHORIZATION"></a>
### POL-MODEL-ACCESS-AUTHORIZATION

所有 protected READ/WRITE/EXECUTE（含 STATIC_ALLOW）进入 Gateway→Guard。Guard 对 current Context 的 `ModelAccessPolicyIndex` 做 exact lookup；missing/invalid DENY。

Direct bridge decision 生效：caller 当前可选择 exact compiler-published ruleKey/op；consumer provenance 不进入 authorization key。但 requested op 必须与 exact rule key/rule 一致，不得 fallback/upgrade。

<a id="SVC-MODEL-ACCESS-AUTHORIZATION"></a>
### SVC-MODEL-ACCESS-AUTHORIZATION

Runtime-required branch 使用 compiler-published exact requirement/plan 验证 current frame/owner/cursor/actual target membership。target/operation 在 resolver 后绑定到 one-shot capability；Guard 通过后不能替换 target/op。

## 7. Protected-access seam 与阶段边界

<a id="INV-COMPILER-020"></a>
`INV-COMPILER-020`：P2 唯一受支持 production protected-operation seam 为 Bridge→Gateway→Guard；不存在 public capability mint、public issued-pair mint、compatibility write bypass 或 secondary permission authority。

根据 `DEC-P2-AC007-STAGE-BOUNDARY-001`：

- P2 验收 seam/visibility/dependency no-bypass；
- P3 Rule/Information、P4 change/custom-action/produce、P6 QueryPlan concrete executor integration 是 downstream acceptance obligation；
- 后续阶段必须复用 P2 seam，不得建立旁路。

## 8. Publication / digest / Context

<a id="INV-COMPILER-019"></a>
`INV-COMPILER-019`：System snapshot、CompiledRuleView、canonical ModelPath facts、ModelAccessPolicyIndex 与 semantic digest 必须来自同一个 immutable digest-bound closure。

顺序：

```text
compiled systems + ownership
 -> compiled ruleviews + resolved view/rules
 -> canonical model paths
 -> compiled access rules
 -> ModelAccessPolicyIndex.of(rules)
 -> SemanticDigestInput(same immutable facts)
 -> digest
 -> DigestBoundCompiledInput(same facts + digest)
 -> CompiledModelSet.published(...)
 -> EngineContext
```

不得在 digest 后重建 ownership/policy；failed candidate 不替换 old Context。

## 9. Diagnostic / denial contract

<a id="INV-COMPILER-021"></a>
`INV-COMPILER-021`：compile ERROR 与 runtime DENY 必须 deterministic/source-aware。

Runtime denial 对相同 current facts 重复执行必须稳定关联适用字段：

- denial code；
- SystemKey；
- optional RuleViewKey/provenance；
- AccessOperation；
- canonical ModelPath；
- SourceRef / policy source provenance；
- 不泄露 actual sensitive runtime values。

至少覆盖 `POLICY_NOT_FOUND`、stale proof、wrong plan、target substitution、Guard unavailable。

## 10. Compatibility / migration

<a id="INV-COMPILER-022"></a>
`INV-COMPILER-022`：P2 保留 surviving read-only declaration/System compatibility boundary 到 P7；不恢复 `dec-expand-declaration`，不允许 legacy adapter 写新 Registry/PolicyIndex。

## 11. Gate

BM-R13 只是一轮 content remediation candidate：

- exact BusinessModel/Requirement/Impact/CrossModule Review 未发生；
- `risk_detection.json` 仍 NOT_SCANNED；
- task_state/stage_outcomes/acceptance assertions 仍是历史 machine state；
- 不得标记 PASSED，不得进入 Implementation Plan/TDD/Development。
