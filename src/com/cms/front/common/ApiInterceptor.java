package com.cms.front.common;

import java.util.HashMap;
import java.util.Map;

import com.cms.admin.user.User;
import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;


public class ApiInterceptor implements Interceptor {
	
	public void intercept(ActionInvocation ai) {
		int code=0;
		Map map = new HashMap();
		map.put("code",code);
		
		User loginUser = ai.getController().getSessionAttr("user");
		if(loginUser!=null){
			ai.invoke();
		}
		else{
			ai.getController().renderJson(map);
		}
	}
}