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
import com.cms.admin.content.Content;
import com.cms.admin.content.ContentCount;
import com.cms.admin.goods.Goods;
import com.cms.admin.user.User;
import com.cms.util.DateFmt;
import com.cms.util.MailRun;
import com.cms.util.Md5;
import com.cms.util.Paginable;
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


public class CommonController extends Controller {
	private static SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmsssS");
	private static SimpleDateFormat ym = new SimpleDateFormat("yyyyMM");
	private static String uploadroot = "/upload/";
	
	public void login(){
		boolean login=false;
		/*String cpatcha = getPara("vcode");
		String kaptcha = getSessionAttr(Constants.KAPTCHA_SESSION_KEY);
		if(!cpatcha.equals(kaptcha)){
			System.out.println(kaptcha+"====+++++++"+cpatcha);
			setAttr("resultMsg", "验证码错误");
			forwardAction("/index");
		}
		else{*/
			String user = getPara("user");
			//登录时候防止sql注入 替换字符
			if(user!=null && !"".equals(user)){
				user = user.replaceAll("'", "‘");
				user = user.replaceAll(";", "；");
			}
			String pwd = getPara("pwd");
			List<User> list = User.dao.find("select u.* from user u where (username=? or nick=? or mobile=? or qq=? or email=?) and pwd=?",user,user,user,user,user,Md5.encodePassword(pwd));
			if(list.size()==1){
				// 放数据至session
				setSessionAttr("user", list.get(0));
				login=true;
				/*// 取数据于session
				User loginUser = getSessionAttr("user");
				System.out.println(loginUser.getInt("role_id"));
				// 删除session中的属性
				removeSessionAttr("loginUser");
				// 得到HttpSession
				HttpSession session = getSession();*/
			}
			//String vcode = (getPara("vcode")==null)?"":getPara("vcode").toUpperCase();
			//boolean vcodeOk = CaptchaRender.validate(this, vcode, RANDOM_CODE_KEY);
			if (login) {
				String furl = (getSessionAttr("furl")==null)?"/m":getSessionAttr("furl").toString();
				redirect(furl);
			}else{
				setAttr("code", 0);
				setAttr("msg", "登录信息有误，请更正后重试^-^");
				render("/WEB-INF/f/c/msg.html");
			}
		//}
	}
	
