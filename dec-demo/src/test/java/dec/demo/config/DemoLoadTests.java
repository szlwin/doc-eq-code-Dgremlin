package dec.demo.config;

import javax.sql.DataSource;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import dec.context.parse.xml.exception.XMLParseException;
import dec.context.parse.yaml.parse.config.YamlConfigUtil;
import dec.core.context.data.BaseData;
import dec.core.model.utils.DataUtil;
import dec.core.starter.common.ConfigUtil;
import dec.core.starter.common.DataSourceManager;
import dec.external.datasource.sql.mysql.connection.factory.MySQLDBConnectionFactory;
import dec.external.datasource.sql.mysql.convert.container.factory.MySQLConvertContainerFactory;
import dec.external.datasource.sql.mysql.datatype.convert.factory.MySQLDataConvertContainerFactory;
import dec.external.datasource.sql.mysql.execute.container.factory.MySQLExecuteContainerFactory;

@Tag("mysql-it")
public class DemoLoadTests {


    @Test
	public void testInit() throws Exception{

		//1.添加数据源
		ConfigUtil.addDataSourceConfig("MySQL", "dec.external.datasource.sql.datasource.DBDataSource");
		//2.加载配置文件
		//ConfigUtil.parseConfigInfo("classpath:model/orm-config.xml");
		YamlConfigUtil.parseConfigInfo("classpath:yaml/model/orm-config.yaml");

		//3.添加数据源相关实现
		DataSourceManager.addConnectionFactory("MySQL", new MySQLDBConnectionFactory());
		DataSourceManager.addConvertContainerFactory("MySQL", new MySQLConvertContainerFactory());
		DataSourceManager.addDataConvertContainerFacory("MySQL", new MySQLDataConvertContainerFactory());
		DataSourceManager.addExecuteContainerFacory("MySQL", new MySQLExecuteContainerFactory());

		//4.添加数据源
		DataSourceManager.addDataSource("data1", getDataSource1());
		DataSourceManager.addDataSource("data2", getDataSource2());
	}

	public BaseData createProductData(String name,int count,double price) throws Exception{
		
		BaseData productData = DataUtil.createBaseData("product");
		
		productData.setValue("name", name);
		productData.setValue("count", count);
		productData.setValue("price", price);		
		
		return productData;
	}
	
	public BaseData insertUserData() throws Exception{
		
		BaseData userData = DataUtil.createBaseData("user");
		userData.setValue("name", "test");
		userData.setValue("password", "test");
		return userData;
	}
	
    private static DataSource getDataSource1(){
        // 生成数据源配置
        HikariConfig hikariConfig = new HikariConfig();
        // 设置数据库信息
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setJdbcUrl(env("DEC_MYSQL_URL", "jdbc:mysql://127.0.0.1:3306/demo_test"));
        hikariConfig.setUsername(env("DEC_MYSQL_USER", "root"));
        hikariConfig.setPassword(env("DEC_MYSQL_PASSWORD", ""));
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setMaximumPoolSize(5);
        DataSource dataSource = new HikariDataSource(hikariConfig);

        return dataSource;
    }

    private static DataSource getDataSource2(){
        // 生成数据源配置
        HikariConfig hikariConfig = new HikariConfig();
        
        // 设置数据库信息
		hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
		hikariConfig.setJdbcUrl(env("DEC_MYSQL_URL_2", env("DEC_MYSQL_URL", "jdbc:mysql://127.0.0.1:3306/demo_test")));
		hikariConfig.setUsername(env("DEC_MYSQL_USER", "root"));
		hikariConfig.setPassword(env("DEC_MYSQL_PASSWORD", ""));
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setMaximumPoolSize(5);
        DataSource dataSource = new HikariDataSource(hikariConfig);

        return dataSource;
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }
}
