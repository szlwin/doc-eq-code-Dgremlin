package com.orm.common.check.rule;

import smarter.common.express.check.PatternCheck;
import smarter.common.express.check.PropertyCheck;
import smarter.common.express.check.RuleCheck;

import com.orm.common.xml.util.Constanst;

public class CheckFactory {

	public static RuleCheck<?> getCheck(String type){
		if(type.equals(Constanst.RULE_TYPE_CHECK)){
			return new PropertyCheck();
		}
		
		if(type.equals(Constanst.RULE_TYPE_CHECK_PATTERN)){
			return new PatternCheck();
		}
		return null;
	}
}
