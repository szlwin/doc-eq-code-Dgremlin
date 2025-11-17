package dec.external.datasource.sql.mysql.datatype.convert;
import dec.core.datasource.datatype.convert.DataConvert;

import java.math.BigInteger;

public class LongToSqlBigInteger implements DataConvert<Long, BigInteger> {

    public BigInteger convert(Long value) {
        if (value == null)
            return null;
        return BigInteger.valueOf(value);
    }
}
