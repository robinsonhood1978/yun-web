package com.cms.admin.goods;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class GoodsValidator extends Validator {
	
	protected void validate(Controller controller) {
		validateRequiredString("goods.name", "nameMsg", "请输入设备名称!");
	}
	
	protected void handleError(Controller controller) {
		controller.keepModel(Goods.class);
		
		String actionKey = getActionKey();
		if (actionKey.equals("/admin/goods/save"))
			controller.forwardAction("/admin/goods/add");
		else if (actionKey.equals("/admin/goods/update"))
			controller.forwardAction("/admin/goods/edit");
	}
}
