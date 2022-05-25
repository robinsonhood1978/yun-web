package com.cms.util;

import java.io.FileReader;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
 
/**
* @author Piconjo
*/
public class Html2PlainText {
	public static String convert(String html)
	{
		if (StringUtils.isEmpty(html))
		{
		return "";
		}
		 
		Document document = Jsoup.parse(html);
		Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(true);
		document.outputSettings(outputSettings);
		//document.select("br").append("\\n");
		//document.select("p").prepend("\\n");
		//document.select("p").append("\\n");
		//String newHtml = document.html().replaceAll("\\\\n", "\n");
		String newHtml = document.html();
		String plainText = Jsoup.clean(newHtml, "", Whitelist.none(), outputSettings);
		String result = StringEscapeUtils.unescapeHtml(plainText.trim());
		return result;
	}
	public static void main (String[] args) {
		   try {
		     // the HTML to convert
			 //Reader in=new StringReader("string");	
			 String html =  StrUtil.readHtmlFile("/Users/mark/bitbucket/Cms/WebRoot/");
		     Html2PlainText parser = new Html2PlainText();
		     String txt = parser.convert(html);
		     System.out.println(txt);
		   }
		   catch (Exception e) {
		     e.printStackTrace();
		   }
		 }
}