package com.cms.admin.user;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class PersonValidator extends Validator {
	
	protected void validate(Controller controller) {
		validateEqualField("user.pwd", "pwd_confirm", "pwdConfirmMsg", "两次输入的密码不一致");
	}
	
	protected void handleError(Controller controller) {
		controller.keepModel(User.class);
		
		String actionKey = getActionKey();
		if (actionKey.equals("/admin/user/pwd")){
			controller.forwardAction("/admin/user/person/");
		}
	}
}
