package com.cms.admin.config;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;

@Before(ConfigInterceptor.class)
public class AdminConfigController extends Controller {
	public void index() {
		setAttr("configPage", Config.dao.paginate(getParaToInt(0, 1), 10, "select c.*", 
				"from config c order by c.id asc"));
		render("list.html");
	}
	
	
	public void edit() {
		setAttr("config", Config.dao.findById(getParaToInt()));
	}
	
	public void update() {
		String id = getPara("config.id");
		String value = getPara("config.value");
		
		getModel(Config.class).set("value", value).update();
		forwardAction("/admin/config");
	}
}


