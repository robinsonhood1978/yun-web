package com.cms.admin.common;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cms.admin.user.User;
import com.cms.front.common.MInterceptor;
import com.cms.util.Md5;
import com.cms.util.fck.UploadResponse;
import com.google.code.kaptcha.Constants;
import com.jfinal.aop.Before;
import com.jfinal.aop.ClearInterceptor;
import com.jfinal.aop.ClearLayer;
import com.jfinal.core.Controller;
import com.jfinal.ext.render.CaptchaRender;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.upload.UploadFile;

/**
 * CommonController
 */
@Before(AdminInterceptor.class)
public class AdminController extends Controller {
	
	private static SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmsss");
	private static SimpleDateFormat ym = new SimpleDateFormat("yyyyMM");
	private static String uploadroot = "/upload/";
	private static String root;
	private static final String RANDOM_CODE_KEY = "1";
	
	@ClearInterceptor(ClearLayer.ALL)
	public void index() {
		render("/admin/common/login.html");
		//renderError404();
	}
	
	public void employ(){
		String storeId = getPara("store_id");
		
		String sql = "select id,name from user";
		if(storeId!=null &&!"0".equals(storeId)){
			sql += " where store_id in ("+storeId+")";
		}
		sql += " order by id";
		System.out.println(sql);
		List<User> userList = User.dao.find(sql);
		String userStr = "";
		int count = userList.size();
		if(count>0){
			for(User user:userList){
				userStr += user.getInt("id")+":"+user.getStr("name")+"#";
			}
			userStr = userStr.substring(0,userStr.length()-1);
		}
		setAttr("count",count);
		setAttr("userList",userStr);
		renderJson();
	}
	public void main() {
		render("/admin/common/main.html");
	}
	public void top() {
		render("/admin/common/top.html");
	}
	@ClearInterceptor(ClearLayer.ALL)
	public void noright() {
		render("/admin/common/noright.html");
	}
	@ClearInterceptor(ClearLayer.ALL)
	public void welcome() {
		render("/admin/common/welcome.html");
	}
	public void left() {
		render("/admin/common/left.html");
	}
	@ClearInterceptor(ClearLayer.ALL)
	public void login(){
		boolean login=false;
		String cpatcha = getPara("vcode");
		String kaptcha = getSessionAttr(Constants.KAPTCHA_SESSION_KEY);
		/*if(!cpatcha.equals(kaptcha)){
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
				forwardAction("/admin/main");
			}else{
			    index();
			}
		//}
	}
	
	public void logout(){
		System.out.println(getSessionAttr("menu")+"--=-=============-----------");
		// 删除session中的属性
		removeSessionAttr("user");
		removeSessionAttr("menu");
		index();
	}
	@ClearInterceptor(ClearLayer.ALL)
	public void img() {
	    CaptchaRender img = new CaptchaRender(RANDOM_CODE_KEY);
	    render(img);
	}
	
	public void upload() throws Exception {
		responseInit(getResponse());
		String currentFolderStr = "/";
		String typeStr = "File";
		
		UploadResponse ur = doUpload(getRequest(), typeStr, currentFolderStr, null);
		PrintWriter out = getResponse().getWriter();
		out.print(ur);
		out.flush();
		out.close();
		renderNull();
	}
	
	private void responseInit(HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");
		response.setHeader("Cache-Control", "no-cache");
	}
	
	private UploadResponse doUpload(HttpServletRequest request, String typeStr,
			String currentFolderStr, Boolean mark) throws Exception {
		String savefilename="";
		String filedataFileName ="";
		StringBuffer files=new StringBuffer();
		UploadFile upfile = this.getFile();
		if(upfile!=null){
			String realPath = this.getRequest().getRealPath("/");
			
			String path=ym.format(new Date());
			File dir = new File(realPath+uploadroot+path);
			if(!dir.exists()){
				dir.mkdir();
			}
			File file = upfile.getFile();
			filedataFileName = upfile.getOriginalFileName();
			savefilename = uploadroot+path+"/"+ sf.format(new Date())+ filedataFileName.substring(filedataFileName.lastIndexOf("."));
			if(file != null){
				upfile.getFile().renameTo(new File(realPath + savefilename));
				files.append(realPath+savefilename);
			}
			
		}
		return UploadResponse.getOK(request, savefilename);
	}
}
