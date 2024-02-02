package dec.core.model.execute.rule.data;

import dec.core.context.config.model.view.ViewData;
import dec.core.datasource.connection.DataConnection;
import dec.core.datasource.execute.exception.ExecuteException;

/*import dec.core.common.execute.exception.ExecuteException;
import dec.core.common.xml.model.view.ViewData;
import dec.core.connection.DataConnection;*/

public interface Execute<E> {

	boolean execute() throws ExecuteException;
	
	void setSql(String sql);
	
	void setValue(E e);
	
	void setPropertyName(String name);
	
	void setViewData(ViewData viewData);
	
	//public void setConnection(Connection con);
	
	void setDataSource(String name);
	
	void setDataConnection(DataConnection<?, ?> con);
	
	void setType(String type);
}
