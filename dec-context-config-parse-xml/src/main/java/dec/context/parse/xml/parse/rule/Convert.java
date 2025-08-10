package dec.context.parse.xml.parse.rule;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import dec.core.context.config.model.view.RelationInfo;
import dec.core.context.config.model.view.ViewData;
import dec.core.context.config.model.view.ViewProperty;
import dec.core.context.config.model.view.ViewPropertyInfo;

//import com.orm.common.xml.model.view.RelationInfo;
//import com.orm.common.xml.model.view.ViewData;
//import com.orm.common.xml.model.view.ViewProperty;
//import com.orm.common.xml.model.view.ViewPropertyInfo;
//import com.orm.sql.dom.TableTag;

public class Convert {
	
	private String sql;
	
	private RelationInfo relationInfo;
	
	//private boolean isHasParam;
	
	private Map<String,TableTag> tagMap;
	
	private Map<String,TableTag> tagTableMap;
	
	private ViewData viewData;
	
	public Convert(String sql,ViewData viewData){
		this.sql = sql;
		
		this.viewData = viewData;
		relationInfo = viewData.getRelationInfo();

		tagMap = new HashMap<String,TableTag>(20);
		tagTableMap = new HashMap<String,TableTag>(20);
	}
	
	public String getSql(){
		return sql;
	}
	
	public String convert(){
		return convertSql();
	}
	/*
	public boolean isHasParam(){
		return isHasParam;
	}*/
	
	private String convertSql(){
		getTag(sql);
		
		String tempSql = replaceTag(sql);
		
		return tempSql;
	}
	
	protected void getTag(String sql){
		
		StringTokenizer token = new StringTokenizer(sql," ,()*+-/<>!=[]",false);
		String startStr = token.nextToken();
        if(startStr.equalsIgnoreCase("select") || startStr.equalsIgnoreCase("delete")){
			getTag(token);
        }else{
			getTagByUpdate(token);
			//isHasParam = false;
			/*while(token.hasMoreElements()){
				String partSql = token.nextToken();
				String[] partArray = partSql.split("\\.");

				if(partArray.length > 1){
					if(!tagMap.containsKey(partArray[0])){
						TableTag tableTag =  convertStingToTabelTag(partArray[0],sql);
						if(tableTag != null){
							tagMap.put(tableTag.getTagName(), tableTag);
							tagTableMap.put(tableTag.getDataName(), tableTag);
						}
					}

				}
			}*/
		}
	}

	private void getTagByUpdate(StringTokenizer token) {
		String partSql = null;
		int flag = 1;
		String dataName;
		String dataFlag;
		while(token.hasMoreElements()){
			partSql = token.nextToken();
			if(flag==2){
				if(partSql.equalsIgnoreCase("set")){
					break;
				}
				flag = 1;
			}
			if(flag == 1){
				dataName = partSql;
				partSql = token.nextToken();

				if(partSql.equalsIgnoreCase("as")){
					dataFlag = token.nextToken();
				}else{
					dataFlag = partSql;
				}

				if(!tagMap.containsKey(dataFlag)){
					TableTag tableTag =  convertStingToTabelTag(dataName,dataFlag);
					if(tableTag != null){
						tagMap.put(tableTag.getTagName(), tableTag);
						tagTableMap.put(tableTag.getDataName(), tableTag);
					}
				}
				flag = 2;
				continue;
			}
		}
	}

	private void getTag(StringTokenizer token){
		String partSql = null;
		int flag = 0;
		String dataName;
		String dataFlag;
		while(token.hasMoreElements()){
			partSql = token.nextToken();
			if(partSql.equalsIgnoreCase("from")){
				flag = 1;
				continue;
			}
			if(flag==2){
				if(partSql.equalsIgnoreCase("where")){
					break;
				}
				flag = 1;
			}
			if(flag == 1){
				dataName = partSql;
				partSql = token.nextToken();

				if(partSql.equalsIgnoreCase("as")){
					dataFlag = token.nextToken();
				}else{
					dataFlag = partSql;
				}

				if(!tagMap.containsKey(dataFlag)){
					TableTag tableTag =  convertStingToTabelTag(dataName,dataFlag);
					if(tableTag != null){
						tagMap.put(tableTag.getTagName(), tableTag);
						tagTableMap.put(tableTag.getDataName(), tableTag);
					}
				}
				flag = 2;
				continue;
			}
		}
	}

