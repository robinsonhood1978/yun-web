package com.cms.front.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public final class AvicRequestWrapper extends HttpServletRequestWrapper {
    public AvicRequestWrapper(HttpServletRequest servletRequest) {
        super(servletRequest);
    }
    public String getParameter(String str){
    	String mystr = super.getParameter(str);
    	if(mystr!=null)
    		mystr = mystr.replaceAll("'", "").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("`", "");
    	return mystr;
    }
    public String[] getParameterValues(String parameter) {
        String[] results = super.getParameterValues(parameter);
        if (results == null)
            return null;
        int count = results.length;

        String[] trimResults = new String[count];
        for (int i = 0; i < count; i++) {
            trimResults[i] = results[i].replaceAll("'", "").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("`", "");
        }
        return trimResults;
    }
}
