package dec.external.datasource.sql.convert.common;

import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import dec.core.context.config.model.data.Column;
import dec.core.context.data.NullData;
import dec.core.datasource.dom.DataInfo;
import dec.external.datasource.sql.collections.list.SimpleList;
import dec.external.datasource.sql.convert.AbstractSqlConvert;
import dec.external.datasource.sql.dom.TableTag;
import dec.external.datasource.sql.utils.Util;
import javolution.util.FastMap;

/*import com.orm.common.xml.model.data.Column;
import com.orm.context.data.DataUtil;
import com.orm.context.data.NullData;
import com.orm.sql.convert.AbstractSqlConvert;
import com.orm.sql.dom.DataInfo;
import com.orm.sql.dom.TableTag;
import com.orm.sql.util.Util;*/

public class CommonConvert extends AbstractSqlConvert{
	
	protected Map<String,TableTag> tagMap;
	
	protected Map<String,TableTag> tagTableMap;
	
	protected Map<String,Object> paramMap;
	
	protected boolean isHasParam;
	
	protected boolean isAddColumnTag = false;
	//private String sql;
	
	public CommonConvert() {
		tagMap = new FastMap<String,TableTag>();
		tagTableMap = new FastMap<String,TableTag>();
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
		String sql = this.convertParam.getSql();
		this.paramMap = (Map<String, Object>) this.convertParam.getData();
		isHasParam = paramMap != null && !paramMap.isEmpty();
		
		getTag(sql);
		
		String tempSql = replaceTag(sql);
		
		return tempSql;
	}

	protected String replaceTag(String sql){

		if(isHasParam)
			this.dataInfoSet = new SimpleList<>();
		
		StringBuffer sqlTotalBuffer = new StringBuffer(sql.length());
		
		StringTokenizer token = new StringTokenizer(sql," ,()*+-/<>!=[]",true);
		boolean startFlag = false;
		boolean endFlag = false;
		boolean hasReplaceParam = true;
		boolean notAddFalg = false;
		boolean isAddas = false;
		StringBuffer sqlBuffer = new StringBuffer();
		String propertyStr = null;
		while(token.hasMoreTokens()){
			String str = token.nextToken();
			
			int index = str.indexOf(".");
			if(index > 0 && index != str.length()-1 && !str.startsWith("#")){
				String strArr[] = str.split("\\.");
				
				propertyStr = strArr[1];
				
				TableTag tag = tagMap.get(strArr[0]);
						
				Map<String,Column> dataInfoMap = Util.convert(tag.getDataName(), dataSourceName);
						
				sqlBuffer.append(strArr[0]);
				
				sqlBuffer.append(".");
				
				Column column = dataInfoMap.get(propertyStr);
				
				sqlBuffer.append(column.getName());
				isAddas = true;
				
				
			}else
			{
				if(tagTableMap.containsKey(str)){
					TableTag tag = tagTableMap.get(str);
					sqlBuffer.append(tag.getTableName());
				}
				else if(str.startsWith("#")){
					if(hasReplaceParam)
						hasReplaceParam = replaceParam(str);
					if(hasReplaceParam)
						sqlBuffer.append("?");
				}else if(str.startsWith("[")){
					startFlag = true;
					endFlag = false;
				}else if(str.startsWith("]")){
					endFlag = true;
				}else{
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
					if(tempStr.equals("WHERE") || tempStr.equals("FROM")){
						notAddFalg = true;
					}
					
					if(tempStr.equals("SELECT")){
						notAddFalg = false;
					}
				}
					
			}
			
			if(startFlag){
				if(endFlag){
					if(hasReplaceParam){
						sqlTotalBuffer.append(sqlBuffer.toString());
					}
					sqlBuffer.delete(0, sqlBuffer.length());
					hasReplaceParam = true;
					startFlag = false;
				}
			}else{
				sqlTotalBuffer.append(sqlBuffer.toString());
				sqlBuffer.delete(0, sqlBuffer.length());
			}
		}
		
		return sqlTotalBuffer.toString();
		
	}
	
	private boolean replaceParam(String str){
		if(!isHasParam)
			return false;
		
		str = str.substring(1);
		
		Object value = Util.getValueByKey(str, paramMap);

		if(value == null)
			return false;
		
		DataInfo dataInfo = new DataInfo();
		
		if(value instanceof NullData){
			dataInfo.setValue(null);
		}else{
			dataInfo.setValue(value);
		}
		
		this.dataInfoSet.add(dataInfo);
		return true;
	}


}
