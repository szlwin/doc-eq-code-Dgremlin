package dec.external.datasource.sql.convert;

import dec.core.datasource.convert.DataConvert;
import dec.external.datasource.sql.dom.ConvertInfo;
import dec.external.datasource.sql.dom.ConvertParam;

public interface SqlConvert extends DataConvert<ConvertParam ,ConvertInfo>{
	
	public ConvertInfo convert(ConvertParam convertParam);

}
