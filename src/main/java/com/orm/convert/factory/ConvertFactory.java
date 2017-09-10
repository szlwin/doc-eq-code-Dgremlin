package com.orm.convert.factory;

import com.orm.common.convert.DataConvert;

public interface ConvertFactory {

	public DataConvert<?, ?> getConvert(int type);
}
