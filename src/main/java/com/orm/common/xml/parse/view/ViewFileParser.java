package com.orm.common.xml.parse.view;


import com.orm.common.xml.model.view.ViewData;
import com.orm.common.xml.parse.AbstarctElementsParser;
import com.orm.common.xml.parse.AbstractFileParser;

public class ViewFileParser extends AbstractFileParser {

	@Override
	protected AbstarctElementsParser getParser() {
		return new ViewParser();
	}

	@Override
	protected String getNodeNme() {
		return ViewData.NODE_NAME;
	}



}
