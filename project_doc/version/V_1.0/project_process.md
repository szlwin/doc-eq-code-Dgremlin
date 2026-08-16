# V_1.0 项目过程

## 当前生命周期摘要

| 目标 | 当前结论 | 说明 |
|---|---|---|
| P1-COMPILER-F01 | PASSED / MERGED / ARCHIVED | 历史保持原样，不迁移重写 |
| FEATURE-DESC-3361AD2E54FC (P2) | development / FORMALIZATION_REQUIRED | DEV-01～DEV-09 实际开发已完成；当前 exact-revision Evidence/Review Registry 门禁仍未闭合，禁止提前进入 code_review |
| P3—P8 | TODO | 未开始 |

## P2 当前有效内容 authority

```text
Requirement Confirmation  REQCONF-P2-R02@ef30059b327d
Requirement Analysis      REQAN-P2-R01@d08612768131
Business Model            BM-R20
Design                    DESIGN-P2-R31@685dc64b1a8bb21438440185f4a25d68d120d75f
TestDesign                TESTDESIGN-P2-R33@ac9c0aecd1bf3ebee325d88d2f1b4027d727761d
Implementation Plan       TP-FEATURE-DESC-3361AD2E54FC-R07@604f26f1641e0cf9d7d70a8ee11e90d1d1ffdf1a
TDD                       TDD-P2-R01@3f282bb4e1f6
Development closure       DEV-P2-DEV09-R09@4a82335fbdce7a56b58fd6626af0ec67a7cbebba
```

Development execution DAG 已按以下顺序完成：

```text
DEV-01 -> DEV-04 -> DEV-02 -> DEV-03 -> DEV-05 -> DEV-06 -> DEV-07 -> DEV-08 -> DEV-09
```

DEV-09 closure 已记录 `open_p0_p1=0`。历史 DEV09 exact HEAD `4a82335fbdce7a56b58fd6626af0ec67a7cbebba` 的 P0 #1825 / run `31858378409` 为 SUCCESS；PR36 reconciliation HEAD `4bb11919019f455e400c7088b74b7da413fe80b2` 的 P0 #1828 / run `31927101610` 也已 SUCCESS。旧 run 只作为其所绑定 exact revision 的 provenance，不跨 revision 冒充新的 Review。

## RC21 Runtime Re-baseline（历史基线）

P2 active runtime 已切换到 storage model 6：

```text
task.json                       static identity
task_plan.md                    remaining static execution spec
task_events.jsonl               mutable lifecycle authority
cache/*                         derived / Git ignored
evidence/legacy-runtime/...     immutable pre-RC13 provenance
```

旧 `task_state.md`、`task_attempts.md`、`stage_outcomes.md`、`review_issues.md`、`resume_context.md`、`development_tasks.md` 和旧完整 `task_plan.md` 不再留在 active task 根目录；它们以原 blob 冻结到 legacy evidence。版本级旧 `work_record.md` 同样冻结，当前 work record 从 event ledger 投影恢复。

当前正式 common-develop release contract 为 `2.44 RC21`；下文的 `RC23` 仅表示本项目在 RC21 storage model 6 上继续使用的 reconciliation 标签，不改变 Skill 的正式 release identity。

## RC23 Reconciliation（2026-08-16）

本轮只调整/检查 `project_doc` 的 lifecycle/runtime 兼容层，不修改 production code。

已确认：

1. standalone finalization 已证明当前内容 authority 为 Design R31、TestDesign R33、Implementation Plan R07；
2. 当前 `task_events.jsonl` 的 seed state 仍保存 R30/R32/R05 的 canonical collaboration-review 绑定；
3. `evidence/reviews.jsonl` 当前没有 DEV-09 exact revision 的 `development:TDDReviewAgent` canonical 结论；
4. 因此不能把 R31/R33/R07 直接写成新的 machine PASSED 并同时把 development 关闭，否则会制造 revision/review 不一致；
5. `work.md` 已启用 `review_only=true`，当前 formalization 只允许 task facts / project docs 写入；
6. 当前 closure task 的静态 Implementation Plan 输入已由历史 R05 重绑定到 R07。

初始 reconciliation event 只同步 work mode 和 fail-closed next action；不重写历史 event，也不伪造 Review。

## Development Closure Formalization Attempt R02（2026-08-16）

本轮按当前 `development:TDDReviewAgent` profile 重新检查 DEV09 R09，并重新核对 R31/R33/R07 与当前 Review Profile 的可注册性。

