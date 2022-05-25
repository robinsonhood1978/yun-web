package com.cms.admin.content;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class ContentValidator extends Validator {
	
	protected void validate(Controller controller) {
		//validateRequiredString("content.title", "nameMsg", "请输入信息标题!");
		validateToken("myToken", "msg", "alert('上次已保存，请不要重复提交')");
	}
	
	protected void handleError(Controller controller) {
		controller.keepModel(Content.class);
		
		String actionKey = getActionKey();
		if (actionKey.equals("/admin/content/save"))
			controller.forwardAction("/admin/content/add");
		else if (actionKey.equals("/admin/content/update"))
			controller.forwardAction("/admin/content/edit");
		else if (actionKey.equals("/admin/content/tagSave"))
			controller.forwardAction("/admin/content/tagIndex");
		else if (actionKey.equals("/admin/content/tagUpdate"))
			controller.forwardAction("/admin/content/tagIndex");
	}
}
