package dec.external.datasource.sql.convert.container;


import dec.external.datasource.sql.convert.SqlConvert;
import dec.external.datasource.sql.dom.ConvertInfo;
import dec.external.datasource.sql.dom.ConvertParam;

//import com.orm.sql.convert.SqlConvert;
//import com.orm.sql.dom.ConvertInfo;
//import com.orm.sql.dom.ConvertParam;

public abstract class AbstractSqlConvertContainer implements SqlConvertContainer{

	public ConvertInfo convert(ConvertParam e) {
		SqlConvert convert = getConvert(e);
		return convert.convert(e);
	}
/*
	public ConvertInfo convert(BaseData data) {
		ConvertParam convertParam = new ConvertParam();
		convertParam.setData(data);
		return convert(convertParam);
	}

	public ConvertInfo convert(String sql, Map<String, Object> paramMap) {
		ConvertParam convertParam = new ConvertParam();
		convertParam.setSql(sql);
		convertParam.setData(paramMap);
		return convert(convertParam);
	}*/
	
	protected abstract SqlConvert getConvert(ConvertParam e);
	
}
