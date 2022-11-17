package dec.external.datasource.sql.convert.common;


public class QuerySQLConvert extends CommonConvert{
	
	public QuerySQLConvert() {
		this.isAddColumnTag = true;
	}
	
	public void setReplaceColumnFlag(boolean replaceColumnFlag){
		this.isAddColumnTag = replaceColumnFlag;
	}
	

}
