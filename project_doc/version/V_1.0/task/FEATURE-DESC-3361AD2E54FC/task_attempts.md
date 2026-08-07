# FEATURE-DESC-3361AD2E54FC 任务执行记录

```json task-attempts
[]
```

## 使用规则

- 一次实际执行对应一个稳定 `attempt_id`，开始时登记 `RUNNING`，完成时更新同一记录。
- 已完成记录不可删除或覆盖为另一轮执行；重试必须创建下一个连续的 `attempt_no`。
- 只保存 command/evidence ID 和摘要；完整日志、Diff、测试报告写入文件后注册到 Evidence Registry。
- 字段集合以 `assets/long-task/record-contract.json#records.taskAttempt` 为准。

- `attempt_no` 在每个 iteration 内从 1 重新计数；`iteration_id` 和 `iteration_no` 用于区分正常迭代与失败重试。
