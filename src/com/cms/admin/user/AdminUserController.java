package com.cms.admin.user;

import java.util.List;


import com.cms.admin.role.Role;
import com.cms.util.Md5;
import com.jfinal.aop.Before;
import com.jfinal.aop.ClearInterceptor;
import com.jfinal.aop.ClearLayer;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;

@Before(UserInterceptor.class)
public class AdminUserController extends Controller {
	public void index() {
		setAttr("userPage", User.dao.paginate(getParaToInt(0, 1), 30, "select u.*,r.name as role_name", "from user u left join role r on u.role_id=r.id order by u.id desc"));
		render("user.html");
	}
	
	public void add() {
		setAttr("roleList", Role.dao.find("select * from role"));
	}
	@ClearInterceptor
	public void person() {
		setAttr("user", User.dao.findById(getSessionAttr("userId")));
		setAttr("pwdMsg", "不修改密码请留空。");
	}
	
	@ClearInterceptor
	@Before(PersonValidator.class)
	public void pwd() {
		String id = getPara("user.id");
		String pwd = getPara("user.pwd");
		String originPwd = getPara("origin_pwd");
		long i = Db.queryLong("select count(*) from user where id=? and pwd=?",
				id,Md5.encodePassword(originPwd)) ;
		if(i>0){
			if(pwd==""){
				pwd = User.dao.findById(id).getStr("pwd");
				getModel(User.class).set("pwd", pwd).update();
			}else{
				getModel(User.class).set("pwd", Md5.encodePassword(pwd)).update();
			}
			setAttr("resultMsg", "个人信息编辑成功！");
		}
		else{
			setAttr("resultMsg", "原密码错误，个人信息编辑失败！");
		}
		forwardAction("/admin/user/person");
	}
	@Before(UserValidator.class)
	public void save() {
		String username = getPara("user.username");
		String pwd = getPara("user.pwd");
		List<User> list = User.dao.find("select * from user where username=?",username);
		if(list.size()>0){
			setAttr("usernameMsg", "用户名已存在！请更正。");
			forwardAction("/admin/user/add");
		}
		else{
			getModel(User.class).set("pwd", Md5.encodePassword(pwd)).save();
			setAttr("resultMsg", "用户添加成功！");
			forwardAction("/admin/user");
		}
	}
	
	public void edit() {
		setAttr("roleList", Role.dao.find("select * from role"));
		setAttr("user", User.dao.findById(getParaToInt()));
		setAttr("pwdMsg", "不修改密码请留空。");
	}
	public void kaihu() {
		Db.update("update user set kaihu_status=2 where id=?",getParaToInt());
		setAttr("resultMsg", "开户申请状态变更成功！");
		forwardAction("/admin/user");
	}
	public void view() {
		User user = User.dao.findById(getParaToInt());
		setAttr("roleName", Role.dao.findById(user.getInt("role_id")).getStr("name"));
		setAttr("user", user);
	}
	@ClearInterceptor
	public void self() {
		User user = User.dao.findById(getParaToInt());
		setAttr("roleName", Role.dao.findById(user.getInt("role_id")).getStr("name"));
		setAttr("user", user);
	}
	
	@Before(UserEditValidator.class)
	public void update() {
		String id = getPara("user.id");
		String username = getPara("user.username");
		String pwd = getPara("user.pwd");
		
		List<User> list = User.dao.find("select * from user where username=? and id!=?",username,id);
		if(list.size()>0){
			setAttr("nameMsg", "用户名已存在！请更正。");
			forwardAction("/admin/user/edit/"+id);
		}
		else{
			if(pwd==""){
				pwd = User.dao.findById(id).getStr("pwd");
				getModel(User.class).set("pwd", pwd).update();
			}else{
				getModel(User.class).set("pwd", Md5.encodePassword(pwd)).update();
			}
			setAttr("resultMsg", "用户编辑成功！");
			forwardAction("/admin/user");
		}
	}
	
	public void delete() {
		Integer[] ids = getParaValuesToInt("ids");
		String strId = "";
		if(ids!=null && ids.length>0){
			for(Integer id:ids){
				strId += id+",";
			}
			strId = strId.substring(0, strId.length()-1);
			int i = Db.update("delete from user where id in ("+strId+")");
		}
		else{
			if(getParaToInt()>0)
				User.dao.deleteById(getParaToInt());
		}
		setAttr("resultMsg", "用户删除成功！");
		forwardAction("/admin/user");
	}
}


