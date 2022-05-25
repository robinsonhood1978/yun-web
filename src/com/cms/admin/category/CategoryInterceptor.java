package com.cms.admin.category;

import com.cms.util.StrUtil;
import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;


public class CategoryInterceptor implements Interceptor {
	
	public void intercept(ActionInvocation ai) {
		System.out.println(ai.getActionKey());
		if(StrUtil.exist(ai.getController().getSessionAttr("menu").toString(), ai.getActionKey())){
			ai.getController().setAttr("ap", ai.getViewPath());
			ai.getController().setAttr("ak", ai.getActionKey());
			ai.getController().setAttr("method", ai.getMethodName());
			ai.invoke();
		}
		else{
			ai.getController().forwardAction("/noright");
		}
	}
}
