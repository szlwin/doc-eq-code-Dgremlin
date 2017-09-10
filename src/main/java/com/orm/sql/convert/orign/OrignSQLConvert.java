package com.orm.sql.convert.orign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;

import com.orm.context.data.BaseData;
import com.orm.sql.convert.SqlConvert;
import com.orm.sql.dom.ConvertInfo;
import com.orm.sql.dom.ConvertParam;
import com.orm.sql.dom.DataInfo;


public class OrignSQLConvert implements SqlConvert{

	protected Map<String,Object> paramMap;
	
	protected boolean isHasParam;
	
	protected String sql;

	protected Collection<DataInfo> dataInfoSet;
	
	public String convert(String sql,Map<String,Object> paramMap) {
		this.paramMap = paramMap;
		isHasParam = paramMap != null && !paramMap.isEmpty();
		this.sql = sql;
		return this.convert();
	}

	protected String convert(){
		
		if(isHasParam)
			this.dataInfoSet = new ArrayList(20);
		
		StringBuffer sqlTotalBuffer = new StringBuffer();
		
		StringTokenizer token = new StringTokenizer(sql," ,()*+-/<>!=[]",true);
		boolean startFlag = false;
		boolean endFlag = false;
		boolean hasReplaceParam = true;
		
		StringBuffer sqlBuffer = new StringBuffer();
		
		while(token.hasMoreTokens()){
			String str = token.nextToken();
		
			if(str.startsWith("#")){
				if(hasReplaceParam)
					hasReplaceParam = replaceParam(str);
					if(hasReplaceParam)
						sqlBuffer.append("?");
				}else if(str.startsWith("[")){
					startFlag = true;
					endFlag = false;
				}else if(str.startsWith("]")){
					endFlag = true;
				}else
					sqlBuffer.append(str);
			
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
		
		if(!paramMap.containsKey(str))
			return false;
		DataInfo dataInfo = new DataInfo();

		dataInfo.setValue(paramMap.get(str));
		this.dataInfoSet.add(dataInfo);
		return true;
	}

	public Collection<DataInfo> get() {
		return dataInfoSet;
	}
	
	public void reset(){
		if(dataInfoSet != null){
			dataInfoSet.clear();
		}
		
	}

	public String convert(BaseData baseData) {
		// TODO Auto-generated method stub
		return null;
	}

	public ConvertInfo convert(ConvertParam convertParam) {
		ConvertInfo convertInfo = new ConvertInfo();
		String sql = convert(convertParam.getSql(),(Map<String, Object>) convertParam.getData());
		convertInfo.setCmd(sql);
		convertInfo.setDataInfo(dataInfoSet);
		return convertInfo;
	}

	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDataSource(String dataSource) {
		// TODO Auto-generated method stub
		
	}

	public String getDataSource() {
		// TODO Auto-generated method stub
		return null;
	}
}
