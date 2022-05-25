package com.cms.front.directive;
import static com.cms.front.directive.DirectiveUtils.*;
import static freemarker.template.ObjectWrapper.DEFAULT_WRAPPER;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


import com.cms.front.directive.DirectiveUtils;
import com.cms.front.entity.Advertising;

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
public class AdvertisingDirective implements TemplateDirectiveModel{
	/**
	 * 模板名称
	 */
	public static final String PARAM_ID = "id";



	@SuppressWarnings("unchecked")
	public void execute(Environment env, Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException {
		Advertising ad = getAdvertisingid(params, env);
		
		Map<String, TemplateModel> paramWrap = new HashMap<String, TemplateModel>(
				params);
		paramWrap.put(OUT_BEAN, DEFAULT_WRAPPER.wrap(ad));
		Map<String, TemplateModel> origMap = DirectiveUtils
				.addParamsToVariable(env, paramWrap);
		body.render(env.getOut());
		DirectiveUtils.removeParamsFromVariable(env, paramWrap, origMap);
	}
	protected Advertising getAdvertisingid(Map<String, TemplateModel> params,
			Environment env) throws TemplateException {
		Integer id = DirectiveUtils.getInt(PARAM_ID, params);
		final Advertising dao=new Advertising();
		return dao.getByAdvertisingidForTag(id);
	}
}


