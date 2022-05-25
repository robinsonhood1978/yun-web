package com.cms.front.directive;

import static com.cms.front.directive.DirectiveUtils.OUT_LIST;
import static freemarker.template.ObjectWrapper.DEFAULT_WRAPPER;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.cms.admin.content.ContentAtt;
import com.cms.front.directive.DirectiveUtils;
import com.cms.front.directive.DirectiveUtils.InvokeType;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/**
 * 内容列表标签
 * 
 * @author robin
 * 
 */
public class ContentAttListDirective implements TemplateDirectiveModel {
	
	public static final String PARAM_ID = "id";

	@SuppressWarnings("unchecked")
	public void execute(Environment env, Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException {
		List<ContentAtt> list = getList(params, env);

		Map<String, TemplateModel> paramWrap = new HashMap<String, TemplateModel>(
				params);
		paramWrap.put(OUT_LIST, DEFAULT_WRAPPER.wrap(list));
		Map<String, TemplateModel> origMap = DirectiveUtils
				.addParamsToVariable(env, paramWrap);
		InvokeType type = DirectiveUtils.getInvokeType(params);
		if (InvokeType.body == type) {
			body.render(env.getOut());
		} else {
			throw new RuntimeException("invoke type not handled: " + type);
		}
		DirectiveUtils.removeParamsFromVariable(env, paramWrap, origMap);
	}

	@SuppressWarnings("unchecked")
	protected List<ContentAtt> getList(Map<String, TemplateModel> params,
			Environment env) throws TemplateException {
		Integer id = DirectiveUtils.getInt(PARAM_ID, params);
		int count = FrontUtils.getCount(params);
		return ContentAtt.dao.getContentAttList(id, count);
	}
	
}
