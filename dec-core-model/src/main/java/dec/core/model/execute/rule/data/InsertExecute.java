package dec.core.model.execute.rule.data;

import java.util.Map;

import dec.core.context.config.model.data.Data;
import dec.core.context.config.model.relation.Relation;
import dec.core.model.utils.DataUtil;

/*import dec.core.common.xml.model.data.Data;
import dec.core.common.xml.model.relation.Relation;
import dec.core.context.data.DataUtil;
import dec.core.sql.dom.ExecuteInfo;*/
import dec.core.datasource.execute.dom.ExecuteInfo;

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
