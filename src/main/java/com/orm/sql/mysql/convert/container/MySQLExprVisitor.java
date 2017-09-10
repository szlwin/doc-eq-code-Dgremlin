package com.orm.sql.mysql.convert.container;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import com.orm.common.xml.model.data.Column;
import com.orm.context.data.DataUtil;
import com.orm.context.data.NullData;
import com.orm.sql.dom.ConvertParam;
import com.orm.sql.dom.DataInfo;
import com.orm.sql.dom.TableTag;
import com.orm.sql.util.Util;

import santr.parser.exception.ExecuteInvaildException;
import santr.v4.execute.AbstractVisitor;
import santr.v4.parser.ParserTree;
import santr.v4.parser.RuleContext;

public class MySQLExprVisitor extends AbstractVisitor<ConvertParam>{
	
	private List<DataInfo> dataList = null;
	
	private Map<String,Object> paramMap;
	
	//private StringBuffer sqlBuffer = new StringBuffer();
	
	boolean isHasParam;
	
	public void init(){
		
		this.paramMap = (Map<String, Object>) this.getParamer().getData();
		
		isHasParam = paramMap != null 
				&& !paramMap.isEmpty();
		
		if(isHasParam){
			this.dataList = new ArrayList<DataInfo>();
		}
	}
	
	public void execute(RuleContext context) throws ExecuteInvaildException {
		 String name = context.getName();
		 if(name.equals("sqlExpr")){
			 executeSqlExpr(context);
		 }else if(name.equals("selectExpr")){
			 executeSelectExpr(context);
		 }
		 //else if(name.equals("tableInfo")){
		//	 executeCommon(context);
		 //}
		// else if(name.equals("table")){
		//	 executeCommon(context);
		// }
		 else if(name.equals("tableElement")){
			 executeTableElement(context);
		 }else if(name.equals("ID")){
			 executeID(context);
		 }else if(name.equals("STRINGTEXT")){
			 executeString(context);
		 }else if(name.equals("colElementCon")){
			 executeColElementCon(context);
		 }
		 //else if(name.equals("columnInfo")){
		//	 this.executeCommon(context);
		// }
		 else if(name.equals("column")){
			 this.executeColumn(context);
		 }else if(name.equals("cparam")){
			 this.executeCparam(context);
		 }
		 
		 else{
			 this.executeCommon(context);
		 }
	}
	
	public List<DataInfo> getDataList(){
		return dataList;
	}
	
	private void executeCparam(RuleContext context) throws ExecuteInvaildException {
		Object[] valueArray = this.getAllChildValue(context, context.getParam());
		boolean isSuccess = false;
		if(valueArray.length ==2){
			isSuccess = replaceParam((String) valueArray[1],null);
		}else{
			isSuccess = replaceParam((String)valueArray[1],(String)valueArray[3]);
		}
		
		if(!isSuccess){
			String error = "";
			for(Object object:valueArray){
				error = error+object;
			}
			throw new ExecuteInvaildException("The param:"+error+" is error!");
		}else{
			this.addValue("?", context);
		}
		
	}

	private void executeColElementCon(RuleContext context) throws ExecuteInvaildException {
		TreeValue treeValue = getTreeValue(context);
		ParserTree[] parserTreeArray =context.getAllChild();
		
		Object[] valueArray = this.getAllChildValue(context);
		
		// ID ('.' ID)?
		if(parserTreeArray.length>1){
			TableTag tag = treeValue.getTagTableMap().get(valueArray[0]);
			
			Map<String,Column> dataInfoMap = Util.convert(tag.getDataName(), 
					this.getParamer().getDataSource());
			
			Column column = dataInfoMap.get(valueArray[2]);
			
			this.addValue(valueArray[0]+"."+column.getName(), context);
		}else{
			TableTag tag = treeValue.getTagTableMap().get(null);
			
			Map<String,Column> dataInfoMap = Util.convert(tag.getDataName(), 
					this.getParamer().getDataSource());
			
			Column column = dataInfoMap.get(valueArray[0]);
			this.addValue(column.getName(), context);
		}
	}

