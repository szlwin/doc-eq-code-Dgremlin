package com.orm.common.xml.model.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.orm.common.xml.model.data.Data;
import com.orm.common.xml.model.relation.Relation;

public class RelationView {

	public static final String REF = "ref";
	
	public static final String RELATION_PROPERTY = "relation-property";
	
	public static final String PROPERTY = "property";
	
	public static final String DATA = "data";
	
	private Relation ref;
	
	private String relationProperty;
	
	private Data data;
	
	private Map<String,RelationProperty> properInfo = new HashMap<String,RelationProperty>(10);
	
	/*
	public RelationProperty getProperty(String name) {
		return properInfo.get(name);
	}

	public void addProperty(RelationProperty relationProperty) {
		properInfo.put(relationProperty.getName(),relationProperty);
	}
	
	public Relation getRef() {
		return ref;
	}

	public void setRef(Relation ref) {
		this.ref = ref;
	}

	public String getRelationProperty() {
		return relationProperty;
	}

	public void setRelationProperty(String relationProperty) {
		this.relationProperty = relationProperty;
	}

	public Collection<RelationProperty> getAll(){
		return properInfo.values();
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}
	
	public Map<String,RelationProperty> getValues(){
		return properInfo;
	}*/
	
	
}
