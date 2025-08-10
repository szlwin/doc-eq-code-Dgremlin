# AI配置说明

此文档主要说明如何进行配置以便于结合AI生成代码，或者从另方便说这个文档也是写给AI的，通过让AI读取这份文档，知道生成代码时需要实现那些代码。

编码配置
===
在自己的项目中，添加以下依赖配置信息：<br>
```
<orm-config>
	<orm-code-config>
		<!-- 使用vue3编写前端页面，主要在PC端通过浏览器访问-->
		<orm-code-info type="web" language="vue" vesrion="3" file-path="/project/web" isCreate="false" >
			<!--如isCreate为true，则为新建模块-->
			<module name="order" desc="订单模块" isCreate="true" isFinish="false">
				<function name="查询订单信息" desc="每个订单可以点击付款按钮进行支付" isCreate="true" isFinish="false" url="" file=""/>
				<function name="订单付款" desc="采用微信付款" isCreate="true" isFinish="false" url="" file=""/>
			</module>
		</orm-code-info>
		<!-- 后端使用java编写，jdk为1.8版本-->
		<orm-code-info type="backend" language="java" vesrion="1.8" file-path="/project/web" isCreate="false">
			<!--如isCreate为true，则为新建模块-->
			<module name="order" desc="订单模块" isCreate="true" isFinish="false">
				<function name="查询订单信息" desc="" isCreate="true" isFinish="false" file="" url=""/>
				<function name="订单付款" desc="采用微信付款" isCreate="true" isFinish="false" file="" url=""/>
			</module>
		</orm-code-info>
	</orm-code-config>

	<orm-database-config>
		<!-- 数据库使用mysql8.1版本-->
		<orm-database-info type="mysql" vesrion="8.1"/>
	</orm-database-config>
</orm-config>
```
<br>
在以上配置文档中，说明了前端、后端分别使用Vue3和Java，而数据库使用mysql8.1，需在原有前端和后端项目中，新开发订单模块。其前端需开发相应的查询和付款页面，而后端需开发相应的接口，其xml文档具体如下说明<>：
'orm-code-info'元素说明:<br>
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
    <td>orm-code-config</td>
    <td>orm-code-info</td>
    <td>元素</td>
    <td>是</td>
    <td>AI生成代码说明</td>
    <td></td>
  </tr>
  <tr>
    <td>orm-code-info</td>
    <td>type</td>
    <td>属性</td>
    <td>是</td>
    <td>
     说明生成的项目类型
    </td>
    <td>
     web: 前端项目，主要在PC端通过浏览器访问<br>
     service: 后端服务，主要以提供http接口的方式给前端页面调用    
    </td>
  </tr>
  <tr>
    <td>orm-code-info</td>
    <td>language</td>
    <td>属性</td>
    <td>是</td>
    <td>
     使用何种语言编写
    </td>
    <td>
        如:java,vue,.net,c,c++,go,python等 
    </td>
  </tr>
  <tr>
    <td>orm-code-info</td>
    <td>version</td>
    <td>属性</td>
    <td>否</td>
    <td>
     所使用指定language的版本号
    </td>
    <td>
        如language为vue,version为3,则说明使用vue3版本
    </td>
  </tr>
  <tr>
    <td>orm-code-info</td>
    <td>file-path</td>
    <td>属性</td>
    <td>是</td>
    <td>
     项目所在的目录
    </td>
    <td>
     如项目目录已存在，则在已有目录上进行编码，否则新增项目目录
    </td>
  </tr>
  <tr>
    <td>orm-code-info</td>
    <td>isCreate</td>
    <td>属性</td>
    <td>是</td>
    <td>
     项目是否新增
    </td>
    <td>
        true:新增，false:非新增项目
    </td>
  </tr>
</table>

