package dec.demo.bean.convert;

import artoria.util.Assert;
import dec.demo.bean.dto.SourceObject;
import dec.demo.bean.dto.TargetMapObject;
import dec.demo.bean.dto.TargetObject;
import dec.expand.declare.bean.Bean;

import java.util.ArrayList;
import java.util.HashMap;

public class MapToObject {
    public static void main(String args[]) throws Exception {
        testMap();
        testMapList();
        testMapProperty();
    }

    private static void testMapList() {
        SourceObject sourceObject = new SourceObject();
        TargetMapObject targetMapObject = new TargetMapObject();
        targetMapObject.setTargetObjects(new ArrayList<>());
        targetMapObject.getTargetObjects().add(new HashMap<>());
        targetMapObject.getTargetObjects().get(0).put("m_id",4);
        targetMapObject.getTargetObjects().get(0).put("name","test1");

        Bean bean = Bean.get();
        bean.copy(targetMapObject,new String[]{"targetObjects"},sourceObject,new String[]{"sourceObjects"});
        Assert.isTrue(sourceObject.getSourceObjects().get(0).getM_id() == 4, "error");
        Assert.isTrue(sourceObject.getSourceObjects().get(0).getName().equals("test1"), "error");

        sourceObject.getSourceObjects().clear();
        bean.copy(targetMapObject,new String[]{"targetObjects","m_id"},sourceObject,new String[]{"sourceObjects","m_id"});
        Assert.isTrue(sourceObject.getSourceObjects().get(0).getM_id() == 4, "error");
        Assert.isTrue(sourceObject.getSourceObjects().get(0).getName().equals("test"), "error");
    }

    private static void testMapProperty() {
        SourceObject sourceObject = new SourceObject();
        TargetMapObject targetMapObject = new TargetMapObject();
        targetMapObject.setTargetObject(new HashMap<>());
        targetMapObject.getTargetObject().put("m_id",4);
        targetMapObject.getTargetObject().put("name","test1");

        Bean bean = Bean.get();
        bean.copy(targetMapObject,new String[]{"targetObject","m_id"},sourceObject,new String[]{"sourceObject","m_id"});
        Assert.isTrue(sourceObject.getSourceObject().getM_id() == 4, "error");
        Assert.isTrue(sourceObject.getSourceObject().getName().equals("test"), "error");

        bean.copy(targetMapObject,new String[]{"targetObject","m_id"},sourceObject);
        Assert.isTrue(sourceObject.getSourceObject().getM_id() == 4, "error");
        Assert.isTrue(sourceObject.getSourceObject().getName().equals("test"), "error");
    }



    private static void testMap() {
        SourceObject sourceObject = new SourceObject();
        TargetMapObject targetMapObject = new TargetMapObject();
        targetMapObject.setTargetObject(new HashMap<>());
        targetMapObject.getTargetObject().put("m_id",4);
        targetMapObject.getTargetObject().put("name","test1");

        Bean bean = Bean.get();
        bean.copy(targetMapObject,new String[]{"targetObject"},sourceObject,new String[]{"sourceObject"});
        Assert.isTrue(sourceObject.getSourceObject().getM_id() == 4, "error");
        Assert.isTrue(sourceObject.getSourceObject().getName().equals("test1"), "error");


        bean.copy(targetMapObject,new String[]{"targetObject"},sourceObject);
        Assert.isTrue(sourceObject.getSourceObject().getM_id() == 4, "error");
        Assert.isTrue(sourceObject.getSourceObject().getName().equals("test1"), "error");

    }
}
