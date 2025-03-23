package dec.context.parse.xml.parse.directory;

import dec.context.parse.xml.exception.XMLParseException;
import dec.context.parse.xml.parse.AbstarctElementsParser;
import dec.context.parse.xml.parse.AbstractFileParser;
import dec.context.parse.xml.parse.Resource;
import dec.core.context.config.model.config.data.ConfigBaseData;
import dec.core.context.config.model.directory.DirectoryInfo;
import dec.core.context.config.model.directory.SubDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.orm.common.xml.model.service.ServiceInfo;
//import com.orm.common.xml.parse.AbstarctElementsParser;
//import com.orm.common.xml.parse.AbstractFileParser;

public class DirectoryFileParser extends AbstractFileParser {

    private final static Logger log = LoggerFactory.getLogger(DirectoryFileParser.class);

    @Override
    protected AbstarctElementsParser getParser() {
        return new DirectoryParser();
    }

    @Override
    protected String getNodeNme() {
        return "directory";
    }

    @Override
    public List<ConfigBaseData> parse(String filePath) throws XMLParseException {
        log.info("Start parse filePath:" + filePath);


        elementsParser = getParser();

        nodeName = getNodeNme();

        List<Resource> fileUrls = findAllFileResources(filePath);

        List<ConfigBaseData> directoryList;
        try {
            directoryList = this.parseByFile(fileUrls);

            if (directoryList != null && !directoryList.isEmpty()) {
                Map<String, DirectoryInfo> directoryInfoMap = new HashMap<>();
                for (ConfigBaseData configBaseData : directoryList) {
                    DirectoryInfo directoryInfo = (DirectoryInfo) configBaseData;
                    directoryInfoMap.put(directoryInfo.getName(), directoryInfo);
                }

                for (ConfigBaseData configBaseData : directoryList) {
                    DirectoryInfo directoryInfo = (DirectoryInfo) configBaseData;
                    if (directoryInfo.getSubDirectories() != null) {
                        for (SubDirectory subDirectory : directoryInfo.getSubDirectories()) {
                            System.out.println(subDirectory.getRel());
                            directoryInfoMap.get(subDirectory.getRel()).setParentDirectory(directoryInfo);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new XMLParseException(e);
        }
        return directoryList;
    }

}
