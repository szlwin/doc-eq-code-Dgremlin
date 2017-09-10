package com.orm.model.container;

import com.orm.model.execute.rule.exception.ExecuteRuleException;

public interface Container {

	public void load(ModelLoader modelLoader);
	
	public void execute() throws ExecuteRuleException;
	
	public ResultInfo getResult();
}
