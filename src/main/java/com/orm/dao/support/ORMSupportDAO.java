package com.orm.dao.support;

import com.orm.context.data.BaseData;
import com.orm.dao.ORMDao;

public abstract class ORMSupportDAO implements ORMDao{

	public boolean save(BaseData baseData) {
		//×ª»»³É
		return false;
	}

	public boolean update(BaseData baseData) {
		// TODO Auto-generated method stub
		return false;
	}

	public BaseData get(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	public BaseData get(long id) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean delete(int id) {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean delete(long id) {
		// TODO Auto-generated method stub
		return true;
	}
}
