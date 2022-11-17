package dec.external.datasource.sql.convert.container;

import dec.core.datasource.convert.container.ConvertContainer;
import dec.external.datasource.sql.dom.ConvertInfo;
import dec.external.datasource.sql.dom.ConvertParam;


//import com.orm.common.convert.container.ConvertContainer;
//import com.orm.sql.dom.ConvertInfo;
//import com.orm.sql.dom.ConvertParam;

public interface SqlConvertContainer extends ConvertContainer<ConvertParam, ConvertInfo>{
	//public ConvertInfo convert(BaseData data);
	
	//public ConvertInfo convert(String sql,Map<String,Object> paramMap);
	
	public ConvertInfo convert(ConvertParam e);
}
