package dec.expand.declare.datasorce;

public interface DataSourceManager {

    void connect(ConnecionDesc connecionDesc);

    void commit();

    void close();

    void rollBack();

}
