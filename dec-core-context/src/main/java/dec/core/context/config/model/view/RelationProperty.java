package dec.core.context.config.model.view;

public class RelationProperty {
	public static final String NAME = "name";
	
	public static final String REF_PROPERTY = "ref-property";
	
	private String name;
	
	private String refProperty;

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
}