### DEV09 R09 独立 TDD 语义审计

审查对象：

```text
DEV-P2-DEV09-R09@4a82335fbdce7a56b58fd6626af0ec67a7cbebba
```

审计结论为 `PASSED`（语义审计，不等价于 canonical ReviewResult）：

- `RC-TDD-002`：PASSED。R09 仅把 DEV-09 fixture 收紧到真实 `Order` aggregate 路径，不增加 production capability；
- `RC-TDD-003`：NOT_APPLICABLE。R09 不是 production refactor；
- `RC-TDD-004`：NOT_APPLICABLE。R09 不是 production defect fix，不声明 bug-fix RED/GREEN；
- `RC-EVID-001`：PASSED。P0 #1825 的 head SHA 精确绑定 `4a82335f...`，`core-verify` 与 `mysql-it` 都在声明环境成功；
- `RC-EVID-002`：PASSED。已恢复两个 job 的完整 Actions 日志，并下载/校验两个 immutable artifact。

冻结 Evidence provenance：

```text
P0 run        #1825 / 31858378409 / SUCCESS
core job      94947193621
mysql job     94947193533
core artifact 9239771120 / sha256 873684133e7b2a21adcbb5a9c4308a3744bdab20ed2badafeb86504cd334c2b1
mysql artifact 9239776432 / sha256 6c47eb3fc11236e8da9e3200841c9ef50837a41fef07e8a62c22e887c12613ac
```

其中 MySQL 证据直接验证真实 business tests 和数据库最终状态；目标 WRITE value/sequence 符合预期，非业务行保持不变。详细审计记录见 `dev09_r09_tdd_review_audit_20260816_r01.json`。

### Canonical Registry 仍未闭合

本轮没有把上述语义审计伪装成 canonical PASSED。原因如下：

1. Design R31 standalone finalization 与当前 Design reviewer profile 基本覆盖一致，但尚未在 canonical v4 `evidence/evidence_index.json` + `evidence/reviews.jsonl` 中形成当前 exact-revision 注册；
2. Implementation Plan R07 同样已有 standalone PASSED reviewer 集合，但 canonical v4 exact-revision 注册仍未完成；
3. TestDesign R33 的 standalone reviewer 集合是 `TestDesignReviewAgent / ArchitectureReviewAgent / RequirementReviewAgent / DevelopAgent`，而当前 machine profile 要求 `DesignReviewAgent / RequirementReviewAgent / TDDReviewAgent / TestEvidenceReviewAgent`；二者不能通过改名或复用旧 Reviewer ID 等价替换；
4. 因此 R33 仍需要按当前 profile 对 `TESTDESIGN-P2-R33@ac9c0a...` 形成新的 exact-revision Design/TDD/TestEvidence Review/Evidence；
5. DEV09 的 TDD 语义审计虽然通过，但在 canonical v4 Registry 创建 exact-revision Evidence 与 ReviewResult 前，machine gate 仍然不能标记 PASSED。

因此本轮 formalization 最终结果为：

```text
result                  = FORMALIZATION_BLOCKED
development StageOutcome = NOT_FINALIZED
current_phase            = development
phaseAdvanced            = false
```

详细机器可读记录见 `rc23_development_formalization_20260816_r02.json`。

## 为什么 development 仍没有写成 PASSED

现在阻塞项已经从“DEV09 是否有可信测试证据”进一步收敛为“current exact-revision canonical v4 Evidence/Review 尚未全部注册”。DEV09 的 Actions 证据已经确认充分，但 `$common-develop` 的机器门禁不允许用 standalone JSON、旧 profile Reviewer 或人工状态覆盖替代 `evidence/evidence_index.json` 与 `evidence/reviews.jsonl` 的 exact-revision 记录。

因此当前机器态继续保持：

```text
development = IN_PROGRESS / FORMALIZATION_REQUIRED
review_only = true
next_action = register current exact-revision Evidence/Review -> reconcile -> close
then = PHASE_FINAL_CODE_REVIEW
```

这一步仍不涉及业务代码变更；任何无法满足的 canonical Review criterion 都必须 fail-closed，而不是为了推进阶段写成 PASSED。

## 后续环境文档

`project_env.md` / `harness_env.md` 暂不作为本轮 closure 的必改项。进入 Testing 前，再基于项目实际环境补齐 capability-based validation facts（如 fast/integration/runtime/persistence validation），避免把工具名硬编码成生命周期语义。
