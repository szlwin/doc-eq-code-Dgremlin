package com.orm.api.mongodb.execute;

import java.util.Collection;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.orm.sql.dom.DataInfo;
import com.orm.sql.dom.ExecuteInfo;
import com.orm.api.execute.ApiExecute;

public interface MongoDBExecute extends ApiExecute<Collection<DataInfo>, ExecuteInfo, DB>{

}
