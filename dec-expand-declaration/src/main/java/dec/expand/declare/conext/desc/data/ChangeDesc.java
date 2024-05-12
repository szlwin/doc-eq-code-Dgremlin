package dec.expand.declare.conext.desc.data;

import dec.expand.declare.collections.SimpleList;

public class ChangeDesc {

    private String name;

    private SimpleList<ValueDesc> valueDescList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SimpleList<ValueDesc> getValueDescList() {
        return valueDescList;
    }

    public void setValueDescList(SimpleList<ValueDesc> valueDescList) {
        this.valueDescList = valueDescList;
    }
}
