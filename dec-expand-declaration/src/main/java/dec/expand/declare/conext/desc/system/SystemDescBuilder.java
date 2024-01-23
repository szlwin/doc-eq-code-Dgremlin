package dec.expand.declare.conext.desc.system;

import dec.expand.declare.conext.desc.data.DataDependDesc;
import dec.expand.declare.conext.desc.data.DataDesc;
import dec.expand.declare.conext.desc.data.DataTypeEnum;

public class SystemDescBuilder {

    private SystemDesc systemDesc;

    private DataDesc currentDataDesc;

    public static SystemDescBuilder create() {
        return new SystemDescBuilder();
    }


    public SystemDescBuilder build(String name, String desc) {

        systemDesc = new SystemDesc(name, desc);

        return this;
    }

    public SystemDescBuilder data(String name, String desc) {

        currentDataDesc = new DataDesc(name, desc);

        this.addData(currentDataDesc);

        return this;
    }

    public SystemDescBuilder addData(DataDesc dataDesc) {
        currentDataDesc = dataDesc;

        systemDesc.addData(dataDesc);

        return this;
    }

    public SystemDescBuilder type(DataTypeEnum type) {
        currentDataDesc.setType(type);
        return this;
    }

    public SystemDescBuilder cachePrior(boolean cachePrior) {
        currentDataDesc.setCachePrior(cachePrior);
        return this;
    }

    public SystemDescBuilder depend(String data) {
        currentDataDesc.addDepend(new DataDependDesc(data));

        return this;
    }

    public SystemDescBuilder init(String express) {
        currentDataDesc.getDataDepends().get(
                        currentDataDesc.getDataDepends().size() - 1)
                .setInit(express);
        return this;
    }

    public SystemDescBuilder condition(String express) {
        currentDataDesc.getDataDepends().get(
                        currentDataDesc.getDataDepends().size() - 1)
                .setCondition(express);
        return this;
    }

    public SystemDescBuilder param(String express) {
        currentDataDesc.getDataDepends().get(
                        currentDataDesc.getDataDepends().size() - 1)
                .setParam(express);
        return this;
    }

    public SystemDescBuilder change(String express) {
        DataDependDesc dataDependDesc = currentDataDesc.getDataDepends().get(
                currentDataDesc.getDataDepends().size() - 1);
        dataDependDesc.setChange(express);
        this.currentDataDesc.addChange(dataDependDesc.getData(), dataDependDesc.getChange());
        return this;
    }

    public SystemDesc getSystem() {
        return systemDesc;
    }

}
