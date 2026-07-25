package dec.demo.system;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dec.context.parse.xml.exception.XMLParseException;
import dec.context.parse.yaml.parse.config.YamlConfigUtil;
import dec.core.context.config.manager.ConfigManager;
import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.config.model.data.TableInfo;
import dec.core.context.config.model.view.ViewData;
import dec.core.starter.common.ConfigUtil;
import dec.core.starter.common.DataSourceManager;
import dec.external.datasource.sql.mysql.connection.factory.MySQLDBConnectionFactory;
import dec.external.datasource.sql.mysql.convert.container.factory.MySQLConvertContainerFactory;
import dec.external.datasource.sql.mysql.datatype.convert.factory.MySQLDataConvertContainerFactory;
import dec.external.datasource.sql.mysql.execute.container.factory.MySQLExecuteContainerFactory;

import javax.sql.DataSource;

public class ConfigInit {


	public static void main(String args[]) throws Exception{
		init();
	}
	
	public static void init() throws Exception {
		ConfigUtil.addDataSourceConfig("MySQL", "dec.external.datasource.sql.datasource.DBDataSource");
		ConfigUtil.parseConfigInfo("classpath:system/orm-config.xml");
		//YamlConfigUtil.parseConfigInfo("classpath:yaml/system/orm-config.yaml");
		DataSourceManager.addDataSource("data1", getDataSource());
		DataSourceManager.addDataSource("data2", getDataSource());

		DataSourceManager.addConnectionFactory("MySQL", new MySQLDBConnectionFactory());

		DataSourceManager.addConvertContainerFactory("MySQL", new MySQLConvertContainerFactory());

		DataSourceManager.addDataConvertContainerFacory("MySQL", new MySQLDataConvertContainerFactory());

		DataSourceManager.addExecuteContainerFacory("MySQL", new MySQLExecuteContainerFactory());
	}

	private static DataSource getDataSource(){
		// 生成数据源配置
		HikariConfig hikariConfig = new HikariConfig();
		// 设置数据库信息
		hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
		hikariConfig.setJdbcUrl(env("DEC_MYSQL_URL", "jdbc:mysql://127.0.0.1:3306/demo-test2"));
		hikariConfig.setUsername(env("DEC_MYSQL_USER", "root"));
		hikariConfig.setPassword(env("DEC_MYSQL_PASSWORD", "mysqldb"));
		// 设置可以获取tables remarks信息
		//hikariConfig.addDataSourceProperty("useInformationSchema", "true");
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
