package dec.external.datasource.sql.mysql.datatype.convert;

import dec.core.datasource.datatype.convert.DataConvert;

import java.math.BigInteger;

public class SqlBigIntegerToLong implements DataConvert<BigInteger, Long> {

    public Long convert(BigInteger value) {
        if (value == null)
            return null;
        return value.longValue();
    }
}
