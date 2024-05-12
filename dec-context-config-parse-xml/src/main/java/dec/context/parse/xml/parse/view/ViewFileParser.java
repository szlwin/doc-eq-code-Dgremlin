package dec.context.parse.xml.parse.view;

import dec.context.parse.xml.parse.AbstarctElementsParser;
import dec.context.parse.xml.parse.AbstractFileParser;
import dec.core.context.config.model.view.ViewData;

//import com.orm.common.xml.model.view.ViewData;
//import com.orm.common.xml.parse.AbstarctElementsParser;
//import com.orm.common.xml.parse.AbstractFileParser;

public class ViewFileParser extends AbstractFileParser {

	@Override
	protected AbstarctElementsParser getParser() {
		return new ViewParser();
	}

	@Override
	protected String getNodeNme() {
		return ViewData.NODE_NAME;
	}



}
