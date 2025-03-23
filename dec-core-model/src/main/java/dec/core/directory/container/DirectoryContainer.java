package dec.core.directory.container;

import artoria.beans.BeanUtils;
import dec.core.context.config.model.directory.DirectoryInfo;
import dec.core.context.config.model.directory.SubDirectory;
import dec.core.context.config.model.rule.RuleDefineInfo;
import dec.core.context.config.utils.ConfigContextUtil;
import dec.core.context.data.ModelData;
import dec.core.directory.action.DirectoryAction;
import dec.core.directory.exception.DirectoryException;
import dec.core.session.SimpleSession;

import java.util.*;

public class DirectoryContainer {

    private ModelData modelData;

    private Map<String, DirectoryAction> actionMap = new HashMap<>();

    private Integer type;

    private String root;

    private String start;

    private String end;

    private String[] include;

    private String[] exclude;

    private String[] propertyArray;

    private List findData;

    public DirectoryContainer(ModelData modelData) {
        this.modelData = modelData;
    }

    public <T> List<T> getFindData(Class cls) {
        return BeanUtils.beanToBeanInList(findData, cls);
    }

    public DirectoryContainer invoke() throws DirectoryException {
        if (type == 1) {
            find();
        }
        return this;
    }

    public DirectoryContainer find(String directory) {
        type = 1;
        this.root = directory;
        return this;
    }

    public DirectoryContainer execute(String directory) {
        type = 2;
        this.root = directory;
        return this;
    }

    public DirectoryContainer start(String directory) {
        this.start = directory;
        return this;
    }

    public DirectoryContainer end(String directory) {
        this.end = directory;
        return this;
    }

    public DirectoryContainer with(String name) {
        return this;
    }

    public DirectoryContainer withProperty(String[] property) {
        return this;
    }

    public DirectoryContainer with(String name, String[] property) {
        return this;
    }

    public DirectoryContainer include(String... directory) {
        return this;
    }

    public DirectoryContainer exclude(String... directory) {
        return this;
    }

    public DirectoryContainer equals(String directory) {
        this.start = directory;
        this.end = directory;
        return this;
    }

    public DirectoryContainer addAction(String name, DirectoryAction directoryAction) {
        actionMap.put(name, directoryAction);
        return this;
    }

    private DirectoryContainer() {

    }


    private void find() {

        DirectoryInfo directoryInfo = ConfigContextUtil.getConfigInfo().getDirectory(root);
        if (directoryInfo == null) {
            throw new DirectoryException("The directory " + root + " is not exist!");
        }
        if (!directoryInfo.isRoot()) {
            throw new DirectoryException("The directory " + root + " must be root!");
        }
        if (start != null && !"".equals(start) && (end == null || "".equals(end))) {
            throw new DirectoryException("The end directory is empty!");
        }

        Set<String> subObjectSet = new HashSet<>();
        String condition = getCondition(subObjectSet);
        //directoryInfo.getViewRef()
        String sql = "select * from " + modelData.getViewInfo().getTargetMain().getName() + " where " + condition;
        System.out.println(sql.trim());
        SimpleSession simpleSession = new SimpleSession("con1");
        try {
            simpleSession.begian();
            Collection<Map<String, Object>> dataList = simpleSession.query(sql.trim(), new HashMap<>());
            simpleSession.close();
            System.out.println("data:" + dataList);
            findData = new ArrayList<>();
            findData.addAll(dataList);
        } catch (Exception e) {
            e.printStackTrace();
        }


        System.out.println(condition);
    }

    private String getCondition(Set<String> subObjectSet) {
        String condition = "";
        if (start.equals(end)) {
            condition = condition + getDirectoryCondition(ConfigContextUtil.getConfigInfo().getDirectory(start), subObjectSet) + " ";
        } else {
            DirectoryInfo currentDirectory = ConfigContextUtil.getConfigInfo().getDirectory(start);
            do {
                condition = condition + getDirectoryCondition(currentDirectory, subObjectSet) + " ";
                if (condition.endsWith(" and  ")) {
                    condition = condition.substring(0, condition.length() - 5);
                }
                if (end.equals(currentDirectory.getName())) {
                    break;
                }
                condition = condition + " or ";
                currentDirectory = currentDirectory.getParentDirectory();
            } while (currentDirectory != null);
        }
        return condition;
    }

    private String getDirectoryCondition(DirectoryInfo directoryInfo, Set<String> subObjectSet) {
        if (directoryInfo.getChange() != null && directoryInfo.getChange().getRuleDefineInfo() != null) {
            RuleDefineInfo ruleDefineInfo = directoryInfo.getChange().getRuleDefineInfo();
            if (directoryInfo.getChange().getProperty() != null) {
                subObjectSet.addAll(Arrays.asList(directoryInfo.getChange().getProperty()));
            }
            return ruleDefineInfo.getGrammer().replaceAll(":", "=")
                    .replaceAll(";", " and ")
                    .replaceAll("\n", "");
        } else {
            String[] conditionArray = new String[2];
            List<SubDirectory> subDirectoryList = directoryInfo.getSubDirectories();
            if (subDirectoryList == null) {
                return "";
            }
            for (SubDirectory subDirectory : subDirectoryList) {
                String condition = getDirectoryCondition(ConfigContextUtil.getConfigInfo().getDirectory(subDirectory.getRel()), subObjectSet);
                if (subDirectory.isAnyOne()) {
                    if (conditionArray[1] == null) {
                        conditionArray[1] = condition;
                    } else {
                        conditionArray[1] = conditionArray[1] + " or " + condition;
                    }
                } else {
                    if (conditionArray[0] == null) {
                        conditionArray[0] = condition;
                    } else {
                        conditionArray[0] = conditionArray[0] + " and " + condition;
                    }
                }
            }

            if (conditionArray[1] == null) {
                return conditionArray[0];
            } else {
                return "(" + conditionArray[0] + ") and (" + conditionArray[1] + ")";
            }
        }
    }
}
