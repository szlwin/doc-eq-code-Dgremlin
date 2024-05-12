package dec.expand.declare.conext.desc.event;

import java.util.ArrayList;
import java.util.List;

public class EventDescBuilder {
	
	private List<EventDesc> eventList = new ArrayList<>();
	
	private EventDesc currentEvent;
	
	public static EventDescBuilder create(){
		return new EventDescBuilder();
	}
	
	public EventDescBuilder build(String... args){
		
		for(String name : args){
			addEvnent(new EventDesc(name));
		}
		
		return this;
	}
	
	public EventDescBuilder build(String name, String desc){
		
		addEvnent(new EventDesc(name, desc));
		
		return this;
	}
	
	public EventDescBuilder addMapping(String system, String... mappings){
		
		for(String mapping : mappings){
			
			EventMappingDesc eventMapping = new EventMappingDesc(currentEvent.getName(), system, mapping);
		
			currentEvent.addEventMapping(eventMapping);
		}
		
		
		return this;
	}
	
	public List<EventDesc> getEvents() {
		return eventList;
	}

	private void addEvnent(EventDesc event){
		this.currentEvent = event;
		eventList.add(event);
		//eventMap.put(event.getName(), event);
	}
}
