package com.orm.model.execute.rule.exception;

public class ExecuteRuleException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private String ruleName;
	
	private String con;
	
	public ExecuteRuleException(){
		
	}

	public ExecuteRuleException(Exception e,String ruleName,String con){
		super("rule: "+ruleName+", con: "+con+", error: "+e.getMessage(),e);
		this.ruleName = ruleName;
		this.con = con;
	}
	
	public ExecuteRuleException(String msg){
		super(msg);
	}
	public ExecuteRuleException(String msg,String ruleName,String con){
		super("rule: "+ruleName+", con: "+con+", error: "+msg);
		this.ruleName = ruleName;
		this.con = con;
	}

	public String getRuleName() {
		return ruleName;
	}

	public String getCon() {
		return con;
	}
	
	
}
