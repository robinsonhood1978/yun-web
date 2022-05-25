package com.cms.front.directive;

import freemarker.template.TemplateModelException;

/**
 * 缺少必须参数异常
 * 
 * @author robin
 * 
 */
@SuppressWarnings("serial")
public class ParamsRequiredException extends TemplateModelException {
	public ParamsRequiredException(String paramName) {
		super("The required \"" + paramName + "\" paramter is missing.");
	}
}
