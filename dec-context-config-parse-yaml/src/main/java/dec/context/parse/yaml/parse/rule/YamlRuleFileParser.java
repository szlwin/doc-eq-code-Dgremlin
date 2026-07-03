package dec.context.parse.yaml.parse.rule;

import dec.context.parse.yaml.exception.YAMLParseException;
import dec.context.parse.yaml.parse.FileParser;
import dec.context.parse.yaml.parse.YamlResource;
import dec.context.parse.yaml.parse.YamlSupport;
import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.config.ConfigConstanst;
import dec.core.context.config.model.rule.RuleCheckData;
import dec.core.context.config.model.rule.RuleCheckDataPattern;
import dec.core.context.config.model.rule.RuleCheckInfo;
import dec.core.context.config.model.rule.RuleDefineInfo;
import dec.core.context.config.model.rule.RuleExecuteInfo;
import dec.core.context.config.model.rule.RuleViewInfo;
import dec.core.context.config.model.rule.customer.CustomerInfo;
import dec.core.context.config.model.rule.error.ErrorInfo;
import dec.core.context.config.model.view.ViewData;
import dec.core.context.config.utils.ConfigContextUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YamlRuleFileParser implements FileParser<List<RuleViewInfo>> {

    @Override
    public List<RuleViewInfo> parse(String filePath) throws YAMLParseException {
        List<RuleViewInfo> ruleViewList = new ArrayList<>();
        for (YamlResource resource : YamlSupport.findAll(filePath)) {
            Map<String, Object> root = YamlSupport.loadMap(resource);
            for (Object item : roots(root)) {
                ruleViewList.add(parseRuleView(YamlSupport.map(item, "ruleView")));
            }
        }
        return ruleViewList;
    }

    public RuleViewInfo parseRuleView(Map<String, Object> node) throws YAMLParseException {
        RuleViewInfo ruleViewInfo = new RuleViewInfo();
        String name = YamlSupport.requireStr(node, "ruleView.name", "name");
        if (ConfigContextUtil.getConfigInfo().get(Config.RULE, name) != null) {
            throw new YAMLParseException("The rule view is existed: " + name);
        }
        String viewRef = YamlSupport.requireStr(node, "ruleView.viewRef", "viewRef", "view-ref", "view_ref");
        ViewData viewData = ConfigContextUtil.getConfigInfo().getViewData(viewRef);
        if (viewData == null) {
            throw new YAMLParseException("The view is not existed: " + viewRef);
        }
        ruleViewInfo.setName(name);
        ruleViewInfo.setViewData(viewData);
        for (Object item : YamlSupport.list(YamlSupport.first(node, "rules", "rule"), "rules")) {
            ruleViewInfo.addRule(parseRule(YamlSupport.map(item, "rule")));
        }
        return ruleViewInfo;
    }

    private RuleDefineInfo parseRule(Map<String, Object> node) throws YAMLParseException {
        String type = YamlSupport.requireStr(node, "rule.type", "type");
        RuleDefineInfo rule = createRule(type);
        rule.setName(YamlSupport.requireStr(node, "rule.name", "name"));
        rule.setType(type);
        rule.setProperty(YamlSupport.str(node, "property"));
        rule.setGrammer(YamlSupport.str(node, "process", "customer-process", "customerProcess", "grammer"));
        rule.setErrorInfo(parseError(YamlSupport.first(node, "error", "error-info", "errorInfo")));
        rule.setCustomerInfo(parseCustomerInfo(YamlSupport.first(node, "customer", "customer-info", "customerInfo")));

        if (rule instanceof RuleCheckInfo) {
            ((RuleCheckInfo) rule).setPattern(YamlSupport.str(node, "pattern"));
        }
        if (rule instanceof RuleCheckData) {
            ((RuleCheckData) rule).setPattern(YamlSupport.str(node, "pattern"));
            ((RuleCheckData) rule).setSql(YamlSupport.str(node, "sql", "cmd"));
        }
        if (rule instanceof RuleCheckDataPattern) {
            ((RuleCheckDataPattern) rule).setPattern(YamlSupport.str(node, "pattern"));
            ((RuleCheckDataPattern) rule).setSql(YamlSupport.str(node, "sql", "cmd"));
        }
        if (rule instanceof RuleExecuteInfo) {
            ((RuleExecuteInfo) rule).setSql(YamlSupport.str(node, "sql", "cmd"));
        }
        return rule;
    }

    private RuleDefineInfo createRule(String type) throws YAMLParseException {
        if (ConfigConstanst.RULE_TYPE_CHECK.equals(type) || ConfigConstanst.RULE_TYPE_CHECK_PATTERN.equals(type)) {
            return new RuleCheckInfo();
        }
        if (ConfigConstanst.RULE_TYPE_CHECK_DATA.equals(type)) {
            return new RuleCheckData();
        }
        if (ConfigConstanst.RULE_TYPE_CHECK_DATA_PATTERN.equals(type)) {
            return new RuleCheckDataPattern();
        }
        if (ConfigConstanst.RULE_TYPE_EXECUTE_INSERT.equals(type)
                || ConfigConstanst.RULE_TYPE_EXECUTE_UPDATE.equals(type)
                || ConfigConstanst.RULE_TYPE_EXECUTE_DELETE.equals(type)
                || ConfigConstanst.RULE_TYPE_EXECUTE_GET.equals(type)
                || ConfigConstanst.RULE_TYPE_EXECUTE_SELECT.equals(type)
                || ConfigConstanst.RULE_TYPE_EXECUTE_GRAMMER.equals(type)) {
            return new RuleExecuteInfo();
        }
        throw new YAMLParseException("The rule type is not supported: " + type);
    }

    private ErrorInfo parseError(Object value) throws YAMLParseException {
        if (value == null) {
            return null;
        }
        Map<String, Object> map = YamlSupport.map(value, "error");
        ErrorInfo errorInfo = new ErrorInfo();
        errorInfo.setCode(YamlSupport.str(map, "code"));
        errorInfo.setMessage(YamlSupport.str(map, "message"));
        errorInfo.setLevel(YamlSupport.str(map, "level"));
        return errorInfo;
    }

    private CustomerInfo parseCustomerInfo(Object value) throws YAMLParseException {
        if (value == null) {
            return null;
        }
        Map<String, Object> map = YamlSupport.map(value, "customerInfo");
        Map<String, String> customerMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            customerMap.put(entry.getKey(), entry.getValue() == null ? null : String.valueOf(entry.getValue()));
        }
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setCustomerInfoMap(customerMap);
        return customerInfo;
    }

    private List<Object> roots(Map<String, Object> root) throws YAMLParseException {
        Object ruleViews = YamlSupport.first(root, "ruleViews", "rule-view-info", "ruleViewList");
        if (ruleViews != null) {
            return YamlSupport.list(ruleViews, "ruleViews");
        }
        Object ruleView = YamlSupport.first(root, "ruleView");
        if (ruleView != null) {
            return YamlSupport.list(ruleView, "ruleView");
        }
        Object mapping = YamlSupport.first(root, "orm-rule-mapping", "ormRuleMapping");
        if (mapping != null) {
            return YamlSupport.list(YamlSupport.first(YamlSupport.map(mapping, "orm-rule-mapping"), "rule-view-info", "ruleViews"), "ruleViews");
        }
        return YamlSupport.list(root, "ruleView");
    }
}
