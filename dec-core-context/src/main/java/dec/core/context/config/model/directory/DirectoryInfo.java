package dec.core.context.config.model.directory;

import dec.core.context.config.model.config.data.ConfigBaseData;
import dec.core.context.config.model.rule.RuleDefineInfo;

import java.util.List;

public class DirectoryInfo extends ConfigBaseData {

    public static final String NAME = "name";
    public static final String VIEW_REF = "view-ref";
    public static final String IS_ROOT = "is-root";
    public static final String SUB_DIRECTORY_INFO = "subdirectory-info";
    public static final String ACTION = "action-info";
    public static final String CHANGE = "change-info";

    private String name;

    private String viewRef;

    private boolean isRoot;

    private List<Action> actions;

    private ChangeInfo change;

    private DirectoryInfo parentDirectory;

    private List<SubDirectory> subDirectories;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChangeInfo getChange() {
        return change;
    }

    public void setChange(ChangeInfo change) {
        this.change = change;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean root) {
        isRoot = root;
    }

    public String getViewRef() {
        return viewRef;
    }

    public void setViewRef(String viewRef) {
        this.viewRef = viewRef;
    }

    public List<SubDirectory> getSubDirectories() {
        return subDirectories;
    }

    public void setSubDirectories(List<SubDirectory> subDirectorys) {
        this.subDirectories = subDirectorys;
    }

    public DirectoryInfo getParentDirectory() {
        return parentDirectory;
    }

    public void setParentDirectory(DirectoryInfo parentDirectory) {
        this.parentDirectory = parentDirectory;
    }
}
