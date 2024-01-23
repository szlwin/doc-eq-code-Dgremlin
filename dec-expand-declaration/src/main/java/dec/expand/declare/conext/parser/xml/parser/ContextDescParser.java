package dec.expand.declare.conext.parser.xml.parser;

import dec.expand.declare.collections.SimpleList;
import dec.expand.declare.conext.DescContext;
import dec.expand.declare.conext.desc.business.BusinessDesc;
import dec.expand.declare.conext.desc.system.SystemDesc;
import dec.expand.declare.conext.parser.xml.exception.XMLParseException;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ContextDescParser {

    private final static Logger log = LoggerFactory.getLogger(ContextDescParser.class);

    public void parser(String filePath) throws XMLParseException {

        log.info("Start parser the file:" + filePath);
        SAXReader saxReader = new SAXReader();
        Document doc = null;

        try {
            if (filePath.startsWith("classpath:")) {
                filePath = filePath.substring("classpath:".length());

                InputStream fileStream
                        = SystemDesc.class
                        .getClassLoader()
                        .getResourceAsStream(filePath);

                doc = saxReader.read(fileStream);
            } else {
                doc = saxReader.read(new File(filePath));
            }

            DescContext.get().addSystem(parserSystem(doc));

            DescContext.get().addBusiness(parserBusiness(doc));
        } catch (Exception e) {
            log.error("Parse file error:" + filePath, e);
            throw new XMLParseException(e);
        }
        log.info("end parser the file:" + filePath);
    }

    private List<SystemDesc> parserSystem(Document doc) throws XMLParseException {

        Element element = doc.getRootElement().element("systems");

        if (element == null)
            return Collections.emptyList();
        List<SystemDesc> systemDescList = new SimpleList<>();

        Iterator<?> it = element.elementIterator("system");
        SystemParser systemParser = new SystemParser();
        while (it.hasNext()) {
            systemDescList.add(systemParser.parser((Element) it.next()));
        }
        return systemDescList;
    }

    private List<BusinessDesc> parserBusiness(Document doc) throws XMLParseException {

        Element element = doc.getRootElement().element("businesses");

        if (element == null)
            return Collections.emptyList();
        List<BusinessDesc> businessDescList = new SimpleList<>();

        Iterator<?> it = element.elementIterator("business");
        BusinessParser businessParser = new BusinessParser();
        while (it.hasNext()) {
            businessDescList.add(businessParser.parser((Element) it.next()));
        }

        return businessDescList;
    }
}
