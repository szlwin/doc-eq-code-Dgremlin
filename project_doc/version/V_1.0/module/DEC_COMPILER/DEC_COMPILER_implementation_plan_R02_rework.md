# P1-COMPILER-F01 实施计划 R02：TASK-P1-T01 REWORK

> Revision：`TP-P1-COMPILER-F01-R02@P1-T01-REWORK-I008`  
> 输入：`DESIGN-R06@P1-T01-REWORK-I008`  
> 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 1. 状态重开

- 保留 `TASK-P1-T01` 原 Completion 与 PR #14 作为历史事实；
- 新建 iteration `I008`，状态为 `REWORK`；
- 开放 Finding：5 个 P1；
- T02 PR #15 暂停合并；
- T03 不得启动。

## 2. 执行顺序

1. **Design Revision**：冻结 DirectoryKey、SourceManifest 发布视图、Typed Registry、聚合校验和 Projection 派生方式；
2. **TDD RED**：新增可编译、可执行的反射型合同测试，失败必须来自旧合同不满足；
3. **Architecture Skeleton**：新增中立类型和新构造边界，未实现行为显式失败；
4. **Architecture Review**：核对 context 中立性、依赖方向、无第二事实源；
5. **Development GREEN**：完成不可变实现、身份校验、稳定排序和派生逻辑；
6. **Code Review**：规格、架构、工程标准、TDD 四类独立 Review；
7. **Testing**：Context 模块测试、完整 Maven Reactor、故意失败门禁；
8. **Completion Verification**：关闭 5 个 Finding，开放 P0/P1 为 0；
9. **Git/PR**：提交独立 PR 到 `dev_all`，不包含 T02/T03 实现。

## 3. 文件范围

生产代码：

```text
dec-core-context/src/main/java/dec/core/context/CoreConfigProjection.java
dec-core-context/src/main/java/dec/core/context/EngineContext.java
dec-core-context/src/main/java/dec/core/context/model/DirectoryKey.java
dec-core-context/src/main/java/dec/core/context/model/DeferredDefinition.java
dec-core-context/src/main/java/dec/core/context/model/ImmutableDeferredRegistry.java
dec-core-context/src/main/java/dec/core/context/model/CompiledModelSet.java
dec-core-context/src/main/java/dec/core/context/model/PublishedSourceManifest.java
dec-core-context/src/main/java/dec/core/context/model/PublishedSourceDescriptor.java
dec-core-context/src/main/java/dec/core/context/model/PublishedSourceDependency.java
dec-core-context/src/main/java/dec/core/context/model/TypedDefinitionRegistries.java
```

测试代码：

```text
dec-core-context/src/test/java/dec/core/context/tdd/ContextReworkContractTest.java
dec-core-context/src/test/java/dec/core/context/tdd/ContextContractBehaviorTest.java
dec-core-context/src/test/java/dec/core/context/tdd/EngineContextApiTest.java
```

任务与 Evidence：

```text
project_doc/version/V_1.0/task/P1-COMPILER-F01/**
project_doc/version/V_1.0/tdd_p1_t01_r02_completion.json
```

## 4. TDD RED 有效性

RED 必须满足：

- 测试源编译成功；
- Context 既有测试可执行；
- 失败为 JUnit assertion failure，不是 ClassNotFound 导致测试框架错误；
- Maven Wrapper、依赖和测试选择正常；
- 至少覆盖五个追溯 Finding；
- 不允许通过删除断言或降低规格转为 GREEN。

## 5. 架构门禁

- `dec-core-context` 不依赖 `dec-core-compiler`；
- SourceManifest 发布类型不引用 compiler SourceGraph/Frontend 类型；
- Projection 不是独立可写事实源；
- 未知 TypedKey 类型不静默接受；
- ERROR model、身份错配、不同源组合均在构造边界失败；
- 不增加 static current、ThreadLocal Session 或第二 Registry 运行时。

## 6. 编码规范

- Java 8；
- `@Override` 等注解必须单独一行；
- 新增或修改的方法必须添加中文注释；
- 重要校验、排序、派生逻辑必须添加中文说明；
- 不使用返回 null 表示失败；
- 集合必须防御性复制且对外只读。

## 7. 完成条件

- 新增 REWORK 合同测试全部 GREEN；
- Context 模块与完整 Reactor 全绿；
- 5 个 P1 Finding 全部 CLOSED；
- 最终代码 Head 与 P0 Run 绑定；
- 任务状态从 REWORK 更新为 COMPLETED；
- PR Ready for review 且可合并；
- T02/T03 仍保持未合并/未启动边界。
