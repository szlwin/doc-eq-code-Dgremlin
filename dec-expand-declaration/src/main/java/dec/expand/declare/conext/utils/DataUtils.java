package dec.expand.declare.conext.utils;

import dec.expand.declare.conext.desc.data.ValueDesc;
import smarter.common.exception.ExecuteExpection;
import smarter.common.express.check.PatternCheck;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DataUtils {

    public static boolean check(Object obj, String express) throws ExecuteExpection {

        PatternCheck pattenCheck = new PatternCheck();
        pattenCheck.setCheckValue(obj);
        pattenCheck.setPattern(express);
        return pattenCheck.check();
    }

    public static Object getValue(Object obj, String[] propertyArray, int index) throws ExecuteExpection {
        Object objData = obj;
        for (int i = index; i < propertyArray.length; i++) {
            try {
                Field field = objData.getClass().getDeclaredField(propertyArray[i]);
                field.setAccessible(true);
                objData = field.get(objData);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return objData;
    }

    public static void setValue(Object obj, List<ValueDesc> valueDescList) throws NoSuchFieldException, IllegalAccessException {
        if (obj instanceof Collections) {
            ((Collection) obj).stream().forEach(item -> {
                try {
                    setValue(item, valueDescList);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        Object lastObj = obj;
        Field field = null;
        for (ValueDesc valueDesc : valueDescList) {
            for (int i = 0; i < valueDesc.getProperty().length; i++) {
                field = lastObj.getClass().getDeclaredField(valueDesc.getProperty()[i]);
                field.setAccessible(true);
                if (i == valueDesc.getProperty().length - 1) {
                    field.set(lastObj, valueDesc.getValue());
                } else {
                    lastObj = field.get(lastObj);
                    if (lastObj instanceof Collections) {
                        int from = i + 1;
                        ((Collection) lastObj).stream().forEach(item -> {
                            try {
                                ValueDesc tempValueDesc = new ValueDesc();
                                tempValueDesc.setProperty(Arrays.copyOfRange(valueDesc.getProperty(), from, valueDesc.getProperty().length));
                                tempValueDesc.setValue(valueDesc.getValue());
                                setValue(item, Arrays.asList(tempValueDesc));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }
            }
        }
    }
}
