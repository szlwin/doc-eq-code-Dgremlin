package dec.external.datasource.sql.mysql.datatype.convert;
import dec.core.datasource.datatype.convert.DataConvert;

import java.math.BigInteger;

public class SqlBigIntegerToInteger implements DataConvert<BigInteger, Integer> {

    public Integer convert(BigInteger value) {
        if (value == null)
            return null;
        return value.intValue();
    }
}
