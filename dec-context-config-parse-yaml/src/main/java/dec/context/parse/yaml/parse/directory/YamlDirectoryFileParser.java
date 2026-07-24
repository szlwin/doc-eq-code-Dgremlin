package dec.context.parse.yaml.parse.directory;

import dec.context.parse.yaml.exception.YAMLParseException;
import dec.context.parse.yaml.parse.FileParser;
import dec.context.parse.yaml.parse.YamlResource;
import dec.context.parse.yaml.parse.YamlSupport;
import dec.core.context.config.model.config.ConfigConstanst;
import dec.core.context.config.model.directory.Action;
import dec.core.context.config.model.directory.ChangeInfo;
import dec.core.context.config.model.directory.DirectoryInfo;
import dec.core.context.config.model.directory.SubDirectory;
import dec.core.context.config.model.rule.RuleDefineInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class YamlDirectoryFileParser implements FileParser<List<DirectoryInfo>> {

    @Override
    public List<DirectoryInfo> parse(String filePath) throws YAMLParseException {
        List<DirectoryInfo> directoryList = new ArrayList<>();
        for (YamlResource resource : YamlSupport.findAll(filePath)) {
            Map<String, Object> root = YamlSupport.loadMap(resource);
            for (Object item : roots(root)) {
                directoryList.add(parseDirectory(YamlSupport.map(item, "directory")));
            }
        }
        setParentDirectory(directoryList);
        return directoryList;
    }

    private DirectoryInfo parseDirectory(Map<String, Object> node) throws YAMLParseException {
        DirectoryInfo directoryInfo = new DirectoryInfo();
        directoryInfo.setName(YamlSupport.requireStr(node, "directory.name", "name"));
        directoryInfo.setViewRef(YamlSupport.str(node, "viewRef", "view-ref", "view_ref"));
        String isRoot = YamlSupport.str(node, "isRoot", "is-root", "is_root");
        if (isRoot != null) {
            directoryInfo.setRoot(Boolean.valueOf(isRoot));
        }
        directoryInfo.setSubDirectories(parseSubDirectories(YamlSupport.first(node, "subDirectories", "subdirectory-info", "subdirectoryInfo")));
        directoryInfo.setActions(parseActions(YamlSupport.first(node, "actions", "action-info", "actionInfo")));
        directoryInfo.setChange(parseChange(YamlSupport.first(node, "change", "change-info", "changeInfo")));
        return directoryInfo;
    }

    private List<SubDirectory> parseSubDirectories(Object value) throws YAMLParseException {
        if (value instanceof Map) {
            value = YamlSupport.first(YamlSupport.map(value, "subDirectories"), "subdirectory", "subDirectories");
        }
        List<Object> items = YamlSupport.list(value, "subDirectories");
        if (items.isEmpty()) {
            return null;
        }
        List<SubDirectory> subDirectoryList = new ArrayList<>();
        for (Object item : items) {
            Map<String, Object> map = YamlSupport.map(item, "subDirectory");
            SubDirectory subDirectory = new SubDirectory();
            subDirectory.setRel(YamlSupport.requireStr(map, "subDirectory.rel", "rel"));
            String anyOne = YamlSupport.str(map, "anyOne", "any-one", "any_one");
            if (anyOne != null) {
                subDirectory.setAnyOne(Boolean.valueOf(anyOne));
            }
            String mutualExclusion = YamlSupport.str(map, "mutualExclusion", "mutual-exclusion", "mutual_exclusion");
            if (mutualExclusion != null) {
                subDirectory.setMutualExclusions(new HashSet<>(Arrays.asList(mutualExclusion.split("\\,"))));
            }
            subDirectoryList.add(subDirectory);
        }
        return subDirectoryList;
    }

    private List<Action> parseActions(Object value) throws YAMLParseException {
        if (value instanceof Map) {
            value = YamlSupport.first(YamlSupport.map(value, "actions"), "action", "actions");
        }
        List<Object> items = YamlSupport.list(value, "actions");
        if (items.isEmpty()) {
            return null;
        }
        List<Action> actions = new ArrayList<>();
        for (Object item : items) {
            Map<String, Object> map = YamlSupport.map(item, "action");
            Action action = new Action();
            action.setName(YamlSupport.requireStr(map, "action.name", "name"));
            action.setRefRule(YamlSupport.str(map, "refRule", "ref-rule", "ref_rule"));
            actions.add(action);
        }
        return actions;
    }

    private ChangeInfo parseChange(Object value) throws YAMLParseException {
        Map<String, Object> map = YamlSupport.map(value, "change");
        if (map == null) {
            return null;
        }
        ChangeInfo changeInfo = new ChangeInfo();
        String property = YamlSupport.str(map, "property");
        if (property != null && !"".equals(property)) {
            changeInfo.setProperty(property.split("\\,"));
        }
        String express = normalizeGrammer(YamlSupport.str(map, "process", "grammer", "express", "expression"));
        if (express != null) {
            RuleDefineInfo ruleDefineInfo = new RuleDefineInfo();
            ruleDefineInfo.setGrammer(express);
            ruleDefineInfo.setType(ConfigConstanst.RULE_TYPE_EXECUTE_GRAMMER);
            changeInfo.setRuleDefineInfo(ruleDefineInfo);
            return changeInfo;
        }
        return null;
    }

    private List<Object> roots(Map<String, Object> root) throws YAMLParseException {
        Object directories = YamlSupport.first(root, "directories", "directoryList");
        if (directories != null) {
            return YamlSupport.list(directories, "directories");
        }
        Object directory = YamlSupport.first(root, "directory");
        if (directory != null) {
            return YamlSupport.list(directory, "directory");
        }
        Object mapping = YamlSupport.first(root, "directory-config", "directoryConfig");
        if (mapping != null) {
            return YamlSupport.list(YamlSupport.first(YamlSupport.map(mapping, "directory-config"), "directory", "directories"), "directories");
        }
        return YamlSupport.list(root, "directory");
    }

    private void setParentDirectory(List<DirectoryInfo> directoryList) {
        Map<String, DirectoryInfo> directoryInfoMap = new HashMap<>();
        for (DirectoryInfo directoryInfo : directoryList) {
            directoryInfoMap.put(directoryInfo.getName(), directoryInfo);
        }
        for (DirectoryInfo directoryInfo : directoryList) {
            if (directoryInfo.getSubDirectories() != null) {
                for (SubDirectory subDirectory : directoryInfo.getSubDirectories()) {
                    DirectoryInfo sub = directoryInfoMap.get(subDirectory.getRel());
                    if (sub != null) {
                        sub.setParentDirectory(directoryInfo);
                    }
                }
            }
        }
    }

    private String normalizeGrammer(String grammer) {
        if (grammer == null) {
            return null;
        }
        return grammer.replaceAll("\\s+", " ").trim();
    }
}
