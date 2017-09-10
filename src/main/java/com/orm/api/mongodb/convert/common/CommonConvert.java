package com.orm.api.mongodb.convert.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.orm.api.dom.ConditionInfo;
import com.orm.api.dom.FieldInfo;
import com.orm.api.mongodb.convert.AbstractMongoDBConvert;
import com.orm.api.mongodb.execute.CompareFlag;
import com.orm.common.xml.model.data.Column;
import com.orm.context.data.DataUtil;
import com.orm.context.data.NullData;
import com.orm.sql.dom.DataInfo;
import com.orm.sql.dom.TableTag;
import com.orm.sql.util.Util;

public class CommonConvert extends AbstractMongoDBConvert{
	
	protected Map<String,TableTag> tagMap;
	
	protected Map<String,TableTag> tagTableMap;
	
	protected Map<String,Object> paramMap;
	
	protected boolean isHasParam;
	
	protected boolean isAddColumnTag = false;
	

	private static Map<String,String> cmdMap = new HashMap<String,String>();
	
	static{
		cmdMap.put("get", "findOne");
		cmdMap.put("update", "update");
		cmdMap.put("delete", "remove");
		cmdMap.put("query", "find");
	}
	//private String sql;
	
	public CommonConvert() {
		tagMap = new HashMap<String,TableTag>(20);
		tagTableMap = new HashMap<String,TableTag>(20);
	}

	/*protected String convert(String sql, Map<String,Object> paramMap) {
		this.sql = sql;
		this.paramMap = paramMap;
		return this.convert();
	}*/
	
	protected void getTag(String sql){

		StringTokenizer token = new StringTokenizer(sql," ,()*+-/<>!=[]",false);
		
		while(token.hasMoreElements()){
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
		}
		
	}
	
	private TableTag convertStingToTabelTag(String tag,String sql){
		
		char[] sqlChar = sql.toCharArray();
		
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
		
		String dataName = strBuffer.reverse().toString();
		String tableName =  Util.getTableName(dataName, dataSourceName);
		
		tableTag.setDataName(dataName);
		tableTag.setTableName(tableName);
		return tableTag;
	}
	
	@SuppressWarnings("unchecked")
	protected String convert(){
		this.dataInfoSet = new ArrayList<DataInfo>();
		
		this.fieldInfo = new ArrayList<FieldInfo>();
		this.conditionInfo = new ArrayList<ConditionInfo>();
		
		String sql = this.convertParam.getSql();
		this.paramMap = (Map<String, Object>) this.convertParam.getData();
		isHasParam = paramMap != null && !paramMap.isEmpty();
		
		getTag(sql);
		
		replaceTag(sql);
		
		return cmdMap.get(this.convertParam.getType());
	}

