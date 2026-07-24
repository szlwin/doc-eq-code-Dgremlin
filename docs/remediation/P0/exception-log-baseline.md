# P0 异常与日志风险扫描

## printStackTrace (7)

```text
dec-core-model/src/main/java/dec/core/directory/container/DirectoryContainer.java:143: e.printStackTrace();
dec-core-model/src/main/java/dec/core/model/execute/tran/advance/back/TranProtecter.java:42: e.printStackTrace();
dec-datasource-orm-mysql/src/main/java/dec/external/datasource/sql/mysql/convert/container/MySQLLexer.java:20: e.printStackTrace();
dec-demo/src/main/java/dec/demo/config/DemoLoadTests.java:31: e.printStackTrace();
dec-expand-declaration/src/main/java/dec/expand/declare/bean/Bean.java:194: e.printStackTrace();
dec-expand-declaration/src/main/java/dec/expand/declare/bean/BeanBack.java:111: e.printStackTrace();
dec-expand-declaration/src/main/java/dec/expand/declare/grammar/SimpleExprVisitor.java:38: localException.printStackTrace();
```

## swallowed catch (0)

```text
(none)
```

## return null (86)

```text
dec-context-config-parse-xml/src/main/java/dec/context/parse/xml/parse/config/CommonParser.java:97: return null;
dec-context-config-parse-xml/src/main/java/dec/context/parse/xml/parse/directory/ActionRuleParser.java:61: return null;
dec-context-config-parse-xml/src/main/java/dec/context/parse/xml/parse/directory/ActionRuleParser.java:206: return null;
dec-context-config-parse-xml/src/main/java/dec/context/parse/xml/parse/directory/ActionRuleParser.java:229: return null;
dec-context-config-parse-xml/src/main/java/dec/context/parse/xml/parse/directory/DirectoryParser.java:43: return null;
dec-context-config-parse-xml/src/main/java/dec/context/parse/xml/parse/directory/DirectoryParser.java:47: return null;
dec-context-config-parse-xml/src/main/java/dec/context/parse/xml/parse/directory/DirectoryParser.java:71: return null;
dec-context-config-parse-xml/src/main/java/dec/context/parse/xml/parse/directory/DirectoryParser.java:75: return null;
dec-context-config-parse-xml/src/main/java/dec/context/parse/xml/parse/directory/DirectoryParser.java:95: return null;
dec-context-config-parse-xml/src/main/java/dec/context/parse/xml/parse/directory/DirectoryParser.java:110: return null;
dec-context-config-parse-xml/src/main/java/dec/context/parse/xml/parse/rule/Convert.java:94: return null;
dec-context-config-parse-xml/src/main/java/dec/context/parse/xml/parse/rule/RuleParser.java:232: return null;
dec-context-config-parse-xml/src/main/java/dec/context/parse/xml/parse/rule/RuleParser.java:255: return null;
dec-context-config-parse-xml/src/main/java/dec/context/parse/xml/parse/view/ViewParser.java:191: return null;
dec-context-config-parse-yaml/src/main/java/dec/context/parse/yaml/parse/YamlSupport.java:80: return null;
dec-context-config-parse-yaml/src/main/java/dec/context/parse/yaml/parse/YamlSupport.java:103: return null;
dec-context-config-parse-yaml/src/main/java/dec/context/parse/yaml/parse/YamlSupport.java:110: return null;
dec-context-config-parse-yaml/src/main/java/dec/context/parse/yaml/parse/directory/YamlDirectoryFileParser.java:56: return null;
dec-context-config-parse-yaml/src/main/java/dec/context/parse/yaml/parse/directory/YamlDirectoryFileParser.java:82: return null;
dec-context-config-parse-yaml/src/main/java/dec/context/parse/yaml/parse/directory/YamlDirectoryFileParser.java:98: return null;
dec-context-config-parse-yaml/src/main/java/dec/context/parse/yaml/parse/directory/YamlDirectoryFileParser.java:113: return null;
dec-context-config-parse-yaml/src/main/java/dec/context/parse/yaml/parse/directory/YamlDirectoryFileParser.java:151: return null;
dec-context-config-parse-yaml/src/main/java/dec/context/parse/yaml/parse/rule/Convert.java:69: return null;
dec-context-config-parse-yaml/src/main/java/dec/context/parse/yaml/parse/rule/YamlRuleFileParser.java:94: return null;
dec-context-config-parse-yaml/src/main/java/dec/context/parse/yaml/parse/rule/YamlRuleFileParser.java:122: return null;
dec-context-config-parse-yaml/src/main/java/dec/context/parse/yaml/parse/rule/YamlRuleFileParser.java:134: return null;
dec-core-context/src/main/java/dec/core/context/collections/list/SimpleList.java:164: return null;
dec-core-context/src/main/java/dec/core/context/config/model/connection/config/AbstractConfig.java:14: return null;
dec-core-context/src/main/java/dec/core/context/config/model/view/RelationInfo.java:42: return null;
dec-core-context/src/main/java/dec/core/context/config/model/view/RelationInfo.java:56: return null;
dec-core-model/src/main/java/dec/core/collections/list/SimpleList.java:164: return null;
dec-core-model/src/main/java/dec/core/collections/list/SimpleList.java:169: return null;
dec-core-model/src/main/java/dec/core/collections/list/SimpleList.java:174: return null;
dec-core-model/src/main/java/dec/core/model/check/rule/CheckFactory.java:33: return null;
dec-core-model/src/main/java/dec/core/model/container/ContainerFactory.java:16: return null;
dec-core-model/src/main/java/dec/core/model/container/ModelLoader.java:69: return null;
dec-core-model/src/main/java/dec/core/model/container/SynContainer.java:195: return null;
dec-core-model/src/main/java/dec/core/model/container/ThreadContainer.java:44: return null;
dec-core-model/src/main/java/dec/core/model/execute/rule/RuleContainer.java:90: return null;
dec-core-model/src/main/java/dec/core/model/execute/rule/RuleContainer.java:244: //return null;
dec-core-model/src/main/java/dec/core/model/execute/rule/data/DataExecuteFactory.java:29: //return null;*/
dec-core-model/src/main/java/dec/core/model/execute/rule/data/DataExecuteFactory.java:31: return null;
dec-core-model/src/main/java/dec/core/model/execute/tran/TransactionContainer.java:288: return null;
dec-core-model/src/main/java/dec/core/model/execute/tran/advance/TranAdvanceContainer.java:353: return null;
dec-core-model/src/main/java/dec/core/model/execute/tran/advance/back/TranQueueIO.java:64: return null;
dec-core-model/src/main/java/dec/core/model/execute/tran/advance/group/MainTranGroup.java:80: return null;
dec-core-model/src/main/java/dec/core/model/execute/tran/advance/group/MainTranGroup.java:92: return null;
dec-core-model/src/main/java/dec/core/model/execute/tran/advance/group/NoTranGroup.java:16: return null;
dec-core-model/src/main/java/dec/core/model/utils/DataUtil.java:138: return null;
dec-core-model/src/main/java/dec/core/model/utils/DataUtil.java:153: return null;
dec-core-model/src/main/java/dec/core/model/utils/DataUtil.java:171: return null;
dec-core-model/src/main/java/dec/core/model/utils/DataUtil.java:195: return null;
dec-datasource-orm-mysql/src/main/java/dec/external/datasource/sql/mysql/convert/container/MySQLConvert.java:40: return null;
dec-datasource-orm-mysql/src/main/java/dec/external/datasource/sql/mysql/datatype/convert/IntegerToSqlBigInteger.java:10: return null;
dec-datasource-orm-mysql/src/main/java/dec/external/datasource/sql/mysql/datatype/convert/LongToSqlBigInteger.java:10: return null;
dec-datasource-orm-mysql/src/main/java/dec/external/datasource/sql/mysql/datatype/convert/MySQLDataTypeConvert.java:43: return null;
dec-datasource-orm-mysql/src/main/java/dec/external/datasource/sql/mysql/datatype/convert/SqlBigIntegerToInteger.java:10: return null;
dec-datasource-orm-mysql/src/main/java/dec/external/datasource/sql/mysql/datatype/convert/SqlBigIntegerToLong.java:11: return null;
dec-datasource-orm-sql/src/main/java/dec/external/datasource/sql/collections/list/SimpleList.java:162: return null;
dec-datasource-orm-sql/src/main/java/dec/external/datasource/sql/connection/factory/ConnectionFactory.java:59: return null;
dec-datasource-orm-sql/src/main/java/dec/external/datasource/sql/convert/common/CommonConvert.java:80: return null;
dec-datasource-orm-sql/src/main/java/dec/external/datasource/sql/convert/origin/OrignSQLConvert.java:108: return null;
dec-datasource-orm-sql/src/main/java/dec/external/datasource/sql/convert/origin/OrignSQLConvert.java:126: return null;
dec-datasource-orm-sql/src/main/java/dec/external/datasource/sql/convert/origin/OrignSQLConvert.java:136: return null;
dec-datasource-orm-sql/src/main/java/dec/external/datasource/sql/datatype/convert/DataTypeConvert.java:16: return null;
dec-datasource-orm-sql/src/main/java/dec/external/datasource/sql/query/BatchQuery.java:84: return null;
dec-datasource-orm-sql/src/main/java/dec/external/datasource/sql/query/SelectQuery.java:40: return null;
dec-datasource-orm-sql/src/main/java/dec/external/datasource/sql/utils/SQLUtil.java:17: return null;
dec-datasource-orm-sql/src/main/java/dec/external/datasource/sql/utils/Util.java:161: return null;
dec-datasource-orm-sql/src/main/java/dec/external/datasource/sql/utils/Util.java:177: return null;
dec-datasource-orm-sql/src/main/java/dec/external/datasource/sql/utils/Util.java:195: return null;
dec-datasource-orm-sql/src/main/java/dec/external/datasource/sql/utils/Util.java:200: return null;
dec-expand-declaration/src/main/java/dec/expand/declare/bean/Bean.java:84: return null;
dec-expand-declaration/src/main/java/dec/expand/declare/bean/Bean.java:374: return null;
dec-expand-declaration/src/main/java/dec/expand/declare/bean/Bean.java:495: return null;
dec-expand-declaration/src/main/java/dec/expand/declare/bean/BeanBack.java:350: return null;
dec-expand-declaration/src/main/java/dec/expand/declare/collections/SimpleList.java:164: return null;
dec-expand-declaration/src/main/java/dec/expand/declare/conext/ContextStorage.java:28: return null;
dec-expand-declaration/src/main/java/dec/expand/declare/conext/DataStorage.java:44: return null;
dec-expand-declaration/src/main/java/dec/expand/declare/conext/desc/business/BusinessDesc.java:54: return null;
dec-expand-declaration/src/main/java/dec/expand/declare/conext/parser/xml/parser/BusinessParser.java:210: return null;
dec-expand-declaration/src/main/java/dec/expand/declare/conext/parser/yaml/parser/ContextDescYamlParser.java:290: return null;
dec-expand-declaration/src/main/java/dec/expand/declare/conext/parser/yaml/parser/YamlSupport.java:43: return null;
dec-expand-declaration/src/main/java/dec/expand/declare/conext/parser/yaml/parser/YamlSupport.java:65: return null;
dec-expand-declaration/src/main/java/dec/expand/declare/conext/parser/yaml/parser/YamlSupport.java:72: return null;
dec-expand-declaration/src/main/java/dec/expand/declare/conext/utils/DataUtils.java:449: return null;
```

