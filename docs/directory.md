# 目录说明

本文说明 `doc-eq-code-Dgremlin` 中目录的设计目的、核心概念、XML 格式、查询语义和执行语义。

本文以以下示例为主要依据：

```text
dec-demo/src/main/resources/directory/order/order-directory-new.xml
```

该示例描述了订单从已下单、支付中、支付结果，到支付成功或支付失败的业务目录结构。

> 本文首先定义设计语言应表达的语义。示例中的部分元素属于目标设计，当前 Java 引擎不一定已经完整实现。

## 1. 目录解决什么问题

传统业务代码通常使用状态值表达业务阶段：

```java
order.setStatus(2);
```

查询时也直接使用状态值：

```sql
select * from order_info where status = 2
```

这种方式存在以下问题：

- 状态值缺少稳定的业务含义；
- 流程、判断、操作和状态变化分散在代码中；
- 产品、设计、开发和测试难以使用同一种语言沟通；
- 状态含义变化后，需要修改大量代码；
- 很难统一表达路径补齐、回退、分类查询和结果契约。

目录将业务对象看作资源，并使用业务目录描述资源：

- 当前属于什么业务阶段或分类；
- 可以继续进入哪些目录；
- 进入目录前需要满足哪些业务事实；
- 进入时需要执行哪些操作；
- 操作后必须产生哪些数据；
- 进入后需要物化什么业务状态；
- 如何返回上一级目录并执行补偿操作。

调用方可以使用业务语言：

```text
execute("success")
find("PayResult")
find("ordered").start("ordered").end("success")
```

而不必直接依赖 `status = 1、2、3` 等技术细节。

## 2. 目录在整体模型中的位置

```text
业务模型
    提供业务对象及数据结构
        ↓
Information
    定义对象上成立的业务事实
        ↓
Directory
    使用业务事实定义分类、路径和进入条件
        ↓
Action / Rule
    执行具体业务操作
        ↓
Produce / Change
    校验产出并物化业务状态
```

职责划分：

- Information 回答“什么业务事实成立”；
- Directory 回答“资源位于哪里、可以去哪里”；
- Dependency 回答“进入前必须满足什么”；
- Action 回答“进入过程中执行什么”；
- Produce 回答“执行后必须产生什么”；
- Change 回答“进入后需要物化什么事实”；
- Back 回答“如何沿目录关系返回并补偿”。

## 3. 示例目录结构

```text
ordered
└── paying
    └── PayResult
        ├── success
        └── error
```

| 目录 | Information | 业务含义 |
|---|---|---|
| `ordered` | `order.ordered` | 订单及订单明细处于已下单状态 |
| `paying` | `order.paying` | 订单及订单明细处于支付中状态 |
| `PayResult` | `payment.hasResult` | 当前订单已经存在支付结果 |
| `success` | `order.paySuccess` | 支付结果成功且订单状态已经物化为成功 |
| `error` | `order.payError` | 支付结果失败且订单状态已经物化为失败 |

这里同时存在两类关系：

1. `ordered → paying → PayResult` 表示业务执行路径；
2. `PayResult → success/error` 表示支付结果的业务分类。

## 4. XML 文件整体结构

目录定义在业务配置文件的 `directory-info` 元素中。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<business-config name="order">

    <information-info>
        ...
    </information-info>

    <directory-info>
        <directory ...>
            <subdirectory-info>...</subdirectory-info>
            <dependency-info>...</dependency-info>
            <action-info>...</action-info>
            <change-info .../>
        </directory>
    </directory-info>

</business-config>
```

### 4.1 XML 基本要求

| 项目 | 要求 |
|---|---|
| XML 版本 | 推荐使用 `1.0` |
| 字符编码 | 必须使用 `UTF-8` |
| 根元素 | `business-config` |
| 目录容器 | `directory-info` |
| 目录元素 | `directory` |
| 大小写 | 元素、属性和引用名称均区分大小写 |
| 命名空间 | 当前示例未定义 XML Namespace |
| 未知元素 | 编译器应拒绝，不能静默忽略 |
| 未知属性 | 编译器应拒绝，不能静默忽略 |

### 4.2 推荐子元素顺序

`directory` 内建议使用固定顺序：

```text
subdirectory-info
    ↓
