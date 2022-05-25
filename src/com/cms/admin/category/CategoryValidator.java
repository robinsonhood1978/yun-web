package com.cms.admin.category;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class CategoryValidator extends Validator {
	
	protected void validate(Controller controller) {
		validateRequiredString("category.name", "nameMsg", "请输入设备目录名称!");
	}
	
	protected void handleError(Controller controller) {
		controller.keepModel(Category.class);
		
		String actionKey = getActionKey();
		if (actionKey.equals("/category/save"))
			controller.render("add.html");
		else if (actionKey.equals("/category/update"))
			controller.render("edit.html");
	}
}
