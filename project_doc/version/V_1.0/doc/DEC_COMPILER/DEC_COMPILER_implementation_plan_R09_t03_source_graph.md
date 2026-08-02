# DEC Compiler Implementation Plan R09 — T03 SourceGraph

- Revision：`TP-P1-COMPILER-F01-R09@P1-T03-I001`
- Design：`DESIGN-R13@P1-T03-I001`
- Task：`TASK-P1-T03 / I001`
- Execution：`SEQUENTIAL / auto / architecture_review / git_checkpoint`
- 状态：`PASSED`

## 1. TDD RED

新增可编译 Oracle：

- `MixSourceResolverContractTest`
- `SourcePolicySecurityTest`
- `SourceGraphFailureTest`

先建立公共类型的最小可编译 contract skeleton，所有行为方法明确抛出 `AssertionError("Architecture skeleton only")`。RED 必须满足：

- Java release 8 生产和测试源码编译成功；
- 既有 Context 26 项和 Compiler 47 项保持通过；
- 新增测试失败仅来自 SourceGraph 行为未实现；
- 不允许 ClassNotFound、NoSuchMethod、语法、依赖或 fixture 缺失错误。

## 2. Architecture Skeleton

建立：

- SourcePolicy 值对象和参数校验；
- SourceEdgeType、SourceGraphEdge；
- SourceManifest、MixSourceGraph；
- SourceGraphResolutionStatus/Result/Results；
- MixSourceResolver 协作者和 immutable resolution context；
- package-private 最小声明解析器边界。

Skeleton 阶段允许工厂和不可变值对象通过，但 resolver 主算法和声明提取保持受控 RED。

## 3. Development

### 3.1 安全策略

- 在 Provider 调用前验证绝对 URI、scheme、allowedRoot、query/fragment、raw/decoded traversal 和 depth；
- 非法引用使用声明 SourceRef 产生 `MIX-SOURCE-PATH-ESCAPE`；
- Provider 访问计数不得增加。

### 3.2 Provider 结果验证

- 单文档调用 `validateSingle`；
- 文件集调用 `validateFileSet`；
- 捕获 null、RuntimeException、错误基数和非法 typed result；
- 将合同违规映射为声明位置上的 `MIX-SOURCE-POLICY`；
- 保留 Provider 已返回的稳定业务 Diagnostic。

### 3.3 图构建

- root → data/view/system/business；
- system → rule；
- 目录展开只登记 Source；
- file set 按 sourceId 排序；
- 重复 sourceId 一律阻断；
- 每次登记前检查 maxSources/maxTotalBytes；
- 深度、cycle 和重复边检查；
- 最终冻结 SourceManifest、edge 和 Diagnostic。

### 3.4 资源 Oracle

Compiler 测试通过 Maven testResources 分别挂载：

- `dec-demo/src/main/resources` → `main-fixture/`
- `dec-demo/src/test/resources` → `test-fixture/`

测试只读取 classpath fixture；生产代码不包含 `dec-demo` 路径常量，也不新增 demo 生产依赖。

## 4. 独立 Review

依次执行：

1. SpecificationReviewAgent；
2. ArchitectureReviewAgent；
3. SecurityReviewAgent；
4. CodeReviewAgent；
5. TDDReviewAgent。

任何开放 P0/P1 Finding 均重开当前 iteration，禁止进入 Completion。

## 5. Testing

标准命令：

```bash
./mvnw -pl dec-core-compiler -am \
  -Dtest=MixSourceResolverContractTest,SourcePolicySecurityTest,SourceGraphFailureTest test
./mvnw --batch-mode --no-transfer-progress clean verify
scripts/remediation/prove_test_failure_gate.sh
```

验收：

- Context 回归全绿；
- Compiler 既有测试全绿；
- T03 Oracle 全绿；
- 12 模块 Reactor 全绿；
- Java release 8；
- 故意失败阻断门禁通过；
- MySQL 仅记录 `SKIPPED_NOT_APPLICABLE`。

## 6. Completion

Completion 报告必须绑定：

- Design/Plan/TDD/Skeleton/Development/Review/Testing Revision；
- clean-code Head SHA；
- 当前最终文档化 Head 的独立 P0 Run；
- 精确 Source/Edge 数量；
- 负向安全与资源限制结果；
- 开放 P0/P1 为 0；
- `TASK-P1-T04` 未启动。

## 7. 停止条件

- 实际 fixture 声明与 10 Source / 7 Edge Oracle 不一致；
- 必须修改 T02 已冻结公共 Source API；
- 必须引入网络访问、远程重试或 compiler → demo 生产依赖；
- 必须实现 Canonical Frontend 才能完成 source discovery。
