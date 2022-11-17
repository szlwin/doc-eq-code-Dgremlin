package dec.context.parse.xml.parse.relation;

import dec.context.parse.xml.parse.AbstarctElementsParser;
import dec.context.parse.xml.parse.AbstractFileParser;
import dec.core.context.config.model.relation.Relation;

//import com.orm.common.xml.model.relation.Relation;
//import com.orm.common.xml.parse.AbstarctElementsParser;
//import com.orm.common.xml.parse.AbstractFileParser;

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