	private TableTag convertStingToTabelTag(String dataName,String tag){
		
		/*char[] sqlChar = sql.toCharArray();
		
		int index = sql.indexOf(" "+tag+" ") -1;
		
		if(index < 0)
			index =  sql.indexOf(" "+tag+",") -1;
		
		if(index < 0)
			return null;
		TableTag tableTag = new TableTag();

		tableTag.setTagName(tag);

		StringBuffer strBuffer = new StringBuffer();
		
		while(true){
			char tempChar = sqlChar[index];
			
			if(tempChar != ' ' && tempChar != ','){
				strBuffer.append(tempChar);
			}
				
			else{
				if(strBuffer.toString().toLowerCase().equals("sa")){
					strBuffer.delete(0, 2);
				}else{
					break;
				}
			}
			index--;
		}
		
		String propertyName = strBuffer.reverse().toString();*/

		TableTag tableTag = new TableTag();

		tableTag.setTagName(tag);
		String tableName =  getTableName(dataName);//relationInfo.getRelation(propertyName).getData();
		
		tableTag.setDataName(dataName);
		tableTag.setTableName(tableName);
		return tableTag;
	}
	
	protected String replaceTag(String sql){
		
		StringTokenizer token = new StringTokenizer(sql," ,()*+-/<>!=[]",true);

		StringBuffer sqlBuffer = new StringBuffer();
		
		while(token.hasMoreTokens()){
			String str = token.nextToken();
			int index = str.indexOf(".");
			if(index > 0 && index != str.length()-1 && !str.startsWith("#")){
				String strArr[] = str.split("\\.");
				
				String propertyStr = strArr[1];
				
				
				TableTag tag = tagMap.get(strArr[0]);
						
				sqlBuffer.append(strArr[0]);
				
				sqlBuffer.append(".");
				addColumn(sqlBuffer,propertyStr,tag);
				
				//Map<String,RelationProperty> dataInfoMap = relationInfo.getRelation(tag.getDataName()).getValues();
				
				//RelationProperty property = dataInfoMap.get(propertyStr);
				
				//sqlBuffer.append(property.getRefProperty());
					
			}else
			{

				if(tagTableMap.containsKey(str)){
					TableTag tag = tagTableMap.get(str);
					sqlBuffer.append(tag.getTableName());
				}else{
					sqlBuffer.append(str);
				}
				//else if(str.startsWith("#")){
				//	isHasParam = true;
				//}
			}
			
		}
		
		return sqlBuffer.toString();
		
	}

	private String getTableName(String name){
		if(name.equals(viewData.getName()))
			return viewData.getTargetMain().getName();
		else
			return relationInfo.getRelationByPropertyName1(name)
					.getViewProperty()
					.getViewData()
					.getTargetMain().getName();
	}
	
	private void addColumn(StringBuffer sqlBuffer,String propertyStr,TableTag tag){
		if(tag.getDataName().equals(viewData.getName()))
		{
			ViewPropertyInfo viewPropertyInfo = viewData.getViewPropertyInfo();
			ViewProperty viewProperty = viewPropertyInfo.getProperty().get(propertyStr);
			
			sqlBuffer.append(viewProperty.getRefProperty());
		}else{
			Map<String,ViewProperty> dataInfoMap = relationInfo.getRelationByPropertyName1(tag.getDataName())
					.getViewProperty()
					.getViewData()
					.getViewPropertyInfo()
					.getProperty();
			
			ViewProperty property = dataInfoMap.get(propertyStr);
			
			sqlBuffer.append(property.getRefProperty());
		}

	}
}
