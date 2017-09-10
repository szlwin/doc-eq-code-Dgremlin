package com.orm.common.xml.parse.relation;

import com.orm.common.xml.model.relation.Relation;
import com.orm.common.xml.parse.AbstarctElementsParser;
import com.orm.common.xml.parse.AbstractFileParser;

public class RelationFileParser extends AbstractFileParser{

	@Override
	protected AbstarctElementsParser getParser() {
		return new RelationParser();
	}
	
	@Override
	protected String getNodeNme() {
		return Relation.NODE_NAME;
	}


}
