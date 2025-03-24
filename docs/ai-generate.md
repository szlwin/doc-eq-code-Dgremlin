# AI配置说明

此文档主要说明如何进行配置以便于结合AI生成代码，或者从另方便说这个文档也是写给AI的，通过让AI读取这份文档，知道生成代码时需要实现那些代码。

开发环境配置
===
在自己的项目中，添加以下依赖配置信息：<br>
```
<orm-config>
   <orm-code-config>
	<!-- 使用vue3编写前端页面，主要在PC端通过浏览器访问-->
	<orm-code-info type="web" language="vue" vesrion="3"/>
	<!-- 后端服务使用java编写，jdk为1.8版本-->
	<orm-code-info type="service" language="java" vesrion="1.8"/>
    </orm-code-config>
    <orm-database-config>
        <!-- 数据库使用mysql8.1版本-->
	<orm-code-info type="mysql" vesrion="8.1"/>
    <orm-database-config>
</orm-config>
```
<br>
在以上配置文档中，<orm-code-info>说明了前端、后端分别使用Vue3和Java，而数据库使用mysql8.1,具体说明如下：
xml文档说明
<table>
  <tr>
    <td>类型</td>
    <td>语言</td>
    <td>版本</td>
  </tr>
  <tr>
    <td>
     说明使用何种技术，具体如下：
     web: 主要在PC端通过浏览器访问
     service: 后端服务，主要以提供http接口的方式给前端页面调用    
    </td>
    <td>property-info</td>
    <td>数据属性信息</td>
    <td></td>
  </tr>
</table>

初始化
===
执行dec-demo/main/resource/model/demo.sql文件中的SQL语句，初始化数据库和表。<br><br>
在使用前，需添加相关数据信息及其实现，并加载相应设计文档，以下为代码示例：
```
public void testInit() throws Exception{

    try {
        //1.添加数据源
        ConfigUtil.addDataSourceConfig("MySQL", "dec.external.datasource.sql.datasource.DBDataSource");
        //2.加载配置文件
        ConfigUtil.parseConfigInfo("classpath:model/orm-config.xml");
    } catch (XMLParseException e) {
        e.printStackTrace();
    }

    //3.添加数据源相关实现
    DataSourceManager.addConnectionFactory("MySQL", new MySQLDBConnectionFactory());
    DataSourceManager.addConvertContainerFactory("MySQL", new MySQLConvertContainerFactory());
    DataSourceManager.addDataConvertContainerFacory("MySQL", new MySQLDataConvertContainerFactory());
    DataSourceManager.addExecuteContainerFacory("MySQL", new MySQLExecuteContainerFactory());

    //4.添加数据源
    DataSourceManager.addDataSource("data1", getDataSource1());
    DataSourceManager.addDataSource("data2", getDataSource2());
}
```
在以上代码中，在'1'处，添加了一个名为'MySql'的数据源类型，并设置其对应的实现类，如有多个数据源，可添加多个。之后在'2'处加载配置文件，其配置文件中配置了设计文档的具体加载方式及文件所在位置。
然后在'3'处，对'MySql'这个数据源添加了相关扩展实现(具体可参考[6.数据源扩展](dom-datasource.md))，最后在'4'处为'data1'和'data2'数据源标识添加具体的数据源实现(此处为HikariDataSource)。<br><br>

注：在getDataSource1()和getDataSource2()方法中，将数据库连接参数修改为用户自己的数据库。<br>


orm-config.xml配置文件
===
以下配置文件在dec-demo/main/resource/model目录下
```
<orm-config>
  <!--配置数据源标识及其对应数据源类型-->
  <orm-datasource-info>
    <orm-datasource name="data1">
     <name>MySQL</name>
    </orm-datasource>
    <orm-datasource name="data2">
      <name>MySQL</name>
    </orm-datasource>		
  </orm-datasource-info>

  <!--数据设计文档路径-->
  <orm-data-file-info>
    <orm-file path="classpath:model/test-data/"/>
  </orm-data-file-info>

  <!--业务模型设计文档路径-->
  <orm-view-file-info>
    <orm-file path="classpath:model/test-view"/>
  </orm-view-file-info>

  <!--业务视图设计文档路径-->
  <orm-rule-file-info>
    <orm-file path="classpath:model/test-rule"/>
  </orm-rule-file-info>

  <!--数据链接与数据源标识对应关系-->
  <orm-connection-info>
    <orm-connection name="con1">
      <data-source-info>
        <data-source ref="data1"/>
      </data-source-info>
    </orm-connection>

   <orm-connection name="con2">
     <data-source-info>
       <data-source ref="data2"/>
         </data-source-info>
       </orm-connection>
   </orm-connection-info>
</orm-config>
```
以上文档中需要对'orm-connection-info'做下说明，其在于可对链接具体性质可进一步细化，如两个不同链接来自于同一个数据源，一个用于批量读取，另一个主要用于单个读写。
或对于Redis，不同链接对应不同的数据类型。

代码示例
===
以下代码在dec-demo/main/java/model/RuleTests.java中，相应设计文档在dec-demo/main/resource/model目录下
```
public void testOrderRule() throws Exception {

    //1.创建业务模型对象
    ModelData order = DataUtil.createViewData("OrderInfo");

    //2.创建数据对象
    BaseData userData = insertUserData();

    BaseData productData1 = createProductData("p1", 10, 20);

    BaseData productData2 = createProductData("p1", 5, 30);

    //3.添加数据到业务模型中
    DataUtil.addDataToView("userT", order, userData);

    DataUtil.addDataToView("productList", order, productData1);

    DataUtil.addDataToView("productList", order, productData2);

    order.setValue("productCount", 50);
    order.setValue("totalPrice", 350);
    order.setValue("dateTime", new Date());

    //4.创建执行容器
    ModelContainer container = ContainerManager.getCurrentModelContainer();
    //5.加载视图及业务模型，并设置数据链接
    ModelLoader loader = new ModelLoader();
    loader.load("save-Order", order, "con1").addListener(new SimpleViewListener());
    container.load(loader);

    ModelLoader loader1 = new ModelLoader();
    loader1.load("back-Order", order, "con2");
    container.load(loader1).addListener(new SimpleContainerListener());

    //6.执行
    container.execute();
}
```
以上代码在一个容器中分别执行'save-Order'及'back-Order'，且这两个业务视图在同一事务中。如需对某个业务规则设置链接，则可如下：
```
loader.addConnectionWithRule("insertOrder", "con2");
```
以上对'save-Order'中的'insertOrder'业务规则，采用'con2'链接进行写操作。<br>

在示例代码中，为容器以及"save-Order"视图分别添加了监听器(SimpleContainerListener与SimpleViewListener)，容器监听器可监听容器的开始和结束事件，而视图监听器可监听业务视图及其业务规则的开始和结束事件。

其它
===
1.个人认为，在今后的设计或架构中需摒弃事务这一概念，即在设计时从无事务角度进行开发。<br>
2.其在代码示例中，使用了自定义的数据对象(ModelData与BaseData，其主要基于key-valve结构），而不支持原生对象，是因为：<br>
a）便于代码复用和移植<br>
b) 可包含更丰富的信息，如数据之间的关系，主键等信息<br>
3.另由于此项目成立较早(实际成立于2012年，2017年上传到gitlab)，还存在一定缺陷，如不支持文档型数据，现只支持行列型数据。
