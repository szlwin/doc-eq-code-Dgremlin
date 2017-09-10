package com.orm.common.xml.model.relation;


/**
 * 
 * @author szl
 *
 */
public class ManyRelation extends Relation{

	public static final String MANY_REF = "many-ref";
	
	public static final String MANY_KEY = "many-key";
	
	public static final String ONE_REF = "one-ref";
	
	public static final String ONE_KEY = "one-key";
	
	private String oneRef;
	
	private String oneKey;
	
	private String manyRef;
	
	private String manyKey;

	public String getManyKey() {
		return manyKey;
	}

	
	/**
	 * 
	 * @param manyKey
	 */
	public void setManyKey(String manyKey) {
		this.manyKey = manyKey;
	}

	/**
	 * 
	 * @return
	 */
	public String getManyRef() {
		return manyRef;
	}

	public void setManyRef(String manyRef) {
		this.manyRef = manyRef;
	}

	public String getOneKey() {
		return oneKey;
	}

	public void setOneKey(String oneKey) {
		this.oneKey = oneKey;
	}

	public String getOneRef() {
		return oneRef;
	}

	public void setOneRef(String oneRef) {
		this.oneRef = oneRef;
	}
	
	
}
