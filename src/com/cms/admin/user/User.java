package com.cms.admin.user;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

@SuppressWarnings("serial")
public class User extends Model<User> {
	public static final User dao = new User();
	public List<User> getDealList(Object storeId){
		if(!storeId.toString().equals("0"))
			return User.dao.find("select * from user where store_id=? order by id",storeId.toString());
		else
			return User.dao.find("select * from user order by id");
	}
	public List<User> getCheckList(Object storeId,Object userId){
		if(!storeId.toString().equals("0"))
			return User.dao.find("select * from user where store_id=? and id!=? order by id",storeId.toString(),userId.toString());
		else
			return User.dao.find("select * from user where id!=? order by id",userId.toString());
	}
}