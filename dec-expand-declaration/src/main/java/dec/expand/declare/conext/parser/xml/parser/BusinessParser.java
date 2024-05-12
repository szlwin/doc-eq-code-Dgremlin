package dec.expand.declare.conext.parser.xml.parser;

import dec.core.context.config.utils.ConfigContextUtil;
import dec.expand.declare.conext.DescContext;
import dec.expand.declare.conext.desc.business.BusinessDesc;
import dec.expand.declare.conext.desc.business.ViewRuleDesc;
import dec.expand.declare.conext.desc.data.DataDesc;
import dec.expand.declare.conext.desc.process.ProcessDesc;
import dec.expand.declare.conext.desc.process.TransactionPolicy;
import dec.expand.declare.conext.desc.system.SystemDesc;
import dec.expand.declare.conext.parser.xml.exception.XMLParseException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class BusinessParser {

    private final static Logger log = LoggerFactory.getLogger(BusinessParser.class);

    public BusinessDesc parser(Element element) throws XMLParseException {
        String name = element.attributeValue("name");
        log.info("Start load business:" + name);
        if (DescContext.get().getBusiness(name) != null) {
            throw new XMLParseException("The business is duplicate:" + name);
        }

        BusinessDesc businessDesc = new BusinessDesc();
        businessDesc.setName(name);
        businessDesc.setComment(element.attributeValue("desc"));

        String ruleInfo = element.attributeValue("ref-dom");
        boolean isRefDom = false;
        if (ruleInfo != null && !"".equals(ruleInfo)) {
            parserRule(businessDesc, ruleInfo);
            isRefDom = true;
        }
        parserData(businessDesc, element.element("datas"), isRefDom);
        log.info("End load business:" + name);
        return businessDesc;
    }

    private void parserRule(BusinessDesc businessDesc, String rule) throws XMLParseException {
        String ruleArray[] = rule.split(",");
        ViewRuleDesc viewRuleDescArray[] = new ViewRuleDesc[ruleArray.length];
        for (String ruleInfo : ruleArray) {
            String viewRule[] = ruleInfo.split(":");
            if (ConfigContextUtil.getConfigInfo().getRuleViewInfo(viewRule[0]) == null) {
                throw new XMLParseException("The rule is not exist, rule:" + viewRule[0]);
            }

            if (ConfigContextUtil.getConfigInfo().getConnection(viewRule[1]) == null) {
                throw new XMLParseException("The connection is not exist, rule:" + viewRule[1]);
            }

            ViewRuleDesc viewRuleDesc = new ViewRuleDesc();
            viewRuleDesc.setRuleName(viewRule[0]);
            viewRuleDesc.setDataSourceName(viewRule[1]);
            businessDesc.addViewRuleDesc(viewRuleDesc);
        }
    }

    private void parserData(BusinessDesc businessDesc, Element element, boolean isRefDom) throws XMLParseException {
        if (element == null) {
            return;
        }
        Iterator<?> it = element.elementIterator("data");
        int txCount = 0;
        int txIndex = 0;

        while (it.hasNext()) {
            Element dataElement = (Element) it.next();
            ProcessDesc processDesc = new ProcessDesc();

            String begin = dataElement.attributeValue("begin");
            if (begin != null && !"".equals(begin)) {
                processDesc.setBegin(Boolean.valueOf(begin));
            }

            if (processDesc.isBegin()) {
                String transactionPolicy = dataElement.attributeValue("transactionPolicy");
                if (transactionPolicy == null || "".equals(transactionPolicy)) {
                    processDesc.setTransaction(TransactionPolicy.REQUIRE);
                } else {
                    TransactionPolicy transactionPolicyValue = this.convert(transactionPolicy);
                    if (transactionPolicyValue == null) {
                        throw new XMLParseException("The property 'transactionPolicy' for data is error, transactionPolicy:" + transactionPolicy);
                    }
                }

                String dataSource = dataElement.attributeValue("ref-rule-connection");
                if (isRefDom && (dataSource == null || "".equals(dataSource))) {
                    throw new XMLParseException("The property 'dataSource' for data is error, it can't be empty!");
                }
                processDesc.setDataSource(dataSource);

                txCount++;
                txIndex++;
                businessDesc.add(processDesc);
                continue;
            }

            String end = dataElement.attributeValue("end");
            if (end != null && !"".equals(end)) {
                processDesc.setEnd(Boolean.valueOf(end));
            }

            if (processDesc.isEnd()) {
                businessDesc.add(processDesc);
                txCount--;
                continue;
            }

            String name = dataElement.attributeValue("name");

            processDesc.setData(name);
            if (name != null && !"".equals(name) && name.startsWith("$")) {
                processDesc.setSystem("common");
            } else {
                String refRule = dataElement.attributeValue("ref-rule");

                String system = dataElement.attributeValue("system");

                if ((system == null || "".equals(system)) && (refRule == null || "".equals(refRule))) {
                    throw new XMLParseException("The property 'system' for 'ref-rule' can't empty,data:" + name);
                }
                processDesc.setRule(refRule);
                processDesc.setSystem(system);
            }

            if (!(name == null || "".equals(name))) {
                validate(processDesc.getSystem(), processDesc.getData());
            }

            String refRule = dataElement.attributeValue("ref-rule");
            if (refRule != null && !"".equals(refRule)) {

                if (businessDesc.getViewRuleDesc(refRule) == null) {
                    throw new XMLParseException("The 'ref-rule' for data is not exist, ref-rule:" + refRule);
                }

                if (processDesc.getData() != null) {
                    String refRuleReplace = dataElement.attributeValue("ref-rule-replace");
                    if (refRuleReplace == null || "".equals(refRuleReplace)) {
                        throw new XMLParseException("The 'ref-rule-replace' is empty for 'ref-rule':" + refRule);
                    }
                    processDesc.setRuleReplace(refRuleReplace);
                } else {
                    String refRuleRange = dataElement.attributeValue("ref-rule-range");
                    if (refRuleRange == null || "".equals(refRuleRange)) {
                        throw new XMLParseException("The 'ref-rule-range' is empty for 'ref-rule':" + refRule);
                    }
                    String ruleArray[] = refRuleRange.split(":");
                    processDesc.setRuleStart(ruleArray[0]);
                    processDesc.setRuleEnd(ruleArray[ruleArray.length - 1]);
                }
            }

            String refreshData = dataElement.attributeValue("system-to-dom");
            if(refreshData != null && !"".equals(refreshData)){
                processDesc.setRuleRefresh(refreshData);
                processDesc.setSystemToDom(true);
            }

            refreshData = dataElement.attributeValue("dom-to-system");
            if(refreshData != null && !"".equals(refreshData)){
                processDesc.setRuleRefresh(refreshData);
                processDesc.setSystemToDom(false);
            }

            businessDesc.add(processDesc);
        }
        if (txCount != 0) {
            throw new XMLParseException("The property 'begin' and 'end' must match,business:" + businessDesc.getName());
        }
    }

    private void validate(String system, String data) throws XMLParseException {
        SystemDesc systemDesc = null;
        if ("this".equals(system)) {
            systemDesc = DescContext.get().getSystem("common");
        } else {
            systemDesc = DescContext.get().getSystem(system);
        }

        if (systemDesc == null) {
            log.error("The system is not exist:" + system);
            throw new XMLParseException("The system is not exist:" + system);
        }
        DataDesc dataDesc = systemDesc.getData(data);
        if (dataDesc == null) {
            log.error("The data is not exist:" + system + "-" + data);
            throw new XMLParseException("The data is not exist:" + system + "-" + data);
        }
    }

    private TransactionPolicy convert(String transactionPolicy) {
        switch (transactionPolicy) {
            case "NESTED":
                return TransactionPolicy.NESTED;
            case "NEW":
                return TransactionPolicy.NEW;
            case "NOSUPPORTED":
                return TransactionPolicy.NOSUPPORTED;
            case "REQUIRE":
                return TransactionPolicy.REQUIRE;
        }
        return null;
    }
}