	public void logout(){
		// 删除session中的属性
		removeSessionAttr("user");
		String furl = (getSessionAttr("furl")==null)?"/":getSessionAttr("furl").toString();
		//redirect(furl);
		index();
	}
	public void index() {
		//URL参数样式 channelID_pageNo-contentID-contentPageNo
		String channelPara = getPara(0);
		String channelPath = null;
		String pageNo = null;
			
		//channelID_pageNo处理
		if(channelPara != null){
			if(channelPara.indexOf("_") == -1){
				channelPath = channelPara;
			}
			else{
				channelPath = channelPara.substring(0,channelPara.indexOf('_'));
				pageNo = channelPara.substring(channelPara.indexOf('_')+1,channelPara.length());
			}
		}
		
		String contentId = getPara(1);
		String contentPageNo=getPara(2);
		//处理翻页数据
		if(contentPageNo == null){
			contentPageNo = "1";
		}else {
			if(contentPageNo.indexOf("_") == -1){
			}
			else{
				contentPageNo = contentPageNo.split("_")[1] == null? "1":contentPageNo.split("_")[1];
			}
		} 
		
		int cpNo = Integer.parseInt(contentPageNo);
		//非首页
		if(channelPath!=null && !channelPath.equals("index")){
			//取得栏目对象
			Channel channel = Channel.dao.findFirst("select * from channel where path=?",channelPath);
			
			setAttr("channel",channel);
			if(pageNo != null){
				setAttr("pageNo",pageNo);
			}
			/*if(channelPath.equals("dckf") && getPara("contentId") !=null){
				setAttr("contentId",getPara("contentId"));
				render("/WEB-INF/t/content/land_detail.html");
				return;
			}*/
			//栏目页
			if(contentId==null){
				//取得栏目属性中的栏目模板地址
				String tplChannel = channel.getStr("tpl_channel");
				if(!"".equals(tplChannel) && tplChannel!=null){
					//处理访问路径所带的参数  方便前台展示
					if(getParaMap().size() > 0) {
						Iterator<String> it = getParaMap().keySet().iterator();
						for(int i=0;i<getParaMap().size();i++){
							String paraName = it.next();
							setAttr(paraName,getParaMap().get(paraName)[0]); 
						}
					}

					render("/WEB-INF/f"+tplChannel);
				}
				else{
					renderText("未设定栏目模板");
				}
			}
			//内容详细页
			else{
					//点击文章内容时候要增加点击量
					if(contentId != null){
						contentClickUp(Integer.valueOf(contentId));
					}
					//取得内容对象
					Content content = Content.dao.findById(contentId);
					setAttr("content",content);
					//取得作者信息
					User owner = User.dao.findById(content.getInt("creator"));
					setAttr("owner",owner);
					//取得评论列表
					List<Record> comments = Db.find("select a.id,a.uid,a.content_id,a.txt,a.release_date,b.avatar,b.nick from comment a,user b " +
							"where a.uid=b.id and a.content_id=?",contentId);
					setAttr("comments",comments);
					
					
					setAttr("channelPath",channelPath);
					setAttr("channelName", channel.getStr("name"));
					
					procPreorNextContent(channelPath,contentId);
					
					//获取文章内容
					String txt = content.getStr("txt");
					Paginable pagination = new SimplePage(cpNo, 1, StringUtils.countMatches(txt, "<p>[NextPage]") + 1);
					setAttr("pagination",pagination);
					txt = StringUtils.getTxtByNo(txt, cpNo);
					setAttr("txt",txt);

				//取得栏目属性中的内容模板地址  
				//这段代码公用     查找模板转向模板页的  不能写在分支语句里
				String tplContent = channel.getStr("tpl_content");
				if(!"".equals(tplContent) && tplContent!=null){
					render("/WEB-INF/f"+tplContent);
				}
				else{
					//待内容详细页出来后，此处需要修改，未设定内容模板则显示默认模板
					renderText("未设定内容模板");
				}
			}
		}
		//首页
		else
		{   
			//render("/WEB-INF/f/c/i.html");
			redirect("/f/index.html");
			//forwardAction("/admin");
		}
		
	}
	public void owner() {
		User owner = User.dao.findById(getPara());
		setAttr("owner",owner);
		setAttr("articles",
				Db.find("select a.id,b.path,a.title,a.release_date from content a,channel b where a.channel_id=b.id and a.creator=? order by a.id desc",
						owner.getInt("id")));
		render("/WEB-INF/f/c/owner.html");
	}
	public void kaihu() {
		render("/WEB-INF/f/c/kaihu.html");
	}
	public void r() {
		render("/WEB-INF/f/c/r.html");
	}
	@Before(GlobalInterceptor.class)
	public void l() {
		render("/WEB-INF/f/c/l.html");
	}
	public void rs() {
		String nick = getPara("user.nick");
		String mobile = getPara("user.mobile");
		String qq = getPara("user.qq");
		String email = getPara("user.email");
		String pwd = getPara("user.pwd");
		List<User> list = User.dao.find("select * from user where nick=? or mobile=? or qq=? or email=?",nick,mobile,qq,email);
		if(list.size()>0){
			setAttr("code", 0);
			setAttr("msg", "昵称(用户名)/手机/qq/Email已存在！请更正。");
			render("/WEB-INF/f/c/msg.html");
		}
		else{
			getModel(User.class).set("pwd", Md5.encodePassword(pwd)).save();
			setAttr("code", 1);
			setAttr("msg", "注册成功！");
			render("/WEB-INF/f/c/msg.html");
		}
	}
	@Before(MInterceptor.class)
	public void comment() {
		int code=0;
		String msg="";
		int uid = getParaToInt("uid");
		String cid = getPara("cid");
		String txt = getPara("txt");
		List<User> list = null;
		User loginUser = getSessionAttr("user");
		if(uid!=loginUser.getInt("id")){
			msg = "非法访问";
		}
		else{
			int count = Db.update("insert into comment (content_id,uid,txt,release_date) values (?,?,?,?)",
					cid,uid,txt,DateFmt.addLongDays(0));
			if(count>0){
				code=1;
				msg = "评论成功。^-^";
			}
			else{
				msg = "未知错误，请联系系统管理员。^-^";
			}
		}
		Map map = new HashMap();
		  map.put("code",code);
		  map.put("msg",msg);
		  renderJson(map);
	}
	public void uid() {
		int code=0;
		String msg="";
		String uid = getPara("uid");
		List<User> list = null;
		User loginUser = getSessionAttr("user");
		if(loginUser==null){
			list = User.dao.find("select * from user where uid=?",uid);
		}
		else{
			list = User.dao.find("select * from user where uid=? and id!=?",uid,loginUser.getInt("id"));
		}
		if(list.size()>0){
			code=1;
			msg = "已存在相同的身份证号^-^";
		}
		Map map = new HashMap();
		map.put("code",code);
		map.put("msg",msg);
		renderJson(map);
	}
	public void card() {
		int code=0;
		String msg="";
		String card = getPara("card");
		List<User> list = null;
		User loginUser = getSessionAttr("user");
		if(loginUser==null){
			list = User.dao.find("select * from user where card=?",card);
		}
		else{
			list = User.dao.find("select * from user where card=? and id!=?",card,loginUser.getInt("id"));
		}
		if(list.size()>0){
			code=1;
			msg = "已存在相同的银行卡号^-^";
		}
		Map map = new HashMap();
		map.put("code",code);
		map.put("msg",msg);
		renderJson(map);
	}
	public void mobile() {
		int code=0;
		String msg="";
		String mobile = getPara("mobile");
		List<User> list = null;
		User loginUser = getSessionAttr("user");
		if(loginUser==null){
			list = User.dao.find("select * from user where mobile=?",mobile);
		}
		else{
			list = User.dao.find("select * from user where mobile=? and id!=?",mobile,loginUser.getInt("id"));
		}
		if(list.size()>0){
			code=1;
			msg = "已存在相同的手机号^-^";
		}
		Map map = new HashMap();
		map.put("code",code);
		map.put("msg",msg);
		renderJson(map);
	}
	public void qq() {
		int code=0;
		String msg="";
		String qq = getPara("qq");
		List<User> list = null;
		User loginUser = getSessionAttr("user");
		if(loginUser==null){
			list = User.dao.find("select * from user where qq=?",qq);
		}
		else{
			list = User.dao.find("select * from user where qq=? and id!=?",qq,loginUser.getInt("id"));
		}
		if(list.size()>0){
			code=2;
			msg = "已存在相同的QQ号码^-^";
		}
		Map map = new HashMap();
		map.put("code",code);
		map.put("msg",msg);
		renderJson(map);
	}
	public void email() {
		int code=0;
		String msg="";
		String email = getPara("email");
		List<User> list = null;
		User loginUser = getSessionAttr("user");
		if(loginUser==null){
			list = User.dao.find("select * from user where email=?",email);
		}
		else{
			list = User.dao.find("select * from user where email=? and id!=?",email,loginUser.getInt("id"));
		}
		if(list.size()>0){
			code=3;
			msg = "已存在相同的Email^-^";
		}
		Map map = new HashMap();
		map.put("code",code);
		map.put("msg",msg);
		renderJson(map);
	}
	public void nick() {
		int code=0;
		String msg="";
		String nick = getPara("nick");
		List<User> list = User.dao.find("select * from user where nick=?",nick);
		if(list.size()>0){
			code=4;
			msg = "已存在相同的昵称^-^";
		}
		Map map = new HashMap();
		map.put("code",code);
		map.put("msg",msg);
		renderJson(map);
	}
	//处理上一篇下一篇的文章
	private void procPreorNextContent(String channelPath,String contentId){
		//取出上一篇 和下一篇文章
		List<Content> content_pre = Content.dao.find("select * from content where id < "+contentId+"" +
													" and channel_id in (select id from channel where " +
													" path = '"+channelPath+"')" +
													" order by id");
		setAttr("content_pre",(content_pre.size() == 0 ? null :content_pre.get(content_pre.size()-1)));
		
		List<Content> content_next = Content.dao.find("select * from content where id > "+contentId+"" +
													" and channel_id in (select id from channel where " +
													" path = '"+channelPath+"')" +
													" order by id");
		setAttr("content_next",(content_next.size()== 0 ? null: content_next.get(0)));
	}
	
