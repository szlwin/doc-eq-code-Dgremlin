# dom示例

其dec-demo为整个示例项目,代码及配置文件分别在dec-demo/main/java/model及dec-demo/main/resource/model目录下。

初始化
===
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
然后在'3'处，对'MySql'这个数据源添加了相关扩展实现(具体可参考[6.数据源扩展](docs/dom-datasource.md))，最后在'4'处为'data1'和'data2'数据源标识添加具体的数据源实现(此处为HikariDataSource)。


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
以上对'save-Order'中的'insertOrder'业务规则，采用'con2'链接进行写操作。

其它
===
个人认为，在今后的设计或架构中需摒弃事务这一概念，即在设计时从无事务角度进行开发。
另由于此项目成立较早(实际成立于2012年，2017年上传到gitlab)，还存在一定缺陷，如不支持文档型数据，现只支持行列型数据。