dependency-info
    ↓
action-info
    ↓
change-info
```

对应 XML：

```xml
<directory ...>
    <subdirectory-info>...</subdirectory-info>
    <dependency-info>...</dependency-info>
    <action-info>...</action-info>
    <change-info .../>
</directory>
```

每个部分都可以省略，但出现时应保持该顺序，便于 XSD 校验、解析器实现和人工阅读。

## 5. `directory-info` 元素

```xml
<directory-info>
    <directory .../>
    <directory .../>
</directory-info>
```

约束：

1. 一个 `business-config` 中最多存在一个 `directory-info`；
2. `directory-info` 中至少包含一个 `directory`；
3. 同一有效配置域中的目录名称必须唯一；
4. 每个 `rel`、`information-ref`、`model-ref` 和 `ref-rule` 必须可以解析；
5. 编译器必须在运行前构建完整目录图。

## 6. `directory` 元素格式

### 6.1 属性表

| 属性 | 必填 | 说明 |
|---|---:|---|
| `name` | 是 | 目录的唯一业务名称 |
| `information-ref` | 是 | 定义属于该目录时必须成立的 Information |
| `model-ref` | 是 | 目录操作使用的主要业务模型 |
| `is-root` | 否 | 是否为可独立执行或查询的目录树根节点，默认 `false` |

### 6.2 子元素表

| 子元素 | 数量 | 说明 |
|---|---:|---|
| `subdirectory-info` | `0..1` | 定义直接子目录及父子关系 |
| `dependency-info` | `0..1` | 定义进入目录的前置 Information |
| `action-info` | `0..1` | 定义进入目录时执行的 Action |
| `change-info` | `0..1` | 定义进入目录后需要物化的 Information |

### 6.3 基本示例

```xml
<directory
        name="ordered"
        information-ref="order.ordered"
        model-ref="OrderInfo"
        is-root="true">

    <subdirectory-info>
        <subdirectory rel="paying"/>
    </subdirectory-info>

</directory>
```

### 6.4 `name`

```xml
name="PayResult"
```

要求：

- 在有效配置域内唯一；
- 使用稳定业务语言；
- 不使用数据库状态值命名；
- 被 `subdirectory/@rel` 和执行、查询 API 引用。

### 6.5 `information-ref`

```xml
information-ref="order.paying"
```

它定义该目录对应的最终业务事实。

进入目录完成后，引擎必须验证该 Information 成立。

当目录没有显式 `change-info` 时，可以把目录自身的 `information-ref` 作为默认物化目标，但前提是该 Information 具有 `change-data`。如果该 Information 不可物化，则必须由 Action 产生所需数据。

### 6.6 `model-ref`

```xml
model-ref="OrderInfo"
```

它定义目录执行的主要业务模型上下文。

编译期应校验：

- 业务模型存在；
- 目录引用的 Information 与模型兼容；
- Action 引用的规则可以在该模型上下文中执行；
- Change 所物化的 Information 与模型兼容。

### 6.7 `is-root`

```xml
is-root="true"
```

规则：

- 一个可独立查询或执行的目录树应有且仅有一个根；
- 根目录不能拥有父目录；
- 非根目录必须可从根目录到达；
- 多棵目录树可以存在，但每棵树都必须有独立根；
- 如果允许目录被多个父目录引用，应明确它属于图结构，并执行路径歧义校验。

## 7. `subdirectory-info` 与 `subdirectory`

### 7.1 基本格式

```xml
<subdirectory-info>
    <subdirectory rel="paying"/>
</subdirectory-info>
```

### 7.2 `subdirectory` 属性表

| 属性 | 必填 | 说明 |
|---|---:|---|
| `rel` | 是 | 引用直接子目录名称 |
| `information-ref` | 否 | 用于该父子关系下的自动分类条件 |
| `any-one` | 否 | 同组子目录是否至少命中一个，默认 `false` |
| `mutual-exclusion` | 否 | 显式列出与当前子目录互斥的目录名称 |

### 7.3 执行路径关系

```xml
<directory name="ordered" ...>
    <subdirectory-info>
        <subdirectory rel="paying"/>
    </subdirectory-info>
