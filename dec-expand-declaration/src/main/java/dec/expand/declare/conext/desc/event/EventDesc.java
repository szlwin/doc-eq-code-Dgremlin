package dec.expand.declare.conext.desc.event;

import java.util.ArrayList;
import java.util.List;

import dec.expand.declare.conext.desc.Desc;

public class EventDesc extends Desc{

	private List<EventMappingDesc> eventMappings;

	public EventDesc(){
		
	}
	
	public EventDesc(String name){
		this.setName(name);
	}
	
	public EventDesc(String name, String comment){
		this.setName(name);
		this.setComment(comment);
	}
	
	public List<EventMappingDesc> getEventMapping() {
		return eventMappings;
	}

	public void addEventMapping(EventMappingDesc eventMapping) {
	
		if(eventMappings == null){
			eventMappings = new ArrayList<>();
		}
		
		eventMappings.add(eventMapping);
	}
	
	
}
