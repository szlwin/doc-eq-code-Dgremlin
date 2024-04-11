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
    <td></td>
    <td>view</td>
    <td>元素</td>
    <td>是</td>
    <td>业务模型</td>
    <td></td>
  </tr>
  <tr>
    <td>view</td>
    <td>name</td>
    <td>属性</td>
    <td>是</td>
    <td>模型名称</td>
    <td></td>
  </tr>
  <tr>
    <td>view</td>
    <td>target-main</td>
    <td>属性</td>
    <td>是</td>
    <td>主数据</td>
    <td>此业务模型与此主数据有关</td>
  </tr>
  <tr>
    <td>view</td>
    <td>property-info</td>
    <td>元素</td>
    <td>是</td>
    <td>业务模型属性配置</td>
    <td></td>
  </tr>
  <tr>
    <td>property-info</td>
    <td>property</td>
    <td>元素</td>
    <td>是</td>
    <td>属性信息，并与数据属性进行映射</td>
    <td>此可多层嵌套</td>
  </tr>
  <tr>
    <td>property</td>
    <td>name</td>
    <td>属性</td>
    <td>是</td>
    <td>属性名称</td>
    <td></td>
  </tr>
  <tr>
    <td>property</td>
    <td>ref-property</td>
    <td>属性</td>
    <td>否</td>
    <td>对应数据的属性</td>
    <td></td>
  </tr>
  <tr>
    <td>property</td>
    <td>data</td>
    <td>属性</td>
    <td>否</td>
    <td>此业务属性所对应数据对象</td>
    <td>此不为空时，表明此属性为对象，且relation与key属性不能为空</td>
  </tr>
  <tr>
    <td>property</td>
    <td>relation</td>
    <td>属性</td>
    <td>否</td>
    <td>与当前上层业务元素关系</td>
    <td>data属性不为空时，此为必填，其值为:<br>one-to-one:一对一<br>one-to-many:一对多</td>
  </tr>
  <tr>
    <td>property</td>
    <td>key</td>
    <td>属性</td>
    <td>否</td>
    <td>此业务对象中的唯一标识</td>
    <td>data属性不为空时，此为必填</td>
  </tr>
  <tr>
    <td>property</td>
    <td>rel-key</td>
    <td>属性</td>
    <td>否</td>
    <td>表名上层父模型中对应此对象的关联唯一标识属性</td>
    <td></td>
  </tr>
</table>

以下是dec-demo项目中的model/test-view/orm-view.xml文件
```
<orm-view-mapping>
	<view name="OrderInfo" target-main="order">
		<property-info>
			<property name="id" ref-property="id"/>
			<property name="userId" ref-property="userId"/>
			<property name="productCount" ref-property="count"/>
			<property name="totalPrice" ref-property="totalPrice"/>
			<property name="dateTime" ref-property="dateTime"/>
			<property name="userT" relation="one-to-one" data="user" key="id" rel-key="userId">
				<property name="id" ref-property="id"/>
				<property name="userName" ref-property="name"/>
				<property name="uname" ref-property="name"/>
				<property name="upassword" ref-property="password"/>
			</property>
			<property name="productList" relation="one-to-many" data="product" key="orderId" rel-key="id">
				<property name="id" ref-property="id"/>
				<property name="orderId" ref-property="orderId"/>
				<property name="productName" ref-property="name"/>
				<property name="productCount" ref-property="count"/>
				<property name="productPrice" ref-property="price"/>
			</property>
		</property-info>
	</view>
</orm-view-mapping>
```
以上业务名称为OrderInfo，其主数据为order，其有两个子对象，分别是userT(对应数据对象为user)、productList(对应数据对象为product)。
其userT的唯一标识对应OrderInfo的userId属性，productList的唯一标识orderId对应OrderInfo的id属性。
<br>
<br>

## 业务视图设计文档
