# DEC_COMPILER 实施计划 R03：T01 I009 REWORK

> Revision：`TP-P1-COMPILER-F01-R03@P1-T01-REWORK-I009`  
> 输入：`DESIGN-R07@P1-T01-REWORK-I009`  
> 任务：`TASK-P1-T01`

## 1. 执行模式

- 顺序：`SEQUENTIAL`
- 模式：`auto / architecture_review / git_checkpoint`
- 工作分支：PR #16 当前 Head 分支
- 原 R01、R02 Revision、Review、Evidence 与 Completion 全部保留，不覆盖。

## 2. 阶段顺序

1. **TDD R03 RED**
   - 通过反射冻结专用异常和四个兼容写入口，避免以测试编译失败冒充 RED；
   - 直接构造来源错配的 `PublishedSourceDependency`，要求运行期拒绝；
   - RED 必须由合同缺失或行为未实现导致，现有 I008 测试保持全绿。
2. **Architecture Skeleton R03**
   - 建立 `ProjectionWriteRejectedException` 类型与兼容写入口签名；
   - 所有未完成实现显式失败，不返回伪成功；
   - 冻结 Source dependency 双层校验位置。
3. **Development R03 GREEN**
   - 实现专用异常、统一拒绝逻辑和不可变 Diagnostic；
   - 实现 dependency 声明来源一致性；
   - 补齐普通边、synthetic root、模型不变性负向测试。
4. **Code Review R03**
   - Spec、Architecture、Engineering Standards、TDD 四类独立 Review；
   - 检查 `@Override` 独占一行；新增和修改的方法、关键逻辑使用中文注释。
5. **Testing R03**
   - 运行 P0 Build Gate、完整 Maven Reactor、Java 8、故意失败门禁；
   - MySQL 无数据库影响时只能标记不适用，不能标记通过。
6. **Completion Verification R03**
   - 关闭本轮两个 P1 Finding；
   - 开放 P0/P1 必须为 0；
   - 写入 Evidence、handoff、resume context 和机器恢复入口；
   - 最终 P0 通过后解除 PR #16 Draft。

## 3. 允许修改范围

```text
dec-core-context/src/main/java/dec/core/context/CoreConfigProjection.java
dec-core-context/src/main/java/dec/core/context/ProjectionWriteRejectedException.java
dec-core-context/src/main/java/dec/core/context/model/PublishedSourceDependency.java
dec-core-context/src/main/java/dec/core/context/model/PublishedSourceManifest.java
dec-core-context/src/test/java/dec/core/context/tdd/**
project_doc/version/V_1.0/doc/DEC_COMPILER/**R07/R03**
project_doc/version/V_1.0/task/P1-COMPILER-F01/**
project_doc/version/V_1.0/tdd_p1_t01_r03_completion.json
```

## 4. 停止条件

出现以下任一情况必须停止推进并登记 Finding：

- 需要恢复可成功写入的旧 Registry；
- 需要修改 T02/T03 Compiler 行为；
- 专用异常无法稳定携带 `MIX-PROJECTION-WRITE`；
- synthetic root 身份规则与 SourceGraph 设计冲突；
- 新增开放 P0/P1；
- P0 Build Gate 或适用测试失败。

## 5. 验收

- 四个兼容写入口全部稳定拒绝并携带相同错误码；
- 任一拒绝不改变 ModelSet、Projection 或 Registry；
- dependency 声明 SourceRef 与 fromSourceId 永远一致；
- 普通边和 synthetic root edge 均有正负向测试；
- I008 已关闭的五个 Finding 无回归；
- PR #16 最终 Ready for review，未自动合并。
