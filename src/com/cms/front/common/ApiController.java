package com.cms.front.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane;

import com.cms.admin.channel.Channel;
import com.cms.admin.common.AdminInterceptor;
import com.cms.admin.content.Content;
import com.cms.admin.content.ContentCount;
import com.cms.admin.goods.Goods;
import com.cms.admin.user.User;
import com.cms.util.DateFmt;
import com.cms.util.Html2PlainText;
import com.cms.util.MailRun;
import com.cms.util.QuoteMailRun;
import com.cms.util.Md5;
import com.cms.util.Paginable;
import com.cms.util.RSACoderApp;
import com.cms.util.SendMail;
import com.cms.util.StringUtils;
import com.google.code.kaptcha.Constants;
import com.jfinal.aop.Before;
import com.jfinal.aop.ClearInterceptor;
import com.jfinal.aop.ClearLayer;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.cms.util.SimplePage;
import com.cms.util.StrUtil;

@Before(ApiInterceptor.class)
public class ApiController extends Controller {
	private static SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmsssS");
	private static SimpleDateFormat ym = new SimpleDateFormat("yyyyMM");
	private static String uploadroot = "/upload/";
	
	@ClearInterceptor(ClearLayer.ALL)
	public void track() {
		//String str = "{\"message\": \"Get tracking info successfully.\", \"code\": 0, \"object\": [{\"id\": 912888, \"create_time\": \"2020-12-29T16:27:44.873240+13:00\", \"event\": \"\\u8ba1\\u5212\\u4e2d\", \"location\": \"None\", \"params\": null, \"icon\": null, \"user\": \"\\u674e\\u5fd7\\u6708\", \"customer_visible\": true, \"lat\": 0.0, \"lng\": 0.0, \"parcel\": null, \"fo\": 17409, \"freight\": null, \"editor\": null}, {\"id\": 1186835, \"create_time\": \"2021-01-26T19:36:09.050648+13:00\", \"event\": \"Combine FO from 19639,\", \"location\": null, \"params\": null, \"icon\": null, \"user\": \"\\u8273\\u534e \\u9ec4\", \"customer_visible\": true, \"lat\": 0.0, \"lng\": 0.0, \"parcel\": null, \"fo\": 17409, \"freight\": null, \"editor\": null}, {\"id\": 1191060, \"create_time\": \"2021-01-27T23:29:21.050956+13:00\", \"event\": \"Combine FO from 19837,\", \"location\": null, \"params\": null, \"icon\": null, \"user\": \"\\u8273\\u534e \\u9ec4\", \"customer_visible\": true, \"lat\": 0.0, \"lng\": 0.0, \"parcel\": null, \"fo\": 17409, \"freight\": null, \"editor\": null}]}";
		//String id ="YUN017409S";
		String id="";
		if(getPara()!=null && !"".equals(getPara())) {
			id = getPara();
		}
		String url = "https://packns.com/pack/tracking/"+id;
		//System.out.println(url);
		String str = StrUtil.loadJson(url);
		renderJson(str);
	}
	@ClearInterceptor(ClearLayer.ALL)
	public void l() {
		int code=0;
		String user = getPara("user");
		String pwd = getPara("pwd");
		
		user = RSACoderApp.decrypt(user);
		pwd = RSACoderApp.decrypt(pwd);
		
		boolean login=false;

		List<User> list = User.dao.find("select u.*,r.name as role_name from user u,role r where u.role_id = r.id and username='"+user+"' and pwd='"+Md5.encodePassword(pwd)+"'");
		if(list.size()==1){
			List<String> uris = Db.query("select uri from user u,role_uri ru where u.role_id = ru.role_id and u.id="+list.get(0).getInt("id"));
			StringBuffer sb = new StringBuffer();
			for(String uri:uris){
				sb.append(uri+",");
			}
			// 放数据至session
			setSessionAttr("user", list.get(0));
			setSessionAttr("userId", list.get(0).getInt("id"));
			setSessionAttr("storeId", list.get(0).getInt("store_id"));
			setSessionAttr("menu", sb.toString());
			login=true;
		}
			
		if (login) {
			code=1;
		}

		Map map = new HashMap();
		map.put("code",code);
		renderJson(map);
	}
	public void o(){
		int code=0;
		// 删除session中的属性
		removeSessionAttr("user");
		removeSessionAttr("menu");
		removeSessionAttr("userId");
		removeSessionAttr("storeId");

		Map map = new HashMap();
		map.put("code",code);
		renderJson(map);
	}
	@ClearInterceptor(ClearLayer.ALL)
	public void status() {
		int code=0;
		User user = getSessionAttr("user");
		if(user!=null){
			code=1;
		}
		Map map = new HashMap();
		map.put("code",code);
		renderJson(map);
	}
	public void quote_status() {
		int code=0;
		String msg="Error";
		 try {
			 if(getPara("status")!=null && !"".equals(getPara("status")) && getPara("id")!=null && !"".equals(getPara("id"))) 
			 {
				 int status  = getParaToInt("status");
				 int id  = getParaToInt("id");
				 Db.update("update quote set viewed=? where id=?",status,id);
			
				 code = 200;
				 msg="OK";
			 }
		 }
		 catch(Exception e) {
			 
		 }
		Map map = new HashMap();
		map.put("code",code);
		map.put("msg",msg);
		renderJson(map);
	}
	public void quote_del() {
		int code=0;
		String msg="Error";
		 try {
			 if(getPara("id")!=null && !"".equals(getPara("id"))) 
			 {
				 int id  = getParaToInt("id");
				 Db.update("delete from quote where id=?",id);
			
				 code = 200;
				 msg="OK";
			 }
		 }
		 catch(Exception e) {
			 
		 }
		Map map = new HashMap();
		map.put("code",code);
		map.put("msg",msg);
		renderJson(map);
	}
	public void quote_reply() {
		int code=0;
		String msg="Error";
		 try {
			 if(getPara("status")!=null && !"".equals(getPara("status")) && getPara("id")!=null && !"".equals(getPara("id"))) 
			 {
				 int status  = getParaToInt("status");
				 int id  = getParaToInt("id");
				 Db.update("update quote set replied=? where id=?",status,id);
			
				 code = 200;
				 msg="OK";
			 }
		 }
		 catch(Exception e) {
			 
		 }
		Map map = new HashMap();
		map.put("code",code);
		map.put("msg",msg);
		renderJson(map);
	}
	public void quote_list() {
		int code=0;
		String msg="Error";
		int limit= 10;
		int page = 1;
		if(getPara("limit")!=null&&!"".equals(getPara("limit")))
			limit = getParaToInt("limit");
		if(getPara("page")!=null&&!"".equals(getPara("page")))
			page = getParaToInt("page");

		int start = (page-1)*limit;
		
		int total = Db.queryLong("select count(*) from quote").intValue();
				
		List<Record> l = Db.find("select a.id,a.freight_type,a.weight,a.departure,a.destination,a.viewed,a.replied,"
				+ "a.email,date_format(a.create_time,'%Y-%m-%d') create_date from quote a order by a.id desc limit ?,?",start,limit);
		
		
		Map map = new HashMap();
		map.put("code",code);
		map.put("msg",msg);
		map.put("count", total);
		map.put("data", l);
		renderJson(map);
	}
	@ClearInterceptor(ClearLayer.ALL)
	public void quote() {
		int code=0;
		String msg="Error";
		 try {
			 final String realPath = this.getRequest().getRealPath("/");
			 final String freight_type = getPara("quote_freight"); 
			 final String weight = getPara("quote_weight"); 
			 final String departure = getPara("quote_departure"); 
			 final String destination = getPara("quote_destination"); 
			 final String email = getPara("quote_email"); 
			 if(email!=null && !"".equals(email) 
					 && freight_type!=null && !"".equals(freight_type) 
					 && weight!=null && !"".equals(weight) 
					 && departure!=null && !"".equals(departure) 
					 && destination!=null && !"".equals(destination)) 
			 {
				 Db.update("insert into quote (freight_type,weight,departure,destination,email) values (?,?,?,?,?)",freight_type,weight,departure,destination,email);
				 
				 final String subject = "Quote Form";
				 new Thread(new QuoteMailRun(realPath,subject,freight_type,weight,departure,destination,email,null) {

			    	    @Override
			    	    public void run() {
			    	    	SendMail.asynchronizedSendQuoteEmail(realPath,subject,freight_type,weight,departure,destination,email,null);
			    	    }
			    	}).start();
			
				 code = 200;
				 msg="OK";
			 }
		 }
		 catch(Exception e) {
			 
		 }
		Map map = new HashMap();
		map.put("code",code);
		map.put("msg",msg);
		renderJson(map);
	}
	public void vistor_contact_info_detail() {
		int code=0;
		String msg="ok";
		Record l = null;
		if(getPara("id")!=null && !"".equals(getPara("id"))) {
			int id= getParaToInt("id");
		
			l = Db.findFirst("select a.id,a.name,a.email,a.subject,a.message,b.atts_name,b.atts_url from contact a "
					+ "left join (select contact_id,group_concat(name) as atts_name,group_concat(url) as atts_url "
					+ "from contact_att GROUP BY contact_id) b on a.id=b.contact_id where id=?",id);
			
			
				if(l.getStr("atts_name")!=null) {
					String atts_name = l.getStr("atts_name");
					String atts_url = l.getStr("atts_url");
					String[] atts_name_arr = atts_name.split(",");
					String[] atts_url_arr = atts_url.split(",");
					for(int j=0;j<atts_name_arr.length;j++) {
						atts_name_arr[j] = "<a href='"+atts_url_arr[j]+"'>"+atts_name_arr[j]+"</a>";
						//System.out.println(atts_name_arr[j]);
					}
		           l.set("atts", String.join(",", atts_name_arr));
				}
	        
			
			
		}
		Map map = new HashMap();
		map.put("code",code);
		map.put("msg",msg);
		map.put("data", l);
		renderJson(map);
	}
	public void newsletter_excel() {
		int code=0;
		String msg="Error";
		List<Record> l = Db.find("select a.id,a.email,CASE a.channel WHEN 1 THEN 'Website' WHEN 2 THEN 'Wechat Mini Program' WHEN 3 THEN 'App' ELSE 'OTHERS' END AS channel,date_format(a.create_time,'%Y-%m-%d') create_date from newsletter a order by a.id desc");
		
		renderJson(l);
	}
	public void newsletter_list() {
		int code=0;
		String msg="Error";
		int limit= getParaToInt("limit");
		int page = getParaToInt("page");
		int start = (page-1)*limit;
		
		int total = Db.queryLong("select count(*) from newsletter").intValue();
				
		List<Record> l = Db.find("select a.id,a.email,CASE a.channel WHEN 1 THEN 'Website' WHEN 2 THEN 'Wechat Mini Program' WHEN 3 THEN 'App' ELSE 'OTHERS' END AS channel,date_format(a.create_time,'%Y-%m-%d') create_date from newsletter a order by a.id desc limit ?,?",start,limit);
		
		
		Map map = new HashMap();
		map.put("code",code);
		map.put("msg",msg);
		map.put("count", total);
		map.put("data", l);
		renderJson(map);
	}
	@ClearInterceptor(ClearLayer.ALL)
	public void newsletter_subscribe() {
		int code=0;
		String msg="Error";
		
		//System.out.println("upfiles size:"+upfiles.size());
		
		 try {
		 
			 String email = getPara("email"); 
			 if(email!=null && !"".equals(email)) {
				 Db.update("insert into newsletter (email,channel) values (?,1)",email);
			
				 code = 200;
				 msg="OK";
			 }
		 }
		 catch(Exception e) {
			 
		 }
		 
		/*
		 * if(count>0){ code=1; msg = "OK"; } else{ msg = "Error.^-^"; }
		 */
		
		Map map = new HashMap();
		map.put("code",code);
		map.put("msg",msg);
		renderJson(map);
	}
	@ClearInterceptor(ClearLayer.ALL)
	public void news_detail() {
		int code=0;
		String msg="ok";
		Record l = null;
		if(getPara("id")!=null && !"".equals(getPara("id"))) {
			int id= getParaToInt("id");
		
			l = Db.findFirst("select id,title_img,title,txt,date_format(release_date,'%d/%m/%Y') release_date from content a where id=?",id);
			
			
		}
		Map map = new HashMap();
		map.put("code",code);
		map.put("msg",msg);
		map.put("data", l);
		renderJson(map);
	}
	@ClearInterceptor(ClearLayer.ALL)
	public void news_list() {
		int code=0;
		String msg="ok";
		/*
		 * int limit= getParaToInt("limit"); int page = getParaToInt("page"); int start
		 * = (page-1)*limit;
		 */
		
		//int total = Db.queryLong("select count(*) from contact").intValue();
				
		List<Record> l = Db.find("select id,title_img,title,txt,date_format(release_date,'%d/%m/%Y') release_date from content a where title_img!='' order by a.id desc");
		
		for(int i = 0;i < l.size(); i ++){
			if(l.get(i).getStr("txt")!=null) {
				String txt = l.get(i).getStr("txt");
	           l.get(i).set("txt", StrUtil.getSubString(Html2PlainText.convert(txt),200));
			}
        }
		Map map = new HashMap();
		map.put("code",code);
		map.put("msg",msg);
		map.put("last_page", 2);
		map.put("list_page", 1000);
		map.put("data", l);
		renderJson(map);
	}
	
