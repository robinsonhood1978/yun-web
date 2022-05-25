package com.cms.admin.channel;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class ChannelValidator extends Validator {
	
	protected void validate(Controller controller) {
		validateRequiredString("channel.name", "nameMsg", "请输入栏目名称!");
		validateRequiredString("channel.path", "pathMsg", "请输入访问路径!");
	}
	
	protected void handleError(Controller controller) {
		controller.keepModel(Channel.class);
		
		String actionKey = getActionKey();
		if (actionKey.equals("/admin/channel/save"))
			controller.forwardAction("/admin/channel/add");
		else if (actionKey.equals("/admin/channel/update"))
			controller.forwardAction("/admin/channel/edit");
	}
}