</directory>
```

表示：

```text
ordered
    ↓
paying
```

当目标目录为 `paying`，而资源仅满足 `ordered` 时，引擎可以根据路径执行 `paying`。

### 7.4 业务分类关系

```xml
<directory name="PayResult" ...>
    <subdirectory-info>
        <subdirectory
                rel="success"
                information-ref="payment.success"
                any-one="true"/>
        <subdirectory
                rel="error"
                information-ref="payment.error"
                any-one="true"/>
    </subdirectory-info>
</directory>
```

表示：

```text
PayResult
    ├── success
    └── error
```

当 `payment.success` 成立时归入 `success`；当 `payment.error` 成立时归入 `error`。

### 7.5 `any-one`

`any-one="true"` 表示同组子目录中至少一个必须成立。

```text
matched = 所有分类 Information 成立的 any-one 子目录
require matched.size >= 1
```

对于支付结果场景，业务要求是“必须且只能命中一个”，因此还需要互斥校验：

```text
require matched.size == 1
```

### 7.6 `mutual-exclusion`

如果 DSL 显式声明互斥关系，可以写为：

```xml
<subdirectory
        rel="success"
        information-ref="payment.success"
        any-one="true"
        mutual-exclusion="error"/>

<subdirectory
        rel="error"
        information-ref="payment.error"
        any-one="true"
        mutual-exclusion="success"/>
```

`mutual-exclusion` 可使用逗号分隔多个目录：

```xml
mutual-exclusion="error,cancelled"
```

编译器应校验：

- 引用目录存在；
- 互斥关系位于同一分类范围；
- 建议互斥声明对称；
- 运行时互斥目录不能同时成立。

## 8. `dependency-info` 与 `dependency`

### 8.1 XML 格式

```xml
<dependency-info>
    <dependency information-ref="order.payable"/>
    <dependency information-ref="user.effective"/>
</dependency-info>
```

### 8.2 属性表

| 元素 | 属性 | 必填 | 说明 |
|---|---|---:|---|
| `dependency` | `information-ref` | 是 | 进入当前目录前必须成立的 Information |

多个 `dependency` 默认使用 AND：

```text
order.payable
AND
user.effective
```

任何一个依赖不成立，当前目录都不能进入。

Dependency 应引用 Information，而不是复制底层字段判断。

不推荐：

```xml
<dependency rule-data="order.status = 1 and user.status = 2"/>
```

推荐：

```xml
<dependency information-ref="order.payable"/>
<dependency information-ref="user.effective"/>
```

运行时建议在执行 Action 和 Change 之前检查 Dependency。

## 9. `action-info` 与 `action`

### 9.1 基本格式

```xml
<action-info>
    <action
            name="startPay"
            ref-rule="pay">
        <produce-info>
            <produce ref="PaymentInfo"/>
        </produce-info>
    </action>
</action-info>
```

### 9.2 `action` 属性表

| 属性 | 必填 | 说明 |
|---|---:|---|
| `name` | 是 | 当前目录内的操作名称 |
| `ref-rule` | 是 | 引用实际执行的业务规则 |

### 9.3 `action` 子元素表

| 子元素 | 数量 | 说明 |
|---|---:|---|
| `produce-info` | `0..1` | 声明 Action 成功后必须产生的数据 |

Action 表达业务操作，不应直接绑定 Java 类、方法名或数据库实现。

同一个 `action-info` 可以包含多个 Action。除非显式定义并行语义，否则应按 XML 文档顺序串行执行。

示例中的 Action：

| 目录 | Action | `ref-rule` | 作用 |
|---|---|---|---|
| `paying` | `startPay` | `pay` | 发起支付并创建 `PaymentInfo` |
| `PayResult` | `receivePayResult` | `receivePayResult` | 接收支付回调并创建 `PayResult` |
| `success` | `confirmPaySuccess` | `confirmPaySuccess` | 完成支付成功处理 |
| `error` | `recordPyaError` | `recordPyaError` | 记录支付失败信息 |

## 10. `produce-info` 与 `produce`

### 10.1 XML 格式

```xml
<produce-info>
    <produce ref="PaymentInfo"/>
    <produce ref="PaymentDetail"/>
