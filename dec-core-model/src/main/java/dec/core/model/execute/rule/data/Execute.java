package dec.core.model.execute.rule.data;

import dec.core.context.config.model.view.ViewData;
import dec.core.datasource.connection.DataConnection;
import dec.core.datasource.execute.exception.ExecuteException;

/*import dec.core.common.execute.exception.ExecuteException;
import dec.core.common.xml.model.view.ViewData;
import dec.core.connection.DataConnection;*/

public interface Execute<E> {

	public boolean execute() throws ExecuteException;
	
	public void setSql(String sql);
	
	public void setValue(E e);
	
	public void setPropertyName(String name);
	
	public void setViewData(ViewData viewData);
	
	//public void setConnection(Connection con);
	
	public void setDataSource(String name);
	
	public void setDataConnection(DataConnection<?, ?> con);
	
	public void setType(String type);
}
