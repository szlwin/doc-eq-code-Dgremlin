# dom设计文档
此篇会介绍设计文档格式，目前设计文档主要采用xml格式编写。
<br>
<br>

## 数据设计文档
xml文档说明
<table>
  <tr>
    <td>父元素</td>
    <td>名称</td>
    <td>类型</td>
    <td>必填</td>
    <td>说明</td>
    <td>备注</td>
  </tr>
  <tr>
    <td>data</td>
    <td>property-info</td>
    <td>元素</td>
    <td>是</td>
    <td>数据属性信息</td>
    <td></td>
  </tr>
  <tr>
    <td>property-info</td>
    <td>property</td>
    <td>元素</td>
    <td>是</td>
    <td>数据属性</td>
    <td></td>
  </tr>
  <tr>
    <td>property</td>
    <td>name</td>
    <td>属性</td>
    <td>是</td>
    <td>数据属性名称</td>
    <td></td>
  </tr>
  <tr>
    <td>property</td>
    <td>type</td>
    <td>属性</td>
    <td>是</td>
    <td>数据属性类型</td>
    <td></td>
  </tr>
  <tr>
    <td>data</td>
    <td>table-info</td>
    <td>元素</td>
    <td>否</td>
    <td>数据源映射配置信息</td>
    <td>如无对数据源操作，可不必配置</td>
  </tr>
  <tr>
    <td>table-info</td>
    <td>table</td>
    <td>元素</td>
    <td>是</td>
    <td>数据源映射信息</td>
    <td></td>
  </tr>
  <tr>
    <td>table</td>
    <td>name</td>
    <td>属性</td>
    <td>是</td>
    <td>数据源数据映射名称</td>
    <td>类似表名</td>
  </tr>
  <tr>
    <td>table</td>
    <td>data-source</td>
    <td>属性</td>
    <td>是</td>
    <td>数据源名称</td>
    <td></td>
  </tr>
  <tr>
    <td>table</td>
    <td>key</td>
    <td>属性</td>
    <td>是</td>
    <td>唯一标识</td>
    <td>类似主键</td>
  </tr>
  <tr>
    <td>table</td>
    <td>key-type</td>
    <td>属性</td>
    <td>是</td>
    <td>唯一标识类型</td>
    <td>increment: 自增(类似自增长主键)<br>set: 自行设置值 </td>
  </tr>
  <tr>
    <td>table</td>
    <td>column</td>
    <td>元素</td>
    <td>是</td>
    <td>单字段映射信息</td>
    <td></td>
  </tr>
  <tr>
    <td>column</td>
    <td>name</td>
    <td>属性</td>
    <td>是</td>
    <td>数据源字段名称</td>
    <td>类似表字段名</td>
  </tr>
  <tr>
    <td>column</td>
    <td>ref-property</td>
    <td>属性</td>
    <td>是</td>
    <td>对应数据模型中字段</td>
    <td></td>
  </tr>
  <tr>
    <td>column</td>
    <td>type</td>
    <td>属性</td>
    <td>否</td>
    <td>数据源对应类型</td>
    <td></td>
  </tr>
</table>

以下是dec-demo项目中的model/test-data/Order.xml文件
```
<orm-data-mapping>
  <data name="order">
    <property-info>
      <property name="id" type="int"/>
      <property name="userId" type="int"/>
      <property name="count" type="int"/>
      <property name="totalPrice" type="decimal"/>
      <property name="date" type="date"/>
      <property name="dateTime" type="date"/>
    </property-info>
    <table-info>
      <table name="order_info" data-source="data1" key="o_id" key-type="increment">
        <column name="o_id" ref-property="id"/>
        <column name="o_userId" ref-property="userId"/>
        <column name="o_count" ref-property="count"/>
        <column name="o_totalPrice" ref-property="totalPrice"/>
        <column name="o_date" ref-property="dateTime" type="timestamp"/>
      </table>
      <table name="order_info" data-source="data2" key="o_id" key-type="increment">
        <column name="o_id" ref-property="id"/>
        <column name="o_userId" ref-property="userId"/>
        <column name="o_count" ref-property="count"/>
        <column name="o_totalPrice" ref-property="totalPrice"/>
        <column name="o_date" ref-property="dateTime" type="timestamp"/>
      </table>			
    </table-info>
  </data>
</orm-data-mapping>
```
以上数据名称为order，其对应两个数据源，并对应两个数据源的表和字段对应关系
<br>
<br>

## 业务模型设计文档

## 业务视图设计文档
