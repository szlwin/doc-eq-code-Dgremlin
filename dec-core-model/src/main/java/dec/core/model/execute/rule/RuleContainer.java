package dec.core.model.execute.rule;

import dec.core.context.config.model.rule.RuleDefineInfo;
import dec.core.context.config.model.rule.RuleViewInfo;
import dec.core.context.config.model.rule.error.ErrorInfo;
import dec.core.context.data.ModelData;
import dec.core.datasource.connection.DataConnection;
import dec.core.datasource.execute.exception.ExecuteException;
import dec.core.model.container.ModelLoader;
import dec.core.model.container.ResultInfo;
import dec.core.model.container.listener.ViewEvent;
import dec.core.model.container.listener.ViewEventEnum;
import dec.core.model.execute.rule.exception.ExecuteRuleException;
import dec.core.model.utils.DataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class RuleContainer {

    private final static Logger log = LoggerFactory.getLogger(RuleContainer.class);

    private ModelLoader modelLoader;

    //private String conName;

    private RuleViewInfo ruleViewInfo;

    private DataConnection<?, ?> con;

    private ViewEvent viewEvent;

    private String startRule;

    private String endRule;

    public RuleContainer(ModelLoader modelLoader) {
        this.modelLoader = modelLoader;
    }

    public RuleContainer(ModelLoader modelLoader, DataConnection<?, ?> con) {
        this.modelLoader = modelLoader;
        this.con = con;
    }

    public ResultInfo execute() throws ExecuteException, ExecuteRuleException {

        ruleViewInfo = getRuleInfo();

        ResultInfo resultInfo = null;

        resultInfo = doBefore();

        if (resultInfo != null && !resultInfo.isSuccess()) {
            return resultInfo;
        }

        resultInfo = executeAllRule();

        doAfter(resultInfo);

        return resultInfo;
    }

    public void setStartRule(String startRule) {
        this.startRule = startRule;
    }

    public void setEndRule(String endRule) {
        this.endRule = endRule;
    }

    private ResultInfo doBefore() {

        if (modelLoader.getListener() != null) {

            viewEvent = new ViewEvent();

            viewEvent.setType(ViewEventEnum.VIEW_START);

            viewEvent.setViewName(ruleViewInfo.getName());

            viewEvent.setModelData((ModelData) modelLoader.get());

            return modelLoader.getListener().notify(viewEvent);

        }
        return null;
    }

    private ResultInfo doAfter(ResultInfo resultInfo) {
        if (modelLoader.getListener() != null) {

            viewEvent.setRuleResultInfo(resultInfo);

            viewEvent.setType(ViewEventEnum.VIEW_END);

            return modelLoader.getListener().notify(viewEvent);
        }
        return resultInfo;
    }

    private ResultInfo executeAllRule() throws ExecuteException, ExecuteRuleException {

        ResultInfo resultInfo = null;

        List<RuleDefineInfo> ruleList = ruleViewInfo.getRules();

        int startIndex = this.startRule == null ? 0 : ruleViewInfo.getRuleIndex(this.startRule);
        int endIndex = this.endRule == null ? ruleList.size() : ruleViewInfo.getRuleIndex(this.endRule);
        for (int i = startIndex; i < endIndex || (this.endRule != null && i == endIndex); i++) {
            RuleDefineInfo ruleDefineInfo = ruleList.get(i);

            resultInfo = executeOneRule(ruleDefineInfo);

            if (!resultInfo.isSuccess()) {
                resultInfo.setErrorName(ruleDefineInfo.getName());

                ErrorInfo errorInfo = ruleDefineInfo.getErrorInfo();

                if (errorInfo != null) {

                    resultInfo.setErrorMsg(errorInfo.getMessage());

                    resultInfo.setErrorCode(errorInfo.getCode());

                    resultInfo.setErrorLevel(errorInfo.getLevel());
                }

                break;
            }
        }
        return resultInfo;
    }

    private ResultInfo executeOneRule(RuleDefineInfo rule) throws ExecuteException, ExecuteRuleException {

        if (log.isDebugEnabled()) {
            log.debug("Start execute the rule: {}--{}", ruleViewInfo.getName(), rule.getName());
        }

        if (modelLoader.getListener() != null) {
            viewEvent.setType(ViewEventEnum.RULE_START);
            viewEvent.setRuleName(rule.getName());
            viewEvent.setCustomerInfo(rule.getCustomerInfo());
            modelLoader.getListener().notify(viewEvent);
        }

        ResultInfo resultInfo = new ResultInfo();

        resultInfo.setViewName(modelLoader.getRuleName());
        resultInfo.setRuleName(rule.getName());

        boolean isMain = ruleViewInfo.getViewData().getName()
                .equals(rule.getProperty());

        Object obj = null;

        if (!isMain) {
            obj = convert(rule.getProperty(), rule.getType());
        } else {
            obj = convertByMain();
        }
		
		/*if(log.isDebugEnabled()){
			log.debug("get the op value");
		}*/

        if (obj == null) {
            String errorMsg = "The property: " + rule.getProperty() + " is not exist!";
            resultInfo.setSuccess(false);
            resultInfo.setErrorMsg(errorMsg);

            log.error("Execute the rule: {}--{} is error:{}", ruleViewInfo.getName(), rule.getName(), errorMsg);

            throw new ExecuteRuleException(errorMsg);
        }

        boolean isOk = false;


        isOk = RuleUtil.createRuleExeute(rule.getType(), rule, obj, con, ruleViewInfo.getViewData(), this.modelLoader.getExternal())
                .execute();


        if (isOk &&
                (RuleUtil.isInsert(rule.getType()))) {

            UpdateKeyUtil updateKeyUtil = new UpdateKeyUtil(con.getDataSource(),
                    modelLoader.get(),
                    ruleViewInfo.getViewData());

            String propertyName = rule.getProperty();
            updateKeyUtil.update(isMain, propertyName);
        }

        if (!isOk) {
            log.error("Execute the rule: {}--{} is fail ", ruleViewInfo.getName(), rule.getName());
        }

        if (log.isDebugEnabled()) {
            log.debug("End execute the rule: {}--{}", ruleViewInfo.getName(), rule.getName());
        }

        resultInfo.setSuccess(isOk);

        if (modelLoader.getListener() != null) {
            viewEvent.setType(ViewEventEnum.RULE_END);
            //viewEvent.setRuleName(rule.getName());
            //viewEvent.setCustomerInfo(rule.getCustomerInfo());
            viewEvent.setRuleResultInfo(resultInfo);
            modelLoader.getListener().notify(viewEvent);
        }

        return resultInfo;
    }

    private RuleViewInfo getRuleInfo() {

        return DataUtil.getRuleViewInfo(modelLoader.getRuleName());
    }

    private Object convert(String property, String type) {

        if (type.equals("check") || type.equals("checkData")) {
            return DataUtil.getValueByKey(property, modelLoader.get());
        }
        return DataUtil.getModelValues((ModelData) modelLoader.get());
		/*
		if(type.equals("checkPattern") || type.equals("checkDataPattern"))
			return DataUtil.getModelValues((ModelData) modelLoader.get());
		
		if(property ==null || "".equals(property) || RuleUtil.isQuery(type)){
			return DataUtil.getModelValues((ModelData) modelLoader.get());
		}else{
			Map<String,Object> value = DataUtil.getModelValues((ModelData) modelLoader.get());
			
			return value.get(property);
			
		}*/

        //return null;
    }

    private Object convertByMain() {

        Map<String, Object> paramMap = DataUtil.getModelValues((ModelData) modelLoader.get());

		/*
		Map<String,Object> valueMap = new HashMap<String,Object>(paramMap.size());
		
		Set<String> keySet = paramMap.keySet();
		Iterator<String> it = keySet.iterator();
		while(it.hasNext()){
			String key = it.next();
			Object value = paramMap.get(key);
			
			if(value instanceof Collection || value instanceof Map)
				continue;
			
			valueMap.put(key, value);
		}*/
        //return valueMap;
        return paramMap;
    }


}
