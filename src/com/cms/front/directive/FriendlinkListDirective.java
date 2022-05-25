package com.cms.front.directive;

import static com.cms.front.directive.DirectiveUtils.OUT_LIST;
import static freemarker.template.ObjectWrapper.DEFAULT_WRAPPER;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cms.admin.friendlink.Friendlink;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/**
 * 友情链接类别列表标签
 * 
 * @author robin
 * 
 */
public class FriendlinkListDirective implements TemplateDirectiveModel {
	/**
	 * 排序。
	 */
	public static final String PARAM_ORDER_BY = "orderBy";



	@SuppressWarnings("unchecked")
	public void execute(Environment env, Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException {
		List<Friendlink> list = getFriendlinkId(params, env);
		Map<String, TemplateModel> paramWrap = new HashMap<String, TemplateModel>(
				params);
		paramWrap.put(OUT_LIST, DEFAULT_WRAPPER.wrap(list));
		Map<String, TemplateModel> origMap = DirectiveUtils
				.addParamsToVariable(env, paramWrap);
		body.render(env.getOut());
		DirectiveUtils.removeParamsFromVariable(env, paramWrap, origMap);
	}
	protected int getOrderBy(Map<String, TemplateModel> params)
	throws TemplateException {
		Integer orderBy = DirectiveUtils.getInt(PARAM_ORDER_BY, params);
		if (orderBy == null) {
			return 0;
		} else {
			return orderBy;
		}
	}
	private List<Friendlink> getFriendlinkId(Map<String, TemplateModel> params,
			Environment env) throws TemplateException {
		Integer count = FrontUtils.getCount(params);
		int orderBy = getOrderBy(params);
		System.out.println(count);
		return Friendlink.dao.getListByFriendlinkForTag(orderBy,count);
	
	}



}
