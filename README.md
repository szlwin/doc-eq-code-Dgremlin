# doc-eq-code-Dgremlin

在系统或平台开发中，产品、开发、测试间的沟通及功能、架构扩展一直是长期问题，直到现在没有有效理论或解决方案。本人经由多年的开发、设计、架构，对以上问题长期研究和总结，开发此项目用于解决以上问题。在此项目中涉及到新的理念或是方法论，在使用此项目前，需具体了解下，具体可参考[1.项目理念](docs/concept.md)。
   
主要用于业务建模、将设计与实现分离，通过xml业务配置文档可快速与产品、开发、测试沟通业务需求。以及支持可跨多数据源进行数据读写操作，并提供扩展接口可让用户自定义数据源。

[1.项目理念](docs/concept.md)<br>
[2.项目介绍](docs/info.md)<br>
[3.dom整体架构](docs/architecture.md)<br>
[4.dom设计文档](docs/design.md)<br>
[5.dom示例](docs/dom-demo.md)<br>
[6.数据源扩展](docs/dom-datasource.md)<br>
[7.数据生产与消费](docs/declaration.md)<br>
[8.数据生产与消费示例](docs/declaration-demo.md)<br>
[9.dom与declaration联合示例](docs/mix-demo.md)<br><br>

联系邮箱：szlwin@163.com<br>

注：使用此项目前，需在本地仓库中安装jar包，所需的jar包都在lib目录下，在lib目录下执行以下命令进行安装.<br>
```
mvn install:install-file -Dfile=easy-check-1.0.jar  -DgroupId=smarter -DartifactId=easy-check  -Dversion=1.0 -Dpackaging=jar

mvn install:install-file -Dfile=santr-2.0.3.jar  -DgroupId=smarter -DartifactId=santr  -Dversion=2.0.3 -Dpackaging=jar

mvn install:install-file -Dfile=express-check-1.0.jar  -DgroupId=smarter -DartifactId=express-check  -Dversion=1.0 -Dpackaging=jar
```
