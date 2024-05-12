package dec.context.parse.xml.parse.datasource;
public class DataSourcePaserFactory {

	private static final DataSourcePaserFactory dataSourcePaserFactory 
		= new DataSourcePaserFactory();
	
	private DataSourcePaserFactory(){
		
	}
	
	public static DataSourcePaserFactory getInstance()
	{
		return dataSourcePaserFactory;
	}
}
