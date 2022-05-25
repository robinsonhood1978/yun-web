package com.cms.front.directive;

import java.util.List;
import java.util.Map;


import com.cms.front.entity.Content;
import com.cms.util.StringUtils;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/**
 * 内容标签基类
 * 
 * @author robin
 * 
 */
public abstract class AbstractContentDirective implements
		TemplateDirectiveModel {
	/**
	 * 输入参数，TAG ID。允许多个TAG ID，用","分开。和tagNames之间二选一，ID优先级更高。
	 */
	public static final String PARAM_TAG_ID = "tagId";
	/**
	 * 输入参数，TAG NAME。允许多个TAG NAME，用","分开。
	 */
	public static final String PARAM_TAG_NAME = "tagName";
	/**
	 * 输入参数，专题ID。
	 */
	public static final String PARAM_TOPIC_ID = "topicId";
	/**
	 * 输入参数，栏目ID。允许多个栏目ID，用","分开。和channelPath之间二选一，ID优先级更高。
	 */
	public static final String PARAM_CHANNEL_ID = "channelId";
	/**
	 * 输入参数，栏目路径。允许多个栏目路径，用","分开。
	 */
	public static final String PARAM_CHANNEL_PATH = "channelPath";
	/**
	 * 输入参数，栏目选项。用于单栏目情况下。0：自身栏目；1：包含子栏目；2：包含副栏目。
	 */
	public static final String PARAM_CHANNEL_OPTION = "channelOption";
	/**
	 * 输入参数，站点ID。可选。允许多个站点ID，用","分开。
	 */
	public static final String PARAM_SITE_ID = "siteId";
	/**
	 * 输入参数，类型ID。可选。允许多个类型ID,用","分开。
	 */
	public static final String PARAM_TYPE_ID = "typeId";
	/**
	 * 输入参数，推荐。0：所有；1：推荐；2：不推荐。默认所有。
	 */
	public static final String PARAM_RECOMMEND = "recommend";
	/**
	 * 输入参数，标题。可以为null。
	 */
	public static final String PARAM_TITLE = "title";
	/**
	 * 输入参数，标题图片。0：所有；1：有；2：没有。默认所有。
	 */
	public static final String PARAM_IMAGE = "image";
	/**
	 * 输入参数，排序方式。
	 */
	public static final String PARAM_ORDER_BY = "orderBy";
	/**
	 * 输入参数，不包含的文章ID。用于按tag查询相关文章。
	 */
	public static final String PARAM_EXCLUDE_ID = "excludeId";
	/**
	 * 输入参数，文章ID。用于嘉宾名录与此ID以外的所用嘉宾。
	 */
	public static final String CONTENT_ID = "contentId";

	protected Integer getTopicId(Map<String, TemplateModel> params)
			throws TemplateException {
		return DirectiveUtils.getInt(PARAM_TOPIC_ID, params);
	}
	
	protected Integer getExcludeId(Map<String, TemplateModel> params)
			throws TemplateException {
		return DirectiveUtils.getInt(PARAM_EXCLUDE_ID, params)== null ? null :DirectiveUtils.getInt(PARAM_EXCLUDE_ID, params);
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

	protected String[] getChannelPaths(Map<String, TemplateModel> params)
			throws TemplateException {
		String nameStr = DirectiveUtils.getString(PARAM_CHANNEL_PATH, params);
		if (StringUtils.isBlank(nameStr)) {
			return null;
		}
		return StringUtils.split(nameStr, ',');
	}


	protected int getChannelOption(Map<String, TemplateModel> params)
			throws TemplateException {
		Integer option = DirectiveUtils.getInt(PARAM_CHANNEL_OPTION, params);
		if (option == null ) {
			return 0;
		} else {
			return option;
		}
	}

	protected Integer[] getSiteIds(Map<String, TemplateModel> params)
			throws TemplateException {
		Integer[] siteIds = DirectiveUtils.getIntArray(PARAM_SITE_ID, params);
		return siteIds;
	}

	protected Integer[] getTypeIds(Map<String, TemplateModel> params)
			throws TemplateException {
		Integer[] typeIds = DirectiveUtils.getIntArray(PARAM_TYPE_ID, params);
		return typeIds;
	}

	protected Boolean getHasTitleImg(Map<String, TemplateModel> params)
			throws TemplateException {
		String titleImg = DirectiveUtils.getString(PARAM_IMAGE, params);
		if ("1".equals(titleImg)) {
			return true;
		} else if ("2".equals(titleImg)) {
			return false;
		} else {
			return null;
		}
	}

	protected Boolean getRecommend(Map<String, TemplateModel> params)
			throws TemplateException {
		String recommend = DirectiveUtils.getString(PARAM_RECOMMEND, params);
		if ("1".equals(recommend)) {
			return true;
		} else if ("0".equals(recommend)) {
			return false;
		} else {
			return null;
		}
	}

	protected String getTitle(Map<String, TemplateModel> params)
			throws TemplateException {
		return DirectiveUtils.getString(PARAM_TITLE, params);
	}
	
	protected Integer getGuestId(Map<String, TemplateModel> params)
			throws TemplateException {
		return DirectiveUtils.getInt("guestId", params);
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
	protected int getContentId(Map<String, TemplateModel> params)
	throws TemplateException {
		Integer contentId = DirectiveUtils.getInt(CONTENT_ID, params);
		if (contentId == null) {
			return 0;
		} else {
			return contentId;
		}
		}

	protected Object getData(Map<String, TemplateModel> params, Environment env)
			throws TemplateException {
		int orderBy = getOrderBy(params);
		Boolean titleImg = getHasTitleImg(params);
		Boolean recommend = getRecommend(params);
		Integer[] typeIds = getTypeIds(params);
		String title = getTitle(params);
		int count = FrontUtils.getCount(params);
		Integer guestId = getGuestId(params);

		Integer[] channelIds = getChannelIds(params);
		
		int option = getChannelOption(params);
		int first = FrontUtils.getFirst(params);
		int contentId = getContentId(params);
		
		Integer excludeId = getExcludeId(params);
		
		if(isPage()){
			int pageNo = FrontUtils.getPageNoP(params);
			return Content.dao.getPageByChannelIdsForTag(channelIds,
					typeIds, titleImg, recommend, title, orderBy, option,
					first, pageNo,excludeId,count,guestId,contentId);
		}
		else{
			return Content.dao.getListByChannelIdsForTag(channelIds,
					typeIds, titleImg, recommend, title, orderBy, option,
					first, count,excludeId,contentId);
		}

	}
	
	abstract protected boolean isPage();
}
