# 数据源扩展

在[5.dom示例](docs/dom-demo.md)中，展示了一个Mysql的扩展例子，本章将详细说明如何自定义数据源。
具体扩展方式可参考dec-datasource-orm-sql项目及dec-datasource-orm-mysql项目，使用方式可参考[5.dom示例](docs/dom-demo.md)。

扩展接口
===
如需扩展一个新的数据源，需实现以下五个接口：<br>
```
dec.core.datasource.connection.datasource.DataSource
dec.core.datasource.connection.DataConnection
dec.core.datasource.convert.container.ConvertContainer
dec.core.datasource.datatype.convert.DataConvertContainer
dec.core.datasource.execute.container.ExecuteContainer
```
以上除DataSource，其余四个还需实现相应的factory，用于创建实例：
```
dec.core.datasource.connection.factory.DBConectionFacory
dec.core.datasource.convert.container.factory.ConvertContainerFacory
dec.core.datasource.datatype.convert.factory.DataConvertContainerFacory
dec.core.datasource.execute.container.factory.ExecuteContainerFacory
```
以上接口都在dec-core-datasource，此为数据源的接口包，可引入此项目实现相应项目自定义数据源。

DataSource
===
此为数据源的代理，通过此接口中的方法设置物理数据源实现，并获取底层数据源链接。

DataConnection
===
此为数据源链接的代理，通过此接口中的方法打开、关闭物理数据源链接，并可发送请求及提交事务与回滚。

ConvertContainer
===
对业务规则中的命令进行转换(及rule中sql的内容)，以匹配物理数据源的命令格式。
如对数据对象进行orm转换，并生成相应的sql语句以及参数。

DataConvertContainer
===
将数据对象中的数据类型与数据源中的数据类型进行转换，如对于Mysql，需将java.util.Date与java.sql.Date互相转换，而对于Hase，java.util.Date需转换为Long类型。
如无转换需求，则只需实现一个空类即可。

ExecuteContainer
===
最后将转换后的命令及参数，由此发送命令，对实际的物理数据源进行读写操作。