</produce-info>
```

### 10.2 属性表

| 元素 | 属性 | 必填 | 说明 |
|---|---|---:|---|
| `produce` | `ref` | 是 | Action 成功后必须产生的数据或业务对象 |

Produce 是结果契约，不是执行方式。

```text
规则返回成功
    ≠
Action 一定成功
```

只有规则执行成功，并且全部 Produce 契约都满足，Action 才成功。

例如：

```xml
<action name="receivePayResult" ref-rule="receivePayResult">
    <produce-info>
        <produce ref="PayResult"/>
    </produce-info>
</action>
```

表示 `receivePayResult` 成功后，当前业务上下文必须存在新产生或有效更新的 `PayResult`。

运行时应校验：

- 产出对象存在；
- 类型与 `ref` 一致；
- 需要持久化的产出已经保存；
- 幂等重试时能够区分“已存在的合法结果”和“本次没有产出”。

## 11. `change-info`

### 11.1 显式 XML 格式

```xml
<change-info information-ref="order.paySuccessStatus"/>
```

### 11.2 属性表

| 属性 | 必填 | 说明 |
|---|---:|---|
| `information-ref` | 是 | 需要物化的原子 Information |

`change-info` 引用的 Information 必须：

- 存在；
- 是原子 Information；
- 配置了可执行的 `change-data`；
- 与当前目录的 `model-ref` 兼容。

### 11.3 默认 Change

当目录没有显式配置 `change-info` 时，可以使用目录自身的 `information-ref` 作为默认物化目标：

```text
directory.change-info
    = directory.information-ref
```

例如进入 `paying` 时默认物化 `order.paying`。

但该规则只在 `order.paying` 具有 `change-data` 时有效。

### 11.4 复合 Information 不能直接物化

`success` 目录对应：

```text
order.paySuccess
    = payment.success
      AND order.paySuccessStatus
```

`order.paySuccess` 是复合 Information，没有 `change-data`，因此必须显式指定：

```xml
<change-info information-ref="order.paySuccessStatus"/>
```

执行后重新计算 `order.paySuccess`，并验证最终目录 Information 成立。

## 12. `back` 元素

### 12.1 XML 位置

`back` 定义在父目录指向子目录的 `subdirectory` 关系中：

```xml
<directory name="paying" ...>
    <subdirectory-info>
        <subdirectory rel="PayResult">
            <back name="returnPaying">
                <action-info>
                    <action
                            name="resetPayResult"
                            ref-rule="resetPayResult"/>
                </action-info>
            </back>
        </subdirectory>
    </subdirectory-info>
</directory>
```

该结构已经明确：

```text
来源目录 = PayResult
目标目录 = paying
```

因此 `back` 不需要重复配置 `from` 和 `to`。

### 12.2 `back` 属性表

| 属性 | 必填 | 说明 |
|---|---:|---|
| `name` | 是 | 当前父子关系中的回退操作名称 |

### 12.3 `back` 子元素

建议允许：

| 子元素 | 数量 | 说明 |
|---|---:|---|
| `action-info` | `0..1` | 回退前执行清理或补偿动作 |
| `change-info` | `0..1` | 回退后显式物化父目录状态 |

未配置 `change-info` 时，默认物化父目录的 `information-ref`。

### 12.4 多级 Back

多级回退必须沿相邻父子关系逐级执行。

```text
C → A
```

如果路径为：

```text
A
└── B
    └── C
