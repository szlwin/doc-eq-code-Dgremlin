package test.business;

import artoria.beans.BeanUtils;
import dec.bean.utils.DataUtils;

import java.lang.reflect.InvocationTargetException;

public class Test {
    public static void main(String args[]) throws Exception {

        SourceObject sourceObject = new SourceObject();

        TargetObject targetObject = new TargetObject();

        Long time = System.currentTimeMillis();
        /*for (int i = 0; i < 1000000; i++) {
            targetObject.setaByte(sourceObject.getaByte());
            targetObject.setTest(sourceObject.getTest());
            targetObject.setAmount(sourceObject.getAmount());
            targetObject.setDate(sourceObject.getDate());
            targetObject.setName(sourceObject.getName());
            targetObject.setId(sourceObject.getId());
            targetObject.setM_id(sourceObject.getM_id());
            targetObject.setExist(sourceObject.getExist());
        }

        System.out.println(System.currentTimeMillis() - time);*/

        time = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            DataUtils.copyObjectData(sourceObject, targetObject);
        }
        System.out.println(targetObject.getM_id());
        System.out.println(System.currentTimeMillis() - time);
    }
}
