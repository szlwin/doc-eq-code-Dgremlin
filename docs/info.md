# 项目介绍

## dec-context-config-parse-xml
对dom的xml格式的设计文档进行解析

## dec-core-context
存储解析后的dom设计文档信息

## dec-core-context
根据dom设计文档的内容执行业务功能

## dec-core-datasource
dom的数据源扩展接口，可根据其提供的接口进行扩展，具体可参考[6.数据源扩展](docs/dom-datasource.md)

## dec-core-starter
需使用dom的java项目引入此包，同时提供方法加载设计文档及设置数据源，具体可参考dec-demo中的dec.demo.config.DemoLoadTests。

## dec-expand-declaration
为数据声明的单独项目，此包括设计文档的加载、解析以及功能解析，可独立或结合dom使用。

## dec-demo
Demo项目，其中包括dom与数据声明的例子。

## dec-datasource-orm-sql
基于dec-core-datasource扩展的关系型数据库扩展包，各关系型数据库的数据源可基于此进行扩展。

## dec-datasource-orm-mysql
基于dec-datasource-orm-sql扩展的，mysql数据库的数据源。