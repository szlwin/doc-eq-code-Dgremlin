package dec.expand.declare.conext.parser.xml.parser;

import dec.expand.declare.conext.DescContext;
import dec.expand.declare.conext.desc.business.BusinessDesc;
import dec.expand.declare.conext.desc.data.DataDesc;
import dec.expand.declare.conext.desc.process.ProcessDesc;
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

        parserData(businessDesc, element.element("datas"));
        log.info("End load business:" + name);
        return businessDesc;
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
                txCount++;
            }

            String end = dataElement.attributeValue("end");
            if (end == null || "".equals(end)) {
                processDesc.setEnd(false);
            } else {
                processDesc.setEnd(Boolean.valueOf(end));
            }

            if (processDesc.isEnd()) {
                txCount--;
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
}
