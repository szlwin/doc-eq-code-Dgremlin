package dec.demo.directory;


import com.alibaba.fastjson.JSON;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dec.core.context.config.model.directory.DirectoryInfo;
import dec.core.context.config.utils.ConfigContextUtil;
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
		ConfigUtil.parseConfigInfo("classpath:directory/orm-config.xml");

		DataSourceManager.addDataSource("data1", getDataSource());
		DataSourceManager.addDataSource("data2", getDataSource());

		DataSourceManager.addConnectionFactory("MySQL", new MySQLDBConnectionFactory());

		DataSourceManager.addConvertContainerFactory("MySQL", new MySQLConvertContainerFactory());

		DataSourceManager.addDataConvertContainerFacory("MySQL", new MySQLDataConvertContainerFactory());

		DataSourceManager.addExecuteContainerFacory("MySQL", new MySQLExecuteContainerFactory());

		DirectoryInfo directoryInfo = ConfigContextUtil.getConfigInfo().getDirectory("user");

		System.out.println(JSON.toJSONString(directoryInfo));
	}

	private static DataSource getDataSource(){
		// 生成数据源配置
		HikariConfig hikariConfig = new HikariConfig();
		// 设置数据库信息
		hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
		hikariConfig.setJdbcUrl("jdbc:mysql://xxx.xxx.xxx.xxx:3306/demo-test2");
		hikariConfig.setUsername("root");
		hikariConfig.setPassword("root");
		// 设置可以获取tables remarks信息
		//hikariConfig.addDataSourceProperty("useInformationSchema", "true");
		hikariConfig.setMinimumIdle(2);
		hikariConfig.setMaximumPoolSize(5);
		DataSource dataSource = new HikariDataSource(hikariConfig);

		return dataSource;
	}
	
}
