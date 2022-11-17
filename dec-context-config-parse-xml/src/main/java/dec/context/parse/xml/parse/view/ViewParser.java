package dec.context.parse.xml.parse.view;

import java.util.Iterator;
import java.util.Map;


import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dec.context.parse.xml.exception.XMLParseException;
import dec.context.parse.xml.parse.AbstarctElementsParser;
import dec.context.parse.xml.parse.config.ConfigConstanst;
import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.config.model.data.Data;
import dec.core.context.config.model.data.DataProperty;
import dec.core.context.config.model.relation.ManyRelation;
import dec.core.context.config.model.relation.OneRelation;
import dec.core.context.config.model.relation.Relation;
import dec.core.context.config.model.view.RelationInfo;
import dec.core.context.config.model.view.ViewData;
import dec.core.context.config.model.view.ViewProperty;
import dec.core.context.config.model.view.ViewPropertyInfo;
import dec.core.context.config.utils.ConfigContextUtil;

/*import com.orm.common.config.Config;
import com.orm.common.xml.exception.XMLParseException;
import com.orm.common.xml.model.config.ConfigInfo;
import com.orm.common.xml.model.data.Data;
import com.orm.common.xml.model.data.DataProperty;
import com.orm.common.xml.model.relation.ManyRelation;
import com.orm.common.xml.model.relation.OneRelation;
import com.orm.common.xml.model.relation.Relation;
import com.orm.common.xml.model.view.RelationInfo;
import com.orm.common.xml.model.view.RelationProperty;
import com.orm.common.xml.model.view.RelationView;
import com.orm.common.xml.model.view.ViewData;
import com.orm.common.xml.model.view.ViewProperty;
import com.orm.common.xml.model.view.ViewPropertyInfo;
import com.orm.common.xml.parse.AbstarctElementsParser;
import com.orm.common.xml.util.Constanst;
import com.orm.context.data.DataUtil;*/

public class ViewParser extends AbstarctElementsParser{

	private final static Logger log = LoggerFactory.getLogger(ViewParser.class);
	
	public ViewData parse(Element element) throws XMLParseException {
		ViewData viewData = new ViewData();
		
		viewData.setName(element.attributeValue(ViewData.NAME));
		
		log.info("Load the view:"+viewData.getName()+" start!");
		
		Object viewDataObj = ConfigContextUtil.getConfigInfo().get(Config.VIEWDATA, viewData.getName());
		
		if(viewDataObj != null){
			throw new XMLParseException("The view:"+viewData.getName()+" is existed!");
		}
		
		viewData.setClassName(element.attributeValue(ViewData.CLASS));
		
		String targetMain = element.attributeValue(ViewData.TARGET_MAIN);
		
		Object dataObj = ConfigContextUtil.getConfigInfo().get(Config.DATA, targetMain);
		
		if(dataObj == null){
			throw new XMLParseException("The data:"+targetMain+" is not existed!");
		}
		
		viewData.setTargetMain(ConfigContextUtil.getConfigInfo()
				.getData(targetMain));
		
		Element propertyInfoElement = element.element(ViewData.PROPERTY_INFO);
		
		ViewPropertyInfo propertyInfo = parseViewPropertyInfo(viewData,propertyInfoElement);
		
		viewData.setViewPropertyInfo(propertyInfo);
		
		//RelationInfo relationInfo = parseRelationInfo(element);
		
		//viewData.setRelationInfo(relationInfo);
		
		log.info("Load the view:"+viewData.getName()+" success!");
		return viewData;
	}

