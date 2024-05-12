package dec.demo.bean.convert;


import artoria.util.Assert;
import dec.demo.bean.dto.SourceObject;
import dec.demo.bean.dto.TargetObject;
import dec.expand.declare.bean.Bean;
import dec.expand.declare.conext.utils.DataUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class ObjectListToObjectList {

    public static void main(String args[]) throws Exception {
        testListObj();
    }

    private static void testListObj() {
        SourceObject supperSourceObject = new SourceObject();
        SourceObject sourceObject = new SourceObject();
        supperSourceObject.setSourceObject(sourceObject);
        sourceObject.setSourceObjects(new ArrayList<>());

        TargetObject targetObject = new TargetObject();

        for (int i = 0; i < 10; i++) {
            SourceObject s = new SourceObject();
            sourceObject.getSourceObjects().add(s);
            s.setSourceObject(new SourceObject());
            s.getSourceObject().setSourceObjects(new ArrayList<>());
            for (int j = 0; j < 11; j++) {
                s.getSourceObject().getSourceObjects().add(new SourceObject());
            }
        }

        Bean bean = Bean.get();
        bean.copy(supperSourceObject, new String[]{"sourceObject", "sourceObjects"}, targetObject, new String[]{"targetObject", "targetObjects"});
        Assert.isTrue(targetObject.getTargetObject().getTargetObjects().size()==10,"error");
        Assert.isTrue(targetObject.getTargetObject().getTargetObjects().get(0).getM_id()==3,"error");
    }


}
