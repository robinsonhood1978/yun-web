package com.cms.front.directive;

import static com.cms.front.directive.DirectiveUtils.OUT_LIST;
import static freemarker.template.ObjectWrapper.DEFAULT_WRAPPER;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.cms.admin.channel.Channel;
import com.cms.front.directive.DirectiveUtils;
import com.cms.front.directive.DirectiveUtils.InvokeType;
import com.cms.front.entity.Content;

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
public class ChannelListDirective implements TemplateDirectiveModel{
	/**
	 * 模板名称
	 */
	public static final String TPL_NAME = "content_list";
	/**
	 * 输入参数，栏目ID。允许多个栏目ID，用","分开。和channelPath之间二选一，ID优先级更高。
	 */
	public static final String PARAM_CHANNEL_ID = "channelId";

	/**
	 * 输入参数，文章ID。允许多个文章ID，用","分开。排斥其他所有筛选参数。
	 */
	public static final String PARAM_IDS = "ids";

	@SuppressWarnings("unchecked")
	public void execute(Environment env, Map params, TemplateModel[] loopVars,
			TemplateDirectiveBody body) throws TemplateException, IOException {
		List<Content> list = getList(params, env);

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
	protected List<Content> getList(Map<String, TemplateModel> params,
			Environment env) throws TemplateException {
		return (List<Content>) getData(params, env);
	}
	
	protected Integer[] getChannelIds(Map<String, TemplateModel> params)
	throws TemplateException {
		Integer[] ids = DirectiveUtils.getIntArray(PARAM_CHANNEL_ID, params);
		
		if (ids != null && ids.length > 0) {
			System.out.println("ids=="+ids.length+"++value=="+ids[0]);
			return ids;
		} else {
			return null;
		}
	}
	protected int getChannelId(Map<String, TemplateModel> params)
	throws TemplateException {
		Integer id = DirectiveUtils.getInt(PARAM_CHANNEL_ID, params);
		if (id == null) {
			return 0;
		} else {
			return id;
		}
	}
	protected Object getData(Map<String, TemplateModel> params, Environment env)
	throws TemplateException {
		Integer channelId = getChannelId(params);
		int count = FrontUtils.getCount(params);
		
		return Channel.dao.getList(channelId,count);
	}
		

}
