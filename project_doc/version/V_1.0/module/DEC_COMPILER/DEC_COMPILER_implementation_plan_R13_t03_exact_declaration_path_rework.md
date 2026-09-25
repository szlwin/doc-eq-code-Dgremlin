# DEC_COMPILER Implementation Plan R13 — T03 完整声明路径

## Revision

- Revision：`TP-P1-COMPILER-F01-R13@P1-T03-REWORK-I005`
- Design：`DESIGN-R17@P1-T03-REWORK-I005`
- Task：`TASK-P1-T03 / I005`

## 执行顺序

1. 新增 I005 负向与隔离 Oracle，保持生产代码不变形成有效 RED；
2. Oracle 覆盖错误 root、正确 root 下错误嵌套、合法声明旁 ignored subtree、错误 systems root、错误 system 路径；
3. 修改 `SourceDeclarationParser`：
   - 冻结首个根元素；
   - 使用完整路径精确匹配；
   - root 元素分别限定为 `orm-config` 与 `systems`；
   - systems 无合法 rule 声明时受控失败；
4. 运行 Architecture Skeleton P0；
5. 完成注释、命名和边界收敛，运行 clean-code GREEN；
6. 执行 Specification、Architecture、Security、Code、TDD 五类独立 Review；
7. 固化 Testing、Completion、handoff、resume 和机器恢复记录；
8. 对最终文档化 Head 再运行 P0，更新 PR #18 并恢复 Ready for review。

## Oracle 断言

- wrong-root：`FAILED / MIX_SOURCE_POLICY / graph empty / provider access = 1`；
- wrong-nesting：`FAILED / MIX_SOURCE_POLICY / graph empty / provider access = 1`；
- ignored-subtree：合法直接声明仍成功，10 Source / 7 Edge / provider access = 8；
- wrong-systems-root：`FAILED / MIX_SOURCE_POLICY / graph empty / provider access = 4`；
- wrong-system-path：`FAILED / MIX_SOURCE_POLICY / graph empty / provider access = 4`；
- 错误声明目标不得被 Provider 访问；
- 所有 `@Override` 独占一行；
- 方法、构造器和重要路径/安全逻辑使用中文注释。

## 停止条件

- 新 Oracle 未先形成有效 RED；
- 开放 P0/P1；
- Context 或既有 Compiler 回归；
- Java 8、Reactor、失败阻断失败；
- 修改 Context 生产代码或提前实现 T04；
- PR Head 漂移且未重新验证。
