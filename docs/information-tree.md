# 信息树说明

本文说明 `doc-eq-code-Dgremlin` 中信息树的设计目的、核心概念、配置方式和执行语义。

本文以以下示例为主要依据：

```text
dec-demo/src/main/resources/directory/order/order-directory-new.xml
```

该示例描述了用户有效性、订单状态、支付结果以及订单支付成功或失败等业务事实。

> 本文首先定义设计语言应表达的语义。示例中的部分元素属于目标设计，当前 Java 引擎不一定已经完整实现。

## 1. 信息树解决什么问题

从信息角度看，业务可以理解为信息的生产、消费、识别、组合和变化。

信息树主要回答两个问题：

1. 系统需要识别或生产哪些业务信息；
2. 每种业务信息依赖哪些数据或其他业务信息。

传统代码经常直接判断底层字段：

```java
if (user.getStatus() == 1 && user.getCertified() == 1) {
    // 有效用户
}
```

信息树将其提升为稳定的业务概念：

```text
user.effective
    = user.activated
      AND user.certified
```

调用方只依赖 `user.effective`，不需要知道激活和认证具体使用哪些字段、状态值或规则实现。

## 2. 信息树在整体模型中的位置

信息树与数据模型、业务模型、目录之间的关系如下：

```text
数据模型
    定义数据字段及其与数据源的映射
        ↓
业务模型
    按业务视角组织数据对象与关系
        ↓
信息树
    定义业务模型上成立的业务事实
        ↓
目录
    根据业务事实进行分类、查询、执行和状态迁移
```

各层职责必须保持分离：

- 数据模型不负责定义完整业务含义；
- 业务模型不负责描述完整业务流程；
- Information 不负责描述目录路径；
- Directory 不应直接使用数据库状态值替代 Information。

## 3. Information 的定义

信息树位于 `information-info` 中：

```xml
<information-info>
    <information .../>
    <information .../>
</information-info>
```

一个 `information` 表示一个具有明确、稳定业务含义的事实，例如：

- `user.activated`：用户已经激活；
- `user.effective`：用户是有效用户；
- `order.ordered`：订单数据处于已下单状态；
- `payment.success`：支付结果表示支付成功；
- `order.paySuccess`：支付结果成功，并且订单数据已经物化为支付成功状态。

Information 的名称应使用稳定的业务语言。一旦业务定义确定，不应修改原有含义；新的业务含义应新增 Information。

## 4. Information 的三种主要类型

### 4.1 通过业务规则识别的原子 Information

使用 `model-ref` 和 `rule-ref`：

```xml
<information
        name="user.activated"
        model-ref="UserInfo"
        rule-ref="isActivated"/>
```

含义如下：

- `name`：Information 的全局业务名称；
- `model-ref`：识别该事实所使用的业务模型；
- `rule-ref`：识别该事实的业务规则。

`rule-ref` 应仅根据 `model-ref` 指向的业务模型判断当前事实是否成立。

例如 `isActivated` 可以读取 `UserInfo` 中的字段，但不应直接引用其他 Information。Information 之间的组合应由 `expression` 完成。

### 4.2 通过数据表达式识别和物化的原子 Information

使用 `model-ref`、`rule-data` 和可选的 `change-data`：

```xml
<information
        name="order.ordered"
        model-ref="OrderInfo"
        rule-data="
            order.status = 1
            and
            every(orderDetails, status = 1)
        ">

    <change-data>
        <![CDATA[
            order.status = 1;
            every(orderDetails, status = 1);
        ]]>
    </change-data>

</information>
```

其中：

- `rule-data` 用于识别 Information 是否成立；
- `change-data` 用于将 Information 物化到业务模型数据；
- 两者只能访问 `model-ref` 指向的业务模型及其数据节点和字段。

本例中：

```text
识别 order.ordered：
    order.status = 1
    并且所有 orderDetails.status = 1

物化 order.ordered：
    设置 order.status = 1
    并设置所有 orderDetails.status = 1
```

