package com.orm.model.execute.rule.data;

import java.util.Map;

import com.orm.common.xml.model.data.Data;
import com.orm.common.xml.model.relation.Relation;

import com.orm.context.data.DataUtil;
import com.orm.sql.dom.ExecuteInfo;

public class InsertExecute extends AbstractDataExecute{

	/*@Override
	protected boolean executeBysql(String sql, Object value)
			throws SQLException {
		return false;
	}*/

	/*@Override
	protected SimpleSQLExecute createExecute() {
		
		return new InsertSQLExecute(this.con,dataSource);
	}*/

	@Override
	protected void doAfter(Object result) {
		Object keyValue = ((ExecuteInfo)result).getKeyValue();
		
		if(keyValue == null)
			return;

		Map value = (Map) this.value;
		String dataName = null;
		
		
		boolean isMain = viewData.getTargetMain().getName().equals(propertyName);
		
		if(isMain){
			dataName = viewData.getTargetMain().getName();
		}else{
			if((value).containsKey(propertyName)){
				Relation relationView = viewData.getRelationInfo().getRelationByPropertyName1(propertyName);

				dataName = relationView.getViewProperty().getViewData().getTargetMain().getName();
			}else{
				
				return;
			}
		}
		
		
		
		//Util.getRelationView(viewName, propertyName);
		//viewData.getRelationInfo().getRelation(name);
		
		/*
		if(relationView == null){
			dataName = propertyName;
		}else{
			dataName = relationView.getViewProperty().getViewData().getTargetMain().getName();
		}*/
		
		
		Data data = DataUtil.getConfigInfo().getData(dataName);
		
		//if(type.equals("insert")){
		//if(simpleSQLExecute instanceof InsertSQLExecute){
		String key = data.getTableInfo()
				.getTable(dataSource).getPropertyKey();//Util.getIdKey(data, dataSource);
		((Map<String,Object>)propertyValue).put(key, keyValue);
		//}
		
	}

}
