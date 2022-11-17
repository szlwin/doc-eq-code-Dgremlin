package dec.external.datasource.sql.convert.common;

public class DeleteSQLConvert extends CommonConvert{
	
	protected String convert(){
		String sql = this.convertParam.getSql();
		int index = sql.indexOf("from");
		sql = "delete " + sql.substring(index);
		this.convertParam.setSql(sql);
		return super.convert();
	}
}
