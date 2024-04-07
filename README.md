# doc-eq-code-Dgremlin
主要用于业务建模、将设计与实现分离，通过xml业务配置文档可快速与产品、开发、测试沟通业务需求。以及支持可跨多数据源进行数据读写操作，并提供扩展接口可让用户自定义数据源。

其对数据源定义为：可对其进行读写操作，其可是RMDBping、NoSQL、MQ、文件、磁盘、或是数据库集群、第三方系统、数据中间件。

其在上层进行数据访问统一抽象，对不同数据源提供统一接口接进行接入。对业务侧完全屏蔽底层数据操作细节，并可对数据操作进行文档化，便于追踪数据流向，便于复杂数据操作的维护与扩展。

[1.项目理念](docs/concept.md)<br>
[2.项目介绍](docs/info.md)<br>
[3.整体架构](docs/architecture.md)<br>
[4.dom配置文件](docs/config.md)<br>
[5.dom示例](docs/dom-demo.md)<br>
[6.数据源扩展](docs/dom-datasource.md)<br>
[7.数据生产与消费](docs/declaration.md)<br>
[8.数据生产与消费示例](docs/declaration-demo.md)<br>
[9.dom与declaration联合示例](docs/mix-demo.md)<br>
