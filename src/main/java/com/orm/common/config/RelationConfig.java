package com.orm.common.config;

import java.util.HashMap;
import java.util.Map;

import com.orm.common.xml.model.config.ConfigBaseData;
import com.orm.common.xml.model.relation.Relation;

public class RelationConfig implements Config<Relation>{
	private static final RelationConfig relationConfig =  new RelationConfig();
	
	private Map<String,Relation> relationConfigMap = new HashMap<String,Relation>();
	
	private RelationConfig()
	{
		
	}

	public static RelationConfig getInstance()
	{
		return relationConfig;
	}

	public void add(Relation relation)
	{
		relationConfigMap.put(relation.getName(),relation);
	}
	
	public Relation get(String name)
	{
		return relationConfigMap.get(name);
	}

	public <E extends ConfigBaseData> void add(E relation) {
		add((Relation)relation);
	}
	

}
