package dec.context.parse.xml.parse;

import org.dom4j.Element;

import dec.context.parse.xml.exception.XMLParseException;



public interface ElementParser<E> {

	public E parse(Element element)throws XMLParseException;;
}
