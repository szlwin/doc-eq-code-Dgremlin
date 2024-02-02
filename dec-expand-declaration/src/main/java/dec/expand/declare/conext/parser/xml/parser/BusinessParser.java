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

        String ruleInfo = element.attributeValue("ref-rule");
        if (ruleInfo != null && !"".equals(ruleInfo)) {
            parserRule(businessDesc, ruleInfo);
        }
        parserData(businessDesc, element.element("datas"));
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

            if (ConfigContextUtil.getConfigInfo().getDataSource(viewRule[1]) == null) {
                throw new XMLParseException("The dataSource is not exist, rule:" + viewRule[1]);
            }

            ViewRuleDesc viewRuleDesc = new ViewRuleDesc();
            viewRuleDesc.setRuleName(viewRule[0]);
            viewRuleDesc.setDataSourceName(viewRule[1]);
            businessDesc.addViewRuleDesc(viewRuleDesc);
        }
    }

    private void parserData(BusinessDesc businessDesc, Element element) throws XMLParseException {
        if (element == null) {
            return;
        }
        Iterator<?> it = element.elementIterator("data");
        int txCount = 0;
        while (it.hasNext()) {
            Element dataElement = (Element) it.next();
            String name = dataElement.attributeValue("name");
            if (name == null || "".equals(name)) {
                throw new XMLParseException("The property 'name' for data can't empty");
            }

            ProcessDesc processDesc = new ProcessDesc();
            processDesc.setData(name);
            if (name.startsWith("$")) {
                processDesc.setSystem("this");
            } else {
                String system = dataElement.attributeValue("system");

                if (system == null || "".equals(system)) {
                    throw new XMLParseException("The property 'system' for data can't empty,data:" + name);
                }
                processDesc.setSystem(system);
            }

            validate(processDesc.getSystem(), processDesc.getData());

            String begin = dataElement.attributeValue("begin");
            if (begin == null || "".equals(begin)) {
                processDesc.setBegin(false);
            } else {
                processDesc.setBegin(Boolean.valueOf(begin));
            }
            if (processDesc.isBegin()) {
                String transactionPolicy = dataElement.attributeValue("transactionPolicy");
                if (transactionPolicy == null || "".equals(transactionPolicy)) {
                    processDesc.setTransaction(TransactionPolicy.REQUIRE);
                } else {
                    TransactionPolicy transactionPolicyValue = this.convert(transactionPolicy);
                    if (transactionPolicyValue == null) {
                        throw new XMLParseException("The property 'transactionPolicy' for data is error,data:" + processDesc.getData() + ",transactionPolicy:" + transactionPolicy);
                    }
                }
                txCount++;
            }


            String end = dataElement.attributeValue("end");
            if (end == null || "".equals(end)) {
                processDesc.setEnd(false);
            } else {
                processDesc.setEnd(Boolean.valueOf(end));
            }

            String onlyEnd = dataElement.attributeValue("only-end");
            if (onlyEnd != null && !"".equals(onlyEnd)) {
                processDesc.setOnlyEnd(Boolean.valueOf(onlyEnd));
            }

            if (processDesc.isEnd() || processDesc.isOnlyEnd()) {
                txCount--;
            }

            String refRule = dataElement.attributeValue("ref-rule");
            if (refRule != null && !"".equals(refRule)) {

                if (businessDesc.getViewRuleDesc(refRule) == null) {
                    throw new XMLParseException("The 'ref-rule' for data is not exist,data:" + processDesc.getData() + ",ref-rule:" + refRule);
                }

                String refRuleStart = dataElement.attributeValue("ref-rule-start");
                if (refRuleStart == null || "".equals(refRuleStart)) {
                    throw new XMLParseException("The 'ref-rule-start' for data is empty,data:" + processDesc.getData());
                }

                String refRuleEnd = dataElement.attributeValue("ref-rule-end");
                if (refRuleEnd == null || "".equals(refRuleEnd)) {
                    throw new XMLParseException("The 'ref-rule-end' for data is empty,data:" + processDesc.getData());
                }
                processDesc.setRule(refRule);
                processDesc.setRuleStart(refRuleStart);
                processDesc.setRuleEnd(refRuleEnd);

                String refRuleDataSource = dataElement.attributeValue("ref-rule-dataSource");
                if (refRuleDataSource != null && !"".equals(refRuleDataSource)) {
                    processDesc.setRuleDataSource(refRuleDataSource);
                } else {
                    processDesc.setRuleDataSource(businessDesc.getViewRuleDesc(refRule).getDataSourceName());
                }
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
