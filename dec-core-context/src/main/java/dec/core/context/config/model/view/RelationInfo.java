package dec.core.context.config.model.view;

import java.util.Collection;
import java.util.Map;

import dec.core.context.config.model.relation.Relation;
import javolution.util.FastMap;

public class RelationInfo {

	public static final String RELATION_VIEW = "relation-view";
	
	//private Map<String,RelationView> relationView = new HashMap<String,RelationView>();

	private Map<String,Relation> relationMapByData = new FastMap<String,Relation>();
	
	private Map<String,Relation> relationMapByView = new FastMap<String,Relation>();
	/*
	public RelationView getRelation(String name) {
		return relationView.get(name);
	}
	
	public void addRelation(RelationView relation) {
		relationView.put(relation.getRelationProperty(),relation);
	}
	
	public Collection<RelationView> getRelation(){
		return relationView.values();
	}
	
	public RelationView getRelationByDataName(String dataName){
		Collection<RelationView> rCollection = relationView.values();
		Iterator<RelationView> it = rCollection.iterator();
		while(it.hasNext()){
			RelationView rView = it.next();
			String data = rView.getData().getName();
			
			if(data.equals(dataName)){
				return rView;
			}
		}
		return null;
	}
	
	public RelationView getRelationByPropertyName(String propertyName){
		Collection<RelationView> rCollection = relationView.values();
		Iterator<RelationView> it = rCollection.iterator();
		while(it.hasNext()){
			RelationView rView = it.next();
			String proName = rView.getRelationProperty();
			
			if(proName.equals(propertyName)){
				return rView;
			}
		}
		return null;
	}*/
	
	
	public Relation getRelationByDataName1(String name) {
		return relationMapByData.get(name);
	}
	
	public Relation getRelationByPropertyName1(String name) {
		return relationMapByView.get(name);
	}
	
	public void addRelation1(String name,Relation relation) {
		relationMapByData.put(name,relation);
		relationMapByView.put(relation.getViewProperty().getName(), relation);
	}
	
	public Collection<Relation> getRelation1(){
		return relationMapByData.values();
	}
}
