package dec.expand.declare.conext.desc.business;



import java.util.List;

import dec.expand.declare.collections.SimpleList;
import dec.expand.declare.conext.desc.Desc;
import dec.expand.declare.conext.desc.process.ProcessDesc;

public class BusinessDesc extends Desc{

	private String name;
	
	private List<ProcessDesc> processes;
	
	public void add(ProcessDesc process){
		
		if(processes == null){
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
	
	
}
