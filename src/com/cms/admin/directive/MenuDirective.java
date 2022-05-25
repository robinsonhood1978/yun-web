package com.cms.admin.directive;

import java.io.IOException;
import java.util.Map;


import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class MenuDirective implements TemplateDirectiveModel {

	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException {
		boolean pass = false;
		String menu ="";
		if (params.containsKey("all") && params.get("all") != null) {
			menu = params.get("all").toString();
		}
		if (params.containsKey("url") && params.get("url") != null) {
		    String url = params.get("url").toString();
		    int i = menu.indexOf(url+",");
		    if(i>-1)pass=true;
		} 
		if (pass) {
			body.render(env.getOut());
		}
	}

}
