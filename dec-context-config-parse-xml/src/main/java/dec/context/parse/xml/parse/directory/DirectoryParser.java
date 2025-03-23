package dec.context.parse.xml.parse.directory;

import dec.context.parse.xml.exception.XMLParseException;
import dec.context.parse.xml.parse.AbstarctElementsParser;
import dec.context.parse.xml.parse.config.ConfigConstanst;
import dec.core.context.config.model.directory.Action;
import dec.core.context.config.model.directory.ChangeInfo;
import dec.core.context.config.model.directory.DirectoryInfo;
import dec.core.context.config.model.directory.SubDirectory;
import dec.core.context.config.model.rule.RuleDefineInfo;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DirectoryParser extends AbstarctElementsParser {

    private final static Logger log = LoggerFactory.getLogger(DirectoryParser.class);

    public DirectoryInfo parse(Element element) throws XMLParseException {
        DirectoryInfo directoryInfo = new DirectoryInfo();
        //Iterator<Element> list = element.elementIterator(ServiceInfo.TSAKINFO);

        String name = element.attributeValue(DirectoryInfo.NAME);
        log.info("Load the directory:" + name + " start!");
        directoryInfo.setName(name);
        directoryInfo.setViewRef(element.attributeValue(DirectoryInfo.VIEW_REF));
        String isRoot = element.attributeValue(DirectoryInfo.IS_ROOT);
        if (isRoot != null) {
            directoryInfo.setRoot(Boolean.valueOf(isRoot));
        }

        directoryInfo.setSubDirectories(parseSubDirectory(element.element(DirectoryInfo.SUB_DIRECTORY_INFO)));
        directoryInfo.setActions(parseAction(element.element(DirectoryInfo.ACTION), directoryInfo.getViewRef()));
        directoryInfo.setChange(parseChange(element.element(DirectoryInfo.CHANGE)));
        log.info("Load the directory:" + directoryInfo.getName() + " success!");
        return directoryInfo;
    }

    private List<SubDirectory> parseSubDirectory(Element element) throws XMLParseException {
        if (element == null) {
            return null;
        }
        Iterator it = element.elementIterator("subdirectory");
        if (!it.hasNext()) {
            return null;
        }

        List<SubDirectory> subDirectoryList = new ArrayList<>();
        while (it.hasNext()) {
            SubDirectory subDirectory = new SubDirectory();
            Element subDirectoryElement = (Element) it.next();
            subDirectory.setRel(subDirectoryElement.attributeValue("rel"));
            String anyOne = subDirectoryElement.attributeValue("any-one");
            if (anyOne != null) {
                subDirectory.setAnyOne(Boolean.valueOf(anyOne));
            }
            String mutualExclusion = subDirectoryElement.attributeValue("mutual-exclusion");
            if (mutualExclusion != null) {
                Set<String> set = new HashSet<>();
                set.addAll(Arrays.asList(mutualExclusion.split("\\,")));
                subDirectory.setMutualExclusions(set);
            }
            subDirectoryList.add(subDirectory);
        }
        return subDirectoryList;
    }

    private List<Action> parseAction(Element element, String viewName) throws XMLParseException {
        if (element == null) {
            return null;
        }
        Iterator it = element.elementIterator("action");
        if (!it.hasNext()) {
            return null;
        }
        List<Action> actions = new ArrayList<>();
        ActionRuleParser actionRuleParser = new ActionRuleParser();
        while (it.hasNext()) {
            Action action = new Action();
            Element actionElement = (Element) it.next();
            action.setRuleList(actionRuleParser.parse(actionElement, viewName));
            action.setName(actionElement.attributeValue("name"));
            actions.add(action);
        }
        return actions;
    }

    private ChangeInfo parseChange(Element element) throws XMLParseException {
        if (element == null) {
            return null;
        }
        ChangeInfo changeInfo = new ChangeInfo();
        String property = element.attributeValue("property");
        if (property != null && !"".equals(property)) {
            changeInfo.setProperty(property.split("\\,"));
        }
        String express = element.getTextTrim();
        if (express != null) {
            RuleDefineInfo ruleDefineInfo = new RuleDefineInfo();
            ruleDefineInfo.setGrammer(express);
            ruleDefineInfo.setType(ConfigConstanst.RULE_TYPE_EXECUTE_GRAMMER);
            changeInfo.setRuleDefineInfo(ruleDefineInfo);
            return changeInfo;
        }
        return null;
    }
}
