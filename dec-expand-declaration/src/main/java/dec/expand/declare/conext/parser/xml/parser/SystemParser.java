package dec.expand.declare.conext.parser.xml.parser;

import dec.expand.declare.conext.DescContext;
import dec.expand.declare.conext.desc.data.DataDependDesc;
import dec.expand.declare.conext.desc.data.DataDesc;
import dec.expand.declare.conext.desc.data.DataTypeEnum;
import dec.expand.declare.conext.desc.system.SystemDesc;
import dec.expand.declare.conext.parser.xml.exception.XMLParseException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class SystemParser {

    private final static Logger log = LoggerFactory.getLogger(SystemParser.class);

    public SystemDesc parser(Element element) throws XMLParseException {
        String name = element.attributeValue("name");
        log.info("Start load system:" + name);
        if (DescContext.get().getSystem(name) != null) {
            throw new XMLParseException("The system is duplicate:" + name);
        }

        SystemDesc systemDesc = new SystemDesc();
        systemDesc.setName(name);
        systemDesc.setComment(element.attributeValue("desc"));

        parserData(systemDesc, element.element("datas"));
        log.info("End load system:" + name);
        return systemDesc;
    }

    private void parserData(SystemDesc systemDesc, Element element) throws XMLParseException {
        if (element == null) {
            return;
        }
        Iterator<?> it = element.elementIterator("data");
        while (it.hasNext()) {
            Element dataElement = (Element) it.next();
            String name = dataElement.attributeValue("name");
            if (name == null || "".equals(name)) {
                throw new XMLParseException("The property 'name' for data can't empty");
            }

            if (systemDesc.getData(name) != null) {
                throw new XMLParseException("The data is duplicate:" + name);
            }
            DataDesc dataDesc = new DataDesc();
            dataDesc.setName(name);
            dataDesc.setComment(dataElement.attributeValue("desc"));

            if ("common".equals(systemDesc.getName())) {
                dataDesc.setType(DataTypeEnum.CACHE);
                dataDesc.setCachePrior(false);
            }
            String type = dataElement.attributeValue("type");
            if (type == null || "".equals(type)) {
                dataDesc.setType(DataTypeEnum.CACHE);
            } else {
                if ("persistent".equals(type)) {
                    dataDesc.setType(DataTypeEnum.PERSISTENT);
                } else if ("cache".equals(type)) {
                    dataDesc.setType(DataTypeEnum.CACHE);
                } else {
                    throw new XMLParseException("The property 'type' for data is error,value:" + type);
                }
            }

            String cachePrior = dataElement.attributeValue("isCachePrior");
            if (cachePrior == null || "".equals(cachePrior)) {
                dataDesc.setCachePrior(false);
            } else {
                dataDesc.setCachePrior(Boolean.valueOf(dataElement.attributeValue("isCachePrior")));
            }

            parserDataDesc(dataDesc, element.element("depends"));
            systemDesc.addData(dataDesc);
        }
    }

    private void parserDataDesc(DataDesc dataDesc, Element element) throws XMLParseException {
        if (element == null) {
            return;
        }
        Iterator<?> it = element.elementIterator("depend");
        while (it.hasNext()) {
            Element dependDescElement = (Element) it.next();
            String data = dependDescElement.attributeValue("data");
            if (data == null || "".equals(data)) {
                throw new XMLParseException("The property 'data' for data depend can't empty,data:" + dataDesc.getName());
            }
            DataDependDesc dataDependDesc = new DataDependDesc(data);

            dataDependDesc.setChange(dependDescElement.attributeValue("change"));
            dataDependDesc.setParam(dependDescElement.attributeValue("param"));
            dataDependDesc.setCondition(dependDescElement.attributeValue("condition"));
            dataDependDesc.setInit(dependDescElement.attributeValue("init"));

            if (dataDependDesc.getInit() != null
                    && dataDependDesc.getChange() != null
                    && !"".equals(dataDependDesc.getInit())
                    && !"".equals(dataDependDesc.getChange())) {
                throw new XMLParseException("The property 'init' and 'change' for depend can't set together,depend:" + dataDependDesc.getData());
            }
            dataDesc.addDepend(dataDependDesc);
        }
    }
}
