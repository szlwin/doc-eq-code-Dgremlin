package dec.expand.declare.conext.utils;

import artoria.reflect.ReflectUtils;
import dec.core.context.data.ModelData;
import dec.expand.declare.business.exception.ExecuteException;
import dec.expand.declare.conext.desc.data.ValueDesc;
import smarter.common.express.check.PatternCheck;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class DataUtils {

    public static boolean check(Object obj, String express) {

        PatternCheck pattenCheck = new PatternCheck();
        pattenCheck.setCheckValue(obj);
        pattenCheck.setPattern(express);
        try {
            return pattenCheck.check();
        } catch (Exception ex) {
            throw new ExecuteException(ex);
        }

    }

    public static void copy(Object source, String[] sourcePropertyArray, Object target, String[] targetPropertyArray) {
        copyFormSource(source, sourcePropertyArray, 0, target, targetPropertyArray, 0);
    }

    private static void copyToTarget(Object source, String sourceProperty, Object target, String[] targetPropertyArray, int targetIndex) {
        Object targetObject = target;
        for (int i = targetIndex; i < targetPropertyArray.length - 1; i++) {
            Object distObject = getValue(targetObject, targetPropertyArray[i]);
            if (distObject == null) {
                if (i == 0) {
                    distObject = init(target, targetPropertyArray[0]);
                    setValue(target, targetPropertyArray[0], distObject);
                } else {
                    distObject = init(targetObject, targetPropertyArray[i]);
                    setValue(targetObject, targetPropertyArray[i], distObject);
                }
            }
            targetObject = distObject;
            if (targetObject instanceof Collection) {
                if (targetIndex == targetPropertyArray.length - 2) {
                    break;
                } else {
                    List list = ((List) targetObject);
                    if (list.isEmpty()) {
                        getInitClass(list);
                    }
                    for (Object obj : list) {
                        copyToTarget(source, sourceProperty, obj, targetPropertyArray, i + 1);
                    }

                }
            }
        }
        copyValue(source, sourceProperty, targetObject, targetPropertyArray[targetPropertyArray.length - 1]);
    }

    private static Object init(Object obj, String property) {
        try {
            Field field = obj.getClass().getDeclaredField(property);
            if (Map.class.isAssignableFrom(field.getType())) {
                return new HashMap<>();
            } else if (List.class.isAssignableFrom(field.getType())) {
                return new ArrayList<>();
            } else {
                return field.getDeclaringClass().newInstance();
            }
        } catch (Exception ex) {
            throw new ExecuteException(ex);
        }
    }

    public static void copyValue(Object source, String sourceProperty, Object target, String targetProperty) {

        if (source == null) {
            return;
        }
        if (source instanceof Collection) {
            copyListValue((List) source, sourceProperty, (List) target, targetProperty);
            return;
        }

        Object sourceValue = getValue(source, sourceProperty);
        if (sourceValue == null) {
            return;
        }

        if (isBaseData(sourceValue)) {
            setValue(target, targetProperty, sourceValue);
        } else if (sourceValue instanceof Collection) {
            Object targetValue = getValue(target, targetProperty);
            if (targetValue == null) {
                targetValue = new ArrayList<>();
            }
            copyValueByList((List) sourceValue, (List) targetValue);
            setValue(target, targetProperty, targetValue);
        } else {
            try {
                Object targetValue = getValue(target, targetProperty);
                if (targetValue == null) {
                    targetValue = init(target, targetProperty);
                }
                copyObjectData(sourceValue, targetValue);
                setValue(target, targetProperty, targetValue);
            } catch (Exception ex) {
                throw new ExecuteException(ex);
            }
        }
    }

    private static Class<?> getInitClass(List list) {

        Class<?> genericClass = null;

        Class<?> listClass = list.getClass(); // 获取 List 对象的 Class 对象
        Type listType = listClass.getGenericSuperclass(); // 获取 List 对象的泛型类型
        if (listType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) listType;
            Type[] typeArgs = parameterizedType.getActualTypeArguments(); // 获取泛型类型的参数列表
            genericClass = (Class<?>) typeArgs[0]; // 获取第一个泛型类型的具体类
        }
        return genericClass;
    }

    private static void copyValueByList(List sourceValues, List targetValues) {
        if (sourceValues.isEmpty()) {
            return;
        }

        if (!targetValues.isEmpty() && targetValues.size() != sourceValues.size()) {
            return;
        }

        Class<?> genericClass = null;
        if (targetValues.isEmpty()) {
            Class<?> listClass = targetValues.getClass(); // 获取 List 对象的 Class 对象
            Type listType = listClass.getGenericSuperclass(); // 获取 List 对象的泛型类型
            if (listType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) listType;
                Type[] typeArgs = parameterizedType.getActualTypeArguments(); // 获取泛型类型的参数列表
                genericClass = (Class<?>) typeArgs[0]; // 获取第一个泛型类型的具体类
            }
        }
        for (int i = 0; i < sourceValues.size(); i++) {
            Object sourceObject = sourceValues.get(i);
            Object targetObject;
            if (genericClass == null) {
                targetObject = targetValues.get(i);
            } else {
                try {
                    targetObject = genericClass.newInstance();
                } catch (Exception ex) {
                    throw new ExecuteException(ex);
                }
            }
            try {
                copyObjectData(sourceObject, targetObject);
            } catch (Exception ex) {
                throw new ExecuteException(ex);
            }
        }
    }

    public static void copyObjectData(Object sourceObject, Object targetObject) throws IllegalAccessException, InvocationTargetException {

        PropertyDescriptor[] fromDescriptors = null;
        PropertyDescriptor[] toDescriptors = null;
        if (!(sourceObject instanceof Map)) {
            fromDescriptors = ReflectUtils.getPropertyDescriptors(sourceObject.getClass());

        }
        if (!(targetObject instanceof Map) && !(targetObject instanceof ModelData)) {
            toDescriptors = ReflectUtils.getPropertyDescriptors(targetObject.getClass());

        }

        Map<String, PropertyDescriptor> propertyMap = null;
        Set<String> keys = null;
        if (fromDescriptors != null) {
            propertyMap = getSourceProperty(fromDescriptors);
        } else {
            keys = getSourceKey(sourceObject);
        }

        if (toDescriptors != null) {
            for (PropertyDescriptor propertyDescriptor : toDescriptors) {
                if ((propertyMap != null && propertyMap.containsKey(propertyDescriptor.getName()))
                        || (keys != null && keys.contains(propertyDescriptor.getName()))) {
                    Object value = getValue(sourceObject, propertyDescriptor.getName(), keys, propertyMap);
                    if (validateData(value)) {
                        propertyDescriptor.getWriteMethod().invoke(targetObject, value);
                    }
                }
            }
        } else {
            Iterator<String> targetKeys = ((Map<String, ?>) targetObject).keySet().iterator();

            while (targetKeys.hasNext()) {
                String key = targetKeys.next();
                if ((propertyMap != null && propertyMap.containsKey(key))
                        || (keys != null && keys.contains(key))) {
                    Object value = getValue(sourceObject, key, keys, propertyMap);
                    if (validateData(value)) {
                        DataUtils.setValue(targetObject, key, sourceObject);
                    }
                }
            }
        }
    }

    private static void copyListValue(List sourceList, String sourceProperty, List targetList, String targetProperty) {
        if (sourceList.isEmpty()) {
            return;
        }
        if (!targetList.isEmpty() && targetList.size() != sourceList.size()) {
            return;
        }
        Class<?> genericClass = null;
        if (targetList.isEmpty()) {
            Class<?> listClass = targetList.getClass(); // 获取 List 对象的 Class 对象
            Type listType = listClass.getGenericSuperclass(); // 获取 List 对象的泛型类型
            if (listType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) listType;
                Type[] typeArgs = parameterizedType.getActualTypeArguments(); // 获取泛型类型的参数列表
                genericClass = (Class<?>) typeArgs[0]; // 获取第一个泛型类型的具体类
            }
        }
        for (int i = 0; i < sourceList.size(); i++) {
            Object sourceObj = sourceList.get(i);
            if (isBaseData(sourceObj)) {
                targetList.add(sourceObj);
            } else {
                Object targetObj;
                if (genericClass == null) {
                    targetObj = targetList.get(i);
                } else {
                    try {
                        targetObj = genericClass.newInstance();
                    } catch (Exception ex) {
                        throw new ExecuteException(ex);
                    }
                }
                setValue(targetObj, targetProperty, getValue(sourceObj, sourceProperty));
            }
        }
    }

    private static void copyFormSource(Object source, String[] sourcePropertyArray, int sourceIndex, Object target, String[] targetPropertyArray, int targetIndex) {
        Object sourceObject = source;
        for (int i = sourceIndex; i < sourcePropertyArray.length - 1 && sourcePropertyArray.length > 1; i++) {
            sourceObject = getValue(sourceObject, sourcePropertyArray[i]);
            if (sourceObject == null) {
                return;
            }
            if (sourceObject instanceof Collection) {
                if (((Collection) sourceObject).isEmpty()) {
                    return;
                }
                if (sourceIndex == sourcePropertyArray.length - 2) {
                    break;
                } else {
                    Iterator it = ((Collection) sourceObject).iterator();
                    while (it.hasNext()) {
                        copyFormSource(it.next(), sourcePropertyArray, i + 1, target, targetPropertyArray, targetIndex);
                    }
                }
            }
        }
        copyToTarget(sourceObject, sourcePropertyArray[sourcePropertyArray.length - 1], target, targetPropertyArray, targetIndex);
    }

    public static Object getValue(Object obj, String[] propertyArray, int index) {
        Object objData = obj;
        for (int i = index; i < propertyArray.length; i++) {
            try {
                if (objData instanceof Map) {
                    objData = ((Map) objData).get(propertyArray[i]);
                } else {
                    Field field = objData.getClass().getDeclaredField(propertyArray[i]);
                    field.setAccessible(true);
                    objData = field.get(objData);
                }

            } catch (Exception e) {
                throw new ExecuteException(e);
            }
        }
        return objData;
    }

    public static void setValue(Object obj, String name, Object value) {
        try {
            if (obj instanceof Map) {
                ((Map) obj).put(name, value);
            } else if (obj instanceof ModelData) {
                ((ModelData) obj).setValue(name, value);
            } else {
                Field field = obj.getClass().getDeclaredField(name);
                field.setAccessible(true);
                field.set(obj, value);
            }
        } catch (Exception ex) {
            throw new ExecuteException(ex);
        }
    }

    private static boolean validateData(Object value) {
        if (value instanceof String || value instanceof Number || value instanceof Boolean || value.getClass() == char.class) {
            return true;
        }
        return false;
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

    public static void setValue(Object obj, List<ValueDesc> valueDescList, int start, Map<String, Object> statusMap) throws NoSuchFieldException, IllegalAccessException {
        if (obj instanceof Collections) {
            for (ValueDesc valueDesc : valueDescList) {
                statusMap.put(String.join(".", valueDesc.getProperty()), new ArrayList<>());
            }

            ((Collection) obj).stream().forEach(item -> {
                try {
                    setValue(item, valueDescList, 0, statusMap);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            return;
        }
        Object lastObj = obj;
        Field field = null;
        for (ValueDesc valueDesc : valueDescList) {
            String key = String.join(".", valueDesc.getProperty());
            Object list = statusMap.get(key);
            for (int i = start; i < valueDesc.getProperty().length; i++) {
                field = lastObj.getClass().getDeclaredField(valueDesc.getProperty()[i]);
                field.setAccessible(true);
                if (i == valueDesc.getProperty().length - 1) {
                    if (list != null && list instanceof Collection) {
                        ((Collection) list).add(field.get(lastObj));
                    } else {
                        statusMap.put(key, field.get(lastObj));
                    }
                    field.set(lastObj, valueDesc.getValue());
                } else {
                    lastObj = field.get(lastObj);
                    if (lastObj instanceof Collections) {
                        int from = i + 1;
                        ((Collection) lastObj).stream().forEach(item -> {
                            try {
                                //ValueDesc tempValueDesc = new ValueDesc();
                                //tempValueDesc.setProperty(Arrays.copyOfRange(valueDesc.getProperty(), from, valueDesc.getProperty().length));
                                //tempValueDesc.setValue(valueDesc.getValue());
                                setValue(item, Arrays.asList(valueDesc), from, statusMap);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }
            }
        }
    }

    public static Object getValue(Object obj, String property) {
        Object objData = obj;
        try {
            if (objData instanceof Map) {
                objData = ((Map) objData).get(property);
            } else {
                Field field = objData.getClass().getDeclaredField(property);
                field.setAccessible(true);
                objData = field.get(objData);
            }

        } catch (Exception e) {
            throw new ExecuteException(e);
        }
        return objData;
    }

    private static Object getValue(Object sourceObject, String key, Set<String> keySet, Map<String, PropertyDescriptor> propertyDescriptorMap) throws InvocationTargetException, IllegalAccessException {
        if (keySet != null) {
            return ((Map<String, ?>) sourceObject).get(key);
        }
        return propertyDescriptorMap.get(key).getReadMethod().invoke(sourceObject);
    }

    private static Map<String, PropertyDescriptor> getSourceProperty(PropertyDescriptor[] descriptors) {
        Map<String, PropertyDescriptor> map = new HashMap<>();
        for (PropertyDescriptor propertyDescriptor : descriptors) {
            map.put(propertyDescriptor.getName(), propertyDescriptor);
        }
        return map;
    }

    private static Set<String> getSourceKey(Object sourceObject) {
        if (sourceObject instanceof Map) {
            return ((Map) sourceObject).keySet();
        } else if (sourceObject instanceof ModelData) {
            return ((ModelData) sourceObject).getValues().keySet();
        }
        return null;
    }

    private static boolean isBaseData(Object value) {
        if (value instanceof String || value instanceof Number || value instanceof Boolean || value.getClass() == char.class) {
            return true;
        }
        return false;
    }
}
