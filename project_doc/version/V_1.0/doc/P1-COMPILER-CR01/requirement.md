# P1-COMPILER-CR01 整体退役 `dec-expand-declaration`

> Revision：P1-COMPILER-CR01@0baf19e7438a。该变更已纳入 `REQCONF-R02` 同一需求确认 Revision。

## 1. 需求信息

| 项目 | 内容 |
|---|---|
| 需求编号 | P1-COMPILER-CR01 |
| 需求名称 | 整体退役 `dec-expand-declaration` |
| 版本目标编号 | P1-COMPILER-CR01 |
| 需求类型 | 范围与架构边界变更 |
| 主责模块 | `dec-core-compiler`（新增） |
| 协作模块 | 根 Reactor、`dec-demo`、P1～P8 规划 |
| 受影响角色 | 引擎开发者、测试人员、维护人员 |
| 当前状态 | 已纳入 REQCONF-R02 正式确认 |
| 对应变更需求编号 | P1-COMPILER |

## 2. 背景与问题

### 2.1 当前行为与证据

`dec-expand-declaration` 是临时项目，System 和目标核心运行时不依赖该模块；旧规划仍包含能力抽取、Adapter 和迁移任务。

### 2.2 需要解决的问题

1. 防止临时模块被错误保留或迁移为第二套运行时；
2. 让 P1～P8 统一以真实 `mix` 契约为依据。

## 3. 需求目标

1. 在 P1 整体删除临时模块、依赖和运行入口；
2. 不抽取、不迁移、不建立 Adapter；
3. 后续阶段直接依据 `mix` 重写目标能力。

## 4. 范围

### 4.1 范围内

- 删除目录、根 module、依赖、服务发现、反射字符串和发布 artifact 残留；
- 必要场景基于 `mix` 重写；
- 调整 P1～P8 阶段职责和退出门禁。

### 4.2 范围外

- 迁移旧 declaration XML/YAML；
- 复制旧 Producer/Consumer、BusinessDesc/SystemDesc 或事务执行器；
- 建立 `LegacyDeclarationAdapter`。

### 4.3 约束与依赖

- 删除通过版本控制回退，不保留运行时双轨；
- P1 的默认 Reactor 和必要测试必须在删除后通过。

## 12. 待确认事项与已确认决策

### 12.1 待确认事项

| 决策编号 | 问题 | 可选方案 | 推荐方案 | 是否阻塞 | 责任人/Agent | 状态 |
|---|---|---|---|---|---|---|
| - | 无 | - | - | 否 | RequirementConfirmationAgent | 已确认 |

### 12.2 已确认决策

| 决策编号 | 结论 | 原因 | 证据/确认来源 | supersedes |
|---|---|---|---|---|
| DEC-P1-COMPILER-005 | 整体废弃临时模块，以 `mix` 为唯一目标配置契约 | 用户明确确认，且 System 不依赖该模块 | 用户指令、decision_log.md | - |

## 14. 变更记录

| 文档 revision | 日期 | 阶段 | 变更内容 | 责任 Agent |
|---|---|---|---|---|
| P1-COMPILER-CR01 | 2026-07-26 | 需求确认 | 建立模块整体退役的正式变更需求 | RequirementConfirmationAgent |
