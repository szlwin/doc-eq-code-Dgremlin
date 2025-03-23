package dec.context.parse.xml.parse.directory;

import dec.context.parse.xml.exception.XMLParseException;
import dec.context.parse.xml.parse.config.ConfigConstanst;
import dec.context.parse.xml.parse.rule.Convert;
import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.config.model.rule.*;
import dec.core.context.config.model.rule.customer.CustomerInfo;
import dec.core.context.config.model.rule.error.ErrorInfo;
import dec.core.context.config.model.view.ViewData;
import dec.core.context.config.utils.ConfigContextUtil;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/*import com.orm.common.xml.exception.XMLParseException;
import com.orm.common.xml.model.config.ConfigInfo;
import com.orm.common.xml.model.rule.RuleCheckData;
import com.orm.common.xml.model.rule.RuleCheckDataPattern;
import com.orm.common.xml.model.rule.RuleCheckInfo;
import com.orm.common.xml.model.rule.RuleDefineInfo;
import com.orm.common.xml.model.rule.RuleExecuteInfo;
import com.orm.common.xml.model.rule.RuleViewInfo;
import com.orm.common.xml.model.view.ViewData;
import com.orm.common.xml.parse.AbstarctElementsParser;
import com.orm.common.xml.util.Constanst;
import com.orm.context.data.DataUtil;*/

public class ActionRuleParser {

    private final static Logger log = LoggerFactory.getLogger(ActionRuleParser.class);

    private static Map<String, String> typeMap = new HashMap<String, String>(15);

    static {
        typeMap.put(ConfigConstanst.RULE_TYPE_CHECK, null);
        typeMap.put(ConfigConstanst.RULE_TYPE_CHECK_DATA, null);
        typeMap.put(ConfigConstanst.RULE_TYPE_CHECK_DATA_PATTERN, null);
        typeMap.put(ConfigConstanst.RULE_TYPE_CHECK_PATTERN, null);
        typeMap.put(ConfigConstanst.RULE_TYPE_EXECUTE_INSERT, null);
        typeMap.put(ConfigConstanst.RULE_TYPE_EXECUTE_UPDATE, null);
        typeMap.put(ConfigConstanst.RULE_TYPE_EXECUTE_GET, null);
        typeMap.put(ConfigConstanst.RULE_TYPE_EXECUTE_DELETE, null);
        typeMap.put(ConfigConstanst.RULE_TYPE_EXECUTE_SELECT, null);
        typeMap.put(ConfigConstanst.RULE_TYPE_EXECUTE_GRAMMER, null);
    }

    @SuppressWarnings("rawtypes")
    public List<RuleDefineInfo> parse(Element element, String viewName) throws XMLParseException {
        if (!checkView(viewName)) {
            String msg = "Load the rule  error,the view-ref is error: " + viewName;
            log.error(msg);
            throw new XMLParseException(msg);
        }

        ViewData viewData = ConfigContextUtil.getConfigInfo().getViewData(viewName);
        Iterator itRule = element.elementIterator("rule");
        if (!itRule.hasNext()) {
			return null;
        }
        List<RuleDefineInfo> ruleList = new ArrayList<>();
        while (itRule.hasNext()) {
            Element ruleElement = (Element) itRule.next();
            RuleDefineInfo ruleDefineInfo = parseRuleDefineInfo(ruleElement, viewData);
            ruleList.add(ruleDefineInfo);
        }
        return ruleList;
    }

    private RuleDefineInfo parseRuleDefineInfo(Element element, ViewData viewData) throws XMLParseException {
        String type = element.attributeValue(RuleDefineInfo.TYPE);
        if (!typeMap.containsKey(type)) {
            String errorMsg = "The rule type: " + type + " is not exist!";
            log.error(errorMsg);
            throw new XMLParseException(errorMsg);
        }

        RuleDefineInfo ruleDefineInfo = null;
        if (type.equals(ConfigConstanst.RULE_TYPE_CHECK) || type.equals(ConfigConstanst.RULE_TYPE_CHECK_PATTERN)) {
            ruleDefineInfo = parseRuleCheckInfo(element, viewData);
        } else if (type.equals(ConfigConstanst.RULE_TYPE_CHECK_DATA_PATTERN)) {
            ruleDefineInfo = parseRuleCheckDataPatternInfo(element, viewData);
        } else if (type.equals(ConfigConstanst.RULE_TYPE_CHECK_DATA)) {
            ruleDefineInfo = parseRuleCheckDataInfo(element, viewData);
        } else {
            ruleDefineInfo = parseRuleExecuteInfo(element, viewData);
        }

        ruleDefineInfo.setGrammer(parseGrammer(element));

        ruleDefineInfo.setCustomerInfo(parseCustomerInfo(element));

        ruleDefineInfo.setErrorInfo(parseErrorInfo(element));

        return ruleDefineInfo;
    }

    private String parseGrammer(Element element) {
        Element customerElement = element.element("customer-process");

        if (customerElement != null) {
            return customerElement.getTextTrim();
        }
        return "";
    }

