package dec.core.context.config.utils;

import dec.core.context.config.exception.DataNotDefineException;

import dec.core.context.config.model.data.Data;

import dec.core.context.data.BaseData;
import dec.core.context.data.BaseDataFactory;
import dec.core.context.data.ModelData;
import dec.core.context.data.ModelDataFactory;

/*import com.orm.common.xml.exception.DataNotDefineException;
import com.orm.common.xml.model.config.ConfigInfo;
import com.orm.common.xml.model.data.Data;
import com.orm.common.xml.model.relation.OneRelation;
import com.orm.common.xml.model.relation.Relation;
import com.orm.common.xml.model.rule.RuleViewInfo;
import com.orm.common.xml.model.view.RelationInfo;
import com.orm.common.xml.model.view.RelationProperty;
import com.orm.common.xml.model.view.RelationView;
import com.orm.common.xml.model.view.ViewData;
import com.orm.common.xml.model.view.ViewProperty;
import com.orm.common.xml.util.ConfigManager;*/

public class DataUtil {

	public static ModelData createViewData(String name) throws DataNotDefineException{
		return ModelDataFactory.getInstance().createData(name);
	}
	
	public static BaseData createBaseData(String name) throws DataNotDefineException{
		return BaseDataFactory.getInstance().createData(name);
	}
	
	public static BaseData createBaseData(Data dataInfo) throws DataNotDefineException{
		return BaseDataFactory.getInstance().createData(dataInfo);
	}
	
	
}
