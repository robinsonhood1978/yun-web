package com.cms.front.common;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cms.admin.channel.Channel;
import com.cms.admin.content.ContentCount;
import com.cms.admin.user.User;
import com.cms.front.entity.Content;
import com.cms.util.DateFmt;
import com.cms.util.ImageUtils;
import com.cms.util.Md5;
import com.cms.util.StrUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;

@Before(MInterceptor.class)
public class MController extends Controller {
	private static SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmsss");
	private static SimpleDateFormat ym = new SimpleDateFormat("yyyyMM");
	private static String uploadroot = "/upload/";
	public void index() {
		User loginUser = getSessionAttr("user");
		if(loginUser==null){
			forwardAction("/l");
		}
		else{
			Page<Content> page = null;
			// 当前页
			StringBuffer sql =new StringBuffer("from content a,channel b where a.channel_id=b.id and a.creator="+loginUser.getInt("id"));
			//页数
			int pageNum = 1;
			if (getParaToInt("pageNum") != null) {
				pageNum = getParaToInt("pageNum");
			}
			page = Content.dao.paginate(pageNum, 10, "select a.*,b.name as channel_name",sql.toString());
			//拿到级联数据
			/*if(page!=null && page.getList()!=null && page.getList().size()>0){
				for (int i = 0; i < page.getList().size(); i++) {
					page.getList().get(i).set("channel_id", Channel.dao.findById(page.getList().get(i).get("channel_id")));
					page.getList().get(i).set("creator", User.dao.findById(page.getList().get(i).get("creator")));
				}
			}*/
			setAttr("contentPage", page);
			render("/WEB-INF/m/m.html");
		}
	}
	public void write() {
		render("/WEB-INF/m/write.html");
	}
	public void view() {
		int id = getParaToInt("id");
		Content c = Content.dao.findById(id);
		setAttr("content_att", Db.find(
				" select * from content_att where content_id = ? ",id));
		setAttr("creator_name", User.dao.findById(c.getInt("creator")).getStr("nick"));
		setAttr("content", c);
		
		render("/WEB-INF/m/view.html");
	}
	public void edit() {
		int id = getParaToInt("id");
		Content c = Content.dao.findById(id);
		setAttr("content", c);
		
		render("/WEB-INF/m/edit.html");
	}
	public void del() {
		
		boolean succeed = Db.tx(new IAtom() {
			public boolean run() throws SQLException {
				int id = getParaToInt("id");
				Db.update("delete from content_channel where content_id=?",id);
				Db.update("delete from content_count where content_id=?",id);
				Content.dao.deleteById(id);
				return true;
			}
		});
		int code=0;
		String msg = "";
		if (succeed) {
			code=1;
			msg="信息删除成功！";
		}
		else{
			msg = "信息删除失败，请重试或联系管理员。";
		}
		setAttr("code", code);
		setAttr("msg", msg);
		render("/WEB-INF/m/msg.html");
	}
	public void person() {
		render("/WEB-INF/m/person.html");
	}
	public void upinfo() {
		String mobile = getPara("user.mobile");
		String qq = getPara("user.qq");
		String email = getPara("user.email");
		User loginUser = getSessionAttr("user");
		List<User> list = User.dao.find("select * from user where (mobile=? or qq=? or email=?) and id!=?",mobile,qq,email,loginUser.getInt("id"));
		int code=0;
		String msg = "";
		if(list.size()>0){
			msg="手机/qq/Email已存在！请更正。";
		}
		else{
			boolean boo = getModel(User.class).set("id", loginUser.getInt("id")).update();
			if(boo){
				setSessionAttr("user", User.dao.findById(loginUser.getInt("id")));
				code=1;
				msg="注册信息修改成功！";
			}
			else{
				msg = "信息修改失败，请联系管理员。";
			}
		}
		setAttr("code", code);
		setAttr("msg", msg);
		render("/WEB-INF/m/msg.html");
	}
	public void save_kaihu() {
		String uid = getPara("user.uid");
		String card = getPara("user.card");
		User loginUser = getSessionAttr("user");
		List<User> list = User.dao.find("select * from user where (uid=? or card=?) and id!=?",uid,card,loginUser.getInt("id"));
		int code=0;
		String msg = "";
		if(list.size()>0){
			msg="身份证/银行卡已存在！请更正。";
		}
		else{
			boolean boo = getModel(User.class).set("id", loginUser.getInt("id")).set("kaihu_status", 1).update();
			if(boo){
				setSessionAttr("user", User.dao.findById(loginUser.getInt("id")));
				code=1;
				msg="开户信息修改成功！";
			}
			else{
				msg = "开户信息修改失败，请联系管理员。";
			}
		}
		setAttr("code", code);
		setAttr("msg", msg);
		render("/WEB-INF/m/msg.html");
	}
	public void pwd() {
		render("/WEB-INF/m/pwd.html");
	}
	public void uppwd() {
		int code=0;
		String msg = "";
		User loginUser = getSessionAttr("user");
		String oldpwd = getPara("user.oldpwd");
		String pwd = getPara("user.pwd");
		if(loginUser.getStr("pwd").equals(Md5.encodePassword(oldpwd))){
			boolean boo = loginUser.set("pwd", Md5.encodePassword(pwd)).update();
			if(boo){
				code=1;
				msg="密码修改成功！";
			}
			else{
				msg = "密码修改失败，请联系管理员。";
			}
		}
		else{
			msg = "原密码有误，请重新输入。";
		}
		
		setAttr("code", code);
		setAttr("msg", msg);
		render("/WEB-INF/m/msg.html");
	}
	public void publish() {
		int code=0;
		String msg = "";
		final User loginUser = getSessionAttr("user");
		boolean succeed = Db.tx(new IAtom() {
			public boolean run() throws SQLException {
				getModel(Content.class)
				.set("release_date",DateFmt.addLongDays(0))
				.set("creator", loginUser.getInt("id")).save();
				//拿到刚刚添加文字的ID
				int contentId = Db.queryInt("select max(id) from content");
				Db.save("content_channel", new Record()
				.set("content_id", contentId)
				.set("channel_id",getParaToInt("content.channel_id")));
				//增添文章的点击量
				new ContentCount().set("content_id", contentId).save();
				return true;
			}
		});
		if(succeed){
			code=1;
			msg="博文发表成功！";
		}
		else{
			msg = "博文发表失败，请重试或联系管理员。";
		}
		
		setAttr("code", code);
		setAttr("msg", msg);
		render("/WEB-INF/m/msg.html");
	}
	public void update_blog() {
		int code=0;
		String msg = "";
		boolean succeed = Db.tx(new IAtom() {
			public boolean run() throws SQLException {
				int content_id=getParaToInt("content.id");
				int channel_id=getParaToInt("content.channel_id");
				Db.update("update content_channel set channel_id=? where content_id=?",channel_id,content_id);
				getModel(Content.class).update();
				return true;
			}
		});
		if(succeed){
			code=1;
			msg="博文编辑成功！";
		}
		else{
			msg = "博文编辑失败，请重试或联系管理员。";
		}
		
		setAttr("code", code);
		setAttr("msg", msg);
		render("/WEB-INF/m/msg.html");
	}
	public void save_avatar() {
		int code=0;
		String msg = "";
		User loginUser = getSessionAttr("user");
		String avatar = getPara("avatar");
		String realPath = this.getRequest().getRealPath("/");
		
		String path=ym.format(new Date());
		File dir = new File(realPath+uploadroot+path);
		if(!dir.exists()){
			dir.mkdir();
		}
		String file=uploadroot+path+"/"+ sf.format(new Date())+".jpg";
		ImageUtils.basePngtoJpg(avatar,realPath+file);
		
			boolean boo = loginUser.set("avatar", file).update();
			if(boo){
				code=1;
				msg="头像修改成功！";
			}
			else{
				msg = "头像修改失败，请重试或联系管理员。";
			}
		
		
		setAttr("code", code);
		setAttr("msg", msg);
		render("/WEB-INF/m/msg.html");
	}
	public void file() {
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
		Map map = new HashMap();
		map.put("status","ok");
		map.put("url",savefilename);
		renderJson(map);

	}
	public void avatar() {
		render("/WEB-INF/m/avatar.html");
	}
	public void kaihu() {
		render("/WEB-INF/m/kaihu.html");
	}
}
