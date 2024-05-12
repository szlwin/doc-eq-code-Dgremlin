package dec.external.datasource.sql.convert;

import dec.core.datasource.convert.AbstractConvert;
import dec.external.datasource.sql.dom.ConvertInfo;
import dec.external.datasource.sql.dom.ConvertParam;

//import com.orm.common.convert.AbstractConvert;
//import com.orm.sql.dom.ConvertInfo;
//import com.orm.sql.dom.ConvertParam;

public abstract class AbstractSqlConvert extends AbstractConvert<ConvertParam ,ConvertInfo> implements SqlConvert {
	
	protected ConvertParam convertParam;
	
	//protected String name;
	
	protected String keyType;
	
	protected Object keyValue;
	
	public ConvertInfo convert(ConvertParam convertParam) {
		this.convertParam = convertParam;
		ConvertInfo convertInfo = new ConvertInfo();
		String sql = convert();
		convertInfo.setCmd(sql);
		convertInfo.setDataInfo(dataInfoSet);
		convertInfo.setKeyType(keyType);
		convertInfo.setKeyValue(keyValue);
		return convertInfo;
	}
	
	protected abstract String convert();
}
