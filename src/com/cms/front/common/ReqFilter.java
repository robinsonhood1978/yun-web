package com.cms.front.common;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class ReqFilter implements Filter {
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse)response;
		String url=req.getRequestURI();
		if (url.indexOf(".")<0) {
			//System.out.println("匹配--------"+url);
			HttpServletRequest req2 = new AvicRequestWrapper(req);
			chain.doFilter(req2, resp);
        } 
		else{
        	//System.out.println("不匹配--------"+url);
			chain.doFilter(request, response);
		}

		
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		//System.out.println("防SQL注入拦截器启动...");
	}
}
