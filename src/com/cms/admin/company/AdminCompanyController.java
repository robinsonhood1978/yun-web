package com.cms.admin.company;

import java.util.List;

import com.cms.admin.role.Role;
import com.cms.admin.user.User;
import com.cms.util.Md5;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;

@Before(CompanyInterceptor.class)
public class AdminCompanyController extends Controller {
	public void index() {
		setAttr("companyPage", Company.dao.paginate(getParaToInt(0, 1), 10, "select *", "from company order by id asc"));
		render("company.html");
	}
	
	public void add() {
	}
	
	@Before(CompanyValidator.class)
	public void save() {
		String name = getPara("company.name");
		List<Company> list = Company.dao.find("select * from company where name=?",name);
		if(list.size()>0){
			setAttr("nameMsg", "公司名已存在！请更正。");
			forwardAction("/admin/company/add");
		}
		else{
			int actor = (getSessionAttr("user")==null)?1:((User)getSessionAttr("user")).getInt("id");
			getModel(Company.class).set("create_actor", actor).save();
			setAttr("resultMsg", "数据保存成功。");
			forwardAction("/admin/company");
		}
	}
	
	public void edit() {
		setAttr("roleList", Role.dao.find("select * from role"));
		setAttr("company", Company.dao.findById(getParaToInt()));
		setAttr("pwdMsg", "不修改密码请留空。");
	}
	
	@Before(CompanyValidator.class)
	public void update() {
		String id = getPara("company.id");
		String name = getPara("company.name");
		
		List<Company> list = Company.dao.find("select * from company where name=? and id!=?",name,id);
		if(list.size()>0){
			setAttr("nameMsg", "公司名已存在！请更正。");
			forwardAction("/admin/company/edit/"+id);
		}
		else{
			getModel(Company.class).update();
			setAttr("resultMsg", "数据保存成功。");
			forwardAction("/admin/company");
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
			Long num = Db.queryLong("select count(*) from goods where company_id in ("+strId+")"); 
			if(num>0){
				setAttr("resultMsg", "此厂商下有相关设备存在，删除失败，请删除设备后重试。");
			}
			else{
				int i = Db.update("delete from company where id in ("+strId+")");
				if(i>0)
					setAttr("resultMsg", "删除成功。");
			}
		}
		else{
			if(getParaToInt()>0){
				Long num = Db.queryLong("select count(*) from goods where company_id = "+getParaToInt()); 
				if(num>0){
					setAttr("resultMsg", "此厂商下有相关设备存在，删除失败，请删除设备后重试。");
				}
				else{
					Company.dao.deleteById(getParaToInt());
					setAttr("resultMsg", "删除成功。");
				}
			}
		}
		forwardAction("/admin/company");
	}
}


