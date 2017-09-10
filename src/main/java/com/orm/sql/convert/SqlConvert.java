package com.orm.sql.convert;

import com.orm.common.convert.DataConvert;
import com.orm.sql.dom.ConvertInfo;
import com.orm.sql.dom.ConvertParam;

public interface SqlConvert extends DataConvert<ConvertParam ,ConvertInfo>{
	
	public ConvertInfo convert(ConvertParam convertParam);

}
