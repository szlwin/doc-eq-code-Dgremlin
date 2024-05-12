package dec.expand.declare.bean;

import artoria.reflect.ReflectUtils;
import dec.core.context.data.ModelData;
import dec.expand.declare.business.exception.ExecuteException;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class Bean {

    private String[] sourcePropertyArray;

    private String[] targetPropertyArray;

    private Object sourceObject;

    private Object targetObject;

    private Class tragetClass;

    private Object value;

    private Bean() {

    }

    public static Bean get() {
        return new Bean();
    }

    public static Bean get(Class cls) {
        Bean bean = new Bean();
        bean.cls(cls);
        return bean;
    }

    public Bean cls(Class cls) {
        this.tragetClass = cls;
        return this;
    }

    public Bean sourceProperty(String[] sourceProperty) {
        this.sourcePropertyArray = sourceProperty;
        return this;
    }

    public Bean targetProperty(String[] targetProperty) {
        this.targetPropertyArray = targetProperty;
        return this;
    }

    public Bean sourceObject(Object sourceObject) {
        this.sourceObject = sourceObject;
        return this;
    }

    public Bean targetObject(Object targetObject) {
        this.targetObject = targetObject;
        return this;
    }

    public void copy() {
        Object distSourceObject = getSourceObject();

        Object distTargetObject = getTargetObject();
        this.copyValue(distSourceObject, getProperty(sourcePropertyArray),
                distTargetObject, getProperty(targetPropertyArray));
        //copyFormSource(sourceObject, 0, targetObject, 0);
    }

    private String getProperty(String array[]) {
        if (array != null) {
            if (array.length > 1) {
                return array[array.length - 1];
            } else {
                return array[0];
            }
        }
        return null;
    }

    private Object getSourceObject() {
        Object object = sourceObject;
        if (this.sourcePropertyArray == null || sourcePropertyArray.length == 0) {
            return object;
        }

        for (int i = 0; i < sourcePropertyArray.length - 1; i++) {
            object = this.getValue(object, sourcePropertyArray[i]);
        }
        return object;
    }

    private Object getTargetObject() {

        Object object = targetObject;
        if (this.targetPropertyArray == null || targetPropertyArray.length == 0) {
            return object;
        }

        for (int i = 0; i < targetPropertyArray.length - 1; i++) {
            Object distObject = this.getValue(object, targetPropertyArray[i]);
            if (distObject == null) {
                if (i == 0) {
                    distObject = init(object, targetPropertyArray[0]);
                    setValue(object, targetPropertyArray[0], distObject);
                } else {
                    distObject = init(targetObject, targetPropertyArray[i]);
                    setValue(object, targetPropertyArray[i], distObject);
                }
            }
            if(i == targetPropertyArray.length - 2 && distObject instanceof Collection){
                this.initTragetClass(null, object, targetPropertyArray[i]);
            }
            object = distObject;
        }

        return object;
    }

    public void copy(Object source, Object target) {
        copy(source, null, target, null);
    }

    public void copy(Object source, Object target, String[] targetPropertyArray) {
        copy(source, null, target, targetPropertyArray);
    }

    public void copy(Object source, String[] sourcePropertyArray, Object target) {
        copy(source, null, target, null);
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

    private Object initTarget(Object obj, String property) {
        if (tragetClass != null) {
            return create(tragetClass);
        }

        return this.init(obj, property);
    }

    private Object init(Object obj, String property) {

        try {
            Field field = obj.getClass().getDeclaredField(property);
            if (Map.class.isAssignableFrom(field.getType())) {
                return new HashMap<>();
            } else if (List.class.isAssignableFrom(field.getType())) {
                return new ArrayList<>();
            } else {
                return create(field.getDeclaringClass());
            }
        } catch (Exception ex) {
            throw new ExecuteException(ex);
        }
    }

    private void initTragetClass(Object list, Object obj, String property) {
        if (tragetClass != null) {
            return;
        }

        Field field;
        try {
            field = obj.getClass().getDeclaredField(property);
        } catch (Exception ex) {
            throw new ExecuteException(ex);
        }

        Type listType = field.getGenericType();
        if (listType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) listType;
            Type[] typeArgs = parameterizedType.getActualTypeArguments(); // 获取泛型类型的参数列表
            if (typeArgs[0] instanceof ParameterizedTypeImpl) {
                tragetClass = ((ParameterizedTypeImpl) typeArgs[0]).getRawType();
            } else {
                tragetClass = (Class<?>) typeArgs[0];
            }
        }
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
                initTragetClass(targetValue, target, targetProperty);
            }
            copyValueByList((List) sourceValue, (List) targetValue);
            setValue(target, targetProperty, targetValue);
        } else {
            try {
                Object targetValue = getValue(target, targetProperty);
                if (targetValue == null) {
                    targetValue = initTarget(target, targetProperty);
                }
                copyObjectData(sourceValue, targetValue);
                if(targetProperty != null){
                    setValue(target, targetProperty, targetValue);
                }
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
                    targetObject = this.create(genericClass);
                    targetValues.add(targetObject);
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
        if (property == null)
            return obj;
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

    private Object create(Class cls) {
        try {
            if (cls == Integer.class)
                return null;
            if (Map.class == cls) {
                return new HashMap<>();
            } else if (List.class.isAssignableFrom(cls) || List.class == cls) {
                return new ArrayList<>();
            } else if (Collection.class.isAssignableFrom(cls) || Collection.class == cls) {
                return new ArrayList<>();
            } else {
                return cls.newInstance();
            }
        } catch (Exception ex) {
            throw new ExecuteException(ex);
        }
    }

    private boolean isBaseData(Object value) {
        if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean || value.getClass() == char.class) {
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
        boolean isEmpty = targetList.isEmpty();

        for (int i = 0; i < sourceList.size(); i++) {
            Object sourceObj = sourceList.get(i);
            if (isBaseData(sourceObj)) {
                targetList.add(sourceObj);
            } else {
                Object targetObj;
                if (!isEmpty) {
                    targetObj = targetList.get(i);
                } else {
                    try {
                        targetObj = this.create(this.tragetClass);
                    } catch (Exception ex) {
                        throw new ExecuteException(ex);
                    }
                }
                setValue(targetObj, targetProperty, getValue(sourceObj, sourceProperty));
                targetList.add(targetObj);
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
            Iterator<String> sourceKeys = null;
            if (propertyMap != null && !propertyMap.isEmpty()) {
                sourceKeys = propertyMap.keySet().iterator();
            } else if (keys != null && !keys.isEmpty()) {
                sourceKeys = keys.iterator();
            }
            while (sourceKeys.hasNext()) {
                String key = sourceKeys.next();
                Object value = getValue(sourceObject, key, keys, propertyMap);
                if (isBaseData(value)) {
                    setValue(targetObject, key, value);
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
