# P1-COMPILER-CR03 ModelAccess target-main 优先解析

> Revision：P1-COMPILER-CR03@c186ce681e1e。该变更进入 `REQCONF-R04@c186ce681e1e` 同一需求确认 Revision。

## 1. 需求信息

| 项目 | 内容 |
|---|---|
| 需求编号 | P1-COMPILER-CR03 |
| 需求名称 | ModelAccess target-main 优先解析 |
| 版本目标编号 | P1-COMPILER-CR03 |
| 需求类型 | 配置映射解析规则补充 |
| 主责模块 | `dec-core-compiler` |
| 协作模块 | `dec-core-context`、XML/YAML frontend、`dec-demo`、P2 |
| 受影响角色 | 配置作者、引擎开发者、测试人员 |
| 当前状态 | REQCONF-R04 候选 |
| 对应变更需求编号 | P1-COMPILER、P1-COMPILER-CR02 |

## 2. 背景与问题

### 2.1 当前行为与证据

CR02 已建立 `<read path="user"><ref view="UserInfo" property="user"/></read>`，但未明确 `read@path`、`ref@property` 与 `view@target-main` 的不同角色及解析优先级，容易被实现为同名属性猜测或额外 `root-property` 别名。

### 2.2 需要解决的问题

1. 明确 `read|write@path` 是共享模型源路径；
2. 明确 `ref@view` 选择当前 System 已声明的目标 View；
3. 明确 `ref@property` 是目标 View 选择器，先匹配 `target-main`，失败后再查找属性路径；
4. 两步均失败或出现歧义时必须阻断发布。

## 3. 需求目标

1. `property="user"` 对 `UserInfo target-main="user"` 解析为 View 根目标；
2. 当 selector 与 target-main 不同，例如 `property="name"`，再从 View 的 property 树精确查找；
3. 删除为同一语义额外引入的 `root-property`；
4. 建立可执行的主路径、回退路径和失败路径契约测试。

## 4. 范围

### 4.1 范围内

- `model-access/read|write/ref` 的目标选择器解析顺序；
- `view@target-main` 主匹配；
- View property path 精确回退；
- 未匹配、重复/歧义和跨 System View 的 Diagnostic；
- 主、测试 fixture 与契约测试同步。

### 4.2 范围外

- 模糊匹配、大小写折叠或最相似名称匹配；
- P2 的实际读写授权执行；
- 隐式跨 System View 搜索；
- 新增第二个根别名字段。

### 4.3 约束与依赖

- `read|write@path` 与 `ref@property` 即使文本相同，也分别表示源路径和目标选择器；
- `ref@property` 必须先与目标 View 的 `target-main` 做区分大小写的完整匹配；
- target-main 未匹配时，才按点分隔 property path 在目标 View 属性树中逐段精确查找；
- 两步均失败、路径穿越非复合属性、重复 property 或多个候选均产生 ERROR；
- 不读取当前 System `view-info` 之外的 View。

## 12. 待确认事项与已确认决策

### 12.1 待确认事项

| 决策编号 | 问题 | 可选方案 | 推荐方案 | 是否阻塞 | 责任人/Agent | 状态 |
|---|---|---|---|---|---|---|
| - | 无 | - | - | 否 | RequirementConfirmationAgent | 已由用户明确 |

### 12.2 已确认决策

| 决策编号 | 结论 | 原因 | 证据/确认来源 | supersedes |
|---|---|---|---|---|
| DEC-P1-COMPILER-007 | `ref@property` 先匹配目标 View 的 `target-main`，未匹配时再精确查找 View property path | target-main 表达 View 根目标，property 回退支持一个 System 的多个 View/属性映射，同时避免额外根别名与同名猜测 | 用户当前指令、实际 mix XML | - |

## 14. 变更记录

| 文档 revision | 日期 | 阶段 | 变更内容 | 责任 Agent |
|---|---|---|---|---|
| P1-COMPILER-CR03 | 2026-07-26 | 需求确认 | 明确 ModelAccess target-main 优先、property path 回退的解析规则 | RequirementConfirmationAgent |