'module'元素说明:<br>
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
    <td>orm-code-info</td>
    <td>module</td>
    <td>元素</td>
    <td>是</td>
    <td>
     需要编写的项目模块
    </td>
    <td>
    </td>
  </tr>
  <tr>
    <td>module</td>
    <td>name</td>
    <td>属性</td>
    <td>是</td>
    <td>
     模块名称
    </td>
    <td>
    </td>
  </tr>
    <td>module</td>
    <td>isCreate</td>
    <td>属性</td>
    <td>是</td>
    <td>
     表明此模块为新增模块，还是对原有模块新增功能或对原有功能进行修改
    </td>
    <td>
     true：新增模块，false：模块已存在
    </td>
  </tr>
  </tr>
    <td>module</td>
    <td>isFinish</td>
    <td>属性</td>
    <td>是</td>
    <td>
     表明此模块是否已编写完成，如为true,则此元素下所有'fuction'子元素的功能无需开发
    </td>
    <td>
     true：模块已开发已完成，false：模块未开发完成
    </td>
  </tr>
  <tr>
    <td>module</td>
    <td>desc</td>
    <td>属性</td>
    <td>否</td>
    <td>模块的相关描述</td>
    <td></td>
  </tr>
</table>

'function'元素说明:<br>
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
    <td>module</td>
    <td>function</td>
    <td>元素</td>
    <td>是</td>
    <td>
     需要编写的功能或者页面
    </td>
    <td>
    </td>
  </tr>
  <tr>
    <td>function</td>
    <td>name</td>
    <td>属性</td>
    <td>是</td>
    <td>
     功能名称
    </td>
    <td>
    </td>
  </tr>
    <td>function</td>
    <td>isCreate</td>
    <td>属性</td>
    <td>是</td>
    <td>
     表明此功能为新增功能，还是对原有功能进行修改
    </td>
    <td>
     true：新增功能，false：原有功能修改
    </td>
  </tr>
  </tr>
    <td>function</td>
    <td>isFinish</td>
    <td>属性</td>
    <td>是</td>
    <td>
     表明此功能是否已编写完成
    </td>
    <td>
     true：功能已开发已完成，false：功能未开发完成
    </td>
  </tr>
  <tr>
    <td>function</td>
    <td>desc</td>
    <td>属性</td>
    <td>否</td>
    <td>功能的相关描述</td>
    <td></td>
  </tr>
  <tr>
    <td>function</td>
    <td>file</td>
    <td>属性</td>
    <td>是</td>
    <td>功能在对应的文件中进行编码开发。此为相对目录，如orm-code-info元素中file-path为/project/web，此属性为order.vue，则文件完整路径为/project/web/order.vue</td>
    <td>
      1.此属性有值，如文件不存在，则AI先创建文件，然后AI在指定路径的文件中编写功能。<br>
      2.此属性无值 则AI创建新的文件，然后AI在指定路径的文件中编写功能，且AI需将新创建的文件路径写入到此属性中。
    </td>
  </tr>
  <tr>
    <td>function</td>
    <td>method</td>
    <td>属性</td>
    <td>是</td>
    <td>功能在对应的方法中进行编码开发。</td>
    <td>
      1.此属性有值，如方法不存在，则先创建方法，然后AI在指定路径的方法中编写功能。<br>
      2.此属性无值 则AI创建新的方法，然后AI在指定路径的方法中编写功能，且AI需将新创建的方法名写入到此属性中
    </td>
  </tr>
    <td>function</td>
    <td>url</td>
    <td>属性</td>
    <td>是</td>
    <td>调用后端接口的url</td>
    <td>
      此属性无值 则AI创建新的url，然后AI需将新创建的url写入到此属性中
    </td>
  </tr>
</table>

'orm-database-config'元素说明:<br>
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
    <td>orm-database-config</td>
    <td>orm-database-info</td>
    <td>元素</td>
    <td>是</td>
    <td>
     AI生成的代码所使用的数据库
    </td>
    <td>
    </td>
  </tr>

  <tr>
    <td>orm-database-info</td>
    <td>type</td>
    <td>属性</td>
    <td>是</td>
    <td>
     所使用的数据库，myseql,oracle,sqlserver,pg等
    </td>
    <td>
    </td>
  </tr>

  <tr>
    <td>orm-database-info</td>
    <td>version</td>
    <td>属性</td>
    <td>是</td>
    <td>
     数据库所使用的版本号版本号
    </td>
    <td>
        如type为mysql,version为8,则说明使用mysql8版本
    </td>
  </tr>
  
</table>

