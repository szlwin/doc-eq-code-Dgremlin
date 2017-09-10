package test.common;
import com.orm.common.xml.Config;
import com.orm.common.xml.exception.DataNotDefineException;
import com.orm.common.xml.exception.XMLParseException;
import com.orm.context.data.ModelData;
import com.orm.context.data.ModelDataFactory;


public class TestModel {

	/**
	 * @param args
	 * void
	 * @throws XMLParseException 
	 * @throws DataNotDefineException 
	 */
	public static void main(String[] args) throws XMLParseException, DataNotDefineException {
		test1();

	}

	public static void test1() throws XMLParseException, DataNotDefineException{
		Config config = new Config("config/orm-config.xml");
		config.initContext();
		
		ModelData data = ModelDataFactory.getInstance().createData("OrderInfo");
		
		System.out.println(data);
	}
}
