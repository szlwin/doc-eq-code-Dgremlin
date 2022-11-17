package dec.demo.config;

import dec.core.model.container.ResultInfo;
import dec.core.model.container.listener.ViewEvent;
import dec.core.model.container.listener.ViewEventEnum;
import dec.core.model.container.listener.ViewListener;

public class SimpleViewListener implements ViewListener{

	@Override
	public ResultInfo notify(ViewEvent event) {
		
		if(event.getType() == ViewEventEnum.VIEW_START){
			System.out.println(ViewEventEnum.VIEW_START+":"+event.getViewName());
		}
		
		if(event.getType() == ViewEventEnum.VIEW_END){
			System.out.println(ViewEventEnum.VIEW_END+":"+event.getViewName()+":"+event.getRuleResultInfo().getRuleName());
		}
		
		if(event.getType() == ViewEventEnum.RULE_START){
			System.out.println(ViewEventEnum.RULE_START+":"+event.getViewName()+":"+event.getRuleName());
		}
		
		if(event.getType() == ViewEventEnum.RULE_END){
			System.out.println(ViewEventEnum.RULE_END+":"+event.getViewName()+":"+event.getRuleName()+":"+event.getRuleResultInfo().getRuleName());
		}
		
		return ResultInfo.success();
	}

}
