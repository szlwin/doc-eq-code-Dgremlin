package dec.core.context.data;

import dec.core.context.config.model.data.Data;
import dec.core.context.config.model.data.DataProperty;
import dec.core.context.config.model.data.PropertyInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BaseDataContractTest {
    @Test
    void preservesDeclaredPropertyBehavior() throws Exception {
        Data data = new Data(); data.setName("user");
        PropertyInfo properties = new PropertyInfo();
        DataProperty id = new DataProperty(); id.setName("id"); id.setType("int");
        properties.addProperty(id); data.setPropertyInfo(properties);
        BaseData value = BaseDataFactory.getInstance().createData(data);
        assertEquals("user", value.getName());
        assertTrue(value.checkContainKey("id"));
        value.setValue("id", 7);
        assertEquals(7, value.getValue("id"));
        value.setValue("undeclared", 9);
        assertFalse(value.checkContainKey("undeclared"));
    }
}
