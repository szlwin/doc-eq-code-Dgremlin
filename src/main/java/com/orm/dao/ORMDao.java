package com.orm.dao;

import com.orm.context.data.BaseData;

public interface ORMDao {
	
	public boolean save(BaseData baseData);
	
	public boolean update(BaseData baseData);
	
	public boolean delete(int id);
	
	public boolean delete(long id);
	
	public BaseData get(int id);
	
	public BaseData get(long id);
}
