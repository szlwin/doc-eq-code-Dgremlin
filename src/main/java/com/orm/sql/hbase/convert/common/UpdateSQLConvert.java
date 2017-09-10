package com.orm.sql.hbase.convert.common;

import java.util.Map;
import java.util.StringTokenizer;

import com.orm.common.xml.model.data.Data;
import com.orm.common.xml.model.data.DataProperty;
import com.orm.sql.convert.common.CommonConvert;
import com.orm.sql.util.Util;

public class UpdateSQLConvert extends CommonConvert{
/*
	public String convert(String sql, Map<String,Object> paramMap){
		String strSql = convertSql(sql);
		
		super.convert(sql,paramMap);
		return strSql;
	}*/
	
	public String convert(){
		String strSql = convertSql(convertParam.getSql());
		convertParam.setSql(strSql);
		return super.convert();
	}
	
	public String convertSql(String sql){
		sql = sql.trim();
		
		int startIndex = sql.indexOf("update ")+7;
		int endIndex = sql.indexOf(" set ");
		
		String updateTag = sql.substring(startIndex,endIndex).trim();
		
		if(updateTag.indexOf(" ") > 0)
			return sql;
		
		StringBuffer sqlBuffer = new StringBuffer();
		
		Data dataInfo = Util.getDataInfo(updateTag);
		
		Map<String, DataProperty> map = dataInfo.getPropertyInfo().getProperty();
		
		StringTokenizer token = new StringTokenizer(sql," ",false);
		
		while(token.hasMoreElements()){
			String tempStr = token.nextToken();
			
			if(updateTag.equals(tempStr)){
				sqlBuffer.append(tempStr);
				sqlBuffer.append(" ");
				//sqlBuffer.append(" as ");
				sqlBuffer.append(updateTag);
				sqlBuffer.append("0");
				sqlBuffer.append(" ");
			}else
			{
				if(map.containsKey(tempStr)){
					sqlBuffer.append(updateTag);
					sqlBuffer.append("0.");
				}
				sqlBuffer.append(tempStr);
				sqlBuffer.append(" ");
				
			}
		}
		
		
		return sqlBuffer.toString();
	}
}