`change-data` 不是必须的。如果一个 Information 只有识别方式而没有 `change-data`，则引擎只能判断它是否成立，不能直接将它自动物化。

例如：

```xml
<information
        name="order.waitPay"
        model-ref="OrderInfo"
        rule-ref="isWaitPay"/>
```

`order.waitPay` 可以被识别，但是否以及如何进入等待支付状态，由其规则实现或其他业务操作负责。

### 4.3 通过其他 Information 组合的复合 Information

使用 `expression`：

```xml
<information
        name="user.effective"
        expression="
            user.activated
            and
            user.certified
        "/>
```

复合 Information 的表达式只能引用其他 Information：

```text
允许：
    user.activated and user.certified

不允许：
    user.status = 1 and user.certified
```

复合 Information 一般不配置：

- `model-ref`；
- `rule-ref`；
- `rule-data`；
- `change-data`。

它表示已有业务事实的逻辑组合，而不是直接操作底层数据。

## 5. 识别与物化必须分离

Information 包含两个不同方向的能力：

```text
业务模型数据 ──识别──> Information
Information ──物化──> 业务模型数据
```

### 5.1 识别

识别用于回答：

```text
当前业务事实是否成立？
```

识别方式可以是：

- `rule-ref`；
- `rule-data`；
- `expression`。

### 5.2 物化

物化用于回答：

```text
如何修改业务模型数据，使该业务事实成立？
```

物化方式为原子 Information 的 `change-data`，或者由具体业务规则完成数据生产和保存。

识别与物化不能混淆。能够识别某个事实，并不表示引擎一定知道如何自动产生该事实。

例如 `payment.hasResult`：

```xml
<information
        name="payment.hasResult"
        model-ref="OrderInfo"
        rule-ref="hasPayResult"/>
```

它只能识别 `OrderInfo` 中是否已经存在 `PayResult`。真正创建 `PayResult` 的操作由目录中的 `receivePayResult` 规则负责。

## 6. 示例中的完整信息树

`order-directory-new.xml` 定义了以下 Information。

| Information | 类型 | 业务含义 | 识别或组成方式 | 可自动物化 |
|---|---|---|---|---|
| `user.activated` | 原子 | 用户已经激活 | `UserInfo.isActivated` | 否 |
| `user.certified` | 原子 | 用户已经完成认证 | `UserInfo.isCertified` | 否 |
| `user.effective` | 复合 | 用户有效 | `user.activated AND user.certified` | 否 |
| `order.ordered` | 原子 | 订单及明细处于已下单状态 | `rule-data` | 是 |
| `order.waitPay` | 原子 | 订单处于等待支付状态 | `OrderInfo.isWaitPay` | 否 |
| `order.payable` | 复合 | 订单可以支付 | `order.ordered OR order.waitPay` | 否 |
| `order.paying` | 原子 | 订单及明细处于支付中状态 | `rule-data` | 是 |
| `payment.hasResult` | 原子 | 当前订单已经存在支付结果 | `OrderInfo.hasPayResult` | 否 |
| `payment.success` | 原子 | PayResult 表示支付成功 | `OrderInfo.isPaySuccess` | 否 |
| `payment.error` | 原子 | PayResult 表示支付失败 | `OrderInfo.isPayError` | 否 |
| `payment.completed` | 复合 | 支付结果已经确定 | `payment.success OR payment.error` | 否 |
| `order.paySuccessStatus` | 原子 | 订单及明细已物化为支付成功状态 | `rule-data` | 是 |
| `order.paySuccess` | 复合 | 支付结果成功且订单状态已更新 | `payment.success AND order.paySuccessStatus` | 否 |
| `order.payErrorStatus` | 原子 | 订单及明细已物化为支付失败状态 | `rule-data` | 是 |
| `order.payError` | 复合 | 支付结果失败且订单状态已更新 | `payment.error AND order.payErrorStatus` | 否 |

## 7. 信息依赖关系

示例中的主要信息关系如下：

