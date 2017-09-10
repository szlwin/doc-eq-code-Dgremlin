package com.orm.common.xml.parse;


import org.dom4j.Element;

import com.orm.common.xml.exception.XMLParseException;
import com.orm.common.xml.model.config.ConfigBaseData;

public abstract class AbstarctElementsParser implements ElementParser<ConfigBaseData>{

	public abstract ConfigBaseData parse(Element element) throws XMLParseException;

}
