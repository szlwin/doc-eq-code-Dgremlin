package dec.external.datasource.sql.mysql.datatype.convert;
import dec.core.datasource.datatype.convert.DataConvert;

import java.math.BigInteger;

public class IntegerToSqlBigInteger implements DataConvert<Integer, BigInteger> {

    public BigInteger convert(Integer value) {
        if (value == null)
            return null;
        return BigInteger.valueOf(value);
    }
}
