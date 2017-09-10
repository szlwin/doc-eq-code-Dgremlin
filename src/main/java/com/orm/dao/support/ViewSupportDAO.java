package com.orm.dao.support;

import com.orm.context.data.ModelData;

public abstract class ViewSupportDAO extends ORMSupportDAO{
	
	public boolean save(ModelData baseData) {
		//ת����
		return false;
	}

	public boolean update(ModelData baseData) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	public ModelData get(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	public ModelData get(long id) {
		// TODO Auto-generated method stub
		return null;
	}*/
	
	public boolean execute(ModelData viewBaseData,String rule) {
		// TODO Auto-generated method stub
		return true;
	}
}
