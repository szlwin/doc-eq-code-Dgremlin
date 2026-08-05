# EVD-000991 — TASK-P1-T12 / I007 Rework Input

- Task：`TASK-P1-T12 / I007`
- Source Review：用户提供的独立重新 Review
- Source Revision：`a59a39fde202366742963658bf07797c9537de57`
- Production Revision：`ce8c9252325642cf45e89f71aaa1f807d4916aca`
- Base：`dev_all@3f53ea4a31b0b1366aad383f665736b0487d4d00`
- PR：`#27`
- Gate：`NEEDS_CHANGES`
- Open P0/P1/P2：`0 / 0 / 1`

## Accepted finding

`FND-P1-T12-I007-001` 已从当前源码与 R06 Architecture 独立确认：Map canonical pair 完成阶段缺少 duplicate canonical key 检查；Set 完成阶段也缺少 duplicate canonical element 复核。Finding 不是 CI/Evidence 真实性问题，而是测试矩阵与生产门禁缺失。

## Required outcome

- 两个包含相同非法 canonical collision 结构的 identity-backed Map/Set 不得比较为 true；
- 选择稳定 `CanonicalCollisionException` fail-closed 语义；
- 正常 Map/Set 和普通 hash collision 语义保持；
- 删除未使用 `ConditionalCompareTask`；
- 新增有效 RED、独立 Review 和全量 Evidence；
- R06 全部历史不可变保留。

## Skill baseline

- Tag：`common-develop-v2.44-rc8`
- Commit：`4787876e135d347e9f37580910e2d28b09ea2ba4`
- Guard：`DIRTY / HEAD_MATCHES_BASELINE / CRITICAL_FILE_DRIFT=0`
- Existing drift：11 files，`PRESERVE_AND_REVIEW_DRIFT`
