package dec.expand.declare.conext.desc.business;


import dec.expand.declare.collections.SimpleList;
import dec.expand.declare.conext.desc.Desc;
import dec.expand.declare.conext.desc.process.ProcessDesc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusinessDesc extends Desc {

    private String name;

    private List<ProcessDesc> processes;

    private Map<String, ViewRuleDesc> viewRuleDescMap;

    public void add(ProcessDesc process) {

        if (processes == null) {
            processes = new SimpleList<>();
        }

        processes.add(process);
    }

    public List<ProcessDesc> getProcesses() {
        return processes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProcesses(List<ProcessDesc> processes) {
        this.processes = processes;
    }

    public void addViewRuleDesc(ViewRuleDesc viewRuleDesc) {
		if (viewRuleDescMap == null){
			viewRuleDescMap = new HashMap<>();
		}
        viewRuleDescMap.put(viewRuleDesc.getRuleName(), viewRuleDesc);
    }

    public ViewRuleDesc getViewRuleDesc(String name) {
		if (viewRuleDescMap == null){
			return null;
		}
        return viewRuleDescMap.get(name);
    }

    public Map<String, ViewRuleDesc> getAllViewRuleDesc() {
        return viewRuleDescMap;
    }
}
