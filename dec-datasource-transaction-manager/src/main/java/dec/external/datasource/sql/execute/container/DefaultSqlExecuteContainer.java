package dec.external.datasource.sql.execute.container;

import java.util.Map;

import dec.external.datasource.sql.dom.QueryInfo;
import dec.external.datasource.sql.execute.SQLExecute;

//import com.orm.sql.dom.QueryInfo;
//import com.orm.sql.execute.GetExecute;
///import com.orm.sql.execute.InsertExecute;
//import com.orm.sql.execute.QueryExecute;
//import com.orm.sql.execute.SQLExecute;
//import com.orm.sql.execute.UpdateExecute;
import dec.external.datasource.sql.execute.GetExecute;
import dec.external.datasource.sql.execute.InsertExecute;
import dec.external.datasource.sql.execute.QueryExecute;
import dec.external.datasource.sql.execute.UpdateExecute;
import javolution.util.FastMap;


public class DefaultSqlExecuteContainer extends AbstractSqlExecuteContainer{

	protected final static Map<String,Class<? extends SQLExecute>> executeMap 
		= new FastMap<String,Class<? extends SQLExecute>>();
	
	static{
		executeMap.put("get", GetExecute.class);
		executeMap.put("insert", InsertExecute.class);
		executeMap.put("query", QueryExecute.class);
		executeMap.put("update", UpdateExecute.class);
		executeMap.put("delete", UpdateExecute.class);
	}
	public void init() {
		
	}
	
	protected SQLExecute getExecute(QueryInfo e) {
		SQLExecute execute = null;
		try {
			execute = executeMap.get(e.getType()).newInstance();
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		} 
		
		return execute;
	}
}
