package com.cms.front.entity;

import com.jfinal.plugin.activerecord.Model;

@SuppressWarnings("serial")
public class Advertising extends Model<Advertising> {
	public Advertising getByAdvertisingidForTag(Integer advertisingid) {
		final Advertising dao=new Advertising();
		return dao.findById(advertisingid);
	}
}