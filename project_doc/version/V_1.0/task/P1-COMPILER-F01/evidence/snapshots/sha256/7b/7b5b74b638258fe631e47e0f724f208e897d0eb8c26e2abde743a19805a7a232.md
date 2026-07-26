# P1 `mix` 配置契约实物清单

> 状态：P1-COMPILER-CR01 设计输入。本文基于用户提供的 `dec-demo.zip` 中 `dec-demo/src/main/resources/mix` 实际文件生成，不代表当前解析器已经支持全部元素。

## 1. 实际目录

```text
dec-demo/src/main/resources/mix/
├── orm-config.xml
├── data/
│   ├── Order.xml
│   ├── Pay.xml
│   └── User.xml
├── view/
│   └── orm-view.xml
├── system/
│   └── systems.xml
├── rule/
│   ├── order-rule.xml
│   ├── payment-rule.xml
│   └── user-rule.xml
└── business/
    └── order-business.xml
```

## 2. 装配关系

1. `orm-config.xml` 是根入口；
2. `orm-data-file-info` 发现 `mix/data/`；
3. `orm-view-file-info` 发现 `mix/view/`；
4. `system-file-info` 显式发现 `mix/system/systems.xml`；
5. `business-file-info` 显式发现 `mix/business/order-business.xml`；
6. 每个 `<system>` 再通过自己的 `rule-file-info` 发现 RuleView 文件；
7. Business 文件包含 Information、Directory、Action、Produce 和依赖/回退结构，不是独立 Maven 项目。

## 3. 当前实物规模

- Data：5 个（`user`、`order`、`orderDetail`、`pay`、`payDetail`）；
- View：2 个（`UserInfo`、`OrderInfo`）；
- System：3 个（`user`、`order`、`payment`）；
- RuleView：14 个；
- Information：16 个；
- Directory：5 个；
- Action：8 个；
- Produce：4 个；
- BusinessScope：1 个（`order-payment`）。

## 4. 文件摘要

| 文件 | 根元素 | SHA-256 |
|---|---|---|
| `business/order-business.xml` | `business-config` | `cf3309001b181b2e786d236ce8747cc1642b0aff8bdc4223bbeb422757b4757c` |
| `data/Order.xml` | `orm-data-mapping` | `7d2c4df419f2303f4d9cb8da164e0cb262311ed1b0c7405b0db693619660ad82` |
| `data/Pay.xml` | `orm-data-mapping` | `cf3527673235995bb0dc49dd7327e1c9ac45f9565d6cab5a21437c0a9e585276` |
| `data/User.xml` | `orm-data-mapping` | `7b92ee68356cd855405d85874fa867adb73f8d33a974c3a8df9955930c9ed15d` |
| `orm-config.xml` | `orm-config` | `2306bbef2f6e77a80fcabe8ebf36f7dab8bf3d87c8e7d7397d00600527e0abb9` |
| `rule/order-rule.xml` | `orm-rule-mapping` | `194bc314412e71d365fe4b85cea87c689bc3c5e3662f7bc879c7c27cec4b9941` |
| `rule/payment-rule.xml` | `orm-rule-mapping` | `6cca261d2dbf7068643c199611b29870cf3aeba42de0244293a8b59926059dc0` |
| `rule/user-rule.xml` | `orm-rule-mapping` | `f038017f1adb5bbaaf876075a758735068863c79862799f7943bfd91373d2e69` |
| `system/systems.xml` | `systems` | `52f052d20f7223ee6ab06cd1184350aa9b0944da83e9b71e1bbd2cb2c6738a1a` |
| `view/orm-view.xml` | `orm-view-mapping` | `d9a9b4b65520a9eb7b68790644f48338101ff743430e421a0e8b53e597a86bc1` |

## 5. P1 采用的事实

- `mix` 是目标配置契约的 fixture 名称，不是硬编码运行路径；
- Root、System、Business 和 Rule 文件形成“源发现图”；
- RuleView 的 System 归属同时出现在 System 的 `rule-file-info` 和 RuleView 的 `system` 属性中，P1 必须校验一致性；
- BusinessScope 是配置逻辑作用域，不生成独立模块、Context、事务器或执行器；
- `dec-expand-declaration` 不属于源发现图、不属于兼容范围，也不作为代码抽取来源。
