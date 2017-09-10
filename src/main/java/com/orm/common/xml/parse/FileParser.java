package com.orm.common.xml.parse;
import com.orm.common.xml.exception.XMLParseException;

public interface FileParser<E> {

	public E parse(String filePath) throws XMLParseException;
}
