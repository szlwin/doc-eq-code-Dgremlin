package com.orm.common.xml.parse.service;


import com.orm.common.xml.model.service.ServiceInfo;
import com.orm.common.xml.parse.AbstarctElementsParser;
import com.orm.common.xml.parse.AbstractFileParser;

public class ServiceFileParser extends AbstractFileParser {

	@Override
	protected AbstarctElementsParser getParser() {
		return new ServiceParser();
	}

	@Override
	protected String getNodeNme() {
		return ServiceInfo.NODE_NAME;
	}



}
