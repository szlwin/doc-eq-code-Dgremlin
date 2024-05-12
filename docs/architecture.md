# dom整体架构

整个dom框架分为数据模型、数据源、业务模型、业务规则、业务视图及业务容器，具体参考下图：<br>
<br>
<br>

![image](/docs/architecture.jpg)
<br>
<br>
<br>

## 数据模型与数据源
此处对数据源定义为：可对其进行读写操作，其可是RMDB、NoSQL、MQ、文件、磁盘、或是数据库集群、第三方系统、数据中间件。
其在上层进行数据访问统一抽象，对不同数据源提供统一接口接进行接入。对业务侧完全屏蔽底层数据操作细节，并可对数据操作进行文档化，便于追踪数据流向以及技术或架构的维护与扩展。

数据源分为以下四个部分：<br>
<table>
  <tr>
    <td>类型</td>
    <td>说明</td>
    <td>备注</td>
  </tr>
  <tr>
    <td>connection</td>
    <td>用于数据源的链接和关闭</td>
    <td></td>
  </tr>
  <tr>
    <td>convert</td>
    <td>用于对命令的解析</td>
    <td>如处理orm中的字段映射转换</td>
  </tr>
  <tr>
    <td>dataConvert</td>
    <td>用于数据类型的转换</td>
    <td>如java.util.Date与java.sql.Date之间转换</td>
  </tr>
  <tr>
    <td>execute</td>
    <td>用于执行转换后的命令</td>
    <td></td>
  </tr>
</table>

对于以上四个部分具体可参考[6.数据源扩展](docs/dom-datasource.md)

数据模型为对数据源的orm映射(类似mybaits的表映射)，且一个数据模型可对应多个数据源，目前暂不支持文档式结构。<br>
关于数据模型与数据源具体可参考[4.dom设计文档](docs/design.md)、[5.dom示例](docs/dom-demo.md)
<br>
<br>

## 业务模型
此对应领域驱动模型中的业务模型，其需包含数据模型，以及数据模型之间的关系，如一对一、一对多。在不同的业务模型中，同样的两个数据模型关系可能存在不同，如在某个业务模型中为一对一，另一个模型中为一对多。同时提供业务属性与数据模型中数据属性的映射关系，如用户名称在数据模型中为userName，而在业务模型中为customerName。
<br>
<br>

## 业务规则
用于执行业务逻辑，现有以下类型：
<table>
  <tr>
    <td>类型</td>
    <td>编码</td>
    <td>说明</td>
    <td>备注</td>
  </tr>
  <tr>
    <td>数据校验</td>
    <td>check</td>
    <td>对单个数据进行多种校验</td>
    <td></td>
  </tr>
  <tr>
    <td>表达式校验</td>
    <td>checkPattern</td>
    <td>根据表达式可对多个数据进行复杂校验</td>
    <td></td>
  </tr>
  <tr>
    <td>数据获取并校验</td>
    <td>checkData</td>
    <td>对单个数据进行读取并进行多种校验</td>
    <td>此需数据源支持</td>
  </tr>
  <tr>
    <td>获取数据并进行表达式校验</td>
    <td>checkDataPattern</td>
    <td>读取单个数据并根据表达式对多个数据进行复杂校验</td>
    <td>此需数据源支持</td>
  </tr>
  <tr>
    <td>新增数据</td>
    <td>insert</td>
    <td>插入数据</td>
    <td>此需数据源支持</td>
  </tr>
  <tr>
    <td>更新数据</td>
    <td>update</td>
    <td>更新数据</td>
    <td>此需数据源支持</td>
  </tr>
  <tr>
    <td>删除数据</td>
    <td>delete</td>
    <td>删除数据</td>
    <td>此需数据源支持</td>
  </tr>
  <tr>
    <td>读取单个数据</td>
    <td>get</td>
    <td>读取单个数据</td>
    <td>此需数据源支持</td>
  </tr>
  <tr>
    <td>读取多个数据</td>
    <td>query</td>
    <td>读取多个数据</td>
    <td>此需数据源支持</td>
  </tr>
  <tr>
    <td>自定义语言</td>
    <td>dsl</td>
    <td>可进行赋值、计算、判断及校验逻辑</td>
    <td>具体语法可参考我另一开源项目(https://github.com/szlwin/express-check/edit/master/README.md)</td>
  </tr>
</table>
<br>
<br>

## 业务视图
业务视图由一个业务模型与多个业务规则组成，此对应实际业务中的某项功能，如订购、支付。
<br>
<br>

## 业务容器
此为业务的运行态，在容器中指定业务视图及数据源后(可给每个业务规则制定数据源)，其根据相应业务逻辑与数据源，自动对数据进行读写操作。<br>
且一个容器可加载多个业务视图进行执行，容器中如某个数据读写失败，则会跨数据源、跨业务视图进行回滚。另可为容器中的每个业务视图添加监听器。<br>