	private void executeColumn(RuleContext context) throws ExecuteInvaildException {
		ParserTree[] parserTreeArray =context.getAllChild();
		if(parserTreeArray.length==1){
			if(parserTreeArray[0].isToken()){
				//*
				executeSigleAll(context);
			}else{
				//columnElement
				parserTreeArray[0].getRuleContext().setParam(context.getParam());
				executeColumnElement(parserTreeArray[0].getRuleContext());
			}
		}else{
			if(parserTreeArray.length==2){
				//ID '.*'
				executeSigleTagAll(context);
			}
		}
	}
	
	//ID '.*'
	private void executeSigleTagAll(RuleContext context) {
		TreeValue treeValue = getTreeValue(context);
		String tag = (String) this.getChildParam(context, 0);
		TableTag tableTag = treeValue.getTagTableMap().get(tag);
		
		Map<String,Column> dataInfoMap = Util.convert(tableTag.getDataName(), 
				this.getParamer().getDataSource());
		
		Iterator<Column> it =  dataInfoMap.values().iterator();
		
		while(it.hasNext()){
			Column column = it.next();
			this.addValue(tag+"."+column.getName(), context);
			this.addValue("as", context);
			this.addValue(column.getRefproperty(), context);
		}
	}
	
	//columnElement
	private void executeColumnElement(RuleContext context) throws ExecuteInvaildException {
		ParserTree[] parserTreeArray =  context.getAllChild();
		
		if(parserTreeArray[0].isToken()){
			//'(' selectExpr ')'
			this.addValue("(",context);
			
			this.getChildValue(parserTreeArray[1], context.getParam());
			
			this.addValue(")",context);

		}else{
			//fun | colElementCon ('as' ID)?
			Object[] valueArry = this.getAllChildValue(context, context.getParam());
			
			//colElementCon 'as' ID
			if(valueArry.length>1){
				this.addValue(valueArry[1], context);
				this.addValue(valueArry[2], context);
			}
		}

	}

	//*
	private void executeSigleAll(RuleContext context){
		TreeValue treeValue = getTreeValue(context);
		
		TableTag tag = treeValue.getTagTableMap().get(null);
		
		Map<String,Column> dataInfoMap = Util.convert(tag.getDataName(), 
				this.getParamer().getDataSource());
		
		Iterator<Column> it =  dataInfoMap.values().iterator();
		
		while(it.hasNext()){
			Column column = it.next();
			this.addValue(column.getName(), context);
			this.addValue("as", context);
			this.addValue(column.getRefproperty(), context);
		}
	}
	
	private void executeTableInfo(RuleContext context) throws ExecuteInvaildException {
		executeCommon(context);
	}
	
	private void executeTable(RuleContext context) throws ExecuteInvaildException {
		executeCommon(context);
	}

	private void executeTableElement(RuleContext context) throws ExecuteInvaildException {
		ParserTree[] parserTreeArray = context.getAllChild();
		if(parserTreeArray[0].isToken()){
			
			//子查询暂不实现
		}else{

			Object[] ObjectValue = this.getAllChildValue(context);
			//添加表标记
			@SuppressWarnings("unchecked")
			Map<String,TableTag> tagTableMap 
				= ((TreeValue)context.getParam()).getTagTableMap();
			if(tagTableMap.containsKey(ObjectValue[0])){
				throw new ExecuteInvaildException("The flag is error:"+ObjectValue[0]);
			}
			
			TableTag tableTag = null;
			
			//添加到SQL语句
			if(ObjectValue.length ==1){
				tableTag = convertTableTag((String) ObjectValue[0],null);
				
				addValue(tableTag.getTableName(),context);
			}else{
				tableTag = convertTableTag((String) ObjectValue[0],(String) ObjectValue[1]);
				
				addValue(tableTag.getTableName(),context);
				addValue(tableTag.getTagName(),context);
			}
			
			tagTableMap.put(tableTag.getTagName(), tableTag);
		}
	}


