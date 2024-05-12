package dec.core.context.config.model.relation.config;

import java.util.Map;

import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.config.data.ConfigBaseData;
import dec.core.context.config.model.relation.Relation;
import javolution.util.FastMap;

public class RelationConfig implements Config<Relation>{
	private static final RelationConfig relationConfig =  new RelationConfig();
	
	private Map<String,Relation> relationConfigMap = new FastMap<String,Relation>();
	
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
