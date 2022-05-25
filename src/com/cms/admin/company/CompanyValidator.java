package com.cms.admin.company;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class CompanyValidator extends Validator {
	
	protected void validate(Controller controller) {
		validateRequiredString("company.name", "nameMsg", "请输入公司名称!");
	}
	
	protected void handleError(Controller controller) {
		controller.keepModel(Company.class);
		
		String actionKey = getActionKey();
		if (actionKey.equals("/company/save"))
			controller.render("add.html");
		else if (actionKey.equals("/company/update"))
			controller.render("edit.html");
	}
}
