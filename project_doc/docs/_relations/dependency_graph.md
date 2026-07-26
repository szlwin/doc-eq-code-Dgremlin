# P1 `mix` 编译依赖图（R02 草案）

> 状态：REVIEW_REQUIRED。由实际 `mix` fixture 生成，待 requirement_analysis Review 后成为正式关系事实。

```text
orm-config
  -> datasource[data1]
  -> connection[con1] -> datasource[data1]
  -> data files -> data[user, order, orderDetail, pay, payDetail]
  -> view files -> view[UserInfo, OrderInfo]
  -> systems file
       -> system[user] -> data[user], view[UserInfo,OrderInfo], rule[user-rule]
       -> system[order] -> data[order,orderDetail], view[OrderInfo], rule[order-rule]
       -> system[payment] -> data[pay,payDetail], view[OrderInfo], rule[payment-rule]
  -> business[order-payment]
       -> information[16]
       -> directory[5]
       -> action[8]
       -> produce[4]
```

跨阶段依赖：System→P2，Information→P3，Action/Produce→P4，Directory→P5，Query→P6，资源/事务→P7。
