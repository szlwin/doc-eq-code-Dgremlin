package com.orm.model.execute.rule.common;

import smarter.common.express.check.RuleCheck;

import com.orm.common.check.rule.CheckFactory;
import com.orm.common.execute.exception.ExecuteException;
import com.orm.common.xml.model.rule.RuleCheckInfo;

public class CheckExecute extends AbstractRuleExecute{
	
	
	/*public CheckExecute(RuleCheckInfo checkInfo,Object value){
		this.checkInfo = checkInfo;
		this.value = value;
	}*/
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean execute() throws ExecuteException{
		RuleCheckInfo checkInfo = (RuleCheckInfo)this.ruleInfo;
		RuleCheck check = CheckFactory.getCheck(checkInfo.getType());
		
		check.setCheckValue(value);
		check.setPattern(checkInfo.getPattern());

		try {
			return check.check();
		} catch (smarter.common.exception.ExecuteExpection e) {
			throw new ExecuteException(e);
		}
	}
}
