package dec.expand.declare.conext.parser.yaml.parser;

import dec.core.context.config.utils.ConfigContextUtil;
import dec.expand.declare.collections.SimpleList;
import dec.expand.declare.conext.DescContext;
import dec.expand.declare.conext.desc.business.BusinessDesc;
import dec.expand.declare.conext.desc.business.ViewRuleDesc;
import dec.expand.declare.conext.desc.data.DataDependDesc;
import dec.expand.declare.conext.desc.data.DataDesc;
import dec.expand.declare.conext.desc.data.DataTypeEnum;
import dec.expand.declare.conext.desc.process.ProcessDesc;
import dec.expand.declare.conext.desc.process.TransactionPolicy;
import dec.expand.declare.conext.desc.system.SystemDesc;
import dec.expand.declare.conext.parser.yaml.exception.YAMLParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ContextDescYamlParser {

    private final static Logger log = LoggerFactory.getLogger(ContextDescYamlParser.class);

    public void parser(String filePath) throws YAMLParseException {
        log.info("Start parser the yaml file:" + filePath);
        Map<String, Object> root = YamlSupport.loadMap(filePath);
        DescContext.get().addSystem(parseSystems(root));
        DescContext.get().addBusiness(parseBusinesses(root));
        log.info("End parser the yaml file:" + filePath);
    }

    private List<SystemDesc> parseSystems(Map<String, Object> root) throws YAMLParseException {
        Object value = YamlSupport.first(root, "systems");
        if (value == null) {
            return Collections.emptyList();
        }
        List<SystemDesc> systemDescList = new SimpleList<SystemDesc>();
        for (Object item : YamlSupport.list(value, "systems")) {
            systemDescList.add(parseSystem(YamlSupport.map(item, "system")));
        }
        return systemDescList;
    }

    private SystemDesc parseSystem(Map<String, Object> node) throws YAMLParseException {
        String name = YamlSupport.requireStr(node, "system.name", "name");
        log.info("Start load yaml system:" + name);
        if (DescContext.get().getSystem(name) != null) {
            throw new YAMLParseException("The system is duplicate:" + name);
        }
        SystemDesc systemDesc = new SystemDesc();
        systemDesc.setName(name);
        systemDesc.setComment(YamlSupport.str(node, "desc", "comment"));
        parseData(systemDesc, YamlSupport.first(node, "datas", "data"));
        log.info("End load yaml system:" + name);
        return systemDesc;
    }

    private void parseData(SystemDesc systemDesc, Object value) throws YAMLParseException {
        for (Object item : YamlSupport.list(value, "datas")) {
            Map<String, Object> dataMap = YamlSupport.map(item, "data");
            String name = YamlSupport.requireStr(dataMap, "data.name", "name");
            if (systemDesc.getData(name) != null) {
                throw new YAMLParseException("The data is duplicate:" + name);
            }
            DataDesc dataDesc = new DataDesc();
            dataDesc.setName(name);
            dataDesc.setComment(YamlSupport.str(dataMap, "desc", "comment"));

            if ("common".equals(systemDesc.getName())) {
                dataDesc.setType(DataTypeEnum.CACHE);
                dataDesc.setCachePrior(false);
            }
            String type = YamlSupport.str(dataMap, "type");
            if (type == null || "".equals(type) || "cache".equals(type)) {
                dataDesc.setType(DataTypeEnum.CACHE);
            } else if ("persistent".equals(type)) {
                dataDesc.setType(DataTypeEnum.PERSISTENT);
            } else {
                throw new YAMLParseException("The property 'type' for data is error,value:" + type);
            }

            String cachePrior = YamlSupport.str(dataMap, "isCachePrior", "cachePrior");
            dataDesc.setCachePrior(cachePrior != null && Boolean.valueOf(cachePrior));
            parseDepends(dataDesc, YamlSupport.first(dataMap, "depends", "depend"));
            systemDesc.addData(dataDesc);
        }
    }

    private void parseDepends(DataDesc dataDesc, Object value) throws YAMLParseException {
        for (Object item : YamlSupport.list(value, "depends")) {
            Map<String, Object> dependMap = YamlSupport.map(item, "depend");
            String data = YamlSupport.requireStr(dependMap, "depend.data", "data");
            DataDependDesc dataDependDesc = new DataDependDesc(data);
            String change = YamlSupport.str(dependMap, "change");
            String init = YamlSupport.str(dependMap, "init");
            if (change != null && !"".equals(change)) {
                dataDependDesc.setChange(change);
                dataDesc.addChange(dataDependDesc.getData(), dataDependDesc.getChange());
            }
            String param = YamlSupport.str(dependMap, "param");
            if (param != null && !"".equals(param)) {
                dataDependDesc.setParam(param);
            }
            String condition = YamlSupport.str(dependMap, "condition");
            if (condition != null && !"".equals(condition)) {
                dataDependDesc.setCondition(condition);
            }
            if (init != null && !"".equals(init)) {
                dataDependDesc.setInit(init);
            }
            if (dataDependDesc.getInit() != null && dataDependDesc.getChange() != null) {
                throw new YAMLParseException("The property 'init' and 'change' for depend can't set together,depend:" + data);
            }
            dataDesc.addDepend(dataDependDesc);
        }
    }

    private List<BusinessDesc> parseBusinesses(Map<String, Object> root) throws YAMLParseException {
        Object value = YamlSupport.first(root, "businesses");
        if (value == null) {
            return Collections.emptyList();
        }
        List<BusinessDesc> businessDescList = new SimpleList<BusinessDesc>();
        for (Object item : YamlSupport.list(value, "businesses")) {
            businessDescList.add(parseBusiness(YamlSupport.map(item, "business")));
        }
        return businessDescList;
    }

    private BusinessDesc parseBusiness(Map<String, Object> node) throws YAMLParseException {
        String name = YamlSupport.requireStr(node, "business.name", "name");
        log.info("Start load yaml business:" + name);
        if (DescContext.get().getBusiness(name) != null) {
            throw new YAMLParseException("The business is duplicate:" + name);
        }
        BusinessDesc businessDesc = new BusinessDesc();
        businessDesc.setName(name);
        businessDesc.setComment(YamlSupport.str(node, "desc", "comment"));

        String ruleInfo = YamlSupport.str(node, "refDom", "ref-dom");
        boolean isRefDom = false;
        if (ruleInfo != null && !"".equals(ruleInfo)) {
            parseRule(businessDesc, ruleInfo);
            isRefDom = true;
        }
        parseBusinessData(businessDesc, YamlSupport.first(node, "datas", "data"), isRefDom);
        log.info("End load yaml business:" + name);
        return businessDesc;
    }

    private void parseRule(BusinessDesc businessDesc, String rule) throws YAMLParseException {
        String ruleArray[] = rule.split(",");
        for (String ruleInfo : ruleArray) {
            String viewRule[] = ruleInfo.split(":");
            if (ConfigContextUtil.getConfigInfo().getRuleViewInfo(viewRule[0]) == null) {
                throw new YAMLParseException("The rule is not exist, rule:" + viewRule[0]);
            }
            if (ConfigContextUtil.getConfigInfo().getConnection(viewRule[1]) == null) {
                throw new YAMLParseException("The connection is not exist, rule:" + viewRule[1]);
            }
            ViewRuleDesc viewRuleDesc = new ViewRuleDesc();
            viewRuleDesc.setRuleName(viewRule[0]);
            viewRuleDesc.setDataSourceName(viewRule[1]);
            businessDesc.addViewRuleDesc(viewRuleDesc);
        }
    }

    private void parseBusinessData(BusinessDesc businessDesc, Object value, boolean isRefDom) throws YAMLParseException {
        int txCount = 0;
        for (Object item : YamlSupport.list(value, "datas")) {
            Map<String, Object> dataMap = YamlSupport.map(item, "businessData");
            ProcessDesc processDesc = new ProcessDesc();
            String begin = YamlSupport.str(dataMap, "begin");
            if (begin != null && !"".equals(begin)) {
                processDesc.setBegin(Boolean.valueOf(begin));
            }
            if (processDesc.isBegin()) {
                String transactionPolicy = YamlSupport.str(dataMap, "transactionPolicy");
                if (transactionPolicy == null || "".equals(transactionPolicy)) {
                    processDesc.setTransaction(TransactionPolicy.REQUIRE);
                } else {
                    TransactionPolicy transactionPolicyValue = convert(transactionPolicy);
                    if (transactionPolicyValue == null) {
                        throw new YAMLParseException("The property 'transactionPolicy' for data is error, transactionPolicy:" + transactionPolicy);
                    }
                    processDesc.setTransaction(transactionPolicyValue);
                }
                String dataSource = YamlSupport.str(dataMap, "refRuleConnection", "ref-rule-connection");
                if (isRefDom && (dataSource == null || "".equals(dataSource))) {
                    throw new YAMLParseException("The property 'dataSource' for data is error, it can't be empty!");
                }
                processDesc.setDataSource(dataSource);
                txCount++;
                businessDesc.add(processDesc);
                continue;
            }

            String end = YamlSupport.str(dataMap, "end");
            if (end != null && !"".equals(end)) {
                processDesc.setEnd(Boolean.valueOf(end));
            }
            if (processDesc.isEnd()) {
                businessDesc.add(processDesc);
                txCount--;
                continue;
            }

            String name = YamlSupport.str(dataMap, "name");
            processDesc.setData(name);
            if (name != null && !"".equals(name) && name.startsWith("$")) {
                processDesc.setSystem("common");
            } else {
                String refRule = YamlSupport.str(dataMap, "refRule", "ref-rule");
                String system = YamlSupport.str(dataMap, "system");
                if ((system == null || "".equals(system)) && (refRule == null || "".equals(refRule))) {
                    throw new YAMLParseException("The property 'system' for 'ref-rule' can't empty,data:" + name);
                }
                processDesc.setRule(refRule);
                processDesc.setSystem(system);
            }
            if (name != null && !"".equals(name)) {
                validate(processDesc.getSystem(), processDesc.getData());
            }

            String refRule = YamlSupport.str(dataMap, "refRule", "ref-rule");
            if (refRule != null && !"".equals(refRule)) {
                if (businessDesc.getViewRuleDesc(refRule) == null) {
                    throw new YAMLParseException("The 'ref-rule' for data is not exist, ref-rule:" + refRule);
                }
                if (processDesc.getData() != null) {
                    String refRuleReplace = YamlSupport.str(dataMap, "refRuleReplace", "ref-rule-replace");
                    if (refRuleReplace == null || "".equals(refRuleReplace)) {
                        throw new YAMLParseException("The 'ref-rule-replace' is empty for 'ref-rule':" + refRule);
                    }
                    processDesc.setRuleReplace(refRuleReplace);
                } else {
                    String refRuleRange = YamlSupport.str(dataMap, "refRuleRange", "ref-rule-range");
                    if (refRuleRange == null || "".equals(refRuleRange)) {
                        throw new YAMLParseException("The 'ref-rule-range' is empty for 'ref-rule':" + refRule);
                    }
                    String ruleArray[] = refRuleRange.split(":");
                    processDesc.setRuleStart(ruleArray[0]);
                    processDesc.setRuleEnd(ruleArray[ruleArray.length - 1]);
                }
            }

            String refreshData = YamlSupport.str(dataMap, "systemToDom", "system-to-dom");
            if (refreshData != null && !"".equals(refreshData)) {
                processDesc.setRuleRefresh(refreshData);
                processDesc.setSystemToDom(true);
            }
            refreshData = YamlSupport.str(dataMap, "domToSystem", "dom-to-system");
            if (refreshData != null && !"".equals(refreshData)) {
                processDesc.setRuleRefresh(refreshData);
                processDesc.setSystemToDom(false);
            }
            businessDesc.add(processDesc);
        }
        if (txCount != 0) {
            throw new YAMLParseException("The property 'begin' and 'end' must match,business:" + businessDesc.getName());
        }
    }

    private void validate(String system, String data) throws YAMLParseException {
        SystemDesc systemDesc = "this".equals(system) ? DescContext.get().getSystem("common") : DescContext.get().getSystem(system);
        if (systemDesc == null) {
            throw new YAMLParseException("The system is not exist:" + system);
        }
        DataDesc dataDesc = systemDesc.getData(data);
        if (dataDesc == null) {
            throw new YAMLParseException("The data is not exist:" + system + "-" + data);
        }
    }

    private TransactionPolicy convert(String transactionPolicy) {
        if ("NESTED".equals(transactionPolicy)) {
            return TransactionPolicy.NESTED;
        }
        if ("NEW".equals(transactionPolicy)) {
            return TransactionPolicy.NEW;
        }
        if ("NOSUPPORTED".equals(transactionPolicy)) {
            return TransactionPolicy.NOSUPPORTED;
        }
        if ("REQUIRE".equals(transactionPolicy)) {
            return TransactionPolicy.REQUIRE;
        }
        return null;
    }
}
