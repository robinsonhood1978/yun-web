package com.cms.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class StrUtil {
	public static String loadJson (String url) {
        StringBuilder json = new StringBuilder();
        try {
            URL urlObject = new URL(url);
            URLConnection uc = urlObject.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String inputLine = null;
            while ( (inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
            in.close();
        } catch (MalformedURLException e) {
        	e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        }
        return json.toString();
    }
	public static boolean exist(String menu,String s){		
		String uris = "/back/manage/,/back/score/";
		String urlPrefix = "";
		if(uris.indexOf(s)>-1){
			urlPrefix = s;
		}
		else{
			if(s.lastIndexOf("/")>0){
				urlPrefix = s.substring(0, s.indexOf("/", 1));
			}
			else{
				urlPrefix = s;
			}
		}
		String exclude = "/taskin/,/bugin/";
		if(exclude.indexOf(urlPrefix)>-1)return true;
		if(menu.indexOf(urlPrefix)>-1){
			return true;
		}
		else{
			return false;
		}
	}
	public static boolean isBlank(String s){
		if(s!=null){
			if("".equals(s.trim())){
				return true;
			}
		}
		return false;
	}
	public static String null2Blank(Integer s){
		if(s!=null){
			return String.valueOf(s);
		}
		return "";
	}
	public static String null2Blank(String s){
		if(s!=null){
			return s;
		}
		return "";
	}
	public static int null2Zero(Integer s){
		if(s!=null){
			return s.intValue();
		}
		return 0;
	}
	public static int null2Zero(String s){
		if(s!=null && !"".equals(s)){
			return Integer.parseInt(s);
		}
		return 0;
	}
	public static String getRouteByController(String str) {
		String route="";
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if(Character.isUpperCase(ch)){
				route+=("/"+Character.toLowerCase(ch));
			}else{
				route+=ch;
			}
		}	
		return route;
	}
	/**
	 * 数据替换
	 * @param dbInfo
	 * @param htmlStr
	 * @return
	 */
	public static String readHtmlFile(String filename) {
		Document doc = null;
		 try {
		     doc = Jsoup.parse(new File(filename), "UTF-8");
		 } catch (Exception ex) {
		 }
		    
		 String html = doc.toString();
		 
		 //System.out.println(html);
	    return html;
	}
	public static String getSubString(String str, int length) {
        int count = 0;
        int offset = 0;
        char[] c = str.toCharArray();
        int size = c.length;
        if(size >= length){
            for (int i = 0; i < c.length; i++) {
                if (c[i] > 256) {
                    offset = 2;
                    count += 2;
                } else {
                    offset = 1;
                    count++;
                }
                if (count == length) {
                    return str.substring(0, i + 1);
                }
                if ((count == length + 1 && offset == 2)) {
                    return str.substring(0, i);
                }
            }
        }else{
            return str;
        }
        return "";
    }
	 public static void main(String[] args) throws UnsupportedEncodingException {
	    //System.out.println(StrUtil.readHtmlFile("/Users/mark/bitbucket/Cms/WebRoot/"));
		 String url = "https://packns.com/pack/tracking/YUN017409S";
		 String json = loadJson(url);
       System.out.println(json);
		 
	 }
}