```text
user.activated ──┐
                 ├── AND ──> user.effective
user.certified ──┘

order.ordered ──┐
                ├── OR ───> order.payable
order.waitPay ──┘

payment.success ──┐
                  ├── OR ───> payment.completed
payment.error ────┘

payment.success ────────┐
                        ├── AND ──> order.paySuccess
order.paySuccessStatus ─┘

payment.error ──────────┐
                        ├── AND ──> order.payError
order.payErrorStatus ───┘
```

该关系说明了三个层次：

1. 原始业务数据由原子 Information 识别；
2. 原子 Information 可以组合为更稳定的业务概念；
3. 目录和其他业务只依赖抽象 Information，而不是直接依赖底层状态字段。

## 8. 支付成功与失败为什么拆成两层信息

支付成功被拆成：

```text
payment.success
    PayResult 本身表示成功

order.paySuccessStatus
    订单及订单明细已经更新为成功状态

order.paySuccess
    payment.success
    AND order.paySuccessStatus
```

这样可以区分：

- 外部支付结果已经返回成功；
- 本地订单数据已经完成成功状态物化；
- 两者都完成后，完整的订单支付成功事实才成立。

支付失败采用同样结构：

```text
payment.error
    PayResult 本身表示失败

order.payErrorStatus
    订单及订单明细已经更新为失败状态

order.payError
    payment.error
    AND order.payErrorStatus
```

这种拆分适合处理跨系统调用、异步回调、最终一致性、重试和补偿等场景。

## 9. Information 的命名原则

建议使用：

```text
业务域.业务事实
```

例如：

```text
user.activated
user.effective
order.ordered
order.paying
payment.success
```

命名应满足：

1. 使用业务语言，不使用数据库字段名；
2. 表示一个可判断真假的事实；
3. 在业务含义不变时保持稳定；
4. 不把多个不相关含义放入同一个 Information；
5. 新定义应新增名称，而不是改变旧名称含义。

不推荐：

```text
status1
checkUser
orderFlag
payResultCodeSuccess
```

推荐：

```text
user.effective
order.payable
payment.completed
```

## 10. 表达式边界

### 10.1 `rule-data`

`rule-data` 只访问业务模型数据：

```text
order.status = 3
and every(orderDetails, status = 3)
```

不允许：

```text
payment.success
and order.status = 3
```

因为 `payment.success` 是 Information 引用，应放入 `expression`。

### 10.2 `expression`

`expression` 只组合 Information：

```text
payment.success
and order.paySuccessStatus
```

不允许直接访问：

- 业务模型字段；
- 数据表字段；
- 数据源；
- Java 对象或方法；
- 目录节点。

### 10.3 `change-data`

`change-data` 只修改 `model-ref` 指向的业务模型数据，不应：

- 调用外部系统；
- 创建与当前模型无关的数据；
- 直接执行目录迁移；
- 引用其他 Information 作为赋值对象。

复杂操作、外部调用和新数据生产应由业务规则及目录 Action 完成。

## 11. Information 的计算顺序

引擎计算某个 Information 时，可按以下顺序处理：

1. 根据名称找到 Information 定义；
2. 原子 Information 加载其 `model-ref` 对应业务模型；
3. 使用 `rule-ref` 或 `rule-data` 执行识别；
4. 复合 Information 递归计算 `expression` 中引用的信息；
5. 缓存本次执行范围内的结果；
6. 检测循环依赖；
7. 返回 Information 是否成立及其证据。

伪代码如下：

```text
match(name):
    info = informationRegistry.get(name)

    if info.expression exists:
        return evaluateExpression(info.expression, match)

    model = modelContext.get(info.modelRef)

    if info.ruleRef exists:
        return ruleEngine.match(info.ruleRef, model)

    if info.ruleData exists:
        return dataExpressionEngine.match(info.ruleData, model)

    fail("Information has no recognition definition")
```

## 12. Information 的物化顺序

引擎物化原子 Information 时，可按以下顺序处理：

1. 确认 Information 存在 `change-data`；
2. 加载 `model-ref` 对应业务模型；
3. 执行 `change-data`；
4. 保存或提交变化；
5. 再次执行识别规则；
6. 只有识别成功，才认为 Information 已完成物化。

