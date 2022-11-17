package dec.context.parse.xml.parse;

import dec.context.parse.xml.exception.XMLParseException;

public interface FileParser<E> {

	public E parse(String filePath) throws XMLParseException;
}
