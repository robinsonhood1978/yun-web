package com.cms.admin.role;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class RoleValidator extends Validator {
	
	protected void validate(Controller controller) {
		validateRequiredString("role.name", "nameMsg", "请输入角色名称!");
	}
	
	protected void handleError(Controller controller) {
		controller.keepModel(Role.class);
		
		String actionKey = getActionKey();
		if (actionKey.equals("/role/save"))
			controller.render("add.html");
		else if (actionKey.equals("/role/update"))
			controller.render("edit.html");
	}
}
