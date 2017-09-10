package com.orm.connection.datasource;

import java.sql.SQLException;

import com.orm.connection.datasource.DataSource;

public interface DataSourceFactory {

	public DataSource<?, ?> getDataSource() throws SQLException;
}
