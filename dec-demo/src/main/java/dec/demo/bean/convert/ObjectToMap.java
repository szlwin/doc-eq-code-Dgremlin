package dec.demo.bean.convert;

import artoria.util.Assert;
import dec.demo.bean.dto.SourceObject;
import dec.demo.bean.dto.TargetMapObject;
import dec.demo.bean.dto.TargetObject;
import dec.expand.declare.bean.Bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ObjectToMap {
    public static void main(String args[]) throws Exception {
        testMap();
        testMapList();
        testMapOnlyCopy();
        testMapProperty();
    }

    private static void testMapList() {
        SourceObject sourceObject = new SourceObject();
        sourceObject.setSourceObjects(new ArrayList<>());
        sourceObject.getSourceObjects().add(new SourceObject());

        TargetMapObject targetMapObject = new TargetMapObject();

        Bean bean = Bean.get();
        bean.copy(sourceObject, new String[]{"sourceObjects","m_id"}, targetMapObject,new String[]{"targetObjects","m_id"});
        Assert.isTrue((int)targetMapObject.getTargetObjects().get(0).get("m_id") == 3, "error");

        bean.copy(sourceObject, new String[]{"sourceObjects"}, targetMapObject,new String[]{"targetObjects"});
        Assert.isTrue((int)targetMapObject.getTargetObjects().get(0).get("m_id") == 3, "error");
    }

    private static void testMapProperty() {
        SourceObject sourceObject = new SourceObject();
        TargetMapObject targetMapObject = new TargetMapObject();
        Bean bean = Bean.get();
        bean.copy(sourceObject, new String[]{"m_id"}, targetMapObject,new String[]{"targetObject","m_id"});
        Assert.isTrue((int)targetMapObject.getTargetObject().get("m_id") == 3, "error");
    }

    private static void testMapOnlyCopy() {
        SourceObject sourceObject = new SourceObject();
        TargetMapObject targetMapObject = new TargetMapObject();
        TargetObject targetObject = new TargetObject();
        targetMapObject.setTargetObject(new HashMap<>());
        targetMapObject.getTargetObject().put("targetObject", targetObject);
        targetObject.setTargetObject(new TargetObject());
        Bean bean = Bean.get();

        bean.copy(sourceObject, targetMapObject,new String[]{"targetObject","targetObject"});
        Assert.isTrue(((TargetObject)targetMapObject.getTargetObject().get("targetObject")).getM_id() == 3, "error");
        Assert.isTrue(((TargetObject)targetMapObject.getTargetObject().get("targetObject")) == targetObject, "error");

        bean.copy(sourceObject, targetMapObject,new String[]{"targetObject","targetObject","targetObject"});
        Assert.isTrue(((TargetObject)targetMapObject.getTargetObject().get("targetObject")).getTargetObject().getM_id() == 3, "error");
        Assert.isTrue(((TargetObject)targetMapObject.getTargetObject().get("targetObject")).getTargetObject() == targetObject.getTargetObject(), "error");
    }

    private static void testMap() {
        SourceObject sourceObject = new SourceObject();
        TargetMapObject targetMapObject = new TargetMapObject();
        Bean bean = Bean.get();
        bean.copy(sourceObject, targetMapObject,new String[]{"targetObject"});
        Assert.isTrue((int)targetMapObject.getTargetObject().get("m_id") == 3, "error");

        bean = Bean.get(TargetObject.class);
        bean.copy(sourceObject, targetMapObject,new String[]{"targetObject","targetTestObject"});
        Assert.isTrue(((TargetObject)targetMapObject.getTargetObject().get("targetTestObject")).getM_id() == 3, "error");
    }
}
