# DEC_COMPILER Implementation Plan R06 — TASK-P1-T02 REWORK I003

## 1. 输入 Revision

- Business Model：`BM-R05@4ecb1f8c09f4`
- Base Design：`DESIGN-R05@0b37a9b4dd48`
- API Contract：`DEC_COMPILER_api_contract.md`
- Current Rework Design：`DESIGN-R10@P1-T02-REWORK-I003`
- Final T01 Completion：`COMPLETION-P1-T01-R04@ee99223a243f`
- Superseded T02 Completion：`COMPLETION-P1-T02-R02@8847b3c7dfac`

## 2. 实施顺序

### Phase A — TDD RED

新增只使用反射的完整 API 合同测试，确保生产代码尚未调整时仍可完成 Java 8 编译。RED 只允许来自以下合同偏差：

1. Session 输入类型或完整构造器缺失；
2. Deadline 仍在 CompilationOptions 中；
3. nullable 条件发布与 PublicationResult enum；
4. CompilationResult 不是 interface 或 Published accessor 不完整。

既有 Context 测试和 I002 已正确修复的模型身份、diagnostics 身份测试必须保持通过。

### Phase B — Architecture Skeleton

1. 建立 source、canonical、timing、observer 和 publication 公共类型骨架；
2. 将 CompilationRequest 改为完整 8 参数边界；
3. 将 PublicationRequest、ContextPublisher 和 PublicationResult 改为最终类型形状；
4. 将 CompilationResult 改为 interface；
5. 更新既有测试到新签名；
6. 至少保留一个显式 `Architecture skeleton only` 行为 RED，不以编译错误充当 RED。

### Phase C — Development GREEN

1. 完成所有不可变值语义、null/非法输入校验和防御性复制；
2. 完成 Published 完整事实校验及 Failed 候选隔离；
3. 完成 Deadline、Timing、Transition 等值对象；
4. 完成 Source/Frontend 注入接缝的稳定公共方法；
5. 删除旧 nullable、deadline-in-options、abstract result API；
6. 确保无 static mutable Session 依赖。

### Phase D — Independent Review

由独立 Reviewer 分别验证：

- Spec：完整 R05/R10/API Contract；
- Architecture：无全局 Source/Frontend/Clock/Observer，无 Context → Compiler 反向依赖；
- Engineering：Java 8、中文注释、`@Override` 单独一行、不可变性；
- TDD：有效 RED、Oracle 完整性、GREEN 与回归证据；
- Completion：所有 Finding、Review、Evidence 与最终 Head 绑定。

### Phase E — Testing / Completion

1. 运行完整 P0 Build Gate；
2. 核对 Context、Compiler 与完整 Reactor 测试计数；
3. 验证故意失败测试确实阻断构建；
4. MySQL 无数据库变更时记录 `SKIPPED_NOT_APPLICABLE`；
5. 更新 handoff、resume context 和机器恢复入口；
6. 最终文档化 Head 再运行一次 P0；
7. 开放 P0/P1 为 0 后恢复 PR #17 Ready for review。

## 3. 文件范围

允许修改：

- `dec-core-compiler/**`
- 父 `pom.xml` 仅在模块合同确需调整时修改
- `project_doc/version/V_1.0/doc/DEC_COMPILER/**`
- `project_doc/version/V_1.0/task/P1-COMPILER-F01/**`
- T02 Completion 机器入口

禁止修改：

- `dec-core-context` 生产代码
- T03 SourceGraph 真实实现
- XML/YAML 真实 Frontend
- Compiler Pipeline 与运行时发布重试

## 4. Finding 映射

- `FND-P1-T02-I003-001`：CompilationRequest Session 边界不完整；
- `FND-P1-T02-I003-002`：条件发布 API 与设计不兼容；
- `FND-P1-T02-I003-003`：CompilationResult 与 Published API 不完整；
- `FND-P1-T02-I003-004`：Test Oracle 与 Completion 未验证完整设计。

## 5. 停止条件

出现以下任一情况立即停止推进并保持 PR Draft：

- 新公共签名仍需修改 T03 才能接入；
- RED 来自编译、依赖或环境故障；
- Context 回归失败；
- 开放 P0/P1 Finding；
- 生产代码引入 static mutable Session 依赖；
- 测试跳过被表述为通过。