## System.out (55)

```text
dec-context-config-parse-xml/src/main/java/dec/context/parse/xml/parse/config/CommonParser.java:58: //System.out.println(nodeArray[type]);
dec-context-config-parse-xml/src/main/java/dec/context/parse/xml/parse/config/ConnectionInfoParser.java:55: //System.out.println(Thread.currentThread().getContextClassLoader().loadClass("DoNotDeleteMe").getResource(filePath).getPath());
dec-context-config-parse-xml/src/main/java/dec/context/parse/xml/parse/directory/DirectoryFileParser.java:61: System.out.println(subDirectory.getRel());
dec-core-context/src/main/java/dec/core/context/collections/list/SimpleList.java:119: //System.out.println("addAll:"+c.size()+":"+size);
dec-core-model/src/main/java/dec/core/collections/list/SimpleList.java:119: //System.out.println("addAll:"+c.size()+":"+size);
dec-core-model/src/main/java/dec/core/directory/container/DirectoryContainer.java:128: System.out.println(sql.trim());
dec-core-model/src/main/java/dec/core/directory/container/DirectoryContainer.java:132: System.out.println(sql.trim());
dec-core-model/src/main/java/dec/core/directory/container/DirectoryContainer.java:139: System.out.println("data:" + dataList);
dec-core-model/src/main/java/dec/core/directory/container/DirectoryContainer.java:147: //System.out.println(condition);
dec-core-model/src/main/java/dec/core/model/execute/rule/data/DeleteExecute.java:16: //System.out.println(sql);
dec-core-model/src/main/java/dec/core/model/execute/rule/data/UpdateExecute.java:14: //System.out.println(sql);
dec-datasource-orm-sql/src/main/java/dec/external/datasource/sql/collections/list/SimpleList.java:117: //System.out.println("addAll:"+c.size()+":"+size);
dec-datasource-orm-sql/src/main/java/dec/external/datasource/sql/connection/factory/ConnectionFactory.java:46: //System.out.println("get con start:"+format.format(new Date()));
dec-datasource-orm-sql/src/main/java/dec/external/datasource/sql/connection/factory/ConnectionFactory.java:58: //System.out.println("get con end:"+format.format(new Date()));
dec-demo/src/main/java/dec/demo/declaration/TestOrderBusiness.java:175: System.out.println(storage.getStatus("status"));
dec-demo/src/main/java/dec/demo/declaration/TestOrderBusiness.java:176: System.out.println(order.getStatus());
dec-demo/src/main/java/dec/demo/declaration/datasource/MockDataSourceManager.java:38: System.out.println("rollBack group:"+transactionStatusList.get(index).getGroup());
dec-demo/src/main/java/dec/demo/directory/ConfigInit.java:43: System.out.println(JSON.toJSONString(directoryInfo));
dec-demo/src/main/java/dec/demo/directory/DirectoryTest.java:36: System.out.println(userDataList.size()+":"+userDataList.get(0).getName());
dec-demo/src/main/java/dec/demo/model/RuleTests.java:50: System.out.println(date);
dec-demo/src/main/java/dec/demo/model/RuleTests.java:51: System.out.println(format.format(new Date()));
dec-expand-declaration/src/main/java/dec/expand/declare/collections/SimpleList.java:119: //System.out.println("addAll:"+c.size()+":"+size);
dec-expand-declaration/src/main/java/dec/expand/declare/grammar/SimpleExprVisitor.java:209: System.out.print(variable+".");
dec-expand-declaration/src/main/java/dec/expand/declare/grammar/SimpleExprVisitor.java:211: System.out.println(" ");
dec-expand-declaration/src/main/java/dec/expand/declare/grammar/SimpleExprVisitor.java:214: System.out.println(variable);
dec-expand-declaration/src/main/java/dec/expand/declare/grammar/SimpleExprVisitor.java:218: //System.out.println(variables);
dec-expand-declaration/src/main/java/dec/expand/declare/grammar/SimpleExprVisitor.java:242: //System.out.println(this.getChildValue(context, i));
dec-expand-declaration/src/test/java/Test.java:23: System.out.println("wee:"+str);
dec-expand-declaration/src/test/java/Test.java:34: System.out.println(str.get("data-string"));
dec-expand-declaration/src/test/java/Test.java:41: System.out.println(strs.get("data-stringArray"));
dec-expand-declaration/src/test/java/Test.java:48: System.out.println(str);
dec-expand-declaration/src/test/java/Test.java:53: System.out.println("one:"+str.get("s-data-strings"));
dec-expand-declaration/src/test/java/Test.java:58: System.out.println("last:"+str);
dec-expand-declaration/src/test/java/test/business/MockDataSourceManager.java:19: System.out.println("connect group:"+connecionDesc.getGroup());
dec-expand-declaration/src/test/java/test/business/MockDataSourceManager.java:23: System.out.println("commit group:"+transactionStatusList.get(index).getGroup());
dec-expand-declaration/src/test/java/test/business/MockDataSourceManager.java:33: System.out.println("rollBack group:"+transactionStatusList.get(index).getGroup());
dec-expand-declaration/src/test/java/test/business/TestOrderBusiness.java:29: System.out.println();
dec-expand-declaration/src/test/java/test/business/TestOrderBusiness.java:46: System.out.println("Produce $payResultData");
dec-expand-declaration/src/test/java/test/business/TestOrderBusiness.java:56: System.out.println("Produce $payData");
dec-expand-declaration/src/test/java/test/business/TestOrderBusiness.java:98: System.out.println("Produce $payResultData");
dec-expand-declaration/src/test/java/test/business/TestOrderBusiness.java:108: System.out.println("Produce $payData");
dec-expand-declaration/src/test/java/test/business/TestOrderBusiness.java:150: System.out.println("Produce $payResultData");
dec-expand-declaration/src/test/java/test/business/TestOrderBusiness.java:160: System.out.println("Produce $payData");
dec-expand-declaration/src/test/java/test/business/TestOrderBusiness.java:200: System.out.println("Produce $payResultData");
dec-expand-declaration/src/test/java/test/business/TestOrderBusiness.java:210: System.out.println("Produce $payData");
dec-expand-declaration/src/test/java/test/business/TestOrderBusiness.java:284: System.out.println("change:"+order.getStatus());
dec-expand-declaration/src/test/java/test/business/TestOrderBusiness.java:288: System.out.println("Produce orderData");
dec-expand-declaration/src/test/java/test/business/TestOrderBusiness.java:292: System.out.println("OrderId:"+orderId);
dec-expand-declaration/src/test/java/test/business/TestOrderBusiness.java:302: System.out.println("Produce subscribeOrderData");
dec-expand-declaration/src/test/java/test/business/TestOrderBusiness.java:305: System.out.println("subscribeOrderData.getStatus()" + subscribeOrderData.getStatus());
dec-expand-declaration/src/test/java/test/business/TestOrderBusiness.java:314: java.lang.System.out.println("produce cancelOrderData");
dec-expand-declaration/src/test/java/test/business/TestOrderBusiness.java:316: //java.lang.System.out.println(order.getId());
dec-expand-declaration/src/test/java/test/business/TestOrderBusiness.java:321: System.out.println("Produce orderPayResultData");
dec-expand-declaration/src/test/java/test/business/TestOrderBusiness.java:335: System.out.println("Produce payCmdData");
dec-expand-declaration/src/test/java/test/business/TestOrderBusiness.java:346: System.out.println("Produce payResultData");
```


## 后续映射

- Parser/配置初始化：P1 Diagnostic 与 Loader Pipeline。
- Runtime/Query/Transaction：P7 统一错误模型与事务回滚。
- SQL 拼接和连接硬编码：P6 QueryPlan 与 ConnectionRoute。
