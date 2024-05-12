package dec.demo.bean.convert;


import artoria.util.Assert;
import dec.demo.bean.dto.SourceObject;
import dec.demo.bean.dto.TargetObject;
import dec.expand.declare.bean.Bean;

import java.lang.reflect.InvocationTargetException;

public class ObjectToObject {

    public static void main(String args[]) throws Exception {
       //testObj();
        testObjNoProperties();
        //testCopyObjWithNested();
        //testCopyProperties();
        //testCopyObjWithNestedAndCreate1();
        //testCopyObjWithNestedAndCreate2();
    }

    private static void testObjNoProperties() {
        SourceObject sourceObject = new SourceObject();
        TargetObject targetObject = new TargetObject();

        Bean bean = Bean.get();
        bean.copy(sourceObject, targetObject);

        Assert.isTrue(targetObject.getM_id() == 3, "error");

        sourceObject.setSourceObject(new SourceObject());
        bean.copy(sourceObject, new String[]{"sourceObject"}, targetObject);
        Assert.isTrue(targetObject.getM_id() == 3, "error");

        bean.copy(sourceObject, new String[]{"m_id"}, targetObject,  new String[]{"targetObject","m_id"});
        Assert.isTrue(targetObject.getTargetObject().getM_id() == 3, "error");
        Assert.isTrue(targetObject.getTargetObject().getAmount() == null, "error");

        bean.copy(sourceObject, targetObject,  new String[]{"targetObject"});
        Assert.isTrue(targetObject.getTargetObject().getM_id() == 3, "error");
    }

    private static void testCopyProperties() {
        SourceObject sourceObject = new SourceObject();

        sourceObject.setSourceObject(new SourceObject());
        sourceObject.getSourceObject().setSourceObject(new SourceObject());
        TargetObject targetObject = new TargetObject();

        Bean bean = Bean.get();
        bean.copy(sourceObject, new String[]{"sourceObject", "sourceObject", "m_id"}, targetObject, new String[]{"targetObject", "targetObject", "targetObject", "m_id"});

        Assert.isTrue(targetObject.getTargetObject().getTargetObject().getTargetObject().getM_id() == 3, "error");
        Assert.isTrue(targetObject.getTargetObject().getTargetObject().getTargetObject().getAmount() == null, "error");
    }

    private static void testCopyObjWithNested() {
        SourceObject sourceObject = new SourceObject();
        sourceObject.setSourceObject(new SourceObject());
        sourceObject.getSourceObject().setSourceObject(new SourceObject());

        TargetObject targetObject = new TargetObject();
        TargetObject subTarget = new TargetObject();
        targetObject.setTargetObject(subTarget);

        Bean bean = Bean.get()
                .sourceObject(sourceObject)
                .targetObject(targetObject)
                .sourceProperty(new String[]{"sourceObject"})
                .targetProperty(new String[]{"targetObject"});
        bean.copy();
        //DataUtils.copy(sourceObject, new String[]{"sourceObject"}, targetObject, new String[]{"targetObject"});

        Assert.isTrue(targetObject.getTargetObject().getM_id() == 3, "error");
        Assert.isTrue(targetObject.getTargetObject().getIsExist(), "error");
        Assert.isTrue(targetObject.getTargetObject() == subTarget, "error");
    }

    public static void testObj() throws InvocationTargetException, IllegalAccessException {
        SourceObject sourceObject = new SourceObject();

        TargetObject targetObject = new TargetObject();
        Bean bean = Bean.get();
        bean.copyObjectData(sourceObject, targetObject);

        Assert.isTrue(targetObject.getM_id() == 3, "error");
        Assert.isTrue(targetObject.getIsExist(), "error");
    }

    public static void testCopyObjWithNestedAndCreate() throws InvocationTargetException, IllegalAccessException {
        SourceObject sourceObject = new SourceObject();

        sourceObject.setSourceObject(new SourceObject());
        sourceObject.getSourceObject().setSourceObject(new SourceObject());
        TargetObject targetObject = new TargetObject();

        Bean bean = Bean.get();
        bean.copy(sourceObject, new String[]{"sourceObject", "sourceObject"}, targetObject, new String[]{"targetObject", "targetObject", "targetObject"});

        Assert.isTrue(targetObject.getTargetObject().getTargetObject().getTargetObject().getM_id() == 3, "error");
        Assert.isTrue(targetObject.getTargetObject().getTargetObject().getTargetObject().getIsExist(), "error");

    }

    public static void testCopyObjWithNestedAndCreate1() throws InvocationTargetException, IllegalAccessException {
        SourceObject sourceObject = new SourceObject();

        sourceObject.setSourceObject(new SourceObject());
        sourceObject.getSourceObject().setSourceObject(new SourceObject());
        TargetObject targetObject = new TargetObject();

        Bean bean = Bean.get();
        bean.copy(sourceObject, new String[]{"sourceObject"}, targetObject, new String[]{"targetObject", "targetObject"});

        Assert.isTrue(targetObject.getTargetObject().getTargetObject().getM_id() == 3, "error");
        Assert.isTrue(targetObject.getTargetObject().getTargetObject().getIsExist(), "error");

    }

    public static void testCopyObjWithNestedAndCreate2() throws InvocationTargetException, IllegalAccessException {
        SourceObject sourceObject = new SourceObject();

        sourceObject.setSourceObject(new SourceObject());
        sourceObject.getSourceObject().setSourceObject(new SourceObject());
        TargetObject targetObject = new TargetObject();

        Bean bean = Bean.get();
        bean.copy(sourceObject, new String[]{"sourceObject"}, targetObject, new String[]{"targetObject"});

        Assert.isTrue(targetObject.getTargetObject().getM_id() == 3, "error");
        Assert.isTrue(targetObject.getTargetObject().getIsExist(), "error");

    }
}
