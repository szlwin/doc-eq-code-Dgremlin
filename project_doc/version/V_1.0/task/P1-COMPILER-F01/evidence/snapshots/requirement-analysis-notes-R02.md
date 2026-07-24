# P1 需求分析说明

## 1. 分析结论

P1 的核心不是增加另一套 parser，而是建立唯一、格式中立、可诊断、可重复、不可变的编译事实链。需求被拆为六个验收维度：前端等价、诊断与失败发布、强类型注册、Context 隔离、Legacy 只读兼容、P1 范围约束。

## 2. 原子规则索引

- BR-001～003：文档前端、Canonical 和 Raw AST。
- BR-004～006：固定 Compiler Pipeline、诊断聚合、失败不发布。
- BR-007～008：强类型 Key、符号表、前向引用和不可变 Registry。
- BR-009～011：Compiled AST、稳定 digest、实例级 EngineContext。
- BR-012：Legacy Config 只读迁移。
- BR-013：P2+ 声明保留且明确 deferred。
- CR-001～006：格式中立、会话隔离、发布不可变、错误契约、单一事实源和阶段边界。

## 3. 横切边界

| 维度 | 分析结论 |
|---|---|
| 权限/安全 | XML 禁止 XXE/DTD，YAML 禁止任意类型构造；P2 的业务权限不在本阶段。 |
| 事务/一致性 | 编译发布以单次 CompilationResult 为原子边界；有 ERROR 不发布。 |
| 幂等 | 相同输入、选项、版本与插件集合得到相同 digest 和诊断顺序。 |
| 并发 | CompilationSession、Builder、Collector 不共享可变状态；EngineContext 只读。 |
| 错误 | Diagnostic 是结构化契约，不能以 printStackTrace、null 或空成功代替。 |
| 审计 | SourceLocation、pass、entityKey、source/semantic digest 构成可追溯证据。 |
| 兼容 | 旧 Config 仅只读投影；不双写，不删除 declaration。 |

## 4. 关联影响

当前 XML/YAML parser、ConfigManager/ConfigFactory、RuleConfig、starter 初始化和 demo fixture 均受影响。P1 引入新主路径但保留旧读取路径；每个迁移点采用 `RETAIN_FOR_AUDIT` 或 `TRIGGER_FOLLOWUP`，不得默认级联删除。详见 `docs/_relations/dependency_impact.yaml`。

## 5. 跨模块实现

统一编译流程跨越 context、compiler、XML/YAML frontends、starter 和 demo。所有参与者、步骤、失败点和“旧 Context 保持不变”的恢复策略由 `CMI-P1-COMPILER-001` 固定。

## 6. 需求到测试方向

| AC | 正常 | 边界 | 失败 |
|---|---|---|---|
| AC-001 | XML/YAML 同义 Data/View/Rule | 属性顺序、空可选位置、多文件顺序 | 格式错误、未知属性 |
| AC-002 | 诊断顺序与无 ERROR 发布 | 多错误聚合、位置缺失 | 重复/未知/类型错，Context 为空 |
| AC-003 | 前向引用、命名空间隔离 | 同名不同 Key、跨文件 | 重复同 Key、非法类型引用 |
| AC-004 | 两 Context 隔离、digest 稳定 | 并发读取、重复编译 | parser 节点/未解析引用泄漏 |
| AC-005 | Legacy 读取 | 空 Registry、缺失项 | 写入/注册/删除明确拒绝 |
| AC-006 | 后续实体 Raw 保留 | 未支持 schemaVersion | P2+ 执行尝试明确拒绝 |

## 7. 设计输入

设计必须给出：模块依赖方向、核心类型契约、pass 顺序、发布条件、诊断排序、digest 边界、不可变策略、Legacy 迁移、XML/YAML 安全前端、测试接缝和逐步落地顺序。
