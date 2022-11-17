package dec.expand.declare.conext.desc.event;

import dec.expand.declare.conext.desc.Desc;

public class EventMappingDesc extends Desc{

	private String event;
	
	private String system;
	
	private String mappingEevent;

	public EventMappingDesc(String event, String system, String mapping){
		this.setEvent(event);
		this.setSystem(system);
		this.setMappingEevent(mapping);
	}
	
	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getSystem() {
		return system;
	}

	public void setSystem(String system) {
		this.system = system;
	}

	public String getMappingEevent() {
		return mappingEevent;
	}

	public void setMappingEevent(String mappingEevent) {
		this.mappingEevent = mappingEevent;
	}
	
}
