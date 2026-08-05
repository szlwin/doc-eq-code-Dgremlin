# TASK-P1-T12 I004 Testing Evidence

- Testing：`TESTING-P1-T12-R04@923129b1f20d`
- Evidence：`EVD-000903`～`EVD-000909`
- Review：`REV-000581`～`REV-000584`

## First GREEN

- Head：`2d66b11e31ca66bac10adda2adba2a625dee96c5`
- P0 Run：`30974452808` — SUCCESS
- Artifact：`8917736635`
- SHA-256：`1615dc21d9c2fe44328afa4dda242d239aaa2d4ab2f9f0393d6a2fe04fb6f18a`
- I004：8/8；Compiler：393/393；正常测试：513/513。

## Independent Review GREEN before hash hardening

- Head：`7152ebab537ec25be1807583bcf5dade3db17bfe`
- P0 Run：`30974629383` — SUCCESS
- Artifact：`8917799513`
- SHA-256：`29ce25e4650a4076aeac5a40a323f516b8a10d0f32de434beda70390464cd71a`
- I004：16/16；Compiler：401/401；正常测试：521/521。

## Clean-code validation

- Head：`923129b1f20d6bebe589231b770b5c7675b52737`
- P0 Run：`30975103715` — SUCCESS
- Artifact：`8917961744`
- SHA-256：`df328a44496836e018c4725714adece969f46e0f71a0228c337ff9cadb71a640`
- Surefire XML：94
- I004：17/17
- T12 total：83/83
- Compiler module：402/402
- 正常测试：522/522
- 全部测试记录：523
- 故意失败门禁：1 项按预期失败并被识别
- Errors / Skipped：0 / 0
- 12 模块 Reactor、Java release 8：PASSED
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Covered resource contract

- 深度预算成功/超限；
- unique containers、edges、map entries 精确边界；
- 24 层共享 DAG 线性遍历和 identity 复用；
- 操作计数 Oracle；
- 深路径不能借助 FROZEN memoization 绕过 depth；
- List/Optional、Map key/value 共享 identity；
- Set/Map 目标构建无递归 hash 放大；
- 循环、collision、未知可变对象和 iterator failure；
- 普通/final Pass 资源失败 publisher=0；
- I001～I003 全部回归合同。