```

则执行顺序为：

```text
1. 执行 C → B 的 back action
2. 物化并验证 B
3. 执行 B → A 的 back action
4. 物化并验证 A
```

不能直接跳过中间目录及其补偿逻辑。

## 13. 完整 XML 示例

```xml
<?xml version="1.0" encoding="UTF-8"?>
<business-config name="order">

    <information-info>
        <!-- Information 定义见 information-tree.md -->
    </information-info>

    <directory-info>

        <directory
                name="ordered"
                information-ref="order.ordered"
                model-ref="OrderInfo"
                is-root="true">
            <subdirectory-info>
                <subdirectory rel="paying"/>
            </subdirectory-info>
        </directory>

        <directory
                name="paying"
                information-ref="order.paying"
                model-ref="OrderInfo">

            <subdirectory-info>
                <subdirectory rel="PayResult">
                    <back name="returnPaying">
                        <action-info>
                            <action
                                    name="resetPayResult"
                                    ref-rule="resetPayResult"/>
                        </action-info>
                    </back>
                </subdirectory>
            </subdirectory-info>

            <dependency-info>
                <dependency information-ref="order.payable"/>
                <dependency information-ref="user.effective"/>
            </dependency-info>

            <action-info>
                <action name="startPay" ref-rule="pay">
                    <produce-info>
                        <produce ref="PaymentInfo"/>
                    </produce-info>
                </action>
            </action-info>

        </directory>

        <directory
                name="PayResult"
                information-ref="payment.hasResult"
                model-ref="OrderInfo">

            <subdirectory-info>
                <subdirectory
                        rel="success"
                        information-ref="payment.success"
                        any-one="true"/>
                <subdirectory
                        rel="error"
                        information-ref="payment.error"
                        any-one="true"/>
            </subdirectory-info>

            <action-info>
                <action
                        name="receivePayResult"
                        ref-rule="receivePayResult">
                    <produce-info>
                        <produce ref="PayResult"/>
                    </produce-info>
                </action>
            </action-info>

        </directory>

        <directory
                name="success"
                information-ref="order.paySuccess"
                model-ref="OrderInfo">

            <dependency-info>
                <dependency information-ref="payment.success"/>
            </dependency-info>

            <action-info>
                <action
                        name="confirmPaySuccess"
                        ref-rule="confirmPaySuccess"/>
            </action-info>

            <change-info
                    information-ref="order.paySuccessStatus"/>

        </directory>

        <directory
                name="error"
                information-ref="order.payError"
                model-ref="OrderInfo">

            <dependency-info>
                <dependency information-ref="payment.error"/>
            </dependency-info>

            <action-info>
                <action
                        name="recordPyaError"
                        ref-rule="recordPyaError">
                    <produce-info>
                        <produce ref="PyaError"/>
                    </produce-info>
                </action>
            </action-info>

            <change-info
                    information-ref="order.payErrorStatus"/>

        </directory>

    </directory-info>

</business-config>
```

## 14. XML 编译期校验

### 14.1 结构校验

- 根元素必须为 `business-config`；
- `directory-info` 数量合法；
- 子元素顺序符合规范；
- 元素数量符合 `0..1`、`0..n` 约束；
- 不允许未知元素和未知属性；
- 布尔属性只能使用合法布尔值。

### 14.2 目录图校验

- `directory/@name` 唯一；
- 每个 `subdirectory/@rel` 都存在；
- 每棵目录树根节点唯一；
- 非根目录可以从根到达；
- 不存在非法循环；
- 多父目录关系不会造成不可解析的执行路径；
- Back 只能定义在有效父子关系中。

### 14.3 引用校验

- `model-ref` 指向已存在的业务模型；
- `information-ref` 指向已存在的 Information；
- `ref-rule` 指向已存在且兼容的业务规则；
- `produce/@ref` 指向已定义的数据或业务对象；
- `change-info/@information-ref` 指向可物化原子 Information；
- `mutual-exclusion` 指向同一分类范围内的目录。

### 14.4 语义校验

- `any-one` 组至少包含两个候选目录；
- 互斥目录的 Information 不允许同时成立；
- Directory 最终 Information 可被识别；
- 默认 Change 的 Information 必须可物化；
- 复合 Information 必须通过显式 Change 物化其原子组成；
- Action 的 Produce 契约可以被运行时验证；
- 多级 Back 存在完整的相邻回退路径。

## 15. 运行时执行语义

建议进入单个目录时按以下顺序执行：

```text
1. 解析当前目录与目标目录之间的路径
2. 检查当前目录是否已满足，避免重复执行
3. 计算并验证 Dependency
4. 按文档顺序执行 Action
5. 校验每个 Action 的 Produce 契约
6. 执行显式 Change；没有显式 Change 时执行默认 Change
7. 重新计算受影响的原子和复合 Information
8. 验证目标 Directory 的 information-ref 成立
9. 根据 subdirectory 分类规则归类
10. 保存执行结果和路径记录
```

任何一步失败，当前目录执行失败，后续目录不得继续执行。

## 16. 路径补齐

当调用方直接执行较深目录时，引擎不能绕过中间目录。

```text
当前：ordered
目标：PayResult
```

执行路径：

```text
ordered
    → paying
    → PayResult
