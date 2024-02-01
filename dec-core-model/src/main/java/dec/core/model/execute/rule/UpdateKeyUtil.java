package dec.core.model.execute.rule;

import dec.core.context.config.model.config.ConfigConstanst;
import dec.core.context.config.model.data.Data;
import dec.core.context.config.model.relation.ManyRelation;
import dec.core.context.config.model.relation.OneRelation;
import dec.core.context.config.model.relation.Relation;
import dec.core.context.config.model.view.RelationInfo;
import dec.core.context.config.model.view.ViewData;
import dec.core.context.config.model.view.ViewProperty;
import dec.core.context.data.ModelData;
import dec.core.model.utils.DataUtil;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class UpdateKeyUtil {

    private String dataSourceName;

    private Map<String, Object> value;

    private ViewData viewData;

    public UpdateKeyUtil(String dataSourceName, Object value, ViewData viewData) {
        this.dataSourceName = dataSourceName;

        this.value = DataUtil.getModelValues((ModelData) value);

        this.viewData = viewData;
    }

    public void update(boolean isMain, String propertyName) {
        updateToOther(isMain, propertyName);

        if(!isMain){
            updateToMain(propertyName);
        }

    }

    private void updateToOther(boolean isMain, String propertyName) {

        Data dataInfo = null;
        Object value = null;
        RelationInfo relationInfo = null;
        if (isMain) {
            dataInfo = viewData.getTargetMain();
            value = this.value;
            relationInfo = viewData.getRelationInfo();
        } else {
            ViewData propertyViewData = viewData.getRelationInfo().getRelationByPropertyName1(propertyName).getViewProperty().getViewData();
            relationInfo = propertyViewData.getRelationInfo();
            if(relationInfo == null){
                return;
            }
            dataInfo = propertyViewData.getTargetMain();
            value = DataUtil.getValueByKey(propertyName, this.value);
        }
        String idKey = dataInfo.getTableInfo().getTable(dataSourceName).getPropertyKey();

        Object idValue = ((Map<String,Object>)value).get(idKey);

        String proRefName = idKey;// Util.getKeyPropertyName(dataName,dataSourceName);

        if (relationInfo == null) {
            return;
        }
        Collection<Relation> rCollection = relationInfo.getRelation1();

        ((Map<String,Object>)value).put(proRefName, idValue);

        Iterator<Relation> it = rCollection.iterator();

        while (it.hasNext()) {
            Relation relation = it.next();

            String objName = relation.getViewProperty().getName();

            Object obj = ((Map<String,Object>)value).get(objName);

            Map<String, ViewProperty> proMap = relation.getViewProperty().getViewData().getViewPropertyInfo().getProperty();

            if (relation.getType().equals(ConfigConstanst.RELATION_TYPE_ONE_TO_ONE)) {

                OneRelation oRrelation = (OneRelation) relation;
                if (oRrelation.getOneMainkey().equals(proRefName)) {
                    ViewProperty relationProperty = proMap.get(oRrelation.getOneKey());
                    if (relationProperty != null) {
                        Map<String, Object> data = (Map<String, Object>) obj;
                        data.put(relationProperty.getName(), idValue);
                    }
                }
            } else {
                ManyRelation manyRrelation = (ManyRelation) relation;

                ViewProperty relationProperty = proMap.get(manyRrelation.getManyKey());
                if (relationProperty != null) {
                    Collection<Map<String, Object>> oCollection = (Collection<Map<String, Object>>) obj;

                    Iterator<Map<String, Object>> itObj = oCollection.iterator();

                    while (itObj.hasNext()) {
                        Map<String, Object> objPor = itObj.next();
                        objPor.put(relationProperty.getName(), idValue);
                    }
                }
            }
        }
    }

    private void updateToMain(String propertyName) {

        Object propertyValue = DataUtil.getValueByKey(propertyName, value);

        if (propertyValue instanceof Collection)
            return;

        Data dataInfo = viewData
                .getRelationInfo()
                .getRelationByPropertyName1(propertyName).getViewProperty().getViewData().getTargetMain();

        String idKey = dataInfo.getTableInfo()
                .getTable(dataSourceName)
                .getPropertyKey();

        Object idValue = ((Map<String, Object>) propertyValue).get(idKey);

        RelationInfo relationInfo = viewData.getRelationInfo();

        Collection<Relation> rCollection = relationInfo.getRelation1();

        Iterator<Relation> it = rCollection.iterator();

        while (it.hasNext()) {
            Relation relation = it.next();
            //Relation relation = relationView.getRef();//Util.getRelation(relationView.getRef());
            if (!relation.getType().equals(ConfigConstanst.RELATION_TYPE_ONE_TO_ONE))
                continue;

            OneRelation oRrelation = (OneRelation) relation;

            if (oRrelation.getOneRef().equals(dataInfo.getName())) {
                value.put(oRrelation.getOneMainkey(), idValue);
                break;
            }
        }

    }

}
