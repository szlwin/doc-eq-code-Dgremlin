package com.orm.common.xml.model.relation;


public class OneRelation extends Relation{

	public static final String ONE_MAIN_REF = "one-main-ref";
	
	public static final String ONE_MAIN_KEY = "one-main-key";
	
	public static final String ONE_REF = "one-ref";
	
	public static final String ONE_KEY = "one-key";
	
	private String oneMainRef;
	
	private String oneMainkey;
	
	private String oneRef;
	
	private String oneKey;

	public String getOneKey() {
		return oneKey;
	}

	public void setOneKey(String oneKey) {
		this.oneKey = oneKey;
	}

	public String getOneMainkey() {
		return oneMainkey;
	}

	public void setOneMainkey(String oneMainkey) {
		this.oneMainkey = oneMainkey;
	}

	public String getOneMainRef() {
		return oneMainRef;
	}

	public void setOneMainRef(String oneMainRef) {
		this.oneMainRef = oneMainRef;
	}

	public String getOneRef() {
		return oneRef;
	}

	public void setOneRef(String oneRef) {
		this.oneRef = oneRef;
	}
	
	
}