```

其中每一层都必须执行：

```text
Dependency
→ Action
→ Produce 校验
→ Change
→ 最终 Information 校验
```

如果中间目录已经成立，可以根据幂等策略跳过其副作用 Action，但仍需验证状态和产出。

## 17. 查询语义

### 17.1 查询一个目录

```text
find("success")
```

查询满足 `success` 目录最终 Information 的数据。

### 17.2 查询父目录覆盖范围

```text
find("PayResult")
```

应包含：

```text
success
OR
error
```

但如果 `PayResult` 自身存在可以直接识别且尚未完成分类的数据，是否包含这些数据，应由查询模式明确规定。推荐：父目录查询包含自身及所有后代目录。

### 17.3 查询路径范围

```text
find("ordered")
    .start("ordered")
    .end("success")
```

表示查询指定业务路径范围内覆盖的数据。

### 17.4 关联业务模型

```text
find("ordered")
    .with("UserInfo")
    .start("ordered")
    .end("success")
```

`with` 应根据业务模型关系和数据源能力生成关联查询，而不是把目录 DSL 直接拼接为数据库 SQL。

## 18. 非法 XML 示例

### 18.1 引用不存在的子目录

```xml
<subdirectory rel="not-exist"/>
```

错误原因：`not-exist` 未定义。

### 18.2 目录缺少最终 Information

```xml
<directory name="paying" model-ref="OrderInfo"/>
```

错误原因：无法定义“属于 paying”的业务事实。

### 18.3 复合 Information 作为默认 Change

```xml
<directory
        name="success"
        information-ref="order.paySuccess"
        model-ref="OrderInfo"/>
```

如果没有显式 `change-info`，引擎无法直接物化复合 Information `order.paySuccess`。

### 18.4 Produce 引用未知对象

```xml
<produce-info>
    <produce ref="UnknownData"/>
</produce-info>
```

错误原因：`UnknownData` 未定义。

### 18.5 Back 跳过中间目录

不应设计允许 `C` 直接返回 `A`，同时跳过已有 `C → B`、`B → A` 回退定义的结构。多级回退必须逐级执行。

## 19. 当前实现边界

`order-directory-new.xml` 描述的是目标设计语义。当前代码中的旧目录解析器和 `DirectoryContainer` 只实现了部分能力，例如：

- 基本目录与父子关系；
- `any-one` 和部分互斥配置；
- Action、Change 的部分解析；
- 基于目录范围生成查询条件。

以下能力需要继续实现或完善：

- `information-info` 的完整解析和计算；
- `dependency-info`；
- `produce-info`；
- `information-ref` 驱动的默认 Change；
- `back` 及多级回退；
- 完整的 `execute` 路径补齐；
- 分类互斥运行时校验；
- 编译期引用、图结构和表达式校验；
- 数据源无关的查询计划。

文档描述“应该支持什么”，代码实现应以该语义为目标逐步补齐。

## 20. 设计原则

1. Directory 使用业务名称，不直接暴露状态值；
2. Directory 必须通过 `information-ref` 定义最终业务事实；
3. Dependency 只引用 Information；
4. Action 只引用业务规则，不绑定具体技术实现；
5. Produce 是 Action 的结果契约；
6. Change 只物化原子 Information；
7. 复合 Information 必须通过原子 Information 间接物化；
8. 路径执行不能绕过中间目录；
9. Back 必须沿相邻目录逐级执行；
10. 父目录查询应覆盖其业务分类范围；
11. XML 必须经过结构、引用、图和语义编译校验；
12. 当前实现能力与目标设计语义必须明确区分。
