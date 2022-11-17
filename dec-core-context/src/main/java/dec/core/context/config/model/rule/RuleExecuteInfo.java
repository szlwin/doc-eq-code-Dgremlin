package dec.core.context.config.model.rule;

public class RuleExecuteInfo extends RuleDefineInfo{
	
	public static String SQL = "sql";
	
	private String sql;

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}
}
