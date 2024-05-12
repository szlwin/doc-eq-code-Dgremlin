package dec.context.parse.xml.parse;


import org.dom4j.Element;

import dec.context.parse.xml.exception.XMLParseException;
import dec.core.context.config.model.config.data.ConfigBaseData;



public abstract class AbstarctElementsParser implements ElementParser<ConfigBaseData>{

	public abstract ConfigBaseData parse(Element element) throws XMLParseException;

}
