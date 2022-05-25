package com.cms.admin.common;

import java.util.List;

import com.cms.admin.directive.MenuDirective;
import com.cms.admin.user.User;
import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.plugin.activerecord.Db;


public class AdminInterceptor implements Interceptor {
	
	public void intercept(ActionInvocation ai) {
		ai.getController().setAttr("menu", new MenuDirective());
		User loginUser = ai.getController().getSessionAttr("user");
		//开发用
		//--start--
		/*if(loginUser==null){
			loginUser = User.dao.findById(1);
			List<String> uris = Db.query("select uri from user u,role_uri ru where u.role_id = ru.role_id and u.id=1");
			StringBuffer sb = new StringBuffer();
			for(String uri:uris){
				sb.append(uri+",");
			}
			// 放数据至session
			ai.getController().setSessionAttr("user", loginUser);
			ai.getController().setSessionAttr("userId", 1);
			ai.getController().setSessionAttr("storeId", 1);
			ai.getController().setSessionAttr("menu", sb.toString());
		}*/
		//--end---
		if(loginUser!=null){
			ai.invoke();
		}
		else{
			ai.getController().forwardAction("/admin");
		}
	}
}