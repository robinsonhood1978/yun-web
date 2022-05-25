package com.cms.admin.user;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class UserValidator extends Validator {
	
	protected void validate(Controller controller) {
		validateRequiredString("user.nick", "usernameMsg", "请输入昵称!");
		validateRequiredString("user.name", "nameMsg", "请输入真实姓名!");
		validateRequiredString("user.pwd", "pwdMsg", "请输入密码!");
		validateEqualField("user.pwd", "pwd_confirm", "pwdConfirmMsg", "两次输入的密码不一致");
	}
	
	protected void handleError(Controller controller) {
		controller.keepModel(User.class);
		
		String actionKey = getActionKey();
		if (actionKey.equals("/admin/user/save"))
			controller.forwardAction("/admin/user/add");
		else if (actionKey.equals("/admin/user/update"))
			controller.render("edit.html");
	}
}