    private RuleExecuteInfo parseRuleExecuteInfo(Element element, ViewData viewData) throws XMLParseException {
        RuleExecuteInfo ruleExecuteInfo = new RuleExecuteInfo();

        ruleExecuteInfo.setName(element.attributeValue(RuleDefineInfo.NAME));
        ruleExecuteInfo.setProperty(element.attributeValue(RuleDefineInfo.PROPERTY));
        ruleExecuteInfo.setType(element.attributeValue(RuleDefineInfo.TYPE));

        if (ruleExecuteInfo.getType().equals(ConfigConstanst.RULE_TYPE_EXECUTE_GET)
                || ruleExecuteInfo.getType().equals(ConfigConstanst.RULE_TYPE_EXECUTE_SELECT)) {
            String propertyStr = ruleExecuteInfo.getProperty();

            if (propertyStr == null || "".equals(propertyStr)) {
                String errorMsg = "The rule: " + ruleExecuteInfo.getName() + ", the property can't be empty!";
                throw new XMLParseException(errorMsg);
            }

        }


        String sql = element.attributeValue(RuleExecuteInfo.SQL);
        if (sql != null && !"".equals(sql)) {
            Convert convert = new Convert(sql, viewData);
            sql = convert.convert();
        }/*else{
			if(ruleExecuteInfo.getType().equals(Constanst.RULE_TYPE_EXECUTE_GET)){
				String getSql = "select * from " + ruleExecuteInfo.getProperty() =;
				Convert convert = new Convert(getSql,viewData);
				sql = convert.convert();
			}
		}*/
        ruleExecuteInfo.setSql(sql);

        return ruleExecuteInfo;
    }

    private RuleCheckInfo parseRuleCheckInfo(Element element, ViewData viewData) {
        RuleCheckInfo ruleCheckInfo = new RuleCheckInfo();

        ruleCheckInfo.setName(element.attributeValue(RuleDefineInfo.NAME));
        ruleCheckInfo.setProperty(element.attributeValue(RuleDefineInfo.PROPERTY));
        ruleCheckInfo.setType(element.attributeValue(RuleDefineInfo.TYPE));
        ruleCheckInfo.setPattern(element.attributeValue(RuleCheckInfo.PATTERN));


        return ruleCheckInfo;
    }

    private RuleCheckDataPattern parseRuleCheckDataPatternInfo(Element element, ViewData viewData) {
        RuleCheckDataPattern ruleCheckDataPattern = new RuleCheckDataPattern();

        ruleCheckDataPattern.setName(element.attributeValue(RuleDefineInfo.NAME));
        //ruleCheckDataPattern.setProperty(element.getAttribute(RuleDefineInfo.PROPERTY));
        ruleCheckDataPattern.setType(element.attributeValue(RuleDefineInfo.TYPE));
        ruleCheckDataPattern.setPattern(element.attributeValue(RuleCheckDataPattern.PATTERN));

        String sql = element.attributeValue(RuleCheckDataPattern.SQL);

        if (sql != null && !"".equals(sql)) {
            Convert convert = new Convert(sql, viewData);
            sql = convert.convert();
        }
        ruleCheckDataPattern.setSql(sql);

        return ruleCheckDataPattern;
    }

    private RuleCheckData parseRuleCheckDataInfo(Element element, ViewData viewData) {
        RuleCheckData ruleCheckData = new RuleCheckData();

        ruleCheckData.setName(element.attributeValue(RuleDefineInfo.NAME));
        ruleCheckData.setProperty(element.attributeValue(RuleDefineInfo.PROPERTY));
        ruleCheckData.setType(element.attributeValue(RuleDefineInfo.TYPE));
        ruleCheckData.setPattern(element.attributeValue(RuleCheckDataPattern.PATTERN));

        //String sql = element.getAttribute(RuleCheckDataPattern.SQL);

        //if(sql !=null && !"".equals(sql)){
        //	Convert convert = new Convert(sql,viewName);
        //	sql = convert.convert();
        //}
        //ruleCheckDataPattern.setSql(sql);

        return ruleCheckData;
    }

    private ErrorInfo parseErrorInfo(Element element) {

        Element errorElement = element.element("error-info");

        if (errorElement != null) {
            ErrorInfo errorInfo = new ErrorInfo();
            errorInfo.setCode(errorElement.attributeValue("code"));
            errorInfo.setMessage(errorElement.attributeValue("message"));
            errorInfo.setLevel(errorElement.attributeValue("level"));

            return errorInfo;
        }
        return null;

    }

    private CustomerInfo parseCustomerInfo(Element element) {
        Element customerElement = element.element("customer-info");

        if (customerElement != null) {

            CustomerInfo customerInfo = new CustomerInfo();

            Iterator<Element> it = customerElement.elementIterator();

            Map<String, String> customerMap = new HashMap<>();

            while (it.hasNext()) {
                Element subElement = it.next();
                customerMap.put(subElement.getName(), subElement.getText());
            }

            customerInfo.setCustomerInfoMap(customerMap);
            return customerInfo;
        }
        return null;
    }


    private boolean checkView(String name) {
        ConfigInfo configInfo = ConfigContextUtil.getConfigInfo();
        if (configInfo.getViewData(name) == null) {
            return false;
        }
        return true;
    }
}
