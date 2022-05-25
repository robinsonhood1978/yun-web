package com.cms.front.common;

import com.cms.admin.directive.MenuDirective;
import com.cms.admin.user.User;
import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;


public class GlobalInterceptor implements Interceptor {
	
	public void intercept(ActionInvocation ai) {
		String current_url = ai.getController().getRequest().getRequestURL().toString();
		String visit_url = current_url;
		current_url = current_url.substring(7);
		String vurl = current_url;
		vurl = vurl.substring(0,current_url.indexOf("/"));
		if(vurl.equals("zhhzsh.com")||vurl.equals("www.zhhzsh.com")){
			visit_url = visit_url.replaceAll(vurl, "123.56.179.134:82");
			System.out.println("vurl===="+vurl);
			System.out.println("visit_url===="+visit_url);
			ai.getController().redirect(visit_url);
		}
		else{
			current_url = current_url.substring(current_url.indexOf("/"));
			
			String referer_url = ai.getController().getRequest().getHeader("referer");
			if(referer_url!=null){
				referer_url = referer_url.substring(7);
				referer_url = referer_url.substring(referer_url.indexOf("/"));
				if(current_url.equals("/l")){
					if(referer_url.equals("/")||referer_url.equals("/logout")||referer_url.equals("/login"))referer_url="/m";
					ai.getController().setSessionAttr("furl", referer_url);
				}
			}
			
			//System.out.println(referer_url);
			ai.getController().setAttr("menu", new MenuDirective());
			//User loginUser = ai.getController().getSessionAttr("user");
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
			ai.invoke();
		}
	}
}