package com.orm.sql.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import com.orm.sql.dom.DataInfo;

public class SQLUtil {

	public static void convert(PreparedStatement preState,Collection<DataInfo> dataSet) throws SQLException{
		if(dataSet == null || dataSet.isEmpty())
			return;
		
		int i = 1;
		
		Iterator<DataInfo> it = dataSet.iterator();
		
		while(it.hasNext()){
			DataInfo dataInfo = it.next();
			Object value = dataInfo.getValue();
			preState.setObject(i, value);
			i++;
		}
	}
	
	public static void convert(PreparedStatement preState,Collection<DataInfo> dataSet,String key) throws SQLException{
		int i = 1;
		
		Iterator<DataInfo> it = dataSet.iterator();
		
		while(it.hasNext()){
			DataInfo dataInfo = it.next();
			String column = dataInfo.getColumn();
			
			if(column.equals(key))
				continue;
			Object value = dataInfo.getValue();
			
			preState.setObject(i, value);
		}
	}
}
