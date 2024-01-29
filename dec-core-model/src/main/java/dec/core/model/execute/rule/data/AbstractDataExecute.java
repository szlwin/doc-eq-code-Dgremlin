package dec.core.model.execute.rule.data;

import dec.core.context.config.exception.DataNotDefineException;
import dec.core.context.config.model.relation.Relation;
import dec.core.context.config.model.rule.RuleExecuteInfo;
import dec.core.context.config.model.view.RelationInfo;
import dec.core.context.data.BaseData;
import dec.core.datasource.execute.exception.ExecuteException;
import dec.core.model.execute.rule.common.AbstractRuleExecute;
import dec.core.model.utils.DataUtil;
import dec.external.datasource.sql.dom.ExecuteParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public abstract class AbstractDataExecute extends AbstractRuleExecute {

    private final static Logger log = LoggerFactory.getLogger(AbstractDataExecute.class);

    protected String sql;

    //protected Object paramValue;

    //protected ViewData viewData;

    protected String propertyName;

    protected String dataSource;

    //protected SimpleSQLExecute simpleSQLExecute;

    //protected DataConnection<Object, Object> dataCon;

    protected String type;

    protected Object propertyValue;

    public void setSql(String sql) {
        this.sql = sql;

    }

    //public void setValue(Object e) {
    //	this.paramValue = e;

    //}

    public void setPropertyName(String name) {
        this.propertyName = name;
    }

    //public void setViewData(ViewData viewData) {
    //	this.viewData = viewData;

    //}

    public void setDataSource(String name) {
        this.dataSource = name;

    }

    public boolean execute() throws ExecuteException {

        RuleExecuteInfo ruleExecuteInfo = (RuleExecuteInfo) this.ruleInfo;

        if (viewData.getName().equals(ruleExecuteInfo.getProperty())) {
            this.setPropertyName(viewData.getTargetMain().getName());
        } else {
            this.setPropertyName(ruleExecuteInfo.getProperty());
        }
        this.setSql(ruleExecuteInfo.getSql());
        this.setType(ruleExecuteInfo.getType());
        this.setDataSource(con.getDataSource());

        if (sql == null || "".equals(sql)) {
            //simpleSQLExecute = createExecute();
            return executeByData();
        } else {
            return executeBySql();
        }
    }

    @SuppressWarnings("unchecked")
    protected boolean executeByData() throws ExecuteException {
        boolean isMain = viewData.getTargetMain().getName().equals(propertyName);
        Object dataValue = null;
        if (!isMain) {

            dataValue = DataUtil.getValueByKey(propertyName, value);;
        } else {
            dataValue = value;
        }

        if (dataValue instanceof Collection) {
            Iterator<Object> it = ((Collection<Object>) dataValue).iterator();
            while (it.hasNext()) {
                propertyValue = it.next();
                boolean result = executeByData(propertyValue);
                if (!result)
                    return false;
            }
            return true;
        } else {

            propertyValue = dataValue;

            return executeByData(propertyValue);
        }
    }

    @SuppressWarnings("unchecked")
    protected boolean executeBySql() throws ExecuteException {
        //Convert convert = new Convert(sql,viewName);
        //String tempSql = convert.convert();

        boolean isMain = viewData.getTargetMain().getName().equals(propertyName);

        if (!isMain) {
            Object dataValue = ((Map) value).get(propertyName);

            if (dataValue instanceof Collection) {
                Iterator<Object> it = ((Collection<Object>) dataValue).iterator();

                while (it.hasNext()) {
                    propertyValue = it.next();
                    boolean result = executeBysql(sql, propertyValue);
                    if (!result)
                        return false;
                }
                return true;
            } else {
                propertyValue = dataValue;
                return executeBysql(sql, value);
            }

        } else {
            propertyValue = value;
            return executeBysql(sql, value);
        }
		/*
		if(dataValue instanceof Collection){
			Iterator<Object> it = ((Collection<Object>)dataValue).iterator();
			
			while(it.hasNext()){
				propertyValue = it.next();
				boolean result = executeBysql(sql,propertyValue);
				if(!result)
					return false;
			}
			return true;
		}else{
			
			if(!isMain){
				propertyValue = ((Map)paramValue).get(propertyName);
			}else{
				propertyValue = paramValue;
			}
			
			//propertyValue = dataValue;
			return executeBysql(sql,paramValue);
		}*/
    }

    @SuppressWarnings("unchecked")
    protected boolean executeByData(Object value) throws ExecuteException {
        RelationInfo relationInfo = viewData.getRelationInfo();
        Relation relationView = null;
        String dataName = null;
        boolean isMain = viewData.getTargetMain().getName().equals(propertyName);

        if (isMain) {
            dataName = viewData.getTargetMain().getName();
        } else {
            relationView = relationInfo.getRelationByPropertyName1(propertyName);//Util.getRelationView(viewName, propertyName)
            //if(relationView == null){
            //	dataName = propertyName;
            //}else{
            dataName = relationView.getViewProperty().getViewData().getTargetMain().getName();
            //}
            //dataName = propertyName;
        }
        //viewData.getRelationInfo().getRelation(name);


        BaseData data = null;
        try {
            data = DataUtil.createBaseData(dataName);
        } catch (DataNotDefineException e) {
            return false;
        }

        if (!isMain) {
            Map<String, Object> dataValue = (Map<String, Object>) value;
            DataUtil.convertDataToBaseData(dataValue, data, relationView);
        } else
            DataUtil.convertDataToBaseData((Map<String, Object>) value, data, viewData);

        //simpleSQLExecute.execute(data);
        Object object = null;
        ExecuteParam execParam = new ExecuteParam();
        execParam.setType(type);
        execParam.setValue(data);
        //execParam.setCmd(cmd);

        object = con.execute(execParam);


        doAfter(object);
        return true;
    }

    protected boolean executeBysql(String sql, Object value) throws ExecuteException {
        Object object = null;
        ExecuteParam execParam = new ExecuteParam();
        execParam.setType(type);
        execParam.setValue(value);
        execParam.setCmd(sql);
        object = this.con.execute(execParam);
        doAfter(object);
        return true;
    }

    //protected abstract SimpleSQLExecute createExecute();

    protected abstract void doAfter(Object result);


    public void setType(String type) {
        this.type = type;
    }
}
