package com.orm.sql.convert.container;

import com.orm.common.convert.container.ConvertContainer;
import com.orm.sql.dom.ConvertInfo;
import com.orm.sql.dom.ConvertParam;

public interface SqlConvertContainer extends ConvertContainer<ConvertParam, ConvertInfo>{
	//public ConvertInfo convert(BaseData data);
	
	//public ConvertInfo convert(String sql,Map<String,Object> paramMap);
	
	public ConvertInfo convert(ConvertParam e);
}
