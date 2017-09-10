package com.orm.api.convert;

import com.orm.common.convert.DataConvert;
import com.orm.api.dom.ApiConvertInfo;
import com.orm.sql.dom.ConvertParam;

public interface ApiConvert extends DataConvert<ConvertParam ,ApiConvertInfo>{

	public ApiConvertInfo convert(ConvertParam convertParam);
}
