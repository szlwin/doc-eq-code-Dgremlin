package dec.external.datasource.sql.mysql.execute.container.factory;

import dec.core.datasource.execute.container.ExecuteContainer;
import dec.core.datasource.execute.container.factory.ExecuteContainerFacory;
import dec.core.datasource.execute.dom.ExecuteInfo;
import dec.external.datasource.sql.dom.QueryInfo;
import dec.external.datasource.sql.mysql.execute.container.MySQLExecuteContainer;

public class MySQLExecuteContainerFactory implements ExecuteContainerFacory<QueryInfo, ExecuteInfo>{

	private MySQLExecuteContainer executeContainer = new MySQLExecuteContainer();

	@Override
	public ExecuteContainer<QueryInfo, ExecuteInfo> getExecuteContainer() {
		return executeContainer;
	}
	


}
