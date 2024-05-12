package dec.external.datasource.sql.datatype.convert;

import dec.core.context.config.utils.ConfigContextUtil;
import dec.core.datasource.datatype.convert.DataConvertContainer;
import dec.external.datasource.sql.utils.Util;

//import com.orm.context.data.DataUtil;
//import com.orm.sql.util.Util;

public class DataTypeConvert {

    //private static final ThreadLocal<ConvertContainer> typeContainerLocal = new ThreadLocal<ConvertContainer>();

    public static Object convert(Object data, String type, String originType, String dataSource, boolean isTo) {
        if (data == null)
            return null;

        String conName = Util.getDataSourceByName(dataSource).getConName();

        DataConvertContainer convertContainer = (DataConvertContainer) ConfigContextUtil.getConfigInfo().getConnection(conName)
                .getConnectionInfo().getDataConvertContainer();

        if (isTo) {
            return convertContainer.convertToDataSource(data, type, originType);
        } else {
            return convertContainer.convertFromDataSource(data, type, originType);
        }
    }

    public static Object convert(Object data, String type, String originType, String dataSource) {
        return convert(data, type, originType, dataSource, true);
    }

    //public static void setDataSourceType(String dataSource){
    //	String type = DataUtil.getConfigInfo().getDataSource(dataSource).getType();

    //	typeContainerLocal.set(DataTypeConvertFactory.getConvertContainer(type));
    //}
}
