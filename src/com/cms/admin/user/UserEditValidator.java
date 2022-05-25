package com.cms.admin.user;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class UserEditValidator extends Validator {
	
	protected void validate(Controller controller) {
		validateRequiredString("user.nick", "usernameMsg", "用户名不能为空，请重新录入!");
		validateEqualField("user.pwd", "pwd_confirm", "pwdConfirmMsg", "两次输入的密码不一致");
		validateRequiredString("user.name", "nameMsg", "请输入真实姓名!");
	}
	
	protected void handleError(Controller controller) {
		controller.keepModel(User.class);
		
		String actionKey = getActionKey();
		if (actionKey.equals("/admin/user/update")){
			controller.forwardAction("/admin/user/edit/"+controller.getPara("user.id"));
		}
	}
}
