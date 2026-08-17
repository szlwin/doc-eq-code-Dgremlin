# COMPILER P2 架构增量

> Revision：`DESIGN-P2-R01@8875f042898c`。基于 P1 `DEC_COMPILER` 已通过架构与 `BM-R07@7d7bf504ca9d`，仅描述 P2 增量。

## 1. 依赖方向

```text
dec-core-context        <- neutral immutable P2 facts + guard contracts
       ^
       |
dec-core-compiler       <- System/RuleView/ModelPath/static authorization
       ^
       |
frontends / starter     <- explicit input + assembly only

legacy XML Config path  -- compatibility read boundary only; no write into P2 registry
```

禁止 context -> compiler 反向依赖；禁止 compiler -> concrete XML parser；禁止 starter 持有全局 current Context。

## 2. 发布闭包

一个 `CompiledModelSet` 同时冻结 System、RuleView、ModelPath/access rules、remaining Deferred、Diagnostic 和 digest。P2 不引入单独的 AccessRegistry 生命周期；policy index 是同一发布闭包的只读派生/成员。

## 3. 动态权限边界

Guard contract 位于中立层，具体 runtime fact evaluator 由执行环境注入。执行模块必须先取得明确 Context，再提交 owner-qualified request。Guard DENY/故障不触发模型 mutation、事务推进或外部 IO。

## 4. 迁移架构

旧 bare-name RuleView/Config 读取与新 composite registry 并存仅是迁移读取边界，不是双写；新事实只写新不可变 Context。P7 才允许在完成调用迁移后移除旧入口。
