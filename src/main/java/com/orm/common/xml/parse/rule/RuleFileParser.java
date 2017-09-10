package com.orm.common.xml.parse.rule;

import com.orm.common.xml.model.rule.RuleViewInfo;
import com.orm.common.xml.parse.AbstarctElementsParser;
import com.orm.common.xml.parse.AbstractFileParser;

public class RuleFileParser extends AbstractFileParser{

	@Override
	protected AbstarctElementsParser getParser() {
		return new RuleParser();
	}

	@Override
	protected String getNodeNme() {
		return RuleViewInfo.NODE_NAME;
	}

}