	public void vistor_contact_info_list() {
		int code=0;
		String msg="Error";
		int limit= getParaToInt("limit");
		int page = getParaToInt("page");
		int start = (page-1)*limit;
		
		int total = Db.queryLong("select count(*) from contact").intValue();
				
		List<Record> l = Db.find("select a.id,a.name,a.email,a.subject,a.message,b.atts_name,b.atts_url from contact a "
				+ "left join (select contact_id,group_concat(name) as atts_name,group_concat(url) as atts_url "
				+ "from contact_att GROUP BY contact_id) b on a.id=b.contact_id order by a.id desc limit ?,?",start,limit);
		
		/*
		 * for(Record r:l) { System.out.println(r.get("atts")); }
		 */
		for(int i = 0;i < l.size(); i ++){
			if(l.get(i).getStr("atts_name")!=null) {
				String atts_name = l.get(i).getStr("atts_name");
				String atts_url = l.get(i).getStr("atts_url");
				String[] atts_name_arr = atts_name.split(",");
				String[] atts_url_arr = atts_url.split(",");
				for(int j=0;j<atts_name_arr.length;j++) {
					atts_name_arr[j] = "<a href='"+atts_url_arr[j]+"'>"+atts_name_arr[j]+"</a>";
					//System.out.println(atts_name_arr[j]);
				}
	           l.get(i).set("atts", String.join(",", atts_name_arr));
			}
        }
		Map map = new HashMap();
		map.put("code",code);
		map.put("msg",msg);
		map.put("count", total);
		map.put("data", l);
		renderJson(map);
	}
	@ClearInterceptor(ClearLayer.ALL)
	public void contact() {
		int code=0;
		String msg="Error";
		
		//System.out.println("upfiles size:"+upfiles.size());
		
		 try {
		 
		StringBuffer files=new StringBuffer();
		List<UploadFile> upfiles = this.getFiles();
		
		
		final String name = getPara("name"); 
		final String email = getPara("email"); 
		final String subject = getPara("subject"); 
		  final String message = getPara("message"); 
		  int count = Db.update("insert into contact (name,email,subject,message) values (?,?,?,?)",name,email,subject,message);
		  int id = Db.queryInt("select max(id) from contact");
		  
		String savefilename="";
		String filedataFileName ="";
		ArrayList<String> list = new ArrayList<String>();
		int i=0;
		final String realPath = this.getRequest().getRealPath("/");
		System.out.println("realPath:"+realPath);
		for(UploadFile upfile:upfiles) {
			if(upfile!=null){
				
				
				String path=ym.format(new Date());
				File dir = new File(realPath+uploadroot+path);
				if(!dir.exists()){
					dir.mkdir();
				}
				File file = upfile.getFile();
				filedataFileName = upfile.getOriginalFileName();
				savefilename = uploadroot+path+"/"+ sf.format(new Date())+"_"+i+ filedataFileName.substring(filedataFileName.lastIndexOf("."));
				if(file != null){
					upfile.getFile().renameTo(new File(realPath + savefilename));
					files.append(realPath+savefilename);
					list.add(realPath+savefilename);
				}
				 Db.update("insert into contact_att(contact_id,name,url,priority) values (?,?,?,?)",id,filedataFileName,savefilename,i);
				
			}
			i++;
		}
		//String mail_subject = "A New Website Vistor Contact Info";
    	//String htmlbody = "hehehaha";
    	final String[] atts = list.toArray(new String[list.size()]);
    	new Thread(new MailRun(realPath, name,email,subject, message, atts) {

    	    @Override
    	    public void run() {
    	    	SendMail.asynchronizedSendEmail(realPath, name,email,subject, message, atts);
    	    }
    	}).start();
		
		msg="OK";
		 }
		 catch(Exception e) {
			 
		 }
		 
		/*
		 * if(count>0){ code=1; msg = "OK"; } else{ msg = "Error.^-^"; }
		 */
		
		Map map = new HashMap();
		map.put("code",code);
		map.put("msg",msg);
		renderJson(map);
	}
	
}
