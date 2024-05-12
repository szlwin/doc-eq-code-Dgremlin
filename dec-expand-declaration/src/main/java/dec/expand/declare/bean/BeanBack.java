package dec.expand.declare.bean;

import artoria.reflect.ReflectUtils;
import dec.core.context.data.ModelData;
import dec.expand.declare.business.exception.ExecuteException;
import dec.expand.declare.conext.utils.DataUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class BeanBack {

    private String[] sourcePropertyArray;

    private String[] targetPropertyArray;

    private Object sourceObject;

    private Object targetObject;

    private Class tragetClass;

    private BeanBack() {

    }

    public BeanBack sourceProperty(String[] sourceProperty) {
        this.sourcePropertyArray = sourceProperty;
        return this;
    }

    public BeanBack targetProperty(String[] targetProperty) {
        this.targetPropertyArray = targetProperty;
        return this;
    }

    public BeanBack sourceObject(Object sourceObject) {
        this.sourceObject = sourceObject;
        return this;
    }

    public BeanBack targetObject(Object targetObject) {
        this.targetObject = targetObject;
        return this;
    }

    public void copy() {
        copyFormSource(sourceObject, 0, targetObject, 0);
    }

    public void copy(Object source, String[] sourcePropertyArray, Object target, String[] targetPropertyArray) {
        this.sourceObject(source)
                .sourceProperty(sourcePropertyArray)
                .targetObject(target)
                .targetProperty(targetPropertyArray);
        this.copy();
    }

    private void copyFormSource(Object source, int sourceIndex, Object target, int targetIndex) {
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
                    List sourceList = ((List) sourceObject);
                    for (Object obj : sourceList) {
                        copyFormSource(obj, i + 1, target, targetIndex);
                    }
                }
            }
        }
        copyToTarget(sourceObject, sourcePropertyArray[sourcePropertyArray.length - 1], target, targetIndex);
    }

    private void copyToTarget(Object source, String sourceProperty, Object target, int targetIndex) {
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
                } /*else {
                    List list = ((List) targetObject);
                    try {
                       Object obj = getInitClass(list).newInstance();
                       copyToTarget(source, sourceProperty, obj, i + 1);
                       list.add(obj);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }*/
            }
        }
        copyValue(source, sourceProperty, targetObject, targetPropertyArray[targetPropertyArray.length - 1]);
    }

    private Object init(Object obj, String property) {
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

    private Class<?> getTragetClass(Class clz) {

        Class<?> genericClass = null;

        Type listType = clz.getGenericSuperclass(); // 获取 List 对象的泛型类型
        if (listType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) listType;
            Type[] typeArgs = parameterizedType.getActualTypeArguments(); // 获取泛型类型的参数列表
            genericClass = (Class<?>) typeArgs[0]; // 获取第一个泛型类型的具体类
        }
        return genericClass;
    }

    private void copyValue(Object source, String sourceProperty, Object target, String targetProperty) {

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

    public void setValue(Object obj, String name, Object value) {
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

    private void copyValueByList(List sourceValues, List targetValues) {
        if (sourceValues.isEmpty()) {
            return;
        }

        if (!targetValues.isEmpty() && targetValues.size() != sourceValues.size()) {
            return;
        }

        Class<?> genericClass = null;
        if (targetValues.isEmpty()) {
            genericClass = this.tragetClass;
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

    public Object getValue(Object obj, String property) {
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

    private boolean isBaseData(Object value) {
        if (value instanceof String || value instanceof Number || value instanceof Boolean || value.getClass() == char.class) {
            return true;
        }
        return false;
    }

    private void copyListValue(List sourceList, String sourceProperty, List targetList, String targetProperty) {
        if (sourceList.isEmpty()) {
            return;
        }
        if (!targetList.isEmpty() && targetList.size() != sourceList.size()) {
            return;
        }
        Class<?> genericClass = null;
        if (targetList.isEmpty()) {
            genericClass = this.tragetClass;
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

    public void copyObjectData(Object sourceObject, Object targetObject) throws IllegalAccessException, InvocationTargetException {

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
                    if (isBaseData(value)) {
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
                    if (isBaseData(value)) {
                        DataUtils.setValue(targetObject, key, sourceObject);
                    }
                }
            }
        }
    }

    private Map<String, PropertyDescriptor> getSourceProperty(PropertyDescriptor[] descriptors) {
        Map<String, PropertyDescriptor> map = new HashMap<>();
        for (PropertyDescriptor propertyDescriptor : descriptors) {
            map.put(propertyDescriptor.getName(), propertyDescriptor);
        }
        return map;
    }

    private Set<String> getSourceKey(Object sourceObject) {
        if (sourceObject instanceof Map) {
            return ((Map) sourceObject).keySet();
        } else if (sourceObject instanceof ModelData) {
            return ((ModelData) sourceObject).getValues().keySet();
        }
        return null;
    }

    private Object getValue(Object sourceObject, String key, Set<String> keySet, Map<String, PropertyDescriptor> propertyDescriptorMap) throws InvocationTargetException, IllegalAccessException {
        if (keySet != null) {
            return ((Map<String, ?>) sourceObject).get(key);
        }
        return propertyDescriptorMap.get(key).getReadMethod().invoke(sourceObject);
    }
}
