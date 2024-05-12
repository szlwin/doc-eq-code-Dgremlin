package dec.core.context.config.model.view;

import dec.core.context.config.model.relation.Relation;

public class ViewProperty {

	public static final String NAME = "name";
	
	public static final String REF_PROPERTY = "ref-property";
	
	public static final String RELATION = "relation";
	
	private String name;
	
	private String refProperty;

	private ViewData viewData;
	
	private Relation relation;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRefProperty() {
		return refProperty;
	}

	public void setRefProperty(String refProperty) {
		this.refProperty = refProperty;
	}

	public ViewData getViewData() {
		return viewData;
	}

	public void setViewData(ViewData viewData) {
		this.viewData = viewData;
	}

	public Relation getRelation() {
		return relation;
	}

	public void setRelation(Relation relation) {
		this.relation = relation;
	}
	
	
}
