package com.orm.common.xml.parse.relation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import com.orm.common.xml.exception.XMLParseException;
import com.orm.common.xml.model.relation.ManyRelation;
import com.orm.common.xml.model.relation.OneRelation;
import com.orm.common.xml.model.relation.Relation;
import com.orm.common.xml.parse.AbstarctElementsParser;
import com.orm.common.xml.util.Constanst;

public class RelationParser extends AbstarctElementsParser{

	Log log = LogFactory.getLog(RelationParser.class);
	
	public Relation parse(Element element) throws XMLParseException {
		String type = element.attributeValue(Relation.TYPE);
		
		Relation relation = null;
		
		if(type.equals(Constanst.RELATION_TYPE_ONE_TO_ONE))
		{
			relation = parseOntoOneRelation(element);
		}else
		{
			relation = parseOntoManyRelation(element);
		}
		log.info("Load the relation:"+relation.getName()+" success!");
		return relation;
	}
	
	private Relation parseOntoOneRelation(Element element)
	{
		OneRelation relation = new OneRelation();
		
		relation.setType(element.attributeValue(Relation.TYPE));
		relation.setName(element.attributeValue(Relation.NAME));
		
		relation.setOneKey(element.attributeValue(OneRelation.ONE_KEY));
		relation.setOneRef(element.attributeValue(OneRelation.ONE_REF));
		
		relation.setOneMainkey(element.attributeValue(OneRelation.ONE_MAIN_KEY));
		relation.setOneMainRef(element.attributeValue(OneRelation.ONE_MAIN_REF));
		
		return relation;
	}
	
	private Relation parseOntoManyRelation(Element element)
	{
		ManyRelation relation = new ManyRelation();
		
		relation.setType(element.attributeValue(Relation.TYPE));
		relation.setName(element.attributeValue(Relation.NAME));
		
		relation.setOneKey(element.attributeValue(ManyRelation.ONE_KEY));
		relation.setOneRef(element.attributeValue(ManyRelation.ONE_REF));
		
		relation.setManyKey(element.attributeValue(ManyRelation.MANY_KEY));
		relation.setManyRef(element.attributeValue(ManyRelation.MANY_REF));
		
		return relation;
	}

}