	@SuppressWarnings("unchecked")
	protected String replaceTag(String sql){
		
		if(isHasParam)
			this.dataInfoSet = new ArrayList();
		
		StringBuffer sqlTotalBuffer = new StringBuffer();
		
		StringTokenizer token = new StringTokenizer(sql," ,()*+-/<>!=[]",true);
		boolean startFlag = false;
		boolean endFlag = false;
		boolean hasReplaceParam = true;
		//boolean notAddFalg = false;
		//boolean isAddas = false;
		
		boolean isStartWhere = false;
		boolean isData = false;
		boolean isAddData = false;
		//StringBuffer sqlBuffer = new StringBuffer();
		String propertyStr = null;
		
		String compareFlag = null;
		String type = this.convertParam.getType();
		
		boolean isField = type.equals("get") || type.equals("query");
		boolean isUpdate =	type.equals("update");
		int cInfoCount = 0;
		while(token.hasMoreTokens()){
			String str = token.nextToken();
			if(str.equals(" ")){
				continue;
			}
			int index = str.indexOf(".");
			if(index > 0 && index != str.length()-1 && !str.startsWith("#")){
				String strArr[] = str.split("\\.");
				
				propertyStr = strArr[1];
				
				TableTag tag = tagMap.get(strArr[0]);
						
				Map<String,Column> dataInfoMap = Util.convert(tag.getDataName(), dataSourceName);
				
				/*
				sqlBuffer.append(strArr[0]);
				
				sqlBuffer.append(".");
				
				Column column = dataInfoMap.get(propertyStr);
				
				sqlBuffer.append(column.getName());*/
				
				Column column = dataInfoMap.get(propertyStr);
				
				if(isStartWhere){
					ConditionInfo cInfo = new ConditionInfo();
					cInfo.setColumn(column.getName());
					this.conditionInfo.add(cInfo);
				}else if(isUpdate && !isStartWhere){
					isData = true;
					DataInfo dataInfo = new DataInfo();
					dataInfo.setColumn(column.getName());
					dataInfo.setProterty(column.getRefproperty());
					this.dataInfoSet.add(dataInfo);
				}else if(isField && !isStartWhere){
					FieldInfo fieldInfo = new FieldInfo();
					fieldInfo.setColumn(column.getName());
					fieldInfo.setProperty(column.getRefproperty());
					this.fieldInfo.add(fieldInfo);
				}
				
				//isAddas = true;
				
				
			}else
			{
				if(tagTableMap.containsKey(str)){
					TableTag tag = tagTableMap.get(str);
					this.dataName = tag.getTableName();
				}else if(isData && "=".equals(str)){
					isAddData = true;
				}else if(isAddData){
					replaceParam(str,isStartWhere,compareFlag);
					isAddData = false;
					isData = false;
				}
				else if(str.startsWith("#")){
					if(hasReplaceParam)
						hasReplaceParam = replaceParam(str,isStartWhere,compareFlag);
					if(hasReplaceParam)
						cInfoCount++;
					//	sqlBuffer.append("?");
				}else if(str.startsWith("[")){
					startFlag = true;
					endFlag = false;
				}else if(str.startsWith("]")){
					endFlag = true;
				}else{
					String tempStr = str.toUpperCase();
					if(tempStr.equals("WHERE")){
						isStartWhere = true;
					}else if(CompareFlag.getFlag(str)!= null && isStartWhere){
						compareFlag = str;
						isAddData = true;
					}
						
				}/*else{
					/*
					if(str.toLowerCase().equals("as")){
						isAddas = false;
					}else if(isAddColumnTag 
							&& !notAddFalg 
							&& isAddas
							&& !" ".equals(str)){
						sqlBuffer.append(" as ");
						sqlBuffer.append(propertyStr);
						sqlBuffer.append(" ");
					}
					
					sqlBuffer.append(str);
					
					String tempStr = str.toUpperCase();
					//if(tempStr.equals("WHERE") || tempStr.equals("FROM")){
					//	notAddFalg = true;
					//}
					
					//if(tempStr.equals("SELECT")){
					//	notAddFalg = false;
					//}
				}*/
					
			}
			
			if(startFlag){
				if(endFlag){
					if(!hasReplaceParam){
						for(int i = 0; i < cInfoCount;i++ ){
							((ArrayList<ConditionInfo>)conditionInfo).remove(conditionInfo.size()-1);
						}
						
					}
					//if(hasReplaceParam){
					//	sqlTotalBuffer.append(sqlBuffer.toString());
					//}
					//sqlBuffer.delete(0, sqlBuffer.length());
					hasReplaceParam = true;
					startFlag = false;
					cInfoCount = 0;
				}
			}/*else{
				sqlTotalBuffer.append(sqlBuffer.toString());
				sqlBuffer.delete(0, sqlBuffer.length());
			}*/
		}
		
		return sqlTotalBuffer.toString();
		
	}
	
	private boolean replaceParam(String str,boolean isStartWhere,String compareFlag){
		if(!isHasParam)
			return false;
		
		Object value = null;
		if(str.startsWith("#")){
			str = str.substring(1);
			value = DataUtil.getValueByKey(str, paramMap);

			if(value == null)
				return false;
			
			if(value instanceof NullData){
				value = null;
			}
		}
		else if(str.startsWith("'")){
			value = str.substring(1,str.length()-1);
		}else{
			value =str;
		}

		
		String type = this.convertParam.getType();
		
		if(isStartWhere){
			ConditionInfo cInfo = ((ArrayList<ConditionInfo>)conditionInfo).get(conditionInfo.size()-1);
			cInfo.setValue(value);
			cInfo.setFlag(compareFlag);
		}else{
			if(type.equals("update")){
				DataInfo dataInfo = ((ArrayList<DataInfo>)dataInfoSet).get(dataInfoSet.size()-1);
				dataInfo.setValue(value);
			}
		}

		
		


		//this.dataInfoSet.add(dataInfo);
		return true;
	}


}
