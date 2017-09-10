package com.orm.common.xml.model.config;

import java.util.List;

import com.orm.common.config.Config;
import com.orm.common.config.ConfigFactory;
import com.orm.common.config.ConnectionConfig;
import com.orm.common.config.DataSourceConfig;
import com.orm.common.xml.model.config.connection.Connection;
import com.orm.common.xml.model.config.datasource.DataSource;
import com.orm.common.xml.model.data.Data;
import com.orm.common.xml.model.relation.Relation;
import com.orm.common.xml.model.rule.RuleViewInfo;
import com.orm.common.xml.model.view.ViewData;

public class ConfigInfo {

	public static final String CONNECTION_INFO = "orm-connection-info";
	
	public static final String DEFAULT_CONNECTION = "default";
	
	public static final String DATASOURCE_INFO = "orm-datasource-info";
	
	public static final String DEFAULT_DATASOURCE = "default";
	
	public static final String DATA_FILE_INFO = "orm-data-file-info";
	
	public static final String RELATION_FILE_INFO = "orm-relation-file-info";
	
	public static final String VIEW_FILE_INFO = "orm-view-file-info";
	
	public static final String RULE_FILE_INFO = "orm-rule-file-info";
	
	public static final String DATASOURCE_CONFIG =  "orm-dataSource-config";
	
	public static final String CONNECTION_CONFIG =  "orm-connection-config";
	
	public static final String SERVICE_FILE_INFO = "orm-service-info";
	
	private String defaultConnection;
	
	private String defaultDataSource;

	public Data getData(String name) {
		return (Data) get(Config.DATA,name);
	}

	public void addData(Data data) {
		add(Config.DATA,data);
	}

	public void addDataList(List<Data> list) {
		for(int i = 0; i < list.size(); i++)
		{
			addData(list.get(i));
		}
	}
	
	public Relation getRelation(String name)
	{
		return (Relation) get(Config.RELATION,name);
	}
	
	public void addRelation(Relation relation) {
		add(Config.RELATION,relation);
	}

	public void addRelationList(List<Relation> list) {
		for(int i = 0; i < list.size(); i++)
		{
			addRelation(list.get(i));
		}
	}

	public void addViewData(ViewData viewdata) {
		add(Config.VIEWDATA,viewdata);
	}

	public void addViewDataList(List<ViewData> list) {
		for(int i = 0; i < list.size(); i++)
		{
			addViewData(list.get(i));
		}
	}

	public ViewData getViewData(String name) {
		return (ViewData) get(Config.VIEWDATA,name);
	}
	
	public String getDefaultConnection() {
		return defaultConnection;
	}

	public void setDefaultConnection(String defaultConnection) {
		this.defaultConnection = defaultConnection;
		ConnectionConfig.getInstance().setDefaultName(defaultConnection);
	}

	public String getDefaultDataSource() {
		return defaultDataSource;
	}

	public void setDefaultDataSource(String defaultDataSource) {
		this.defaultDataSource = defaultDataSource;
		DataSourceConfig.getInstance().setDefaultName(defaultDataSource);
	}
	
	public DataSource getDataSource(String name)
	{
		return (DataSource) get(Config.DATASOURCE,name);
	}
	
	public void addDataSource(DataSource dataSource)
	{
		add(Config.DATASOURCE,dataSource);
	}
	
	public Connection getConnection(String name)
	{
		return (Connection) get(Config.CONNECTION,name);
	}
	
	public void addConnection(Connection connection)
	{
		ConnectionConfig.getInstance().add(connection);
	}
	
	public RuleViewInfo getRuleViewInfo(String name)
	{
		return (RuleViewInfo) get(Config.RULE,name);
	}
	
	public void addRuleViewInfo(RuleViewInfo ruleViewInfo) {
		add(Config.RULE,ruleViewInfo);
	}

	public void addRuleViewInfoList(List<RuleViewInfo> list) {
		for(int i = 0; i < list.size(); i++)
		{
			addRuleViewInfo(list.get(i));
		}
	}
	
	protected Config<? extends ConfigBaseData> getConfig(int type){
		return ConfigFactory.getConfig(type);
	}
	
	public ConfigBaseData get(int type,String name){
		return getConfig(type).get(name);
	}
	
	public <V extends ConfigBaseData> void add(int type,V v){
		getConfig(type).add(v);
	}
	
	public void addList(int type,List<ConfigBaseData> list) {
		for(int i = 0; i < list.size(); i++)
		{
			add(type,list.get(i));
		}
	}
}
