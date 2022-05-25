package com.cms.admin.friendlinkctg;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class FriendlinkctgValidator extends Validator {
	
	protected void validate(Controller controller) {
		validateRequiredString("friendlinkctg.name", "nameMsg", "请输入链接名称!");
	}
	
	protected void handleError(Controller controller) {
		controller.keepModel(Friendlinkctg.class);
		
		String actionKey = getActionKey();
		if (actionKey.equals("/friendlinkctg/save"))
			controller.forwardAction("/friendlinkctg/add");
		else if (actionKey.equals("/friendlinkctg/update"))
			controller.forwardAction("/friendlinkctg/edit");
	}
}