	public void c() {
		render("/WEB-INF/t/c.html");
	}
	public void n() {
		render("/WEB-INF/t/n.html");
	}
	
	
	/**
	 * 下载文件
	 * @param filePath --文件完整路径
	 * @param response --HttpServletResponse对象
	 */
	private static void downloadFile(
			String filePath, 
			HttpServletResponse response) {

		String fileName = ""; //文件名，输出到用户的下载对话框
		//从文件完整路径中提取文件名，并进行编码转换，防止不能正确显示中文名
		try {
			if(filePath.lastIndexOf("/") > 0) {
				fileName = new String(filePath.substring(filePath.lastIndexOf("/")+1, filePath.length()).getBytes("UTF-8"), "ISO8859-1");
			}else if(filePath.lastIndexOf("\\") > 0) {
				fileName = new String(filePath.substring(filePath.lastIndexOf("\\")+1, filePath.length()).getBytes("UTF-8"), "ISO8859-1");
			}

		}catch(Exception e) {}
		//打开指定文件的流信息
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(new File(filePath));
		}catch(FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "文件找不到");
			return;
		}
		//设置响应头和保存文件名 
		response.setContentType("APPLICATION/OCTET-STREAM"); 
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		//写出流信息
		int b = 0;
		try {
			PrintWriter out = response.getWriter();
			while((b=fs.read())!=-1) {
				out.write(b);
			}
			fs.close();
			out.close();
			System.out.println("文件下载完毕.");
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("下载文件失败!");
		}
	}
	
	//文章点击的时候对总点击量进行+1操作
	private boolean contentClickUp(int contentId){
		boolean flag=false;
		ContentCount contentCount = ContentCount.dao.findFirst(" select * from content_count where content_id = ? ",contentId);
		if(contentCount!=null){
			flag = contentCount.set("totalClick", contentCount.getInt("totalClick")+1).update();
		}
		return flag;
	}
}
