package com.cms.admin.company;

import com.cms.util.StrUtil;
import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;


public class CompanyInterceptor implements Interceptor {
	
	public void intercept(ActionInvocation ai) {
		if(StrUtil.exist(ai.getController().getSessionAttr("menu").toString(), ai.getActionKey())){
			ai.invoke();
		}
		else{
			ai.getController().forwardAction("/noright");
		}
	}
}
