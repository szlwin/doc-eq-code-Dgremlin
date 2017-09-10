package com.orm.common.xml.parse;

import org.dom4j.Element;

import com.orm.common.xml.exception.XMLParseException;

public interface ElementParser<E> {

	public E parse(Element element)throws XMLParseException;;
}
