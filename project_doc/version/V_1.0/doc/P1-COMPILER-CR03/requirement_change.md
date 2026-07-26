# P1-COMPILER-CR03 需求变更说明

- 变更类型：配置映射解析规则补充
- 关联原需求：`P1-COMPILER`、`P1-COMPILER-CR02`
- 状态：进入 `REQCONF-R04@c186ce681e1e` 正式确认

## 变更内容

1. `read|write@path` 表示共享模型源路径；
2. `ref@view` 选择当前 System View；
3. `ref@property` 先精确匹配该 View 的 `target-main`；
4. target-main 未匹配时，再在该 View property 树中按路径精确查找；
5. 未匹配或歧义均产生 ERROR，不做模糊或同名猜测；
6. 删除 `root-property` 这一重复根别名。