	@SuppressWarnings("rawtypes")
	private ViewPropertyInfo parseViewPropertyInfo(ViewData viewData,Element element) throws XMLParseException {
		ViewPropertyInfo viewPropertyInfo = new ViewPropertyInfo();
		
		Iterator propertyList  = element.elementIterator(ViewPropertyInfo.PROERTY);
		
		while(propertyList.hasNext())
		{
			Element protertyElement = (Element)propertyList.next();
			ViewProperty property = new ViewProperty();
			
			String ref = protertyElement.attributeValue(ViewProperty.RELATION);
			
			Map<String,DataProperty> refProMap = viewData.getTargetMain().getPropertyInfo().getProperty();
			if(ref != null && !"".equals(ref)){
				
				RelationInfo relationInfo = viewData.getRelationInfo();
				
				if(relationInfo == null){
					relationInfo = new RelationInfo();
					viewData.setRelationInfo(relationInfo);
				}
				
				ConfigInfo configInfo = ConfigContextUtil.getConfigInfo();
				
				String name = protertyElement.attributeValue(ViewProperty.NAME);
				
				ViewData subViewData = new ViewData();
				subViewData.setName(name);
				
				String dataName = protertyElement.attributeValue("data");
				
				Data data = configInfo.getData(dataName);
				
				if(data == null){
					throw new XMLParseException("The data:"+dataName+" is not existed!");
				}
				
				subViewData.setTargetMain(data);
				
				Relation relation = createRelation(viewData,protertyElement,ref);//configInfo.getRelation(ref);
				relation.setViewProperty(property);
				
				property.setName(name);
				property.setViewData(subViewData);
				property.setRelation(relation);

				relationInfo.addRelation1(data.getName(),relation);
				
				viewPropertyInfo.addProperty(property);
				ViewPropertyInfo subViewPropertyInfo = parseViewPropertyInfo(subViewData,protertyElement);
				subViewData.setViewPropertyInfo(subViewPropertyInfo);
			}else{
				property.setName(protertyElement.attributeValue(ViewProperty.NAME));
				
				protertyElement.attributeValue(ViewProperty.REF_PROPERTY);
				
				String refPro = protertyElement.attributeValue(ViewProperty.REF_PROPERTY);
				
				if(!refProMap.containsKey(refPro)){
					throw new XMLParseException("The ref-property:"+refPro+" is not existed in:"+viewData.getTargetMain().getName());
				}
				
				property.setRefProperty(refPro);
				
				viewPropertyInfo.addProperty(property);
			}

		}
		
		return viewPropertyInfo;
	}
	
	private Relation createRelation(ViewData view,Element protertyElement,String ref){
		
		if(ref.equals(ConfigConstanst.RELATION_TYPE_ONE_TO_ONE)){
			OneRelation relation = new OneRelation();
			
			relation.setType(ConfigConstanst.RELATION_TYPE_ONE_TO_ONE);
			//relation.setName(element.attributeValue(Relation.NAME));
			
			relation.setOneKey(protertyElement.attributeValue("key"));
			relation.setOneRef(protertyElement.attributeValue("data"));
			
			relation.setOneMainkey(protertyElement.attributeValue("rel-key"));
			relation.setOneMainRef(view.getTargetMain().getName());
			return relation;
		}else if(ref.equals(ConfigConstanst.RELATION_TYPE_ONE_TO_MANY)){
			ManyRelation relation = new ManyRelation();
			
			relation.setType(ConfigConstanst.RELATION_TYPE_ONE_TO_MANY);
			//relation.setName(element.attributeValue(Relation.NAME));
			
			relation.setOneKey(protertyElement.attributeValue("rel-key"));
			relation.setOneRef(view.getTargetMain().getName());
			
			relation.setManyKey(protertyElement.attributeValue("key"));
			relation.setManyRef(protertyElement.attributeValue("data"));
			return relation;
		}
		
		return null;
	}
	/*
	@SuppressWarnings("rawtypes")
	private RelationInfo parseRelationInfo(Element element) throws XMLParseException {
		
		RelationInfo relationInfo = new RelationInfo();
		Element relationInfoElement = element.element(ViewData.RELATION_INFO);
		
		Iterator relationList = relationInfoElement.elementIterator(RelationInfo.RELATION_VIEW);
		
		while(relationList.hasNext())
		{
			Element relationElement = (Element)relationList.next();
			RelationView relationView = new RelationView();
			
			String ref = relationElement.attributeValue(RelationView.REF);
			
			ConfigInfo configInfo = DataUtil.getConfigInfo();
			Relation relation = configInfo.getRelation(ref);
			
			relationView.setRef(relation);
			relationView.setRelationProperty(relationElement.attributeValue(RelationView.RELATION_PROPERTY));
			relationView.setData(configInfo.getData(relationElement.attributeValue(RelationView.DATA)));
			

			Iterator relationPropertyList = relationElement.elementIterator(RelationView.PROPERTY);
			
			while(relationPropertyList.hasNext())
			{
				Element relationPropertyElement = (Element)relationPropertyList.next();
				RelationProperty relationProperty = parseRelationProperty(relationPropertyElement);
				
				relationView.addProperty(relationProperty);
			}
			
			relationInfo.addRelation(relationView);
		}
		
		return relationInfo;
	}*/
	/*
	private RelationProperty parseRelationProperty(Element element){
		RelationProperty relationProperty = new RelationProperty();
		
		relationProperty.setName(element.attributeValue(RelationProperty.NAME));
		relationProperty.setRefProperty(element.attributeValue(RelationProperty.REF_PROPERTY));
		
		return relationProperty;
	}*/
}
