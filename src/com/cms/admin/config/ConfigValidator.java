package com.cms.admin.config;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class ConfigValidator extends Validator {
	
	protected void validate(Controller controller) {
	}
	
	protected void handleError(Controller controller) {
		controller.keepModel(Config.class);
		
		String actionKey = getActionKey();
		if (actionKey.equals("/config/save"))
			controller.render("add.html");
		else if (actionKey.equals("/config/update"))
			controller.render("edit.html");
	}
}