	private void executeCommon(RuleContext context) throws ExecuteInvaildException{
		ParserTree[] parserTreeArray = context.getAllChild();
		for(ParserTree parserTree:parserTreeArray){
			if(parserTree.isToken()){
				addValue(parserTree.getToken(),context);
			}else{
				this.getChildValue(parserTree,context.getParam());
			}
		}
	}
	
	private void executeSelectExpr(RuleContext context) throws ExecuteInvaildException{
		TreeValue paramTreeValue = this.getTreeValue(context);
		
		 TreeValue treeValue= new TreeValue();
		 
		 this.getChildValue(context, 3, treeValue);
		 ParserTree[] parserTreeArray = context.getAllChild();
		 
		 TreeValue columnTreeValue = new TreeValue();
		 columnTreeValue.setTagTableMap(treeValue.getTagTableMap());
		 
		 //columnInfo
		 this.getChildValue(parserTreeArray[1], columnTreeValue);
		 
		 StringBuffer sqlBuffer = new StringBuffer();
		 
		 //select
		 sqlBuffer.append(parserTreeArray[0].getToken()).append(" ");
		 
		 //columnInfo
		 sqlBuffer.append(columnTreeValue.getSqlBuffer()).append(" ");
		 
		 //from
		 sqlBuffer.append(parserTreeArray[2].getToken()).append(" ");
		 
		 //sql合并
		 treeValue.getSqlBuffer().insert(0, sqlBuffer);
		 
		 for(int i = 4; i<parserTreeArray.length;i++){
			 if(parserTreeArray[i].isToken()){
				 this.addValue(parserTreeArray[i].getToken(),context);
			 }else{
				 this.getChildValue(parserTreeArray[i], treeValue);
			 }
		 }
		 context.setValue(treeValue.getSqlBuffer());
		 if(paramTreeValue !=null){
			this.addValue(treeValue.getSqlBuffer(), context);
		 }
	}
	
	private void executeSqlExpr(RuleContext context) throws ExecuteInvaildException{
		Object value = this.getChildValue(context, 0);
		context.setValue(value);
	}
	
    private void executeID(RuleContext context){
    	//Save the value to this tree.
        context.setValue(context.getText());
    }

    private void executeString(RuleContext context){
        //Save the value to this tree.
        context.setValue("'"+context.getText()+"'");
    }
    
	private void addValue(Object object,RuleContext context){
		TreeValue treeValue = (TreeValue) context.getParam();
		treeValue.addValue(object);
		treeValue.addValue(" ");
	}
	
	private TableTag convertTableTag(String dataName,String tagName){
		TableTag tableTag = new TableTag();
		tableTag.setDataName(dataName);
		
		tableTag.setTableName(Util.getTableName(dataName, this.getParamer().getDataSource()));
		
		tableTag.setTagName(tagName);
		return tableTag;
		
	}
	
	private TreeValue getTreeValue(RuleContext context){
		return (TreeValue)context.getParam();
	}
	
	private class TreeValue{
		private StringBuffer sqlBuffer = new StringBuffer();
		
		private Map<String,TableTag> tagTableMap = new FastMap<String,TableTag>();
		
		public void addValue(Object object){
			sqlBuffer.append(object).append(" ");
		}

		public StringBuffer getSqlBuffer() {
			return sqlBuffer;
		}

		public Map<String, TableTag> getTagTableMap() {
			return tagTableMap;
		}

		public void setTagTableMap(Map<String, TableTag> tagTableMap) {
			this.tagTableMap = tagTableMap;
		}
		
	}
	
	private boolean replaceParam(String str,String str1){
		if(!isHasParam)
			return false;
		
		Object value = DataUtil.getValueByKey(str, paramMap);

		if(value == null)
			return false;
		
		if(str1!=null){
			value = ((Map<String,Object>) value).get(str1);
			
			if(value == null)
				return false;
		}
		
		DataInfo dataInfo = new DataInfo();
		
		if(value instanceof NullData){
			dataInfo.setValue(null);
		}else{
			dataInfo.setValue(value);
		}
		
		this.dataList.add(dataInfo);
		return true;
	}
}
