# DEC_COMPILER Implementation Plan R07 — TASK-P1-T02 REWORK I004

## 1. 输入 Revision

- Business Model：`BM-R05@4ecb1f8c09f4`
- Base Design：`DESIGN-R05@0b37a9b4dd48`
- API Contract：`DEC_COMPILER_api_contract.md`
- Session API Revision：`DESIGN-R10@P1-T02-REWORK-I003`
- Source/Frontend Closure Revision：`DESIGN-R11@P1-T02-REWORK-I004`
- Context Completion：`COMPLETION-P1-T01-R04@ee99223a243f`
- 被推翻 Completion：`COMPLETION-P1-T02-R03@122ffc28165f`

## 2. 执行模式

`SEQUENTIAL / auto / architecture_review / git_checkpoint`

每一阶段必须绑定当前 revision 和 Git Head；旧 I002/I003 记录只读保留。开放 P0/P1、无效 RED、测试源码编译失败或新公共签名仍需 T03 修改时立即停止。

## 3. 阶段顺序

### 3.1 Design Revision

1. 明确 `DocumentSource` 六项不可变来源事实；
2. 明确 `AllowedRoot` 的绝对 URI、规范化和路径边界；
3. 明确 `CanonicalDocumentNode` 的格式中立树结构；
4. 明确 `FrontendStatus` 与 `FrontendResult` 成功/失败不变量；
5. 明确最小 Provider → Frontend → Canonical 数据闭包；
6. 保持 T03 实现行为不在本任务范围。

### 3.2 TDD RED

新增 `CompilerSourceFrontendClosureR04Test` 并更新基础 Required Types：

- 测试使用反射冻结新公共类型和签名，使当前旧生产代码仍可 Java 8 编译；
- RED 必须精确表现为 Source 来源事实缺失、Canonical 结果缺失和最小数据流不成立；
- 既有 Context 26 项与 Compiler 20 项必须保持绿色；
- 不接受 `ClassNotFoundException` 导致测试源码无法编译、依赖失败或环境失败作为有效 RED。

### 3.3 Architecture Skeleton

建立公共类型形状：

- `AllowedRoot`；
- final `DocumentSource`；
- `CanonicalDocumentNode`；
- `FrontendStatus`；
- `FrontendResult.status/canonicalRoot/diagnostics`；
- `FrontendResults` 工厂形状。

骨架阶段允许 `FrontendResults.parsed(...)` 保留一个显式 `Architecture skeleton only` 受控 RED；其余类型、签名和既有测试必须编译并通过。

### 3.4 Development GREEN

1. 完成 AllowedRoot URI 边界判断；
2. 完成 DocumentSource 全字段校验、防御性复制和值语义；
3. 完成 CanonicalDocumentNode 集合冻结和值语义；
4. 完成 FrontendResults PARSED/FAILED 不变量和 Diagnostic 排序；
5. 更新基础测试 Provider 替身，使其返回真实 SourceResolutionResult 而不是 null；
6. 完成最小 Provider → Frontend → Canonical 行为 Oracle；
7. 确保不修改 `DocumentFrontend.parse`、`DocumentSourceProvider` 或 `CompilationRequest` 的已有公共方法签名。

### 3.5 Independent Review

- SpecComplianceReviewAgent：核对 BM-R05 四项 Source 核心事实和 Canonical 成功/失败闭包；
- ArchitectureReviewAgent：核对格式中立、无 DOM/YAML 泄漏、T03 无需修改 T02 公共 API；
- EngineeringStandardsReviewAgent：核对 Java 8、不可变性、`@Override` 单独一行和中文注释；
- TDDReviewAgent：核对有效 RED、受控 Skeleton RED 和完整 GREEN；
- TestEvidenceReviewAgent：核对 Head、Run、测试计数和 Artifact 绑定。

### 3.6 Testing

必须通过：

- Context 全量回归；
- Compiler 全量回归和新增 Source/Frontend 闭包测试；
- 完整 12 模块 `clean verify`；
- Maven Compiler `release 8`；
- 故意失败测试阻断门禁；
- MySQL 仅在数据库影响存在时执行，本任务保持 `SKIPPED_NOT_APPLICABLE`。

### 3.7 Completion Verification

Completion 只有在以下条件全部成立时通过：

- 三个 I004 P1 Finding 全部 CLOSED；
- `FND-P1-T02-I003-004` 的重新打开状态已由完整数据闭包 Oracle 关闭；
- 开放 P0/P1 为 0；
- 最终代码 Head 和文档化 Head P0 均通过；
- PR #17 更新为 I004 当前事实并恢复 Ready for review；
- 未执行 merge；
- `TASK-P1-T03` 未启动且继续阻断。

## 4. 停止条件

出现以下任一情况立即停止 Completion：

1. DocumentSource 仍需要 T03 通过文件后缀猜格式或 downcast；
2. Frontend 成功结果仍无法取得 Canonical 根节点；
3. 失败结果可携带 Canonical 候选或没有 ERROR；
4. 公共 API 暴露 DOM、YAML Node 或第三方 Parser 类型；
5. T03 接入仍需修改 T02 构造器或公共方法；
6. Context 生产代码被修改；
7. 任一 P0/P1 未关闭；
8. 最终 P0 或完整 Reactor 未通过。
