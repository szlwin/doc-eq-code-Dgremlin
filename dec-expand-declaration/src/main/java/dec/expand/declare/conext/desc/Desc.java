package dec.expand.declare.conext.desc;

public class Desc {

	private String model;
	
	private String name;
	
	private Desc rel;

	private String comment;

	protected void init(){
		
	}
	
	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Desc getRel() {
		return rel;
	}

	public void setRel(Desc rel) {
		this.rel = rel;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	
}
