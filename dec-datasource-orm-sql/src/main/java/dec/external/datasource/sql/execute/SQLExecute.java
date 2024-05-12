package dec.external.datasource.sql.execute;

import java.sql.Connection;
import java.util.Collection;

import dec.core.datasource.dom.DataInfo;
import dec.core.datasource.execute.Execute;
import dec.core.datasource.execute.dom.ExecuteInfo;

//import com.orm.common.execute.Execute;
//import com.orm.sql.dom.DataInfo;
//import com.orm.sql.dom.ExecuteInfo;

public interface SQLExecute extends Execute<Collection<DataInfo>, ExecuteInfo, Connection> {

}
