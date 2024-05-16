package dec.demo.model;

import dec.core.model.container.ResultInfo;
import dec.core.model.container.listener.ViewEvent;
import dec.core.model.container.listener.ViewEventEnum;
import dec.core.model.container.listener.ViewListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleViewListener implements ViewListener{

	private final static Logger log = LoggerFactory.getLogger(SimpleViewListener.class);
	@Override
	public ResultInfo notify(ViewEvent event) {
		
		if(event.getType() == ViewEventEnum.VIEW_START){
			log.info(ViewEventEnum.VIEW_START+":"+event.getViewName());
		}
		
		if(event.getType() == ViewEventEnum.VIEW_END){
			log.info(ViewEventEnum.VIEW_END+":"+event.getViewName()+":"+event.getRuleResultInfo().getRuleName());
		}
		
		if(event.getType() == ViewEventEnum.RULE_START){
			log.info(ViewEventEnum.RULE_START+":"+event.getViewName()+":"+event.getRuleName());
		}
		
		if(event.getType() == ViewEventEnum.RULE_END){
			log.info(ViewEventEnum.RULE_END+":"+event.getViewName()+":"+event.getRuleName()+":"+event.getRuleResultInfo().getRuleName());
		}
		
		return ResultInfo.success();
	}

}
