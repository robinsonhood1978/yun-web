package com.cms.admin.friendlink;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class FriendlinkValidator extends Validator {
	
	protected void validate(Controller controller) {
		validateRequiredString("friendlink.name", "nameMsg", "请输入链接名称!");
	}
	
	protected void handleError(Controller controller) {
		controller.keepModel(Friendlink.class);
		
		String actionKey = getActionKey();
		if (actionKey.equals("/friendlink/save"))
			controller.forwardAction("/friendlink/add");
		else if (actionKey.equals("/friendlink/update"))
			controller.forwardAction("/friendlink/edit");
	}
}

