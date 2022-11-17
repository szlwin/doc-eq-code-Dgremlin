package dec.context.parse.xml.parse.data;

import dec.context.parse.xml.parse.AbstarctElementsParser;
import dec.context.parse.xml.parse.AbstractFileParser;
import dec.core.context.config.model.data.Data;

//import com.orm.common.xml.model.data.Data;
//import com.orm.common.xml.parse.AbstarctElementsParser;
//import com.orm.common.xml.parse.AbstractFileParser;

public class DataFileParse extends AbstractFileParser{

	@Override
	protected AbstarctElementsParser getParser() {
		return new DataParser();
	}
	
	@Override
	protected String getNodeNme() {
		return Data.NODE_NAME;
	}

}
