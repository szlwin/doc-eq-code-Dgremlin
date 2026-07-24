# P0 整改前可重复基线

- Revision: `f9424aebabcf8b2350477931f71d98376ef81cd8`
- Git: `git version 2.47.3`
- Java runtime used for audit: `openjdk version "21.0.10" 2026-01-20`
- Maven executable used for audit: `not installed`
- Maven Wrapper at baseline: `absent`
- Modules in Reactor: 9 (`dec-core-context`, `dec-core-datasource`, `dec-context-config-parse-xml`, `dec-context-config-parse-yaml`, `dec-datasource-orm-sql`, `dec-datasource-orm-mysql`, `dec-core-model`, `dec-core-starter`, `dec-expand-declaration`)
- `dec-demo` in Reactor: `no`
- Tracked files: 535
- Java: 420; XML: 43; YAML: 18; Markdown: 14

## 已知构建阻断
- 基线没有 Maven Wrapper，当前审计容器也没有系统 Maven。
- `11` 处 `testFailureIgnore` 允许测试失败不阻断。
- `dec-demo` 未进入默认 Reactor。
- 数据库示例含固定 URL、账号、密码或连接名，无法作为无外部数据库的核心门禁。

## main 方法 (14)
```text
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-datasource-orm-mysql/src/main/java/dec/external/datasource/sql/mysql/convert/container/MySQLLexer.java:30:	public static void main(String args[]) throws ExecuteInvaildException{
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/bean/convert/MapToObject.java:13:    public static void main(String args[]) throws Exception {
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/bean/convert/ObjectListToObjectList.java:15:    public static void main(String args[]) throws Exception {
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/bean/convert/ObjectToMap.java:14:    public static void main(String args[]) throws Exception {
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/bean/convert/ObjectToObject.java:13:    public static void main(String args[]) throws Exception {
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/config/ConfigInit.java:10:	public static void main(String args[]) throws XMLParseException{
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/declaration/TestOrderBusiness.java:21:    public static void main(String[] args) throws Throwable {
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/directory/ConfigInit.java:22:	public static void main(String args[]) throws Exception{
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/directory/DirectoryTest.java:16:    public static void main(String args[]) throws Exception {
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/model/RuleTests.java:20:    public static void main(String args[]) throws Exception {
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/system/ConfigInit.java:24:	public static void main(String args[]) throws Exception{
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/system/OrderTest.java:21:    public static void main(String args[]) throws Exception {
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-expand-declaration/src/test/java/Test.java:10:	public static void main(String[] args) {
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-expand-declaration/src/test/java/test/business/TestOrderBusiness.java:14:    public static void main(String[] args) throws Throwable {
```

## testFailureIgnore (11)
```text
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-context-config-parse-xml/pom.xml:55:					<testFailureIgnore>true</testFailureIgnore>
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-context-config-parse-yaml/pom.xml:49:          <testFailureIgnore>true</testFailureIgnore>
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-core-context/pom.xml:48:					<testFailureIgnore>true</testFailureIgnore>
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-core-datasource/pom.xml:31:					<testFailureIgnore>true</testFailureIgnore>
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-core-model/pom.xml:92:					<testFailureIgnore>true</testFailureIgnore>
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-core-starter/pom.xml:70:					<testFailureIgnore>true</testFailureIgnore>
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-datasource-orm-mysql/pom.xml:59:					<testFailureIgnore>true</testFailureIgnore>
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-datasource-orm-sql/pom.xml:62:					<testFailureIgnore>true</testFailureIgnore>
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/pom.xml:87:					<testFailureIgnore>true</testFailureIgnore>
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-expand-declaration/pom.xml:74:					<testFailureIgnore>true</testFailureIgnore>
f9424aebabcf8b2350477931f71d98376ef81cd8:pom.xml:106:					<testFailureIgnore>true</testFailureIgnore>
```

## 固定数据库/连接 (41)
```text
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-core-model/src/main/java/dec/core/directory/container/DirectoryContainer.java:134:        SimpleSession simpleSession = new SimpleSession("con1");
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-datasource-orm-sql/src/main/java/dec/external/datasource/sql/connection/factory/ConnectionFactory.java:54:		mysqlDataSource.setUrl("jdbc:mysql://localhost/orm-test?maxActive=10&amp;useUnicode=true&amp;characterEncoding=utf8");
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/config/DemoLoadTests.java:69:        hikariConfig.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/demo-test");
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/config/DemoLoadTests.java:71:        hikariConfig.setPassword("mysqldb");
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/config/DemoLoadTests.java:85:		hikariConfig.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/demo-test1");
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/config/DemoLoadTests.java:87:		hikariConfig.setPassword("mysqldb");
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/directory/ConfigInit.java:51:		hikariConfig.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/demo-test2");
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/directory/ConfigInit.java:53:		hikariConfig.setPassword("mysqldb");
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/model/RuleTests.java:37:        loader.load("get-user", order, "con1");
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/model/RuleTests.java:77:        loader.load("save-Order", order, "con1").addListener(new SimpleViewListener());
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/model/RuleTests.java:81:        loader1.load("back-Order", order, "con2");
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/model/RuleTests.java:114:        loader.load("save-Order", order, "con1").addListener(new SimpleViewListener());
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/model/RuleTests.java:118:        loader1.load("back-Order", order, "con2");
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/system/ConfigInit.java:49:		hikariConfig.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/demo-test2");
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/system/ConfigInit.java:51:		hikariConfig.setPassword("mysqldb");
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/java/dec/demo/system/OrderTest.java:39:        loader.load("save-Order", order, "con1").addListener(new SimpleViewListener());
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/directory/orm-config.xml:29:		<orm-connection name="con1">
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/directory/orm-config.xml:35:		<orm-connection name="con2">
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/mix/orm-config.xml:38:    <orm-connection-info default="con1">
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/mix/orm-config.xml:39:        <orm-connection name="con1">
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/model/orm-config.xml:30:		<orm-connection name="con1">
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/model/orm-config.xml:36:		<orm-connection name="con2">
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/model/test-service/order_service.xml:5:			<task execute="save-Order" con="con1"/>
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/model/test-service/order_service.xml:8:			<task execute="save-Order" con="con2"/>
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/system/declare-config.xml:80:		<business name="subscribeOrderWithDom" ref-dom="save-User:con1,save-Order:con1">
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/system/declare-config.xml:82:				<data begin="true" ref-rule-connection="con1"/>
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/system/declare-config.xml:86:				<data begin="true" ref-rule-connection="con1"/>
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/system/declare-config.xml:88:						<data begin="true" transactionPolicy="NEW" ref-rule-connection="con1"/>
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/system/declare-config.xml:91:							<data begin="true" transactionPolicy="NEW" ref-rule-connection="con1"/>
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/system/orm-config.xml:24:	<orm-connection-info default="con1">
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/system/orm-config.xml:25:		<orm-connection name="con1">
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/system/orm-config.xml:30:		<orm-connection name="con2">
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/yaml/directory/orm-config.yaml:19:    - name: con1
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/yaml/directory/orm-config.yaml:22:    - name: con2
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/yaml/model/orm-config.yaml:17:  - name: con1
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/yaml/model/orm-config.yaml:20:  - name: con2
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/yaml/model/test-service/order_service.yaml:10:            con: con1
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/yaml/model/test-service/order_service.yaml:16:            con: con2
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/yaml/system/orm-config.yaml:19:  default: con1
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/yaml/system/orm-config.yaml:21:    - name: con1
f9424aebabcf8b2350477931f71d98376ef81cd8:dec-demo/src/main/resources/yaml/system/orm-config.yaml:24:    - name: con2
```
