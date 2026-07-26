# P1-COMPILER-CR02 需求变更

- 变更类型：配置契约与模型所有权变更
- 关联原需求：`P1-COMPILER`
- 状态：已进入 `REQCONF-R03@7a9c82bdc1db` 候选，待独立 Review 与 StageOutcome

## 变更内容

1. Information 从 BusinessScope 迁入 System；
2. Information 只能通过 `view-ref` 关联本 System View；
3. BusinessScope 仅引用 `{system}.{information}` 并组织 Directory/Action/Produce；
4. `model-access/read|write/ref` 显式建立共享模型路径与 System View 属性的对应关系；
5. user System 移除 `OrderInfo`，使用 `UserInfo` 和 `OrderInfo.user -> UserInfo.user` 映射。

## 影响

REQCONF-R02 后的需求分析、业务模型、设计、测试设计和实施计划均需按 R03 重新验证。
