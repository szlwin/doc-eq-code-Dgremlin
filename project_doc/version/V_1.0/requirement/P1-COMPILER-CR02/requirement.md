# P1-COMPILER-CR02 System-owned Information 与跨 View 映射

> Revision：P1-COMPILER-CR02@1f342f7961dc。该变更进入 `REQCONF-R03@7a9c82bdc1db` 同一需求确认 Revision。

## 1. 需求信息

| 项目 | 内容 |
|---|---|
| 需求编号 | P1-COMPILER-CR02 |
| 需求名称 | System-owned Information 与跨 View 映射 |
| 版本目标编号 | P1-COMPILER-CR02 |
| 需求类型 | 配置契约与模型所有权变更 |
| 主责模块 | `dec-core-compiler` |
| 协作模块 | `dec-core-context`、XML/YAML frontend、`dec-demo`、P2～P5 |
| 受影响角色 | 配置作者、引擎开发者、测试人员 |
| 当前状态 | REQCONF-R03 候选 |
| 对应变更需求编号 | P1-COMPILER |

## 2. 背景与问题

### 2.1 当前行为与证据

R02 在 `business/order-business.xml` 中集中定义 Information，并通过 `system-ref` 指定归属。`user` System 为在 `OrderInfo.user` 上执行用户规则，同时声明 `UserInfo` 与 `OrderInfo`，使 System View 边界与共享模型访问关系不清晰。

### 2.2 需要解决的问题

1. Information 应成为 System 能力的一部分，而不是 BusinessScope 成员；
2. Information 和 RuleView 只能直接引用所属 System 的 View；
3. 共享模型路径与一个或多个 System View 的对应关系必须显式表达，不能按名称猜测。

## 3. 需求目标

1. 将 16 个 Information 分别迁入 user、order、payment System；
2. BusinessScope 只保留 Directory、Action、Produce 和跨 System Information 引用；
3. `user` System 只声明 `UserInfo`，通过 `<read path="user"><ref view="UserInfo" property="user"/></read>` 映射 `OrderInfo.user`；
4. 为后续一个 System 多 View 映射建立可重复、可诊断的结构契约。

## 4. 范围

### 4.1 范围内

- System 内 `information-info`；
- Information 局部名称及 `{system}.{information}` 外部引用；
- Information/RuleView 的 System View 归属校验；
- `model-access/read|write/ref` 显式映射；
- 主、测试 mix fixture 与契约测试同步。

### 4.2 范围外

- P2 的实际访问授权执行；
- P3 的 Information 求值、DAG 与物化；
- BusinessScope 独立 runtime；
- 隐式同名映射或跨 System 直接 View 访问。

### 4.3 约束与依赖

- ref 的 View 必须在当前 System `view-info` 中；
- 同一共享模型路径存在多个 ref 时必须无歧义并保持声明顺序稳定；
- 旧 R02 文档、Review 和 Evidence 保留，后续阶段 revision 标记 STALE 并重新验证。

## 12. 待确认事项与已确认决策

### 12.1 待确认事项

| 决策编号 | 问题 | 可选方案 | 推荐方案 | 是否阻塞 | 责任人/Agent | 状态 |
|---|---|---|---|---|---|---|
| - | 无 | - | - | 否 | RequirementConfirmationAgent | 已由用户明确 |

### 12.2 已确认决策

| 决策编号 | 结论 | 原因 | 证据/确认来源 | supersedes |
|---|---|---|---|---|
| DEC-P1-COMPILER-006 | Information 归属 System，只关联本 System View；BusinessScope 仅编排；共享模型访问使用显式 ref 映射 | 支持 System 多 View，同时避免跨 System View 所有权混乱和隐式映射 | 用户当前指令、实际 XML 调整 | - |

## 14. 变更记录

| 文档 revision | 日期 | 阶段 | 变更内容 | 责任 Agent |
|---|---|---|---|---|
| P1-COMPILER-CR02 | 2026-07-26 | 需求确认 | 建立 System-owned Information 与跨 View 映射配置契约 | RequirementConfirmationAgent |