为AI配置Rule
===
在rule-view-info中，添加对应的rule,以告知让AI通过编写Listener来实现业务逻辑，以下为具体示例：<br>
配置文件：
```
<orm-rule-mapping>
    <rule-view-info name="save-Order" view-ref="OrderInfo">
    <rule name="dsl" type="grammer" >
    <error-info code="C001" message="user error" level="1"/>
        <customer-process>
        <![CDATA[
            #num : if totalPrice>10 then totalPrice*1.1  else totalPrice*1.2;
            totalPrice : totalPrice*#num;
        ]]>
    </customer-process>
    </rule>
    
    <!--第一处-->
    <rule name="ai-demo1" type="ai">
        <content>
        检查订单的商品数量是否超过10，如果超过10则报错返回
        </content>
    </rule>
    
    <!--第二处-->
    <rule name="ai-demo2" type="ai">
        <content>
        totalPrice = sum(productList.count*productList.productPrice)
        </content>
    </rule>
    
    <rule name="checkName" type="checkPattern" pattern="userT.userName = userT.uname" >
    <error-info code="C001" message="user error" level="1"/>
    </rule>
    
    <rule name="insertUser" type="insert" property="userT" />
    
    <rule name="checkUser" type="checkDataPattern" property="userT" sql="select a.id as u_id from userT a where a.id = #userT.id" pattern="u_id = userId"/>
    
    <rule name="checkUser1" type="checkData" property="userT" pattern="NOTNULL"/>
    
    <rule name="check1" type="checkPattern" pattern="userId != 0 and userT.id != 0 and ( productCount > 0 or totalPrice >= 0 ) or (productCount*(totalPrice+10) > 200 )"/>
    
    <rule name="checkUerId" type="check" property="userId" pattern="NOTNULL;NOTEQUAL:2"/>
    
    <rule name="insertOrder" type="insert" property="OrderInfo" />
    
    <rule name="insertProduct" type="insert" property="productList" />
    
    <rule name="deleteProduct" type="delete" sql="delete p.* from productList p where p.productPrice = 20" />
    </rule-view-info>
</orm-rule-mapping>
```
在以上配置中，'第一处'与'第二处'，分别添加了type为ai的rule，而contenet中的内容为ai需要处理的逻辑，'第一处'采用的是纯文字描述的方式，'第二处'采用的是类似语法的方式。
而AI需要自动编写一个Listener类，其需实现ViewListener类，具体如下：<br>
```
public class SaveOrderAIViewListener implements ViewListener{

    private final static Logger log = LoggerFactory.getLogger(SaveOrderAIViewListener.class);
    
    @Override
    public ResultInfo notify(ViewEvent event) {
    
        //1.检查产品数量是否大于10
        if(event.getType() == ViewEventEnum.VIEW_START && event.getRuleName().equals("ai-demo1")){
            ModelData modelData = event.getModelData();
            List<Map<String,Object>> productList = (List<Map<String, Object>>) modelData.getValue("productList");
            if(productList.size()>10){
                return ResultInfo.fail("C001","产品数量不能大于10","1");
            }
        }
    
        //2.对产品总金额求和
        if(event.getType() == ViewEventEnum.VIEW_START && event.getRuleName().equals("ai-demo2")){
            ModelData modelData = event.getModelData();
            List<Map<String,Object>> productList = (List<Map<String, Object>>) modelData.getValue("productList");
            double totalPrice = 0d;
            for(Map<String,Object> product : productList){
                totalPrice  = totalPrice+ ((Integer) product.get("productCount")) * ((Double) product.get("productPrice"));
            }
            modelData.setValue("totalPrice",totalPrice);
    
        }
    
        return ResultInfo.success();
    }
}    
```
以上代码中，'1'处的代码逻辑对应配置文件中rule名称为'ai-demo1'的配置内容，而'2'处的代码逻辑对应配置文件中rule名称为'ai-demo2'的配置内容，
最后以下是SaveOrderAIViewListener的使用方式：<br>
```
  ModelContainer container = ContainerManager.getCurrentModelContainer();
  ModelLoader loader = new ModelLoader();
  //1.增加相应AI生成的listener
  loader.load("save-Order", order).addListener(new SaveOrderAIViewListener());
  container.load(loader);
```
在以上代码中，在'1'处将AI生成的Listener加入到ModelLoader中，之后执行时会调用相应的代码进行处理。
