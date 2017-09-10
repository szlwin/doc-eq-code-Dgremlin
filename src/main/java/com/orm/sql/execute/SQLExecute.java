package com.orm.sql.execute;

import java.sql.Connection;
import java.util.Collection;

import com.orm.common.execute.Execute;
import com.orm.sql.dom.DataInfo;
import com.orm.sql.dom.ExecuteInfo;

public interface SQLExecute extends Execute<Collection<DataInfo>, ExecuteInfo, Connection> {

}