伪代码如下：

```text
materialize(name):
    info = informationRegistry.get(name)

    require info.modelRef
    require info.changeData

    model = modelContext.get(info.modelRef)
    dataExpressionEngine.change(info.changeData, model)
    modelRepository.save(model)

    require match(name)
```

复合 Information 不直接物化。如果目录引用复合 Information，目录必须显式指定需要物化的原子 Information。

例如 `success` 目录最终引用 `order.paySuccess`，但实际物化的是：

```xml
<change-info information-ref="order.paySuccessStatus"/>
```

## 13. 与 Directory 的关系

Information 描述事实，Directory 使用事实。

一个目录可以使用 Information 完成三类工作：

### 13.1 定义目录含义

```xml
<directory
        name="paying"
        information-ref="order.paying"/>
```

表示属于 `paying` 目录的数据必须满足 `order.paying`。

### 13.2 定义进入前提

```xml
<dependency-info>
    <dependency information-ref="order.payable"/>
    <dependency information-ref="user.effective"/>
</dependency-info>
```

表示进入 `paying` 前，订单必须可支付且用户必须有效。

### 13.3 定义状态物化

```xml
<change-info information-ref="order.paySuccessStatus"/>
```

表示进入目录时，应将对应原子 Information 物化到业务模型。

目录不应复制 Information 的内部判断表达式。Information 的业务定义应只有一个来源。

## 14. 与 Action 和 Produce 的关系

Information 主要描述事实，不负责所有数据生产。

例如支付结果由 Action 负责创建：

```xml
<action
        name="receivePayResult"
        ref-rule="receivePayResult">
    <produce-info>
        <produce ref="PayResult"/>
    </produce-info>
</action>
```

执行完成后，引擎再通过：

```text
payment.hasResult
payment.success
payment.error
```

识别产出数据代表的业务事实。

因此完整过程是：

```text
Action 执行业务规则
    ↓
Produce 校验必须产生的数据
    ↓
Information 识别产出数据形成的事实
    ↓
Directory 根据事实分类或继续执行
```

## 15. 编译期校验建议

加载信息树时，建议至少执行以下校验：

1. Information 名称全局唯一；
2. `expression` 引用的 Information 必须存在；
3. Information 依赖图不能存在循环；
4. `rule-ref` 引用的规则必须存在；
5. `model-ref` 引用的业务模型必须存在；
6. `rule-data` 和 `change-data` 中的数据路径必须属于 `model-ref`；
7. `rule-data` 不能引用 Information；
8. `expression` 不能引用业务模型字段；
9. 复合 Information 不能配置 `change-data`；
10. 配置 `change-data` 时必须配置 `model-ref`；
11. 同一个原子 Information 不应同时出现相互冲突的识别方式；
12. 目录引用的 `information-ref` 必须存在；
13. `change-info` 应引用可物化的原子 Information；
14. 用于互斥分类的 Information 应经过互斥性校验。

## 16. 运行期校验建议

运行时建议保留以下证据：

- Information 名称；
- 使用的业务模型及版本；
- 使用的规则或表达式；
- 参与判断的数据摘要；
- 判断结果；
- 物化前后的数据变化；
- 关联的目录和 Action；
- 执行编码或跟踪 ID。

这些信息可用于：

- 业务问题定位；
- 自动化测试；
- 审计；
- 重试和补偿；
- 设计文档与实际执行的一致性验证。

## 17. 设计原则总结

信息树应遵循以下原则：

1. 从业务事实出发，而不是从状态字段出发；
2. Information 的业务定义必须稳定、唯一、无歧义；
3. 原子 Information 负责识别业务模型数据；
4. 复合 Information 只组合其他 Information；
5. 识别与物化必须分离；
6. `rule-data` 只访问业务模型数据；
7. `expression` 只引用 Information；
8. 复杂数据生产由 Action 和业务规则负责；
9. Directory 只引用 Information，不复制其判断逻辑；
10. 设计文档应成为业务、开发、测试共同使用的精确语言。
